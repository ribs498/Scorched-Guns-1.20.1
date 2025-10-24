package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Config;
import top.ribs.scguns.block.*;
import top.ribs.scguns.common.Turret;
import top.ribs.scguns.common.TurretManager;
import top.ribs.scguns.entity.projectile.turret.TurretProjectileEntity;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.EnemyLogItem;
import top.ribs.scguns.item.TeamLogItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageMuzzleFlash;

import javax.annotation.Nullable;
import java.util.*;

public abstract class TurretBlockEntity extends BlockEntity implements MenuProvider {
    protected final ResourceLocation turretId;
    protected Turret config;

    protected double targetingRadius;
    protected int cooldown;

    public final ItemStackHandler itemHandler = new ItemStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    protected LivingEntity target;
    protected UUID ownerUUID;
    protected String ownerName;
    protected float yaw;
    protected double smoothedTargetX;
    protected double smoothedTargetZ;
    protected float pitch;
    protected double smoothedTargetY;
    protected float previousYaw;
    protected float previousPitch;
    public float recoilPitchOffset = 0.0F;

    protected boolean hasFireRateModule;
    protected boolean hasDamageModule;
    protected boolean hasRangeModule;
    protected boolean hasShellCatchingModule;

    public boolean disabled = false;
    public int disableCooldown = 0;

    private static final int DAMAGE_INCREASE = 2;
    private static final double RANGE_INCREASE = 8.0;

    private static final int IDLE_BEFORE_SCAN = 60;
    private static final float SCAN_ANGLE = 60.0F;
    private static final float SCAN_SPEED = 0.02F;
    private static final float SCAN_PITCH = 0.0F;

    private int idleTicks = 0;
    private boolean isScanning = false;
    private boolean scanningRight = true;
    private float scanStartYaw = 0.0F;
    private boolean returningToScanPitch = false;

    public TurretBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ResourceLocation turretId) {
        super(type, pos, state);
        this.turretId = turretId;
        this.config = TurretManager.getTurret(turretId);

        if (this.config != null) {
            this.targetingRadius = this.config.getTargeting().getRange();
            this.cooldown = this.config.getCombat().getCooldown();
        } else {
            this.targetingRadius = 12.0;
            this.cooldown = 16;
        }
    }

    public void reloadConfig() {
        this.config = TurretManager.getTurret(this.turretId);
        if (this.config != null) {
            this.targetingRadius = this.config.getTargeting().getRange();
            this.cooldown = this.config.getCombat().getCooldown();
        }
    }

    public void tick() {
        if (this.level == null) return;

        if (this.config == null) {
            reloadConfig();
            if (this.config == null) return;
        }

        this.hasFireRateModule = isAdjacentToFireRateModule(this.level, this.worldPosition);
        this.hasDamageModule = isAdjacentToDamageModule(this.level, this.worldPosition);
        this.hasRangeModule = isAdjacentToRangeModule(this.level, this.worldPosition);
        this.hasShellCatchingModule = isAdjacentToShellCatchingModule();

        int fireRateModifier = this.hasFireRateModule ? 2 : 1;
        int damageModifier = this.hasDamageModule ? DAMAGE_INCREASE : 0;
        double rangeModifier = this.hasRangeModule ? RANGE_INCREASE : 0;

        if (this.cooldown > 0) {
            this.cooldown -= fireRateModifier;
        }
        tickRecoil();

        if (this.disabled) {
            this.disableCooldown--;
            if (this.disableCooldown <= 0) {
                this.disabled = false;
                this.disableCooldown = 0;
            }
            resetToRestPosition();
            idleTicks = 0;
            isScanning = false;
        } else if (!isPowered(getBlockState())) {
            updateTargetRange(rangeModifier);
            if (!isTargetValid()) {
                this.target = null;
            }
            findTarget(this.level, this.worldPosition);

            if (this.target != null) {
                idleTicks = 0;
                isScanning = false;
                returningToScanPitch = false;
                updateYaw();
                updatePitch();

                if (this.cooldown <= 0 && isReadyToFire()) {
                    fireWeapon(damageModifier);
                }
            } else {
                idleTicks++;

                if (idleTicks < IDLE_BEFORE_SCAN) {
                    this.previousYaw = this.yaw;
                    this.previousPitch = this.pitch;
                } else {
                    updateScanningBehavior();
                }
            }
        } else {
            resetToRestPosition();
            idleTicks = 0;
            isScanning = false;
            returningToScanPitch = false;
        }
    }

    protected void updateScanningBehavior() {
        if (this.config == null) return;

        this.previousYaw = this.yaw;
        this.previousPitch = this.pitch;

        if (!returningToScanPitch) {
            float pitchDiff = SCAN_PITCH - this.pitch;

            if (Math.abs(pitchDiff) > 0.5F) {
                this.pitch += pitchDiff * this.config.getTargeting().getRotationSpeed();
                return;
            } else {
                this.pitch = SCAN_PITCH;
                returningToScanPitch = true;
            }
        }
        this.pitch = SCAN_PITCH;
        if (!isScanning) {
            isScanning = true;
            scanStartYaw = this.yaw;
            scanningRight = true;
        }
        float targetYaw;
        if (scanningRight) {
            targetYaw = scanStartYaw + SCAN_ANGLE;
        } else {
            targetYaw = scanStartYaw - SCAN_ANGLE;
        }

        float yawDiff = targetYaw - this.yaw;
        if (yawDiff > 180) yawDiff -= 360;
        else if (yawDiff < -180) yawDiff += 360;

        this.yaw += yawDiff * SCAN_SPEED;
        this.yaw = this.yaw % 360.0F;
        if (this.yaw < 0) this.yaw += 360.0F;

        // Check if reached scan limit
        float currentDiff = Math.abs(Mth.wrapDegrees(this.yaw - scanStartYaw));
        if (currentDiff >= SCAN_ANGLE - 1.0F) {
            scanningRight = !scanningRight;
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof TurretBlockEntity turret) {
            turret.tick();
        }
    }

    protected abstract boolean isPowered(BlockState state);

    protected void fireWeapon(int damageModifier) {
        Turret.Ammunition.AmmoType ammoType = findAndConsumeAmmo();
        if (ammoType != null) {
            fire(ammoType, damageModifier);
            this.cooldown = this.config.getCombat().getCooldown();
        }
    }

    protected void fire(Turret.Ammunition.AmmoType ammoType, int damageModifier) {
        if (this.level == null || this.target == null || this.config == null) {
            return;
        }

        float yaw = this.getYaw();
        float pitch = this.getPitch();
        Vec3 muzzlePos = getMuzzlePosition(yaw, pitch);

        if (!this.level.isClientSide) {
            PacketHandler.getPlayChannel().sendToTrackingChunk(() -> level.getChunkAt(worldPosition),
                    new S2CMessageMuzzleFlash(muzzlePos, yaw, pitch));
        }

        SoundEvent fireSound = null;
        ResourceLocation soundLoc = this.config.getCombat().getFireSound();
        if (soundLoc != null) {
            fireSound = ForgeRegistries.SOUND_EVENTS.getValue(soundLoc);
        }
        if (fireSound != null) {
            this.level.playSound(null, this.worldPosition, fireSound, SoundSource.BLOCKS, 0.7F, 0.7F);
        }

        Vec3 targetPos = new Vec3(target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ());
        Vec3 direction = targetPos.subtract(muzzlePos).normalize();

        float inaccuracy = this.config.getCombat().getInaccuracy();
        if (inaccuracy > 0) {
            direction = direction.add(
                    this.level.random.triangle(0, inaccuracy),
                    this.level.random.triangle(0, inaccuracy),
                    this.level.random.triangle(0, inaccuracy)
            ).normalize();
        }

        int pelletCount = this.config.getCombat().getPelletCount();
        if (pelletCount > 1) {
            fireCluster(ammoType, muzzlePos, direction, damageModifier, pelletCount);
        } else {
            fireSingleProjectile(ammoType, muzzlePos, direction, damageModifier);
        }

        this.recoilPitchOffset = this.config.getCombat().getRecoilMax();
        handleCasingEjection(ammoType);
    }

    protected void fireSingleProjectile(Turret.Ammunition.AmmoType ammoType, Vec3 muzzlePos, Vec3 direction, int damageModifier) {
        TurretProjectileEntity projectile = createProjectile();
        projectile.setPos(muzzlePos.x, muzzlePos.y, muzzlePos.z);

        double speed = this.config.getCombat().getProjectileSpeed();
        projectile.shoot(direction.x, direction.y, direction.z, (float) speed, 0.0F);

        double finalDamage = getDamageForAmmoType(ammoType) + damageModifier;
        projectile.setBaseDamage(finalDamage);
        projectile.setArmorPenetration(ammoType.getArmorPenetration());

        String bulletType = ammoType.getBulletType().toString();
        if (bulletType.equals("scguns:bear_pack_shell")) {
            projectile.setMobPenetration(1);
        } else if (bulletType.equals("scguns:gibbs_round")) {
            projectile.setGibbsRound(true);
        } else if (bulletType.equals("scguns:shatter_round")) {
            projectile.setShatterRound(true);
        }

        assert this.level != null;
        this.level.addFreshEntity(projectile);
    }

    protected void fireCluster(Turret.Ammunition.AmmoType ammoType, Vec3 muzzlePos, Vec3 baseDirection, int damageModifier, int pelletCount) {
        double baseDamage = getDamageForAmmoType(ammoType);
        double finalDamage = baseDamage + damageModifier;
        double pelletDamage = finalDamage / pelletCount;
        float spreadAngle = this.config.getCombat().getSpreadAngle();

        String bulletType = ammoType.getBulletType().toString();
        boolean isBearPackShell = bulletType.equals("scguns:bear_pack_shell");
        boolean isGibbsRound = bulletType.equals("scguns:gibbs_round");
        boolean isShatterRound = bulletType.equals("scguns:shatter_round");

        for (int i = 0; i < pelletCount; i++) {
            Vec3 spreadDirection = applySpread(baseDirection, spreadAngle);
            TurretProjectileEntity projectile = createProjectile();
            projectile.setPos(muzzlePos.x, muzzlePos.y, muzzlePos.z);

            double speed = this.config.getCombat().getProjectileSpeed();
            projectile.shoot(spreadDirection.x, spreadDirection.y, spreadDirection.z, (float) speed, 0.0F);
            projectile.setBaseDamage(pelletDamage);
            projectile.setArmorPenetration(ammoType.getArmorPenetration());

            if (isBearPackShell) {
                projectile.setMobPenetration(1);
            } else if (isGibbsRound) {
                projectile.setGibbsRound(true);
            } else if (isShatterRound) {
                projectile.setShatterRound(true);
            }

            assert this.level != null;
            this.level.addFreshEntity(projectile);
        }
    }

    protected Vec3 applySpread(Vec3 baseDirection, float spreadAngle) {
        assert this.level != null;
        float angleX = (float) (this.level.random.nextGaussian() * spreadAngle);
        float angleY = (float) (this.level.random.nextGaussian() * spreadAngle);

        double yawRad = Math.toRadians(angleX);
        double pitchRad = Math.toRadians(angleY);
        double x = baseDirection.x;
        double y = baseDirection.y;
        double z = baseDirection.z;

        double tempX = x * Math.cos(yawRad) - z * Math.sin(yawRad);
        double tempZ = x * Math.sin(yawRad) + z * Math.cos(yawRad);
        x = tempX;
        z = tempZ;

        double tempY = y * Math.cos(pitchRad) - z * Math.sin(pitchRad);
        tempZ = y * Math.sin(pitchRad) + z * Math.cos(pitchRad);
        y = tempY;
        z = tempZ;

        return new Vec3(x, y, z).normalize();
    }

    protected TurretProjectileEntity createProjectile() {
        return new TurretProjectileEntity(this.level);
    }

    protected double getDamageForAmmoType(Turret.Ammunition.AmmoType ammoType) {
        double baseDamage = ammoType.getDamage();
        baseDamage *= Config.COMMON.gameplay.globalTurretDamageMultiplier.get();
        return baseDamage;
    }

    protected void handleCasingEjection(Turret.Ammunition.AmmoType ammoType) {
        if (this.hasShellCatchingModule) {
            boolean inserted = tryInsertIntoShellCatcher(ammoType);
            if (!inserted) {
                spawnCasing(ammoType);
            }
        } else {
            float ejectChance = this.config.getAmmunition().getCasingEjectChance();
            assert this.level != null;
            if (this.level.random.nextFloat() < ejectChance) {
                spawnCasing(ammoType);
            }
        }
    }

    protected void spawnCasing(Turret.Ammunition.AmmoType ammoType) {
        ResourceLocation casingType = ammoType.getCasingType();
        if (casingType == null) return;

        ItemStack casingStack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(casingType)));
        assert this.level != null;
        ItemEntity casingEntity = new ItemEntity(this.level,
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 1.0,
                this.worldPosition.getZ() + 0.5,
                casingStack);

        double ejectSpeed = 0.1;
        double ejectX = Direction.NORTH.getStepX() * ejectSpeed;
        double ejectY = 0.15;
        double ejectZ = Direction.NORTH.getStepZ() * ejectSpeed;
        casingEntity.setDeltaMovement(ejectX, ejectY, ejectZ);

        this.level.addFreshEntity(casingEntity);
    }

    protected boolean tryInsertIntoShellCatcher(Turret.Ammunition.AmmoType ammoType) {
        ResourceLocation casingType = ammoType.getCasingType();
        if (casingType == null) return false;

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(direction);
            assert this.level != null;
            BlockEntity blockEntity = this.level.getBlockEntity(neighborPos);

            if (blockEntity instanceof ShellCatcherModuleBlockEntity shellCatcher) {
                ItemStack casingStack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(casingType)));

                for (int i = 0; i < shellCatcher.getContainerSize(); i++) {
                    ItemStack existingStack = shellCatcher.getItemStackHandler().getStackInSlot(i);
                    if (existingStack.isEmpty()) {
                        shellCatcher.getItemStackHandler().setStackInSlot(i, casingStack);
                        return true;
                    } else if (ItemStack.isSameItemSameTags(existingStack, casingStack) &&
                            existingStack.getCount() < existingStack.getMaxStackSize()) {
                        existingStack.grow(1);
                        shellCatcher.getItemStackHandler().setStackInSlot(i, existingStack);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected Vec3 getMuzzlePosition(float yaw, float pitch) {
        double muzzleLength = this.config.getDisplay().getMuzzleLength();
        double muzzleOffsetY = this.config.getDisplay().getMuzzleOffsetY();

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

    protected void updateTargetRange(double rangeModifier) {
        this.targetingRadius = this.config.getTargeting().getRange() + rangeModifier;
    }

    public boolean isReadyToFire() {
        if (this.target == null || this.config == null) return false;

        double dx = smoothedTargetX - (this.worldPosition.getX() + 0.5);
        double dy = smoothedTargetY - (this.worldPosition.getY() + 1.0);
        double dz = smoothedTargetZ - (this.worldPosition.getZ() + 0.5);
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Math.atan2(dx, dz) * (180 / Math.PI)) + 180;
        targetYaw = (targetYaw + 360) % 360;
        float targetPitch = (float) (Math.atan2(dy, horizontalDistance) * (180 / Math.PI));
        targetPitch = Mth.clamp(targetPitch, this.config.getTargeting().getMinPitch(),
                this.config.getTargeting().getMaxPitch());

        float yawDifference = Math.abs(targetYaw - this.yaw);
        if (yawDifference > 180) yawDifference = 360 - yawDifference;
        float pitchDifference = Math.abs(targetPitch - this.pitch);

        double distanceSquared = dx * dx + dy * dy + dz * dz;
        double minDist = this.config.getTargeting().getMinFiringDistance();
        if (distanceSquared < minDist * minDist) {
            return false;
        }

        return yawDifference < 2.0F && pitchDifference < 2.0F;
    }

    public void tickRecoil() {
        if (this.config == null) return;

        if (this.recoilPitchOffset > 0) {
            this.recoilPitchOffset -= this.config.getCombat().getRecoilSpeed();
            if (this.recoilPitchOffset < 0) {
                this.recoilPitchOffset = 0;
            }
        }
    }

    public void resetToRestPosition() {
        if (this.config == null) return;

        this.target = null;
        float restingYaw = this.config.getBehavior().getRestingYaw();
        float restingPitch = this.config.getBehavior().getRestingPitch();

        this.previousYaw = this.yaw;
        this.previousPitch = this.pitch;

        float yawDifference = restingYaw - this.yaw;
        if (yawDifference > 180) yawDifference -= 360;
        else if (yawDifference < -180) yawDifference += 360;

        float rotSpeed = this.config.getTargeting().getRotationSpeed();
        this.yaw += yawDifference * rotSpeed;
        this.yaw = this.yaw % 360.0F;
        if (this.yaw < 0) this.yaw += 360.0F;

        float pitchDifference = restingPitch - this.pitch;
        this.pitch += pitchDifference * rotSpeed;

        this.smoothedTargetX = 0;
        this.smoothedTargetY = 0;
        this.smoothedTargetZ = 0;
    }

    public void onHitByLightningProjectile() {
        if (this.config == null) return;

        this.disabled = true;
        this.disableCooldown = this.config.getBehavior().getDisableTime();
        this.resetToRestPosition();
        this.setChanged();

        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            spawnDisableParticles();
        }
    }

    protected void spawnDisableParticles() {
        if (this.level instanceof ServerLevel serverLevel) {
            double x = this.worldPosition.getX() + 0.5;
            double y = this.worldPosition.getY() + 1.0;
            double z = this.worldPosition.getZ() + 0.5;

            for (int i = 0; i < 20; i++) {
                double offsetX = this.level.random.nextDouble() * 0.5 - 0.25;
                double offsetY = this.level.random.nextDouble() * 0.5;
                double offsetZ = this.level.random.nextDouble() * 0.5 - 0.25;

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        x + offsetX, y + offsetY, z + offsetZ,
                        1, 0, 0, 0, 0.05);
            }
            serverLevel.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Nullable
    protected Turret.Ammunition.AmmoType findAndConsumeAmmo() {
        if (this.config == null) return null;

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            for (Turret.Ammunition.AmmoType ammoType : this.config.getAmmunition().getAcceptedAmmo()) {
                if (stack.getItem() == ammoType.getItem()) {
                    consumeAmmo(i);
                    return ammoType;
                }
            }
        }
        return null;
    }

    protected void consumeAmmo(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        stack.shrink(1);
        if (stack.isEmpty()) {
            itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    protected void findTarget(Level level, BlockPos pos) {
        if (this.config == null) return;

        this.target = null;
        boolean hasTargetingModule = false;
        boolean isPlayerTargetingModule = false;
        boolean isHostileTargetingModule = false;

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

        if (!hasTargetingModule) return;

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
                } else {
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

        Vec3 turretPos = new Vec3(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0,
                this.worldPosition.getZ() + 0.5);
        double verticalSearchRange = this.config.getTargeting().getVerticalRange();
        AABB searchBox = new AABB(pos).inflate(targetingRadius, verticalSearchRange, targetingRadius);

        boolean finalIsPlayerTargetingModule = isPlayerTargetingModule;
        boolean finalIsHostileTargetingModule = isHostileTargetingModule;

        List<LivingEntity> potentialTargets = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != null
                        && entity.isAlive()
                        && !isOwner(entity)
                        && ((!hasTeamLog && !hasEnemyLog) ||
                        (hasTeamLog && !loggedEntityUUIDs.contains(entity.getUUID()) &&
                                !blacklistedEntityTypes.contains(EntityType.getKey(entity.getType()).toString())) ||
                        (hasEnemyLog && (whitelistedEntityUUIDs.contains(entity.getUUID()) ||
                                whitelistedEntityTypes.contains(EntityType.getKey(entity.getType()).toString()))))
                        && !(entity instanceof EnderMan)
                        && (!entity.isInvisible() || this.hasRangeModule)
                        && (!finalIsPlayerTargetingModule || (entity instanceof Player && !((Player) entity).isCreative()))
                        && (!finalIsHostileTargetingModule ||
                        (entity.getType().getCategory() == MobCategory.MONSTER ||
                                entity.getType().is(ModTags.Entities.TURRET_ENEMY_WHITELIST)))
                        && !entity.getType().is(ModTags.Entities.TURRET_BLACKLIST)
        );

        if (!potentialTargets.isEmpty()) {
            if (this.config.getTargeting().requiresLineOfSight()) {
                this.target = potentialTargets.stream()
                        .filter(entity -> hasLineOfSight(level, turretPos, entity))
                        .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(turretPos)))
                        .orElse(null);
            } else {
                this.target = potentialTargets.stream()
                        .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(turretPos)))
                        .orElse(null);
            }

            if (this.target != null) {
                int predMult = this.config.getTargeting().getPredictionMultiplier();
                double predictedX = this.target.getX() + this.target.getDeltaMovement().x * predMult;
                double predictedY = this.target.getY() + (this.target.getBbHeight() / 2);
                double predictedZ = this.target.getZ() + this.target.getDeltaMovement().z * predMult;

                float smoothing = this.config.getTargeting().getPositionSmoothing();
                double resultX = lerp(smoothedTargetX, predictedX, smoothing);
                double resultY = lerp(smoothedTargetY, predictedY, smoothing);
                double resultZ = lerp(smoothedTargetZ, predictedZ, smoothing);
                smoothedTargetX = resultX;
                smoothedTargetY = resultY;
                smoothedTargetZ = resultZ;
            }
        }
    }

    protected boolean hasLineOfSight(Level level, Vec3 turretPos, LivingEntity target) {
        Vec3 targetPos = target.getEyePosition();
        Vec3 toTarget = targetPos.subtract(turretPos);
        double distance = toTarget.length();
        Vec3 rayVector = toTarget.normalize().scale(distance);
        Vec3 adjustedTurretPos = turretPos.add(0, 0.5, 0);

        ClipContext clipContext = new ClipContext(adjustedTurretPos, adjustedTurretPos.add(rayVector),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        BlockHitResult hitResult = level.clip(clipContext);

        return hitResult.getType() == HitResult.Type.MISS;
    }

    protected boolean isTargetValid() {
        if (this.target == null || !this.target.isAlive() || this.target.isRemoved()) {
            return false;
        }

        ChunkPos targetChunkPos = new ChunkPos(this.target.blockPosition());
        assert this.level != null;
        if (!this.level.hasChunk(targetChunkPos.x, targetChunkPos.z)) {
            return false;
        }

        double distanceSquared = this.target.distanceToSqr(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5
        );
        return distanceSquared <= (targetingRadius * targetingRadius);
    }

    protected static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    protected void updateYaw() {
        if (this.config == null) return;

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

            float rotSpeed = this.config.getTargeting().getRotationSpeed();
            this.yaw += yawDifference * rotSpeed;
            this.yaw = this.yaw % 360.0F;
            if (this.yaw < 0) this.yaw += 360.0F;
        }
    }

    protected void updatePitch() {
        if (this.config == null) return;

        this.previousPitch = this.pitch;

        if (smoothedTargetY != 0) {
            double dx = smoothedTargetX - (this.worldPosition.getX() + 0.5);
            float pitchDifference = getPitchDifference(dx);
            float rotSpeed = this.config.getTargeting().getRotationSpeed();
            this.pitch += pitchDifference * rotSpeed;
        }
    }

    private float getPitchDifference(double dx) {
        double dy = smoothedTargetY - (this.worldPosition.getY() + 1.0);
        double dz = smoothedTargetZ - (this.worldPosition.getZ() + 0.5);
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float targetPitch = (float) (Math.atan2(dy, horizontalDistance) * (180 / Math.PI));
        targetPitch = Mth.clamp(targetPitch, this.config.getTargeting().getMinPitch(),
                this.config.getTargeting().getMaxPitch());
        return targetPitch - this.pitch;
    }

    protected boolean isAdjacentToFireRateModule(BlockGetter world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (world.getBlockState(neighborPos).getBlock() instanceof FireRateModuleBlock) {
                return true;
            }
        }
        return false;
    }

    protected boolean isAdjacentToDamageModule(BlockGetter world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (world.getBlockState(neighborPos).getBlock() instanceof DamageModuleBlock) {
                return true;
            }
        }
        return false;
    }

    protected boolean isAdjacentToRangeModule(BlockGetter world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (world.getBlockState(neighborPos).getBlock() instanceof RangeModuleBlock) {
                return true;
            }
        }
        return false;
    }

    protected boolean isAdjacentToShellCatchingModule() {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(direction);
            assert this.level != null;
            if (this.level.getBlockState(neighborPos).getBlock() instanceof ShellCatcherModuleBlock) {
                return true;
            }
        }
        return false;
    }

    protected boolean isOwner(LivingEntity entity) {
        return entity.getUUID().equals(this.ownerUUID);
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

    public float getPitch() {
        return this.pitch;
    }

    public float getRecoilPitchOffset() {
        return recoilPitchOffset;
    }

    public void setOwner(ServerPlayer player) {
        this.ownerUUID = player.getUUID();
        this.ownerName = player.getName().getString();
    }

    public String getOwnerName() {
        return ownerName;
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
        tag.putInt("IdleTicks", this.idleTicks);
        tag.putBoolean("IsScanning", this.isScanning);
        tag.putBoolean("ScanningRight", this.scanningRight);
        tag.putFloat("ScanStartYaw", this.scanStartYaw);
        tag.putBoolean("ReturningToScanPitch", this.returningToScanPitch);
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
        this.idleTicks = tag.getInt("IdleTicks");
        this.isScanning = tag.getBoolean("IsScanning");
        this.scanningRight = tag.getBoolean("ScanningRight");
        this.scanStartYaw = tag.getFloat("ScanStartYaw");
        this.returningToScanPitch = tag.getBoolean("ReturningToScanPitch");
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
            this.ownerName = tag.getString("OwnerName");
        }
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
        SimpleContainer container = new SimpleContainer(10);
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
}