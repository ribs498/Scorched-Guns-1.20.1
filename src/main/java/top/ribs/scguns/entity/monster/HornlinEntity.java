package top.ribs.scguns.entity.monster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.entity.projectile.BrassBoltEntity;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModSounds;
import java.util.EnumSet;

public class HornlinEntity extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(HornlinEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MUZZLE_FLASH_TIMER = SynchedEntityData.defineId(HornlinEntity.class, EntityDataSerializers.INT);

    private static final double ALLIANCE_RANGE = 26.0;
    private static final int ALERT_RANGE_Y = 10;
    private int ticksUntilNextAlert = 0;

    private int goldEatingCooldown = 0;
    private boolean isEatingGold = false;
    private int goldEatingTime = 0;
    private ItemEntity targetGoldItem = null;
    private int eatingPreparationTime = 0;

    private float accumulatedGoldValue = 0.0F;
    private static final float GOLD_VALUE_FOR_SLAG = 10F;
    private int slagProductionCooldown = 0;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    public int attackAnimationTimeout = 0;
    private int idleAnimationTimeout = 0;

    private int shotsFired = 0;
    private int burstCooldown = 0;
    private int attackTime = -1;
    private int conversionTime = -1;
    private boolean burstInterrupted = false;

    public HornlinEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30D)
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

        if (!this.level().isClientSide) {
            this.checkForConversion();
            if (this.getTarget() != null) {
                this.maybeAlertAllies();
            }
            this.handleGoldEating();
            this.handleSlagProduction();
        }

        if (this.isAttacking()) {
            if (this.burstCooldown > 0) {
                this.burstCooldown--;
            } else {
                if (--this.attackTime <= 0) {
                    this.attackTime = 8;
                    LivingEntity target = this.getTarget();
                    if (target != null && !this.burstInterrupted) {
                        this.performRangedAttack(target, 1.0F);
                        this.shotsFired++;

                        if (this.shotsFired >= 1 + this.random.nextInt(3)) {
                            this.burstCooldown = 20 + this.random.nextInt(20);
                            this.shotsFired = 0;
                            this.burstInterrupted = false;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float amount) {
        boolean result = super.hurt(damageSource, amount);

        if (result && this.isAttacking() && this.burstCooldown == 0 && this.shotsFired > 0) {
            if (this.random.nextFloat() < 0.4F) {
                this.burstInterrupted = true;
                this.burstCooldown = 15 + this.random.nextInt(15);
                this.shotsFired = 0;
            }
        }

        return result;
    }
    public ItemEntity getTargetGoldItem() {
        return this.targetGoldItem;
    }
    private void handleGoldEating() {
        if (this.goldEatingCooldown > 0) {
            this.goldEatingCooldown--;
        }
        if (this.slagProductionCooldown > 0) {
            this.slagProductionCooldown--;
        }

        if (this.eatingPreparationTime > 0) {
            this.eatingPreparationTime--;
            if (this.targetGoldItem != null && !this.targetGoldItem.isRemoved()) {
                this.getNavigation().stop();
                // Look at the gold item while preparing
                this.getLookControl().setLookAt(this.targetGoldItem, 30.0F, 30.0F);
                if (this.eatingPreparationTime <= 0) {
                    this.startEating();
                }
            } else {
                this.cancelEating();
            }
            return;
        }

        if (this.isEatingGold) {
            this.goldEatingTime--;
            // Keep looking at the gold item while eating
            if (this.targetGoldItem != null && !this.targetGoldItem.isRemoved()) {
                this.getLookControl().setLookAt(this.targetGoldItem, 30.0F, 30.0F);
            }
            if (this.goldEatingTime % 10 == 0) {
                this.showEatingParticles();
            }
            if (this.goldEatingTime <= 0) {
                this.finishEating();
            }
            return;
        }

        if (!this.level().isClientSide && this.goldEatingCooldown == 0 && this.targetGoldItem == null) {
            AABB closeArea = new AABB(this.getX() - 1.5, this.getY() - 0.5, this.getZ() - 1.5,
                    this.getX() + 1.5, this.getY() + 1.5, this.getZ() + 1.5);

            this.level().getEntitiesOfClass(ItemEntity.class, closeArea)
                    .stream()
                    .filter(item -> !item.isRemoved())
                    .filter(item -> isGoldItem(item.getItem()))
                    .filter(item -> this.distanceTo(item) <= 1.5)
                    .findFirst()
                    .ifPresent(goldItem -> {
                        this.targetGoldItem = goldItem;
                        this.prepareToEat();
                    });
        }
    }

    private void showEatingParticles() {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 2; i++) {
                double particleX = this.getX() + (this.random.nextDouble() - 0.5) * 0.4;
                double particleY = this.getY() + this.getEyeHeight() - 0.2;
                double particleZ = this.getZ() + (this.random.nextDouble() - 0.5) * 0.4;
                double velocityX = (this.random.nextDouble() - 0.5) * 0.2;
                double velocityY = this.random.nextDouble() * 0.2;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.2;

                serverLevel.sendParticles(ParticleTypes.EGG_CRACK,
                        particleX, particleY, particleZ,
                        1, velocityX, velocityY, velocityZ, 0.0);
            }
        }
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

        // Alert other Hornlins
        this.level().getEntitiesOfClass(HornlinEntity.class, alertArea, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(entity -> entity != this)
                .filter(entity -> entity.getTarget() == null)
                .filter(entity -> !entity.isAlliedTo(this.getTarget()))
                .forEach(entity -> {
                    entity.setTarget(this.getTarget());
                });

        // Alert nearby Piglins
        this.level().getEntitiesOfClass(Piglin.class, alertArea, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(entity -> entity.getTarget() == null)
                .filter(entity -> !entity.isAlliedTo(this.getTarget()))
                .forEach(entity -> {
                    if (this.getTarget() instanceof Player player) {
                        try {
                            var brain = entity.getBrain();
                            brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT, player.getUUID());
                            brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, player);
                            entity.setTarget(player);
                        } catch (Exception e) {
                            entity.setTarget(player);
                        }
                    } else {
                        entity.setTarget(this.getTarget());
                    }
                });
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.getTarget() == null && target != null) {
            this.ticksUntilNextAlert = this.random.nextInt(20);
        }
        super.setTarget(target);
    }
    private static boolean isGoldItem(ItemStack stack) {
        return isPoisonItem(stack) ||
                stack.is(Items.GOLD_INGOT) ||
                stack.is(Items.GOLD_NUGGET) ||
                stack.is(Items.GOLD_BLOCK) ||
                stack.is(Items.RAW_GOLD) ||
                stack.is(Items.RAW_GOLD_BLOCK) ||
                (stack.getItem() instanceof ArmorItem armor && armor.getMaterial() == ArmorMaterials.GOLD) ||
                stack.is(Items.GOLDEN_SWORD) ||
                stack.is(Items.GOLDEN_PICKAXE) ||
                stack.is(Items.GOLDEN_AXE) ||
                stack.is(Items.GOLDEN_SHOVEL) ||
                stack.is(Items.GOLDEN_HOE) ||
                stack.is(Items.GOLDEN_APPLE) ||
                stack.is(Items.GOLDEN_CARROT) ||
                stack.is(Items.GOLDEN_HORSE_ARMOR) ||
                stack.is(Items.DEEPSLATE_GOLD_ORE) ||
                stack.is(Items.GOLD_ORE) ||
                stack.is(Items.NETHER_GOLD_ORE) ||
                stack.is(Items.ENCHANTED_GOLDEN_APPLE);
    }
    private static boolean isPoisonItem(ItemStack stack) {
        return stack.is(ModItems.SULFUR_CHUNK.get()) ||
                stack.is(ModBlocks.SULFUR_BLOCK.get().asItem()) ||
                stack.is(ModBlocks.NETHER_SULFUR_ORE.get().asItem()) ||
                stack.is(ModBlocks.SULFUR_ORE.get().asItem()) ||
                stack.is(ModBlocks.DEEPSLATE_SULFUR_ORE.get().asItem()) ||
                stack.is(ModItems.SULFUR_DUST.get());
    }
    private float getPoisonEffect(ItemStack stack) {
        return -8.0F;
    }

    private int getPoisonDuration(ItemStack stack) {
        return 100;
    }

    private void prepareToEat() {
        if (this.targetGoldItem == null || this.targetGoldItem.isRemoved()) {
            this.cancelEating();
            return;
        }

        this.eatingPreparationTime = 10;
        this.getNavigation().stop();

        this.playSound(SoundEvents.ITEM_PICKUP, 0.8F, 1.2F + this.random.nextFloat() * 0.4F);
    }

    private void startEating() {
        if (this.targetGoldItem == null || this.targetGoldItem.isRemoved()) {
            this.cancelEating();
            return;
        }

        this.isEatingGold = true;
        this.goldEatingTime = 20;
        this.playSound(SoundEvents.PLAYER_BURP, 0.6F, 1.5F);
    }

    private void finishEating() {
        if (this.targetGoldItem != null && !this.targetGoldItem.isRemoved()) {
            this.eatGold(this.targetGoldItem);
        }

        this.isEatingGold = false;
        this.targetGoldItem = null;
        this.goldEatingCooldown = 10;
    }

    private void cancelEating() {
        this.isEatingGold = false;
        this.eatingPreparationTime = 0;
        this.goldEatingTime = 0;
        this.targetGoldItem = null;
        this.goldEatingCooldown = 3;
    }

    private void eatGold(ItemEntity goldItem) {
        ItemStack stack = goldItem.getItem();

        if (isPoisonItem(stack)) {
            float poisonPenalty = getPoisonEffect(stack);
            int poisonDuration = getPoisonDuration(stack);

            this.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, 0));

            this.accumulatedGoldValue = Math.max(0, this.accumulatedGoldValue + poisonPenalty);

            this.hurt(this.damageSources().magic(), 2.0F);

            this.playSound(SoundEvents.PLAYER_HURT, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            this.playSound(SoundEvents.GENERIC_DRINK, 0.8F, 0.5F); // Sickly drinking sound

            if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 8; i++) {
                    double particleX = this.getX() + (this.random.nextDouble() - 0.5) * 0.6;
                    double particleY = this.getY() + this.getEyeHeight() - 0.1;
                    double particleZ = this.getZ() + (this.random.nextDouble() - 0.5) * 0.6;
                    double velocityX = (this.random.nextDouble() - 0.5) * 0.3;
                    double velocityY = this.random.nextDouble() * 0.3 + 0.1;
                    double velocityZ = (this.random.nextDouble() - 0.5) * 0.3;

                    serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, // Green-ish particles
                            particleX, particleY, particleZ,
                            1, velocityX, velocityY, velocityZ, 0.0);
                }
            }
        } else {
            float healthToRestore = getHealthFromGold(stack);
            float goldNuggetValue = getGoldNuggetValue(stack);

            this.heal(healthToRestore);
            this.accumulatedGoldValue += goldNuggetValue;

            float pitch = 0.8F + this.random.nextFloat() * 0.4F;
            this.playSound(SoundEvents.PLAYER_BURP, 1.2F, pitch);
            this.playSound(SoundEvents.BONE_MEAL_USE, 0.8F, 0.6F + this.random.nextFloat() * 0.8F);
        }

        stack.shrink(1);
        if (stack.isEmpty()) {
            goldItem.discard();
        }
    }

    private void produceSlag() {
        if (this.accumulatedGoldValue < GOLD_VALUE_FOR_SLAG) {
            return;
        }
        this.accumulatedGoldValue -= GOLD_VALUE_FOR_SLAG;

        if (this.accumulatedGoldValue < 0) {
            this.accumulatedGoldValue = 0;
        }

        this.playSound(SoundEvents.PLAYER_BURP, 1.2F, 0.5F + this.random.nextFloat() * 0.3F);

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 5; i++) {
                double particleX = this.getX() + (this.random.nextDouble() - 0.5);
                double particleY = this.getY() + this.getEyeHeight();
                double particleZ = this.getZ() + (this.random.nextDouble() - 0.5);
                double velocityX = (this.random.nextDouble() - 0.5) * 0.4;
                double velocityY = this.random.nextDouble() * 0.4 + 0.2;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.4;

                serverLevel.sendParticles(ParticleTypes.ITEM_SLIME,
                        particleX, particleY, particleZ,
                        1, velocityX, velocityY, velocityZ, 0.0);
            }
        }

        ItemStack slagStack = new ItemStack(ModItems.AUREOUS_SLAG.get());
        this.spawnAtLocation(slagStack);

        this.slagProductionCooldown = 150;
    }

    private float getGoldNuggetValue(ItemStack stack) {
        if (stack.is(Items.GOLD_NUGGET)) return 0.7F;
        if (stack.is(Items.GOLD_INGOT)) return 6.0F;      // Worth 9 nuggets, gives 6
        if (stack.is(Items.GOLD_BLOCK)) return 54.0F;     // Worth 81 nuggets, gives 54
        if (stack.is(Items.RAW_GOLD)) return 6.0F;        // Same as ingot when smelted
        if (stack.is(Items.RAW_GOLD_BLOCK)) return 54.0F; // Same as gold block when smelted

        // Special food items
        if (stack.is(Items.GOLDEN_APPLE)) return 48.0F;   // Worth 72 nuggets (8 ingots), gives 48
        if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) return 432.0F; // Worth 648 nuggets, gives 432
        if (stack.is(Items.GOLDEN_CARROT)) return 6.0F;   // Worth 8 nuggets, gives 6
        if (stack.is(Items.GOLDEN_HORSE_ARMOR)) return 24.0F; // Worth 36 nuggets, gives 24

        if (stack.is(Items.NETHER_GOLD_ORE) || stack.is(Items.GOLD_ORE) || stack.is(Items.DEEPSLATE_GOLD_ORE)) {
            return 5.0F;
        }

        // Armor pieces - based on crafting cost with ~33% loss
        if (stack.is(Items.GOLDEN_HELMET)) return 30.0F;     // Worth 45 nuggets (5 ingots)
        if (stack.is(Items.GOLDEN_CHESTPLATE)) return 48.0F; // Worth 72 nuggets (8 ingots)
        if (stack.is(Items.GOLDEN_LEGGINGS)) return 42.0F;   // Worth 63 nuggets (7 ingots)
        if (stack.is(Items.GOLDEN_BOOTS)) return 24.0F;      // Worth 36 nuggets (4 ingots)

        // Tools - also with ~33% loss
        if (stack.is(Items.GOLDEN_SWORD)) return 12.0F;      // Worth 18 nuggets (2 ingots)
        if (stack.is(Items.GOLDEN_PICKAXE)) return 18.0F;    // Worth 27 nuggets (3 ingots)
        if (stack.is(Items.GOLDEN_AXE)) return 18.0F;        // Worth 27 nuggets (3 ingots)
        if (stack.is(Items.GOLDEN_SHOVEL)) return 6.0F;      // Worth 9 nuggets (1 ingot)
        if (stack.is(Items.GOLDEN_HOE)) return 12.0F;        // Worth 18 nuggets (2 ingots)

        return 5.0F; // Conservative default
    }

    private float getHealthFromGold(ItemStack stack) {
        float goldValue = getGoldNuggetValue(stack);
        return Math.min(goldValue / 3.0F, 8.0F);
    }


    private void handleSlagProduction() {
        if (this.slagProductionCooldown > 0 || this.isEatingGold || this.eatingPreparationTime > 0) {
            return;
        }
        if (this.accumulatedGoldValue >= GOLD_VALUE_FOR_SLAG) {
            this.produceSlag();
        }
    }


    public boolean isEatingGold() {
        return this.isEatingGold;
    }

    public boolean isPreparingToEat() {
        return this.eatingPreparationTime > 0;
    }

    private void checkForConversion() {
        if (this.level().dimension() == Level.OVERWORLD && !this.isConverting()) {
            this.startConversion();
        }

        if (this.isConverting()) {
            this.conversionTime--;
            double shakeIntensity = 0.01;
            this.setPosRaw(this.getX() + (this.random.nextDouble() - 0.5) * shakeIntensity,
                    this.getY(),
                    this.getZ() + (this.random.nextDouble() - 0.5) * shakeIntensity);

            if (this.conversionTime <= 0) {
                this.convertToZombifiedHornlin();
            }
        }
    }

    private boolean isConverting() {
        return this.conversionTime > -1;
    }

    private void startConversion() {
        this.conversionTime = 200;
    }

    private void convertToZombifiedHornlin() {
        ZombifiedHornlinEntity zombifiedHornlin = this.convertTo(ModEntities.ZOMBIFIED_HORNLIN.get(), true);
        if (zombifiedHornlin != null) {
            zombifiedHornlin.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
            this.playConvertedSound();
        }
    }

    protected void playConvertedSound() {
        this.playSound(SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED, 1.0F, 1.0F);
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
    public void registerGoals() {
        this.goalSelector.addGoal(1, new GoldSeekingGoal(this, 1.0, 8.0F));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 10, 15.0F) {
            @Override
            public void stop() {
                super.stop();
                HornlinEntity.this.setAttacking(false);
            }

            @Override
            public void start() {
                Player targetPlayer = HornlinEntity.this.level().getNearestPlayer(HornlinEntity.this, 26.0D);
                if (targetPlayer != null && isWearingGold(targetPlayer) && !targetPlayer.isCreative() && !targetPlayer.isSpectator()) {
                    super.start();
                    HornlinEntity.this.setAttacking(true);
                }
            }
        });
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // Updated targeting goals with creative/spectator checks
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> isWearingGold((Player) player) && !((Player) player).isCreative() && !((Player) player).isSpectator()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    private boolean isWearingGold(Player player) {
        for (ItemStack itemStack : player.getArmorSlots()) {
            if (itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial() == ArmorMaterials.GOLD) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIGLIN_BRUTE_AMBIENT;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.PIGLIN_BRUTE_HURT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_BRUTE_DEATH;
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

        float inaccuracy = 0.15F;
        projectile.shoot(d0, d1, d2, 1.7F, inaccuracy);

        this.level().addFreshEntity(projectile);
        this.playSound(ModSounds.GREASER_SMG_FIRE.get(), 1.0F, 1.0F);
        this.triggerMuzzleFlash();
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("AccumulatedGoldValue", this.accumulatedGoldValue);
        tag.putInt("SlagProductionCooldown", this.slagProductionCooldown);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.accumulatedGoldValue = tag.getFloat("AccumulatedGoldValue");
        this.slagProductionCooldown = tag.getInt("SlagProductionCooldown");
    }

    public static class GoldSeekingGoal extends Goal {
        private final HornlinEntity hornlin;
        private final double speed;
        private final float searchRange;
        private ItemEntity targetGold;

        public GoldSeekingGoal(HornlinEntity hornlin, double speed, float searchRange) {
            this.hornlin = hornlin;
            this.speed = speed;
            this.searchRange = searchRange;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.hornlin.isEatingGold() || this.hornlin.isPreparingToEat()) {
                return false;
            }

            this.targetGold = this.findNearestGold();
            return this.targetGold != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.hornlin.isEatingGold() || this.hornlin.isPreparingToEat()) {
                return false;
            }

            return this.targetGold != null && !this.targetGold.isRemoved();
        }

        @Override
        public void start() {
            if (this.targetGold != null) {
                this.hornlin.getNavigation().moveTo(this.targetGold, this.speed);
            }
        }

        @Override
        public void stop() {
            this.targetGold = null;
            this.hornlin.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.targetGold != null) {
                if (this.hornlin.getNavigation().isDone()) {
                    this.hornlin.getNavigation().moveTo(this.targetGold, this.speed);
                }
            }
        }

        private ItemEntity findNearestGold() {
            AABB searchArea = AABB.unitCubeFromLowerCorner(this.hornlin.position()).inflate(this.searchRange);

            return this.hornlin.level().getEntitiesOfClass(ItemEntity.class, searchArea)
                    .stream()
                    .filter(item -> !item.isRemoved())
                    .filter(item -> HornlinEntity.isGoldItem(item.getItem()))
                    .min((item1, item2) -> Double.compare(
                            this.hornlin.distanceToSqr(item1),
                            this.hornlin.distanceToSqr(item2)
                    ))
                    .orElse(null);
        }


    }
}