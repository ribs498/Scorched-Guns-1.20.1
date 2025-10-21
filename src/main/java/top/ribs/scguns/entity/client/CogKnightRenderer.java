package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.CogKnightEntity;
public class CogKnightRenderer extends MobRenderer<CogKnightEntity, CogKnightModel<CogKnightEntity>> {
    public CogKnightRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new CogKnightModel<>(pContext.bakeLayer(ModModelLayers.COG_KNIGHT_LAYER)), 0.7f);
        this.addLayer(new ItemInHandLayer<>(this, pContext.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(CogKnightEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/cog_knight.png");
    }

    @Override
    public void render(CogKnightEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        pMatrixStack.translate(0.0D, 0.35D, 0.0D);
        pMatrixStack.scale(1.0f, 1.0f, 1.0f);

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);

        pMatrixStack.popPose();
    }
}


