package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.NetheriteGasMaskArmorItem;

public class NetheriteGasMaskArmorModel extends GeoModel<NetheriteGasMaskArmorItem> {
    @Override
    public ResourceLocation getModelResource(NetheriteGasMaskArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/netherite_respirator.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NetheriteGasMaskArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/armor/netherite_respirator.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NetheriteGasMaskArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/netherite_gas_mask.animation.json");
    }
}