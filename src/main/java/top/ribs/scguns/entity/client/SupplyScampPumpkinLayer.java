package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.entity.monster.SupplyScampEntity;

public class SupplyScampPumpkinLayer extends RenderLayer<SupplyScampEntity, SupplyScampModel<SupplyScampEntity>> {
    private final BlockRenderDispatcher blockRenderer;

    public SupplyScampPumpkinLayer(RenderLayerParent<SupplyScampEntity, SupplyScampModel<SupplyScampEntity>> renderer) {
        super(renderer);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       SupplyScampEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (!entity.isWearingPumpkin()) {
            return;
        }

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        poseStack.translate(0.0, 0.6, 1.1);

        float scale = 0.7f;
        poseStack.scale(scale, scale, scale);

        poseStack.translate(-0.5, -0.5, -0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));

        poseStack.translate(0.0, -1.0, 0.0);

        BlockState pumpkinState = Blocks.JACK_O_LANTERN.defaultBlockState();
        this.blockRenderer.renderSingleBlock(pumpkinState, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}