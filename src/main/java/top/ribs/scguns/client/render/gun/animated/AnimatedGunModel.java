package top.ribs.scguns.client.render.gun.animated;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import top.ribs.scguns.item.animated.AnimatedGunItem;


public class AnimatedGunModel extends DefaultedItemGeoModel<AnimatedGunItem> {
    private final String modelPath;

    public AnimatedGunModel(ResourceLocation path) {
        super(path);
        this.modelPath = path.getPath();
    }

    @Override
    public ResourceLocation getModelResource(AnimatedGunItem gunItem) {
        return new ResourceLocation("scguns", "geo/item/gun/" + modelPath + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AnimatedGunItem gunItem) {
        return new ResourceLocation("scguns", "textures/animated/gun/" + modelPath + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(AnimatedGunItem gunItem) {
        return new ResourceLocation("scguns", "animations/item/" + modelPath + ".animation.json");
    }
}