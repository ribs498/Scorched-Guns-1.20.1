package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.RedcoatArmorItem;

public class RedcoatArmorModel extends GeoModel<RedcoatArmorItem> {
    @Override
    public ResourceLocation getModelResource(RedcoatArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/redcoat_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RedcoatArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/armor/redcoat_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RedcoatArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/redcoat_armor.animation.json");
    }
}