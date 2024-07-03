package top.ribs.scguns.entity.monster;


import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.entity.projectile.BrassBoltEntity;
import top.ribs.scguns.init.ModSounds;

import java.util.EnumSet;

public class SkyCarrierEntity extends FlyingMob implements Enemy {
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(SkyCarrierEntity.class, EntityDataSerializers.BOOLEAN);
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public SkyCarrierEntity(EntityType<? extends SkyCarrierEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new SkyCarrierMoveControl(this, 5.0, 8.0, 1.5, 0.5, 0.2);
    }


    @Override
    public boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RandomFloatAroundGoal(this, 100));
        this.goalSelector.addGoal(3, new ShootProjectileGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(6, new SkyCarrierFaceAndBackAwayFromTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 30D)
                .add(Attributes.FOLLOW_RANGE, 50D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.1f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0f)
                .add(Attributes.ATTACK_DAMAGE, 2f);
    }

    private void setupAnimationStates() {
        if (idleAnimationTimeout <= 0) {
            idleAnimationTimeout = random.nextInt(40) + 80;
            idleAnimationState.start(tickCount);
        } else {
            --idleAnimationTimeout;
        }
    }
    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;
        if(this.getPose() == Pose.STANDING) {
            f = Math.min(pPartialTick * 6F, 1f);
        } else {
            f = 0f;
        }
        this.walkAnimation.update(f, 0.2f);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_CHARGING, false);
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BEACON_AMBIENT;
    }
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    ///MOVE CONTROL
    private static class SkyCarrierMoveControl extends MoveControl {
        private final SkyCarrierEntity skyCarrier;
        private final double minDistance;
        private final double maxDistance;
        private final double bufferZone;
        private final double approachSpeed;
        private final double backingSpeed;

        public SkyCarrierMoveControl(SkyCarrierEntity skyCarrier, double minDistance, double maxDistance, double bufferZone, double approachSpeed, double backingSpeed) {
            super(skyCarrier);
            this.skyCarrier = skyCarrier;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.bufferZone = bufferZone;
            this.approachSpeed = approachSpeed;
            this.backingSpeed = backingSpeed;
        }

        @Override
        public void tick() {
            LivingEntity target = this.skyCarrier.getTarget();
            if (target != null) {
                Vec3 targetPos = target.position();
                Vec3 ourPos = this.skyCarrier.position();
                Vec3 directionToTarget = targetPos.subtract(ourPos);
                double distance = directionToTarget.length();
                Vec3 movementVector;
                if (distance < minDistance) {
                    movementVector = directionToTarget.normalize().reverse().scale(backingSpeed);
                    this.skyCarrier.setDeltaMovement(movementVector);
                    updateRotationTowardsTarget(targetPos);
                } else if (distance > maxDistance + bufferZone) {
                    movementVector = directionToTarget.normalize().scale(approachSpeed);
                    this.skyCarrier.setDeltaMovement(movementVector);
                    updateRotationTowardsDirection(movementVector);
                } else if (distance < minDistance - bufferZone) {
                    movementVector = directionToTarget.normalize().reverse().scale(backingSpeed);
                    this.skyCarrier.setDeltaMovement(movementVector);
                    updateRotationTowardsTarget(targetPos);
                } else {
                    this.skyCarrier.setDeltaMovement(Vec3.ZERO);
                    updateRotationTowardsDirection(directionToTarget.normalize());
                }
            } else {
                handleIdleMovement();
            }
        }

        private void handleIdleMovement() {
            if (this.operation == Operation.MOVE_TO) {
                Vec3 direction = new Vec3(this.wantedX - this.skyCarrier.getX(), this.wantedY - this.skyCarrier.getY(), this.wantedZ - this.skyCarrier.getZ());
                double distance = direction.length();
                if (distance < 1.0) {
                    this.operation = Operation.WAIT;
                    this.skyCarrier.setDeltaMovement(Vec3.ZERO);
                } else {
                    direction = direction.normalize().scale(approachSpeed);
                    this.skyCarrier.setDeltaMovement(direction);
                    updateRotationTowardsDirection(direction);
                }
            }
        }

        private void updateRotationTowardsTarget(Vec3 targetPos) {
            double dx = targetPos.x - this.skyCarrier.getX();
            double dz = targetPos.z - this.skyCarrier.getZ();
            float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI) - 90.0);
            this.skyCarrier.setYRot(targetYaw);
            this.skyCarrier.yBodyRot = this.skyCarrier.getYRot();
            this.skyCarrier.yHeadRot = this.skyCarrier.getYRot();
        }

        private void updateRotationTowardsDirection(Vec3 direction) {
            if (direction.lengthSqr() == 0) return;
            float targetYaw = (float) (Math.atan2(direction.z, direction.x) * (180.0 / Math.PI) - 90.0);
            this.skyCarrier.setYRot(targetYaw);
            this.skyCarrier.yBodyRot = this.skyCarrier.getYRot();
            this.skyCarrier.yHeadRot = this.skyCarrier.getYRot();
        }
    }


    ////FLOAT
    public static class RandomFloatAroundGoal extends Goal {
        private final SkyCarrierEntity skyCarrier;
        private int tickDelay;

        public RandomFloatAroundGoal(SkyCarrierEntity skyCarrier, int initialDelay) {
            this.skyCarrier = skyCarrier;
            this.tickDelay = initialDelay;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }
        @Override
        public boolean canUse() {
            return --this.tickDelay <= 0;
        }
        @Override
        public void start() {
            this.setNewWanderTarget();
            this.tickDelay = 100;
        }
        private void setNewWanderTarget() {
            double x = this.skyCarrier.getX() + (this.skyCarrier.getRandom().nextDouble() * 20.0 - 10.0);
            double y = this.skyCarrier.getY() + (this.skyCarrier.getRandom().nextDouble() * 20.0 - 10.0);
            double z = this.skyCarrier.getZ() + (this.skyCarrier.getRandom().nextDouble() * 20.0 - 10.0);
            this.skyCarrier.getMoveControl().setWantedPosition(x, y, z, 1.0);
        }
    }
    @Override
    protected @NotNull PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }
    ///LOOK
    private static class SkyCarrierFaceAndBackAwayFromTargetGoal extends Goal {
        private final SkyCarrierEntity skyCarrier;
        public SkyCarrierFaceAndBackAwayFromTargetGoal(SkyCarrierEntity skyCarrier) {
            this.skyCarrier = skyCarrier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }
        @Override
        public boolean canUse() {
            return this.skyCarrier.getTarget() != null;
        }
        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
        @Override
        public void tick() {
            LivingEntity target = this.skyCarrier.getTarget();
            if (target != null) {
                Vec3 targetPos = new Vec3(target.getX(), target.getY(), target.getZ());
                Vec3 ourPos = new Vec3(this.skyCarrier.getX(), this.skyCarrier.getY(), this.skyCarrier.getZ());
                Vec3 vectorToTarget = targetPos.subtract(ourPos).normalize();
                float targetYaw = -((float)Math.atan2(vectorToTarget.x, vectorToTarget.z)) * (180F / (float)Math.PI);
                targetYaw = Mth.wrapDegrees(targetYaw);
                this.skyCarrier.setYRot(targetYaw);
                this.skyCarrier.yBodyRot = this.skyCarrier.getYRot();
                this.skyCarrier.yHeadRot = this.skyCarrier.yBodyRot;
            }
        }
    }
    public static class ShootProjectileGoal extends Goal {
        private final SkyCarrierEntity skyCarrier;
        private int cooldown;

        public ShootProjectileGoal(SkyCarrierEntity skyCarrier) {
            this.skyCarrier = skyCarrier;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.skyCarrier.getTarget();
            return target != null && this.skyCarrier.distanceToSqr(target) < 256;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && this.cooldown > 0;
        }

        @Override
        public void start() {
            this.cooldown = 20;
        }

        @Override
        public void stop() {
            this.cooldown = 0;
        }

        @Override
        public void tick() {
            if (--this.cooldown <= 0) {
                this.fireProjectile();
                this.cooldown = 20;
            }
        }

        private void fireProjectile() {
            LivingEntity target = this.skyCarrier.getTarget();
            if (target != null) {
                double turretOffsetHeight = 0.4;
                double turretOffsetBack = 0.4;
                double spawnHeight = this.skyCarrier.getY() + this.skyCarrier.getBbHeight() + turretOffsetHeight;
                double spawnX = this.skyCarrier.getX() - Math.sin(Math.toRadians(this.skyCarrier.getYRot())) * turretOffsetBack;
                double spawnZ = this.skyCarrier.getZ() + Math.cos(Math.toRadians(this.skyCarrier.getYRot())) * turretOffsetBack;
                BrassBoltEntity brassBolt = new BrassBoltEntity(this.skyCarrier.level(), this.skyCarrier);
                brassBolt.setPos(spawnX, spawnHeight, spawnZ);
                double dx = target.getX() - spawnX;
                double dy = target.getEyeY() - spawnHeight;
                double dz = target.getZ() - spawnZ;
                brassBolt.shoot(dx, dy, dz, 1.5f, 6.0f);
                this.skyCarrier.level().addFreshEntity(brassBolt);
                this.skyCarrier.level().playSound(null, this.skyCarrier.getX(), this.skyCarrier.getY(), this.skyCarrier.getZ(), ModSounds.BRUISER_SILENCED_FIRE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
    }

}

