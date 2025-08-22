package top.ribs.scguns.entity.monster;

import net.minecraft.core.Vec3i;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class DissidentEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(DissidentEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LEAPING =
            SynchedEntityData.defineId(DissidentEntity.class, EntityDataSerializers.BOOLEAN);

    public DissidentEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = XP_REWARD_LARGE;
    }

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;
    private int idleAnimationTimeout = 0;

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

        if (this.level().isClientSide()) {
            setupAnimationStates();
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
                for (int i = 0; i < 15; i++) {
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
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_HORSE_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_HORSE_DEATH;
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

    public class LeapAttackGoal extends Goal {
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
            return distanceToTarget >= 9.0 && distanceToTarget <= this.maxLeapDistance * this.maxLeapDistance
                    && this.mob.onGround() && this.mob.hasLineOfSight(this.target);
        }

        @Override
        public boolean canContinueToUse() {
            return this.isLeaping && this.leapTicks > 0 && this.target != null && this.target.isAlive();
        }

        @Override
        public void start() {
            this.isLeaping = true;
            this.leapTicks = 30;
            this.mob.setLeaping(true);

            // Calculate leap direction and perform the leap
            double dx = this.target.getX() - this.mob.getX();
            double dy = this.target.getY() - this.mob.getY();
            double dz = this.target.getZ() - this.mob.getZ();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

            if (horizontalDistance > 0.1) {
                dx = dx / horizontalDistance;
                dz = dz / horizontalDistance;

                double horizontalVelocity = this.leapStrength;
                double verticalVelocity = 0.6;

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

            if (this.mob.distanceToSqr(this.target) <= 3.0) {
                this.performLeapAttack();
            }
            if (this.mob.onGround() && this.mob.getDeltaMovement().y <= 0.1) {
                this.leapTicks = Math.min(this.leapTicks, 5);

                if (this.mob.distanceToSqr(this.target) <= 6.0) {
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
            if (this.target != null && this.mob.distanceToSqr(this.target) <= 6.0) {
                float leapDamage = (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.3f;
                this.target.hurt(this.mob.damageSources().mobAttack(this.mob), leapDamage);
                this.target.setDeltaMovement(this.target.getDeltaMovement().add(0, 0.2, 0));
                this.mob.playSound(SoundEvents.RABBIT_HURT, 1.0F, 1.2F);
                this.mob.setAttacking(true);
            }

            // End the leap
            this.leapTicks = 0;
        }
    }
}
