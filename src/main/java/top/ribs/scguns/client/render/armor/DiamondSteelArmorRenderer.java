package top.ribs.scguns.client.render.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.DiamondSteelArmorItem;

public class DiamondSteelArmorRenderer extends GeoArmorRenderer<DiamondSteelArmorItem> {
    public DiamondSteelArmorRenderer() {
        super(new DiamondSteelArmorModel());
    }
}