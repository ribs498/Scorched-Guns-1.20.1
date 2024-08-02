package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.BlundererEntity;
import top.ribs.scguns.entity.monster.HiveEntity;

public class BlundererRenderer extends MobRenderer<BlundererEntity, BlundererModel<BlundererEntity>> {
    public BlundererRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new BlundererModel<>(pContext.bakeLayer(ModModelLayers.BLUNDERER_LAYER)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(BlundererEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/blunderer.png");
    }

    @Override
    public void render(BlundererEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.scale(0.9f, 0.9f, 0.9f);
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}






