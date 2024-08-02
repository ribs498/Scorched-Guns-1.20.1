package top.ribs.scguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.ribs.scguns.block.PolarGeneratorBlock;
import top.ribs.scguns.blockentity.PolarGeneratorBlockEntity;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.util.RenderUtil;

@OnlyIn(Dist.CLIENT)
public class PolarGeneratorRenderer implements BlockEntityRenderer<PolarGeneratorBlockEntity> {

    public PolarGeneratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PolarGeneratorBlockEntity polarGenerator, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState blockState = polarGenerator.getBlockState();
        float rotation = polarGenerator.getWheelRotation(partialTicks);

        if (blockState.getValue(PolarGeneratorBlock.LIT)) {
            renderWheel(matrixStack, buffer, light, overlay, SpecialModels.POLAR_GENERATOR_WHEEL_1.getModel(), rotation, 0.5, 0.5, 0.5);
            renderWheel(matrixStack, buffer, light, overlay, SpecialModels.POLAR_GENERATOR_WHEEL_2.getModel(), -rotation, 0.5, 0.5, 0.5); // Opposite direction
        } else {
            renderWheel(matrixStack, buffer, light, overlay, SpecialModels.POLAR_GENERATOR_WHEEL_1.getModel(), 0, 0.5, 0.5, 0.5);
            renderWheel(matrixStack, buffer, light, overlay, SpecialModels.POLAR_GENERATOR_WHEEL_2.getModel(), 0, 0.5, 0.5, 0.5);
        }
    }

    private void renderWheel(PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, BakedModel model, float rotation, double x, double y, double z) {
        if (model != null) {
            matrixStack.pushPose();
            matrixStack.translate(x, y, z);
            matrixStack.mulPose(Axis.YP.rotationDegrees(rotation));
            matrixStack.translate(-x, -y, -z);
            RenderUtil.renderPolarGeneratorWheel(model, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
        }
    }
}
