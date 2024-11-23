package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.AdrienArmorItem;

public class AdrienArmorModel extends GeoModel<AdrienArmorItem> {
    @Override
    public ResourceLocation getModelResource(AdrienArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/adrien_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AdrienArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/armor/adrien_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AdrienArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/adrien_armor.animation.json");
    }
}