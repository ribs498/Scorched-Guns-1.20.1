package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class RocketAmmoBoxItem extends AmmoBoxItem {
    private static final int ROCKET_MAX_ITEM_COUNT = 128;
    private static final int ROCKET_BAR_COLOR = Mth.color(0.4F, 0.4F, 0.7F);

    public RocketAmmoBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    protected ResourceLocation getAmmoTag() {
        return new ResourceLocation("scguns", "rocket_ammo");
    }

    @Override
    protected int getMaxItemCount() {
        return ROCKET_MAX_ITEM_COUNT;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ROCKET_BAR_COLOR;
    }

}
