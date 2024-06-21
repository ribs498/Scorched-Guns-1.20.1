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
import org.jetbrains.annotations.Nullable;

public class DissidentEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(DissidentEntity.class, EntityDataSerializers.BOOLEAN);

    public DissidentEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setCanPickUpLoot(true);
    }

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;
    private int idleAnimationTimeout = 0;

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 60D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.5f)
                .add(Attributes.ARMOR, 2f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8f)
                .add(Attributes.ATTACK_DAMAGE, 2f);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }
    @Override
    public boolean wantsToPickUp(ItemStack pStack) {
        return this.canHoldItem(pStack);
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
                attackAnimationTimeout = 20;
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
    @Override
    protected Vec3i getPickupReach() {
        return new Vec3i(3, 3, 3);
    }

    @Override
    protected void pickUpItem(ItemEntity pItemEntity) {
        super.pickUpItem(pItemEntity);
    }
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new CogMinionAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(DissidentEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.0, 30));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0));
    }
    @Override
    public boolean canHoldItem(ItemStack stack) {
        return true;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    public float getAttackSoundVolume() {
        float attackSoundVolume = 1.0F;
        return attackSoundVolume;
    }


    public static class CogMinionAttackGoal extends MeleeAttackGoal {
        private final DissidentEntity entity;
        private int attackDelay = 10;
        private int ticksUntilNextAttack = 10;
        private boolean shouldCountTillNextAttack = false;

        public CogMinionAttackGoal(PathfinderMob pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
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
            double adjustedAttackDistance = this.getAttackReachSqr(pEnemy) * 1.1; // Increase attack distance by 10%
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
            if (this.mob instanceof DissidentEntity) {
                DissidentEntity cogMinion = (DissidentEntity) this.mob;
                this.mob.level().playSound(null, cogMinion.getX(), cogMinion.getY(), cogMinion.getZ(),
                        SoundEvents.PISTON_EXTEND, SoundSource.HOSTILE,
                        cogMinion.getAttackSoundVolume(), 1.0F);
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



}
