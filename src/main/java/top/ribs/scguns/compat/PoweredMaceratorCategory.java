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
import top.ribs.scguns.client.screen.PoweredMaceratorRecipe;
import top.ribs.scguns.init.ModBlocks;
import java.util.List;
import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

public class PoweredMaceratorCategory implements IRecipeCategory<PoweredMaceratorRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Reference.MOD_ID, "powered_macerating");
    public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/powered_macerator_gui.png");
    public static final RecipeType<PoweredMaceratorRecipe> POWERED_MACERATING_TYPE = new RecipeType<>(UID, PoweredMaceratorRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final int offsetX = 11;
    private final int offsetY = 16;

    public PoweredMaceratorCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, offsetX, offsetY, 128, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.POWERED_MACERATOR.get()));
    }

    @Override
    public RecipeType<PoweredMaceratorRecipe> getRecipeType() {
        return POWERED_MACERATING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.scguns.powered_macerator");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PoweredMaceratorRecipe recipe, IFocusGroup focuses) {
        int inputBaseX = 33;
        int inputBaseY = 11;
        int inputSpacing = 18;

        for (int i = 0; i < 4; i++) {
            int row = i / 2;
            int col = i % 2;
            int x = inputBaseX + col * inputSpacing;
            int y = inputBaseY + row * inputSpacing;
            if (i < recipe.getIngredients().size()) {
                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .addIngredients(recipe.getIngredients().get(i));
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 103, inputBaseY)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(PoweredMaceratorRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        int processingTime = recipe.getProcessingTime();
        if (processingTime > 0) {
            int seconds = processingTime / 20;
            String processingTimeText = String.format("%ds", seconds);
            guiGraphics.drawString(Minecraft.getInstance().font, processingTimeText, 70 + offsetX, 44, Color.gray.getRGB(), false);
        }
        drawEnergyBar(guiGraphics, recipe);
        if (isMouseOverEnergyBar(mouseX, mouseY, 3, 5, 7, 42)) {
            renderEnergyTooltip(guiGraphics, mouseX, mouseY, recipe.getEnergyUse());
        }
    }

    private void drawEnergyBar(GuiGraphics guiGraphics, PoweredMaceratorRecipe recipe) {
        int energyRequired = recipe.getEnergyUse();
        int maxEnergy = 10000;
        int barHeight = 42;
        int energyHeight = (int) (energyRequired * barHeight / (float) maxEnergy);

        int energyBarX = 3;
        int energyBarY = 5 + (barHeight - energyHeight);

        guiGraphics.blit(TEXTURE, energyBarX, energyBarY, 176, 20 + (barHeight - energyHeight), 14, energyHeight);
    }

    private boolean isMouseOverEnergyBar(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void renderEnergyTooltip(GuiGraphics guiGraphics, double mouseX, double mouseY, int energyRequired) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.powered_macerator.energy", energyRequired));
        guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), (int) mouseX, (int) mouseY);
    }
}

