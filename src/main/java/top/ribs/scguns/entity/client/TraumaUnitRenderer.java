package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.TraumaUnitEntity;

public class TraumaUnitRenderer extends MobRenderer<TraumaUnitEntity, TraumaUnitModel<TraumaUnitEntity>> {
    public TraumaUnitRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new TraumaUnitModel<>(pContext.bakeLayer(ModModelLayers.TRAUMA_UNIT_LAYER)), 0.7f);
        this.addLayer(new TraumaUnitRedOverlayLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(TraumaUnitEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/trauma_unit.png");
    }

    @Override
    protected void scale(TraumaUnitEntity entity, PoseStack poseStack, float partialTicks) {
        float swell = entity.getSwelling(partialTicks);
        float pulse = 1.0F + Mth.sin(swell * 100.0F) * swell * 0.01F;
        swell = Mth.clamp(swell, 0.0F, 1.0F);
        swell *= swell;
        swell *= swell;
        float scale = (1.0F + swell * 0.2F) * pulse;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    protected float getWhiteOverlayProgress(TraumaUnitEntity entity, float partialTicks) {
        return 0.0F;
    }

    static class TraumaUnitRedOverlayLayer extends RenderLayer<TraumaUnitEntity, TraumaUnitModel<TraumaUnitEntity>> {
        private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/entity/trauma_unit.png");

        public TraumaUnitRedOverlayLayer(RenderLayerParent<TraumaUnitEntity, TraumaUnitModel<TraumaUnitEntity>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           TraumaUnitEntity entity, float limbSwing, float limbSwingAmount,
                           float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity.isPrimed()) {
                float swell = entity.getSwelling(partialTicks);
                if ((int)(swell * 10.0F) % 2 != 0 && swell > 0.01F) {
                    float redIntensity = Mth.clamp(swell, 0.3F, 0.8F);
                    VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
                    this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight,
                            getOverlayCoords(entity, 0.0F), 1.0F, redIntensity * 0.5F, redIntensity * 0.5F, 0.8F);
                }
            }
        }
    }
}