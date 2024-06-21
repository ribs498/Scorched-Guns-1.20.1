package top.ribs.scguns.enchantment;

import top.ribs.scguns.Reference;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.item.BayonetItem;
import top.ribs.scguns.item.GunItem;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Author: MrCrayfish
 */
public class EnchantmentTypes {
    public static final EnchantmentCategory GUN = EnchantmentCategory.create(Reference.MOD_ID + ":gun", item -> item instanceof GunItem);
    public static final EnchantmentCategory SEMI_AUTO_GUN = EnchantmentCategory.create(Reference.MOD_ID + ":semi_auto_gun", item -> item instanceof GunItem && ((GunItem) item).getGun().getGeneral().getFireMode() != FireMode.AUTOMATIC);
    public static final EnchantmentCategory BAYONET = EnchantmentCategory.create(Reference.MOD_ID + ":bayonet", item -> item instanceof BayonetItem);
}
