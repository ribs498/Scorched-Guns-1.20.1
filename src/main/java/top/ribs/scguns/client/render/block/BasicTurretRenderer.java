package top.ribs.scguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.valkyrienskies.core.impl.shadow.Ax;
import top.ribs.scguns.block.BasicTurretBlock;
import top.ribs.scguns.block.MaceratorBlock;
import top.ribs.scguns.blockentity.BasicTurretBlockEntity;
import top.ribs.scguns.blockentity.MaceratorBlockEntity;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.util.RenderUtil;

@OnlyIn(Dist.CLIENT)
public class BasicTurretRenderer implements BlockEntityRenderer<BasicTurretBlockEntity> {

    public BasicTurretRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BasicTurretBlockEntity turret, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        double previousYaw = turret.getPreviousYaw();
        double yaw = turret.getYaw();
        float interpolatedYaw = (float) (previousYaw + Mth.wrapDegrees(yaw - previousYaw) * partialTicks);
        double previousPitch = turret.getPreviousPitch();
        double pitch = turret.getPitch();
        float interpolatedPitch = (float) (previousPitch + (pitch - previousPitch) * partialTicks);
        renderTurretTop(turret, matrixStack, buffer, light, overlay, SpecialModels.BASIC_TURRET_TOP.getModel(), 0.5, 1.0, 0.5, interpolatedYaw, interpolatedPitch);
    }

    private void renderTurretTop(BasicTurretBlockEntity turret, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, BakedModel model, double x, double y, double z, float yaw, float pitch) {
        if (model != null) {
            matrixStack.pushPose();
            matrixStack.translate(x, y, z);
            matrixStack.mulPose(Axis.YP.rotationDegrees(yaw));

            // Apply recoil to pitch only
            float recoilPitch = pitch + turret.getRecoilPitchOffset(); // Add recoil pitch offset
            matrixStack.mulPose(Axis.XP.rotationDegrees(recoilPitch));

            matrixStack.translate(-x, -y, -z);
            RenderUtil.renderMaceratorWheel(model, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
        }
    }


    @Override
    public boolean shouldRenderOffScreen(BasicTurretBlockEntity p_112304_) {
        return true;
    }
}
