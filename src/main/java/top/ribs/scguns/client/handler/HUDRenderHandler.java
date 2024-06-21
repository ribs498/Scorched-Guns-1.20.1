package top.ribs.scguns.client.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HUDRenderHandler {
    private static final ResourceLocation CHARGE_BAR = new ResourceLocation(Reference.MOD_ID, "textures/gui/charging_bar.png");
    private static final ResourceLocation FILL_BAR = new ResourceLocation(Reference.MOD_ID, "textures/gui/fill_bar.png");

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.screen != null) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof GunItem)) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();

        renderGunInfoHUD(heldItem, event.getPartialTick(), poseStack, guiGraphics);
        renderChargeBarHUD(heldItem, event.getPartialTick(), poseStack, guiGraphics, player);
    }

    private static void renderGunInfoHUD(ItemStack heldItem, float partialTick, PoseStack poseStack, GuiGraphics guiGraphics) {


        if(!Config.CLIENT.display.displayGunInfo.get())
            return;

        Minecraft mc = Minecraft.getInstance();
        Gun gun = ((GunItem) heldItem.getItem()).getGun();
        CompoundTag tagCompound = heldItem.getTag();

        if (tagCompound != null) {
            int currentAmmo = tagCompound.getInt("AmmoCount");
            MutableComponent ammoCountValue = Component.literal(currentAmmo + " / " + GunModifierHelper.getModifiedAmmoCapacity(heldItem, gun)).withStyle(ChatFormatting.BOLD);
            if (Gun.hasInfiniteAmmo(heldItem))
                ammoCountValue = (Component.literal("∞ / ∞").withStyle(ChatFormatting.BOLD));
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            int ammoPosX = (int) (screenWidth * 0.88);
            int ammoPosY = (int) (screenHeight * 0.8);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            guiGraphics.drawString(mc.font, ammoCountValue, ammoPosX, ammoPosY, 0xFFFFFF);
            RenderSystem.disableBlend();
        }
    }

    private static void renderChargeBarHUD(ItemStack heldItem, float partialTick, PoseStack poseStack, GuiGraphics guiGraphics, LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);

        if (gun.getGeneral().getFireMode() != FireMode.PULSE) {
            return;
        }

        int maxChargeTime = gun.getGeneral().getFireTimer();
        int chargeTime = ChargeHandler.getChargeTime();
        float chargeRatio = (chargeTime > 0) ? (float) chargeTime / maxChargeTime : 0;

        int barWidth = 16;
        int barHeight = 16;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int barX = screenWidth / 2 + 10;
        int barY = screenHeight / 2 - barHeight / 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CHARGE_BAR);

        guiGraphics.blit(CHARGE_BAR, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        if (chargeRatio > 0) {
            int fillBarWidth = 16;
            int fillBarHeight = 16;
            int fillHeight = (int) (fillBarHeight * chargeRatio);
            int fillY = barY + (fillBarHeight - fillHeight);

            RenderSystem.setShaderTexture(0, FILL_BAR);
            // Render the fill bar vertically from bottom up
            guiGraphics.blit(FILL_BAR, barX, fillY, 0, fillBarHeight - fillHeight, fillBarWidth, fillHeight, fillBarWidth, fillBarHeight);
        }

        RenderSystem.disableBlend();
    }

}


