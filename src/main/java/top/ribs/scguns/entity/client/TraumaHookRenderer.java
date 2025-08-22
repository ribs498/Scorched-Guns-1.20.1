package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import top.ribs.scguns.entity.projectile.TraumaHookEntity;

public class TraumaHookRenderer extends EntityRenderer<TraumaHookEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/fishing_hook.png");

    public TraumaHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TraumaHookEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Entity owner = entity.getOwner();
        if (owner != null) {
            renderFishingLine(entity, owner, partialTicks, poseStack, buffer, packedLight);
        }

        // Render the hook itself
        renderHook(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderHook(TraumaHookEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        // Simple quad with full vertex data
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 0.0F, 0, 0, 1);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 1.0F, 0, 1, 1);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 1.0F, 1, 1, 0);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 0.0F, 1, 0, 0);

        poseStack.popPose();
    }

    private void renderFishingLine(TraumaHookEntity hook, Entity owner, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Get owner position (from trauma unit's left arm/hook casting position)
        double ownerX = Mth.lerp(partialTicks, owner.xo, owner.getX());
        double ownerY = Mth.lerp(partialTicks, owner.yo, owner.getY()) + owner.getEyeHeight() * 0.8; // Approximate arm height
        double ownerZ = Mth.lerp(partialTicks, owner.zo, owner.getZ());

        // Get hook position
        double hookX = Mth.lerp(partialTicks, hook.xo, hook.getX());
        double hookY = Mth.lerp(partialTicks, hook.yo, hook.getY()) + 0.25D;
        double hookZ = Mth.lerp(partialTicks, hook.zo, hook.getZ());

        // Calculate line vector
        float deltaX = (float)(ownerX - hookX);
        float deltaY = (float)(ownerY - hookY);
        float deltaZ = (float)(ownerZ - hookZ);

        VertexConsumer lineConsumer = buffer.getBuffer(RenderType.lineStrip());
        PoseStack.Pose pose = poseStack.last();

        // Render the line in segments for a natural curve
        int segments = 16;
        for (int i = 0; i <= segments; i++) {
            renderLineSegment(deltaX, deltaY, deltaZ, lineConsumer, pose, fraction(i, segments), fraction(i + 1, segments));
        }

        poseStack.popPose();
    }

    private static float fraction(int numerator, int denominator) {
        return (float)numerator / (float)denominator;
    }

    private static void renderLineSegment(float deltaX, float deltaY, float deltaZ, VertexConsumer consumer, PoseStack.Pose pose, float start, float end) {
        float x = deltaX * start;
        // Add a slight curve to the line for realism
        float y = deltaY * (start * start + start) * 0.5F + 0.25F;
        float z = deltaZ * start;

        float nextX = deltaX * end - x;
        float nextY = deltaY * (end * end + end) * 0.5F + 0.25F - y;
        float nextZ = deltaZ * end - z;

        float length = Mth.sqrt(nextX * nextX + nextY * nextY + nextZ * nextZ);
        nextX /= length;
        nextY /= length;
        nextZ /= length;

        // Render line segment with dark color (like fishing line)
        consumer.vertex(pose.pose(), x, y, z)
                .color(32, 32, 32, 255) // Dark gray line
                .normal(pose.normal(), nextX, nextY, nextZ)
                .endVertex();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, int light, float x, int y, int u, int v) {
        consumer.vertex(pose, x - 0.5F, y - 0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(TraumaHookEntity entity) {
        return TEXTURE;
    }
}