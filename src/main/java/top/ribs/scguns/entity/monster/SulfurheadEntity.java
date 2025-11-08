package top.ribs.scguns.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.entity.projectile.SulfurGasCloudEntity;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModSounds;

public class SulfurheadEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(SulfurheadEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TIMEOUT =
            SynchedEntityData.defineId(SulfurheadEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PRIMED =
            SynchedEntityData.defineId(SulfurheadEntity.class, EntityDataSerializers.BOOLEAN);

    private static final float LOW_HEALTH_THRESHOLD = 0.30f;
    private static final float GAS_CLOUD_RADIUS = 6.0f;
    private static final int GAS_CLOUD_DURATION = 200;
    private static final int MAX_SWELL = 43;

    private int oldSwell;
    private int swell;
    private boolean hasTriggeredGasCloud = false;

    public SulfurheadEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(ATTACK_TIMEOUT, 0);
        this.entityData.define(PRIMED, false);
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEFINED;
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
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.isAttacking() && this.getAttackTimeout() > 0) {
                this.setAttackTimeout(this.getAttackTimeout() - 1);
                if (this.getAttackTimeout() <= 0) {
                    this.setAttacking(false);
                }
            }

            if (this.isAlive() && isLowHealth() && !hasTriggeredGasCloud) {
                if (!isPrimed()) {
                    setPrimed(true);
                    this.level().broadcastEntityEvent(this, (byte) 5);
                    this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.8F);
                }
            }

            if (isPrimed() && this.swell >= MAX_SWELL) {
                this.swell = MAX_SWELL;
                spawnGasCloudAndDie();
            }
        }

        if (this.isAlive()) {
            this.oldSwell = this.swell;
            if (isPrimed()) {
                this.swell++;
            } else if (this.swell > 0) {
                this.swell--;
            }

            if (this.swell < 0) {
                this.swell = 0;
            }
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

    public void setPrimed(boolean primed) {
        this.entityData.set(PRIMED, primed);
    }

    public boolean isPrimed() {
        return this.entityData.get(PRIMED);
    }

    public boolean isLowHealth() {
        return this.getHealth() / this.getMaxHealth() <= LOW_HEALTH_THRESHOLD;
    }

    public float getSwelling(float partialTicks) {
        return Mth.lerp(partialTicks, (float)this.oldSwell, (float)this.swell) / (float)(MAX_SWELL - 2);
    }

    private void spawnGasCloudAndDie() {
        if (!this.level().isClientSide && !hasTriggeredGasCloud) {
            hasTriggeredGasCloud = true;

            Vec3 center = this.position().add(0, this.getBbHeight() * 0.5, 0);

            SulfurGasCloudEntity gasCloud = new SulfurGasCloudEntity(
                    ModEntities.SULFUR_GAS_CLOUD.get(),
                    this.level(),
                    center,
                    GAS_CLOUD_RADIUS,
                    GAS_CLOUD_DURATION,
                    300,
                    2
            );

            this.level().addFreshEntity(gasCloud);

            spawnDeathParticleBurst(center);

            this.playSound(SoundEvents.CAT_HISS, 1.5F, 0.8F);

            this.discard();
        }
    }

    private void spawnDeathParticleBurst(Vec3 center) {
        net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) this.level();

        int smokeParticles = 40;
        int dustParticles = 30;

        for (int i = 0; i < smokeParticles; i++) {
            double angle = this.random.nextDouble() * 2 * Math.PI;
            double verticalAngle = (this.random.nextDouble() - 0.5) * Math.PI;
            double speed = 0.15 + this.random.nextDouble() * 0.25;

            double xSpeed = Math.cos(angle) * Math.cos(verticalAngle) * speed;
            double ySpeed = Math.sin(verticalAngle) * speed + 0.1;
            double zSpeed = Math.sin(angle) * Math.cos(verticalAngle) * speed;

            serverLevel.sendParticles(ModParticleTypes.SULFUR_SMOKE.get(),
                    center.x, center.y, center.z,
                    3,
                    xSpeed, ySpeed, zSpeed,
                    0.2);
        }

        for (int i = 0; i < dustParticles; i++) {
            double angle = this.random.nextDouble() * 2 * Math.PI;
            double radius = this.random.nextDouble() * 1.5;
            double speed = 0.1 + this.random.nextDouble() * 0.15;

            double xSpeed = Math.cos(angle) * speed;
            double ySpeed = 0.05 + this.random.nextDouble() * 0.1;
            double zSpeed = Math.sin(angle) * speed;

            serverLevel.sendParticles(ModParticleTypes.SULFUR_DUST.get(),
                    center.x + Math.cos(angle) * radius,
                    center.y,
                    center.z + Math.sin(angle) * radius,
                    2,
                    xSpeed, ySpeed, zSpeed,
                    0.15);
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (!this.level().isClientSide && pSource.getEntity() instanceof Player) {
            return super.hurt(pSource, pAmount);
        } else if (!this.level().isClientSide && !(pSource.getEntity() instanceof Player)) {
            return false;
        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1, false) {
            @Override
            protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
                if (pDistToEnemySqr <= this.getAttackReachSqr(pEnemy) && this.getTicksUntilNextAttack() <= 0 && !SulfurheadEntity.this.isAttacking()) {
                    SulfurheadEntity.this.setAttacking(true);
                    this.resetAttackCooldown();
                    this.mob.swing(InteractionHand.MAIN_HAND);
                    this.mob.doHurtTarget(pEnemy);
                }
            }
        });
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !((Player) player).isSpectator()));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 30D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.1f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5f)
                .add(Attributes.ATTACK_DAMAGE, 3f);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SULFURHEAD_IDLE.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return ModSounds.SULFURHEAD_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SULFURHEAD_DIE.get();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("Swell", (short)this.swell);
        tag.putBoolean("HasTriggeredGasCloud", this.hasTriggeredGasCloud);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.swell = tag.getShort("Swell");
        this.oldSwell = this.swell;
        this.hasTriggeredGasCloud = tag.getBoolean("HasTriggeredGasCloud");
    }
}