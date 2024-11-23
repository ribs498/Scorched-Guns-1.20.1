package top.ribs.scguns.blockentity;

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
import top.ribs.scguns.Config;
import top.ribs.scguns.block.*;
import top.ribs.scguns.entity.projectile.turret.TurretProjectileEntity;
import top.ribs.scguns.init.ModBlockEntities;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModSounds;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.EnemyLogItem;
import top.ribs.scguns.item.TeamLogItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageMuzzleFlash;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class EnemyTurretBlockEntity extends BlockEntity {
    private static final double TARGETING_RADIUS = 24.0;
    private static final int COOLDOWN = 40;
    private static final float MAX_PITCH = 60.0F;
    private static final float MIN_PITCH = -25.0F;
    private static final float POSITION_SMOOTHING_FACTOR = 0.2F;
    private static final float ROTATION_SPEED = 0.45F;
    private static final float RECOIL_MAX = 4.0F;
    private static final float RECOIL_SPEED = 0.3F;
    private static final double MINIMUM_FIRING_DISTANCE = 1.3;
    private static final float INACCURACY = 0.05F;

    private Player target;
    private float yaw;
    private float pitch;
    private float previousYaw;
    private float previousPitch;
    private double smoothedTargetX;
    private double smoothedTargetY;
    private double smoothedTargetZ;
    private float recoilPitchOffset = 0.0F;
    private int cooldown = COOLDOWN;
    private boolean disabled = false;
    private int disableCooldown = 0;
    private static final int MAX_DISABLE_TIME = 200;
    private float disabledRotationOffset = 0.0F;

    // Add a damage multiplier variable
    private float damageMultiplier = 1.0F;

    public EnemyTurretBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENEMY_TURRET.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnemyTurretBlockEntity turret) {
        if (turret.cooldown > 0) {
            turret.cooldown--;
        }
        turret.tickRecoil();

        if (turret.disabled) {
            turret.handleDisabled();
        } else {
            turret.findTarget(level, pos);
            turret.updateRotation();

            if (turret.target != null && turret.cooldown <= 0 && turret.isReadyToFire()) {
                turret.fire();
                turret.cooldown = COOLDOWN;
            }
        }
    }

    private void handleDisabled() {
        disableCooldown--;
        if (disableCooldown <= 0) {
            disabled = false;
            disableCooldown = 0;
            disabledRotationOffset = 0.0F;
        } else {
            disabledRotationOffset = (float) Math.sin(disableCooldown * 0.1) * 5.0F;
        }
        resetToRestPosition();
    }

    private void findTarget(Level level, BlockPos pos) {
        target = null;
        Vec3 turretPos = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5);
        AABB searchBox = new AABB(pos).inflate(TARGETING_RADIUS, TARGETING_RADIUS, TARGETING_RADIUS);

        List<Player> potentialTargets = level.getEntitiesOfClass(Player.class, searchBox,
                player -> player != null
                        && player.isAlive()
                        && !player.isCreative()
                        && !player.isSpectator()
                        && hasLineOfSight(level, turretPos, player));

        if (!potentialTargets.isEmpty()) {
            target = potentialTargets.stream()
                    .min(Comparator.comparingDouble(player -> player.distanceToSqr(turretPos)))
                    .orElse(null);

            updateTargetPosition();
        }
    }

    private void updateTargetPosition() {
        if (target != null) {
            double predictedX = target.getX() + target.getDeltaMovement().x * 7;
            double predictedY = target.getY() + target.getEyeHeight() + target.getDeltaMovement().y * 7;
            double predictedZ = target.getZ() + target.getDeltaMovement().z * 7;

            smoothedTargetX = lerp(smoothedTargetX, predictedX, POSITION_SMOOTHING_FACTOR);
            smoothedTargetY = lerp(smoothedTargetY, predictedY, POSITION_SMOOTHING_FACTOR);
            smoothedTargetZ = lerp(smoothedTargetZ, predictedZ, POSITION_SMOOTHING_FACTOR);
        }
    }

    private void updateRotation() {
        previousYaw = yaw;
        previousPitch = pitch;

        if (smoothedTargetX != 0 || smoothedTargetZ != 0) {
            updateYaw();
            updatePitch();
        }
    }

    private void updateYaw() {
        double dx = smoothedTargetX - (worldPosition.getX() + 0.5);
        double dz = smoothedTargetZ - (worldPosition.getZ() + 0.5);
        float targetYaw = (float) (Math.atan2(dx, dz) * (180 / Math.PI)) + 180;
        targetYaw = (targetYaw + 360) % 360;

        float yawDifference = targetYaw - yaw;
        if (yawDifference > 180) {
            yawDifference -= 360;
        } else if (yawDifference < -180) {
            yawDifference += 360;
        }

        yaw += yawDifference * ROTATION_SPEED;
        yaw = yaw % 360.0F;
        if (yaw < 0) yaw += 360.0F;
    }

    private void updatePitch() {
        if (smoothedTargetY != 0) {
            double dx = smoothedTargetX - (worldPosition.getX() + 0.5);
            double dy = smoothedTargetY - (worldPosition.getY() + 1.0);
            double dz = smoothedTargetZ - (worldPosition.getZ() + 0.5);
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

            float targetPitch = (float) (Math.atan2(dy, horizontalDistance) * (180 / Math.PI));
            targetPitch = Mth.clamp(targetPitch, MIN_PITCH, MAX_PITCH);
            float pitchDifference = targetPitch - pitch;
            pitch += pitchDifference * ROTATION_SPEED;
            pitch = Mth.clamp(pitch, MIN_PITCH, MAX_PITCH);
        }
    }
    private boolean isReadyToFire() {
        if (target == null) return false;
        double dx = smoothedTargetX - (worldPosition.getX() + 0.5);
        double dy = smoothedTargetY - (worldPosition.getY() + 1.0);
        double dz = smoothedTargetZ - (worldPosition.getZ() + 0.5);
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Math.atan2(dx, dz) * (180 / Math.PI)) + 180;
        targetYaw = (targetYaw + 360) % 360;
        float targetPitch = (float) (Math.atan2(dy, horizontalDistance) * (180 / Math.PI));
        targetPitch = Mth.clamp(targetPitch, MIN_PITCH, MAX_PITCH);

        float yawDifference = Math.abs(targetYaw - yaw);
        if (yawDifference > 180) yawDifference = 360 - yawDifference;

        float pitchDifference = Math.abs(targetPitch - pitch);
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        return distanceSquared >= MINIMUM_FIRING_DISTANCE * MINIMUM_FIRING_DISTANCE
                && yawDifference < 2.0F && pitchDifference < 2.0F;
    }

    private void fire() {
        if (level == null || target == null) {
            return;
        }

        Vec3 muzzlePos = getMuzzlePosition(yaw, pitch);
        Vec3 targetPos = new Vec3(target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ());
        Vec3 direction = targetPos.subtract(muzzlePos).normalize();
        direction = direction.add(
                level.random.triangle(0, INACCURACY),
                level.random.triangle(0, INACCURACY),
                level.random.triangle(0, INACCURACY)
        ).normalize();

        TurretProjectileEntity projectile = new TurretProjectileEntity(level, TurretProjectileEntity.BulletType.COMPACT_COPPER_ROUND);
        projectile.setPos(muzzlePos.x, muzzlePos.y, muzzlePos.z);
        projectile.shoot(direction.x, direction.y, direction.z, 3.0F, 0.0F);
        projectile.setBaseDamage(2.5* damageMultiplier);

        level.addFreshEntity(projectile);
        level.playSound(null, worldPosition, ModSounds.IRON_RIFLE_FIRE.get(), SoundSource.BLOCKS, 0.7F, 0.7F);
        recoilPitchOffset = RECOIL_MAX;
    }

    private Vec3 getMuzzlePosition(float yaw, float pitch) {
        double muzzleLength = 1.0;
        double muzzleOffsetY = 1.4;
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double muzzleX = -Math.sin(yawRad) * Math.cos(pitchRad) * muzzleLength;
        double muzzleY = Math.sin(pitchRad) * muzzleLength + muzzleOffsetY;
        double muzzleZ = -Math.cos(yawRad) * Math.cos(pitchRad) * muzzleLength;
        return new Vec3(
                worldPosition.getX() + 0.5 + muzzleX,
                worldPosition.getY() + muzzleY,
                worldPosition.getZ() + 0.5 + muzzleZ
        );
    }

    private boolean hasLineOfSight(Level level, Vec3 turretPos, LivingEntity target) {
        Vec3 targetPos = target.getEyePosition();
        Vec3 toTarget = targetPos.subtract(turretPos);
        double distance = toTarget.length();
        Vec3 rayVector = toTarget.normalize().scale(distance);

        Vec3 adjustedTurretPos = turretPos.add(0, 0.5, 0);

        ClipContext clipContext = new ClipContext(adjustedTurretPos, adjustedTurretPos.add(rayVector), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        BlockHitResult hitResult = level.clip(clipContext);

        return hitResult.getType() == HitResult.Type.MISS;
    }

    private void tickRecoil() {
        if (recoilPitchOffset > 0) {
            recoilPitchOffset -= RECOIL_SPEED;
            if (recoilPitchOffset < 0) {
                recoilPitchOffset = 0;
            }
        }
    }

    private void resetToRestPosition() {
        target = null;
        float restingYaw = 0.0F;
        float restingPitch = -30.0F;
        previousYaw = yaw;
        previousPitch = pitch;
        float yawDifference = (restingYaw + disabledRotationOffset) - yaw;
        if (yawDifference > 180) yawDifference -= 360;
        else if (yawDifference < -180) yawDifference += 360;
        yaw += yawDifference * ROTATION_SPEED;
        yaw = yaw % 360.0F;
        if (yaw < 0) yaw += 360.0F;

        float pitchDifference = restingPitch - pitch;
        pitch += pitchDifference * ROTATION_SPEED;
        smoothedTargetX = 0;
        smoothedTargetY = 0;
        smoothedTargetZ = 0;
    }

    public void onHitByLightningProjectile() {
        disabled = true;
        disableCooldown = MAX_DISABLE_TIME;
        resetToRestPosition();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            spawnDisableParticles();
            level.playSound(null, worldPosition, SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.BLOCKS, 1.0F, 0.5F);
        }
    }

    private void spawnDisableParticles() {
        if (level instanceof ServerLevel serverLevel) {
            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 1.0;
            double z = worldPosition.getZ() + 0.5;

            int particleCount = 20;
            double spread = 0.5;

            for (int i = 0; i < particleCount; i++) {
                double offsetX = level.random.nextDouble() * spread - spread / 2;
                double offsetY = level.random.nextDouble() * spread;
                double offsetZ = level.random.nextDouble() * spread - spread / 2;

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        x + offsetX, y + offsetY, z + offsetZ,
                        1, 0, 0, 0, 0.05);
            }
            serverLevel.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    // Getters and setters
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getPreviousYaw() { return previousYaw; }
    public float getPreviousPitch() { return previousPitch; }
    public float getRecoilPitchOffset() { return recoilPitchOffset; }

    // Method to set damage multiplier
    public void setDamageMultiplier(float multiplier) {
        this.damageMultiplier = multiplier;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("Yaw", yaw);
        tag.putFloat("Pitch", pitch);
        tag.putBoolean("Disabled", disabled);
        tag.putInt("DisableCooldown", disableCooldown);
        tag.putFloat("DamageMultiplier", damageMultiplier);
        tag.putFloat("DisabledRotationOffset", disabledRotationOffset);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        yaw = tag.getFloat("Yaw");
        previousYaw = yaw;
        pitch = tag.getFloat("Pitch");
        previousPitch = pitch;
        disabled = tag.getBoolean("Disabled");
        disableCooldown = tag.getInt("DisableCooldown");
        damageMultiplier = tag.getFloat("DamageMultiplier");
        disabledRotationOffset = tag.getFloat("DisabledRotationOffset");
    }
}