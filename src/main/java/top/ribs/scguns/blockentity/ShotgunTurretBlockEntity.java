package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
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
import top.ribs.scguns.block.*;
import top.ribs.scguns.client.screen.ShotgunTurretMenu;
import top.ribs.scguns.client.screen.ShotgunTurretMenu;
import top.ribs.scguns.entity.projectile.turret.TurretProjectileEntity;
import top.ribs.scguns.init.ModBlockEntities;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModSounds;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.TeamLogItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageMuzzleFlash;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ShotgunTurretBlockEntity extends BlockEntity implements MenuProvider {
    private static double TARGETING_RADIUS = 7.0f;
    private static final int PELLET_COUNT = 8;
    private static final float SPREAD_ANGLE = 8.0F;
    private int cooldown = 30;
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
    private static final float MIN_PITCH = -15.0F;
    private double smoothedTargetY;
    private static final float POSITION_SMOOTHING_FACTOR = 0.2F;
    private static final float ROTATION_SPEED = 0.5F;
    public static final float RECOIL_MAX = 4.0F;
    private static final float RECOIL_SPEED = 0.3F;
    public float recoilPitchOffset = 0.0F;
    private static final double MINIMUM_FIRING_DISTANCE = 1.3;
    private static final int DAMAGE_INCREASE = 2;
    private static final double RANGE_INCREASE = 8.0;

    private float previousYaw;
    private float previousPitch;
    private boolean hasFireRateModule;
    private boolean hasDamageModule;
    private boolean hasRangeModule;
    private boolean hasShellCatchingModule;


    public ShotgunTurretBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHOTGUN_TURRET.get(), pos, state);
    }


    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.shotgun_turret");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new ShotgunTurretMenu(id, playerInventory, this);
    }
    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof ShotgunTurretBlockEntity turret) {
            // Cache module checks
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

            if (state.getValue(ShotgunTurretBlock.POWERED)) {
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
                        turret.cooldown = 30;
                    }
                }
            } else {
                turret.resetToRestPosition();
            }
        }
    }

    private void updateTargetRange(double rangeModifier) {
        TARGETING_RADIUS = 7.0f + (float)rangeModifier;
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
        this.level.playSound(null, this.worldPosition, ModSounds.BRASS_SHOTGUN_FIRE.get(), SoundSource.BLOCKS, 0.7F, 0.7F);

        Vec3 targetPos = new Vec3(target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ());
        Vec3 direction = targetPos.subtract(muzzlePos).normalize();

        fireCluster(bulletType, muzzlePos, direction, damageModifier);

        this.recoilPitchOffset = RECOIL_MAX;

        handleCasingEjection(bulletType);
    }

    private void fireCluster(TurretProjectileEntity.BulletType bulletType, Vec3 muzzlePos, Vec3 baseDirection, int damageModifier) {
        double baseDamage = bulletType.getDamage() + damageModifier;
        double pelletDamage = baseDamage / PELLET_COUNT;

        for (int i = 0; i < PELLET_COUNT; i++) {
            Vec3 spreadDirection = applySpread(baseDirection);
            TurretProjectileEntity projectile = getTurretProjectileEntity(bulletType, spreadDirection.x, spreadDirection.y, spreadDirection.z);
            projectile.setPos(muzzlePos.x, muzzlePos.y, muzzlePos.z);
            projectile.setBaseDamage(pelletDamage);
            assert this.level != null;
            this.level.addFreshEntity(projectile);
        }
    }

    private Vec3 applySpread(Vec3 baseDirection) {
        float angleX = (float) (this.level.random.nextGaussian() * SPREAD_ANGLE);
        float angleY = (float) (this.level.random.nextGaussian() * SPREAD_ANGLE);

        // Convert to radians
        double yawRad = Math.toRadians(angleX);
        double pitchRad = Math.toRadians(angleY);

        // Apply rotation
        double x = baseDirection.x;
        double y = baseDirection.y;
        double z = baseDirection.z;

        // Rotate around Y axis (yaw)
        double tempX = x * Math.cos(yawRad) - z * Math.sin(yawRad);
        double tempZ = x * Math.sin(yawRad) + z * Math.cos(yawRad);
        double tempY = y;
        x = tempX;
        z = tempZ;

        // Rotate around X axis (pitch)
        tempY = y * Math.cos(pitchRad) - z * Math.sin(pitchRad);
        tempZ = y * Math.sin(pitchRad) + z * Math.cos(pitchRad);
        y = tempY;
        z = tempZ;

        return new Vec3(x, y, z).normalize();
    }
    private void handleCasingEjection(TurretProjectileEntity.BulletType bulletType) {
        if (this.hasShellCatchingModule) {
            boolean inserted = tryInsertIntoShellCatcher(bulletType);
            if (!inserted) {
                spawnCasing(bulletType);
            }
        } else {
            assert this.level != null;
            if (this.level.random.nextFloat() < 0.65) {
                spawnCasing(bulletType);
            }
        }
    }
    Vec3 getMuzzlePosition(float yaw, float pitch) {
        double muzzleLength = 1.3;
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

        ClipContext clipContext = new ClipContext(turretPos, turretPos.add(rayVector), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
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
        double speed = 3.0;
        projectile.shoot(dx, dy, dz, (float) speed, 0.0F);
        return projectile;
    }


    private void spawnCasing(TurretProjectileEntity.BulletType bulletType) {
        ItemStack casingStack;

        if (bulletType == TurretProjectileEntity.BulletType.SHELL) {
            casingStack = new ItemStack(ModItems.SMALL_BRASS_CASING.get());
        } else if (bulletType == TurretProjectileEntity.BulletType.BEARPACK) {
            casingStack = new ItemStack(ModItems.MEDIUM_BRASS_CASING.get());
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
                    case BEARPACK -> new ItemStack(ModItems.MEDIUM_BRASS_CASING.get());
                    default -> new ItemStack(ModItems.SMALL_COPPER_CASING.get());
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
                 if (stack.getItem() == ModItems.SHOTGUN_SHELL.get()) {
                    consumeAmmo(i);
                    return TurretProjectileEntity.BulletType.SHELL;
                } else if (stack.getItem() == ModItems.BEARPACK_SHELL.get()) {
                    consumeAmmo(i);
                    return TurretProjectileEntity.BulletType.BEARPACK;
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
        ItemStack teamLogStack = itemHandler.getStackInSlot(9);
        List<UUID> loggedEntityUUIDs = new ArrayList<>();
        List<String> blacklistedEntityTypes = new ArrayList<>();
        if (teamLogStack.getItem() instanceof TeamLogItem) {
            CompoundTag tag = teamLogStack.getTag();
            if (tag != null) {
                if (tag.contains("Entities", Tag.TAG_LIST)) {
                    ListTag listTag = tag.getList("Entities", Tag.TAG_COMPOUND);
                    for (int i = 0; i < listTag.size(); i++) {
                        CompoundTag entityTag = listTag.getCompound(i);
                        UUID entityUUID = entityTag.getUUID("UUID");
                        loggedEntityUUIDs.add(entityUUID);
                    }
                }
                if (tag.contains("Blacklist", Tag.TAG_LIST)) {
                    ListTag blacklistTag = tag.getList("Blacklist", Tag.TAG_STRING);
                    for (int i = 0; i < blacklistTag.size(); i++) {
                        String entityType = blacklistTag.getString(i);
                        blacklistedEntityTypes.add(entityType);
                    }
                }
            }
        }
        boolean finalIsPlayerTargetingModule = isPlayerTargetingModule;
        boolean finalIsHostileTargetingModule = isHostileTargetingModule;
        List<LivingEntity> potentialTargets = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(TARGETING_RADIUS),
                entity -> entity != null
                        && entity.isAlive()
                        && !isOwner(entity)
                        && !loggedEntityUUIDs.contains(entity.getUUID())
                        && !blacklistedEntityTypes.contains(EntityType.getKey(entity.getType()).toString())
                        && !(entity instanceof EnderMan)
                        && (!finalIsPlayerTargetingModule || (entity instanceof Player && !((Player) entity).isCreative()))
                        && (!finalIsHostileTargetingModule || entity.getType().getCategory() == MobCategory.MONSTER)
                        && !entity.getType().is(ModTags.Entities.TURRET_BLACKLIST)
        );

        if (!potentialTargets.isEmpty()) {
            Vec3 turretPos = new Vec3(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5);

            this.target = potentialTargets.stream()
                    .filter(entity -> hasLineOfSight(level, turretPos, entity))
                    .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(turretPos)))
                    .orElse(null);

            if (this.target != null) {
                double predictedX = this.target.getX() + this.target.getDeltaMovement().x * 7;
                double predictedY = (this.target.getY() + this.target.getEyeY()) / 2;
                double predictedZ = this.target.getZ() + this.target.getDeltaMovement().z * 7;

                smoothedTargetX = smoothedTargetX * (1.0F - POSITION_SMOOTHING_FACTOR) + predictedX * POSITION_SMOOTHING_FACTOR;
                smoothedTargetY = smoothedTargetY * (1.0F - POSITION_SMOOTHING_FACTOR) + predictedY * POSITION_SMOOTHING_FACTOR;
                smoothedTargetZ = smoothedTargetZ * (1.0F - POSITION_SMOOTHING_FACTOR) + predictedZ * POSITION_SMOOTHING_FACTOR;
            }
        }
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
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
            tag.putString("OwnerName", ownerName); // Save the owner's name
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.yaw = tag.getFloat("Yaw");
        this.previousYaw = this.yaw;
        this.pitch = tag.getFloat("Pitch");
        this.previousPitch = this.pitch;
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
            this.ownerName = tag.getString("OwnerName"); // Load the owner's name
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