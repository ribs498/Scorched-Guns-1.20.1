package top.ribs.scguns.client.render.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.IronMaskArmorItem;

public class IronMaskArmorRenderer extends GeoArmorRenderer<IronMaskArmorItem> {
    public IronMaskArmorRenderer() {
        super(new IronMaskArmorModel());
    }
}