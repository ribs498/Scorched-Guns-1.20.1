package top.ribs.scguns.entity.monster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.entity.projectile.BrassBoltEntity;
import top.ribs.scguns.init.ModSounds;

import java.util.UUID;

public class BlundererEntity extends Raider implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(BlundererEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MUZZLE_FLASH_TIMER = SynchedEntityData.defineId(BlundererEntity.class, EntityDataSerializers.INT);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    public int attackAnimationTimeout = 0;
    private int idleAnimationTimeout = 0;

    private int attackTime = -1;
    @Override
    public @NotNull MobType getMobType() {
        return MobType.ILLAGER;
    }
    public BlundererEntity(EntityType<? extends Raider> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setUUID(UUID.randomUUID()); // Ensure unique UUID upon creation
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30D)
                .add(Attributes.FOLLOW_RANGE, 21D)
                .add(Attributes.MOVEMENT_SPEED, 0.3499999940395355D)
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

        if (this.isAttacking()) {
            if (--this.attackTime <= 0) {
                this.attackTime = 50;
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
    public void applyRaidBuffs(int pWave, boolean pUnusedFalse) {
        // Apply raid buffs if necessary
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
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RestrictSunGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new RangedAttackGoal(this, 1.0, 50, 15.0F) {
            @Override
            public void stop() {
                super.stop();
                BlundererEntity.this.setAttacking(false);
            }

            @Override
            public void start() {
                super.start();
                BlundererEntity.this.setAttacking(true);
            }
        });
        this.goalSelector.addGoal(8, new MeleeAttackGoal(this, 1.2, false));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, Raider.class)).setAlertOthers(Raider.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float distanceFactor) {
        int numberOfPellets = 5;
        float maxSpreadAngle = 11.0F;
        double offsetX = 0.0;
        double offsetY = this.getEyeHeight() - 0.5;
        double offsetZ = -0.5;

        double targetX = target.getX() - (this.getX() + offsetX);
        double targetY = target.getEyeY() - (this.getY() + offsetY);
        double targetZ = target.getZ() - (this.getZ() + offsetZ);

        double distance = Math.sqrt(targetX * targetX + targetY * targetY + targetZ * targetZ);
        double baseX = targetX / distance;
        double baseY = targetY / distance;
        double baseZ = targetZ / distance;

        for (int i = 0; i < numberOfPellets; i++) {
            BrassBoltEntity projectile = new BrassBoltEntity(this.level(), this);

            float spreadRadius = (float) (Math.random() * Math.toRadians(maxSpreadAngle));
            float spreadAngle = (float) (Math.random() * 2 * Math.PI);

            double perpX1, perpY1, perpZ1;
            double perpX2, perpY2, perpZ2;

            if (Math.abs(baseY) < 0.9) {
                perpX1 = 0;
                perpY1 = baseZ;
                perpZ1 = -baseY;
            } else {
                perpX1 = baseZ;
                perpY1 = 0;
                perpZ1 = -baseX;
            }

            double perpLength1 = Math.sqrt(perpX1 * perpX1 + perpY1 * perpY1 + perpZ1 * perpZ1);
            perpX1 /= perpLength1;
            perpY1 /= perpLength1;
            perpZ1 /= perpLength1;

            perpX2 = baseY * perpZ1 - baseZ * perpY1;
            perpY2 = baseZ * perpX1 - baseX * perpZ1;
            perpZ2 = baseX * perpY1 - baseY * perpX1;

            double spreadX = Math.cos(spreadAngle) * Math.sin(spreadRadius);
            double spreadZ = Math.sin(spreadAngle) * Math.sin(spreadRadius);

            double finalX = baseX * Math.cos(spreadRadius) +
                    (perpX1 * spreadX + perpX2 * spreadZ);
            double finalY = baseY * Math.cos(spreadRadius) +
                    (perpY1 * spreadX + perpY2 * spreadZ);
            double finalZ = baseZ * Math.cos(spreadRadius) +
                    (perpZ1 * spreadX + perpZ2 * spreadZ);

            projectile.setPos(this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ);
            projectile.shoot(finalX, finalY, finalZ, 2.6F, 0.0F); // Set inaccuracy to 0 since we're handling spread manually
            this.level().addFreshEntity(projectile);
        }

        this.playSound(ModSounds.BLACKPOWDER_FIRE.get(), 1.0F, 1.0F);
        this.triggerMuzzleFlash();
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, @NotNull DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.setCanJoinRaid(this.getType() != EntityType.WITCH || pReason != MobSpawnType.NATURAL);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }
    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return this.getCurrentRaid() == null && super.removeWhenFarAway(pDistanceToClosestPlayer);
    }
    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.getCurrentRaid() != null;
    }
    @Override
    public void setUUID(UUID uuid) {
        super.setUUID(uuid);
    }
}

