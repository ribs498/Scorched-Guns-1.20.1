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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.init.ModSounds;

import java.util.Objects;
import java.util.UUID;

public class PraetorEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(PraetorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TIMEOUT =
            SynchedEntityData.defineId(PraetorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_VARIATION =
            SynchedEntityData.defineId(PraetorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_IN_SECOND_PHASE =
            SynchedEntityData.defineId(PraetorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ROARING =
            SynchedEntityData.defineId(PraetorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ROAR_TICK =
            SynchedEntityData.defineId(PraetorEntity.class, EntityDataSerializers.INT);

    public static final float SECOND_PHASE_HEALTH_THRESHOLD = 30.0F;
    private static final float REGENERATION_TARGET = 60.0F;
    private static final float REGENERATION_RATE = 0.5F;
    private static final int REGENERATION_DURATION = 100;
    private static final int ROAR_DURATION = 40;

    private boolean hasTriggeredSecondPhase = false;
    private boolean isRegenerating = false;
    private int regenerationTicks = 0;

    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("d4c7f9a2-8b3e-4a1c-9d6e-2f8c4b1a5e3d");
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("e5d8f0b3-9c4f-5b2d-0e7f-3a9d5c2b6f4e");

    public PraetorEntity(EntityType<? extends PraetorEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 90D)
                .add(Attributes.FOLLOW_RANGE, 32D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0f)
                .add(Attributes.ARMOR, 6f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7f)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2f)
                .add(Attributes.ATTACK_DAMAGE, 10f);
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(ATTACK_TIMEOUT, 0);
        this.entityData.define(ATTACK_VARIATION, 0);
        this.entityData.define(IS_IN_SECOND_PHASE, false);
        this.entityData.define(IS_ROARING, false);
        this.entityData.define(ROAR_TICK, 0);
    }

    public boolean isInSecondPhase() {
        return this.entityData.get(IS_IN_SECOND_PHASE);
    }

    public void setInSecondPhase(boolean inSecondPhase) {
        this.entityData.set(IS_IN_SECOND_PHASE, inSecondPhase);
    }

    public boolean isRoaring() {
        return this.entityData.get(IS_ROARING);
    }

    public void setRoaring(boolean roaring) {
        this.entityData.set(IS_ROARING, roaring);
    }

    public int getRoarTick() {
        return this.entityData.get(ROAR_TICK);
    }

    public void setRoarTick(int tick) {
        this.entityData.set(ROAR_TICK, tick);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            handleSecondPhase();
            handleRegeneration();
            handleRoaring();

            if (this.isAttacking() && this.getAttackTimeout() > 0) {
                this.setAttackTimeout(this.getAttackTimeout() - 1);

                if (this.getAttackTimeout() == 6) {
                    LivingEntity target = this.getTarget();
                    if (target != null && this.distanceToSqr(target) <= this.getBbWidth() * 2.5F * this.getBbWidth() * 2.5F + target.getBbWidth()) {
                        this.doHurtTarget(target);
                    }
                }

                if (this.getAttackTimeout() <= 0) {
                    this.setAttacking(false);
                }
            }
        } else {
            if (this.isRoaring()) {
                spawnRoarParticles();
            }
        }
    }

    private void handleSecondPhase() {
        if (!hasTriggeredSecondPhase && this.getHealth() <= SECOND_PHASE_HEALTH_THRESHOLD) {
            hasTriggeredSecondPhase = true;
            isRegenerating = true;
            regenerationTicks = 0;
            setInSecondPhase(true);
            setRoaring(true);
            setRoarTick(0);

            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, REGENERATION_DURATION, 2, false, false));

            if (Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).getModifier(SPEED_MODIFIER_UUID) == null) {
                Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).addPermanentModifier(
                        new AttributeModifier(SPEED_MODIFIER_UUID, "Second phase speed boost", 0.08, AttributeModifier.Operation.ADDITION)
                );
            }
            if (Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).getModifier(DAMAGE_MODIFIER_UUID) == null) {
                Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).addPermanentModifier(
                        new AttributeModifier(DAMAGE_MODIFIER_UUID, "Second phase damage boost", 3.0, AttributeModifier.Operation.ADDITION)
                );
            }

            this.playSound(ModSounds.PRAETOR_ROAR.get(), 1.5F, 0.8F);
        }
    }

    private void handleRegeneration() {
        if (isRegenerating) {
            regenerationTicks++;

            if (regenerationTicks <= REGENERATION_DURATION && this.getHealth() < REGENERATION_TARGET) {
                this.heal(REGENERATION_RATE);
            } else if (regenerationTicks > REGENERATION_DURATION || this.getHealth() >= REGENERATION_TARGET) {
                isRegenerating = false;
                this.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            }
        }
    }

    private void handleRoaring() {
        if (this.isRoaring()) {
            int currentTick = this.getRoarTick();
            if (currentTick >= ROAR_DURATION) {
                this.setRoaring(false);
                this.setRoarTick(0);
            } else {
                this.setRoarTick(currentTick + 1);
            }
        }
    }

    private void spawnRoarParticles() {
        if (this.random.nextFloat() < 0.3f) {
            double offsetX = (this.random.nextDouble() - 0.5) * this.getBbWidth() * 1.5;
            double offsetY = this.random.nextDouble() * this.getBbHeight();
            double offsetZ = (this.random.nextDouble() - 0.5) * this.getBbWidth() * 1.5;

            this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0.0, 0.05, 0.0);

            if (this.random.nextFloat() < 0.5f) {
                this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0.0, 0.02, 0.0);
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
                if (pDistToEnemySqr <= this.getAttackReachSqr(pEnemy) && this.getTicksUntilNextAttack() <= 0 && !PraetorEntity.this.isAttacking()) {
                    PraetorEntity.this.setAttacking(true);
                    this.resetAttackCooldown();
                    this.mob.swing(InteractionHand.MAIN_HAND);
                }
            }

            @Override
            protected double getAttackReachSqr(LivingEntity pEnemy) {
                return super.getAttackReachSqr(pEnemy) * 2.0;
            }

            @Override
            protected void resetAttackCooldown() {
                this.adjustedTickDelay(30);
            }
        });

        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.0, 35));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(PraetorEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
        if (attacking) {
            this.setAttackTimeout(12);
            this.entityData.set(ATTACK_VARIATION, this.random.nextInt(3));
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

    public int getAttackVariation() {
        return this.entityData.get(ATTACK_VARIATION);
    }

    @Override
    public void readAdditionalSaveData(@NotNull net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.hasTriggeredSecondPhase = compound.getBoolean("HasTriggeredSecondPhase");
        this.isRegenerating = compound.getBoolean("IsRegenerating");
        this.regenerationTicks = compound.getInt("RegenerationTicks");

        if (compound.getBoolean("IsInSecondPhase")) {
            this.setInSecondPhase(true);
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("HasTriggeredSecondPhase", this.hasTriggeredSecondPhase);
        compound.putBoolean("IsRegenerating", this.isRegenerating);
        compound.putInt("RegenerationTicks", this.regenerationTicks);
        compound.putBoolean("IsInSecondPhase", this.isInSecondPhase());
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.PRAETOR_IDLE.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return ModSounds.PRAETOR_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.PRAETOR_DIE.get();
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.8F, 1.0F);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.onGround() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.7, 1.0));
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
        double velocityThreshold = -0.6;
        if (y >= velocityThreshold) {
            this.fallDistance = 0;
        } else {
            super.checkFallDamage(y, onGroundIn, state, pos);
        }
    }
}