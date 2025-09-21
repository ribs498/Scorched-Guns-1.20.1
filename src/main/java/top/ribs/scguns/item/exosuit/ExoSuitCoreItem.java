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
import top.ribs.scguns.Config;

import javax.annotation.Nullable;
import java.util.List;

public class ExoSuitCoreItem extends Item {
    private final CoreTier tier;

    public ExoSuitCoreItem(Properties properties, CoreTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() ->
                    new SimpleExoSuitEnergyStorage(stack, tier.getCapacity()));

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
                return cap == ForgeCapabilities.ENERGY ? energy.cast() : LazyOptional.empty();
            }
        };
    }

    public static class SimpleExoSuitEnergyStorage implements IEnergyStorage {
        private final ItemStack stack;
        private final int capacity;

        public SimpleExoSuitEnergyStorage(ItemStack stack, int capacity) {
            this.stack = stack;
            this.capacity = capacity;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int currentEnergy = getEnergyFromNBT();
            int energyReceived = Math.min(capacity - currentEnergy, maxReceive);

            if (!simulate && energyReceived > 0) {
                setEnergyToNBT(currentEnergy + energyReceived);
            }
            return energyReceived;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int currentEnergy = getEnergyFromNBT();
            int energyExtracted = Math.min(currentEnergy, maxExtract);

            if (!simulate && energyExtracted > 0) {
                setEnergyToNBT(currentEnergy - energyExtracted);
            }
            return energyExtracted;
        }

        @Override
        public int getEnergyStored() {
            return getEnergyFromNBT();
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

        private int getEnergyFromNBT() {
            CompoundTag tag = stack.getTag();
            return tag != null && tag.contains("Energy", Tag.TAG_INT) ? tag.getInt("Energy") : 0;
        }

        private void setEnergyToNBT(int energy) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("Energy", Math.max(0, Math.min(energy, capacity)));
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

    public enum CoreTier {
        BASIC("Basic"),
        ADVANCED("Advanced"),
        ELITE("Elite");

        private final String displayName;

        CoreTier(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
        public int getCapacity() {
            return switch (this) {
                case BASIC -> Config.COMMON.exoSuitCores.basicCoreCapacity.get();
                case ADVANCED -> Config.COMMON.exoSuitCores.advancedCoreCapacity.get();
                case ELITE -> Config.COMMON.exoSuitCores.eliteCoreCapacity.get();
            };
        }
    }
}