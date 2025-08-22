package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.ScampRocketEntity;

public class ScampRocketRenderer extends EntityRenderer<ScampRocketEntity> {
    private final ScampRocketModel<ScampRocketEntity> model;

    public ScampRocketRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.model = new ScampRocketModel<>(pContext.bakeLayer(ModModelLayers.SCAMP_ROCKET_LAYER));
    }

    @Override
    public ResourceLocation getTextureLocation(ScampRocketEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/scamp_rocket.png");
    }

    @Override
    public void render(ScampRocketEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        Vec3 velocity = entity.getDeltaMovement();

        if (velocity.lengthSqr() > 0.01) {
            float yaw = (float) (Mth.atan2(velocity.x, velocity.z) * (180.0 / Math.PI));

            double horizontalDistance = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            float pitch = (float) (Mth.atan2(-velocity.y, horizontalDistance) * (180.0 / Math.PI));

            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        }

        this.model.renderToBuffer(poseStack,
                buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity))),
                packedLight, 0, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}