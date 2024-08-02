package top.ribs.scguns.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import top.ribs.scguns.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PoweredMaceratorScreen extends AbstractContainerScreen<PoweredMaceratorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Reference.MOD_ID, "textures/gui/powered_macerator_gui.png");
    private static final int BAR_WIDTH = 7;
    private static final int BAR_HEIGHT = 42;
    private static final int BAR_X = 14; // Adjust as needed
    private static final int BAR_Y = 21; // Adjust as needed
    private static final int TEXTURE_BAR_X = 176;
    private static final int TEXTURE_BAR_Y = 32; // Adjust as needed

    public PoweredMaceratorScreen(PoweredMaceratorMenu menu, Inventory inv, Component title) {
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
        renderEnergyBar(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            int progress = this.menu.getScaledProgress();
            guiGraphics.blit(TEXTURE, x + 84, y + 25, 176, 14, progress + 1, 16);
        }
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();
        int energyHeight = (int) (energy * BAR_HEIGHT / (float) maxEnergy);
        guiGraphics.blit(TEXTURE, x + BAR_X, y + BAR_Y + (BAR_HEIGHT - energyHeight), TEXTURE_BAR_X, TEXTURE_BAR_Y + (BAR_HEIGHT - energyHeight), BAR_WIDTH, energyHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        if (isMouseOverEnergyBar(mouseX, mouseY, x, y)) {
            renderEnergyTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    private boolean isMouseOverEnergyBar(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x + BAR_X && mouseX <= x + BAR_X + BAR_WIDTH && mouseY >= y + BAR_Y && mouseY <= y + BAR_Y + BAR_HEIGHT;
    }

    private void renderEnergyTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.powered_macerator.energy", this.menu.getEnergy()));
        guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
    }
}
