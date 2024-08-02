package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.block.LightningBattery;
import top.ribs.scguns.client.screen.LightningBatteryMenu;
import top.ribs.scguns.client.screen.LightningBatteryRecipe;
import top.ribs.scguns.client.screen.MechanicalPressMenu;
import top.ribs.scguns.client.screen.PoweredMaceratorMenu;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nullable;
public class LightningBatteryBlockEntity extends BlockEntity implements MenuProvider {
    private static final int MAX_ENERGY = 32000;

    public final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final EnergyStorage energyStorage = new EnergyStorage(MAX_ENERGY) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) {
                setChanged();
                sync();
                updateBlockState();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) {
                setChanged();
                sync();
                updateBlockState();
            }
            return extracted;
        }
    };
    private final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    private int processingTime;
    private int processingTimeTotal;

    public LightningBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIGHTNING_BATTERY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.lightning_battery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new LightningBatteryMenu(id, inv, this, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> processingTime;
                    case 1 -> processingTimeTotal;
                    case 2 -> energyStorage.getEnergyStored();
                    case 3 -> energyStorage.getMaxEnergyStored();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> processingTime = value;
                    case 1 -> processingTimeTotal = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        });
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
        tag.put("Energy", energyStorage.serializeNBT());
        tag.putInt("ProcessingTime", processingTime);
        tag.putInt("ProcessingTimeTotal", processingTimeTotal);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        energyStorage.deserializeNBT(tag.get("Energy"));
        processingTime = tag.getInt("ProcessingTime");
        processingTimeTotal = tag.getInt("ProcessingTimeTotal");
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

    public int getEnergy() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergy() {
        return energyStorage.getMaxEnergyStored();
    }

    public void setEnergyStored(int i) {
        energyStorage.receiveEnergy(i, false);
        updateBlockState();
    }

    public void tick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        boolean wasProcessing = isProcessing();

        ItemStack inputStack = itemHandler.getStackInSlot(INPUT_SLOT);
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);

        if (!inputStack.isEmpty()) {
            LightningBatteryRecipe recipe = getRecipe(inputStack);
            if (recipe != null) {
                ItemStack recipeOutput = recipe.getResultItem(RegistryAccess.EMPTY);
                if (outputStack.isEmpty() || (outputStack.is(recipeOutput.getItem()) && outputStack.getCount() + recipeOutput.getCount() <= outputStack.getMaxStackSize())) {
                    if (hasEnoughEnergy(recipe.getEnergyUse())) {
                        processingTime++;
                        processingTimeTotal = recipe.getProcessingTime();
                        if (processingTime >= processingTimeTotal) {
                            consumeEnergy(recipe.getEnergyUse());
                            inputStack.shrink(1);
                            if (outputStack.isEmpty()) {
                                itemHandler.setStackInSlot(OUTPUT_SLOT, recipeOutput.copy());
                            } else {
                                outputStack.grow(recipeOutput.getCount());
                            }
                            processingTime = 0;
                        }
                    } else {
                        processingTime = 0;
                    }
                } else {
                    processingTime = 0;
                }
            } else {
                processingTime = 0;
            }
        } else {
            processingTime = 0;
        }

        if (wasProcessing != isProcessing()) {
            setChanged();
            sync();
        } else {
            setChanged();
        }

        updateBlockState();
    }

    private LightningBatteryRecipe getRecipe(ItemStack inputStack) {
        if (this.level == null) {
            return null;
        }
        return this.level.getRecipeManager().getRecipeFor(LightningBatteryRecipe.Type.INSTANCE, new SimpleContainer(inputStack), this.level).orElse(null);
    }

    public boolean isProcessing() {
        return processingTime > 0 && processingTime < processingTimeTotal;
    }

    private boolean hasEnoughEnergy(int energyUse) {
        return energyStorage.getEnergyStored() >= energyUse;
    }

    private void consumeEnergy(int amount) {
        int energyBefore = energyStorage.getEnergyStored();
        energyStorage.extractEnergy(amount, false);
        int energyAfter = energyStorage.getEnergyStored();
    }

    private void updateBlockState() {
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            int energyStored = this.getEnergy();
            boolean isCharged = energyStored > 0;
            LightningBattery.ChargeLevel chargeLevel = calculateChargeLevel(energyStored);

            this.level.setBlock(this.worldPosition, state.setValue(LightningBattery.CHARGED, isCharged)
                    .setValue(LightningBattery.CHARGE_LEVEL, chargeLevel), 3);
        }
    }

    private LightningBattery.ChargeLevel calculateChargeLevel(int energyStored) {
        if (energyStored > 40000) {
            return LightningBattery.ChargeLevel.HIGH;
        } else if (energyStored > 16000) {
            return LightningBattery.ChargeLevel.MID;
        } else if (energyStored > 0) {
            return LightningBattery.ChargeLevel.LOW;
        } else {
            return LightningBattery.ChargeLevel.NONE;
        }
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
            if (slot == INPUT_SLOT) {
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
            return slot == INPUT_SLOT;
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
