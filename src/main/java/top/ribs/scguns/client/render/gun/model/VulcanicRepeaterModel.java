package top.ribs.scguns.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
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
public class VulcanicRepeaterModel implements IOverrideModel {

    @SuppressWarnings("resource")
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {

        //Renders the static parts of the model.
        RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);
        //Renders the iron sights if no scope is attached.
        if ((Gun.getScope(stack) == null))
            RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        else
            RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_NO_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);



        if ((Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)))
        {
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_STOCK_HEAVY.getModel(), stack, matrixStack, buffer, light, overlay);
        }
        else
            RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_STANDARD_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.UNDER_BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.VERTICAL_GRIP.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_GRIP_VERTICAL.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.LIGHT_GRIP.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_GRIP_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.IRON_BAYONET.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_IRON_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.ANTHRALITE_BAYONET.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_ANTHRALITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.DIAMOND_BAYONET.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_DIAMOND_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.NETHERITE_BAYONET.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_NETHERITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
        }
        if ((Gun.hasAttachmentEquipped(stack, IAttachment.Type.MAGAZINE)))
        {
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.EXTENDED_MAG.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_EXTENDED_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.PLUS_P_MAG.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_EXTENDED_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.SPEED_MAG.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_SPEED_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
        }
        else
            RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_STANDARD_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
        if ((Gun.hasAttachmentEquipped(stack, IAttachment.Type.BARREL)))
        {
            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.ADVANCED_SILENCER.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_ADVANCED_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.SILENCER.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.MUZZLE_BRAKE.get())
                RenderUtil.renderModel(SpecialModels.VULCANIC_REPEATER_MUZZLE_BRAKE.getModel(), stack, matrixStack, buffer, light, overlay);
        }


    }
}