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
public class RatKingAndQueenModel implements IOverrideModel {

    @SuppressWarnings("resource")
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        // Renders the static parts of the model.
        RenderUtil.renderModel(SpecialModels.RAT_KING_AND_QUEEN_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.SILENCER.get())
                RenderUtil.renderModel(SpecialModels.RAT_KING_AND_QUEEN_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.MUZZLE_BRAKE.get())
                RenderUtil.renderModel(SpecialModels.RAT_KING_AND_QUEEN_MUZZLE_BRAKE.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.ADVANCED_SILENCER.get())
                RenderUtil.renderModel(SpecialModels.RAT_KING_AND_QUEEN_ADVANCED_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
        }

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.MAGAZINE)) {
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.EXTENDED_MAG.get())
                RenderUtil.renderModel(SpecialModels.RAT_KING_AND_QUEEN_EXTENDED_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.SPEED_MAG.get())
                RenderUtil.renderModel(SpecialModels.RAT_KING_AND_QUEEN_SPEED_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.PLUS_P_MAG.get())
                RenderUtil.renderModel(SpecialModels.RAT_KING_AND_QUEEN_EXTENDED_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
        } else {
            RenderUtil.renderModel(SpecialModels.RAT_KING_AND_QUEEN_STANDARD_MAG.getModel(), stack, matrixStack, buffer, light, overlay);
        }

        if (entity.equals(Minecraft.getInstance().player)) {
            matrixStack.pushPose();
            matrixStack.translate(0, -5.8 * 0.0625, 0);

            ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
            cooldown = (float) ease(cooldown);

            int shotCount = GunFireEventRatHandler.getShotCount();

            // Render Receiver 1
            matrixStack.pushPose();
            if (shotCount % 2 == 0 && cooldown > 0) {
                matrixStack.translate(0, 0, cooldown / 8);
            }
            matrixStack.translate(0, 5.8 * 0.0625, 0);
            RenderUtil.renderModel(SpecialModels.RAT_KING_AND_QUEEN_RECEIVER_1.getModel(), stack, matrixStack, buffer, light, overlay);
            matrixStack.popPose();

            // Render Receiver 2
            matrixStack.pushPose();
            if (shotCount % 2 == 1 && cooldown > 0) {
                matrixStack.translate(0, 0, cooldown / 8);
            }
            matrixStack.translate(0, 5.8 * 0.0625, 0);
            RenderUtil.renderModel(SpecialModels.RAT_KING_AND_QUEEN_RECEIVER_2.getModel(), stack, matrixStack, buffer, light, overlay);
            matrixStack.popPose();

            matrixStack.popPose();
        }
    }

    private double ease(double x) {
        return 1 - Math.pow(1 - (2 * x), 4);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
    public static class GunFireEventRatHandler {

        private static int shotCount = 0;

        @SubscribeEvent
        public static void onGunFire(GunFireEvent.Post event) {
            if (event.isClient()) {
                shotCount++;
            }
        }

        public static int getShotCount() {
            return shotCount;
        }
    }
}
