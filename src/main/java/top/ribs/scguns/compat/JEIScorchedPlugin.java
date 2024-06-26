package top.ribs.scguns.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.*;
import top.ribs.scguns.init.ModBlocks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEIScorchedPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Reference.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new GunBenchCategory(guiHelper));
        registration.addRecipeCategories(new MaceratorCategory(guiHelper));
        registration.addRecipeCategories(new MechanicalPressCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        List<GunBenchRecipe> gunBenchRecipes = recipeManager.getAllRecipesFor(GunBenchRecipe.Type.INSTANCE);
        List<MaceratorRecipe> maceratorRecipes = recipeManager.getAllRecipesFor(MaceratorRecipe.Type.INSTANCE);
        List<MechanicalPressRecipe> mechanicalPressRecipes = recipeManager.getAllRecipesFor(MechanicalPressRecipe.Type.INSTANCE);
        registration.addRecipes(GunBenchCategory.GUN_BENCH_TYPE, gunBenchRecipes);
        registration.addRecipes(MaceratorCategory.MACERATING_TYPE, maceratorRecipes);
        registration.addRecipes(MechanicalPressCategory.MECHANICAL_PRESS_TYPE, mechanicalPressRecipes);

    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(GunBenchScreen.class, 100, 47, 30, 20, GunBenchCategory.GUN_BENCH_TYPE);
        registration.addRecipeClickArea(MaceratorScreen.class, 90, 25, 30, 20, MaceratorCategory.MACERATING_TYPE);
        registration.addRecipeClickArea(MechanicalPressScreen.class, 80, 25, 25, 20, MechanicalPressCategory.MECHANICAL_PRESS_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GUN_BENCH.get()), GunBenchCategory.GUN_BENCH_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACERATOR.get()), MaceratorCategory.MACERATING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MECHANICAL_PRESS.get()), MechanicalPressCategory.MECHANICAL_PRESS_TYPE);
    }
}


