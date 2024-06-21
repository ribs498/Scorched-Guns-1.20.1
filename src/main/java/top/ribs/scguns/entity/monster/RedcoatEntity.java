package top.ribs.scguns.entity.monster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.entity.projectile.BrassBoltEntity;
import top.ribs.scguns.init.ModSounds;

import java.util.EnumSet;

public class RedcoatEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(RedcoatEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MUZZLE_FLASH_TIMER = SynchedEntityData.defineId(RedcoatEntity.class, EntityDataSerializers.INT);


    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    public int attackAnimationTimeout = 0;
    private int idleAnimationTimeout = 0;


    public RedcoatEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.FOLLOW_RANGE, 26D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.5f)
                .add(Attributes.ARMOR, 2f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8f)
                .add(Attributes.ATTACK_DAMAGE, 2f);
    }

    public void triggerMuzzleFlash() {
        this.entityData.set(MUZZLE_FLASH_TIMER, 10);
    }

    public boolean isMuzzleFlashVisible() {
        return this.entityData.get(MUZZLE_FLASH_TIMER) > 0;
    }


    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            setupAnimationStates();
            spawnSmokeParticles();
        }

        int currentTimer = this.entityData.get(MUZZLE_FLASH_TIMER);
        if (currentTimer > 0) {
            this.entityData.set(MUZZLE_FLASH_TIMER, currentTimer - 1);
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

            for (int i = 0; i < 3; i++) {
                double particleOffsetX = random.nextGaussian() * 0.1;
                double particleOffsetY = random.nextGaussian() * 0.1;
                double particleOffsetZ = random.nextGaussian() * 0.1;
                this.level().addParticle(ParticleTypes.SMOKE, posX, posY, posZ, particleOffsetX, particleOffsetY, particleOffsetZ);
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
                attackAnimationTimeout = 20;
                attackAnimationState.start(this.tickCount);
            }
            --attackAnimationTimeout;
        } else {
            attackAnimationState.stop();
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(MUZZLE_FLASH_TIMER, 0);
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
        this.goalSelector.addGoal(2, new RestrictSunGoal(this));
        this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new RangedMusketAttackGoal(this, 1.0, 40, 6.0F));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    public static class RangedMusketAttackGoal extends Goal {
        private final RedcoatEntity redcoat;
        private final double speedModifier;
        private final int attackInterval;
        private final float attackRadius;
        private int attackTime = -1;

        public RangedMusketAttackGoal(RedcoatEntity redcoat, double speedModifier, int attackInterval, float attackRadius) {
            this.redcoat = redcoat;
            this.speedModifier = speedModifier;
            this.attackInterval = attackInterval;
            this.attackRadius = attackRadius;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.redcoat.getTarget() != null && this.isHoldingUsableItem();
        }

        protected boolean isHoldingUsableItem() {
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return (this.canUse() || !this.redcoat.getNavigation().isDone()) && this.redcoat.isTargetInRange(this.attackRadius);
        }

        @Override
        public void stop() {
            this.redcoat.setAggressive(false);
            this.redcoat.setTarget(null);
            this.attackTime = -1;
        }

        @Override
        public void tick() {
            LivingEntity target = this.redcoat.getTarget();
            if (target != null) {
                double distance = this.redcoat.distanceToSqr(target.getX(), target.getY(), target.getZ());

                if (distance <= this.attackRadius * this.attackRadius) {
                    this.redcoat.getNavigation().stop();
                } else {
                    this.redcoat.getNavigation().moveTo(target, this.speedModifier);
                }

                this.redcoat.getLookControl().setLookAt(target, 30.0F, 30.0F);
                this.faceTarget(target);

                if (--this.attackTime <= 0) {
                    this.attackTime = this.attackInterval;
                    if (distance <= this.attackRadius * this.attackRadius) {
                        this.redcoat.performRangedAttack(target);
                    }
                }
            }
        }

        private void faceTarget(LivingEntity target) {
            double dx = target.getX() - this.redcoat.getX();
            double dz = target.getZ() - this.redcoat.getZ();
            float targetYaw = (float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
            this.redcoat.setYRot(targetYaw);
            this.redcoat.yHeadRot = this.redcoat.getYRot();
        }
    }

    private boolean isTargetInRange(float attackRadius) {
        LivingEntity target = this.getTarget();
        if (target == null) {
            return false;
        } else {
            return this.distanceToSqr(target) <= (double) (attackRadius * attackRadius);
        }
    }

    public void performRangedAttack(LivingEntity target) {
        BrassBoltEntity projectile = new BrassBoltEntity(this.level(), this);
        double offsetX = -0.0;
        double offsetY = this.getEyeHeight() - 0.5;
        double offsetZ = -0.5;
        double d0 = target.getX() - (this.getX() + offsetX);
        double d1 = target.getEyeY() - (this.getY() + offsetY);
        double d2 = target.getZ() - (this.getZ() + offsetZ);
        projectile.setPos(this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ);
        projectile.shoot(d0, d1, d2, 2.6F, 0.0F);
        this.level().addFreshEntity(projectile);
        this.playSound(ModSounds.BLACKPOWDER_FIRE.get(), 1.0F, 1.0F);
        this.triggerMuzzleFlash();
    }

}
