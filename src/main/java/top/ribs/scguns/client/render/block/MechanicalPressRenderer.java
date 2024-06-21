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
import top.ribs.scguns.block.MaceratorBlock;
import top.ribs.scguns.block.MechanicalPressBlock;
import top.ribs.scguns.blockentity.MaceratorBlockEntity;
import top.ribs.scguns.blockentity.MechanicalPressBlockEntity;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.util.RenderUtil;

@OnlyIn(Dist.CLIENT)
public class MechanicalPressRenderer implements BlockEntityRenderer<MechanicalPressBlockEntity> {

    public MechanicalPressRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MechanicalPressBlockEntity pressEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState blockState = pressEntity.getBlockState();
        boolean isLit = blockState.getValue(MechanicalPressBlock.LIT);
        float pressPosition = pressEntity.getPressPosition(partialTicks, isLit);
        renderPress(matrixStack, buffer, light, overlay, SpecialModels.MECHANICAL_PRESS_PRESS.getModel(), pressPosition);
    }

    private void renderPress(PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, BakedModel model, float pressPosition) {
        if (model != null) {
            matrixStack.pushPose();
            matrixStack.translate(0.0, pressPosition, 0.0);
            RenderUtil.renderMaceratorWheel(model, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
        }
    }
}




