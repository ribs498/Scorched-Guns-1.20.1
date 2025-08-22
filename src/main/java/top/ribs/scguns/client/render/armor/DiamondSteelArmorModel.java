package top.ribs.scguns.client.render.armor;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.DiamondSteelArmorItem;

public class DiamondSteelArmorModel extends GeoModel<DiamondSteelArmorItem> {
    @Override
    public ResourceLocation getModelResource(DiamondSteelArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/diamond_steel_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DiamondSteelArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/armor/diamond_steel_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DiamondSteelArmorItem animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/diamond_steel_armor.animation.json");
    }
}