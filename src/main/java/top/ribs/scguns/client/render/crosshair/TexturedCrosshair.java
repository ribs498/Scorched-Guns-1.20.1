package top.ribs.scguns.client.render.crosshair;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import top.ribs.scguns.client.handler.AimingHandler;

/**
 * Author: MrCrayfish
 */
public class TexturedCrosshair extends Crosshair
{
    private final ResourceLocation texture;
    private final boolean blend;

    public TexturedCrosshair(ResourceLocation id)
    {
        this(id, true);
    }

    public TexturedCrosshair(ResourceLocation id, boolean blend)
    {
        super(id);
        this.texture = new ResourceLocation(id.getNamespace(), "textures/crosshair/" + id.getPath() + ".png");
        this.blend = blend;
    }

    @Override
    public void render(Minecraft mc, PoseStack stack, int windowWidth, int windowHeight, float partialTicks)
    {
        stack.pushPose();

        float alpha = 1.0F - (float) AimingHandler.get().getNormalisedAdsProgress();
        float size = 8.0F;
        stack.translate((windowWidth - size) / 2F, (windowHeight - size) / 2F, 0);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.texture);
        RenderSystem.enableBlend();

        if(this.blend)
        {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        Matrix4f matrix = stack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, 0, size, 0).uv(0, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size, size, 0).uv(1, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, size, 0, 0).uv(1, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, 0, 0, 0).uv(0, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());

        if(this.blend)
        {
            RenderSystem.defaultBlendFunc();
        }

        stack.popPose();
    }
}