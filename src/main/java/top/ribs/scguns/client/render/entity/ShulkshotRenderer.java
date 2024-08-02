package top.ribs.scguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import top.ribs.scguns.entity.projectile.ShulkshotProjectileEntity;

public class ShulkshotRenderer extends EntityRenderer<ShulkshotProjectileEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/shulker/spark.png");
    private static final RenderType RENDER_TYPE;
    private final ShulkerBulletModel<ShulkshotProjectileEntity> model;

    public ShulkshotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ShulkerBulletModel<ShulkshotProjectileEntity>(context.bakeLayer(ModelLayers.SHULKER_BULLET));
    }

    @Override
    protected int getBlockLightLevel(ShulkshotProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(ShulkshotProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Get interpolated position and rotation
        float interpolatedYaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float interpolatedPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float tickCountWithPartialTicks = (float) entity.tickCount + partialTicks;

        // Apply position and rotation transformations
        poseStack.translate(0.0F, 0.15F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(tickCountWithPartialTicks * 0.1F) * 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.cos(tickCountWithPartialTicks * 0.1F) * 180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(tickCountWithPartialTicks * 0.15F) * 360.0F));
        poseStack.scale(-0.5F, -0.5F, 0.5F);

        // Set up animation and render the model
        this.model.setupAnim(entity, 0.0F, 0.0F, 0.0F, interpolatedYaw, interpolatedPitch);
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(TEXTURE_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        // Render translucent overlay
        poseStack.scale(1.5F, 1.5F, 1.5F);
        VertexConsumer translucentVertexConsumer = buffer.getBuffer(RENDER_TYPE);
        this.model.renderToBuffer(poseStack, translucentVertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.15F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ShulkshotProjectileEntity entity) {
        return TEXTURE_LOCATION;
    }

    static {
        RENDER_TYPE = RenderType.entityTranslucent(TEXTURE_LOCATION);
    }
}