package top.ribs.scguns.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.gun.IOverrideModel;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.attachment.IAttachment;

/**
 * Since we want to have an animation for the charging handle, we will be overriding the standard model rendering.
 * This also allows us to replace the model for the different stocks.
 */
public class VenturiModel implements IOverrideModel {
    private static final float BOLT_MOVEMENT_DISTANCE = 1.0f;

    @SuppressWarnings("resource")
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {

        // Renders the static parts of the model.
        RenderUtil.renderModel(SpecialModels.VENTURI_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);

        if (Gun.getScope(stack) == null) {
            RenderUtil.renderModel(SpecialModels.VENTURI_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        } else {
            RenderUtil.renderModel(SpecialModels.VENTURI_NO_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        }

        renderStockAttachments(stack, matrixStack, buffer, light, overlay);
        if (entity.equals(Minecraft.getInstance().player)) {
            renderAnimatedParts(stack, matrixStack, buffer, light, overlay);
        }
    }

    private void renderStockAttachments(ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        if(Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get())
                RenderUtil.renderModel(SpecialModels.VENTURI_STOCK_HEAVY.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get())
                RenderUtil.renderModel(SpecialModels.VENTURI_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get())
                RenderUtil.renderModel(SpecialModels.VENTURI_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
        } else {
            RenderUtil.renderModel(SpecialModels.VENTURI_STANDARD_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
        }
    }





    private void renderAnimatedParts(ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        matrixStack.pushPose();
        matrixStack.translate(0, -5.8 * 0.0625, 0);
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
        cooldown = (float) ease(cooldown);

        matrixStack.translate(0, 0, cooldown / 8);
        matrixStack.translate(0, 5.8 * 0.0625, 0);

        RenderUtil.renderModel(SpecialModels.VENTURI_BOLT.getModel(), stack, matrixStack, buffer, light, overlay);

        matrixStack.popPose();
    }


    private double ease(double x) {
        return 1 - Math.pow(1 - (2 * x), 4);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
    public static class GunFireEventVenturiHandler {
        private static float pumpProgress = 0.0f;

        @SubscribeEvent
        public static void onGunFire(GunFireEvent.Post event) {
            if (event.isClient()) {
                // Start the pump animation
                pumpProgress = 1.0f;
            }
        }

        public static float getPumpProgress(float partialTicks) {
            return pumpProgress > 0.0f ? pumpProgress -= partialTicks * 0.1f : 0.0f;
        }
    }
}