package top.ribs.scguns.client.render.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.AnthraliteGasMaskArmorItem;
import top.ribs.scguns.item.animated.RidgetopArmorItem;

public class RidgetopArmorRenderer extends GeoArmorRenderer<RidgetopArmorItem> {
    public RidgetopArmorRenderer() {
        super(new RidgetopArmorModel());
    }
}