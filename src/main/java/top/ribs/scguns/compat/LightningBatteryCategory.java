package top.ribs.scguns.compat;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.LightningBatteryRecipe;
import top.ribs.scguns.init.ModBlocks;

import java.util.ArrayList;
import java.util.List; // Ensure this import is present
import java.util.Collections;
import java.awt.*;
import java.util.Optional;

public class LightningBatteryCategory implements IRecipeCategory<LightningBatteryRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Reference.MOD_ID, "lightning_battery");
    public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/lightning_battery_gui.png");
    public static final RecipeType<LightningBatteryRecipe> LIGHTNING_BATTERY_TYPE = new RecipeType<>(UID, LightningBatteryRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final int offsetX = 41;
    private final int offsetY = 16;

    public LightningBatteryCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, offsetX, offsetY, 96, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.LIGHTNING_BATTERY.get()));
    }

    @Override
    public RecipeType<LightningBatteryRecipe> getRecipeType() {
        return LIGHTNING_BATTERY_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.scguns.lightning_battery");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LightningBatteryRecipe recipe, IFocusGroup focuses) {
        int inputX = 15;
        int inputY = 19;

        builder.addSlot(RecipeIngredientRole.INPUT, inputX, inputY)
                .addIngredients(recipe.getInput());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 75, 19)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(LightningBatteryRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        int processingTime = recipe.getProcessingTime();
        if (processingTime > 0) {
            int seconds = processingTime / 20;
            String processingTimeText = String.format("%ds", seconds);
            guiGraphics.drawString(Minecraft.getInstance().font, processingTimeText, 36 + offsetX, 44, Color.gray.getRGB(), false);
        }
        drawEnergyBar(guiGraphics, recipe);
        if (isMouseOverEnergyBar(mouseX, mouseY, 3, 5, 7, 42)) {
            renderEnergyTooltip(guiGraphics, mouseX, mouseY, recipe.getEnergyUse());
        }
    }

    private void drawEnergyBar(GuiGraphics guiGraphics, LightningBatteryRecipe recipe) {
        int energyRequired = recipe.getEnergyUse();
        int maxEnergy = 64000; // Assuming 64000 is the max energy capacity
        int barHeight = 42; // Height of the energy bar in the texture
        int energyHeight = (int) (energyRequired * barHeight / (float) maxEnergy);

        int energyBarX = 3; // X position of the energy bar
        int energyBarY = 5 + (barHeight - energyHeight); // Y position, adjusted to start from the bottom

        guiGraphics.blit(TEXTURE, energyBarX, energyBarY, 176, 20 + (barHeight - energyHeight), 14, energyHeight);
    }

    private boolean isMouseOverEnergyBar(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void renderEnergyTooltip(GuiGraphics guiGraphics, double mouseX, double mouseY, int energyRequired) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.lightning_battery.energy", energyRequired));
        guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), (int) mouseX, (int) mouseY);
    }
}
