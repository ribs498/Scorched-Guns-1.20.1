package top.ribs.scguns.item.exosuit;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

public class ExoSuitCoreItem extends Item {
    private final int capacity;
    private final CoreTier tier;

    public ExoSuitCoreItem(Properties properties, CoreTier tier) {
        super(properties);
        this.tier = tier;
        this.capacity = tier.getCapacity();
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() ->
                    new SimpleExoSuitEnergyStorage(stack, capacity));

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
                return cap == ForgeCapabilities.ENERGY ? energy.cast() : LazyOptional.empty();
            }
        };
    }

    public static class SimpleExoSuitEnergyStorage implements IEnergyStorage {
        private final ItemStack stack;
        private final int capacity;
        private int energy;

        public SimpleExoSuitEnergyStorage(ItemStack stack, int capacity) {
            this.stack = stack;
            this.capacity = capacity;
            this.energy = loadEnergyFromNBT();
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

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int energyStored = getEnergyStored(stack);
        int maxEnergy = getMaxEnergyStored(stack);
        if (maxEnergy == 0) return 0;
        return Math.round(13.0F * energyStored / maxEnergy);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float ratio = (float) getEnergyStored(stack) / getMaxEnergyStored(stack);

        if (ratio > 0.66f) {
            return 0x00FFFF;
        } else if (ratio > 0.33f) {
            return 0xFFFF00;
        } else {
            return 0xFF4444;
        }
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

        tooltip.add(Component.translatable("tooltip.scguns.energy")
                .append(": ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%,d", energyStored))
                        .withStyle(ChatFormatting.BLUE))
                .append(Component.literal(" / ")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.format("%,d", maxEnergy) + " FE")
                        .withStyle(ChatFormatting.BLUE)));
    }

    /**
     * Enum defining different tiers of power cores
     */

    public enum CoreTier {
        BASIC("Basic", 10000, 500),
        ADVANCED("Advanced", 20000, 800),
        ELITE("Elite", 30000, 1000);

        private final String displayName;
        private final int capacity;

        CoreTier(String displayName, int capacity, int maxTransfer) {
            this.displayName = displayName;
            this.capacity = capacity;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getCapacity() {
            return capacity;
        }
    }
}