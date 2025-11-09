package top.ribs.scguns.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.entity.ai.AIType;
import top.ribs.scguns.entity.ai.GunAttackGoal;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModTags;

import java.util.Objects;
import java.util.Optional;

public class ViventrumEntity extends TamableAnimal {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TIMEOUT =
            SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PATROLLING =
            SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> PATROL_ORIGIN =
            SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> PARTYING =
            SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DEFENSIVE =
            SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ARMOR_PLATES =
            SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEAVY_ARMOR_PLATES =
            SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.INT);

    private static final int MAX_ARMOR_PLATES = 4;
    private static final int PATROL_RADIUS = 9;
    private static final int PATROL_MOVE_INTERVAL = 100;
    private static final int PATROL_DURATION = 80;
    private int patrolTimer = 0;
    private BlockPos currentPatrolTarget = null;

    private static final float DEFENSIVE_HEALTH_THRESHOLD = 0.25f;
    private static final float DEFENSIVE_ARMOR_BONUS = 15.0f;
    private static final float DEFENSIVE_DAMAGE_REDUCTION = 0.6f;

    public ViventrumEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 24D)
                .add(Attributes.FOLLOW_RANGE, 16D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ARMOR, 2f)
                .add(Attributes.ATTACK_DAMAGE, 4f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.3f)
                .add(Attributes.FLYING_SPEED, 0.3D);
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(ATTACK_TIMEOUT, 0);
        this.entityData.define(PATROLLING, false);
        this.entityData.define(PATROL_ORIGIN, Optional.empty());
        this.entityData.define(PARTYING, false);
        this.entityData.define(DEFENSIVE, false);
        this.entityData.define(ARMOR_PLATES, 0);
        this.entityData.define(HEAVY_ARMOR_PLATES, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (this.isTame()) {
                updateDefensiveState();
            }

            if (this.isAttacking() && this.getAttackTimeout() > 0) {
                this.setAttackTimeout(this.getAttackTimeout() - 1);
                if (this.getAttackTimeout() == 5) {
                    LivingEntity target = this.getTarget();
                    if (target != null && this.distanceToSqr(target) <= this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F + target.getBbWidth()) {
                        this.doHurtTarget(target);
                    }
                }
                if (this.getAttackTimeout() <= 0) {
                    this.setAttacking(false);
                }
            }

            if (this.isPatrolling()) {
                handlePatrolling();
            }
        }
    }

    private void updateDefensiveState() {
        float healthPercent = this.getHealth() / this.getMaxHealth();
        boolean shouldBeDefensive = healthPercent <= DEFENSIVE_HEALTH_THRESHOLD;

        if (shouldBeDefensive != this.isDefensive()) {
            this.setDefensive(shouldBeDefensive);

            if (shouldBeDefensive) {
                this.getNavigation().stop();
                this.setTarget(null);
            }
        }
    }

    private void handlePatrolling() {
        Optional<BlockPos> patrolOrigin = this.getPatrolOrigin();
        if (!patrolOrigin.isPresent()) {
            return;
        }

        if (this.patrolTimer <= 0) {
            if (this.random.nextFloat() < 0.5) {
                this.currentPatrolTarget = patrolOrigin.get().offset(
                        this.random.nextInt(PATROL_RADIUS * 2) - PATROL_RADIUS,
                        0,
                        this.random.nextInt(PATROL_RADIUS * 2) - PATROL_RADIUS
                );
                this.getNavigation().moveTo(this.currentPatrolTarget.getX() + 0.5,
                        this.currentPatrolTarget.getY(),
                        this.currentPatrolTarget.getZ() + 0.5, 0.8);
                this.patrolTimer = PATROL_DURATION;
            } else {
                this.getNavigation().stop();
                this.currentPatrolTarget = null;
                this.patrolTimer = PATROL_MOVE_INTERVAL / 2;
            }
        } else {
            this.patrolTimer--;

            if (this.currentPatrolTarget != null &&
                    this.distanceToSqr(this.currentPatrolTarget.getX(), this.currentPatrolTarget.getY(), this.currentPatrolTarget.getZ()) < 4.0) {
                this.getNavigation().stop();
                this.currentPatrolTarget = null;
                this.patrolTimer = 20;
            }
        }

        if (this.distanceToSqr(patrolOrigin.get().getX(), patrolOrigin.get().getY(), patrolOrigin.get().getZ()) > (PATROL_RADIUS + 3) * (PATROL_RADIUS + 3)) {
            this.getNavigation().moveTo(patrolOrigin.get().getX() + 0.5,
                    patrolOrigin.get().getY(),
                    patrolOrigin.get().getZ() + 0.5, 1.0);
            this.currentPatrolTarget = null;
            this.patrolTimer = 40;
        }
    }

    @Override
    protected void registerGoals() {
        ItemStack mainHandItem = this.getMainHandItem();
        boolean hasGun = mainHandItem.getItem() instanceof GunItem;

        if (!hasGun) {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false) {
                @Override
                public boolean canUse() {
                    return !ViventrumEntity.this.isDefensive() && super.canUse();
                }

                @Override
                public boolean canContinueToUse() {
                    return !ViventrumEntity.this.isDefensive() && super.canContinueToUse();
                }

                @Override
                protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
                    if (pDistToEnemySqr <= this.getAttackReachSqr(pEnemy) && this.getTicksUntilNextAttack() <= 0 && !ViventrumEntity.this.isAttacking()) {
                        ViventrumEntity.this.setAttacking(true);
                        this.resetAttackCooldown();
                        this.mob.swing(InteractionHand.MAIN_HAND);
                    }
                }
            });
        } else {
            int difficulty = this.level().getDifficulty().getId() + 1;
            this.goalSelector.addGoal(2, new GunAttackGoal<>(this, mainHandItem, 1.0F, AIType.SMART, difficulty) {
                @Override
                public boolean canUse() {
                    return !ViventrumEntity.this.isDefensive() && super.canUse();
                }

                @Override
                public boolean canContinueToUse() {
                    return !ViventrumEntity.this.isDefensive() && super.canContinueToUse();
                }
            });
        }

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new SitWhenOrderedToGoal(this));

        if (this.isTame()) {
            this.goalSelector.addGoal(4, new ViventrumFollowOwnerGoal(this, 1.3, 10.0F, 2.0F, false));
        }

        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Zombie.class, true));
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull net.minecraft.world.entity.player.Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || itemstack.is(Items.DIAMOND) && !this.isTame();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            if (this.isTame()) {
                if (player.isShiftKeyDown() && itemstack.isEmpty()) {
                    ItemStack heldItem = this.getMainHandItem();
                    if (!heldItem.isEmpty()) {
                        this.spawnAtLocation(heldItem);
                        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        return InteractionResult.SUCCESS;
                    }

                    ItemStack helmet = this.getItemBySlot(EquipmentSlot.HEAD);
                    if (!helmet.isEmpty()) {
                        this.spawnAtLocation(helmet);
                        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        return InteractionResult.SUCCESS;
                    }

                    return InteractionResult.PASS;
                }

                if (player.isShiftKeyDown() && itemstack.getItem() == ModItems.ARMOR_PLATE.get()) {
                    if (this.getArmorPlates() + this.getHeavyArmorPlates() >= MAX_ARMOR_PLATES) {
                        player.displayClientMessage(Component.translatable("message.mechanical_entity.max_armor_plates"), true);
                        return InteractionResult.FAIL;
                    }

                    this.addArmorPlate(false);
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.playSound(SoundEvents.ARMOR_EQUIP_IRON, 0.5F, 1.0F);

                    int currentTotal = this.getArmorPlates() + this.getHeavyArmorPlates();
                    player.displayClientMessage(Component.translatable("message.mechanical_entity.armor_plating_added", currentTotal, MAX_ARMOR_PLATES), true);
                    return InteractionResult.SUCCESS;
                }

                if (player.isShiftKeyDown() && itemstack.getItem() == ModItems.HEAVY_ARMOR_PLATE.get()) {
                    if (this.getArmorPlates() + this.getHeavyArmorPlates() >= MAX_ARMOR_PLATES) {
                        player.displayClientMessage(Component.translatable("message.mechanical_entity.max_armor_plates"), true);
                        return InteractionResult.FAIL;
                    }

                    this.addArmorPlate(true);
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.playSound(SoundEvents.ARMOR_EQUIP_NETHERITE, 0.5F, 1.0F);

                    int currentTotal = this.getArmorPlates() + this.getHeavyArmorPlates();
                    player.displayClientMessage(Component.translatable("message.mechanical_entity.heavy_armor_plating_added", currentTotal, MAX_ARMOR_PLATES), true);
                    return InteractionResult.SUCCESS;
                }

                if (player.isShiftKeyDown() && itemstack.getItem() instanceof net.minecraft.world.item.AxeItem && (this.getArmorPlates() > 0 || this.getHeavyArmorPlates() > 0)) {
                    boolean wasHeavy = this.removeArmorPlate();
                    this.playSound(SoundEvents.ARMOR_STAND_BREAK, 0.5F, 1.0F);

                    ItemStack droppedPlate = new ItemStack(wasHeavy ? ModItems.HEAVY_ARMOR_PLATE.get() : ModItems.ARMOR_PLATE.get());
                    this.spawnAtLocation(droppedPlate);

                    int currentTotal = this.getArmorPlates() + this.getHeavyArmorPlates();
                    player.displayClientMessage(Component.translatable("message.mechanical_entity.armor_plating_removed", currentTotal, MAX_ARMOR_PLATES), true);
                    return InteractionResult.SUCCESS;
                }

                if (player.isShiftKeyDown() && itemstack.getItem() instanceof ArmorItem armorItem) {
                    if (armorItem.getEquipmentSlot() == EquipmentSlot.HEAD) {
                        if (itemstack.is(ModTags.Items.VIVENTRUM_BANNED_ITEMS)) {
                            player.displayClientMessage(Component.translatable("message.viventrum.item_too_heavy"), true);
                            return InteractionResult.FAIL;
                        }

                        ItemStack currentHelmet = this.getItemBySlot(EquipmentSlot.HEAD);

                        if (!currentHelmet.isEmpty()) {
                            this.spawnAtLocation(currentHelmet);
                        }

                        this.setItemSlot(EquipmentSlot.HEAD, itemstack.copy());
                        if (!player.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }

                if (player.isShiftKeyDown() && !itemstack.isEmpty() && !(itemstack.getItem() instanceof ArmorItem)) {
                    if (itemstack.is(ModTags.Items.VIVENTRUM_BANNED_ITEMS)) {
                        player.displayClientMessage(Component.translatable("message.viventrum.item_too_heavy"), true);
                        return InteractionResult.FAIL;
                    }

                    ItemStack currentWeapon = this.getMainHandItem();

                    if (!currentWeapon.isEmpty()) {
                        this.spawnAtLocation(currentWeapon);
                    }

                    this.setItemInHand(InteractionHand.MAIN_HAND, itemstack.copy());
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }

                if (player.isShiftKeyDown() && itemstack.getItem() == ModItems.ARMOR_PLATE.get()) {
                    if (this.getArmorPlates() >= MAX_ARMOR_PLATES) {
                        player.displayClientMessage(Component.translatable("message.mechanical_entity.max_armor_plates"), true);
                        return InteractionResult.FAIL;
                    }

                    this.addArmorPlate(false);
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.playSound(SoundEvents.ARMOR_EQUIP_IRON, 0.5F, 1.0F);

                    int currentPlates = this.getArmorPlates();
                    player.displayClientMessage(Component.translatable("message.mechanical_entity.armor_plating_added", currentPlates, MAX_ARMOR_PLATES), true);
                    return InteractionResult.SUCCESS;
                }

                if (player.isShiftKeyDown() && itemstack.getItem() == ModItems.HEAVY_ARMOR_PLATE.get()) {
                    if (this.getArmorPlates() >= MAX_ARMOR_PLATES) {
                        player.displayClientMessage(Component.translatable("message.mechanical_entity.max_armor_plates"), true);
                        return InteractionResult.FAIL;
                    }

                    this.addArmorPlate(true);
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.playSound(SoundEvents.ARMOR_EQUIP_NETHERITE, 0.5F, 1.0F);

                    int currentPlates = this.getArmorPlates();
                    player.displayClientMessage(Component.translatable("message.mechanical_entity.heavy_armor_plating_added", currentPlates, MAX_ARMOR_PLATES), true);
                    return InteractionResult.SUCCESS;
                }

                if (player.isShiftKeyDown() && itemstack.getItem() == Items.IRON_AXE && this.getArmorPlates() > 0) {
                    this.removeArmorPlate();
                    this.playSound(SoundEvents.ARMOR_STAND_BREAK, 0.5F, 1.0F);

                    ItemStack droppedPlate = new ItemStack(ModItems.ARMOR_PLATE.get());
                    this.spawnAtLocation(droppedPlate);

                    int currentPlates = this.getArmorPlates();
                    player.displayClientMessage(Component.translatable("message.mechanical_entity.armor_plating_removed", currentPlates, MAX_ARMOR_PLATES), true);
                    return InteractionResult.SUCCESS;
                }

                if (!player.isShiftKeyDown() && itemstack.is(ModItems.REPAIR_KIT.get()) && this.getHealth() < this.getMaxHealth()) {
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.heal(10.0F);
                    this.playSound(SoundEvents.GENERIC_EAT, 0.5F, 1.0F);
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HEART,
                                this.getX(), this.getY() + 0.5, this.getZ(),
                                3, 0.3, 0.3, 0.3, 0.1);
                    }
                    return InteractionResult.SUCCESS;
                }

                if (!player.isShiftKeyDown() && itemstack.isEmpty()) {
                    Component entityName = this.hasCustomName() ? this.getCustomName() : Component.translatable("entity.scguns.viventrum");

                    if (this.isOrderedToSit()) {
                        this.setOrderedToSit(false);
                        this.setPatrolling(false);
                        player.displayClientMessage(Component.translatable("message.viventrum.following", entityName), true);
                    } else if (this.isPatrolling()) {
                        this.setPatrolling(false);
                        this.setOrderedToSit(true);
                        player.displayClientMessage(Component.translatable("message.viventrum.sitting", entityName), true);
                    } else {
                        this.setPatrolling(true);
                        this.setPatrolOrigin(this.blockPosition());
                        this.spawnPatrolOriginParticles();
                        player.displayClientMessage(Component.translatable("message.viventrum.patrolling", entityName), true);
                    }
                    return InteractionResult.SUCCESS;
                }

                return InteractionResult.SUCCESS;
            } else if (itemstack.is(Items.DIAMOND)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }
    }

    @Override
    public boolean canTakeItem(ItemStack stack) {
        EquipmentSlot slot = Mob.getEquipmentSlotForItem(stack);
        if (!this.getItemBySlot(slot).isEmpty()) {
            return false;
        }
        return slot == EquipmentSlot.HEAD;
    }

    @Override
    public boolean canReplaceCurrentItem(ItemStack candidate, ItemStack existing) {
        if (existing.isEmpty()) {
            return Mob.getEquipmentSlotForItem(candidate) == EquipmentSlot.HEAD;
        }
        return false;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        ItemStack oldStack = this.getItemBySlot(slot);
        super.setItemSlot(slot, stack);

        if (!this.level().isClientSide && slot == EquipmentSlot.MAINHAND) {
            boolean hadGun = oldStack.getItem() instanceof GunItem;
            boolean hasGun = stack.getItem() instanceof GunItem;

            if (hadGun != hasGun) {
                this.goalSelector.removeAllGoals(goal -> true);
                this.registerGoals();
            }
        }
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
        if (attacking) {
            this.setAttackTimeout(10);
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

    public boolean isPatrolling() {
        return this.entityData.get(PATROLLING);
    }

    public void setPatrolling(boolean patrolling) {
        this.entityData.set(PATROLLING, patrolling);
    }

    public Optional<BlockPos> getPatrolOrigin() {
        return this.entityData.get(PATROL_ORIGIN);
    }

    public void setPatrolOrigin(BlockPos pos) {
        if (pos != null) {
            this.entityData.set(PATROL_ORIGIN, Optional.of(pos));
        } else {
            this.entityData.set(PATROL_ORIGIN, Optional.empty());
        }
    }

    public boolean isPartying() {
        return this.entityData.get(PARTYING);
    }

    public void setPartying(boolean partying) {
        this.entityData.set(PARTYING, partying);
    }

    public boolean isDefensive() {
        return this.entityData.get(DEFENSIVE);
    }

    public void setDefensive(boolean defensive) {
        this.entityData.set(DEFENSIVE, defensive);

        if (defensive) {
            Objects.requireNonNull(this.getAttribute(Attributes.ARMOR)).addTransientModifier(
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                            "defensive_bonus", DEFENSIVE_ARMOR_BONUS,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        } else {
            Objects.requireNonNull(this.getAttribute(Attributes.ARMOR)).removeModifier(
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                            "defensive_bonus", DEFENSIVE_ARMOR_BONUS,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION).getId());
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }

        if (this.isTame() && this.isDefensive()) {
            amount *= (1.0f - DEFENSIVE_DAMAGE_REDUCTION);
        }

        return super.hurt(source, amount);
    }

    public void spawnPatrolOriginParticles() {
        if (this.level() instanceof ServerLevel) {
            BlockPos pos = this.getPatrolOrigin().orElse(this.blockPosition());
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    10, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float f = (this.getPose() == Pose.STANDING) ? Math.min(partialTick * 6F, 1f) : 0f;
        this.walkAnimation.update(f, 0.2f);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void setRecordPlayingNearby(@NotNull BlockPos pos, boolean playing) {
        this.setPartying(playing && this.isTame());
    }

    public int getArmorPlates() {
        return this.entityData.get(ARMOR_PLATES);
    }

    public int getHeavyArmorPlates() {
        return this.entityData.get(HEAVY_ARMOR_PLATES);
    }

    public void setArmorPlates(int plates) {
        this.entityData.set(ARMOR_PLATES, plates);
        updateArmorFromPlates();
    }

    public void setHeavyArmorPlates(int plates) {
        this.entityData.set(HEAVY_ARMOR_PLATES, plates);
        updateArmorFromPlates();
    }

    public void addArmorPlate(boolean isHeavy) {
        int total = this.getArmorPlates() + this.getHeavyArmorPlates();
        if (total < MAX_ARMOR_PLATES) {
            if (isHeavy) {
                this.setHeavyArmorPlates(this.getHeavyArmorPlates() + 1);
            } else {
                this.setArmorPlates(this.getArmorPlates() + 1);
            }
        }
    }

    public boolean removeArmorPlate() {
        if (this.getHeavyArmorPlates() > 0) {
            this.setHeavyArmorPlates(this.getHeavyArmorPlates() - 1);
            return true;
        } else if (this.getArmorPlates() > 0) {
            this.setArmorPlates(this.getArmorPlates() - 1);
            return false;
        }
        return false;
    }

    private void updateArmorFromPlates() {
        int regularPlates = this.getArmorPlates();
        int heavyPlates = this.getHeavyArmorPlates();
        int totalArmor = regularPlates + (heavyPlates * 2);
        Objects.requireNonNull(this.getAttribute(Attributes.ARMOR)).setBaseValue(2.0 + totalArmor);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull net.minecraft.world.level.block.state.BlockState state, @NotNull net.minecraft.core.BlockPos pos) {
        // No fall damage
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.VEX_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int looting, boolean hitByPlayer) {
        super.dropCustomDeathLoot(damageSource, looting, hitByPlayer);

        ItemStack mainHandItem = this.getMainHandItem();
        if (!mainHandItem.isEmpty()) {
            this.spawnAtLocation(mainHandItem);
            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }

        ItemStack helmet = this.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.isEmpty()) {
            this.spawnAtLocation(helmet);
            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }

        int regularPlates = this.getArmorPlates();
        if (regularPlates > 0) {
            ItemStack armorPlates = new ItemStack(ModItems.ARMOR_PLATE.get(), regularPlates);
            this.spawnAtLocation(armorPlates);
        }

        int heavyPlates = this.getHeavyArmorPlates();
        if (heavyPlates > 0) {
            ItemStack heavyArmorPlates = new ItemStack(ModItems.HEAVY_ARMOR_PLATE.get(), heavyPlates);
            this.spawnAtLocation(heavyArmorPlates);
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Attacking", this.isAttacking());
        tag.putInt("AttackTimeout", this.getAttackTimeout());
        tag.putBoolean("Patrolling", this.isPatrolling());
        tag.putBoolean("Defensive", this.isDefensive());
        tag.putInt("ArmorPlates", this.getArmorPlates());
        tag.putInt("HeavyArmorPlates", this.getHeavyArmorPlates());
        this.getPatrolOrigin().ifPresent(pos -> {
            tag.putInt("PatrolOriginX", pos.getX());
            tag.putInt("PatrolOriginY", pos.getY());
            tag.putInt("PatrolOriginZ", pos.getZ());
        });
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAttacking(tag.getBoolean("Attacking"));
        this.setAttackTimeout(tag.getInt("AttackTimeout"));
        this.setPatrolling(tag.getBoolean("Patrolling"));
        if (tag.contains("Defensive")) {
            this.setDefensive(tag.getBoolean("Defensive"));
        }
        if (tag.contains("ArmorPlates")) {
            this.setArmorPlates(tag.getInt("ArmorPlates"));
        }
        if (tag.contains("HeavyArmorPlates")) {
            this.setHeavyArmorPlates(tag.getInt("HeavyArmorPlates"));
        }
        if (tag.contains("PatrolOriginX") && tag.contains("PatrolOriginY") && tag.contains("PatrolOriginZ")) {
            BlockPos patrolOrigin = new BlockPos(
                    tag.getInt("PatrolOriginX"),
                    tag.getInt("PatrolOriginY"),
                    tag.getInt("PatrolOriginZ")
            );
            this.setPatrolOrigin(patrolOrigin);
        }
    }

    @Override
    public void setTame(boolean tamed) {
        super.setTame(tamed);
        if (tamed) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0);
            this.setHealth(40.0F);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(24.0);
        }
        this.goalSelector.removeAllGoals(goal -> true);
        this.registerGoals();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }

    public static class ViventrumFollowOwnerGoal extends FollowOwnerGoal {
        private final ViventrumEntity viventrum;

        public ViventrumFollowOwnerGoal(ViventrumEntity viventrum, double speed, float minDistance, float maxDistance, boolean leavesAllowed) {
            super(viventrum, speed, minDistance, maxDistance, leavesAllowed);
            this.viventrum = viventrum;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !viventrum.isPatrolling();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !viventrum.isPatrolling();
        }

        @Override
        public void start() {
            if (!viventrum.isPatrolling()) {
                super.start();
            }
        }

        @Override
        public void tick() {
            if (!viventrum.isPatrolling()) {
                super.tick();
            }
        }
    }
}