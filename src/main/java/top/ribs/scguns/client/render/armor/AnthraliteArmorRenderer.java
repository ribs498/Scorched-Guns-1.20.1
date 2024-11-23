package top.ribs.scguns.client.render.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.AdrienArmorItem;
import top.ribs.scguns.item.animated.AnthraliteArmorItem;

public class AnthraliteArmorRenderer extends GeoArmorRenderer<AnthraliteArmorItem> {
    public AnthraliteArmorRenderer() {
        super(new AnthraliteArmorModel());
    }
}