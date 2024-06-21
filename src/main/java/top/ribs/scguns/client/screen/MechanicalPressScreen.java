package top.ribs.scguns.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import top.ribs.scguns.Reference;

public class MechanicalPressScreen extends AbstractContainerScreen<MechanicalPressMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Reference.MOD_ID, "textures/gui/mechanical_press_gui.png");

    public MechanicalPressScreen(MechanicalPressMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY = 6;
    }
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        renderProgressArrow(guiGraphics, x, y);
        renderFuelBar(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            int progress = this.menu.getScaledProgress();
            guiGraphics.blit(TEXTURE, x + 84, y + 25, 176, 14, progress + 1, 16);
        }
    }

    private void renderFuelBar(GuiGraphics guiGraphics, int x, int y) {
        if (menu.hasFuel()) {
            int fuel = this.menu.getScaledFuelProgress();
            guiGraphics.blit(TEXTURE, x + 86, y + 58 - fuel, 176, 12 - fuel, 14, fuel + 1);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
