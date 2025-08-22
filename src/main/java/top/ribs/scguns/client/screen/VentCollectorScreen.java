package top.ribs.scguns.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class VentCollectorScreen extends AbstractContainerScreen<VentCollectorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("scguns", "textures/gui/vent_collector.png");

    public VentCollectorScreen(VentCollectorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int filterCharge = menu.getFilterCharge();
        int barWidth = (int)(((float)filterCharge / 64) * 52);
        guiGraphics.blit(TEXTURE, x + 62, y + 37, 176, 0, barWidth, 8);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        int startX = (this.width - this.imageWidth) / 2 + 62;
        int startY = (this.height - this.imageHeight) / 2 + 37;
        if (x >= startX && x < startX + 52 && y >= startY && y < startY + 8) {
            int filterCharge = menu.getFilterCharge();
            guiGraphics.renderTooltip(this.font, Component.translatable("gui.scguns.vent_collector.filter_charge", filterCharge, 64), x, y);
        }
    }
}