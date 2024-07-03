package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.block.MechanicalPressBlock;
import top.ribs.scguns.client.screen.MechanicalPressMenu;
import top.ribs.scguns.client.screen.MechanicalPressRecipe;
import top.ribs.scguns.init.ModBlockEntities;
import top.ribs.scguns.item.MoldItem;

import java.util.Optional;

public class MechanicalPressBlockEntity extends BlockEntity implements MenuProvider {

    public final ItemStackHandler itemHandler = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            assert level != null;
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                if (isInputSlot(slot) || isMoldSlot(slot)) {
                    if (!isRecipeValid()) {
                        resetProgress();
                    }
                }
            }
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // Handle mold items insertion
            if (stack.getItem() instanceof MoldItem) {
                ItemStack moldStack = itemHandler.getStackInSlot(MOLD_SLOT);
                if (moldStack.isEmpty() || (moldStack.isDamageableItem() && moldStack.getDamageValue() < moldStack.getMaxDamage())) {
                    return super.insertItem(MOLD_SLOT, stack, simulate);
                }
            }

            // Proceed with usual insertion logic
            return super.insertItem(slot, stack, simulate);
        }
    };

    private boolean isInputSlot(int slot) {
        return slot >= FIRST_INPUT_SLOT && slot <= LAST_INPUT_SLOT;
    }

    private boolean isMoldSlot(int slot) {
        return slot == MOLD_SLOT;
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ContainerData data;
    private int progress = 0;
    private int maxProgress = 100;
    private int burnTime = 0;
    private int maxBurnTime = 0;
    public static final int FIRST_INPUT_SLOT = 0;
    public static final int LAST_INPUT_SLOT = 2;
    public static final int MOLD_SLOT = 3;
    public static final int FUEL_SLOT = 4;
    public static final int OUTPUT_SLOT = 5;
    private float pressPosition = 0.0f;
    private final float pressSpeed = 0.04f;
    private boolean movingDown = true;

    public MechanicalPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MECHANICAL_PRESS.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                switch (index) {
                    case 0: return progress;
                    case 1: return maxProgress;
                    case 2: return burnTime;
                    case 3: return maxBurnTime;
                    default: return 0;
                }
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0: progress = value; break;
                    case 1: maxProgress = value; break;
                    case 2: burnTime = value; break;
                    case 3: maxBurnTime = value; break;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.mechanical_press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new MechanicalPressMenu(id, inv, this, this.data);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return lazyItemHandler.cast();
            } else if (side == Direction.DOWN) {
                return LazyOptional.of(() -> new OutputItemHandler(itemHandler)).cast();
            } else if (side == Direction.UP) {
                return LazyOptional.of(() -> new TopItemHandler(itemHandler)).cast();
            } else {
                return LazyOptional.of(() -> new FuelItemHandler(itemHandler)).cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("mechanical_press.progress", progress);
        tag.putInt("mechanical_press.burnTime", burnTime);
        tag.putInt("mechanical_press.maxBurnTime", maxBurnTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("mechanical_press.progress");
        burnTime = tag.getInt("mechanical_press.burnTime");
        maxBurnTime = tag.getInt("mechanical_press.maxBurnTime");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
    public static void tick(Level level, BlockPos pos, BlockState state, MechanicalPressBlockEntity blockEntity) {
        boolean wasLit = state.getValue(MechanicalPressBlock.LIT);
        boolean isLit = false;

        if (!level.isClientSide) {
            boolean hasValidRecipe = blockEntity.hasRecipe();
            boolean canOutput = blockEntity.canOutput();

            if (blockEntity.hasFuel()) {
                blockEntity.burnTime--;
                isLit = true;
            } else if (hasValidRecipe && blockEntity.canBurnFuel() && canOutput) {
                blockEntity.burnFuel();
                isLit = true;
            } else {
                if (blockEntity.progress > 0) {
                    blockEntity.resetProgress();
                }
            }

            if (hasValidRecipe && blockEntity.hasFuel() && canOutput) {
                blockEntity.progress++;
                if (blockEntity.progress >= blockEntity.maxProgress) {
                    blockEntity.craftItem();
                    blockEntity.resetProgress();
                }
            } else if (!hasValidRecipe || !canOutput) {
                blockEntity.resetProgress();
            }

            if (wasLit != isLit) {
                level.setBlock(pos, state.setValue(MechanicalPressBlock.LIT, isLit), 3);
            }
        }

        if (state.getValue(MechanicalPressBlock.LIT)) {
            blockEntity.updatePressPosition();
        }
    }
    private boolean canOutput() {
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        SimpleContainer inventory = new SimpleContainer(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 2); // Add one for the mold slot
        for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            inventory.setItem(i - FIRST_INPUT_SLOT, itemHandler.getStackInSlot(i));
        }
        inventory.setItem(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 1, itemHandler.getStackInSlot(MOLD_SLOT)); // Add mold slot
        Optional<MechanicalPressRecipe> match = level.getRecipeManager()
                .getRecipeFor(MechanicalPressRecipe.Type.INSTANCE, inventory, level);

        if (match.isPresent()) {
            ItemStack resultItem = match.get().getResultItem(level.registryAccess());
            if (outputStack.isEmpty() || (outputStack.getItem() == resultItem.getItem() && outputStack.getCount() + resultItem.getCount() <= outputStack.getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }


    public float getPressPosition(float partialTicks, boolean isLit) {
        if (isLit) {
            return pressPosition + (movingDown ? -pressSpeed : pressSpeed) * partialTicks;
        } else {
            return pressPosition;
        }
    }


    public void updatePressPosition() {
        if (movingDown) {
            pressPosition -= pressSpeed;
            float endPosition = -0.25f;
            if (pressPosition <= endPosition) {
                movingDown = false;
                if (level != null) {
                    level.playSound(null, worldPosition, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.2f, 0.60f);
                }
            }
        } else {
            pressPosition += pressSpeed;
            float startPosition = 0.0f;
            if (pressPosition >= startPosition) {
                movingDown = true;
            }
        }
    }
    private boolean hasRecipe() {
        SimpleContainer inventory = new SimpleContainer(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 2);
        for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            inventory.setItem(i - FIRST_INPUT_SLOT, itemHandler.getStackInSlot(i));
        }
        inventory.setItem(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 1, itemHandler.getStackInSlot(MOLD_SLOT));
        assert level != null;
        Optional<MechanicalPressRecipe> match = level.getRecipeManager()
                .getRecipeFor(MechanicalPressRecipe.Type.INSTANCE, inventory, level);

        if (match.isPresent()) {
            MechanicalPressRecipe recipe = match.get();
            this.maxProgress = recipe.getProcessingTime();
            return true;
        }
        return false;
    }
    private void craftItem() {
        SimpleContainer inventory = new SimpleContainer(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 2); // Add one for the mold slot
        for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            inventory.setItem(i - FIRST_INPUT_SLOT, itemHandler.getStackInSlot(i));
        }
        inventory.setItem(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 1, itemHandler.getStackInSlot(MOLD_SLOT)); // Add mold slot
        assert level != null;
        Optional<MechanicalPressRecipe> match = level.getRecipeManager()
                .getRecipeFor(MechanicalPressRecipe.Type.INSTANCE, inventory, level);
        if (match.isPresent()) {
            MechanicalPressRecipe recipe = match.get();
            ItemStack resultItem = recipe.getResultItem(level.registryAccess());
            ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
            if (outputStack.isEmpty() || (outputStack.getItem() == resultItem.getItem() && outputStack.getCount() + resultItem.getCount() <= outputStack.getMaxStackSize())) {
                // Consume only the required ingredients
                for (Ingredient ingredient : recipe.getIngredients()) {
                    for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
                        if (ingredient.test(itemHandler.getStackInSlot(i))) {
                            itemHandler.extractItem(i, 1, false);
                            break;
                        }
                    }
                }

                // Reduce mold durability
                ItemStack moldStack = itemHandler.getStackInSlot(MOLD_SLOT);
                if (!moldStack.isEmpty() && moldStack.isDamageableItem()) {
                    int newDamage = moldStack.getDamageValue() + 1;
                    if (newDamage >= moldStack.getMaxDamage()) {
                        moldStack.shrink(1); // Remove the item if it reaches max damage
                    } else {
                        moldStack.setDamageValue(newDamage); // Otherwise, set the new damage value
                    }
                    itemHandler.setStackInSlot(MOLD_SLOT, moldStack);
                }

                if (outputStack.isEmpty()) {
                    itemHandler.setStackInSlot(OUTPUT_SLOT, resultItem.copy());
                } else {
                    outputStack.grow(resultItem.getCount());
                }
            }
        }
    }


    private void resetProgress() {
        this.progress = 0;
    }

    private boolean isRecipeValid() {
        SimpleContainer inventory = new SimpleContainer(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 2); // Add one for the mold slot
        for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            inventory.setItem(i - FIRST_INPUT_SLOT, itemHandler.getStackInSlot(i));
        }
        inventory.setItem(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 1, itemHandler.getStackInSlot(MOLD_SLOT)); // Add mold slot

        Optional<MechanicalPressRecipe> currentRecipe = getCurrentRecipe();
        if (currentRecipe.isPresent()) {
            MechanicalPressRecipe recipe = currentRecipe.get();
            assert level != null;
            return recipe.matches(inventory, level);
        }
        return false;
    }

    private Optional<MechanicalPressRecipe> getCurrentRecipe() {
        if (level == null) return Optional.empty();
        RecipeManager recipeManager = level.getRecipeManager();
        SimpleContainer inventory = new SimpleContainer(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 2); // Add one for the mold slot
        for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            inventory.setItem(i - FIRST_INPUT_SLOT, itemHandler.getStackInSlot(i));
        }
        inventory.setItem(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 1, itemHandler.getStackInSlot(MOLD_SLOT)); // Add mold slot
        return recipeManager.getAllRecipesFor(MechanicalPressRecipe.Type.INSTANCE).stream()
                .filter(recipe -> recipe.matches(inventory, level))
                .findFirst();
    }

    private boolean canBurnFuel() {
        ItemStack fuelStack = itemHandler.getStackInSlot(FUEL_SLOT);
        return !fuelStack.isEmpty() && ForgeHooks.getBurnTime(fuelStack, RecipeType.SMELTING) > 0;
    }

    private void burnFuel() {
        ItemStack fuelStack = itemHandler.getStackInSlot(FUEL_SLOT);
        this.burnTime = ForgeHooks.getBurnTime(fuelStack, RecipeType.SMELTING);
        this.maxBurnTime = this.burnTime;
        if (fuelStack.hasCraftingRemainingItem()) {
            itemHandler.setStackInSlot(FUEL_SLOT, fuelStack.getCraftingRemainingItem());
        } else {
            fuelStack.shrink(1);
            if (fuelStack.isEmpty()) {
                itemHandler.setStackInSlot(FUEL_SLOT, ItemStack.EMPTY);
            }
        }
    }
    private boolean hasFuel() {
        return this.burnTime > 0;
    }
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    /////CAPABILITIES
        private record FuelItemHandler(ItemStackHandler itemHandler) implements IItemHandlerModifiable {

        @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                itemHandler.setStackInSlot(slot, stack);
            }

            @Override
            public int getSlots() {
                return itemHandler.getSlots();
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return itemHandler.getStackInSlot(slot);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (slot == FUEL_SLOT && ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0) {
                    return itemHandler.insertItem(slot, stack, simulate);
                }
                return stack;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }

            @Override
            public int getSlotLimit(int slot) {
                return itemHandler.getSlotLimit(slot);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return slot == FUEL_SLOT && ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
            }
        }

    private record OutputItemHandler(ItemStackHandler itemHandler) implements IItemHandlerModifiable {

        @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                itemHandler.setStackInSlot(slot, stack);
            }
            @Override
            public int getSlots() {
                return itemHandler.getSlots();
            }
            @Override
            public @NotNull ItemStack getStackInSlot(int i) {
                return itemHandler.getStackInSlot(i);
            }
            @Override
            public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return stack;
            }
            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == OUTPUT_SLOT) {
                    return itemHandler.extractItem(slot, amount, simulate);
                }
                return ItemStack.EMPTY;
            }
            @Override
            public int getSlotLimit(int slot) {
                return itemHandler.getSlotLimit(slot);
            }
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return false;
            }
        }
    private class TopItemHandler implements IItemHandlerModifiable {
        private final ItemStackHandler itemHandler;
        public TopItemHandler(ItemStackHandler itemHandler) {
            this.itemHandler = itemHandler;
        }
        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            itemHandler.setStackInSlot(slot, stack);
        }
        @Override
        public int getSlots() {
            return itemHandler.getSlots();
        }
        @Override
        public ItemStack getStackInSlot(int slot) {
            return itemHandler.getStackInSlot(slot);
        }
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.getItem() instanceof MoldItem) {
                ItemStack moldStack = itemHandler.getStackInSlot(MOLD_SLOT);
                if (moldStack.isEmpty() || (moldStack.isDamageableItem() && moldStack.getDamageValue() < moldStack.getMaxDamage())) {
                    return itemHandler.insertItem(MOLD_SLOT, stack, simulate);
                }
            }
            return itemHandler.insertItem(slot, stack, simulate);
        }
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
        @Override
        public int getSlotLimit(int slot) {
            return itemHandler.getSlotLimit(slot);
        }
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= FIRST_INPUT_SLOT && slot <= LAST_INPUT_SLOT;
        }
    }
}