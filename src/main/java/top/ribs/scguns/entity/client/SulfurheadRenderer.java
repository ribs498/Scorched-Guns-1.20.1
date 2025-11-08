package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.SulfurheadEntity;

public class SulfurheadRenderer extends MobRenderer<SulfurheadEntity, SulfurheadModel<SulfurheadEntity>> {
    public SulfurheadRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SulfurheadModel<>(pContext.bakeLayer(ModModelLayers.SULFURHEAD_LAYER)), 0.5f);
        this.addLayer(new SulfurheadGelLayer(this));
        this.addLayer(new SulfurheadPrimeOverlayLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(SulfurheadEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/sulfurhead.png");
    }

    @Override
    public void render(SulfurheadEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.scale(1.0f, 1.0f, 1.0f);
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    @Override
    protected void scale(SulfurheadEntity entity, PoseStack poseStack, float partialTicks) {
        float swell = entity.getSwelling(partialTicks);
        float pulse = 1.0F + Mth.sin(swell * 100.0F) * swell * 0.01F;
        swell = Mth.clamp(swell, 0.0F, 1.0F);
        swell *= swell;
        swell *= swell;
        float scale = (1.0F + swell * 0.15F) * pulse;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    protected float getWhiteOverlayProgress(SulfurheadEntity entity, float partialTicks) {
        return 0.0F;
    }

    static class SulfurheadPrimeOverlayLayer extends RenderLayer<SulfurheadEntity, SulfurheadModel<SulfurheadEntity>> {
        private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/entity/sulfurhead.png");

        public SulfurheadPrimeOverlayLayer(RenderLayerParent<SulfurheadEntity, SulfurheadModel<SulfurheadEntity>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           SulfurheadEntity entity, float limbSwing, float limbSwingAmount,
                           float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity.isPrimed()) {
                float swell = entity.getSwelling(partialTicks);
                if ((int)(swell * 10.0F) % 2 != 0 && swell > 0.01F) {
                    float intensity = Mth.clamp(swell, 0.3F, 0.9F);
                    VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
                    this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight,
                            getOverlayCoords(entity, 0.0F), 1.0F, intensity * 0.8F, 0.2F, 0.7F);
                }
            }
        }
    }
}