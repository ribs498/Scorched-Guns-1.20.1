package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class MagnumAmmoBoxItem extends AmmoBoxItem {
    private static final int MAGNUM_MAX_ITEM_COUNT = 256;
    private static final int MAGNUM_BAR_COLOR = Mth.color(0.4F, 0.4F, 0.7F);

    public MagnumAmmoBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    protected ResourceLocation getAmmoTag() {
        return new ResourceLocation("scguns", "magnum_ammo");
    }

    @Override
    protected int getMaxItemCount() {
        return MAGNUM_MAX_ITEM_COUNT;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return MAGNUM_BAR_COLOR;
    }

}
