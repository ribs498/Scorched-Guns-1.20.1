package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.AdjudicatorEntity;

public class AdjudicatorRenderer extends MobRenderer<AdjudicatorEntity, AdjudicatorModel<AdjudicatorEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/entity/adjudicator.png");

    public AdjudicatorRenderer(EntityRendererProvider.Context context) {
        super(context, new AdjudicatorModel<>(context.bakeLayer(ModModelLayers.ADJUDICATOR_LAYER)), 0.6f);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(AdjudicatorEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(AdjudicatorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(1.15f, 1.15f, 1.15f);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        poseStack.popPose();
    }
}