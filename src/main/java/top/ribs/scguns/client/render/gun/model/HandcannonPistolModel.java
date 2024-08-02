package top.ribs.scguns.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.gun.IOverrideModel;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.attachment.IAttachment;

/**
 * Since we want to have an animation for the charging handle, we will be overriding the standard model rendering.
 * This also allows us to replace the model for the different stocks.
 */
public class HandcannonPistolModel implements IOverrideModel {
    private static final int FLASH_DURATION = 10;
    private int flashTimer = 0;
    @SuppressWarnings("resource")
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        RenderUtil.renderModel(SpecialModels.HANDCANNON_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get())
                RenderUtil.renderModel(SpecialModels.FLINTLOCK_PISTOL_STOCK_WEIGHTED.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get())
                RenderUtil.renderModel(SpecialModels.FLINTLOCK_PISTOL_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get())
                RenderUtil.renderModel(SpecialModels.FLINTLOCK_PISTOL_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
        }

        if (entity.equals(Minecraft.getInstance().player)) {
            matrixStack.pushPose();
            matrixStack.translate(0, -0.5, 0.23);
            ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
            cooldown = (float) ease(cooldown);
            float rotationAngle = -cooldown * 45;
            matrixStack.mulPose(Axis.XP.rotationDegrees(rotationAngle));
            matrixStack.translate(0, 0.5, -0.23);
            RenderUtil.renderModel(SpecialModels.FLINTLOCK_PISTOL_HAMMER.getModel(), stack, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
            if (cooldown >= 0.9f) {
                flashTimer = FLASH_DURATION;
            }
        }

        if (flashTimer > 0) {
            matrixStack.pushPose();
            matrixStack.translate(0, -0.0, -0.23);
            RenderUtil.renderModel(SpecialModels.MUSKET_FLASH.getModel(), stack, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
            flashTimer--;
        }
    }

    private double ease(double x) {
        return 1 - Math.pow(1 - x, 4);
    }
}