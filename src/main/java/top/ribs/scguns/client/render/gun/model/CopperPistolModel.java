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
    public class CopperPistolModel implements IOverrideModel {

        @SuppressWarnings("resource")
        @Override
        public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {

            RenderUtil.renderModel(SpecialModels.COPPER_PISTOL_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.BARREL)) {
                if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.SILENCER.get())
                    RenderUtil.renderModel(SpecialModels.COPPER_PISTOL_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
                if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.MUZZLE_BRAKE.get())
                    RenderUtil.renderModel(SpecialModels.COPPER_PISTOL_MUZZLE_BRAKE.getModel(), stack, matrixStack, buffer, light, overlay);
                if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.ADVANCED_SILENCER.get())
                    RenderUtil.renderModel(SpecialModels.COPPER_PISTOL_ADVANCED_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            }
            if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
                if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get())
                    RenderUtil.renderModel(SpecialModels.COPPER_PISTOL_STOCK_HEAVY.getModel(), stack, matrixStack, buffer, light, overlay);
                if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get())
                    RenderUtil.renderModel(SpecialModels.COPPER_PISTOL_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
                if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get())
                    RenderUtil.renderModel(SpecialModels.COPPER_PISTOL_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
            }
            if (entity.equals(Minecraft.getInstance().player)) {
                matrixStack.pushPose();
                matrixStack.translate(0, -5.8 * 0.0625, 0);
                ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
                float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
                cooldown = (float) ease(cooldown);
                matrixStack.translate(0, 0, cooldown / 16);
                matrixStack.translate(0, 5.8 * 0.0625, 0);
                RenderUtil.renderModel(SpecialModels.COPPER_PISTOL_BOLT.getModel(), stack, matrixStack, buffer, light, overlay);
                matrixStack.popPose();
            }
            // Render the magazine and adjust its position based on ammo count.
            float magazinePosition = calculateMagazinePosition(stack);
            float translationMultiplier = 0.25f; // Adjust this value to fine-tune the end position
            matrixStack.pushPose();
            matrixStack.translate(clampMagazinePosition(magazinePosition * translationMultiplier), 0, 0);
            RenderUtil.renderModel(SpecialModels.COPPER_PISTOL_MAGAZINE.getModel(), stack, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
        }

        private double ease(double x) {
            return 1 - Math.pow(1 - (2 * x), 4);
        }
        private float calculateMagazinePosition(ItemStack stack) {
            int maxAmmo = Gun.getMaxAmmo(stack);
            int currentAmmo = Gun.getAmmoCount(stack);
            return Math.min((maxAmmo - currentAmmo) / (float) maxAmmo, 1.0f);
        }

        private float clampMagazinePosition(float position) {
            return Math.max(0, Math.min(position, 1.0f));
        }
    }