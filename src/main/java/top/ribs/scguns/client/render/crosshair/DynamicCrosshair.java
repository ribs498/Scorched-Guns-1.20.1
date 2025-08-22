package top.ribs.scguns.client.render.crosshair;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.common.ChargeHandler;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.SpreadTracker;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.util.GunCompositeStatHelper;

//Thanks to Miga!
public class DynamicCrosshair extends Crosshair {
    private static final ResourceLocation DYNAMIC_CROSSHAIR_H = new ResourceLocation("scguns", "textures/crosshair/dynamic_horizontal.png");
    private static final ResourceLocation DYNAMIC_CROSSHAIR_V = new ResourceLocation("scguns", "textures/crosshair/dynamic_vertical.png");
    private static final ResourceLocation DOT_CROSSHAIR = new ResourceLocation("scguns", "textures/crosshair/dot.png");
    private float scale;
    private float prevScale;
    private float fireBloom;
    private float prevFireBloom;

    public DynamicCrosshair() {
        super(new ResourceLocation("scguns", "dynamic"));
    }

    public void tick() {
        this.prevScale = this.scale;
        this.scale *= 0.7F;
        this.prevFireBloom = this.fireBloom;
        if (this.fireBloom > 0.0F) {
            float i = (float) Config.COMMON.projectileSpread.spreadThreshold.get() / 50.0F;
            this.fireBloom -= Math.min(2.0F / Math.max(i, 1.0F), this.fireBloom);
        }
    }
    public void onGunFired() {
        this.prevScale = 0.0F;
        this.scale = 1.2F;
        this.fireBloom = 5.0F;
    }
    private float getCurrentChargeProgress(Player player, ItemStack weapon, Gun modifiedGun) {
        int maxChargeTime = modifiedGun.getGeneral().getFireTimer();
        int chargeTime = ChargeHandler.getChargeTime(player.getUUID());

        if (maxChargeTime > 0 && chargeTime > 0) {
            return Math.min(1.0f, (float) chargeTime / maxChargeTime);
        }

        return 0.0f;
    }

    private float calculateChargeSpreadMultiplier(float chargeProgress, Gun modifiedGun) {
        chargeProgress = Mth.clamp(chargeProgress, 0.0f, 1.0f);

        float weaponSpreadPenalty = 3.0f;

        float minSpreadMultiplier = 1.0f + weaponSpreadPenalty;
        float maxSpreadMultiplier = 0.15f;

        float curveValue = chargeProgress * chargeProgress * chargeProgress;
        return minSpreadMultiplier - (minSpreadMultiplier - maxSpreadMultiplier) * curveValue;
    }
    public void render(Minecraft mc, PoseStack stack, int windowWidth, int windowHeight, float partialTicks) {
        float alpha = 1.0F;
        float size1 = 7.0F;
        float size2 = 1.0F;
        float spread = 0.0F;
        float scaleMultiplier = 2.0F;
        boolean renderDot = false;
        float finalSpreadTranslate;

        if (mc.player != null) {
            ItemStack heldItem = mc.player.getMainHandItem();
            Item var14 = heldItem.getItem();
            if (var14 instanceof GunItem) {
                GunItem gun = (GunItem)heldItem.getItem();
                Gun modifiedGun = gun.getModifiedGun(heldItem);

                if (Gun.hasLaserSight(heldItem)) {
                    return;
                }

                finalSpreadTranslate = (float) AimingHandler.get().getNormalisedAdsProgress();
                float sprintTransition = GunRenderingHandler.get().getSprintTransition(Minecraft.getInstance().getFrameTime());
                float spreadCount = SpreadTracker.get(mc.player).getNextSpread(gun, finalSpreadTranslate);
                float spreadModifier = (spreadCount + 1.0F / Math.max((float) Config.COMMON.projectileSpread.maxCount.get(), 1.0F)) * Math.min(Mth.lerp(partialTicks, this.prevFireBloom, this.fireBloom), 1.0F);
                spreadModifier = (float)Mth.lerp((double)sprintTransition * 0.5, spreadModifier, 1.0);

                float baseSpread = GunCompositeStatHelper.getCompositeSpread(heldItem, modifiedGun);
                float minSpread = modifiedGun.getGeneral().isAlwaysSpread() ? baseSpread : 0.0F;
                if (modifiedGun.getGeneral().getFireMode() == FireMode.PULSE) {
                    float chargeProgress = getCurrentChargeProgress(mc.player, heldItem, modifiedGun);
                    float chargeSpreadMultiplier = calculateChargeSpreadMultiplier(chargeProgress, modifiedGun);
                    baseSpread *= chargeSpreadMultiplier;
                    minSpread *= chargeSpreadMultiplier;
                }

                float aimingSpreadMultiplier = Mth.lerp(finalSpreadTranslate, 1.0F, 0.5F);
                spread = Math.max(Mth.lerp(spreadModifier, minSpread, baseSpread) * aimingSpreadMultiplier, 0.0F);

                DotRenderMode dotRenderMode = Config.CLIENT.display.dynamicCrosshairDotMode.get();
                renderDot = dotRenderMode == DotRenderMode.ALWAYS ||
                        dotRenderMode == DotRenderMode.AT_MIN_SPREAD && SpreadTracker.get(mc.player).getNextSpread(gun, finalSpreadTranslate) * spreadModifier <= 0.0F && (double)spread <= (Double)Config.CLIENT.display.dynamicCrosshairDotThreshold.get() ||
                        dotRenderMode == DotRenderMode.THRESHOLD && (double)spread <= Config.CLIENT.display.dynamicCrosshairDotThreshold.get() && (!(Boolean)Config.CLIENT.display.onlyRenderDotWhileAiming.get() || finalSpreadTranslate > 0.9F);
            }
        }

        // Rest of the render method remains unchanged...
        float baseScale = 1.0F + Mth.lerp(partialTicks, this.prevScale, this.scale) * scaleMultiplier;
        float adjustedSpread = spread > 1.0F ? (float)(1.0F + Math.log(spread) / 1.5) : spread;
        float fireBloomScale = Mth.lerp(partialTicks, this.prevScale, this.scale) * scaleMultiplier;
        float spreadScale = (float)((double)adjustedSpread * 2.0 * Config.CLIENT.display.dynamicCrosshairSpreadMultiplier.get());
        float scale = baseScale + spreadScale + fireBloomScale;
        scale = Math.min(scale, (float)(double)Config.CLIENT.display.dynamicCrosshairMaxScale.get() + fireBloomScale);
        float scaleSize = scale / 6.0F + 1.15F;
        float crosshairBaseTightness = (float)(0.8 - Config.CLIENT.display.dynamicCrosshairBaseSpread.get() / 2.0);
        finalSpreadTranslate = (float)(Mth.lerp(0.95, scaleSize - 1.0F, Math.log(scaleSize)) * 2.799999952316284);
        double windowCenteredX = (double)Math.round((float)windowWidth / 2.0F) - 0.5;
        double windowCenteredY = (double)Math.round((float)windowHeight / 2.0F) - 0.5;
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        stack.pushPose();
        Matrix4f matrix = stack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR_H);
        stack.translate(windowCenteredX, windowCenteredY, 0.0);
        stack.scale(scaleSize, 1.0F, 1.0F);
        stack.translate(-size1 / 2.0F - finalSpreadTranslate + crosshairBaseTightness - 0.0F, -size2 / 2.0F, 0.0F);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, 0.0F, size2, 0.0F).uv(0.0F, 0.11111111F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size1, size2, 0.0F).uv(1.0F, 0.11111111F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size1, 0.0F, 0.0F).uv(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).uv(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        stack.popPose();
        stack.pushPose();
        matrix = stack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR_H);
        stack.translate(windowCenteredX, windowCenteredY, 0.0);
        stack.scale(scaleSize, 1.0F, 1.0F);
        stack.translate(-size1 / 2.0F + finalSpreadTranslate - crosshairBaseTightness, -size2 / 2.0F, 0.0F);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, 0.0F, size2, 0.0F).uv(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size1, size2, 0.0F).uv(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size1, 0.0F, 0.0F).uv(1.0F, 0.8888889F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).uv(0.0F, 0.8888889F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        stack.popPose();
        stack.pushPose();
        matrix = stack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR_V);
        stack.translate(windowCenteredX, windowCenteredY, 0.0);
        stack.scale(1.0F, scaleSize, 1.0F);
        stack.translate(-size2 / 2.0F, -size1 / 2.0F - finalSpreadTranslate + crosshairBaseTightness, 0.0F);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, 0.0F, size1, 0.0F).uv(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size2, size1, 0.0F).uv(0.11111111F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size2, 0.0F, 0.0F).uv(0.11111111F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).uv(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        stack.popPose();
        stack.pushPose();
        matrix = stack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR_V);
        stack.translate(windowCenteredX, windowCenteredY, 0.0);
        stack.scale(1.0F, scaleSize, 1.0F);
        stack.translate(-size2 / 2.0F - 0.0F, -size1 / 2.0F + finalSpreadTranslate - crosshairBaseTightness, 0.0F);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, 0.0F, size1, 0.0F).uv(0.8888889F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size2, size1, 0.0F).uv(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size2, 0.0F, 0.0F).uv(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).uv(0.8888889F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        stack.popPose();
        if (renderDot) {
            stack.pushPose();
            int dotSize = 9;
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DOT_CROSSHAIR);
            matrix = stack.last().pose();
            stack.translate(windowCenteredX, windowCenteredY, 0.0);
            stack.translate((float)(-dotSize) / 2.0F, (float)(-dotSize) / 2.0F, 0.0F);
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0.0F, (float)dotSize, 0.0F).uv(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, (float)dotSize, (float)dotSize, 0.0F).uv(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, (float)dotSize, 0.0F, 0.0F).uv(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).uv(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
            stack.popPose();
        }

        RenderSystem.defaultBlendFunc();
    }
}