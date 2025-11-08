package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.TreatedBrassArmorItem;

public class TreatedBrassArmorModel extends GeoModel<TreatedBrassArmorItem> {
    @Override
    public ResourceLocation getModelResource(TreatedBrassArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/treated_brass_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TreatedBrassArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/armor/treated_brass_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TreatedBrassArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/treated_brass_armor.animation.json");
    }
}