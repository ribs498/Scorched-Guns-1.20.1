package top.ribs.scguns.client.screen;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
public class GunBenchRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeItems;
    private final Ingredient blueprint;

    public GunBenchRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems, Ingredient blueprint) {
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
        this.blueprint = blueprint;
    }
    @Override
    public boolean matches(Container container, Level level) {
        // Check if the blueprint matches
        ItemStack blueprintStack = container.getItem(GunBenchMenu.SLOT_BLUEPRINT);
        if (!blueprint.test(blueprintStack)) {
            return false;
        }
        for (int i = 0; i < recipeItems.size(); i++) {
            ItemStack stackInSlot = container.getItem(i);
            Ingredient requiredIngredient = recipeItems.get(i);
            if (!requiredIngredient.isEmpty() && !requiredIngredient.test(stackInSlot)) {
                return false;
            } else if (requiredIngredient.isEmpty() && !stackInSlot.isEmpty()) {
                return false;
            }
        }
        return true;
    }


    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public NonNullList<Ingredient> getIngredients() {
        return recipeItems;
    }

    public Ingredient getBlueprint() {
        return blueprint;
    }

    public static class Type implements RecipeType<GunBenchRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "gun_bench";
    }

    public static class Serializer implements RecipeSerializer<GunBenchRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation("scguns", "gun_bench");

        public GunBenchRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            JsonObject ingredients = GsonHelper.getAsJsonObject(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(10, Ingredient.EMPTY);

            if (ingredients.has("gun_top_internal_1"))
                inputs.set(0, Ingredient.fromJson(GsonHelper.getAsJsonObject(ingredients, "gun_top_internal_1")));
            if (ingredients.has("gun_top_internal_2"))
                inputs.set(1, Ingredient.fromJson(GsonHelper.getAsJsonObject(ingredients, "gun_top_internal_2")));
            if (ingredients.has("gun_top_barrel_1"))
                inputs.set(2, Ingredient.fromJson(GsonHelper.getAsJsonObject(ingredients, "gun_top_barrel_1")));
            if (ingredients.has("gun_top_barrel_2"))
                inputs.set(3, Ingredient.fromJson(GsonHelper.getAsJsonObject(ingredients, "gun_top_barrel_2")));
            if (ingredients.has("gun_internal_1"))
                inputs.set(4, Ingredient.fromJson(GsonHelper.getAsJsonObject(ingredients, "gun_internal_1")));
            if (ingredients.has("gun_internal_2"))
                inputs.set(5, Ingredient.fromJson(GsonHelper.getAsJsonObject(ingredients, "gun_internal_2")));
            if (ingredients.has("gun_barrel_1"))
                inputs.set(6, Ingredient.fromJson(GsonHelper.getAsJsonObject(ingredients, "gun_barrel_1")));
            if (ingredients.has("gun_barrel_2"))
                inputs.set(7, Ingredient.fromJson(GsonHelper.getAsJsonObject(ingredients, "gun_barrel_2")));
            if (ingredients.has("gun_grip"))
                inputs.set(8, Ingredient.fromJson(GsonHelper.getAsJsonObject(ingredients, "gun_grip")));
            if (ingredients.has("gun_magazine"))
                inputs.set(9, Ingredient.fromJson(GsonHelper.getAsJsonObject(ingredients, "gun_magazine")));

            Ingredient blueprint = Ingredient.EMPTY;
            if (ingredients.has("blueprint")) {
                blueprint = Ingredient.fromJson(GsonHelper.getAsJsonObject(ingredients, "blueprint"));
            }

            return new GunBenchRecipe(recipeId, output, inputs, blueprint);
        }


        @Override
        public GunBenchRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(10, Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }

            Ingredient blueprint = Ingredient.fromNetwork(buffer);
            ItemStack output = buffer.readItem();

            return new GunBenchRecipe(recipeId, output, inputs, blueprint);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, GunBenchRecipe recipe) {
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buffer);
            }
            recipe.blueprint.toNetwork(buffer);
            buffer.writeItemStack(recipe.getResultItem(), false);
        }
    }

    private ItemStack getResultItem() {
        return output;
    }
}
