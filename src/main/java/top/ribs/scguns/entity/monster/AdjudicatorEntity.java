package top.ribs.scguns.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.config.EntityEquipmentConfig;
import top.ribs.scguns.entity.ai.AIType;
import top.ribs.scguns.entity.ai.GunAttackGoal;
import top.ribs.scguns.item.GunItem;

public class AdjudicatorEntity extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(AdjudicatorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TIMEOUT =
            SynchedEntityData.defineId(AdjudicatorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DODGING =
            SynchedEntityData.defineId(AdjudicatorEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double ALLIANCE_RANGE = 32.0;
    private static final int ALERT_RANGE_Y = 10;
    private int ticksUntilNextAlert = 0;
    private int dodgeCooldown = 0;
    private int lastDodgeTime = 0;

    public AdjudicatorEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40D)
                .add(Attributes.FOLLOW_RANGE, 32D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ARMOR_TOUGHNESS, 1.0f)
                .add(Attributes.ARMOR, 4f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5f)
                .add(Attributes.ATTACK_DAMAGE, 4f);
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty,
                                        MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData,
                                        @Nullable CompoundTag pDataTag) {
        EntityEquipmentConfig.equipEntity(this, "scguns:adjudicator");
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(ATTACK_TIMEOUT, 0);
        this.entityData.define(DODGING, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (this.dodgeCooldown > 0) {
                this.dodgeCooldown--;
            }

            if (this.isAttacking() && this.getAttackTimeout() > 0) {
                this.setAttackTimeout(this.getAttackTimeout() - 1);
                if (this.getAttackTimeout() == 6) {
                    LivingEntity target = this.getTarget();
                    if (target != null && this.distanceToSqr(target) <= this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F + target.getBbWidth()) {
                        this.doHurtTarget(target);
                    }
                }
                if (this.getAttackTimeout() <= 0) {
                    this.setAttacking(false);
                }
            }

            if (this.getTarget() != null) {
                this.maybeAlertAllies();

                if (this.getHealth() < this.getMaxHealth() * 0.35f) {
                    this.attemptEvasiveDodge();
                }
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isDodging() && !this.onGround() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.85, 1.0));
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        boolean wasHurt = super.hurt(pSource, pAmount);

        if (wasHurt && !this.level().isClientSide && !this.isDeadOrDying() && this.getHealth() < this.getMaxHealth() * 0.35f) {
            if (this.dodgeCooldown <= 0 && this.random.nextFloat() < 0.6f) {
                this.performDodgeLeap(pSource);
            }
        }

        return wasHurt;
    }

    private void attemptEvasiveDodge() {
        if (this.dodgeCooldown > 0 || !this.onGround() || this.isDeadOrDying()) return;

        LivingEntity target = this.getTarget();
        if (target == null) return;

        double distance = this.distanceToSqr(target);

        float dodgeChance = distance < 64.0 ? 0.08f : 0.03f;

        if (this.random.nextFloat() < dodgeChance) {
            this.performDodgeLeap(null);
        }
    }

    private void performDodgeLeap(DamageSource damageSource) {
        if (this.isDeadOrDying()) return;

        this.setDodging(true);

        Vec3 dodgeDirection;
        if (damageSource != null && damageSource.getEntity() != null) {
            Vec3 awayFromSource = this.position().subtract(damageSource.getEntity().position()).normalize();
            double angle = (this.random.nextDouble() - 0.5) * Math.PI * 0.6;
            dodgeDirection = awayFromSource.yRot((float)angle);
        } else if (this.getTarget() != null) {
            Vec3 toTarget = this.getTarget().position().subtract(this.position()).normalize();
            double angle = this.random.nextBoolean() ? Math.PI * 0.5 : -Math.PI * 0.5;
            dodgeDirection = toTarget.yRot((float)angle);
        } else {
            double randomAngle = this.random.nextDouble() * Math.PI * 2.0;
            dodgeDirection = new Vec3(Math.cos(randomAngle), 0, Math.sin(randomAngle));
        }

        double leapPower = 0.9 + this.random.nextDouble() * 0.4;
        double verticalBoost = 0.5 + this.random.nextDouble() * 0.3;

        this.setDeltaMovement(
                dodgeDirection.x * leapPower,
                verticalBoost,
                dodgeDirection.z * leapPower
        );
        this.level().broadcastEntityEvent(this, (byte) 8);
        this.playSound(SoundEvents.RAVAGER_ROAR, 0.8F, 1.2F + this.random.nextFloat() * 0.3F);
        this.dodgeCooldown = 25 + this.random.nextInt(35);
        this.lastDodgeTime = this.tickCount;

        this.level().getServer().execute(() -> {
            try {
                Thread.sleep(400);
                if (this.isAlive()) {
                    this.setDodging(false);
                }
            } catch (InterruptedException e) {
                this.setDodging(false);
            }
        });
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 8) {
            if (this.level().isClientSide) {
                for (int i = 0; i < 12; i++) {
                    this.level().addParticle(
                            net.minecraft.core.particles.ParticleTypes.CLOUD,
                            this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            this.getY() + this.random.nextDouble() * 0.5,
                            this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            (this.random.nextDouble() - 0.5) * 0.3,
                            0.1,
                            (this.random.nextDouble() - 0.5) * 0.3
                    );
                }
            }
        } else {
            super.handleEntityEvent(pId);
        }
    }

    @Override
    protected void registerGoals() {
        ItemStack mainHandItem = this.getMainHandItem();
        boolean hasGun = mainHandItem.getItem() instanceof GunItem;

        if (hasGun) {
            this.goalSelector.addGoal(1, new GunAttackGoal<>(this, mainHandItem, 1.0F, AIType.TACTICAL, 3));
        } else {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false) {
                @Override
                protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
                    if (pDistToEnemySqr <= this.getAttackReachSqr(pEnemy) && this.getTicksUntilNextAttack() <= 0 && !AdjudicatorEntity.this.isAttacking()) {
                        AdjudicatorEntity.this.setAttacking(true);
                        this.resetAttackCooldown();
                        this.mob.swing(InteractionHand.MAIN_HAND);
                    }
                }

                @Override
                protected double getAttackReachSqr(LivingEntity pEnemy) {
                    return super.getAttackReachSqr(pEnemy) * 1.3;
                }
            });
        }

        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                if (this.mob.getLastHurtByMob() instanceof AdjudicatorEntity) {
                    return false;
                }
                return super.canUse();
            }
        }.setAlertOthers());

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof AdjudicatorEntity) {
            return false;
        }
        return super.canAttack(target);
    }

    private void maybeAlertAllies() {
        if (this.ticksUntilNextAlert > 0) {
            --this.ticksUntilNextAlert;
        } else {
            if (this.getSensing().hasLineOfSight(this.getTarget())) {
                this.alertAllies();
            }
            this.ticksUntilNextAlert = 20 + this.random.nextInt(20);
        }
    }

    private void alertAllies() {
        AABB alertArea = AABB.unitCubeFromLowerCorner(this.position()).inflate(ALLIANCE_RANGE, ALERT_RANGE_Y, ALLIANCE_RANGE);

        this.level().getEntitiesOfClass(AdjudicatorEntity.class, alertArea, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(entity -> entity != this)
                .filter(entity -> entity.getTarget() == null)
                .filter(entity -> !entity.isAlliedTo(this.getTarget()))
                .forEach(entity -> entity.setTarget(this.getTarget()));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof AdjudicatorEntity) {
            return;
        }

        if (this.getTarget() == null && target != null) {
            this.ticksUntilNextAlert = this.random.nextInt(20);
        }
        super.setTarget(target);
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

    public void setDodging(boolean dodging) {
        this.entityData.set(DODGING, dodging);
    }

    public boolean isDodging() {
        return this.entityData.get(DODGING);
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
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float distanceFactor) {
        this.doHurtTarget(target);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AlertCooldown", this.ticksUntilNextAlert);
        tag.putInt("DodgeCooldown", this.dodgeCooldown);
        tag.putInt("LastDodgeTime", this.lastDodgeTime);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ticksUntilNextAlert = tag.getInt("AlertCooldown");
        this.dodgeCooldown = tag.getInt("DodgeCooldown");
        this.lastDodgeTime = tag.getInt("LastDodgeTime");
    }
}