package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;

public class AirCanisterItem extends Item {
    private final int capacity;
    private static final int AIR_PER_USE = 50;
    private static final int HUNGER_COST = 1;
    private static final int USE_COOLDOWN = 10;

    public AirCanisterItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IEnergyStorage> airStorage = LazyOptional.of(() -> new AirStorage(stack, capacity));

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
                return cap == ForgeCapabilities.ENERGY ? airStorage.cast() : LazyOptional.empty();
            }
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        FoodData foodData = player.getFoodData();
        int currentAir = getAirStored(stack);
        int maxAir = getMaxAirStored(stack);

        if (currentAir >= maxAir) {
            return InteractionResultHolder.pass(stack);
        }

        if (foodData.getFoodLevel() < HUNGER_COST && !player.isCreative()) {
            return InteractionResultHolder.pass(stack);
        }

        IEnergyStorage airStorage = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        int airAdded = airStorage.receiveEnergy(AIR_PER_USE, false);

        if (airAdded > 0) {
            if (!player.isCreative() && !level.isClientSide) {
                foodData.addExhaustion(0.5f);
            }
            level.playSound(null, player.blockPosition(),
                    SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 0.5F, 1.2F);
            player.getCooldowns().addCooldown(this, USE_COOLDOWN);
            updateDurabilityDisplay(stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int airStored = getAirStored(stack);
        int maxAir = getMaxAirStored(stack);
        if (maxAir == 0) return 0;
        return Math.round(13.0F * airStored / maxAir);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int airStored = getAirStored(stack);
        int maxAir = getMaxAirStored(stack);

        if (maxAir == 0) return 0x808080;

        float ratio = (float) airStored / maxAir;

        if (ratio < 0.25f) {
            return 0xFF4444;
        } else if (ratio < 0.5f) {
            return 0xFFAA00;
        } else {
            return 0x00AAFF;
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        int airStored = getAirStored(stack);
        int maxAir = getMaxAirStored(stack);

        tooltip.add(Component.translatable("info.scguns.air_stored")
                .append(": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(airStored + " / " + maxAir).withStyle(ChatFormatting.AQUA)));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("info.scguns.air_canister.usage")
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.scguns.hold_shift")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    public int getAirStored(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public int getMaxAirStored(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getMaxEnergyStored).orElse(capacity);
    }

    private void updateDurabilityDisplay(ItemStack stack) {
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10;
    }

    public static class AirStorage implements IEnergyStorage {
        private final ItemStack stack;
        private final int capacity;
        private int air;

        public AirStorage(ItemStack stack, int capacity) {
            this.stack = stack;
            this.capacity = capacity;
            this.air = loadAirFromNBT();
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int airReceived = Math.min(capacity - air, maxReceive);
            if (!simulate && airReceived > 0) {
                air += airReceived;
                updateAirTag();
            }
            return airReceived;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int airExtracted = Math.min(air, maxExtract);
            if (!simulate && airExtracted > 0) {
                air -= airExtracted;
                updateAirTag();
            }
            return airExtracted;
        }

        @Override
        public int getEnergyStored() {
            return air;
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

        private void updateAirTag() {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("AirStored", air);
        }

        private int loadAirFromNBT() {
            CompoundTag tag = stack.getTag();
            return tag != null && tag.contains("AirStored", Tag.TAG_INT) ? tag.getInt("AirStored") : 0;
        }
    }
}