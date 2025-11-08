package top.ribs.scguns.entity.monster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.entity.projectile.EnemyProjectileEntity;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModSounds;

import java.util.EnumSet;
import java.util.List;

public class SkyCarrierEntity extends FlyingMob implements Enemy {
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(SkyCarrierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MUZZLE_FLASH_TIMER = SynchedEntityData.defineId(SkyCarrierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_PHASING = SynchedEntityData.defineId(SkyCarrierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGE_ATTACKING = SynchedEntityData.defineId(SkyCarrierEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private int shootCooldown = 0;
    private int chargeAttackCooldown = 0;

    private Vec3 initialTargetPosition = null;
    private int phasingTimer = 0;
    private static final int MAX_PHASING_TIME = 200;

    public SkyCarrierEntity(EntityType<? extends SkyCarrierEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new SkyCarrierMoveControl(this, 5.0, 8.0, 2.0, 0.9, 0.15);
    }

    public void setInitialTarget(Vec3 targetPosition) {
        this.initialTargetPosition = targetPosition;
        this.phasingTimer = MAX_PHASING_TIME;
        this.setPhasing(true);

        Vec3 direction = targetPosition.subtract(this.position()).normalize();
        float targetYaw = (float) (Math.atan2(direction.z, direction.x) * (180.0 / Math.PI) - 90.0);
        this.setYRot(targetYaw);
        this.yBodyRot = targetYaw;
        this.yHeadRot = targetYaw;
    }

    public boolean isPhasing() {
        return this.entityData.get(DATA_IS_PHASING);
    }

    public void setPhasing(boolean phasing) {
        this.entityData.set(DATA_IS_PHASING, phasing);
        this.noPhysics = phasing;
    }

    public boolean isChargeAttacking() {
        return this.entityData.get(DATA_IS_CHARGE_ATTACKING);
    }

    public void setChargeAttacking(boolean charging) {
        this.entityData.set(DATA_IS_CHARGE_ATTACKING, charging);
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance pPotionEffect) {
        MobEffect effect = pPotionEffect.getEffect();

        if (effect == MobEffects.POISON ||
                effect == MobEffects.WITHER ||
                effect == MobEffects.HUNGER ||
                effect == MobEffects.REGENERATION ||
                effect == MobEffects.SATURATION ||
                effect == MobEffects.CONFUSION ||
                effect == MobEffects.BLINDNESS ||
                effect == MobEffects.WEAKNESS ||
                effect == MobEffects.MOVEMENT_SLOWDOWN ||
                effect == MobEffects.DIG_SLOWDOWN ||
                effect == MobEffects.HARM ||
                effect == MobEffects.HEAL) {
            return false;
        }

        return super.canBeAffected(pPotionEffect);
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.isPhasing()) {
                if (this.getTarget() != null) {
                    this.setPhasing(false);
                    this.initialTargetPosition = null;
                } else {
                    handlePhasing();
                }
            }
        }

        if (this.level().isClientSide()) {
            setupAnimationStates();
            spawnSmokeParticles();
        } else {
            if (shootCooldown > 0) {
                shootCooldown--;
            } else {
                LivingEntity target = this.getTarget();
                if (target != null && this.distanceToSqr(target) < 625.0 && !this.isPhasing()) {
                    fireProjectile();
                    shootCooldown = 15;
                }
            }

            if (chargeAttackCooldown > 0) {
                chargeAttackCooldown--;
            }
        }

        int currentTimer = this.entityData.get(MUZZLE_FLASH_TIMER);
        if (currentTimer > 0) {
            this.entityData.set(MUZZLE_FLASH_TIMER, currentTimer - 1);
        }
    }

    private void handlePhasing() {
        if (this.initialTargetPosition == null) {
            this.setPhasing(false);
            return;
        }
        double distanceToTarget = this.position().distanceTo(this.initialTargetPosition);
        this.phasingTimer--;

        if (distanceToTarget < 3.0 || this.phasingTimer <= 0) {
            this.setPhasing(false);
            this.initialTargetPosition = null;
        } else {
            Vec3 direction = this.initialTargetPosition.subtract(this.position()).normalize();
            this.setDeltaMovement(direction.scale(0.3));

            float targetYaw = (float) (Math.atan2(direction.z, direction.x) * (180.0 / Math.PI) - 90.0);
            this.setYRot(targetYaw);
            this.yBodyRot = targetYaw;
            this.yHeadRot = targetYaw;
        }
    }


    private void spawnSmokeParticles() {
        if (this.isMuzzleFlashVisible()) {
            double offsetX = 0.0;
            double offsetY = this.getEyeHeight() - 0.5;
            double offsetZ = 0.0;
            double posX = this.getX() + offsetX;
            double posY = this.getY() + offsetY;
            double posZ = this.getZ() + offsetZ;
            RandomSource random = this.getRandom();
            for (int i = 0; i < 1; i++) {
                double particleOffsetX = random.nextGaussian() * 0.1;
                double particleOffsetY = random.nextGaussian() * 0.1;
                double particleOffsetZ = random.nextGaussian() * 0.1;
                this.level().addParticle(ParticleTypes.SMOKE, posX, posY, posZ, particleOffsetX, particleOffsetY, particleOffsetZ);
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RandomFloatAroundGoal(this, 100));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(6, new SkyCarrierFaceAndBackAwayFromTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 32D)
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
        float f = this.getPose() == Pose.STANDING ? Math.min(pPartialTick * 6F, 1f) : 0f;
        this.walkAnimation.update(f, 0.2f);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_CHARGING, false);
        this.entityData.define(MUZZLE_FLASH_TIMER, 0);
        this.entityData.define(DATA_IS_PHASING, false);
        this.entityData.define(DATA_IS_CHARGE_ATTACKING, false);
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

    public void triggerMuzzleFlash() {
        this.entityData.set(MUZZLE_FLASH_TIMER, 6);
    }

    public boolean isMuzzleFlashVisible() {
        return this.entityData.get(MUZZLE_FLASH_TIMER) > 0;
    }

    private void fireProjectile() {
        LivingEntity target = this.getTarget();
        if (target != null) {
            double turretOffsetHeight = 0.4;
            double turretOffsetBack = 0.4;
            double spawnHeight = this.getY() + this.getBbHeight() + turretOffsetHeight;
            double spawnX = this.getX() - Math.sin(Math.toRadians(this.getYRot())) * turretOffsetBack;
            double spawnZ = this.getZ() + Math.cos(Math.toRadians(this.getYRot())) * turretOffsetBack;
            EnemyProjectileEntity brassBolt = new EnemyProjectileEntity(this.level(), this);
            brassBolt.setPos(spawnX, spawnHeight, spawnZ);
            double dx = target.getX() - spawnX;
            double dy = target.getEyeY() - spawnHeight + 0.1;
            double dz = target.getZ() - spawnZ;
            brassBolt.shoot(dx, dy, dz, 3.0f, 2.0f);
            this.level().addFreshEntity(brassBolt);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.BRUISER_SILENCED_FIRE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
            this.triggerMuzzleFlash();
        }
    }

    private static class SkyCarrierMoveControl extends MoveControl {
        private final SkyCarrierEntity skyCarrier;
        private final double minDistance;
        private final double maxDistance;
        private final double bufferZone;
        private final double maxSpeed;
        private final double backingSpeed;

        private Vec3 currentVelocity = Vec3.ZERO;
        private float currentYaw = 0;
        private float targetYaw = 0;
        private int chargeTicks = 0;
        private Vec3 chargeDirection = Vec3.ZERO;

        public SkyCarrierMoveControl(SkyCarrierEntity skyCarrier, double minDistance, double maxDistance, double bufferZone, double maxSpeed, double backingSpeed) {
            super(skyCarrier);
            this.skyCarrier = skyCarrier;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.bufferZone = bufferZone;
            this.maxSpeed = maxSpeed;
            this.backingSpeed = backingSpeed;
            this.currentYaw = skyCarrier.getYRot();
        }

        @Override
        public void tick() {
            if (this.skyCarrier.isPhasing()) {
                return;
            }

            if (this.skyCarrier.isChargeAttacking()) {
                handleChargeAttack();
                return;
            }

            LivingEntity target = this.skyCarrier.getTarget();
            Vec3 desiredVelocity = Vec3.ZERO;

            AABB repulsionBox = this.skyCarrier.getBoundingBox().inflate(2.0);
            List<SkyCarrierEntity> nearbyCarriers = this.skyCarrier.level().getEntitiesOfClass(SkyCarrierEntity.class, repulsionBox, e -> e != this.skyCarrier);
            Vec3 repulsionVector = Vec3.ZERO;
            for (SkyCarrierEntity other : nearbyCarriers) {
                Vec3 toOther = this.skyCarrier.position().subtract(other.position());
                double distance = toOther.length();
                if (distance < 2.0 && distance > 0) {
                    repulsionVector = repulsionVector.add(toOther.normalize().scale(0.5 / distance));
                }
            }

            if (target != null) {
                Vec3 targetPos = target.position();
                Vec3 ourPos = this.skyCarrier.position();
                Vec3 directionToTarget = targetPos.subtract(ourPos);
                double distance = directionToTarget.length();

                if (distance < minDistance) {
                    desiredVelocity = directionToTarget.normalize().reverse().scale(backingSpeed);

                    if (this.skyCarrier.chargeAttackCooldown <= 0 && this.skyCarrier.getRandom().nextFloat() < 0.08f) {
                        initiateChargeAttack(directionToTarget.normalize());
                        return;
                    }
                } else if (distance > maxDistance + bufferZone) {
                    desiredVelocity = directionToTarget.normalize().scale(maxSpeed);
                } else if (distance < minDistance - bufferZone) {
                    desiredVelocity = directionToTarget.normalize().reverse().scale(backingSpeed);
                }

                desiredVelocity = desiredVelocity.add(repulsionVector.scale(0.3));
            } else {
                handleIdleMovement();
                return;
            }

            double acceleration = 0.15;
            double deceleration = 0.88;

            currentVelocity = currentVelocity.scale(deceleration);
            Vec3 accelerationVec = desiredVelocity.subtract(currentVelocity).scale(acceleration);
            currentVelocity = currentVelocity.add(accelerationVec);

            double currentSpeed = currentVelocity.length();
            if (currentSpeed > maxSpeed) {
                currentVelocity = currentVelocity.normalize().scale(maxSpeed);
            }

            this.skyCarrier.setDeltaMovement(currentVelocity);

            double dx = target.getX() - this.skyCarrier.getX();
            double dz = target.getZ() - this.skyCarrier.getZ();
            targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI) - 90.0);

            float yawDifference = Mth.wrapDegrees(targetYaw - currentYaw);
            float maxTurnSpeed = 9.0f;
            float turnAmount = Mth.clamp(yawDifference, -maxTurnSpeed, maxTurnSpeed);

            currentYaw = Mth.wrapDegrees(currentYaw + turnAmount);

            this.skyCarrier.setYRot(currentYaw);
            this.skyCarrier.yBodyRot = currentYaw;
            this.skyCarrier.yHeadRot = currentYaw;
        }

        private void initiateChargeAttack(Vec3 direction) {
            this.skyCarrier.setChargeAttacking(true);
            this.chargeDirection = direction;
            this.chargeTicks = 15;
            this.skyCarrier.chargeAttackCooldown = 100;
            this.skyCarrier.level().playSound(null, this.skyCarrier.getX(), this.skyCarrier.getY(),
                    this.skyCarrier.getZ(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 0.8F, 1.5F);
        }

        private void handleChargeAttack() {
            if (chargeTicks > 0) {
                chargeTicks--;

                double chargeSpeed = 1.2;
                this.skyCarrier.setDeltaMovement(chargeDirection.scale(chargeSpeed));

                LivingEntity target = this.skyCarrier.getTarget();
                if (target != null) {
                    AABB collisionBox = this.skyCarrier.getBoundingBox().inflate(0.5);
                    if (collisionBox.intersects(target.getBoundingBox())) {
                        performChargeImpact(target);
                        return;
                    }
                }

                AABB nearbyBox = this.skyCarrier.getBoundingBox().inflate(1.5);
                List<LivingEntity> nearbyEntities = this.skyCarrier.level().getEntitiesOfClass(
                        LivingEntity.class,
                        nearbyBox,
                        entity -> entity != this.skyCarrier && entity instanceof Player
                );

                for (LivingEntity entity : nearbyEntities) {
                    if (this.skyCarrier.hasLineOfSight(entity)) {
                        performChargeImpact(entity);
                        return;
                    }
                }

                if (chargeTicks <= 0) {
                    this.skyCarrier.setChargeAttacking(false);
                }
            } else {
                this.skyCarrier.setChargeAttacking(false);
            }
        }

        private void performChargeImpact(LivingEntity target) {
            float damage = 4.0f;
            target.hurt(this.skyCarrier.damageSources().mobAttack(this.skyCarrier), damage);

            Vec3 targetPos = target.position();
            Vec3 carrierPos = this.skyCarrier.position();
            Vec3 knockbackDir = targetPos.subtract(carrierPos).normalize();

            double knockbackStrength = 1.5;
            target.setDeltaMovement(
                    knockbackDir.x * knockbackStrength,
                    0.4,
                    knockbackDir.z * knockbackStrength
            );
            target.hurtMarked = true;

            this.skyCarrier.level().playSound(null, this.skyCarrier.getX(), this.skyCarrier.getY(),
                    this.skyCarrier.getZ(), SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.HOSTILE, 1.0F, 1.2F);

            this.skyCarrier.setChargeAttacking(false);
            this.chargeTicks = 0;
        }

        private void handleIdleMovement() {
            if (this.operation == Operation.MOVE_TO) {
                Vec3 direction = new Vec3(this.wantedX - this.skyCarrier.getX(), this.wantedY - this.skyCarrier.getY(), this.wantedZ - this.skyCarrier.getZ());
                double distance = direction.length();
                if (distance < 1.0) {
                    this.operation = Operation.WAIT;
                    currentVelocity = currentVelocity.scale(0.85);
                    this.skyCarrier.setDeltaMovement(currentVelocity);
                } else {
                    Vec3 desiredVelocity = direction.normalize().scale(Math.min(maxSpeed * 0.7, distance * 0.2));

                    currentVelocity = currentVelocity.scale(0.88).add(desiredVelocity.subtract(currentVelocity).scale(0.15));
                    this.skyCarrier.setDeltaMovement(currentVelocity);

                    if (currentVelocity.lengthSqr() > 0.001) {
                        targetYaw = (float) (Math.atan2(currentVelocity.z, currentVelocity.x) * (180.0 / Math.PI) - 90.0);
                        float yawDifference = Mth.wrapDegrees(targetYaw - currentYaw);
                        currentYaw = Mth.wrapDegrees(currentYaw + Mth.clamp(yawDifference, -9.0f, 9.0f));

                        this.skyCarrier.setYRot(currentYaw);
                        this.skyCarrier.yBodyRot = currentYaw;
                        this.skyCarrier.yHeadRot = currentYaw;
                    }
                }
            }
        }
    }

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
            return !this.skyCarrier.isPhasing() && --this.tickDelay <= 0;
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

    private static class SkyCarrierFaceAndBackAwayFromTargetGoal extends Goal {
        private final SkyCarrierEntity skyCarrier;
        private float currentYaw;

        public SkyCarrierFaceAndBackAwayFromTargetGoal(SkyCarrierEntity skyCarrier) {
            this.skyCarrier = skyCarrier;
            this.currentYaw = skyCarrier.getYRot();
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !this.skyCarrier.isPhasing() && this.skyCarrier.getTarget() != null;
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
                float targetYaw = -((float) Math.atan2(vectorToTarget.x, vectorToTarget.z)) * (180F / (float) Math.PI);
                targetYaw = Mth.wrapDegrees(targetYaw);

                float yawDiff = Mth.wrapDegrees(targetYaw - currentYaw);
                float turnSpeed = 4.0f;
                currentYaw = Mth.wrapDegrees(currentYaw + Mth.clamp(yawDiff, -turnSpeed, turnSpeed));

                this.skyCarrier.yHeadRot = currentYaw;
            }
        }
    }
}