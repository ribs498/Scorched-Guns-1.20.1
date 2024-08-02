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
import top.ribs.scguns.client.screen.MechanicalPressRecipe;
import top.ribs.scguns.client.screen.PoweredMechanicalPressRecipe;
import top.ribs.scguns.init.ModBlocks;
import java.util.List; // Ensure this import is present
import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

public class PoweredMechanicalPressCategory implements IRecipeCategory<PoweredMechanicalPressRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Reference.MOD_ID, "powered_mechanical_pressing");
    public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/powered_mechanical_press_gui.png");
    public static final RecipeType<PoweredMechanicalPressRecipe> POWERED_MECHANICAL_PRESS_TYPE = new RecipeType<>(UID, PoweredMechanicalPressRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final int offsetX = 11;
    private final int offsetY = 16;

    public PoweredMechanicalPressCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, offsetX, offsetY, 128, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.POWERED_MECHANICAL_PRESS.get()));
    }

    @Override
    public RecipeType<PoweredMechanicalPressRecipe> getRecipeType() {
        return POWERED_MECHANICAL_PRESS_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.scguns.powered_mechanical_press");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PoweredMechanicalPressRecipe recipe, IFocusGroup focuses) {
        int inputBaseX = 33;
        int inputBaseY = 11;
        int inputSpacing = 18;

        builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX - inputSpacing, inputBaseY)
                .addIngredients(recipe.getIngredients().get(0));
        if (recipe.getIngredients().size() > 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX, inputBaseY)
                    .addIngredients(recipe.getIngredients().get(1));
        }
        if (recipe.getIngredients().size() > 2) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX + inputSpacing, inputBaseY)
                    .addIngredients(recipe.getIngredients().get(2));
        }
        if (!recipe.getMoldItem().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX, inputBaseY + inputSpacing)
                    .addIngredients(recipe.getMoldItem());
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 103, inputBaseY) // Adjust the Y position for the output slot
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(PoweredMechanicalPressRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
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

    private void drawEnergyBar(GuiGraphics guiGraphics, PoweredMechanicalPressRecipe recipe) {
        int energyRequired = recipe.getEnergyUse();
        int maxEnergy = 10000; // Assuming 10000 is the max energy capacity for the powered press
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
        tooltip.add(Component.translatable("tooltip.powered_mechanical_press.energy", energyRequired));
        guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), (int) mouseX, (int) mouseY);
    }
}

