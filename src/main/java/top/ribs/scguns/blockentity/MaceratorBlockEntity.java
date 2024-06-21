package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
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
import top.ribs.scguns.block.MaceratorBlock;
import top.ribs.scguns.client.screen.MaceratorMenu;
import top.ribs.scguns.client.screen.MaceratorRecipe;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.init.ModBlockEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaceratorBlockEntity extends BlockEntity implements MenuProvider {

    public final ItemStackHandler itemHandler = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                if (isInputSlot(slot)) {
                    if (!isRecipeValid()) {
                        resetProgress();
                    }
                }
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot >= FIRST_INPUT_SLOT && slot <= LAST_INPUT_SLOT) {
                ItemStack existingStack = itemHandler.getStackInSlot(slot);
                if (!existingStack.isEmpty() && !ItemStack.isSameItem(existingStack, stack)) {
                    return stack;
                }
            }
            return super.insertItem(slot, stack, simulate);
        }
    };

    private boolean isInputSlot(int slot) {
        return slot >= FIRST_INPUT_SLOT && slot <= LAST_INPUT_SLOT;
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ContainerData data;
    private int progress = 0;
    private int maxProgress = 100;
    private int burnTime = 0;
    private int maxBurnTime = 0;
    private static final float WHEEL_ROTATION_SPEED = 20.0f;
    public static final int FIRST_INPUT_SLOT = 0;
    public static final int LAST_INPUT_SLOT = 3;
    public static final int FUEL_SLOT = 4;
    public static final int OUTPUT_SLOT = 5;

    public MaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACERATOR.get(), pos, state);
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
        return Component.translatable("container.macerator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        MaceratorMenu menu = new MaceratorMenu(id, inv, this, this.data);
        return menu;
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
                return LazyOptional.of(() -> new InputItemHandler(itemHandler)).cast();
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
        tag.putInt("macerator.progress", progress);
        tag.putInt("macerator.burnTime", burnTime);
        tag.putInt("macerator.maxBurnTime", maxBurnTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("macerator.progress");
        burnTime = tag.getInt("macerator.burnTime");
        maxBurnTime = tag.getInt("macerator.maxBurnTime");
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

    public static void tick(Level level, BlockPos pos, BlockState state, MaceratorBlockEntity blockEntity) {
        boolean wasLit = state.getValue(MaceratorBlock.LIT);
        boolean isLit = false;

        if (!level.isClientSide) {
            boolean hasValidRecipe = blockEntity.hasRecipe();

            if (blockEntity.hasFuel()) {
                blockEntity.burnTime--;
                isLit = true;
            } else if (hasValidRecipe && blockEntity.canBurnFuel()) {
                blockEntity.burnFuel();
                isLit = true;
            } else {
                if (blockEntity.progress > 0) {
                    blockEntity.resetProgress();
                }
            }
            if (hasValidRecipe && blockEntity.hasFuel()) {
                blockEntity.progress++;
                if (blockEntity.progress >= blockEntity.maxProgress) {
                    blockEntity.craftItem();
                    blockEntity.resetProgress();
                }
            } else if (!hasValidRecipe) {
                blockEntity.resetProgress();
            }

            if (wasLit != isLit) {
                level.setBlock(pos, state.setValue(MaceratorBlock.LIT, isLit), 3);
            }
        }
    }

    private boolean hasRecipe() {
        SimpleContainer inventory = new SimpleContainer(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 1);
        for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            inventory.setItem(i - FIRST_INPUT_SLOT, itemHandler.getStackInSlot(i));
        }
        assert level != null;
        Optional<MaceratorRecipe> match = level.getRecipeManager()
                .getRecipeFor(MaceratorRecipe.Type.INSTANCE, inventory, level);
        return match.isPresent();
    }

    private void craftItem() {
        SimpleContainer inventory = new SimpleContainer(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 1);
        for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            inventory.setItem(i - FIRST_INPUT_SLOT, itemHandler.getStackInSlot(i));
        }
        assert level != null;
        Optional<MaceratorRecipe> match = level.getRecipeManager()
                .getRecipeFor(MaceratorRecipe.Type.INSTANCE, inventory, level);
        if (match.isPresent()) {
            MaceratorRecipe recipe = match.get();
            ItemStack resultItem = recipe.getResultItem(level.registryAccess());
            ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
            if (outputStack.isEmpty() || (outputStack.getItem() == resultItem.getItem() && outputStack.getCount() + resultItem.getCount() <= outputStack.getMaxStackSize())) {
                for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
                    itemHandler.extractItem(i, 1, false);
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
        SimpleContainer inventory = new SimpleContainer(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 1);
        for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            inventory.setItem(i - FIRST_INPUT_SLOT, itemHandler.getStackInSlot(i));
        }

        Optional<MaceratorRecipe> currentRecipe = getCurrentRecipe();
        if (currentRecipe.isPresent()) {
            MaceratorRecipe recipe = currentRecipe.get();
            return recipe.matches(inventory, level);
        }
        return false;
    }

    private Optional<MaceratorRecipe> getCurrentRecipe() {
        if (level == null) return Optional.empty();
        RecipeManager recipeManager = level.getRecipeManager();
        SimpleContainer inventory = new SimpleContainer(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 1);
        for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            inventory.setItem(i - FIRST_INPUT_SLOT, itemHandler.getStackInSlot(i));
        }
        return recipeManager.getAllRecipesFor(MaceratorRecipe.Type.INSTANCE).stream()
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

    public float getWheelRotation(float partialTicks) {
        assert level != null;
        return (level.getGameTime() + partialTicks) * WHEEL_ROTATION_SPEED % 360;
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    /////CAPABILITIES
    private class InputItemHandler implements IItemHandlerModifiable {
        private final ItemStackHandler itemHandler;

        public InputItemHandler(ItemStackHandler itemHandler) {
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
            if (slot >= FIRST_INPUT_SLOT && slot <= LAST_INPUT_SLOT) {
                return itemHandler.insertItem(slot, stack, simulate);
            }
            return stack; // Prevent insertion into non-input slots
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY; // Prevent extraction from input slots
        }

        @Override
        public int getSlotLimit(int slot) {
            return itemHandler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= FIRST_INPUT_SLOT && slot <= LAST_INPUT_SLOT; // Only input slots are valid
        }
    }

    private class FuelItemHandler implements IItemHandlerModifiable {
        private final ItemStackHandler itemHandler;

        public FuelItemHandler(ItemStackHandler itemHandler) {
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
            if (slot == FUEL_SLOT && ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0) {
                return itemHandler.insertItem(slot, stack, simulate);
            }
            return stack;
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
            return slot == FUEL_SLOT && ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
        }
    }

    private class OutputItemHandler implements IItemHandlerModifiable {
        private final ItemStackHandler itemHandler;

        public OutputItemHandler(ItemStackHandler itemHandler) {
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
}