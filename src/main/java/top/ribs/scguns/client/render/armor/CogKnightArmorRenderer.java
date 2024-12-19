package top.ribs.scguns.client.render.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.AdrienArmorItem;
import top.ribs.scguns.item.animated.CogKnightArmorItem;

public class CogKnightArmorRenderer extends GeoArmorRenderer<CogKnightArmorItem> {
    public CogKnightArmorRenderer() {
        super(new CogKnightArmorModel());
    }
}