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
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.entity.projectile.ProjectileEntity;

public class ShotballRenderer extends EntityRenderer<ProjectileEntity>
{
    public ShotballRenderer(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ProjectileEntity entity)
    {
        return null;
    }

    @Override
    public void render(ProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light)
    {
        if(entity.getProjectile().isVisible() || entity.tickCount <= 2)
        {
            return;
        }

        poseStack.pushPose();
        float spin = (entity.tickCount + partialTicks) * 0.5F;
        poseStack.mulPose(Axis.XP.rotationDegrees(spin * 2.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(spin * 1.5F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin));

        if(!RenderUtil.getModel(entity.getItem()).isGui3d())
        {
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            Minecraft.getInstance().getItemRenderer().renderStatic(entity.getItem(), ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, poseStack, renderTypeBuffer, entity.level(), 0);
        }
        else
        {
            Minecraft.getInstance().getItemRenderer().renderStatic(entity.getItem(), ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, poseStack, renderTypeBuffer, entity.level(), 0);
        }

        poseStack.popPose();
    }
}