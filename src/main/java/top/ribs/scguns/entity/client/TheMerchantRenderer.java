package top.ribs.scguns.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.TheMerchantEntity;

public class TheMerchantRenderer extends MobRenderer<TheMerchantEntity, TheMerchantModel<TheMerchantEntity>> {
    public TheMerchantRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new TheMerchantModel<>(pContext.bakeLayer(ModModelLayers.THE_MERCHANT_LAYER)), 0.7f);
    }
    @Override
    public ResourceLocation getTextureLocation(TheMerchantEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/the_merchant.png");
    }
}


