package top.ribs.scguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import top.ribs.scguns.entity.throwable.ThrowableShotballEntity;

import javax.annotation.Nullable;


public class ThrowableShotballRenderer extends EntityRenderer<ThrowableShotballEntity> {

    public ThrowableShotballRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Nullable
    @Override
    public ResourceLocation getTextureLocation(ThrowableShotballEntity entity) {
        return null;
    }

    @Override
    public void render(ThrowableShotballEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
        poseStack.pushPose();

        float spin = (entity.tickCount + partialTicks) * 0.5F;
        poseStack.mulPose(Axis.XP.rotationDegrees(spin * 2.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(spin * 1.5F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin));

        poseStack.scale(1.0F, 1.0F, 1.0F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                entity.getItem(),
                ItemDisplayContext.NONE,
                light,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                renderTypeBuffer,
                entity.level(),
                0
        );

        poseStack.popPose();
    }
}