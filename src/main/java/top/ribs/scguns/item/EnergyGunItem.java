package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import top.ribs.scguns.item.GunItem;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import javax.annotation.Nullable;

import java.util.List;

public class EnergyGunItem extends GunItem {
    private final int capacity;

    public EnergyGunItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> new ItemEnergyStorage(stack, capacity));

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
                return cap == ForgeCapabilities.ENERGY ? energy.cast() : LazyOptional.empty();
            }
        };
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true; // Always show the energy bar
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getEnergyStored(stack) / getMaxEnergyStored(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FF00; // Green color for the energy bar
    }

    public int getEnergyStored(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public int getMaxEnergyStored(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getMaxEnergyStored).orElse(0);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);

        int energyStored = getEnergyStored(stack);
        int maxEnergy = getMaxEnergyStored(stack);

        tooltip.add(Component.translatable("info.scguns.energy")
                .append(": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(energyStored + " / " + maxEnergy + " FE").withStyle(ChatFormatting.GREEN)));
    }

    public static class ItemEnergyStorage implements IEnergyStorage {
        private final ItemStack stack;
        private final int capacity;
        private int energy;

        public ItemEnergyStorage(ItemStack stack, int capacity) {
            this.stack = stack;
            this.capacity = capacity;
            this.energy = loadEnergyFromNBT(); // Load energy from NBT when the capability is created
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int energyReceived = Math.min(capacity - energy, maxReceive);
            if (!simulate) {
                energy += energyReceived;
                updateEnergyTag();
            }
            return energyReceived;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int energyExtracted = Math.min(energy, maxExtract);
            if (!simulate) {
                energy -= energyExtracted;
                updateEnergyTag();
            }
            return energyExtracted;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }

        private void updateEnergyTag() {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("Energy", energy);
        }

        private int loadEnergyFromNBT() {
            CompoundTag tag = stack.getTag();
            return tag != null && tag.contains("Energy", Tag.TAG_INT) ? tag.getInt("Energy") : 0;
        }
    }

}
