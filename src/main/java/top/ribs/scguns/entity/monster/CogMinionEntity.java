package top.ribs.scguns.entity.monster;

import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.attributes.SCAttributes;
import top.ribs.scguns.entity.config.CogMinionConfig;
import top.ribs.scguns.entity.weapon.ScGunsWeapon;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.interfaces.IEntityCanReload;
import top.ribs.scguns.item.GunItem;

import java.util.List;

public class CogMinionEntity extends Monster implements IEntityCanReload {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(CogMinionEntity.class, EntityDataSerializers.BOOLEAN);

    private int reloadTick;
    public int ticksUntilNextAttack = 0;
    private int shotCount = 0;
    private int maxShots;
    private static final int MIN_SHOTS = 4;
    private static final int MAX_SHOTS = 9;
    private static final int RELOAD_TIME = 30;

    public CogMinionEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setCanPickUpLoot(true);
    }
    private void setMaxShots() {
        this.maxShots = MIN_SHOTS + this.random.nextInt(MAX_SHOTS - MIN_SHOTS + 1);
    }
    @Override
    public int mob$getReloadTick() {
        return this.reloadTick;
    }

    @Override
    public void mob$setReloadTick(int reloadTick) {
        this.reloadTick = reloadTick;
    }

    public void setTicksUntilNextAttack(int ticksUntilNextAttack) {
        this.ticksUntilNextAttack = ticksUntilNextAttack;
    }
    private boolean isHoldingExplosiveBlock() {
        ItemStack mainHandItem = this.getMainHandItem();
        return mainHandItem.is(ModTags.Items.EXPLOSIVE_BLOCK);
    }
    private void explodeIfHoldingExplosive() {
        if (isHoldingExplosiveBlock() && !this.level().isClientSide) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 5.0F, false, Level.ExplosionInteraction.MOB);
            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }
    public int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }
    public boolean canBreatheUnderwater() {
        return true;
    }
    public int getAttackCooldown() {
        ItemStack mainHandItem = this.getMainHandItem();
        if (mainHandItem.getItem() instanceof GunItem) {
            ScGunsWeapon weapon = new ScGunsWeapon(mainHandItem);
            double attackSpeedModifier = 2.0;
            return weapon.getAdjustedAttackCooldown(attackSpeedModifier);
        }
        return 40;
    }
    public void performRangedAttack(LivingEntity target) {
        if (this.shotCount >= this.maxShots) {
            this.setTicksUntilNextAttack(RELOAD_TIME);
            this.shotCount = 0;
            this.setMaxShots();
            return;
        }
        ItemStack mainHandItem = this.getMainHandItem();
        if (mainHandItem.getItem() instanceof GunItem) {
            ScGunsWeapon weapon = new ScGunsWeapon(mainHandItem);
            double inaccuracy = 0.33; // inaccuracy
            double dx = target.getX() - this.getX();
            double dy = target.getEyeY() - this.getEyeY();
            double dz = target.getZ() - this.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            double xSpread = dx + (this.random.nextDouble() - 0.5) * inaccuracy;
            double ySpread = dy + (this.random.nextDouble() - 0.5) * inaccuracy;
            double zSpread = dz + (this.random.nextDouble() - 0.5) * inaccuracy;
            double projectileSpeedModifier = 0.5;
            weapon.performRangedAttackIWeapon(this, this.getX() + xSpread, this.getY() + ySpread, this.getZ() + zSpread, weapon.getAdjustedProjectileSpeed(projectileSpeedModifier));
            this.shotCount++;
        }
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

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.5D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
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
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
        RandomSource random = pLevel.getRandom();
        CogMinionConfig config = ScorchedGuns.COG_MINION_CONFIG;

        if (random.nextFloat() < config.getSpawnWithItemChance()) {
            CogMinionConfig.ItemSpawnData spawnData = selectItemBasedOnChance(config.getItems(), random);
            if (spawnData != null) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(spawnData.getItem()));
                if (item != null) {
                    ItemStack itemStack = new ItemStack(item);
                    if (itemStack.isDamageableItem() && spawnData.getMinDurability() != null && spawnData.getMaxDurability() != null) {
                        float durabilityRange = spawnData.getMaxDurability() - spawnData.getMinDurability();
                        float randomDurability = spawnData.getMinDurability() + random.nextFloat() * durabilityRange;
                        itemStack.setDamageValue((int)(itemStack.getMaxDamage() * (1 - randomDurability)));
                    }

                    this.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                    this.setDropChance(EquipmentSlot.MAINHAND, spawnData.getDropChance());
                }
            }
        }
        return pSpawnData;
    }


    private CogMinionConfig.ItemSpawnData selectItemBasedOnChance(List<CogMinionConfig.ItemSpawnData> items, RandomSource random) {
        float totalChance = 0;
        for (CogMinionConfig.ItemSpawnData item : items) {
            totalChance += item.getSpawnChance();
        }
        float randomChance = random.nextFloat() * totalChance;
        float currentChance = 0;
        for (CogMinionConfig.ItemSpawnData item : items) {
            currentChance += item.getSpawnChance();
            if (randomChance < currentChance) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            setupAnimationStates();
        }

        if (this.ticksUntilNextAttack > 0) {
            this.ticksUntilNextAttack--;
        }
//
//        // Check and log if the entity is holding a gun item
//        ItemStack mainHandItem = this.getItemInHand(InteractionHand.MAIN_HAND);
//        if (mainHandItem.getItem() instanceof GunItem) {
//            LOGGER.info("CogMinionEntity is holding a GunItem.");
//        } else {
//            LOGGER.info("CogMinionEntity is not holding a GunItem.");
//        }
    }
    @Override
    public boolean wantsToPickUp(ItemStack pStack) {
        return this.canHoldItem(pStack);
    }
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;
    private int idleAnimationTimeout = 0;
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
    public boolean doHurtTarget(Entity pEntity) {
        boolean didHurt = super.doHurtTarget(pEntity);
        if (didHurt) {
            this.explodeIfHoldingExplosive();
        }
        return didHurt;
    }
    @Override
    protected Vec3i getPickupReach() {
        return new Vec3i(3, 3, 3);
    }

    @Override
    protected void pickUpItem(ItemEntity pItemEntity) {
        super.pickUpItem(pItemEntity);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new CogMinionGunAttackGoal(this, 15.0, 1.2));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(CogMinionEntity.class));
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
        return 1.0F;
    }
    public static class CogMinionGunAttackGoal extends Goal {
        private final CogMinionEntity shooter;
        private final double stopRange;
        private final double speedModifier;

        public CogMinionGunAttackGoal(CogMinionEntity shooter, double stopRange, double speedModifier) {
            this.shooter = shooter;
            this.stopRange = stopRange;
            this.speedModifier = speedModifier;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.shooter.getTarget();
            return target != null && target.isAlive() && this.isGunInHand();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void tick() {
            LivingEntity target = this.shooter.getTarget();
            if (target != null && target.isAlive()) {
                double distanceToTarget = this.shooter.distanceToSqr(target);
                if (distanceToTarget <= this.stopRange * this.stopRange) {
                    this.shooter.getNavigation().stop();
                } else {
                    this.shooter.getNavigation().moveTo(target, this.speedModifier);
                }

                if (this.shooter.getSensing().hasLineOfSight(target)) {
                    this.shooter.getLookControl().setLookAt(target);
                    this.shooter.getLookControl().setLookAt(target.getX(), target.getEyeY(), target.getZ());

                    if (this.shooter.getTicksUntilNextAttack() <= 0) {
                        this.shooter.setTicksUntilNextAttack(this.shooter.getAttackCooldown());
                        this.shooter.performRangedAttack(target);
                    }
                }
            }
        }

        private boolean isGunInHand() {
            ItemStack mainHandItem = this.shooter.getMainHandItem();
            return mainHandItem.getItem() instanceof GunItem;
        }
    }

}
