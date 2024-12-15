package top.ribs.scguns.client.render.gun.animated;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import top.ribs.scguns.item.animated.AnimatedGunItem;


public class AttachmentRenderer extends GeoRenderLayer<AnimatedGunItem> {
    protected ItemStack currentItemStack;
    private static ResourceLocation model_resource = null;
    private static ResourceLocation texture_resource = null;
    private static ItemStack itemStack = null;

    public AttachmentRenderer(GeoRenderer entityRendererIn) {
        super(entityRendererIn);
    }

    public void updateAttachment(ItemStack attachmentStack) {
        itemStack = attachmentStack;
        model_resource = new ResourceLocation("scguns", "geo/item/attachment/" + attachmentStack.getItem() + ".geo.json");
        texture_resource = new ResourceLocation("scguns", "textures/animated/attachment/" + attachmentStack.getItem() + ".png");

    }

    public void renderForBone(PoseStack poseStack, AnimatedGunItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (model_resource != null && texture_resource != null && itemStack != null) {
          AnimatedGunModel focusModel = new AnimatedGunModel(model_resource);
            ResourceLocation focusModelModelResource = model_resource;
            RenderType focusModelRenderLayer = RenderType.entityTranslucent(texture_resource);
            if (bone.getName().matches("attachment_bone") && !bone.getName().matches(itemStack.getItem().toString()) && itemStack != null) {
                this.getRenderer().reRender(focusModel.getBakedModel(focusModelModelResource), poseStack, bufferSource, animatable, focusModelRenderLayer, bufferSource.getBuffer(focusModelRenderLayer), partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

    }
}
