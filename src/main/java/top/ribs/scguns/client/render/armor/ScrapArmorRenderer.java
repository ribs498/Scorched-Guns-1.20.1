package top.ribs.scguns.client.render.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.ScrapArmorItem;

public class ScrapArmorRenderer extends GeoArmorRenderer<ScrapArmorItem> {
    public ScrapArmorRenderer() {
        super(new ScrapArmorModel());
    }
}