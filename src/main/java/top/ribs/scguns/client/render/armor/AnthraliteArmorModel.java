package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.AnthraliteArmorItem;

public class AnthraliteArmorModel extends GeoModel<AnthraliteArmorItem> {
    @Override
    public ResourceLocation getModelResource(AnthraliteArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/anthralite_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AnthraliteArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/armor/anthralite_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AnthraliteArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/anthralite_armor.animation.json");
    }
}