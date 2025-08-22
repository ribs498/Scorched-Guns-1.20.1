package top.ribs.scguns.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.init.ModEntities;

import java.util.EnumSet;

public class CogKnightEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(CogKnightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TIMEOUT =
            SynchedEntityData.defineId(CogKnightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CHARGING =
            SynchedEntityData.defineId(CogKnightEntity.class, EntityDataSerializers.BOOLEAN);

    public CogKnightEntity(EntityType<? extends CogKnightEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
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
    public final AnimationState idleAnimationState = new AnimationState();

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 34D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.5f)
                .add(Attributes.ARMOR, 3f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8f)
                .add(Attributes.ATTACK_DAMAGE, 6f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.isAttacking() && this.getAttackTimeout() > 0) {
                this.setAttackTimeout(this.getAttackTimeout() - 1);

                // Deal damage at the right moment in the animation (when arm is swinging down)
                if (this.getAttackTimeout() == 6) { // Damage happens mid-swing
                    LivingEntity target = this.getTarget();
                    if (target != null && this.distanceToSqr(target) <= this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F + target.getBbWidth()) {
                        this.doHurtTarget(target);
                    }
                }

                if (this.getAttackTimeout() <= 0) {
                    this.setAttacking(false);
                }
            }
        }

        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            if (source.getEntity() instanceof Player) {
                float rand = this.random.nextFloat();
                if (rand < 0.15f) {
                    SignalBeaconEntity beacon = new SignalBeaconEntity(ModEntities.SIGNAL_BEACON.get(), this.level());
                    beacon.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                    this.level().addFreshEntity(beacon);
                }
            }
        }
    }

    private void setupAnimationStates() {
        if (!this.idleAnimationState.isStarted()) {
            this.idleAnimationState.start(this.tickCount);
        }
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
        if (attacking) {
            this.setAttackTimeout(12);
        }
    }

    public void setAttackTimeout(int timeout) {
        this.entityData.set(ATTACK_TIMEOUT, timeout);
    }

    public int getAttackTimeout() {
        return this.entityData.get(ATTACK_TIMEOUT);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(ATTACK_TIMEOUT, 0);
        this.entityData.define(CHARGING, false);
    }
    public void setCharging(boolean charging) {
        this.entityData.set(CHARGING, charging);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
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
        this.goalSelector.addGoal(1, new ChargeAttackGoal(this, 0.8, 12.0, 80));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false) {

            @Override
            protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
                if (pDistToEnemySqr <= this.getAttackReachSqr(pEnemy) && this.getTicksUntilNextAttack() <= 0 && !CogKnightEntity.this.isAttacking()) {
                    CogKnightEntity.this.setAttacking(true);
                    this.resetAttackCooldown();
                    this.mob.swing(InteractionHand.MAIN_HAND);
                }
            }

            @Override
            protected double getAttackReachSqr(LivingEntity pEnemy) {
                return super.getAttackReachSqr(pEnemy) * 1.5;
            }

            @Override
            protected void resetAttackCooldown() {
                this.adjustedTickDelay(25);
            }
        });

        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.0, 30));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 3f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(CogKnightEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
    }
    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 4) {
            if (this.level().isClientSide) {
                for (int i = 0; i < 8; i++) {
                    this.level().addParticle(
                            ParticleTypes.CLOUD,
                            this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            this.getY() + this.random.nextDouble() * this.getBbHeight(),
                            this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            0.0, 0.0, 0.0
                    );
                }
            }
        } else {
            super.handleEntityEvent(pId);
        }
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

    public class ChargeAttackGoal extends Goal {
        private final CogKnightEntity mob;
        private final double speedModifier;
        private final double chargeRange;
        private final int chargeCooldown;
        private int cooldownTicks;
        private int chargeTicks;
        private LivingEntity target;
        private boolean isCharging;

        public ChargeAttackGoal(CogKnightEntity mob, double speedModifier, double chargeRange, int chargeCooldown) {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.chargeRange = chargeRange;
            this.chargeCooldown = chargeCooldown;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.cooldownTicks > 0) {
                this.cooldownTicks--;
                return false;
            }

            this.target = this.mob.getTarget();
            if (this.target == null) {
                return false;
            }

            double distanceToTarget = this.mob.distanceToSqr(this.target);
            return distanceToTarget <= this.chargeRange * this.chargeRange && distanceToTarget > 4.0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.target.isAlive() && this.chargeTicks > 0;
        }

        @Override
        public void start() {
            this.isCharging = true;
            this.chargeTicks = 20;
            this.mob.setCharging(true);
            this.mob.level().broadcastEntityEvent(this.mob, (byte) 4);
        }

        @Override
        public void tick() {
            if (this.target == null) {
                return;
            }

            this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

            if (this.chargeTicks > 0) {
                this.chargeTicks--;

                // Calculate charge direction
                double dx = this.target.getX() - this.mob.getX();
                double dy = this.target.getY() - this.mob.getY();
                double dz = this.target.getZ() - this.mob.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (distance > 0.1) {
                    // Normalize and apply speed
                    dx = (dx / distance) * this.speedModifier;
                    dz = (dz / distance) * this.speedModifier;

                    // Set movement
                    this.mob.setDeltaMovement(dx, this.mob.getDeltaMovement().y, dz);

                    // Check if we hit the target during charge
                    if (this.mob.distanceToSqr(this.target) <= 2.0) {
                        this.performChargeAttack();
                    }
                }
            }
        }

        @Override
        public void stop() {
            this.isCharging = false;
            this.mob.setCharging(false);
            this.cooldownTicks = this.chargeCooldown;
            this.target = null;
            this.chargeTicks = 0;
        }

        private void performChargeAttack() {
            if (this.target != null) {
                // Deal extra damage from the charge
                float chargeDamage = (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f;
                this.target.hurt(this.mob.damageSources().mobAttack(this.mob), chargeDamage);

                // Knockback effect
                double knockbackStrength = 1.0;
                double dx = this.target.getX() - this.mob.getX();
                double dz = this.target.getZ() - this.mob.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);

                if (distance > 0) {
                    this.target.setDeltaMovement(
                            this.target.getDeltaMovement().add(
                                    (dx / distance) * knockbackStrength,
                                    0.2,
                                    (dz / distance) * knockbackStrength
                            )
                    );
                }
                this.mob.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
            }

            this.chargeTicks = 0;
        }
    }
}