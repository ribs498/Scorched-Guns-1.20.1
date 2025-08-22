package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModEffects;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SulfurHeartsEventHandler {

    private static final ResourceLocation SULFUR_FULL = new ResourceLocation("scguns", "textures/gui/heart/sulfur_poisoned_full.png");
    private static final ResourceLocation SULFUR_HALF = new ResourceLocation("scguns", "textures/gui/heart/sulfur_poisoned_half.png");
    private static final ResourceLocation SULFUR_FULL_BLINKING = new ResourceLocation("scguns", "textures/gui/heart/sulfur_poisoned_full_blinking.png");
    private static final ResourceLocation SULFUR_HALF_BLINKING = new ResourceLocation("scguns", "textures/gui/heart/sulfur_poisoned_half_blinking.png");
    private static final ResourceLocation SULFUR_FULL_HARDCORE = new ResourceLocation("scguns", "textures/gui/heart/sulfur_poisoned_full_hardcore.png");
    private static final ResourceLocation SULFUR_HALF_HARDCORE = new ResourceLocation("scguns", "textures/gui/heart/sulfur_poisoned_half_hardcore.png");
    private static final ResourceLocation SULFUR_FULL_BLINKING_HARDCORE = new ResourceLocation("scguns", "textures/gui/heart/sulfur_poisoned_full_blinking_hardcore.png");
    private static final ResourceLocation SULFUR_HALF_BLINKING_HARDCORE = new ResourceLocation("scguns", "textures/gui/heart/sulfur_poisoned_half_blinking_hardcore.png");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.options.hideGui) {
            return;
        }

        MobEffectInstance sulfurEffect = player.getEffect(ModEffects.SULFUR_POISONING.get());
        if (sulfurEffect == null) {
            return;
        }

        int amplifier = sulfurEffect.getAmplifier();
        int duration = sulfurEffect.getDuration();

        float alpha = Math.min(1.0f, duration / 100.0f);
        alpha = Math.min(1.0f, alpha + (amplifier * 0.2f));

        if (alpha <= 0.1f) return;

        int health = Mth.ceil(player.getHealth());
        int maxHealth = Mth.ceil(player.getMaxHealth());

        if (health <= 0 || maxHealth <= 0) return;

        boolean isHardcore = player.level().getLevelData().isHardcore();
        long currentTime = System.currentTimeMillis();
        boolean isBlinking = (currentTime / 250) % 2 == 0;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int left = screenWidth / 2 - 91;
        int top = screenHeight - 39;

        guiGraphics.pose().pushPose();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, alpha);

        renderSulfurHeartOverlay(guiGraphics, left, top, health, maxHealth, isHardcore, isBlinking);

        guiGraphics.pose().popPose();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderSulfurHeartOverlay(GuiGraphics guiGraphics, int x, int y, int health, int maxHealth, boolean hardcore, boolean blinking) {
        int heartsPerRow = 10;
        int fullHearts = health / 2;
        boolean hasHalfHeart = (health % 2) == 1;

        for (int row = 0; row < (maxHealth + 19) / 20; row++) {
            int heartsInThisRow = Math.min(heartsPerRow, (maxHealth / 2) - row * heartsPerRow);
            if (heartsInThisRow <= 0) break;
            for (int col = 0; col < heartsInThisRow; col++) {
                int heartIndex = row * heartsPerRow + col;
                int heartX = x + col * 8;
                int heartY = y - row * 10;
                if (heartIndex >= fullHearts && !(heartIndex == fullHearts && hasHalfHeart)) {
                    continue;
                }
                ResourceLocation textureToUse = getSulfurHeartTexture(heartIndex, fullHearts, hasHalfHeart, hardcore, blinking);
                guiGraphics.blit(textureToUse, heartX, heartY, 0, 0, 9, 9, 9, 9);
            }
        }
    }

    private static ResourceLocation getSulfurHeartTexture(int heartIndex, int fullHearts, boolean hasHalfHeart, boolean hardcore, boolean blinking) {
        boolean isFullHeart = heartIndex < fullHearts;
        boolean isHalfHeart = heartIndex == fullHearts && hasHalfHeart;

        if (isFullHeart) {
            if (hardcore) {
                return blinking ? SULFUR_FULL_BLINKING_HARDCORE : SULFUR_FULL_HARDCORE;
            } else {
                return blinking ? SULFUR_FULL_BLINKING : SULFUR_FULL;
            }
        } else if (isHalfHeart) {
            if (hardcore) {
                return blinking ? SULFUR_HALF_BLINKING_HARDCORE : SULFUR_HALF_HARDCORE;
            } else {
                return blinking ? SULFUR_HALF_BLINKING : SULFUR_HALF;
            }
        }
        return SULFUR_FULL;
    }
}