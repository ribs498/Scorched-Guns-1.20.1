package top.ribs.scguns.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.client.screen.SupplyScampMenuProvider;
import top.ribs.scguns.init.ModItems;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class SupplyScampEntity extends TamableAnimal {
    private static final EntityDataAccessor<Boolean> PANICKING =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SITTING =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MASK_COLOR =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.INT);

    private static final int INVENTORY_SIZE = 27;

    public final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);

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

    public SupplyScampEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(true);
    }

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState panicAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public int panicAnimationTimeout = 0;
    public int idleAnimationTimeout = 0;

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            setupAnimationStates();
        } else if (this.isAlive()) {
            pickUpNearbyItems();
        }
    }

    private void setupAnimationStates() {
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

    private void pickUpNearbyItems() {
        if (this.inventory.canAddItem(ItemStack.EMPTY)) {
            List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(5.0D));
            for (ItemEntity itemEntity : items) {
                if (itemEntity.isAlive() && !itemEntity.hasPickUpDelay() && !itemEntity.getItem().isEmpty() && this.canPickUpLoot()) {
                    ItemStack itemStack = itemEntity.getItem();
                    ItemStack remaining = this.inventory.addItem(itemStack);
                    if (remaining.isEmpty()) {
                        itemEntity.discard();
                        this.setPersistenceRequired();
                    } else {
                        itemEntity.setItem(remaining);
                    }
                }
            }
        }
    }
    public boolean canBreatheUnderwater() {
        return true;
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

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            return super.hurt(source, amount);
        }
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SupplyScampPanicGoal(this, 2.5));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        if (this.isTame()) {
            this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F, false));
        } else {
            this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        }
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

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
        this.entityData.define(MASK_COLOR, 0); // Default to black
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
    public InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || itemstack.is(ModItems.ANCIENT_BRASS.get()) && !this.isTame();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            if (player.isShiftKeyDown() && itemstack.getItem() instanceof DyeItem) {
                // Handle dyeing mask
                DyeColor dyeColor = ((DyeItem) itemstack.getItem()).getDyeColor();
                this.setMaskColor(DYE_COLOR_TO_MASK_INDEX[dyeColor.getId()]);
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            } else if (player.isShiftKeyDown() && this.isTame()) {
                // Open inventory GUI if shift-right-clicked without dye and only if tamed
                player.openMenu(new SupplyScampMenuProvider(this));
                return InteractionResult.SUCCESS;
            } else {
                if (this.isTame()) {
                    if (itemstack.is(ModItems.ANCIENT_BRASS.get())) {
                        // Feed the SupplyScampEntity
                        if (this.getHealth() < this.getMaxHealth()) {
                            this.heal(4.0F); // Heal amount can be adjusted as needed
                            if (!player.getAbilities().instabuild) {
                                itemstack.shrink(1);
                            }
                            this.gameEvent(GameEvent.EAT, this);
                            return InteractionResult.SUCCESS;
                        } else {
                            return InteractionResult.PASS; // Do nothing if health is full
                        }
                    } else if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                        this.heal((float) itemstack.getFoodProperties(this).getNutrition());
                        if (!player.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }
                        this.gameEvent(GameEvent.EAT, this);
                        return InteractionResult.SUCCESS;
                    } else {
                        InteractionResult interactionresult = super.mobInteract(player, hand);
                        if (!interactionresult.consumesAction() && this.isOwnedBy(player)) {
                            this.setOrderedToSit(!this.isOrderedToSit());
                            this.setSitting(this.isOrderedToSit());
                            this.jumping = false;
                            this.navigation.stop();
                            this.setTarget(null);
                            return InteractionResult.SUCCESS;
                        } else {
                            return interactionresult;
                        }
                    }
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
                } else {
                    return super.mobInteract(player, hand);
                }
            }
        }
    }


    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
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

        compound.put("Items", listnbt);
        compound.putInt("MaskColor", this.getMaskColor());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        ListTag listnbt = compound.getList("Items", 10);

        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundTag compoundnbt = listnbt.getCompound(i);
            int j = compoundnbt.getByte("Slot") & 255;
            if (j >= 0 && j < this.inventory.getContainerSize()) {
                this.inventory.setItem(j, ItemStack.of(compoundnbt));
            }
        }

        if (compound.contains("MaskColor", 3)) {
            this.setMaskColor(compound.getInt("MaskColor"));
        }
    }

    @Override
    public void setTame(boolean tamed) {
        super.setTame(tamed);
        if (tamed) {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(24.0);
            this.setHealth(24.0F);
        } else {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(8.0);
        }
        this.goalSelector.removeAllGoals(goal -> true);
        this.registerGoals();
    }

    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BONE);
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
}
