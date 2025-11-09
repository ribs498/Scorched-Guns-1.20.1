package top.ribs.scguns.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.screen.SupplyScampMenuProvider;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModSounds;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class SupplyScampEntity extends TamableAnimal {
    private static final EntityDataAccessor<Boolean> PANICKING =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SITTING =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PATROLLING =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MASK_COLOR =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> PATROL_ORIGIN =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> WEARING_PUMPKIN =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PARTYING =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ARMOR_PLATES =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEAVY_ARMOR_PLATES =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.INT);

    private static final int MAX_ARMOR_PLATES = 4;
    private static final int PATROL_COOLDOWN = 15;
    private int patrolCooldownTimer = PATROL_COOLDOWN;

    private static final int ITEM_COOLDOWN = 12;
    private int itemCooldownTimer = ITEM_COOLDOWN;
    private BlockPos scheduledBarrelClose = null;
    private int barrelCloseTimer = 0;
    private static final int PATROL_RADIUS = 9;
    private static final int PATROL_MOVE_INTERVAL = 100;
    private static final int PATROL_DURATION = 80;
    private static final EntityDataAccessor<Boolean> STATIONARY =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double ITEM_DETECTION_RANGE = 9.0;
    private static final double ITEM_PICKUP_RANGE = 2.5;
    private int patrolTimer = 0;
    private BlockPos currentPatrolTarget = null;
    private static final int INVENTORY_SIZE = 27;

    public final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);

    public SupplyScampEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(true);
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
                effect == MobEffects.HARM
        ) {
            return false;
        }

        return super.canBeAffected(pPotionEffect);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.isAlive() && this.isTame()) {
            if (scheduledBarrelClose != null && barrelCloseTimer > 0) {
                barrelCloseTimer--;
                if (barrelCloseTimer <= 0) {
                    BlockState barrelState = this.level().getBlockState(scheduledBarrelClose);
                    if (barrelState.getBlock() instanceof BarrelBlock && barrelState.getValue(BarrelBlock.OPEN)) {
                        this.level().setBlock(scheduledBarrelClose, barrelState.setValue(BarrelBlock.OPEN, false), 3);
                        this.playSound(SoundEvents.BARREL_CLOSE, 0.5F, 1.0F);
                    }
                    scheduledBarrelClose = null;
                }
            }

            if (patrolCooldownTimer <= 0) {
                if (this.isPatrolling()) {
                    handlePatrolling();
                }
                patrolCooldownTimer = PATROL_COOLDOWN;
            } else {
                patrolCooldownTimer--;
            }

            if (itemCooldownTimer <= 0) {
                if (this.isPatrolling() || (!this.isOrderedToSit() && !this.isSitting())) {
                    checkForItems();
                }
                itemCooldownTimer = ITEM_COOLDOWN;
            } else {
                itemCooldownTimer--;
            }
        }
    }
    private void handlePatrolling() {
        Optional<BlockPos> patrolOrigin = this.getPatrolOrigin();
        if (!patrolOrigin.isPresent()) {
            return;
        }
        int totalItems = 0;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty()) {
                totalItems += stack.getCount();
            }
        }

        if (totalItems >= 3) {
            BlockPos nearestBarrel = findNearestBarrel();
            if (nearestBarrel != null) {
                double distanceToBarrel = this.distanceToSqr(Vec3.atCenterOf(nearestBarrel));

                if (distanceToBarrel <= 9.0D) {
                    BlockEntity blockEntity = this.level().getBlockEntity(nearestBarrel);
                    if (blockEntity instanceof Container container) {
                        depositAllItems(container);
                    }
                } else {
                    this.getNavigation().moveTo(nearestBarrel.getX() + 0.5, nearestBarrel.getY(), nearestBarrel.getZ() + 0.5, 1.0);
                    return;
                }
            }
        }

        if (totalItems < 25) {
            ItemEntity nearestItem = findNearestItem();
            if (nearestItem != null && this.distanceToSqr(nearestItem) <= ITEM_DETECTION_RANGE * ITEM_DETECTION_RANGE) {
                if (this.distanceToSqr(nearestItem) > ITEM_PICKUP_RANGE * ITEM_PICKUP_RANGE) {
                    this.getNavigation().moveTo(nearestItem, 1.0);
                    return;
                } else {
                    pickUpItem(nearestItem);
                }
            }
        }
        if (this.patrolTimer <= 0) {
            if (this.random.nextFloat() < 0.5) {
                this.currentPatrolTarget = patrolOrigin.get().offset(
                        this.random.nextInt(PATROL_RADIUS * 2) - PATROL_RADIUS,
                        0,
                        this.random.nextInt(PATROL_RADIUS * 2) - PATROL_RADIUS
                );
                this.getNavigation().moveTo(this.currentPatrolTarget.getX() + 0.5, this.currentPatrolTarget.getY(), this.currentPatrolTarget.getZ() + 0.5, 0.8);
                this.patrolTimer = PATROL_DURATION;
            } else {
                this.getNavigation().stop();
                this.currentPatrolTarget = null;
                this.patrolTimer = PATROL_MOVE_INTERVAL / 2;
            }
        } else {
            this.patrolTimer--;

            if (this.currentPatrolTarget != null && this.distanceToSqr(Vec3.atCenterOf(this.currentPatrolTarget)) < 4.0) {
                this.getNavigation().stop();
                this.currentPatrolTarget = null;
                this.patrolTimer = 20;
            }
        }

        if (this.distanceToSqr(Vec3.atCenterOf(patrolOrigin.get())) > (PATROL_RADIUS + 3) * (PATROL_RADIUS + 3)) {
            this.getNavigation().moveTo(patrolOrigin.get().getX() + 0.5, patrolOrigin.get().getY(), patrolOrigin.get().getZ() + 0.5, 1.0);
            this.currentPatrolTarget = null;
            this.patrolTimer = 40;
        }
    }
    private BlockPos findNearestBarrel() {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        BlockPos nearestBarrel = null;
        double nearestDistance = Double.MAX_VALUE;
        int searchRange = 16;

        for (int x = -searchRange; x <= searchRange; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -searchRange; z <= searchRange; z++) {
                    mutablePos.set(this.blockPosition().getX() + x, this.blockPosition().getY() + y, this.blockPosition().getZ() + z);
                    if (this.level().getBlockState(mutablePos).getBlock().toString().contains("barrel")) {
                        BlockEntity blockEntity = this.level().getBlockEntity(mutablePos);
                        if (blockEntity instanceof Container) {
                            double distance = this.distanceToSqr(Vec3.atCenterOf(mutablePos));
                            if (distance < nearestDistance) {
                                nearestDistance = distance;
                                nearestBarrel = mutablePos.immutable();
                            }
                        }
                    }
                }
            }
        }
        return nearestBarrel;
    }
    private void depositAllItems(Container container) {
        BlockPos barrelPos = null;
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos checkPos = this.blockPosition().offset(x, y, z);
                    BlockState blockState = this.level().getBlockState(checkPos);
                    BlockEntity blockEntity = this.level().getBlockEntity(checkPos);

                    if (blockState.getBlock() instanceof BarrelBlock && blockEntity == container) {
                        barrelPos = checkPos;
                        break;
                    }
                }
            }
        }
        if (barrelPos != null) {
            BlockState barrelState = this.level().getBlockState(barrelPos);
            if (barrelState.getBlock() instanceof BarrelBlock && !barrelState.getValue(BarrelBlock.OPEN)) {
                this.level().setBlock(barrelPos, barrelState.setValue(BarrelBlock.OPEN, true), 3);

                scheduleBarrelClose(barrelPos);
            }
        }

        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack itemStack = this.inventory.getItem(i);
            if (!itemStack.isEmpty()) {
                ItemStack remainingStack = tryAddItemToContainer(container, itemStack);
                this.inventory.setItem(i, remainingStack);
            }
        }

        this.playSound(SoundEvents.BARREL_OPEN, 0.5F, 1.0F);
    }
    private void scheduleBarrelClose(BlockPos barrelPos) {
        this.scheduledBarrelClose = barrelPos;
        this.barrelCloseTimer = 40;
    }
    private ItemStack tryAddItemToContainer(Container container, ItemStack itemStack) {
        for (int j = 0; j < container.getContainerSize(); j++) {
            ItemStack containerStack = container.getItem(j);
            if (containerStack.isEmpty()) {
                container.setItem(j, itemStack.copy());
                return ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameTags(containerStack, itemStack)) {
                int maxStackSize = containerStack.getMaxStackSize();
                int spaceInSlot = maxStackSize - containerStack.getCount();
                if (spaceInSlot > 0) {
                    int transferAmount = Math.min(itemStack.getCount(), spaceInSlot);
                    containerStack.grow(transferAmount);
                    itemStack.shrink(transferAmount);
                    if (itemStack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return itemStack;
    }

    private void checkForItems() {
        if (this.isPatrolling()) {
            return;
        }

        ItemEntity nearestItem = findNearestItem();
        if (nearestItem != null) {
            double distance = this.distanceToSqr(nearestItem);
            if (distance <= ITEM_PICKUP_RANGE * ITEM_PICKUP_RANGE) {
                if (this.inventory.canAddItem(nearestItem.getItem())) {
                    ItemStack remaining = this.inventory.addItem(nearestItem.getItem());
                    if (remaining.isEmpty()) {
                        nearestItem.discard();
                    } else {
                        nearestItem.setItem(remaining);
                    }
                    this.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }
            } else if (distance <= ITEM_DETECTION_RANGE * ITEM_DETECTION_RANGE) {
                this.getNavigation().moveTo(nearestItem, 1.0);
            }
        }
    }

    private ItemEntity findNearestItem() {
        List<ItemEntity> nearbyItems = this.level().getEntitiesOfClass(ItemEntity.class,
                this.getBoundingBox().inflate(ITEM_DETECTION_RANGE),
                item -> this.inventory.canAddItem(item.getItem()));

        return nearbyItems.stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    public void pickUpItem(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        ItemStack remaining = this.inventory.addItem(itemStack);
        if (remaining.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(remaining);
        }
        this.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
    }

    public void setStationary(boolean stationary) {
        this.entityData.set(STATIONARY, stationary);
    }

    public Optional<BlockPos> getPatrolOrigin() {
        return this.entityData.get(PATROL_ORIGIN);
    }

    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            if (source.getEntity() instanceof Player) {
                float baseChance = Config.COMMON.gameplay.cogBeaconSpawnChance.get().floatValue();
                float spawnChance = baseChance * 2.0f;
                if (baseChance > 0 && this.random.nextFloat() < spawnChance) {
                    SignalBeaconEntity beacon = new SignalBeaconEntity(ModEntities.SIGNAL_BEACON.get(), this.level());
                    beacon.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                    this.level().addFreshEntity(beacon);
                }
            }
        }
    }


    public boolean isPanicked() {
        return this.entityData.get(PANICKING);
    }


    public boolean isSitting() {
        return this.entityData.get(SITTING);
    }

    public void setSitting(boolean sitting) {
        this.entityData.set(SITTING, sitting);
    }

    public int getMaskColor() {
        return this.entityData.get(MASK_COLOR);
    }

    public void setMaskColor(int color) {
        this.entityData.set(MASK_COLOR, color);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 28D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0f)
                .add(Attributes.ARMOR, 6f);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SupplyScampPanicGoal(this, 2.5));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        if (this.isTame()) {
            this.goalSelector.addGoal(3, new SupplyScampFollowOwnerGoal(this, 1.3, 10.0F, 2.0F, false));
        } else {
            this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        }
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || itemstack.is(ModItems.ANCIENT_BRASS.get()) && !this.isTame();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            if (this.isTame()) {
                if (itemstack.is(ModItems.REPAIR_KIT.get()) && this.getHealth() < this.getMaxHealth()) {
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    float healAmount = 10.0F;
                    this.heal(healAmount);
                    this.playSound(SoundEvents.GENERIC_EAT, 0.5F, 1.0F);
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HEART,
                                this.getX(), this.getY() + 0.5, this.getZ(),
                                3, 0.3, 0.3, 0.3, 0.1);
                    }
                    return InteractionResult.SUCCESS;
                }
                if (itemstack.getItem() == Items.SHEARS && this.isWearingPumpkin()) {
                    if (!this.level().isClientSide) {
                        this.setWearingPumpkin(false);
                        this.gameEvent(GameEvent.SHEAR, player);
                        itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));

                        this.spawnAtLocation(Items.CARVED_PUMPKIN);
                        this.playSound(SoundEvents.PUMPKIN_CARVE, 1.0F, 1.0F);
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
                if ((itemstack.is(Items.CARVED_PUMPKIN) || itemstack.is(Items.JACK_O_LANTERN))
                        && !this.isWearingPumpkin()) {
                    if (!this.level().isClientSide) {
                        this.setWearingPumpkin(true);
                        if (!player.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }
                        this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
                // Dyeing
                if (itemstack.getItem() instanceof DyeItem) {
                    DyeColor dyeColor = ((DyeItem) itemstack.getItem()).getDyeColor();
                    this.setMaskColor(DYE_COLOR_TO_MASK_INDEX[dyeColor.getId()]);
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
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

                if (player.isShiftKeyDown() && itemstack.isEmpty()) {
                    if (this.isOrderedToSit()) {
                        this.setOrderedToSit(false);
                        this.setSitting(false);
                        this.setPatrolling(true);
                        this.setPatrolOrigin(this.blockPosition());
                        this.spawnPatrolOriginParticles();

                        BlockPos nearestBarrel = findNearestBarrel();
                        if (nearestBarrel == null) {
                            player.displayClientMessage(Component.translatable("message.supply_scamp.patrolling_no_barrel"), true);
                        } else {
                            player.displayClientMessage(Component.translatable("message.supply_scamp.patrolling"), true);
                        }

                    } else if (this.isPatrolling()) {
                        this.setPatrolling(false);
                        this.setOrderedToSit(false);
                        player.displayClientMessage(Component.translatable("message.supply_scamp.following"), true);
                    } else {
                        this.setOrderedToSit(true);
                        this.setSitting(true);
                        this.setPatrolling(false);
                        player.displayClientMessage(Component.translatable("message.supply_scamp.sitting"), true);
                    }
                } else {
                    player.openMenu(new SupplyScampMenuProvider(this));
                }
                return InteractionResult.SUCCESS;
            } else if (itemstack.is(ModItems.ANCIENT_BRASS.get())) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                if (this.random.nextInt(3) == 0 && !ForgeEventFactory.onAnimalTame(this, player)) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
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
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            return super.hurt(source, amount);
        }
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
        Objects.requireNonNull(this.getAttribute(Attributes.ARMOR)).setBaseValue(6.0 + totalArmor);
    }

    private static final int[] DYE_COLOR_TO_MASK_INDEX = new int[]{
            15, // WHITE
            14, // ORANGE
            13, // MAGENTA
            12, // LIGHT_BLUE
            11, // YELLOW
            10, // LIME
            9,  // PINK
            8,  // GRAY
            7,  // LIGHT_GRAY
            6,  // CYAN
            5,  // PURPLE
            4,  // BLUE
            3,  // BROWN
            2,  // GREEN
            1,  // RED
            0   // BLACK
    };

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PANICKING, false);
        this.entityData.define(SITTING, false);
        this.entityData.define(PATROLLING, false);
        this.entityData.define(MASK_COLOR, 0);
        this.entityData.define(PATROL_ORIGIN, Optional.empty());
        this.entityData.define(STATIONARY, false);
        this.entityData.define(WEARING_PUMPKIN, false);
        this.entityData.define(PARTYING, false);
        this.entityData.define(ARMOR_PLATES, 0);
        this.entityData.define(HEAVY_ARMOR_PLATES, 0);
    }
    public boolean isPartying() {
        return this.entityData.get(PARTYING);
    }

    public void setPartying(boolean partying) {
        this.entityData.set(PARTYING, partying);
    }
    @Override
    public void setRecordPlayingNearby(@NotNull BlockPos pos, boolean playing) {
        this.setPartying(playing && this.isTame());
    }
    public boolean isWearingPumpkin() {
        return this.entityData.get(WEARING_PUMPKIN);
    }

    public void setWearingPumpkin(boolean wearing) {
        this.entityData.set(WEARING_PUMPKIN, wearing);
    }

    private static boolean isHalloweenSeason() {
        LocalDate date = LocalDate.now();
        return date.getMonth() == Month.OCTOBER;
    }
    @Override
    protected void updateWalkAnimation(float partialTick) {
        float f = (this.getPose() == Pose.STANDING) ? Math.min(partialTick * 6F, 1f) : 0f;
        this.walkAnimation.update(f, 0.2f);
    }
    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return ModSounds.SCAMP_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SCAMP_DIE.get();
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        if (reason == RemovalReason.KILLED) {
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                ItemStack itemStack = this.inventory.getItem(i);
                if (!itemStack.isEmpty()) {
                    this.spawnAtLocation(itemStack);
                }
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
        super.remove(reason);
    }

    @Override
    public void setPersistenceRequired() {
        super.setPersistenceRequired();
    }
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
                                        @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);

        if (isHalloweenSeason() && this.random.nextFloat() < 0.85f) {
            this.setWearingPumpkin(true);
        }

        return spawnData;
    }
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        ListTag listnbt = new ListTag();

        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty()) {
                CompoundTag compoundnbt = new CompoundTag();
                compoundnbt.putByte("Slot", (byte) i);
                itemstack.save(compoundnbt);
                listnbt.add(compoundnbt);
            }
        }

        compound.putBoolean("Patrolling", this.isPatrolling());
        this.getPatrolOrigin().ifPresent(pos -> {
            compound.putInt("PatrolOriginX", pos.getX());
            compound.putInt("PatrolOriginY", pos.getY());
            compound.putInt("PatrolOriginZ", pos.getZ());
        });
        compound.put("Items", listnbt);
        compound.putInt("MaskColor", this.getMaskColor());
        compound.putBoolean("WearingPumpkin", this.isWearingPumpkin());
        compound.putInt("ArmorPlates", this.getArmorPlates());
        compound.putInt("HeavyArmorPlates", this.getHeavyArmorPlates());

        if (scheduledBarrelClose != null) {
            compound.putInt("BarrelCloseX", scheduledBarrelClose.getX());
            compound.putInt("BarrelCloseY", scheduledBarrelClose.getY());
            compound.putInt("BarrelCloseZ", scheduledBarrelClose.getZ());
            compound.putInt("BarrelCloseTimer", barrelCloseTimer);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        ListTag listnbt = compound.getList("Items", 10);

        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundTag compoundnbt = listnbt.getCompound(i);
            int j = compoundnbt.getByte("Slot") & 255;
            if (j < this.inventory.getContainerSize()) {
                this.inventory.setItem(j, ItemStack.of(compoundnbt));
            }
        }

        this.setPatrolling(compound.getBoolean("Patrolling"));
        if (compound.contains("PatrolOriginX") && compound.contains("PatrolOriginY") && compound.contains("PatrolOriginZ")) {
            BlockPos patrolOrigin = new BlockPos(
                    compound.getInt("PatrolOriginX"),
                    compound.getInt("PatrolOriginY"),
                    compound.getInt("PatrolOriginZ")
            );
            this.setPatrolOrigin(patrolOrigin);
        }
        if (compound.contains("MaskColor", 3)) {
            this.setMaskColor(compound.getInt("MaskColor"));
        }
        if (compound.contains("WearingPumpkin")) {
            this.setWearingPumpkin(compound.getBoolean("WearingPumpkin"));
        }
        if (compound.contains("ArmorPlates")) {
            this.setArmorPlates(compound.getInt("ArmorPlates"));
        }
        if (compound.contains("HeavyArmorPlates")) {
            this.setHeavyArmorPlates(compound.getInt("HeavyArmorPlates"));
        }
        // Load barrel closing state
        if (compound.contains("BarrelCloseX")) {
            scheduledBarrelClose = new BlockPos(
                    compound.getInt("BarrelCloseX"),
                    compound.getInt("BarrelCloseY"),
                    compound.getInt("BarrelCloseZ")
            );
            barrelCloseTimer = compound.getInt("BarrelCloseTimer");
        }
    }

    public void setPatrolOrigin(BlockPos pos) {
        if (pos != null) {
            this.entityData.set(PATROL_ORIGIN, Optional.of(pos));
        } else {
            this.entityData.set(PATROL_ORIGIN, Optional.empty());
        }
    }


    public void spawnPatrolOriginParticles() {
        if (this.level() instanceof ServerLevel) {
            BlockPos pos = this.getPatrolOrigin().orElse(this.blockPosition());
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    10, 0.5, 0.5, 0.5, 0.1);
        }
    }

    public boolean isPatrolling() {
        return this.entityData.get(PATROLLING);
    }

    public void setPatrolling(boolean patrolling) {
        this.entityData.set(PATROLLING, patrolling);
    }

    @Override
    public void setTame(boolean tamed) {
        super.setTame(tamed);
        if (tamed) {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(42.0);
            this.setHealth(42.0F);
        } else {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(24.0);
        }
        this.goalSelector.removeAllGoals(goal -> true);
        this.registerGoals();
    }

    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.ANCIENT_BRASS.get());
    }

    public boolean canBeLeashed(@NotNull Player player) {
        return this.isTame() && super.canBeLeashed(player);
    }

    public static class SupplyScampPanicGoal extends PanicGoal {
        private final SupplyScampEntity scamp;

        public SupplyScampPanicGoal(SupplyScampEntity scamp, double speedModifier) {
            super(scamp, speedModifier);
            this.scamp = scamp;
        }

        @Override
        public boolean canUse() {
            if (this.scamp.isTame() && this.scamp.getLastHurtByMob() instanceof Player) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public void start() {
            this.scamp.setPanicking(true);
            super.start();
        }

        @Override
        public void stop() {
            this.scamp.setPanicking(false);
            super.stop();
        }
    }

    private void setPanicking(boolean b) {
        this.entityData.set(PANICKING, b);
    }

    public static class SupplyScampFollowOwnerGoal extends FollowOwnerGoal {
        private final SupplyScampEntity scamp;

        public SupplyScampFollowOwnerGoal(SupplyScampEntity scamp, double speed, float minDistance, float maxDistance, boolean leavesAllowed) {
            super(scamp, speed, minDistance, maxDistance, leavesAllowed);
            this.scamp = scamp;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !scamp.isPatrolling();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !scamp.isPatrolling();
        }

        @Override
        public void start() {
            if (!scamp.isPatrolling()) {
                super.start();
            }
        }

        @Override
        public void tick() {
            if (!scamp.isPatrolling()) {
                super.tick();
            }
        }
    }

}