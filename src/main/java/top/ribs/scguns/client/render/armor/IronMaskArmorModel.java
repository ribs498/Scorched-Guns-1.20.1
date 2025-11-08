package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.IronMaskArmorItem;

public class IronMaskArmorModel extends GeoModel<IronMaskArmorItem> {
    @Override
    public ResourceLocation getModelResource(IronMaskArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/iron_mask.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IronMaskArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/armor/iron_mask.png");
    }

    @Override
    public ResourceLocation getAnimationResource(IronMaskArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/iron_mask.animation.json");
    }
}