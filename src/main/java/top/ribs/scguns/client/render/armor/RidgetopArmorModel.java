package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.RidgetopArmorItem;

public class RidgetopArmorModel extends GeoModel<RidgetopArmorItem> {
    @Override
    public ResourceLocation getModelResource(RidgetopArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/ridgetop.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RidgetopArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/armor/ridgetop.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RidgetopArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/ridgetop.animation.json");
    }
}