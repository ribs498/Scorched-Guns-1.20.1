package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class SpecialAmmoBoxItem extends AmmoBoxItem {
    private static final int SPECIAL_MAX_ITEM_COUNT = 256;
    private static final int SPECIAL_BAR_COLOR = Mth.color(0.4F, 0.4F, 0.7F);

    public SpecialAmmoBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    protected ResourceLocation getAmmoTag() {
        return new ResourceLocation("scguns", "special_ammo");
    }

    @Override
    protected int getMaxItemCount() {
        return SPECIAL_MAX_ITEM_COUNT;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return SPECIAL_BAR_COLOR;
    }

}
