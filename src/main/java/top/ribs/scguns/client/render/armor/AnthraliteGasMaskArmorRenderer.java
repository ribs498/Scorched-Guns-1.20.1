package top.ribs.scguns.client.render.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.AnthraliteGasMaskArmorItem;

public class AnthraliteGasMaskArmorRenderer extends GeoArmorRenderer<AnthraliteGasMaskArmorItem> {
    public AnthraliteGasMaskArmorRenderer() {
        super(new AnthraliteGasMaskArmorModel());
    }
}