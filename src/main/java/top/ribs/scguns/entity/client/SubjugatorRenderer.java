package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.SubjugatorEntity;

public class SubjugatorRenderer extends MobRenderer<SubjugatorEntity, SubjugatorModel<SubjugatorEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/entity/subjugator.png");

    public SubjugatorRenderer(EntityRendererProvider.Context context) {
        super(context, new SubjugatorModel<>(context.bakeLayer(ModModelLayers.SUBJUGATOR_LAYER)), 0.5f);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(SubjugatorEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(SubjugatorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(1.15f, 1.15f, 1.15f);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        poseStack.popPose();
    }
}