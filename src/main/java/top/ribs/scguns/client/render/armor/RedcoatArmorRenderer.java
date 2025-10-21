package top.ribs.scguns.client.render.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.RedcoatArmorItem;

public class RedcoatArmorRenderer extends GeoArmorRenderer<RedcoatArmorItem> {
    public RedcoatArmorRenderer() {
        super(new RedcoatArmorModel());
    }
}