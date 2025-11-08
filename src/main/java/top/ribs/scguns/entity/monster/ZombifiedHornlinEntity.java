package top.ribs.scguns.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.config.EntityEquipmentConfig;
import top.ribs.scguns.entity.ai.ConsumeGoldGoal;
import top.ribs.scguns.entity.ai.GoldSeekingGoal;
import top.ribs.scguns.entity.util.IGoldConsumingEntity;

import java.util.UUID;

public class ZombifiedHornlinEntity extends Monster implements RangedAttackMob, NeutralMob, IGoldConsumingEntity {
    private static final double ALLIANCE_RANGE = 26.0;
    private static final int ALERT_RANGE_Y = 10;

    private static final EntityDataAccessor<Boolean> DATA_EATING = SynchedEntityData.defineId(ZombifiedHornlinEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_PREPARING_EAT = SynchedEntityData.defineId(ZombifiedHornlinEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> DATA_HELD_FOOD = SynchedEntityData.defineId(ZombifiedHornlinEntity.class, EntityDataSerializers.ITEM_STACK);

    private int goldEatingCooldown = 0;
    private int slagProductionCooldown = 0;
    private ItemEntity targetGoldItem = null;
    private float accumulatedGoldValue = 0.0F;

    private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost", 0.05D, AttributeModifier.Operation.ADDITION);
    private static final UniformInt FIRST_ANGER_SOUND_DELAY = TimeUtil.rangeOfSeconds(0, 1);
    private int playFirstAngerSoundIn;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;
    private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(1, 2);
    private int ticksUntilNextAlert;

    public ZombifiedHornlinEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEAD;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30D)
                .add(Attributes.FOLLOW_RANGE, 26D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.5f)
                .add(Attributes.ARMOR, 2f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8f)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0D);
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty,
                                        MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData,
                                        @Nullable CompoundTag pDataTag) {
        EntityEquipmentConfig.equipEntity(this, "zombified_hornlin");
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_EATING, false);
        this.entityData.define(DATA_PREPARING_EAT, false);
        this.entityData.define(DATA_HELD_FOOD, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (this.goldEatingCooldown > 0) {
                this.goldEatingCooldown--;
            }
            if (this.slagProductionCooldown > 0) {
                this.slagProductionCooldown--;
            }

            if (this.getTarget() != null) {
                this.maybeAlertAllies();
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new ConsumeGoldGoal(this, this));
        this.goalSelector.addGoal(3, new GoldSeekingGoal(this, this, 1.0, 16.0F));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (player) -> this.isAngryAt(player) && !((Player) player).isCreative() && !((Player) player).isSpectator()));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
    }


    private void maybeAlertAllies() {
        if (this.ticksUntilNextAlert > 0) {
            --this.ticksUntilNextAlert;
        } else {
            if (this.getSensing().hasLineOfSight(this.getTarget())) {
                this.alertAllies();
            }
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }
    }

    private void alertAllies() {
        AABB alertArea = AABB.unitCubeFromLowerCorner(this.position()).inflate(ALLIANCE_RANGE, ALERT_RANGE_Y, ALLIANCE_RANGE);

        this.level().getEntitiesOfClass(ZombifiedHornlinEntity.class, alertArea, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(entity -> entity != this)
                .filter(entity -> entity.getTarget() == null)
                .filter(entity -> !entity.isAlliedTo(this.getTarget()))
                .forEach(entity -> {
                    entity.setTarget(this.getTarget());
                    entity.startPersistentAngerTimer();
                    if (this.getTarget() instanceof Player) {
                        entity.setPersistentAngerTarget(this.getTarget().getUUID());
                    }
                });

        this.level().getEntitiesOfClass(ZombifiedPiglin.class, alertArea, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(entity -> entity.getTarget() == null)
                .filter(entity -> !entity.isAlliedTo(this.getTarget()))
                .forEach(entity -> {
                    if (this.getTarget() instanceof Player player) {
                        entity.setLastHurtByPlayer(player);
                        entity.startPersistentAngerTimer();
                        entity.setPersistentAngerTarget(player.getUUID());
                        entity.setTarget(this.getTarget());

                        try {
                            java.lang.reflect.Field ticksUntilNextAlertField = entity.getClass().getDeclaredField("ticksUntilNextAlert");
                            ticksUntilNextAlertField.setAccessible(true);
                            ticksUntilNextAlertField.setInt(entity, 0);

                            java.lang.reflect.Field playFirstAngerSoundInField = entity.getClass().getDeclaredField("playFirstAngerSoundIn");
                            playFirstAngerSoundInField.setAccessible(true);
                            playFirstAngerSoundInField.setInt(entity, 0);
                        } catch (Exception e) {
                        }
                    }
                });
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.getTarget() == null && target != null) {
            this.playFirstAngerSoundIn = FIRST_ANGER_SOUND_DELAY.sample(this.random);
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }

        if (target instanceof Player) {
            this.setLastHurtByPlayer((Player) target);
        }

        super.setTarget(target);
    }

    @Override
    protected void customServerAiStep() {
        AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (this.isAngry()) {
            if (!this.isBaby() && !attributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING)) {
                attributeinstance.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }
            this.maybePlayFirstAngerSound();
        } else if (attributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING)) {
            attributeinstance.removeModifier(SPEED_MODIFIER_ATTACKING);
        }

        this.updatePersistentAnger((ServerLevel)this.level(), true);

        if (this.isAngry()) {
            this.lastHurtByPlayerTime = this.tickCount;
        }

        if (this.getTarget() == null && this.isAngry() && this.getPersistentAngerTarget() != null) {
            Player persistentTarget = this.level().getPlayerByUUID(this.getPersistentAngerTarget());
            if (persistentTarget != null && this.distanceToSqr(persistentTarget) <= this.getAttributeValue(Attributes.FOLLOW_RANGE) * this.getAttributeValue(Attributes.FOLLOW_RANGE)) {
                this.setTarget(persistentTarget);
            }
        }

        super.customServerAiStep();
    }

    private void maybePlayFirstAngerSound() {
        if (this.playFirstAngerSoundIn > 0) {
            --this.playFirstAngerSoundIn;
            if (this.playFirstAngerSoundIn == 0) {
                this.playAngerSound();
            }
        }
    }

    private void playAngerSound() {
        this.playSound(SoundEvents.ZOMBIFIED_PIGLIN_ANGRY, this.getSoundVolume() * 2.0F, this.getVoicePitch() * 1.8F);
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
        return this.isAngry() ? SoundEvents.ZOMBIFIED_PIGLIN_ANGRY : SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.ZOMBIFIED_PIGLIN_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.7F;
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float distanceFactor) {
        this.doHurtTarget(target);
    }

    @Override
    public boolean isEatingGold() {
        return this.entityData.get(DATA_EATING);
    }

    public void setEatingGold(boolean eating) {
        this.entityData.set(DATA_EATING, eating);
    }

    @Override
    public boolean isPreparingToEat() {
        return this.entityData.get(DATA_PREPARING_EAT);
    }

    public void setPreparingToEat(boolean preparing) {
        this.entityData.set(DATA_PREPARING_EAT, preparing);
    }

    @Override
    public ItemStack getHeldFoodItem() {
        return this.entityData.get(DATA_HELD_FOOD);
    }

    public void setHeldFoodItem(ItemStack stack) {
        this.entityData.set(DATA_HELD_FOOD, stack);
    }

    @Override
    public ItemEntity getTargetGoldItem() {
        return this.targetGoldItem;
    }

    @Override
    public void setTargetGoldItem(ItemEntity item) {
        this.targetGoldItem = item;
    }

    @Override
    public float getAccumulatedGoldValue() {
        return this.accumulatedGoldValue;
    }

    @Override
    public void setAccumulatedGoldValue(float value) {
        this.accumulatedGoldValue = value;
    }

    @Override
    public void addAccumulatedGoldValue(float value) {
        this.accumulatedGoldValue += value;
    }

    @Override
    public int getGoldEatingCooldown() {
        return this.goldEatingCooldown;
    }

    @Override
    public void setGoldEatingCooldown(int ticks) {
        this.goldEatingCooldown = ticks;
    }

    @Override
    public int getSlagProductionCooldown() {
        return this.slagProductionCooldown;
    }

    @Override
    public void setSlagProductionCooldown(int ticks) {
        this.slagProductionCooldown = ticks;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.remainingPersistentAngerTime = time;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.addPersistentAngerSaveData(tag);
        tag.putFloat("AccumulatedGoldValue", this.accumulatedGoldValue);
        tag.putInt("SlagProductionCooldown", this.slagProductionCooldown);
        tag.putInt("GoldEatingCooldown", this.goldEatingCooldown);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readPersistentAngerSaveData(this.level(), tag);
        this.accumulatedGoldValue = tag.getFloat("AccumulatedGoldValue");
        this.slagProductionCooldown = tag.getInt("SlagProductionCooldown");
        this.goldEatingCooldown = tag.getInt("GoldEatingCooldown");
    }

    @Override
    public boolean isPreventingPlayerRest(Player player) {
        return this.isAngryAt(player);
    }
}