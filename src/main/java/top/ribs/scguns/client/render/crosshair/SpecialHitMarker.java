package top.ribs.scguns.client.render.crosshair;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.handler.CrosshairHandler;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.handler.HUDRenderHandler;

/**
 * Author: MrCrayfish
 */
public class SpecialHitMarker extends Crosshair {
    private static final ResourceLocation NORMAL_HITMARKER = new ResourceLocation(Reference.MOD_ID, "textures/crosshair/special_hit_marker.png");
    private static final ResourceLocation ALT_HITMARKER = new ResourceLocation(Reference.MOD_ID, "textures/crosshair/special_hit_marker2.png");

    public SpecialHitMarker() {
        super(new ResourceLocation(Reference.MOD_ID, "special_hit_marker"));
    }

    @Override
    public void render(Minecraft mc, PoseStack stack, int windowWidth, int windowHeight, float partialTicks) {
        stack.pushPose();

        float alpha = 1.0F; // - (float) AimingHandler.get().getNormalisedAdsProgress();
        float size = 9.0F;
        float hitMarkerProgress = HUDRenderHandler.getHitMarkerProgress(partialTicks);
        boolean crit = HUDRenderHandler.getHitMarkerCrit();
        float scale = 1.5F + (hitMarkerProgress * 1.0F);
        stack.translate(Math.round((windowWidth) / 2F) - 0.5, Math.round((windowHeight) / 2F) - 0.5, 0);
        stack.scale(scale, scale, scale);
        stack.translate(-size / 2F, -size / 2F, 0);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, (!crit ? 1.0F : 0.25F), (!crit ? 1.0F : 0.25F), 1.0F - (hitMarkerProgress * 0.3F));
        boolean useAltHitMarker = CrosshairHandler.get().getCurrentCrosshair().getLocation().equals(new ResourceLocation(Reference.MOD_ID, "hit_marker"));
        ResourceLocation texture = (useAltHitMarker ? ALT_HITMARKER : NORMAL_HITMARKER);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();

        Matrix4f matrix = stack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, 0, size, 0).uv(0, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size, size, 0).uv(1, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size, 0, 0).uv(1, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, 0, 0, 0).uv(0, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());

        stack.popPose();
    }
}

