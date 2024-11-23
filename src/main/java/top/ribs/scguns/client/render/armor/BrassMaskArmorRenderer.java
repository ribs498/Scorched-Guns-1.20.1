package top.ribs.scguns.client.render.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.BrassMaskArmorItem;

public class BrassMaskArmorRenderer extends GeoArmorRenderer<BrassMaskArmorItem> {
    public BrassMaskArmorRenderer() {
        super(new BrassMaskArmorModel());
    }
}