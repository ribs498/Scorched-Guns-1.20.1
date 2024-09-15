package top.ribs.scguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import top.ribs.scguns.block.GunShelfBlock;
import top.ribs.scguns.blockentity.GunShelfBlockEntity;

public class GunShelfRenderer implements BlockEntityRenderer<GunShelfBlockEntity> {
    private final ItemRenderer itemRenderer;

    public GunShelfRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(GunShelfBlockEntity tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        ItemStack displayedItem = tile.getDisplayedItem();
        if (displayedItem != null && !displayedItem.isEmpty()) {
            matrixStackIn.pushPose();
            try {
                Direction facing = tile.getBlockState().getValue(GunShelfBlock.FACING);

                // Adjust position based on facing direction
                switch (facing) {
                    case NORTH -> matrixStackIn.translate(0.5, 0.4, 0.8f);
                    case SOUTH -> matrixStackIn.translate(0.5, 0.4, 0.2);
                    case EAST -> matrixStackIn.translate(0.2, 0.4, 0.5);
                    case WEST -> matrixStackIn.translate(0.8f, 0.4, 0.5);
                }

                // Rotate to face outward
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));

                // Scale down slightly to fit on the shelf
                matrixStackIn.scale(0.55f, 0.55f, 0.55f);

                // Render item
                BakedModel model = itemRenderer.getModel(displayedItem, tile.getLevel(), null, 0);
                itemRenderer.render(displayedItem, ItemDisplayContext.FIXED, false, matrixStackIn,
                        bufferIn, combinedLightIn, combinedOverlayIn, model);
            } finally {
                matrixStackIn.popPose();
            }
        }
    }
}