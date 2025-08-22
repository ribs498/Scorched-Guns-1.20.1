package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.ExoSuitItem;

public class ExoSuitModel extends GeoModel<ExoSuitItem> {
    @Override
    public ResourceLocation getModelResource(ExoSuitItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/exo_suit.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ExoSuitItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/armor/exo_suit.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ExoSuitItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/exo_suit.animation.json");
    }
}