package top.ribs.scguns.enchantment;

import top.ribs.scguns.Reference;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.BayonetItem;
import top.ribs.scguns.item.GunItem;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import top.ribs.scguns.item.NonUnderwaterGunItem;
import top.ribs.scguns.item.animated.NonUnderwaterAnimatedGunItem;

/**
 * Author: MrCrayfish
 */
public class EnchantmentTypes {
    public static final EnchantmentCategory GUN = EnchantmentCategory.create(Reference.MOD_ID + ":gun", item -> item instanceof GunItem);
    public static final EnchantmentCategory SEMI_AUTO_GUN = EnchantmentCategory.create(Reference.MOD_ID + ":semi_auto_gun", item -> item instanceof GunItem && ((GunItem) item).getGun().getGeneral().getFireMode() != FireMode.AUTOMATIC);
    public static final EnchantmentCategory BAYONET = EnchantmentCategory.create(Reference.MOD_ID + ":bayonet", item -> item instanceof BayonetItem);

    public static final EnchantmentCategory TRIGGER_FINGER_COMPATIBLE = EnchantmentCategory.create(
            Reference.MOD_ID + ":trigger_finger_compatible",
            item -> item instanceof GunItem && !item.builtInRegistryHolder().is(ModTags.Items.SINGLE_SHOT)
    );

    // Single shot guns
    public static final EnchantmentCategory SINGLE_SHOT_GUN = EnchantmentCategory.create(
            Reference.MOD_ID + ":single_shot_gun",
            item -> item instanceof GunItem && item.builtInRegistryHolder().is(ModTags.Items.SINGLE_SHOT)
    );

    // Water-proof compatible guns
    public static final EnchantmentCategory WATER_PROOF_COMPATIBLE = EnchantmentCategory.create(
            Reference.MOD_ID + ":water_proof_compatible",
            item -> item instanceof GunItem && !item.builtInRegistryHolder().is(ModTags.Items.NON_UNDERWATER)
    );

    // Shell catcher compatible
    public static final EnchantmentCategory SHELL_CATCHER_COMPATIBLE = EnchantmentCategory.create(
            Reference.MOD_ID + ":shell_catcher_compatible",
            item -> item instanceof GunItem && !item.builtInRegistryHolder().is(ModTags.Items.DOES_NOT_EJECT_CASINGS)
    );

    // Collateral compatible
    public static final EnchantmentCategory COLLATERAL_COMPATIBLE = EnchantmentCategory.create(
            Reference.MOD_ID + ":collateral_compatible",
            item -> item instanceof GunItem && !item.builtInRegistryHolder().is(ModTags.Items.NON_COLLATERAL)
    );

    private static boolean isSingleShotGun(GunItem gunItem) {
        return gunItem != ModItems.FLINTLOCK_PISTOL.get() &&
                gunItem != ModItems.HANDCANNON.get() &&
                gunItem != ModItems.MUSKET.get() &&
                gunItem != ModItems.BLUNDERBUSS.get() &&
                gunItem != ModItems.ROCKET_RIFLE.get() &&
                gunItem != ModItems.BOMB_LANCE.get() &&
                gunItem != ModItems.BRAWLER.get() &&
                gunItem != ModItems.SAKETINI.get();
    }
    private static boolean isNonCollateral(GunItem gunItem) {
        return gunItem == ModItems.ROCKET_RIFLE.get() ||
                gunItem == ModItems.GYROJET_PISTOL.get() ||
                gunItem == ModItems.MK43_RIFLE.get() ||
                gunItem == ModItems.DOZIER_RL.get() ||
                gunItem == ModItems.CYCLONE.get() ||
                gunItem == ModItems.SHELLURKER.get() ||
                gunItem == ModItems.CARAPICE.get();
    }
    private static boolean doesNotEjectCasings(GunItem gunItem) {
        return gunItem == ModItems.FLINTLOCK_PISTOL.get() ||
                gunItem == ModItems.HANDCANNON.get() ||
                gunItem == ModItems.MUSKET.get() ||
                gunItem == ModItems.BLUNDERBUSS.get() ||
                gunItem == ModItems.BOMB_LANCE.get() ||
                gunItem == ModItems.DOZIER_RL.get() ||
                gunItem == ModItems.REPEATING_MUSKET.get() ||
                gunItem == ModItems.ROCKET_RIFLE.get() ||
                gunItem == ModItems.MK43_RIFLE.get() ||
                gunItem == ModItems.GYROJET_PISTOL.get();
    }
    private static boolean isNonUnderwaterGun(GunItem gunItem) {
        return gunItem instanceof NonUnderwaterGunItem || gunItem instanceof NonUnderwaterAnimatedGunItem;
    }
}

