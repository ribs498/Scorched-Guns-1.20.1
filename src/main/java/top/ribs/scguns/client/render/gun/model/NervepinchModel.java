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

public class NervepinchModel implements IOverrideModel {

    @SuppressWarnings("resource")
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {

        //Renders the static parts of the model.
        RenderUtil.renderModel(SpecialModels.NERVEPINCH_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);

        //Renders the iron sights if no scope is attached.
        if ((Gun.getScope(stack) == null))
            RenderUtil.renderModel(SpecialModels.NERVEPINCH_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        else
            RenderUtil.renderModel(SpecialModels.NERVEPINCH_NO_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get())
                RenderUtil.renderModel(SpecialModels.NERVEPINCH_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get())
                RenderUtil.renderModel(SpecialModels.NERVEPINCH_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get())
                RenderUtil.renderModel(SpecialModels.NERVEPINCH_STOCK_HEAVY.getModel(), stack, matrixStack, buffer, light, overlay);
        }
        // Render magazine attachments
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.MAGAZINE)) {
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.EXTENDED_MAG.get())
                RenderUtil.renderModel(SpecialModels.NERVEPINCH_EXT_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.PLUS_P_MAG.get())
                RenderUtil.renderModel(SpecialModels.NERVEPINCH_EXT_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.SPEED_MAG.get())
                RenderUtil.renderModel(SpecialModels.NERVEPINCH_SPEED_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
        } else {
            RenderUtil.renderModel(SpecialModels.NERVEPINCH_STAN_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
        }
    }

    private double ease(double x) {

        return 1 - Math.pow(1 - (2 * x), 4);

    }

}