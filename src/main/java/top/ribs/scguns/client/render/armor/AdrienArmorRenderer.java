package top.ribs.scguns.client.render.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.AdrienArmorItem;

public class AdrienArmorRenderer extends GeoArmorRenderer<AdrienArmorItem> {
    public AdrienArmorRenderer() {
        super(new AdrienArmorModel());
    }
}