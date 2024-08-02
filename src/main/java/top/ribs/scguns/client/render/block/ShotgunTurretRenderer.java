package top.ribs.scguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.ribs.scguns.blockentity.ShotgunTurretBlockEntity;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.util.RenderUtil;

@OnlyIn(Dist.CLIENT)
public class ShotgunTurretRenderer implements BlockEntityRenderer<ShotgunTurretBlockEntity> {

    public ShotgunTurretRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ShotgunTurretBlockEntity turret, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        double previousYaw = turret.getPreviousYaw();
        double yaw = turret.getYaw();
        float interpolatedYaw = (float) (previousYaw + Mth.wrapDegrees(yaw - previousYaw) * partialTicks);
        double previousPitch = turret.getPreviousPitch();
        double pitch = turret.getPitch();
        float interpolatedPitch = (float) (previousPitch + (pitch - previousPitch) * partialTicks);
        renderTurretTop(turret, matrixStack, buffer, light, overlay, SpecialModels.SHOTGUN_TURRET_TOP.getModel(), 0.5, 1.0, 0.5, interpolatedYaw, interpolatedPitch);
    }

    private void renderTurretTop(ShotgunTurretBlockEntity turret, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, BakedModel model, double x, double y, double z, float yaw, float pitch) {
        if (model != null) {
            matrixStack.pushPose();
            matrixStack.translate(x, y, z);
            matrixStack.mulPose(Axis.YP.rotationDegrees(yaw));
            float recoilPitch = pitch + turret.getRecoilPitchOffset();
            matrixStack.mulPose(Axis.XP.rotationDegrees(recoilPitch));

            matrixStack.translate(-x, -y, -z);
            RenderUtil.renderMaceratorWheel(model, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
        }
    }
    @Override
    public boolean shouldRenderOffScreen(ShotgunTurretBlockEntity p_112304_) {
        return true;
    }
}
