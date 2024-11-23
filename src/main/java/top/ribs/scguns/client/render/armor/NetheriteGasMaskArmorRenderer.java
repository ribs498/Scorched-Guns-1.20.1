package top.ribs.scguns.client.render.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.item.animated.NetheriteGasMaskArmorItem;

public class NetheriteGasMaskArmorRenderer extends GeoArmorRenderer<NetheriteGasMaskArmorItem> {
    public NetheriteGasMaskArmorRenderer() {
        super(new NetheriteGasMaskArmorModel());
    }
}