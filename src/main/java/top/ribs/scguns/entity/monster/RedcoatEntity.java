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
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.entity.projectile.BrassBoltEntity;
import top.ribs.scguns.init.ModSounds;

import java.util.EnumSet;

public class RedcoatEntity extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(RedcoatEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MUZZLE_FLASH_TIMER = SynchedEntityData.defineId(RedcoatEntity.class, EntityDataSerializers.INT);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    public int attackAnimationTimeout = 0;
    private int idleAnimationTimeout = 0;

    private int attackTime = -1;

    public RedcoatEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.FOLLOW_RANGE, 26D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.5f)
                .add(Attributes.ARMOR, 2f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8f)
                .add(Attributes.ATTACK_DAMAGE, 2f);
    }
    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEAD;
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

        if (this.isAttacking()) {
            if (--this.attackTime <= 0) {
                this.attackTime = 120; // Cooldown between shots
                LivingEntity target = this.getTarget();
                if (target != null) {
                    this.performRangedAttack(target, 1.0F);
                }
            }
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

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new RestrictSunGoal(this));
        this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new RangedAttackGoal(this, 1.0, 40, 15.0F) {
            @Override
            public void stop() {
                super.stop();
                RedcoatEntity.this.setAttacking(false);
            }

            @Override
            public void start() {
                super.start();
                RedcoatEntity.this.setAttacking(true);
            }
        });
        this.goalSelector.addGoal(8, new MeleeAttackGoal(this, 1.2, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
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

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float distanceFactor) {
        BrassBoltEntity projectile = new BrassBoltEntity(this.level(), this);
        double offsetX = 0.0;
        double offsetY = this.getEyeHeight() - 0.5;
        double offsetZ = -0.5;
        double d0 = target.getX() - (this.getX() + offsetX);
        double d1 = target.getEyeY() - (this.getY() + offsetY);
        double d2 = target.getZ() - (this.getZ() + offsetZ);
        projectile.setPos(this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ);
        projectile.shoot(d0, d1, d2, 2.5F, 0.0F);
        this.level().addFreshEntity(projectile);
        this.playSound(ModSounds.BLACKPOWDER_FIRE.get(), 1.0F, 1.0F);
        this.triggerMuzzleFlash();
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isAlive() && this.isSunSensitive() && this.isSunBurnTick()) {
            ItemStack itemstack = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!itemstack.isEmpty()) {
                if (itemstack.isDamageableItem()) {
                    itemstack.setDamageValue(itemstack.getDamageValue() + this.random.nextInt(2));
                    if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) {
                        this.broadcastBreakEvent(EquipmentSlot.HEAD);
                        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    }
                }
            } else {
                this.setSecondsOnFire(8);
            }
        }
    }

    private boolean isSunSensitive() {
        return true;
    }
}

