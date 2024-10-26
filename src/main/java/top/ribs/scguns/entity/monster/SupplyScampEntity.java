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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.client.screen.SupplyScampMenuProvider;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.ScampControllerItem;

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
        private static final EntityDataAccessor<Boolean> LINKED_TO_CONTROLLER =
                SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.BOOLEAN);
        private static final EntityDataAccessor<Optional<BlockPos>> LINKED_CONTAINER =
                SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

        private static final int PATROL_COOLDOWN = 30;
        private int patrolCooldownTimer = PATROL_COOLDOWN;

        private static final int ITEM_COOLDOWN = 20;
        private int itemCooldownTimer = ITEM_COOLDOWN;

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
        private boolean inventoryFullCached = false;
        private boolean hasItemsToDepositCached = false;
        private static final int ANIMATION_UPDATE_INTERVAL = 5;
        private int animationUpdateTimer = ANIMATION_UPDATE_INTERVAL;

        public SupplyScampEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
            super(entityType, level);
            this.setCanPickUpLoot(true);
        }

        public final AnimationState idleAnimationState = new AnimationState();
        public final AnimationState panicAnimationState = new AnimationState();
        public final AnimationState sitAnimationState = new AnimationState();
        public int panicAnimationTimeout = 0;
        public int idleAnimationTimeout = 0;
        private BlockPos cachedNearestContainer = null;
        private int containerSearchCooldown = 100; // Cooldown for search
        @Override
        public void tick() {
            super.tick();

            if (!this.level().isClientSide && this.isAlive() && this.isTame()) {
                if (patrolCooldownTimer <= 0) {
                    if (this.isPatrolling()) {
                        handlePatrolling();
                    }
                    patrolCooldownTimer = PATROL_COOLDOWN;
                } else {
                    patrolCooldownTimer--;
                }

                if (itemCooldownTimer <= 0) {
                    checkForItems();
                    itemCooldownTimer = ITEM_COOLDOWN;
                } else {
                    itemCooldownTimer--;
                }

                Optional<BlockPos> containerPosOpt = this.getLinkedContainer();
                if (containerPosOpt.isPresent() && this.distanceToSqr(Vec3.atCenterOf(containerPosOpt.get())) < 8.0D) {
                    BlockEntity blockEntity = this.level().getBlockEntity(containerPosOpt.get());
                    if (blockEntity instanceof Container container) {
                        depositItems(container);
                    }
                }
            }

            if (this.level().isClientSide) {
                setupAnimationStates();
            }
        }

        private boolean isInventoryFull() {
            if (inventoryFullCached) {
                return true;
            }
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                if (this.inventory.getItem(i).isEmpty()) {
                    inventoryFullCached = false;
                    return false;
                }
            }
            inventoryFullCached = true;
            return true;
        }

        private boolean hasItemsToDeposit() {
            if (hasItemsToDepositCached) {
                return true;
            }
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                if (!this.inventory.getItem(i).isEmpty()) {
                    hasItemsToDepositCached = true;
                    return true;
                }
            }
            hasItemsToDepositCached = false;
            return false;
        }

        private void handlePatrolling() {
            Optional<BlockPos> patrolOrigin = this.getPatrolOrigin();
            Optional<BlockPos> linkedContainerPos = this.getLinkedContainer();
            if (patrolOrigin.isPresent()) {
                boolean hasItemsToDeposit = hasItemsToDeposit();
                boolean inventoryFull = isInventoryFull();
                if (hasItemsToDeposit) {
                    BlockPos targetContainer = null;
                    targetContainer = linkedContainerPos.orElseGet(this::findNearestContainer);

                    if (targetContainer != null) {
                        double distanceToContainer = this.distanceToSqr(Vec3.atCenterOf(targetContainer));

                        if (distanceToContainer <= 2.0D) {
                            BlockEntity blockEntity = this.level().getBlockEntity(targetContainer);
                            if (blockEntity instanceof Container container) {
                                depositItems(container);
                            }
                        } else if (inventoryFull || distanceToContainer <= 15.0) {
                            this.getNavigation().moveTo(targetContainer.getX() + 0.5, targetContainer.getY(), targetContainer.getZ() + 0.5, 1.0);
                            this.setStationary(false);
                            return;
                        }
                    }
                }
                if (!inventoryFull) {
                    ItemEntity nearestItem = findNearestItem();
                    if (nearestItem != null && this.distanceToSqr(nearestItem) <= ITEM_DETECTION_RANGE * ITEM_DETECTION_RANGE) {
                        if (this.distanceToSqr(nearestItem) > ITEM_PICKUP_RANGE * ITEM_PICKUP_RANGE) {
                            this.getNavigation().moveTo(nearestItem, 1.0);
                            this.setStationary(false);
                            return;
                        } else {
                            pickUpItem(nearestItem);
                        }
                    }
                }
                if (this.patrolTimer <= 0) {
                    if (this.random.nextFloat() < 0.45) {
                        this.currentPatrolTarget = patrolOrigin.get().offset(
                                this.random.nextInt(PATROL_RADIUS * 2) - PATROL_RADIUS,
                                0,
                                this.random.nextInt(PATROL_RADIUS * 2) - PATROL_RADIUS
                        );
                        this.getNavigation().moveTo(this.currentPatrolTarget.getX(), this.currentPatrolTarget.getY(), this.currentPatrolTarget.getZ(), 1.0);
                        this.setStationary(false);
                        this.patrolTimer = PATROL_DURATION;
                    } else {
                        this.getNavigation().stop();
                        this.setStationary(true);
                        this.currentPatrolTarget = null;
                        this.patrolTimer = PATROL_MOVE_INTERVAL;
                    }
                } else {
                    this.patrolTimer--;
                    if (this.currentPatrolTarget != null && this.distanceToSqr(Vec3.atCenterOf(this.currentPatrolTarget)) < 1.0) {
                        this.getNavigation().stop();
                        this.setStationary(true);
                        this.currentPatrolTarget = null;
                        this.patrolTimer = PATROL_MOVE_INTERVAL;
                    }
                }
                if (this.distanceToSqr(Vec3.atCenterOf(patrolOrigin.get())) > PATROL_RADIUS * PATROL_RADIUS) {
                    this.getNavigation().moveTo(patrolOrigin.get().getX(), patrolOrigin.get().getY(), patrolOrigin.get().getZ(), 1.0);
                    this.setStationary(false);
                    this.currentPatrolTarget = null;
                    this.patrolTimer = PATROL_MOVE_INTERVAL;
                }
            }
        }
        private void depositItems(Container container) {
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                ItemStack itemStack = this.inventory.getItem(i);
                if (!itemStack.isEmpty()) {
                    ItemStack remainingStack = tryAddItemToContainer(container, itemStack);
                    this.inventory.setItem(i, remainingStack);
                    if (remainingStack.isEmpty()) {                        inventoryFullCached = false;
                        hasItemsToDepositCached = false;
                    }
                }
            }
        }

        private BlockPos findNearestContainer() {
            if (containerSearchCooldown > 0 && cachedNearestContainer != null) {
                containerSearchCooldown--;
                return cachedNearestContainer;
            }

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            BlockPos nearestContainer = null;
            double nearestDistance = Double.MAX_VALUE;

            for (int x = -10; x <= 10; x++) {
                for (int y = -5; y <= 5; y++) {
                    for (int z = -10; z <= 10; z++) {
                        mutablePos.set(this.blockPosition().getX() + x, this.blockPosition().getY() + y, this.blockPosition().getZ() + z);
                        BlockEntity blockEntity = this.level().getBlockEntity(mutablePos);
                        if (blockEntity instanceof Container) {
                            double distance = this.distanceToSqr(Vec3.atCenterOf(mutablePos));
                            if (distance < nearestDistance) {
                                nearestDistance = distance;
                                nearestContainer = mutablePos.immutable();
                            }
                        }
                    }
                }
            }

            cachedNearestContainer = nearestContainer;
            containerSearchCooldown = 100; // Reset cooldown
            return nearestContainer;
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
                } else {
                    this.getNavigation().moveTo(nearestItem, 1.0);
                    this.setStationary(false);
                    this.patrolTimer = PATROL_DURATION;
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
        public void setLinkedContainer(BlockPos pos) {
            this.entityData.set(LINKED_CONTAINER, Optional.of(pos));
        }

        public Optional<BlockPos> getLinkedContainer() {
            return this.entityData.get(LINKED_CONTAINER);
        }
        public Optional<BlockPos> getPatrolOrigin() {
            return this.entityData.get(PATROL_ORIGIN);
        }
        private void setupAnimationStates() {
            if (animationUpdateTimer > 0) {
                animationUpdateTimer--;
                return;
            }
            animationUpdateTimer = ANIMATION_UPDATE_INTERVAL;

            if (this.isSitting()) {
                if (!sitAnimationState.isStarted()) {
                    sitAnimationState.start(this.tickCount);
                }
                sitAnimationState.updateTime(this.tickCount, 1.0f);
                panicAnimationState.stop();
                idleAnimationState.stop();
                return;
            } else {
                sitAnimationState.stop();
            }

            if (this.isPanicked()) {
                if (panicAnimationTimeout <= 0) {
                    panicAnimationTimeout = 50;
                    panicAnimationState.start(this.tickCount);
                }
                panicAnimationState.updateTime(this.tickCount, 1.0f);
                idleAnimationState.stop();
                --panicAnimationTimeout;
            } else {
                panicAnimationState.stop();
            }

            if (!this.isSitting() && !this.isPanicked()) {
                if (idleAnimationTimeout <= 0) {
                    idleAnimationTimeout = 60;
                    idleAnimationState.start(this.tickCount);
                }
                idleAnimationState.updateTime(this.tickCount, 1.0f);
                --idleAnimationTimeout;
            } else {
                idleAnimationState.stop();
            }
        }

        public boolean canBreatheUnderwater() {
            return true;
        }
        @Override
        public void die(DamageSource pCause) {
            super.die(pCause);
            if (this.isLinkedToController()) {
                this.setLinkedToController(false);
            }
        }
        public boolean isLinkedToController() {
            return this.entityData.get(LINKED_TO_CONTROLLER);
        }

        public void setLinkedToController(boolean linked) {
            this.entityData.set(LINKED_TO_CONTROLLER, linked);
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
                    .add(Attributes.MAX_HEALTH, 24D)
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
        public InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
            ItemStack itemstack = player.getItemInHand(hand);
            if (this.level().isClientSide) {
                boolean flag = this.isOwnedBy(player) || this.isTame() || itemstack.is(ModItems.ANCIENT_BRASS.get()) && !this.isTame();
                return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
            } else {
                if (this.isTame()) {
                    if (itemstack.getItem() instanceof DyeItem) {
                        DyeColor dyeColor = ((DyeItem) itemstack.getItem()).getDyeColor();
                        this.setMaskColor(DYE_COLOR_TO_MASK_INDEX[dyeColor.getId()]);
                        if (!player.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }
                        return InteractionResult.SUCCESS;
                    }
                    if (player.isShiftKeyDown()) {
                        if (itemstack.getItem() instanceof ScampControllerItem scampControllerItem) {
                            UUID currentLinkedUUID = scampControllerItem.getLinkedScampUUID(itemstack);
                            UUID thisScampUUID = this.getUUID();
                            if (currentLinkedUUID == null || !currentLinkedUUID.equals(thisScampUUID)) {
                                scampControllerItem.linkScamp(itemstack, this);
                                player.displayClientMessage(Component.literal("Scamp linked to controller."), true);
                                if (currentLinkedUUID != null && !currentLinkedUUID.equals(thisScampUUID)) {
                                    scampControllerItem.unlinkScamp(this.level(), currentLinkedUUID);
                                    player.displayClientMessage(Component.literal("Unlinked previous scamp."), true);
                                }
                                return InteractionResult.SUCCESS;
                            }
                        }
                        if (this.isOrderedToSit()) {
                            setScampPatrolling(player);
                        } else if (this.isPatrolling()) {
                            setScampFollowing(player);
                        } else {
                            setScampSitting(player);
                        }
                    } else {
                        player.openMenu(new SupplyScampMenuProvider(this));
                    }
                    return InteractionResult.SUCCESS;
                } else if (itemstack.is(ModItems.ANCIENT_BRASS.get())) {
                    // Handle taming logic
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



        private void setScampPatrolling(Player player) {
            this.setOrderedToSit(false);
            this.setSitting(false);
            this.setPatrolling(true);
            this.setPatrolOrigin(this.blockPosition());
            this.spawnPatrolOriginParticles();
            player.displayClientMessage(Component.translatable("message.supply_scamp.patrolling"), true);
        }

        private void setScampFollowing(Player player) {
            this.setPatrolling(false);
            this.setOrderedToSit(false);
            this.getNavigation().moveTo(player, 1.0);
            player.displayClientMessage(Component.translatable("message.supply_scamp.following"), true);
        }

        private void setScampSitting(Player player) {
            this.setOrderedToSit(true);
            this.setSitting(true);
            player.displayClientMessage(Component.translatable("message.supply_scamp.sitting"), true);
        }

        @Override
        public boolean hurt(DamageSource source, float amount) {
            if (this.isInvulnerableTo(source)) {
                return false;
            } else {
                return super.hurt(source, amount);
            }
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
            this.entityData.define(MASK_COLOR, 0); // Default to black
            this.entityData.define(PATROL_ORIGIN, Optional.empty());
            this.entityData.define(STATIONARY, false);
            this.entityData.define(LINKED_TO_CONTROLLER, false);
            this.entityData.define(LINKED_CONTAINER, Optional.empty());

        }

        @Override
        protected void updateWalkAnimation(float partialTick) {
            float f = (this.getPose() == Pose.STANDING) ? Math.min(partialTick * 6F, 1f) : 0f;
            this.walkAnimation.update(f, 0.2f);
        }

        @Nullable
        @Override
        protected SoundEvent getAmbientSound() {
            return SoundEvents.IRON_GOLEM_STEP;
        }

        @Nullable
        @Override
        protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
            return SoundEvents.IRON_GOLEM_HURT;
        }

        @Nullable
        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.IRON_GOLEM_DEATH;
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
            }
            super.remove(reason);
        }

        @Override
        public void setPersistenceRequired() {
            super.setPersistenceRequired();
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
            this.getLinkedContainer().ifPresent(pos -> {
                compound.putInt("ContainerPosX", pos.getX());
                compound.putInt("ContainerPosY", pos.getY());
                compound.putInt("ContainerPosZ", pos.getZ());
            });
            compound.putBoolean("Patrolling", this.isPatrolling());
            this.getPatrolOrigin().ifPresent(pos -> {
                compound.putInt("PatrolOriginX", pos.getX());
                compound.putInt("PatrolOriginY", pos.getY());
                compound.putInt("PatrolOriginZ", pos.getZ());
            });
            compound.put("Items", listnbt);
            compound.putInt("MaskColor", this.getMaskColor());
            compound.putBoolean("LinkedToController", this.isLinkedToController());
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
            if (compound.contains("ContainerPosX")) {
                BlockPos containerPos = new BlockPos(
                        compound.getInt("ContainerPosX"),
                        compound.getInt("ContainerPosY"),
                        compound.getInt("ContainerPosZ")
                );
                this.setLinkedContainer(containerPos);
            }
            this.setLinkedToController(compound.getBoolean("LinkedToController"));
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
                Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(36.0);
                this.setHealth(36.0F);
            } else {
                Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(8.0);
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
