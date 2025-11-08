package top.ribs.scguns.client.render.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.TreatedBrassArmorItem;

public class TreatedBrassArmorRenderer extends GeoArmorRenderer<TreatedBrassArmorItem> {
    public TreatedBrassArmorRenderer() {
        super(new TreatedBrassArmorModel());
    }
}