package top.ribs.scguns.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.item.GunItem;

public class GunCurseUtil {
    private static final float MOB_GUN_CURSE_CHANCE = 0.85f;

    public static ItemStack applyCurseIfRoll(ItemStack stack, RandomSource random) {
        if (stack.isEmpty() || !(stack.getItem() instanceof GunItem)) {
            return stack;
        }

        if (random.nextFloat() < MOB_GUN_CURSE_CHANCE) {
            stack.enchant(ModEnchantments.GUN_RUST.get(), 1);
        }

        return stack;
    }

    public static boolean isCursed(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.GUN_RUST.get(), stack) > 0;
    }

    public static void removeCurse(ItemStack stack) {
        if (isCursed(stack)) {
            stack.removeTagKey("Enchantments");
        }
    }
}