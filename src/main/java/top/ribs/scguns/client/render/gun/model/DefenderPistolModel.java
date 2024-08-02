package top.ribs.scguns.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
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
public class DefenderPistolModel implements IOverrideModel {

    @SuppressWarnings("resource")
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {

        //Renders the static parts of the model.
        RenderUtil.renderModel(SpecialModels.DEFENDER_PISTOL_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);

        renderBarrelAndAttachments(stack, matrixStack, buffer, light, overlay);
        renderMagazineAttachments(stack, matrixStack, buffer, light, overlay);

        if (entity.equals(Minecraft.getInstance().player)) {
            renderAnimatedParts(stack, matrixStack, buffer, light, overlay);
        }
    }

    private void renderBarrelAndAttachments(ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        boolean hasExtendedBarrel = false;

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.EXTENDED_BARREL.get()) {
                RenderUtil.renderModel(SpecialModels.DEFENDER_PISTOL_EXT_BARREL.getModel(), stack, matrixStack, buffer, light, overlay);
                hasExtendedBarrel = true;
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.SILENCER.get())
                RenderUtil.renderModel(SpecialModels.DEFENDER_PISTOL_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.MUZZLE_BRAKE.get())
                RenderUtil.renderModel(SpecialModels.DEFENDER_PISTOL_MUZZLE_BRAKE.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.ADVANCED_SILENCER.get())
                RenderUtil.renderModel(SpecialModels.DEFENDER_PISTOL_ADVANCED_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
        }

        // Render the standard barrel if no extended barrel is attached
        if (!hasExtendedBarrel) {
            RenderUtil.renderModel(SpecialModels.DEFENDER_PISTOL_STAN_BARREL.getModel(), stack, matrixStack, buffer, light, overlay);
        }
    }

    private void renderMagazineAttachments(ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        if ((Gun.hasAttachmentEquipped(stack, IAttachment.Type.MAGAZINE))) {
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.EXTENDED_MAG.get())
                RenderUtil.renderModel(SpecialModels.DEFENDER_PISTOL_EXTENDED_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.SPEED_MAG.get())
                RenderUtil.renderModel(SpecialModels.DEFENDER_PISTOL_SPEED_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.PLUS_P_MAG.get())
                RenderUtil.renderModel(SpecialModels.DEFENDER_PISTOL_EXTENDED_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
        } else
            RenderUtil.renderModel(SpecialModels.DEFENDER_PISTOL_STANDARD_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
    }

    private void renderAnimatedParts(ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        //Always push.
        matrixStack.pushPose();
        //Don't touch this, it's better to use the display options in Blockbench.
        matrixStack.translate(0, -5.8 * 0.0625, 0);
        //Gets the cooldown tracker for the item. Items like swords and enderpearls also have this.
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
        cooldown = (float) ease(cooldown);
        /**
         * We are moving whatever part is moving.
         * X,Y,Z, use Z for moving back and forth.
         * The higher the number, the shorter the distance.
         */
        matrixStack.translate(0, 0, cooldown / 8);
        matrixStack.translate(0, 5.8 * 0.0625, 0);
        //Renders the moving part of the gun.
        RenderUtil.renderModel(SpecialModels.DEFENDER_PISTOL_RECEIVER.getModel(), stack, matrixStack, buffer, light, overlay);
        //Always pop
        matrixStack.popPose();
    }

    private double ease(double x) {
        return 1 - Math.pow(1 - (2 * x), 4);
    }
}