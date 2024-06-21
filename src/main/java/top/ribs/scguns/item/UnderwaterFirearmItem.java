package top.ribs.scguns.item;

import net.minecraft.world.item.ItemStack;

/*
 * Makes so Assault Rifles can be assigned with different features.
 */
public class UnderwaterFirearmItem extends GunItem {

    public UnderwaterFirearmItem(Properties properties) {

        super(properties);

    }

    //Disables the enchantment foil to allow a different kind of customization.
    public boolean isFoil(ItemStack ignored) {
        return false;
    }

}