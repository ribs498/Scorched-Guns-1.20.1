package top.ribs.scguns.item;

import net.minecraft.world.item.ItemStack;

public class MakeshiftGunItem extends GunItem {

    public MakeshiftGunItem(Properties properties) {

        super(properties);

    }

    public boolean isFoil(ItemStack ignored) {
        return false;
    }

}