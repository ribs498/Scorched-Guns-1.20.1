package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.ScrapArmorItem;

public class ScrapArmorModel extends GeoModel<ScrapArmorItem> {
    @Override
    public ResourceLocation getModelResource(ScrapArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/scrap_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ScrapArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/armor/scrap_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ScrapArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/scrap_armor.animation.json");
    }
}