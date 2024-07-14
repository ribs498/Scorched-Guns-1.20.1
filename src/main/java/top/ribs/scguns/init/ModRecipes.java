package top.ribs.scguns.init;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.GunBenchRecipe;
import top.ribs.scguns.client.screen.MaceratorRecipe;
import top.ribs.scguns.client.screen.MechanicalPressRecipe;

import java.sql.Ref;


public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Reference.MOD_ID);

    public static final RegistryObject<RecipeSerializer<MechanicalPressRecipe>> MECHANICAL_PRESS_SERIALIZER =
            SERIALIZERS.register("mechanical_pressing", () -> MechanicalPressRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<MaceratorRecipe>> MACERATOR_SERIALIZER =
            SERIALIZERS.register("macerating", () -> MaceratorRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<GunBenchRecipe>> GUN_BENCH_SERIALIZER =
            SERIALIZERS.register("gun_bench", () -> GunBenchRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}



