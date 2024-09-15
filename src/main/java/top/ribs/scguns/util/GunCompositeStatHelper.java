package top.ribs.scguns.util;


import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

/**
 * Author: MrCrayfish
 */
public class GunCompositeStatHelper {
    // This helper delivers composite stats derived from GunModifierHelper and GunEnchantmentHelper.

    public static int getCompositeRate(ItemStack weapon, Gun modifiedGun, Player player) {
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        int b = GunModifierHelper.getModifiedRate(weapon, a);
        return a;
    }
}
