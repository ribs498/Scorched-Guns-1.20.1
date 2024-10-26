package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class CreativeAmmoBoxItem extends AmmoBoxItem {
    private static final int CREATIVE_MAX_ITEM_COUNT = 1000000;
    private static final int CREATIVE_BAR_COLOR = Mth.color(0.4F, 0.4F, 0.7F);

    public CreativeAmmoBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    protected ResourceLocation getAmmoTag() {
        return new ResourceLocation("scguns", "all_ammo");
    }



    @Override
    public int getBarColor(ItemStack stack) {
        return CREATIVE_BAR_COLOR;
    }

    @Override
    protected int getBaseMaxItemCount() {
        return CREATIVE_MAX_ITEM_COUNT;
    }

}
