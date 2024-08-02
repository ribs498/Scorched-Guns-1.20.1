package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.item.AmmoBoxItem;

public class RifleAmmoBoxItem extends AmmoBoxItem {
    private static final int RIFLE_MAX_ITEM_COUNT = 768;
    private static final int RIFLE_BAR_COLOR = Mth.color(0.4F, 0.4F, 0.7F);

    public RifleAmmoBoxItem(Item.Properties properties) {
        super(properties);
    }
    @Override
    protected ResourceLocation getAmmoTag() {
        return new ResourceLocation("scguns", "rifle_ammo");
    }

    @Override
    protected int getMaxItemCount() {
        return RIFLE_MAX_ITEM_COUNT;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return RIFLE_BAR_COLOR;
    }

}

