package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.CogKnightEntity;
import top.ribs.scguns.entity.monster.PraetorEntity;

public class PraetorRenderer extends MobRenderer<PraetorEntity, PraetorModel<PraetorEntity>> {
    public PraetorRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new PraetorModel<>(pContext.bakeLayer(ModModelLayers.PRAETOR_LAYER)), 0.7f);
    }
    @Override
    public ResourceLocation getTextureLocation(PraetorEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/praetor.png");
    }
    @Override
    public void render(PraetorEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        pMatrixStack.translate(0.0D, 0.0D, 0.0D);
        pMatrixStack.scale(1.1f, 1.1f, 1.1f);

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);

        pMatrixStack.popPose();
    }
}
