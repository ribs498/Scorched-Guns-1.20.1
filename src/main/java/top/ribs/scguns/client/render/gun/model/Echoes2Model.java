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
public class Echoes2Model implements IOverrideModel {

    @SuppressWarnings("resource")
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        if ((Gun.getScope(stack) == null))
            RenderUtil.renderModel(SpecialModels.ECHOES_2_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        else
            RenderUtil.renderModel(SpecialModels.ECHOES_2_NO_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);

        // Renders the static parts of the model.
        RenderUtil.renderModel(SpecialModels.ECHOES_2_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get())
                RenderUtil.renderModel(SpecialModels.SCULK_RESONATOR_STOCK_HEAVY.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get())
                RenderUtil.renderModel(SpecialModels.SCULK_RESONATOR_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get())
                RenderUtil.renderModel(SpecialModels.SCULK_RESONATOR_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
        }

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.UNDER_BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.VERTICAL_GRIP.get())
                RenderUtil.renderModel(SpecialModels.ECHOES_2_GRIP_VERTICAL.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.LIGHT_GRIP.get())
                RenderUtil.renderModel(SpecialModels.ECHOES_2_GRIP_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.IRON_BAYONET.get())
                RenderUtil.renderModel(SpecialModels.PLASGUN_IRON_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.ANTHRALITE_BAYONET.get())
                RenderUtil.renderModel(SpecialModels.PLASGUN_ANTHRALITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.DIAMOND_BAYONET.get())
                RenderUtil.renderModel(SpecialModels.PLASGUN_DIAMOND_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.NETHERITE_BAYONET.get())
                RenderUtil.renderModel(SpecialModels.PLASGUN_NETHERITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
        }

        if (entity.equals(Minecraft.getInstance().player)) {
            // Always push.
            matrixStack.pushPose();
            // Don't touch this, it's better to use the display options in Blockbench.
            matrixStack.translate(0, -5.8 * 0.0625, 0);
            // Gets the cooldown tracker for the item. Items like swords and enderpearls also have this.
            ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
            cooldown = (float) ease(cooldown);
            // Apply transformations to the barrel and attached components
            matrixStack.translate(0, 0, cooldown / 12);
            matrixStack.translate(0, 5.8 * 0.0625, 0);
            // Render the barrel and its attachments
            renderBarrelAndAttachments(stack, matrixStack, buffer, light, overlay);
            // Always pop
            matrixStack.popPose();
        }
    }

    private void renderBarrelAndAttachments(ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        RenderUtil.renderModel(SpecialModels.ECHOES_2_BARRELS.getModel(), stack, matrixStack, buffer, light, overlay);

    }

    private double ease(double x) {
        return 1 - Math.pow(1 - (2 * x), 4);
    }
}
