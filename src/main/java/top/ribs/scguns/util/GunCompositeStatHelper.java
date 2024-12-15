package top.ribs.scguns.util;


import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

/**
 * Author: MrCrayfish
 */
public class GunCompositeStatHelper {
    public GunCompositeStatHelper() {
    }

    public static int getCompositeRate(ItemStack weapon, Gun modifiedGun) {
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        return GunModifierHelper.getModifiedRate(weapon, a);
    }

    public static int getCompositeRate(ItemStack weapon) {
        Gun modifiedGun = ((GunItem)weapon.getItem()).getModifiedGun(weapon);
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        return GunModifierHelper.getModifiedRate(weapon, a);
    }

    public static int getCompositeBaseRate(ItemStack weapon, Gun modifiedGun) {
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        return GunModifierHelper.getModifiedRate(weapon, a);
    }

    public static int getCompositeBaseRate(ItemStack weapon) {
        Gun modifiedGun = ((GunItem)weapon.getItem()).getModifiedGun(weapon);
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        return GunModifierHelper.getModifiedRate(weapon, a);
    }

    public static float getCompositeSpread(ItemStack weapon, Gun modifiedGun) {
        return GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getGeneral().getSpread());
    }

    public static float getCompositeMinSpread(ItemStack weapon, Gun modifiedGun) {
        return GunModifierHelper.getModifiedSpread(weapon, 0.0F);
    }
    public static int getCompositeRate(ItemStack weapon, Gun modifiedGun, Player player) {
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        int b = GunModifierHelper.getModifiedRate(weapon, a);
        return a;
    }
}
