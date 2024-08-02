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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.block.PoweredMaceratorBlock;
import top.ribs.scguns.client.screen.MaceratorMenu;
import top.ribs.scguns.client.screen.MaceratorRecipe;
import top.ribs.scguns.client.screen.PoweredMaceratorMenu;
import top.ribs.scguns.init.ModBlockEntities;

import java.util.Optional;

public class PoweredMaceratorBlockEntity extends BlockEntity implements MenuProvider {

    public final ItemStackHandler itemHandler = new ItemStackHandler(5) {
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
    };

    private boolean isInputSlot(int slot) {
        return slot >= FIRST_INPUT_SLOT && slot <= LAST_INPUT_SLOT;
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ContainerData data;
    private int progress = 0;
    private int maxProgress = 100;
    private final EnergyStorage energyStorage = new EnergyStorage(16000) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) {
                setChanged();
                sync();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) {
                setChanged();
                sync();
            }
            return extracted;
        }
    };
    private final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);
    public static final int FIRST_INPUT_SLOT = 0;
    public static final int LAST_INPUT_SLOT = 3;
    public static final int OUTPUT_SLOT = 4;
    private static final float WHEEL_ROTATION_SPEED = 20.0f;
    public PoweredMaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POWERED_MACERATOR.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                switch (index) {
                    case 0: return progress;
                    case 1: return maxProgress;
                    case 2: return energyStorage.getEnergyStored();
                    case 3: return energyStorage.getMaxEnergyStored();
                    default: return 0;
                }
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0: progress = value; break;
                    case 1: maxProgress = value; break;
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
        return Component.translatable("container.powered_macerator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new PoweredMaceratorMenu(id, inv, this, this.data);
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
                return LazyOptional.of(() -> new InputItemHandler(itemHandler)).cast();
            }
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("powered_macerator.progress", progress);
        tag.put("Energy", energyStorage.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("powered_macerator.progress");
        energyStorage.deserializeNBT(tag.get("Energy"));
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
    public static void tick(Level level, BlockPos pos, BlockState state, PoweredMaceratorBlockEntity blockEntity) {
        boolean wasLit = state.getValue(PoweredMaceratorBlock.LIT);
        boolean isLit = false;

        if (!level.isClientSide) {
            boolean hasValidRecipe = blockEntity.hasRecipe();
            ItemStack resultItem = blockEntity.getRecipeResult();

            if (hasValidRecipe && blockEntity.hasEnoughEnergy(50) && blockEntity.hasSpaceForOutput(resultItem)) {
                isLit = true;
                blockEntity.progress++;
                blockEntity.consumeEnergy(50);
                if (blockEntity.progress >= blockEntity.maxProgress) {
                    blockEntity.craftItem();
                    blockEntity.resetProgress();
                }
            } else {
                if (blockEntity.progress > 0) {
                    blockEntity.resetProgress();
                }
            }

            if (wasLit != isLit) {
                level.setBlock(pos, state.setValue(PoweredMaceratorBlock.LIT, isLit), 3);
            }
        }
    }

    private ItemStack getRecipeResult() {
        SimpleContainer inventory = new SimpleContainer(LAST_INPUT_SLOT - FIRST_INPUT_SLOT + 1);
        for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            inventory.setItem(i - FIRST_INPUT_SLOT, itemHandler.getStackInSlot(i));
        }
        assert level != null;
        Optional<MaceratorRecipe> match = level.getRecipeManager()
                .getRecipeFor(MaceratorRecipe.Type.INSTANCE, inventory, level);
        return match.map(recipe -> recipe.getResultItem(level.registryAccess())).orElse(ItemStack.EMPTY);
    }

    private boolean hasSpaceForOutput(ItemStack output) {
        ItemStack currentOutput = itemHandler.getStackInSlot(OUTPUT_SLOT);
        return currentOutput.isEmpty() || (currentOutput.getItem() == output.getItem() && currentOutput.getCount() + output.getCount() <= currentOutput.getMaxStackSize());
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

    private boolean hasEnoughEnergy(int amount) {
        return energyStorage.getEnergyStored() >= amount;
    }

    public void consumeEnergy(int amount) {
        energyStorage.extractEnergy(amount, false);
        setChanged();
        sync();
    }

    public void addEnergy(int amount) {
        energyStorage.receiveEnergy(amount, false);
        setChanged();
        sync();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    private void sync() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public float getWheelRotation(float partialTicks) {
        assert level != null;
        return (level.getGameTime() + partialTicks) * WHEEL_ROTATION_SPEED % 360;
    }


    private static class InputItemHandler implements IItemHandlerModifiable {
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
            if (slot == FIRST_INPUT_SLOT || slot == LAST_INPUT_SLOT) {
                return itemHandler.insertItem(slot, stack, simulate);
            }
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= FIRST_INPUT_SLOT && slot <= LAST_INPUT_SLOT) {
                return itemHandler.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return itemHandler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == FIRST_INPUT_SLOT || slot == LAST_INPUT_SLOT;
        }
    }

    private static class OutputItemHandler implements IItemHandlerModifiable {
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
        public ItemStack getStackInSlot(int slot) {
            return itemHandler.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
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
