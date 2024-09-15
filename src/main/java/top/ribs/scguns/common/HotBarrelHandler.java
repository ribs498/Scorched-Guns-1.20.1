package top.ribs.scguns.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import top.ribs.scguns.init.ModEnchantments;

public class HotBarrelHandler {

    public static final int MAX_HOT_BARREL = 100;
    private static final int BASE_DECAY_RATE = 3;

    public static void increaseHotBarrel(ItemStack stack, int amount) {
        CompoundTag tag = stack.getOrCreateTag();
        int currentHotBarrel = tag.getInt("HotBarrelLevel");
        currentHotBarrel = Math.min(MAX_HOT_BARREL, currentHotBarrel + amount);
        tag.putInt("HotBarrelLevel", currentHotBarrel);
    }

    public static void decayHotBarrel(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int ticksSinceLastShot = tag.getInt("TicksSinceLastShot");
        int currentHotBarrel = tag.getInt("HotBarrelLevel");

        int dynamicDecayInterval = getDynamicDecayInterval();

        if (ticksSinceLastShot >= dynamicDecayInterval && currentHotBarrel > 0) {
            currentHotBarrel = Math.max(currentHotBarrel - BASE_DECAY_RATE, 0);
            tag.putInt("HotBarrelLevel", currentHotBarrel);
            ticksSinceLastShot = 0;
        } else {
            ticksSinceLastShot++;
        }

        tag.putInt("TicksSinceLastShot", ticksSinceLastShot);
    }

    public static void clearHotBarrel(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("HotBarrelLevel", 0);  // Clear the hot barrel level
        tag.putInt("TicksSinceLastShot", 0);  // Reset the ticks counter
    }

    private static int getDynamicDecayInterval() {
        return 4;  // Medium decay pace
    }

    public static int getHotBarrelLevel(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt("HotBarrelLevel");
    }
}
