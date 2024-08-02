package top.ribs.scguns.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CogKnightEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(CogKnightEntity.class, EntityDataSerializers.BOOLEAN);
    public CogKnightEntity(EntityType<? extends CogKnightEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 34D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.5f)
                .add(Attributes.ARMOR, 2f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8f)
                .add(Attributes.ATTACK_DAMAGE, 6f);
    }
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }
    private void setupAnimationStates() {
        if (this.isAttacking()) {
            if (attackAnimationTimeout == 0) {
                attackAnimationTimeout = 20;
                attackAnimationState.start(this.tickCount);
            }
        }

        if (attackAnimationTimeout > 0) {
            attackAnimationTimeout--;
            if (attackAnimationTimeout == 0) {
                attackAnimationState.stop();
                this.setAttacking(false);
            }
        } else {
            if (this.isAttacking()) {
                this.setAttacking(false);
            }
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
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new CogKnightAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(CogKnightEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.0, 30));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 3f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }
    public boolean canBreatheUnderwater() {
        return true;
    }
    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }
    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.onGround() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
    }
    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
        double velocityThreshold = -0.5;
        if (y >= velocityThreshold) {
            this.fallDistance = 0;
        } else {
            super.checkFallDamage(y, onGroundIn, state, pos);
        }
    }
    ////// ATTACK //////
    public static class CogKnightAttackGoal extends MeleeAttackGoal {
        private final CogKnightEntity entity;
        private final int attackDelay = 12;
        private int ticksUntilNextAttack = 0;
        private boolean damageDealtDuringAnimation = false;
        private int attackCooldown = 20;
        public CogKnightAttackGoal(PathfinderMob pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
            super(pMob, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
            this.entity = (CogKnightEntity) pMob;
        }
        @Override
        public void start() {
            super.start();
            this.ticksUntilNextAttack = attackDelay;
            this.damageDealtDuringAnimation = false;
        }
        private double calculateAttackRangeSqr() {
            final double baseReach = 2.0;
            return baseReach * baseReach;
        }

        @Override
        protected void checkAndPerformAttack(@NotNull LivingEntity pEnemy, double pDistToEnemySqr) {
            double attackRangeSqr = calculateAttackRangeSqr();

            if (pDistToEnemySqr <= attackRangeSqr && this.mob.hasLineOfSight(pEnemy)) {
                this.entity.setAttacking(true);
                this.mob.getLookControl().setLookAt(pEnemy.getX(), pEnemy.getEyeY(), pEnemy.getZ());
                if (this.ticksUntilNextAttack <= 0 && !damageDealtDuringAnimation) {
                    performAttack(pEnemy);
                    resetAttackCooldown();
                }
            } else {
                this.entity.setAttacking(false);
                resetAttackCooldown();
            }
        }
        @Override
        public void tick() {
            super.tick();
            ticksUntilNextAttack = Math.max(ticksUntilNextAttack - 1, 0);
            attackCooldown = Math.max(attackCooldown - 1, 0);
        }
        @Override
        public void stop() {
            super.stop();
            this.entity.setAttacking(false);
            this.damageDealtDuringAnimation = false;
        }
        private void performAttack(LivingEntity pEnemy) {
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(pEnemy);
            this.entity.setAttacking(true);
        }
        protected void resetAttackCooldown() {
            this.ticksUntilNextAttack = this.adjustedTickDelay(attackDelay);
            this.damageDealtDuringAnimation = false;
        }
    }
}