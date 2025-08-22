package top.ribs.scguns.blockentity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Config;
import top.ribs.scguns.block.*;
import top.ribs.scguns.client.screen.BasicTurretMenu;
import top.ribs.scguns.client.screen.ShotgunTurretMenu;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.entity.projectile.turret.TurretProjectileEntity;
import top.ribs.scguns.init.*;
import top.ribs.scguns.item.EnemyLogItem;
import top.ribs.scguns.item.TeamLogItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageMuzzleFlash;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class BasicTurretBlockEntity extends BlockEntity implements MenuProvider {
    private static double TARGETING_RADIUS = 24.0f;
    private int cooldown = 25;
    public final ItemStackHandler itemHandler = new ItemStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            assert level != null;
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    LivingEntity target;
    private UUID ownerUUID;
    private String ownerName;
    private float yaw;
    private double smoothedTargetX;
    private double smoothedTargetZ;
    private float pitch;
    private static final float MAX_PITCH = 60.0F;
    private static final float MIN_PITCH = -25.0F;
    private double smoothedTargetY;
    private static final float POSITION_SMOOTHING_FACTOR = 0.2F;
    private static final float ROTATION_SPEED = 0.5F;
    public static final float RECOIL_MAX = 4.0F;
    private static final float RECOIL_SPEED = 0.3F;
    public float recoilPitchOffset = 0.0F;
    private static final double MINIMUM_FIRING_DISTANCE = 1.7;
    private static final int DAMAGE_INCREASE = 2;
    private static final double RANGE_INCREASE = 8.0;

    private float previousYaw;
    private float previousPitch;
    public static final float INACCURACY = 0.05F;
    private boolean hasFireRateModule;
    private boolean hasDamageModule;
    private boolean hasRangeModule;
    private boolean hasShellCatchingModule;
    public boolean disabled = false;
    public int disableCooldown = 0;
    public static final int MAX_DISABLE_TIME = 200; // 10 seconds


    public BasicTurretBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_TURRET.get(), pos, state);
    }


    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.basic_turret");
    }

    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {
        boolean hasTargetingModule = false;
        if (this.level != null) {
            for (Direction direction : Direction.values()) {
                BlockState blockState = this.level.getBlockState(this.worldPosition.relative(direction));
                if (blockState.getBlock() instanceof TurretTargetingBlock) {
                    hasTargetingModule = true;
                    break;
                }
            }
        }

        if (!hasTargetingModule) {
            if (this.level != null && !this.level.isClientSide) {
                player.sendSystemMessage(Component.translatable("message.scguns.turret_needs_targeting_module")
                        .withStyle(ChatFormatting.YELLOW));
            }
            return null;
        }

        return new BasicTurretMenu(id, playerInventory, this);
    }
    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof BasicTurretBlockEntity turret) {
            turret.hasFireRateModule = turret.isAdjacentToFireRateModule(level, pos);
            turret.hasDamageModule = turret.isAdjacentToDamageModule(level, pos);
            turret.hasRangeModule = turret.isAdjacentToRangeModule(level, pos);
            turret.hasShellCatchingModule = turret.isAdjacentToShellCatchingModule();

            int fireRateModifier = turret.hasFireRateModule ? 2 : 1;
            int damageModifier = turret.hasDamageModule ? DAMAGE_INCREASE : 0;
            double rangeModifier = turret.hasRangeModule ? RANGE_INCREASE : 0;

            if (turret.cooldown > 0) {
                turret.cooldown -= fireRateModifier;
            }
            turret.tickRecoil();

            if (turret.disabled) {
                turret.disableCooldown--;
                if (turret.disableCooldown <= 0) {
                    turret.disabled = false;
                    turret.disableCooldown = 0;
                }
                turret.resetToRestPosition();
            } else if (!state.getValue(BasicTurretBlock.POWERED)) {
                turret.updateTargetRange(rangeModifier);
                if (!turret.isTargetValid()) {
                    turret.target = null;
                }
                turret.findTarget(level, pos);
                turret.updateYaw();
                turret.updatePitch();

                if (turret.target != null && turret.cooldown <= 0 && turret.isReadyToFire()) {
                    TurretProjectileEntity.BulletType bulletType = turret.findAndConsumeAmmo();
                    if (bulletType != null) {
                        turret.fire(bulletType, damageModifier);
                        turret.cooldown = 25;
                    }
                }
            } else {
                turret.resetToRestPosition();
            }
        }
    }


    private void updateTargetRange(double rangeModifier) {
        TARGETING_RADIUS = 24.0f + (float)rangeModifier;
    }

    public void onHitByLightningProjectile() {
        this.disabled = true;
        this.disableCooldown = MAX_DISABLE_TIME;
        this.resetToRestPosition(); // Stop turret movement
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            spawnDisableParticles();
        }
    }
    private void spawnDisableParticles() {
        if (this.level instanceof ServerLevel serverLevel) {
            double x = this.worldPosition.getX() + 0.5;
            double y = this.worldPosition.getY() + 1.0;
            double z = this.worldPosition.getZ() + 0.5;

            int particleCount = 20;
            double spread = 0.5;

            for (int i = 0; i < particleCount; i++) {
                double offsetX = this.level.random.nextDouble() * spread - spread / 2;
                double offsetY = this.level.random.nextDouble() * spread;
                double offsetZ = this.level.random.nextDouble() * spread - spread / 2;

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        x + offsetX, y + offsetY, z + offsetZ,
                        1, 0, 0, 0, 0.05);
            }
            serverLevel.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }


    public boolean isReadyToFire() {
        if (this.target == null) return false;
        double dx = smoothedTargetX - (this.worldPosition.getX() + 0.5);
        double dy = smoothedTargetY - (this.worldPosition.getY() + 1.0);
        double dz = smoothedTargetZ - (this.worldPosition.getZ() + 0.5);
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Math.atan2(dx, dz) * (180 / Math.PI)) + 180;
        targetYaw = (targetYaw + 360) % 360;
        float targetPitch = (float) (Math.atan2(dy, horizontalDistance) * (180 / Math.PI));
        targetPitch = Mth.clamp(targetPitch, MIN_PITCH, MAX_PITCH);
        float yawDifference = Math.abs(targetYaw - this.yaw);
        if (yawDifference > 180) yawDifference = 360 - yawDifference;

        float pitchDifference = Math.abs(targetPitch - this.pitch);
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        if (distanceSquared < MINIMUM_FIRING_DISTANCE * MINIMUM_FIRING_DISTANCE) {
            return false;
        }

        return yawDifference < 2.0F && pitchDifference < 2.0F;
    }

    public void tickRecoil() {
        if (this.recoilPitchOffset > 0) {
            this.recoilPitchOffset -= RECOIL_SPEED;
            if (this.recoilPitchOffset < 0) {
                this.recoilPitchOffset = 0;
            }
        }
    }
    public float getRecoilPitchOffset() {
        return recoilPitchOffset;
    }

    private void resetToRestPosition() {
        this.target = null;
        float restingYaw = 0.0F;
        float restingPitch = -30.0F;
        this.previousYaw = this.yaw;
        this.previousPitch = this.pitch;
        float yawDifference = restingYaw - this.yaw;
        if (yawDifference > 180) yawDifference -= 360;
        else if (yawDifference < -180) yawDifference += 360;
        this.yaw += yawDifference * ROTATION_SPEED;
        this.yaw = this.yaw % 360.0F;
        if (this.yaw < 0) this.yaw += 360.0F;

        float pitchDifference = restingPitch - this.pitch;
        this.pitch += pitchDifference * ROTATION_SPEED;
        this.smoothedTargetX = 0;
        this.smoothedTargetY = 0;
        this.smoothedTargetZ = 0;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public float getPitch() {
        return this.pitch;
    }
    public void fire(TurretProjectileEntity.BulletType bulletType, int damageModifier) {
        if (this.level == null || this.target == null) {
            return;
        }

        float yaw = this.getYaw();
        float pitch = this.getPitch();

        Vec3 muzzlePos = getMuzzlePosition(yaw, pitch);

        if (!this.level.isClientSide) {
            PacketHandler.getPlayChannel().sendToTrackingChunk(() -> level.getChunkAt(worldPosition),
                    new S2CMessageMuzzleFlash(muzzlePos, yaw, pitch));
        }
        this.level.playSound(null, this.worldPosition, ModSounds.IRON_RIFLE_FIRE.get(), SoundSource.BLOCKS, 0.7F, 0.7F);

        Vec3 targetPos = new Vec3(target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ());
        Vec3 direction = targetPos.subtract(muzzlePos).normalize();
        direction = direction.add(
                this.level.random.triangle(0, INACCURACY),
                this.level.random.triangle(0, INACCURACY),
                this.level.random.triangle(0, INACCURACY)
        ).normalize();
        TurretProjectileEntity projectile = getTurretProjectileEntity(bulletType, direction.x, direction.y, direction.z);
        projectile.setPos(muzzlePos.x, muzzlePos.y, muzzlePos.z);
        double baseDamage = Config.COMMON.turret.bulletDamage.get(bulletType).get();
        if (Config.COMMON.turret.enableDamageScaling.get()) {
            long daysInWorld = this.level.getDayTime() / 24000L;
            double scalingRate = Config.COMMON.turret.damageScalingRate.get();
            double maxDamage = Config.COMMON.turret.maxScaledDamage.get();
            baseDamage = Math.min(baseDamage + (scalingRate * daysInWorld), maxDamage);
        }
        double finalDamage = baseDamage + damageModifier;
        projectile.setBaseDamage(finalDamage);

        this.level.addFreshEntity(projectile);
        this.recoilPitchOffset = RECOIL_MAX;
        if (this.hasShellCatchingModule) {
            boolean inserted = tryInsertIntoShellCatcher(bulletType);
            if (!inserted) {
                spawnCasing(bulletType);
            }
        } else {
            if (this.level.random.nextFloat() < 0.65) {
                spawnCasing(bulletType);
            }
        }
    }

    Vec3 getMuzzlePosition(float yaw, float pitch) {
        double muzzleLength = 1.0;
        double muzzleOffsetY = 1.4;
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double muzzleX = -Math.sin(yawRad) * Math.cos(pitchRad) * muzzleLength;
        double muzzleY = Math.sin(pitchRad) * muzzleLength + muzzleOffsetY;
        double muzzleZ = -Math.cos(yawRad) * Math.cos(pitchRad) * muzzleLength;
        return new Vec3(
                this.worldPosition.getX() + 0.5 + muzzleX,
                this.worldPosition.getY() + muzzleY,
                this.worldPosition.getZ() + 0.5 + muzzleZ
        );
    }
    private boolean hasLineOfSight(Level level, Vec3 turretPos, LivingEntity target) {
        Vec3 targetPos = target.getEyePosition();
        Vec3 toTarget = targetPos.subtract(turretPos);
        double distance = toTarget.length();
        Vec3 rayVector = toTarget.normalize().scale(distance);

        // Adjust the start position to be slightly above the turret base
        Vec3 adjustedTurretPos = turretPos.add(0, 0.5, 0);

        ClipContext clipContext = new ClipContext(adjustedTurretPos, adjustedTurretPos.add(rayVector), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        BlockHitResult hitResult = level.clip(clipContext);

        return hitResult.getType() == HitResult.Type.MISS;
    }
    private boolean isTargetValid() {
        if (this.target == null || !this.target.isAlive() || this.target.isRemoved()) {
            return false;
        }
        assert this.level != null;
        ChunkPos targetChunkPos = new ChunkPos(this.target.blockPosition());
        if (!this.level.hasChunk(targetChunkPos.x, targetChunkPos.z)) {
            return false;
        }
        double distanceSquared = this.target.distanceToSqr(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5
        );
        return distanceSquared <= (TARGETING_RADIUS * TARGETING_RADIUS);
    }

    @NotNull TurretProjectileEntity getTurretProjectileEntity(TurretProjectileEntity.BulletType bulletType, double dx, double dy, double dz) {
        TurretProjectileEntity projectile = new TurretProjectileEntity(this.level, bulletType);
        double speed = 3.0; // Adjust this value as needed
        projectile.shoot(dx, dy, dz, (float) speed, 0.0F);
        return projectile;
    }


    private void spawnCasing(TurretProjectileEntity.BulletType bulletType) {
        ItemStack casingStack;

        if (bulletType == TurretProjectileEntity.BulletType.ADVANCED_ROUND) {
            casingStack = new ItemStack(ModItems.MEDIUM_BRASS_CASING.get());
        } else if (bulletType == TurretProjectileEntity.BulletType.STANDARD_COPPER_ROUND) {
            casingStack = new ItemStack(ModItems.MEDIUM_COPPER_CASING.get());
        } else if (bulletType == TurretProjectileEntity.BulletType.GIBBS_ROUND) {
            casingStack = new ItemStack(ModItems.MEDIUM_DIAMOND_STEEL_CASING.get());
        } else {
            casingStack = new ItemStack(ModItems.SMALL_COPPER_CASING.get());
        }

        ItemEntity casingEntity = getItemEntity(casingStack);
        assert this.level != null;
        this.level.addFreshEntity(casingEntity);
    }

    @NotNull
    private ItemEntity getItemEntity(ItemStack casingStack) {
        assert this.level != null;
        ItemEntity casingEntity = new ItemEntity(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5, casingStack);
        double ejectSpeed = 0.1;
        double ejectX = Direction.NORTH.getStepX() * ejectSpeed;
        double ejectY = 0.15;
        double ejectZ = Direction.NORTH.getStepZ() * ejectSpeed;
        casingEntity.setDeltaMovement(ejectX, ejectY, ejectZ);
        return casingEntity;
    }
    private boolean isAdjacentToFireRateModule(BlockGetter world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (world.getBlockState(neighborPos).getBlock() instanceof FireRateModuleBlock) {
                return true;
            }
        }
        return false;
    }
    private boolean tryInsertIntoShellCatcher(TurretProjectileEntity.BulletType bulletType) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(direction);
            BlockEntity blockEntity = this.level.getBlockEntity(neighborPos);
            if (blockEntity instanceof ShellCatcherModuleBlockEntity shellCatcher) {
                ItemStack casingStack = switch (bulletType) {
                    case GIBBS_ROUND -> new ItemStack(ModItems.MEDIUM_DIAMOND_STEEL_CASING.get());
                    case ADVANCED_ROUND -> new ItemStack(ModItems.MEDIUM_BRASS_CASING.get());
                    default -> new ItemStack(ModItems.MEDIUM_COPPER_CASING.get());
                };
                for (int i = 0; i < shellCatcher.getContainerSize(); i++) {
                    ItemStack existingStack = shellCatcher.getItemStackHandler().getStackInSlot(i);
                    if (existingStack.isEmpty()) {
                        shellCatcher.getItemStackHandler().setStackInSlot(i, casingStack);
                        return true;
                    } else if (ItemStack.isSameItemSameTags(existingStack, casingStack) && existingStack.getCount() < existingStack.getMaxStackSize()) {
                        existingStack.grow(1);
                        shellCatcher.getItemStackHandler().setStackInSlot(i, existingStack);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isAdjacentToDamageModule(BlockGetter world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (world.getBlockState(neighborPos).getBlock() instanceof DamageModuleBlock) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdjacentToRangeModule(BlockGetter world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (world.getBlockState(neighborPos).getBlock() instanceof RangeModuleBlock) {
                return true;
            }
        }
        return false;
    }
    private boolean isAdjacentToShellCatchingModule() {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(direction);
            assert this.level != null;
            if (this.level.getBlockState(neighborPos).getBlock() instanceof ShellCatcherModuleBlock) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public TurretProjectileEntity.BulletType findAndConsumeAmmo() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == ModItems.GIBBS_ROUND.get()) {
                    consumeAmmo(i);
                    return TurretProjectileEntity.BulletType.GIBBS_ROUND;
                } else if (stack.getItem() == ModItems.ADVANCED_ROUND.get()) {
                    consumeAmmo(i);
                    return TurretProjectileEntity.BulletType.ADVANCED_ROUND;
                } else if (stack.getItem() == ModItems.STANDARD_COPPER_ROUND.get()) {
                    consumeAmmo(i);
                    return TurretProjectileEntity.BulletType.STANDARD_COPPER_ROUND;
                }
            }
        }
        return null;
    }

    void consumeAmmo(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        stack.shrink(1);
        if (stack.isEmpty()) {
            itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }
    private void findTarget(Level level, BlockPos pos) {
        this.target = null;
        boolean hasTargetingModule = false;
        boolean isPlayerTargetingModule = false;
        boolean isHostileTargetingModule = false;

        // Check for targeting modules
        for (Direction direction : Direction.values()) {
            BlockState blockState = level.getBlockState(pos.relative(direction));
            if (blockState.getBlock() instanceof TurretTargetingBlock) {
                hasTargetingModule = true;
                if (blockState.getBlock() instanceof PlayerTurretTargetingBlock) {
                    isPlayerTargetingModule = true;
                } else if (blockState.getBlock() instanceof HostileTurretTargetingBlock) {
                    isHostileTargetingModule = true;
                }
                break;
            }
        }

        if (!hasTargetingModule) {
            return;
        }

        ItemStack logStack = itemHandler.getStackInSlot(9);
        boolean hasTeamLog = logStack.getItem() instanceof TeamLogItem && !(logStack.getItem() instanceof EnemyLogItem);
        boolean hasEnemyLog = logStack.getItem() instanceof EnemyLogItem;

        List<UUID> loggedEntityUUIDs = new ArrayList<>();
        List<String> blacklistedEntityTypes = new ArrayList<>();
        List<UUID> whitelistedEntityUUIDs = new ArrayList<>();
        List<String> whitelistedEntityTypes = new ArrayList<>();

        if (hasTeamLog || hasEnemyLog) {
            CompoundTag tag = logStack.getTag();
            if (tag != null) {
                if (hasTeamLog) {
                    if (tag.contains("Entities", Tag.TAG_LIST)) {
                        ListTag listTag = tag.getList("Entities", Tag.TAG_COMPOUND);
                        for (int i = 0; i < listTag.size(); i++) {
                            CompoundTag entityTag = listTag.getCompound(i);
                            loggedEntityUUIDs.add(entityTag.getUUID("UUID"));
                        }
                    }
                    if (tag.contains("Blacklist", Tag.TAG_LIST)) {
                        ListTag blacklistTag = tag.getList("Blacklist", Tag.TAG_STRING);
                        for (int i = 0; i < blacklistTag.size(); i++) {
                            blacklistedEntityTypes.add(blacklistTag.getString(i));
                        }
                    }
                } else if (hasEnemyLog) {
                    if (tag.contains("Whitelist", Tag.TAG_LIST)) {
                        ListTag listTag = tag.getList("Whitelist", Tag.TAG_COMPOUND);
                        for (int i = 0; i < listTag.size(); i++) {
                            CompoundTag entityTag = listTag.getCompound(i);
                            whitelistedEntityUUIDs.add(entityTag.getUUID("UUID"));
                        }
                    }
                    if (tag.contains("WhitelistEntityTypes", Tag.TAG_LIST)) {
                        ListTag whitelistTag = tag.getList("WhitelistEntityTypes", Tag.TAG_STRING);
                        for (int i = 0; i < whitelistTag.size(); i++) {
                            whitelistedEntityTypes.add(whitelistTag.getString(i));
                        }
                    }
                }
            }
        }

        Vec3 turretPos = new Vec3(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5);

        // Increase the vertical search range
        double verticalSearchRange = TARGETING_RADIUS;
        AABB searchBox = new AABB(pos).inflate(TARGETING_RADIUS, verticalSearchRange, TARGETING_RADIUS);

        boolean finalIsPlayerTargetingModule = isPlayerTargetingModule;
        boolean finalIsHostileTargetingModule = isHostileTargetingModule;
        List<LivingEntity> potentialTargets = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != null
                        && entity.isAlive()
                        && !isOwner(entity)
                        && ((!hasTeamLog && !hasEnemyLog) || // Default targeting when no logs are present
                        (hasTeamLog && !loggedEntityUUIDs.contains(entity.getUUID()) && !blacklistedEntityTypes.contains(EntityType.getKey(entity.getType()).toString())) ||
                        (hasEnemyLog && (whitelistedEntityUUIDs.contains(entity.getUUID()) || whitelistedEntityTypes.contains(EntityType.getKey(entity.getType()).toString()))))
                        && !(entity instanceof EnderMan)
                        && (!finalIsPlayerTargetingModule || (entity instanceof Player && !((Player) entity).isCreative()))
                        && (!finalIsHostileTargetingModule || entity.getType().getCategory() == MobCategory.MONSTER)
                        && !entity.getType().is(ModTags.Entities.TURRET_BLACKLIST)
        );

        if (!potentialTargets.isEmpty()) {
            this.target = potentialTargets.stream()
                    .filter(entity -> hasLineOfSight(level, turretPos, entity))
                    .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(turretPos)))
                    .orElse(null);

            if (this.target != null) {
                double predictedX = this.target.getX() + this.target.getDeltaMovement().x * 7;
                double predictedY = this.target.getY() + (this.target.getBbHeight() / 2); // Target center of entity
                double predictedZ = this.target.getZ() + this.target.getDeltaMovement().z * 7;

                smoothedTargetX = lerp(smoothedTargetX, predictedX, POSITION_SMOOTHING_FACTOR);
                smoothedTargetY = lerp(smoothedTargetY, predictedY, POSITION_SMOOTHING_FACTOR);
                smoothedTargetZ = lerp(smoothedTargetZ, predictedZ, POSITION_SMOOTHING_FACTOR);
            }
        }
    }
    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }
    private void updateYaw() {
        this.previousYaw = this.yaw;

        if (smoothedTargetX != 0 || smoothedTargetZ != 0) {
            double dx = smoothedTargetX - (this.worldPosition.getX() + 0.5);
            double dz = smoothedTargetZ - (this.worldPosition.getZ() + 0.5);
            float targetYaw = (float) (Math.atan2(dx, dz) * (180 / Math.PI)) + 180;
            targetYaw = (targetYaw + 360) % 360;
            this.yaw = (this.yaw + 360) % 360;

            float yawDifference = targetYaw - this.yaw;
            if (yawDifference > 180) {
                yawDifference -= 360;
            } else if (yawDifference < -180) {
                yawDifference += 360;
            }

            this.yaw += yawDifference * ROTATION_SPEED;
            this.yaw = this.yaw % 360.0F;
            if (this.yaw < 0) this.yaw += 360.0F;
        }
    }
    private void updatePitch() {
        this.previousPitch = this.pitch;

        if (smoothedTargetY != 0) {
            double dx = smoothedTargetX - (this.worldPosition.getX() + 0.5);
            double dy = smoothedTargetY - (this.worldPosition.getY() + 1.0);
            double dz = smoothedTargetZ - (this.worldPosition.getZ() + 0.5);
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

            float targetPitch = (float) (Math.atan2(dy, horizontalDistance) * (180 / Math.PI));
            targetPitch = Mth.clamp(targetPitch, MIN_PITCH, MAX_PITCH);

            float pitchDifference = targetPitch - this.pitch;

            this.pitch += pitchDifference * ROTATION_SPEED;
        }
    }

    public float getPreviousYaw() {
        return this.previousYaw;
    }

    public float getPreviousPitch() {
        return this.previousPitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putFloat("Yaw", this.yaw);
        tag.putFloat("Pitch", this.pitch);
        tag.putBoolean("Disabled", this.disabled);
        tag.putInt("DisableCooldown", this.disableCooldown);
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
            tag.putString("OwnerName", ownerName);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.yaw = tag.getFloat("Yaw");
        this.previousYaw = this.yaw;
        this.pitch = tag.getFloat("Pitch");
        this.previousPitch = this.pitch;
        this.disabled = tag.getBoolean("Disabled");
        this.disableCooldown = tag.getInt("DisableCooldown");
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
            this.ownerName = tag.getString("OwnerName");
        }
    }
    private boolean isOwner(LivingEntity entity) {
        return entity.getUUID().equals(this.ownerUUID);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    public SimpleContainer getContainer() {
        SimpleContainer container = new SimpleContainer(10); // Updated to include all 10 slots
        for (int i = 0; i < 10; i++) {
            container.setItem(i, itemHandler.getStackInSlot(i));
        }
        return container;
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != Direction.UP) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }


    public ItemStackHandler getItemStackHandler() {
        return this.itemHandler;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void setOwner(ServerPlayer player) {
        this.ownerUUID = player.getUUID();
        this.ownerName = player.getName().getString(); // Save the owner's name
    }

    public String getOwnerName() {
        return ownerName;
    }
}
