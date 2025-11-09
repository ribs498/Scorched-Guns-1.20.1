package top.ribs.scguns.entity.monster;

import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.init.ModSounds;

import java.util.EnumSet;

public class DissidentEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(DissidentEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LEAPING =
            SynchedEntityData.defineId(DissidentEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LANDING =
            SynchedEntityData.defineId(DissidentEntity.class, EntityDataSerializers.BOOLEAN);

    public DissidentEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = XP_REWARD_LARGE;
    }

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;
    public int landingAnimationTimeout = 0;
    private int idleAnimationTimeout = 0;
    private boolean wasInAir = false;

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 40D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.5f)
                .add(Attributes.ARMOR, 2f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8f)
                .add(Attributes.ATTACK_DAMAGE, 5f);
    }
    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEAD;
    }
    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (wasInAir && this.onGround() && this.getDeltaMovement().y <= 0) {
                boolean wasLeaping = this.isLeaping();
                this.setLeaping(false);
                this.setLanding(true);
                this.landingAnimationTimeout = 4;

                if (wasLeaping) {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            wasInAir = !this.onGround();
        }

        if (this.level().isClientSide()) {
            setupAnimationStates();
        }

        if (this.landingAnimationTimeout > 0) {
            --this.landingAnimationTimeout;
            if (this.landingAnimationTimeout <= 0) {
                this.setLanding(false);
            }
        }
    }
    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
        if (this.isAttacking()) {
            if (attackAnimationTimeout <= 0) {
                attackAnimationTimeout = 12;
                attackAnimationState.start(this.tickCount);
            }
            --attackAnimationTimeout;
        } else {
            attackAnimationState.stop();
        }
    }
    @Override
    public double getPassengersRidingOffset() {
        return (double)this.getBbHeight() * 1.05D;
    }

    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Mob) {
            return (Mob)entity;
        }
        return null;
    }

    @Override
    protected void updateControlFlags() {
        boolean flag = !(this.getControllingPassenger() instanceof Mob);
        boolean flag1 = !(this.getVehicle() instanceof net.minecraft.world.entity.vehicle.Boat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, flag);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, flag && flag1);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, flag);
    }
    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        pSpawnData = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);

        if (pLevel.getRandom().nextFloat() < 0.25F) {
            Zombie babyZombie = EntityType.ZOMBIE.create(pLevel.getLevel());
            if (babyZombie != null) {
                babyZombie.setBaby(true);
                babyZombie.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                babyZombie.finalizeSpawn(pLevel, pDifficulty, pReason, null, null);

                if (pLevel.getRandom().nextFloat() < 0.5F) {
                    babyZombie.addTag("MobGunner");
                    babyZombie.addTag("ProgressionGunner");
                }

                babyZombie.startRiding(this);
                pLevel.addFreshEntity(babyZombie);
            }
        }

        return pSpawnData;
    }
    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }
    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(LEAPING, false);
        this.entityData.define(LANDING, false);
    }
    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;
        if (this.getPose() == Pose.STANDING) {
            f = Math.min(pPartialTick * 6F, 1f);
        } else {
            f = 0f;
        }
        this.walkAnimation.update(f, 0.2f);
    }
    public void setLeaping(boolean leaping) {
        this.entityData.set(LEAPING, leaping);
    }

    public boolean isLeaping() {
        return this.entityData.get(LEAPING);
    }

    public void setLanding(boolean landing) {
        this.entityData.set(LANDING, landing);
    }

    public boolean isLanding() {
        return this.entityData.get(LANDING);
    }
    @Override
    protected void pickUpItem(ItemEntity pItemEntity) {
        super.pickUpItem(pItemEntity);
    }
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LeapAttackGoal(this, 1.0, 10.0, 80));
        this.goalSelector.addGoal(2, new DissidentAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(DissidentEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.0, 30));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0));
    }
    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 5) {
            if (this.level().isClientSide) {
                for (int i = 0; i < 10; i++) {
                    this.level().addParticle(
                            net.minecraft.core.particles.ParticleTypes.POOF,
                            this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2,
                            this.getY(),
                            this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2,
                            (this.random.nextDouble() - 0.5) * 0.2,
                            this.random.nextDouble() * 0.1,
                            (this.random.nextDouble() - 0.5) * 0.2
                    );
                }
            }
        } else if (pId == 6) {
            if (this.level().isClientSide) {
                for (int i = 0; i < 30; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2.5;
                    double offsetZ = (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2.5;
                    this.level().addParticle(
                            net.minecraft.core.particles.ParticleTypes.POOF,
                            this.getX() + offsetX,
                            this.getY() + 0.1,
                            this.getZ() + offsetZ,
                            (this.random.nextDouble() - 0.5) * 0.3,
                            this.random.nextDouble() * 0.2,
                            (this.random.nextDouble() - 0.5) * 0.3
                    );
                }
            }
        } else {
            super.handleEntityEvent(pId);
        }
    }
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isLeaping() && !this.onGround() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.8, 1.0));
        }
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        if (this.isLeaping()) {
            return false;
        }
        return super.causeFallDamage(pFallDistance, pMultiplier, pSource);
    }
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.DISSIDENT_IDLE.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return ModSounds.DISSIDENT_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DISSIDENT_DIE.get();
    }

    public float getAttackSoundVolume() {
        return 1.0F;
    }


    public static class DissidentAttackGoal extends MeleeAttackGoal {
        private final DissidentEntity entity;
        private int attackDelay = 10;
        private int ticksUntilNextAttack = 10;
        private boolean shouldCountTillNextAttack = false;

        public DissidentAttackGoal(PathfinderMob pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
            super(pMob, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
            entity = ((DissidentEntity) pMob);
        }

        @Override
        public void start() {
            super.start();
            attackDelay = 10;
            ticksUntilNextAttack = 10;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
            if (isEnemyWithinAttackDistance(pEnemy, pDistToEnemySqr)) {
                shouldCountTillNextAttack = true;

                if (isTimeToStartAttackAnimation()) {
                    entity.setAttacking(true);
                }

                if (isTimeToAttack()) {
                    this.mob.getLookControl().setLookAt(pEnemy.getX(), pEnemy.getEyeY(), pEnemy.getZ());
                    performAttack(pEnemy);
                }
            } else {
                resetAttackCooldown();
                shouldCountTillNextAttack = false;
                entity.setAttacking(false);
                entity.attackAnimationTimeout = 0;
            }
        }

        private boolean isEnemyWithinAttackDistance(LivingEntity pEnemy, double pDistToEnemySqr) {
            double adjustedAttackDistance = this.getAttackReachSqr(pEnemy) * 1.1;
            return pDistToEnemySqr <= adjustedAttackDistance;
        }


        protected void resetAttackCooldown() {
            this.ticksUntilNextAttack = this.adjustedTickDelay(attackDelay * 2);
        }

        protected boolean isTimeToAttack() {
            return this.ticksUntilNextAttack <= 0;
        }

        protected boolean isTimeToStartAttackAnimation() {
            return this.ticksUntilNextAttack <= attackDelay;
        }

        protected int getTicksUntilNextAttack() {
            return this.ticksUntilNextAttack;
        }


        protected void performAttack(LivingEntity pEnemy) {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(pEnemy);
            if (this.mob instanceof DissidentEntity dissident) {
                this.mob.level().playSound(null, dissident.getX(), dissident.getY(), dissident.getZ(),
                        SoundEvents.HOGLIN_ATTACK, SoundSource.HOSTILE,
                        dissident.getAttackSoundVolume(), 1.0F);
            }
        }

        public void tick() {
            super.tick();
            LivingEntity target = this.mob.getTarget();
            if (target != null) {
                double distanceToTarget = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
                if (shouldCountTillNextAttack) {
                    this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
                }
                if (!isEnemyWithinAttackDistance(target, distanceToTarget)) {
                    this.mob.getNavigation().moveTo(target, 1.2);
                    resetAttackCooldown();
                    shouldCountTillNextAttack = false;
                    entity.setAttacking(false);
                    entity.attackAnimationTimeout = 0;
                }
            }
        }
        @Override
        public void stop() {
            entity.setAttacking(false);
            super.stop();
        }
    }

    public static class LeapAttackGoal extends Goal {
        private final DissidentEntity mob;
        private final double leapStrength;
        private final double maxLeapDistance;
        private final int leapCooldown;
        private int cooldownTicks;
        private LivingEntity target;
        private boolean isLeaping;
        private int leapTicks;

        public LeapAttackGoal(DissidentEntity mob, double leapStrength, double maxLeapDistance, int leapCooldown) {
            this.mob = mob;
            this.leapStrength = leapStrength;
            this.maxLeapDistance = maxLeapDistance;
            this.leapCooldown = leapCooldown;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (this.cooldownTicks > 0) {
                this.cooldownTicks--;
                return false;
            }

            this.target = this.mob.getTarget();
            if (this.target == null || !this.target.isAlive()) {
                return false;
            }

            double distanceToTarget = this.mob.distanceToSqr(this.target);
            return distanceToTarget >= 16.0 && distanceToTarget <= this.maxLeapDistance * this.maxLeapDistance
                    && this.mob.onGround() && this.mob.hasLineOfSight(this.target);
        }

        @Override
        public boolean canContinueToUse() {
            return this.isLeaping && this.leapTicks > 0 && this.target != null && this.target.isAlive();
        }

        @Override
        public void start() {
            this.isLeaping = true;
            this.leapTicks = 15;
            this.mob.setLeaping(true);

            double dx = this.target.getX() - this.mob.getX();
            double dy = this.target.getY() - this.mob.getY();
            double dz = this.target.getZ() - this.mob.getZ();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

            if (horizontalDistance > 0.1) {
                dx = dx / horizontalDistance;
                dz = dz / horizontalDistance;

                double distanceRatio = Math.min(horizontalDistance / this.maxLeapDistance, 1.0);
                double horizontalVelocity = this.leapStrength * 1.8 * (0.7 + distanceRatio * 0.3);
                double verticalVelocity = 0.4 + (distanceRatio * 0.3);

                if (dy > 0) {
                    verticalVelocity += Math.min(dy * 0.3, 0.5);
                }

                this.mob.setDeltaMovement(
                        dx * horizontalVelocity,
                        verticalVelocity,
                        dz * horizontalVelocity
                );
                this.mob.level().broadcastEntityEvent(this.mob, (byte) 5);
                this.mob.playSound(SoundEvents.PARROT_IMITATE_GHAST, 1.2F, 0.8F);
            }
        }

        @Override
        public void tick() {
            if (this.target == null || !this.isLeaping) {
                return;
            }
            this.leapTicks--;

            this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

            if (this.mob.distanceToSqr(this.target) <= 4.5) {
                this.performLeapAttack();
            }

            if (this.mob.onGround() && this.mob.getDeltaMovement().y <= 0.1) {
                this.leapTicks = Math.min(this.leapTicks, 3);

                if (this.mob.distanceToSqr(this.target) <= 9.0) {
                    this.performLeapAttack();
                }
            }
        }

        @Override
        public void stop() {
            this.isLeaping = false;
            this.mob.setLeaping(false);
            this.cooldownTicks = this.leapCooldown;
            this.target = null;
            this.leapTicks = 0;
        }

        private void performLeapAttack() {
            if (this.target != null && this.mob.distanceToSqr(this.target) <= 12.0) {
                float leapDamage = (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.3f;
                this.target.hurt(this.mob.damageSources().mobAttack(this.mob), leapDamage);

                this.target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                        60,
                        1
                ));

                double knockbackStrength = 0.8;
                double dx = this.target.getX() - this.mob.getX();
                double dz = this.target.getZ() - this.mob.getZ();
                double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

                if (horizontalDistance > 0.1) {
                    this.target.setDeltaMovement(
                            this.target.getDeltaMovement().add(
                                    (dx / horizontalDistance) * knockbackStrength,
                                    0.4,
                                    (dz / horizontalDistance) * knockbackStrength
                            )
                    );
                }

                this.mob.playSound(SoundEvents.RABBIT_HURT, 1.0F, 1.2F);
                this.mob.setAttacking(true);
            }

            this.leapTicks = 0;
        }
    }
}