package top.ribs.scguns.client.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Reference;

public class MaceratorRecipe implements Recipe<SimpleContainer> {
    private final NonNullList<Ingredient> inputItems;
    private final ItemStack output;
    private final int processingTime;
    private final ResourceLocation id;

    public MaceratorRecipe(ResourceLocation id, NonNullList<Ingredient> inputItems, ItemStack output, int processingTime) {
        this.id = id;
        this.inputItems = inputItems;
        this.output = output;
        this.processingTime = processingTime;
    }

    @Override
    public boolean matches(SimpleContainer inv, Level world) {
        if (world.isClientSide()) {
            return false;
        }

        NonNullList<Ingredient> requiredIngredients = NonNullList.create();
        requiredIngredients.addAll(inputItems);

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                boolean matched = false;
                for (Ingredient ingredient : requiredIngredients) {
                    if (ingredient.test(stackInSlot)) {
                        requiredIngredients.remove(ingredient);
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return false;
                }
            }
        }

        return requiredIngredients.isEmpty();
    }

    @Override
    public ItemStack assemble(SimpleContainer inv, RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.copy();
    }

    public int getProcessingTime() {
        return processingTime;
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
        return inputItems;
    }

    public static class Type implements RecipeType<MaceratorRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "macerating";
    }

    public static class Serializer implements RecipeSerializer<MaceratorRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation("scguns", "macerating");

        @Override
        public MaceratorRecipe fromJson(@NotNull ResourceLocation recipeId, JsonObject json) {
            JsonArray ingredientsArray = json.getAsJsonArray("ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientsArray.size(), Ingredient.EMPTY);
            for (int i = 0; i < ingredientsArray.size(); i++) {
                ingredients.set(i, Ingredient.fromJson(ingredientsArray.get(i)));
            }

            ItemStack output = ShapedRecipe.itemStackFromJson(json.getAsJsonObject("result"));
            int processingTime = json.get("processingTime").getAsInt();

            return new MaceratorRecipe(recipeId, ingredients, output, processingTime);
        }

        @Override
        public MaceratorRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int size = buffer.readInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }

            ItemStack output = buffer.readItem();
            int processingTime = buffer.readInt();

            return new MaceratorRecipe(recipeId, ingredients, output, processingTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MaceratorRecipe recipe) {
            buffer.writeInt(recipe.inputItems.size());
            for (Ingredient ingredient : recipe.inputItems) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeItemStack(recipe.getResultItem(RegistryAccess.EMPTY), false);
            buffer.writeInt(recipe.getProcessingTime());
        }
    }
}

