package top.ribs.scguns.init;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.GunBenchRecipe;
import top.ribs.scguns.client.screen.MaceratorRecipe;
import top.ribs.scguns.client.screen.MechanicalPressRecipe;


public class ModRecipeTypes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Reference.MOD_ID);
    public static final RegistryObject<RecipeSerializer<MaceratorRecipe>> MACERATOR_SERIALIZER = RECIPE_SERIALIZERS.register("macerating", MaceratorRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<MechanicalPressRecipe>> MECHANICAL_PRESS_SERIALIZER = RECIPE_SERIALIZERS.register("mechanical_pressing", MechanicalPressRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<GunBenchRecipe>> GUN_BENCH_SERIALIZER = RECIPE_SERIALIZERS.register("gun_bench", GunBenchRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}


