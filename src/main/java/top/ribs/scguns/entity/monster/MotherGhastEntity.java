package top.ribs.scguns.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.entity.projectile.EnemyProjectileEntity;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModSounds;

import java.util.EnumSet;

public class MotherGhastEntity extends FlyingMob implements Enemy {
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(MotherGhastEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TURRET_FLASH_TIMER = SynchedEntityData.defineId(MotherGhastEntity.class, EntityDataSerializers.INT);
    private int explosionPower = 1;
    private int turretCooldown = 0;
    private int burstCounter = 0;
    private int burstPauseCooldown = 0;
    private static final int TURRET_COOLDOWN_TICKS = 3;
    private static final int BURST_SIZE = 12;
    private static final int BURST_PAUSE_TICKS = 40;
    private static final double TURRET_RANGE = 60.0;

    public MotherGhastEntity(EntityType<? extends MotherGhastEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
        this.moveControl = new MotherGhastEntity.MotherGhastMoveControl(this);
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MotherGhastEntity.MotherGhastShootFireballGoal(this));
        this.goalSelector.addGoal(2, new MotherGhastEntity.AggressiveFloatGoal(this));
        this.goalSelector.addGoal(5, new MotherGhastEntity.RandomFloatAroundGoal(this));
        this.goalSelector.addGoal(7, new MotherGhastEntity.MotherGhastLookGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (entity) -> {
                    if (entity instanceof Player player) {
                        return !player.isCreative() && !player.isSpectator() && Math.abs(entity.getY() - this.getY()) <= 4.0D;
                    }
                    return false;
                }));
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_IS_CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(DATA_IS_CHARGING, charging);
    }

    public int getExplosionPower() {
        return this.explosionPower;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    private static boolean isReflectedFireball(DamageSource damageSource) {
        return damageSource.getDirectEntity() instanceof LargeFireball && damageSource.getEntity() instanceof Player;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE) || (!isReflectedFireball(source) && super.isInvulnerableTo(source));
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }

        if (!this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker) {
            boolean shouldTarget = true;
            if (attacker instanceof Player player) {
                shouldTarget = !player.isCreative() && !player.isSpectator();
            }

            if (shouldTarget && this.getTarget() == null) {
                this.setTarget(attacker);
            }
        }

        if (isReflectedFireball(source)) {
            return super.hurt(source, 25.0F);
        } else {
            return super.hurt(source, amount);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_CHARGING, false);
        this.entityData.define(TURRET_FLASH_TIMER, 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0f)
                .add(Attributes.ARMOR, 12f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8f)
                .add(Attributes.FOLLOW_RANGE, 100.0D);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }
    @Override
    public float getVoicePitch() {
        return 0.7F;
    }


    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putByte("ExplosionPower", (byte)this.explosionPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ExplosionPower", 99)) {
            this.explosionPower = compound.getByte("ExplosionPower");
        }
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return 4.0F;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide && this.tickCount % 4 == 0) {
            spawnVentSmoke();
        }

        if (!this.level().isClientSide) {
            int flashTimer = this.entityData.get(TURRET_FLASH_TIMER);
            if (flashTimer > 0) {
                this.entityData.set(TURRET_FLASH_TIMER, flashTimer - 1);
            }

            if (turretCooldown > 0) {
                turretCooldown--;
            }

            if (burstPauseCooldown > 0) {
                burstPauseCooldown--;
            }

            LivingEntity target = this.getTarget();
            if (target != null && turretCooldown <= 0 && burstPauseCooldown <= 0) {
                double distanceSq = this.distanceToSqr(target);
                if (distanceSq < TURRET_RANGE * TURRET_RANGE && this.hasLineOfSight(target)) {
                    fireTurret(target);
                    turretCooldown = TURRET_COOLDOWN_TICKS;
                    burstCounter++;

                    if (burstCounter >= BURST_SIZE) {
                        burstCounter = 0;
                        burstPauseCooldown = BURST_PAUSE_TICKS;
                    }
                }
            }
        }
    }

    private void spawnVentSmoke() {
        float yaw = (float) Math.toRadians(this.getYRot());
        float cos = (float) Math.cos(yaw);
        float sin = (float) Math.sin(yaw);

        // Left vent
        double leftX = -4.5;
        double leftZ = -6.0;
        double vent1X = this.getX() + cos * leftX - sin * leftZ;
        double vent1Y = this.getY() + 6.5;
        double vent1Z = this.getZ() + sin * leftX + cos * leftZ;

        this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                vent1X, vent1Y, vent1Z,
                0.0, 0.05, 0.0);

        // Right vent
        double rightX = -4.5;
        double rightZ = -4.0;
        double vent2X = this.getX() + cos * rightX - sin * rightZ;
        double vent2Y = this.getY() + 6.5;
        double vent2Z = this.getZ() + sin * rightX + cos * rightZ;

        this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                vent2X, vent2Y, vent2Z,
                0.0, 0.05, 0.0);
    }

    private void fireTurret(LivingEntity target) {
        float yaw = this.getYRot() * ((float)Math.PI / 180F);
        double rightOffsetX = Math.cos(yaw) * 3.5;
        double rightOffsetZ = -Math.sin(yaw) * 3.5;

        Vec3 turretPos = this.position().add(rightOffsetX, 4.0, rightOffsetZ);

        double deltaX = target.getX() - turretPos.x;
        double deltaY = target.getY(0.5) - turretPos.y;
        double deltaZ = target.getZ() - turretPos.z;

        double horizontalDist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        deltaY += horizontalDist * 0.02;

        Vec3 direction = new Vec3(deltaX, deltaY, deltaZ).normalize();

        EnemyProjectileEntity projectile = new EnemyProjectileEntity(ModEntities.ENEMY_PROJECTILE.get(), this.level(), this);
        projectile.setPos(turretPos.x, turretPos.y, turretPos.z);
        projectile.setDeltaMovement(direction.scale(6.5));
        projectile.setNoGravity(true);

        this.level().addFreshEntity(projectile);
        triggerTurretFlash();

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.MACHINE_GUN_FIRE.get(), this.getSoundSource(), 1.0F, 1.0F + this.random.nextFloat() * 0.2F);
    }

    public void triggerTurretFlash() {
        this.entityData.set(TURRET_FLASH_TIMER, 2);
    }

    public boolean isTurretFlashVisible() {
        return this.entityData.get(TURRET_FLASH_TIMER) > 0;
    }

    static class MotherGhastLookGoal extends Goal {
        private final MotherGhastEntity ghast;

        public MotherGhastLookGoal(MotherGhastEntity ghast) {
            this.ghast = ghast;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.ghast.getTarget() == null) {
                Vec3 deltaMovement = this.ghast.getDeltaMovement();
                this.ghast.setYRot(-((float)Mth.atan2(deltaMovement.x, deltaMovement.z)) * (180F / (float)Math.PI));
                this.ghast.yBodyRot = this.ghast.getYRot();
            } else {
                LivingEntity target = this.ghast.getTarget();
                if (target.distanceToSqr(this.ghast) < 4096.0D) {
                    double deltaX = target.getX() - this.ghast.getX();
                    double deltaZ = target.getZ() - this.ghast.getZ();
                    this.ghast.setYRot(-((float)Mth.atan2(deltaX, deltaZ)) * (180F / (float)Math.PI));
                    this.ghast.yBodyRot = this.ghast.getYRot();
                }
            }
        }
    }

    static class MotherGhastMoveControl extends MoveControl {
        private final MotherGhastEntity ghast;
        private int floatDuration;

        public MotherGhastMoveControl(MotherGhastEntity ghast) {
            super(ghast);
            this.ghast = ghast;
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                if (this.floatDuration-- <= 0) {
                    this.floatDuration += this.ghast.getRandom().nextInt(5) + 2;
                    Vec3 vec3 = new Vec3(this.wantedX - this.ghast.getX(), this.wantedY - this.ghast.getY(), this.wantedZ - this.ghast.getZ());
                    double distance = vec3.length();
                    vec3 = vec3.normalize();
                    if (this.canReach(vec3, Mth.ceil(distance))) {
                        this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().add(vec3.scale(0.1D)));
                    } else {
                        this.operation = MoveControl.Operation.WAIT;
                    }
                }
            }
        }

        private boolean canReach(Vec3 pos, int length) {
            AABB aabb = this.ghast.getBoundingBox();
            for(int i = 1; i < length; i++) {
                aabb = aabb.move(pos);
                if (!this.ghast.level().noCollision(this.ghast, aabb)) {
                    return false;
                }
            }
            return true;
        }
    }

    static class MotherGhastShootFireballGoal extends Goal {
        private final MotherGhastEntity ghast;
        public int chargeTime;
        private int fireballBurstCount = 0;
        private int fireballsToShoot = 0;

        public MotherGhastShootFireballGoal(MotherGhastEntity ghast) {
            this.ghast = ghast;
        }

        @Override
        public boolean canUse() {
            return this.ghast.getTarget() != null;
        }

        @Override
        public void start() {
            this.chargeTime = 0;
            this.fireballBurstCount = 0;
            this.fireballsToShoot = 0;
        }

        @Override
        public void stop() {
            this.ghast.setCharging(false);
            this.fireballBurstCount = 0;
            this.fireballsToShoot = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.ghast.getTarget();
            if (target != null) {
                if (target.distanceToSqr(this.ghast) < 4096.0D && this.ghast.hasLineOfSight(target)) {
                    Level level = this.ghast.level();
                    this.chargeTime++;

                    if (this.chargeTime == 10 && !this.ghast.isSilent()) {
                        level.levelEvent(null, 1015, this.ghast.blockPosition(), 0);
                        this.fireballsToShoot = 2 + this.ghast.getRandom().nextInt(3);
                    }

                    if (this.chargeTime == 20 || (this.fireballBurstCount > 0 && this.chargeTime % 5 == 0)) {
                        if (this.fireballBurstCount < this.fireballsToShoot) {
                            Vec3 viewVector = this.ghast.getViewVector(1.0F);
                            double d2 = target.getX() - (this.ghast.getX() + viewVector.x * 4.0D);
                            double d3 = target.getY(0.5D) - (0.5D + this.ghast.getY(0.5D));
                            double d4 = target.getZ() - (this.ghast.getZ() + viewVector.z * 4.0D);

                            if (!this.ghast.isSilent() && this.fireballBurstCount == 0) {
                                level.levelEvent(null, 1016, this.ghast.blockPosition(), 0);
                            }

                            double speed = 2.0;
                            LargeFireball fireball = new LargeFireball(level, this.ghast, d2 * speed, d3 * speed, d4 * speed, 0);
                            fireball.setPos(this.ghast.getX() + viewVector.x * 4.0D, this.ghast.getY(0.5D) + 0.5D, fireball.getZ() + viewVector.z * 4.0D);
                            level.addFreshEntity(fireball);

                            this.fireballBurstCount++;

                            if (this.fireballBurstCount >= this.fireballsToShoot) {
                                this.chargeTime = -60;
                                this.fireballBurstCount = 0;
                                this.fireballsToShoot = 0;
                            }
                        }
                    }
                } else if (this.chargeTime > 0) {
                    this.chargeTime--;
                }

                this.ghast.setCharging(this.chargeTime > 10);
            }
        }
    }

    static class AggressiveFloatGoal extends Goal {
        private final MotherGhastEntity ghast;
        private static final double PREFERRED_DISTANCE = 35.0;
        private static final double MIN_DISTANCE = 25.0;
        private static final double MAX_DISTANCE = 50.0;

        public AggressiveFloatGoal(MotherGhastEntity ghast) {
            this.ghast = ghast;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.ghast.getTarget();
            if (target == null) {
                return false;
            }

            MoveControl moveControl = this.ghast.getMoveControl();
            if (!moveControl.hasWanted()) {
                return true;
            }

            double deltaX = moveControl.getWantedX() - this.ghast.getX();
            double deltaY = moveControl.getWantedY() - this.ghast.getY();
            double deltaZ = moveControl.getWantedZ() - this.ghast.getZ();
            double distanceSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
            return distanceSq < 1.0D || distanceSq > 900.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            LivingEntity target = this.ghast.getTarget();
            if (target == null) {
                return;
            }

            RandomSource random = this.ghast.getRandom();
            double currentDistance = this.ghast.distanceTo(target);

            double targetX, targetY, targetZ;

            if (currentDistance < MIN_DISTANCE) {
                double dx = this.ghast.getX() - target.getX();
                double dy = this.ghast.getY() - target.getY();
                double dz = this.ghast.getZ() - target.getZ();
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

                targetX = target.getX() + (dx / length) * PREFERRED_DISTANCE;
                targetY = target.getY() + (dy / length) * PREFERRED_DISTANCE + (random.nextFloat() * 2.0F - 1.0F) * 3.0F;
                targetZ = target.getZ() + (dz / length) * PREFERRED_DISTANCE;
            } else if (currentDistance > MAX_DISTANCE) {
                double dx = target.getX() - this.ghast.getX();
                double dy = target.getY() - this.ghast.getY();
                double dz = target.getZ() - this.ghast.getZ();
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

                targetX = this.ghast.getX() + (dx / length) * (currentDistance - PREFERRED_DISTANCE);
                targetY = this.ghast.getY() + (dy / length) * (currentDistance - PREFERRED_DISTANCE);
                targetZ = this.ghast.getZ() + (dz / length) * (currentDistance - PREFERRED_DISTANCE);
            } else {
                double angle = random.nextDouble() * Math.PI * 2;
                double distance = PREFERRED_DISTANCE * 0.5;

                targetX = target.getX() + Math.cos(angle) * distance;
                targetY = target.getY() + (random.nextFloat() * 6.0F - 3.0F);
                targetZ = target.getZ() + Math.sin(angle) * distance;
            }

            this.ghast.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.0D);
        }
    }

    static class RandomFloatAroundGoal extends Goal {
        private final MotherGhastEntity ghast;

        public RandomFloatAroundGoal(MotherGhastEntity ghast) {
            this.ghast = ghast;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.ghast.getTarget() != null) {
                return false;
            }

            MoveControl moveControl = this.ghast.getMoveControl();
            if (!moveControl.hasWanted()) {
                return true;
            }

            double deltaX = moveControl.getWantedX() - this.ghast.getX();
            double deltaY = moveControl.getWantedY() - this.ghast.getY();
            double deltaZ = moveControl.getWantedZ() - this.ghast.getZ();
            double distanceSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
            return distanceSq < 1.0D || distanceSq > 3600.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            RandomSource random = this.ghast.getRandom();
            double d0 = this.ghast.getX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d1 = this.ghast.getY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d2 = this.ghast.getZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            this.ghast.getMoveControl().setWantedPosition(d0, d1, d2, 1.0D);
        }
    }
}