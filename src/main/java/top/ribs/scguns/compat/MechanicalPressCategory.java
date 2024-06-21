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
import top.ribs.scguns.init.ModBlocks;

import java.awt.*;

public class MechanicalPressCategory implements IRecipeCategory<MechanicalPressRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Reference.MOD_ID, "mechanical_pressing");
    public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/mechanical_press_gui.png");
    public static final RecipeType<MechanicalPressRecipe> MECHANICAL_PRESS_TYPE = new RecipeType<>(UID, MechanicalPressRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final int offsetX = 20;
    private final int offsetY = 26;

    public MechanicalPressCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, offsetX, offsetY, 120, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.MECHANICAL_PRESS.get()));
    }

    @Override
    public RecipeType<MechanicalPressRecipe> getRecipeType() {
        return MECHANICAL_PRESS_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.scguns.mechanical_press");
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
    public void setRecipe(IRecipeLayoutBuilder builder, MechanicalPressRecipe recipe, IFocusGroup focuses) {
        int inputBaseX = 24;
        int inputBaseY = 1;
        int inputSpacing = 18;
        builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX - inputSpacing, inputBaseY)
                .addIngredients(recipe.getIngredients().get(0));
        if (recipe.getIngredients().size() > 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX, inputBaseY)
                    .addIngredients(recipe.getIngredients().get(1));
        }
        if (recipe.getIngredients().size() > 2) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX + inputSpacing, inputBaseY) // Third input slot
                    .addIngredients(recipe.getIngredients().get(2));
        }
        if (!recipe.getMoldItem().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX, inputBaseY + inputSpacing) // Mold slot
                    .addIngredients(recipe.getMoldItem());
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 1)
                .addItemStack(recipe.getResultItem(null));
    }
    @Override
    public void draw(MechanicalPressRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        int processingTime = recipe.getProcessingTime();
        if (processingTime > 0) {
            int seconds = processingTime / 20;
            String processingTimeText = String.format("%ds", seconds);
            guiGraphics.drawString(Minecraft.getInstance().font, processingTimeText, 70 + offsetX, 44, Color.gray.getRGB(), false);
        }
    }
}
