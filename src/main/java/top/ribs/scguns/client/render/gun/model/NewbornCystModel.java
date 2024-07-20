package top.ribs.scguns.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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

import java.util.HashMap;
import java.util.Map;

public class NewbornCystModel implements IOverrideModel {
    private static final int TOTAL_SHOTS = 7;
    private static final float ROTATION_INCREMENT = 360.0f / TOTAL_SHOTS;
    private float currentRotation = 0.0f;
    private float targetRotation = 0.0f;

    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);
        renderStockAttachments(matrixStack, buffer, stack, light, overlay);
        renderBarrelAttachments(matrixStack, buffer, stack, light, overlay);
        renderUnderBarrelAttachments(matrixStack, buffer, stack, light, overlay);
        if ((Gun.getScope(stack) == null))
            RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        else
            RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_NO_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);

        assert entity != null;
        if (entity.equals(Minecraft.getInstance().player)) {
            renderBoltAndMagazine(matrixStack, buffer, stack, partialTicks, light, overlay);
        }
    }

    private void renderStockAttachments(PoseStack matrixStack, MultiBufferSource buffer, ItemStack stack, int light, int overlay) {
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WOODEN_STOCK.get()) {
                RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get()) {
                RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get()) {
                RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_STOCK_HEAVY.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }
    }

    private void renderBarrelAttachments(PoseStack matrixStack, MultiBufferSource buffer, ItemStack stack, int light, int overlay) {
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.SILENCER.get()) {
                RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            }
            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.MUZZLE_BRAKE.get()) {
                RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_MUZZLE_BRAKE.getModel(), stack, matrixStack, buffer, light, overlay);
            }
            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.ADVANCED_SILENCER.get()) {
                RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_ADVANCED_SILENCER.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }
    }
    private void renderUnderBarrelAttachments(PoseStack matrixStack, MultiBufferSource buffer, ItemStack stack, int light, int overlay) {
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.UNDER_BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.VERTICAL_GRIP.get()) {
                RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_TACT_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.LIGHT_GRIP.get()) {
                RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_LIGHT_GRIP.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.IRON_BAYONET.get()) {
                RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_IRON_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.ANTHRALITE_BAYONET.get()) {
                RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_ANTHRALITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.DIAMOND_BAYONET.get()) {
                RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_DIAMOND_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.NETHERITE_BAYONET.get()) {
                RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_NETHERITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }
    }
    private void renderBoltAndMagazine(PoseStack matrixStack, MultiBufferSource buffer, ItemStack stack, float partialTicks, int light, int overlay) {
        assert Minecraft.getInstance().player != null;
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
        int shotCount = GunFireCystEventHandler.getShotCount();
        targetRotation = shotCount * ROTATION_INCREMENT;

        // Interpolate rotation
        currentRotation = currentRotation + (targetRotation - currentRotation) * partialTicks;

        matrixStack.pushPose();
        matrixStack.translate(0, -5.8 * 0.0625, 0);
        if (cooldown > 0) {
            matrixStack.translate(0, 0, cooldown / 8);
        }
        matrixStack.translate(0, 5.8 * 0.0625, 0);
        matrixStack.popPose();
        renderMagazineRotation(matrixStack, buffer, stack, light, overlay);
    }

    private void renderMagazineRotation(PoseStack matrixStack, MultiBufferSource buffer, ItemStack stack, int light, int overlay) {
        matrixStack.pushPose();
        matrixStack.translate(0, -0.21, 0);
        matrixStack.mulPose(Axis.ZP.rotationDegrees(currentRotation));
        matrixStack.translate(-0, 0.21, -0);
        RenderUtil.renderModel(SpecialModels.NEWBORN_CYST_DRUM.getModel(), stack, matrixStack, buffer, light, overlay);
        matrixStack.popPose();
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
    public static class GunFireCystEventHandler {

        private static int shotCount = 0;

        @SubscribeEvent
        public static void onGunFire(GunFireEvent.Post event) {
            if (event.isClient()) {
                shotCount++;
                shotCount %= TOTAL_SHOTS; // Ensure shotCount is always within the bounds of the drum capacity
            }
        }

        public static int getShotCount() {
            return shotCount;
        }
    }
}


