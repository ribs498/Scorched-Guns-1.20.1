package top.ribs.scguns.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
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
import top.ribs.scguns.init.ModItems;

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
        registration.addRecipeCategories(new PoweredMaceratorCategory(guiHelper));
        registration.addRecipeCategories(new MechanicalPressCategory(guiHelper));
        registration.addRecipeCategories(new PoweredMechanicalPressCategory(guiHelper));
        registration.addRecipeCategories(new LightningBatteryCategory(guiHelper));

    }
    public static MutableComponent getTranslation(String key, Object... args) {
        return Component.translatable("scguns." + key, args);
    }
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        assert Minecraft.getInstance().level != null;
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registration.addIngredientInfo(new ItemStack(ModItems.REPAIR_KIT.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.repair_kit"));
        registration.addIngredientInfo(new ItemStack(ModItems.COPPER_BLUEPRINT.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.copper_blueprint"));
        registration.addIngredientInfo(new ItemStack(ModItems.IRON_BLUEPRINT.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.iron_blueprint"));
        registration.addIngredientInfo(new ItemStack(ModItems.WRECKER_BLUEPRINT.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.wrecker_blueprint"));
        registration.addIngredientInfo(new ItemStack(ModItems.DIAMOND_STEEL_BLUEPRINT.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.diamond_steel_blueprint"));
        registration.addIngredientInfo(new ItemStack(ModItems.TREATED_BRASS_BLUEPRINT.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.treated_brass_blueprint"));
        registration.addIngredientInfo(new ItemStack(ModItems.OCEAN_BLUEPRINT.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.ocean_blueprint"));
        registration.addIngredientInfo(new ItemStack(ModItems.PIGLIN_BLUEPRINT.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.piglin_blueprint"));
        registration.addIngredientInfo(new ItemStack(ModItems.DEEP_DARK_BLUEPRINT.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.deep_dark_blueprint"));
        registration.addIngredientInfo(new ItemStack(ModItems.END_BLUEPRINT.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.end_blueprint"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.VENT_COLLECTOR.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.vent_collector"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.NITER_GLASS.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.niter_glass"));
        registration.addIngredientInfo(new ItemStack(ModItems.BLASPHEMY.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.blasphemy"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.LIGHTNING_BATTERY.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.lightning_battery"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.LIGHTNING_ROD_CONNECTOR.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.lightning_rod_connector"));
        registration.addIngredientInfo(new ItemStack(ModItems.PISTOL_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.pistol_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.RIFLE_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.rifle_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.SHOTGUN_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.shotgun_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.MAGNUM_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.magnum_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.ENERGY_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.energy_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.ROCKET_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.rocket_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.SPECIAL_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.special_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.EMPTY_CASING_POUCH.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.empty_casing_pouch"));
        registration.addIngredientInfo(new ItemStack(ModItems.CREATIVE_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.creative_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.DISHES_POUCH.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.dishes_pouch"));
        registration.addIngredientInfo(new ItemStack(ModItems.ROCK_POUCH.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.rock_pouch"));
        registration.addIngredientInfo(new ItemStack(ModItems.ANCIENT_BRASS.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.ancient_brass"));

        registration.addIngredientInfo(new ItemStack(ModBlocks.BASIC_TURRET.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.basic_turret"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.AUTO_TURRET.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.auto_turret"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.SHOTGUN_TURRET.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.shotgun_turret"));
        registration.addIngredientInfo(new ItemStack(ModItems.TEAM_LOG.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.team_log"));
        registration.addIngredientInfo(new ItemStack(ModItems.ENEMY_LOG.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.enemy_log"));

        registration.addIngredientInfo(new ItemStack(ModItems.SUPER_SHOTGUN.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.super_shotgun"));
        registration.addIngredientInfo(new ItemStack(ModItems.AUREOUS_SLAG.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.aureous_slag"));

        registration.addIngredientInfo(new ItemStack(ModBlocks.HOSTILE_TURRET_TARGETING_BLOCK.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.hostile_turret_targeting_block"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.PLAYER_TURRET_TARGETING_BLOCK.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.player_turret_targeting_block"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.TURRET_TARGETING_BLOCK.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.turret_targeting_block"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.FIRE_RATE_TURRET_MODULE.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.fire_rate_turret_module"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.DAMAGE_TURRET_MODULE.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.damage_turret_module"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.RANGE_TURRET_MODULE.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.range_turret_module"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.SHELL_CATCHER_TURRET_MODULE.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.shell_catcher_turret_module"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.CRYONITER.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.cryoniter"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.THERMOLITH.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.thermolith"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.PENETRATOR.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.penetrator"));

        registration.addIngredientInfo(new ItemStack(ModItems.NITER_DUST.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.niter_dust"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.ADVANCED_COMPOSTER.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.advanced_composter"));



        List<GunBenchRecipe> gunBenchRecipes = recipeManager.getAllRecipesFor(GunBenchRecipe.Type.INSTANCE);
        List<MaceratorRecipe> maceratorRecipes = recipeManager.getAllRecipesFor(MaceratorRecipe.Type.INSTANCE);
        List<PoweredMaceratorRecipe> poweredMaceratorRecipes = recipeManager.getAllRecipesFor(PoweredMaceratorRecipe.Type.INSTANCE);
        List<MechanicalPressRecipe> mechanicalPressRecipes = recipeManager.getAllRecipesFor(MechanicalPressRecipe.Type.INSTANCE);

        List<PoweredMechanicalPressRecipe> poweredMechanicalPressRecipes = recipeManager.getAllRecipesFor(PoweredMechanicalPressRecipe.Type.INSTANCE);
        registration.addRecipes(GunBenchCategory.GUN_BENCH_TYPE, gunBenchRecipes);
        registration.addRecipes(MaceratorCategory.MACERATING_TYPE, maceratorRecipes);
        registration.addRecipes(PoweredMaceratorCategory.POWERED_MACERATING_TYPE, poweredMaceratorRecipes);
        registration.addRecipes(MechanicalPressCategory.MECHANICAL_PRESS_TYPE, mechanicalPressRecipes);
        registration.addRecipes(PoweredMechanicalPressCategory.POWERED_MECHANICAL_PRESS_TYPE, poweredMechanicalPressRecipes);
        registration.addRecipes(LightningBatteryCategory.LIGHTNING_BATTERY_TYPE, recipeManager.getAllRecipesFor(LightningBatteryRecipe.Type.INSTANCE));


    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(GunBenchScreen.class, 100, 47, 30, 20, GunBenchCategory.GUN_BENCH_TYPE);
        registration.addRecipeClickArea(MaceratorScreen.class, 80, 25, 30, 20, MaceratorCategory.MACERATING_TYPE);
        registration.addRecipeClickArea(PoweredMaceratorScreen.class, 80, 25, 25, 20, PoweredMaceratorCategory.POWERED_MACERATING_TYPE);
        registration.addRecipeClickArea(MechanicalPressScreen.class, 80, 25, 25, 20, MechanicalPressCategory.MECHANICAL_PRESS_TYPE);
        registration.addRecipeClickArea(PoweredMechanicalPressScreen.class, 80, 25, 25, 20, PoweredMechanicalPressCategory.POWERED_MECHANICAL_PRESS_TYPE);
        registration.addRecipeClickArea(LightningBatteryScreen.class, 80, 32, 25, 20, LightningBatteryCategory.LIGHTNING_BATTERY_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GUN_BENCH.get()), GunBenchCategory.GUN_BENCH_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACERATOR.get()), MaceratorCategory.MACERATING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.POWERED_MACERATOR.get()), PoweredMaceratorCategory.POWERED_MACERATING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MECHANICAL_PRESS.get()), MechanicalPressCategory.MECHANICAL_PRESS_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.POWERED_MECHANICAL_PRESS.get()), PoweredMechanicalPressCategory.POWERED_MECHANICAL_PRESS_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.LIGHTNING_BATTERY.get()), LightningBatteryCategory.LIGHTNING_BATTERY_TYPE);
    }
}


