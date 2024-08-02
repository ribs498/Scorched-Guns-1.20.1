package top.ribs.scguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.ribs.scguns.entity.block.PrimedNitroKeg;
import top.ribs.scguns.init.ModBlocks;

@OnlyIn(Dist.CLIENT)
public class NitroKegRenderer extends EntityRenderer<PrimedNitroKeg> {
    private final BlockRenderDispatcher blockRenderer;

    public NitroKegRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(PrimedNitroKeg entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        int fuse = entity.getFuse();
        if ((float)fuse - partialTicks + 1.0F < 10.0F) {
            float scale = 1.0F - ((float)fuse - partialTicks + 1.0F) / 10.0F;
            scale = Mth.clamp(scale, 0.0F, 1.0F);
            scale *= scale;
            scale *= scale;
            float scaleFactor = 1.0F + scale * 0.3F;
            poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        BlockState nitroKegState = ModBlocks.NITRO_KEG.get().defaultBlockState();
        renderBlock(nitroKegState, poseStack, buffer, packedLight, fuse / 5 % 2 == 0);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderBlock(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight, boolean flash) {
        if (flash) {
            int flashColor = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
            this.blockRenderer.renderSingleBlock(state, poseStack, buffer, packedLight, flashColor);
        } else {
            this.blockRenderer.renderSingleBlock(state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(PrimedNitroKeg entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}