package top.ribs.scguns.client.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class MechanicalPressRecipe implements Recipe<SimpleContainer> {
    private final NonNullList<Ingredient> inputItems;
    private final Ingredient moldItem;
    private final ItemStack output;
    private final int processingTime;
    private final ResourceLocation id;

    public MechanicalPressRecipe(ResourceLocation id, NonNullList<Ingredient> inputItems, Ingredient moldItem, ItemStack output, int processingTime) {
        this.id = id;
        this.inputItems = inputItems;
        this.moldItem = moldItem;
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

        boolean moldMatched = moldItem.isEmpty() || moldItem.test(inv.getItem(3)); // Check mold slot if mold is present

        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (i == 3) continue; // Skip mold slot
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                boolean matched = false;
                Iterator<Ingredient> iterator = requiredIngredients.iterator();
                while (iterator.hasNext()) {
                    Ingredient ingredient = iterator.next();
                    if (ingredient.test(stackInSlot)) {
                        iterator.remove();
                        matched = true;
                        break;
                    }
                }
                // If the item is not part of the required ingredients, it acts as a placeholder
                if (!matched) {
                    continue;
                }
            }
        }

        return requiredIngredients.isEmpty() && moldMatched;
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

    public Ingredient getMoldItem() {
        return moldItem;
    }

    public static class Type implements RecipeType<MechanicalPressRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "mechanical_pressing";
    }

    public static class Serializer implements RecipeSerializer<MechanicalPressRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation("scguns", "mechanical_pressing");

        @Override
        public MechanicalPressRecipe fromJson(@NotNull ResourceLocation recipeId, JsonObject json) {
            JsonArray ingredientsArray = json.getAsJsonArray("ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientsArray.size(), Ingredient.EMPTY);
            for (int i = 0; i < ingredientsArray.size(); i++) {
                ingredients.set(i, Ingredient.fromJson(ingredientsArray.get(i)));
            }

            Ingredient moldItem = Ingredient.EMPTY;
            if (json.has("mold") && !json.get("mold").isJsonNull()) {
                moldItem = Ingredient.fromJson(json.getAsJsonObject("mold"));
            }

            ItemStack output = ShapedRecipe.itemStackFromJson(json.getAsJsonObject("result"));
            int processingTime = json.get("processingTime").getAsInt();

            return new MechanicalPressRecipe(recipeId, ingredients, moldItem, output, processingTime);
        }

        @Override
        public MechanicalPressRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int size = buffer.readInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }

            Ingredient moldItem = Ingredient.fromNetwork(buffer);
            ItemStack output = buffer.readItem();
            int processingTime = buffer.readInt();

            return new MechanicalPressRecipe(recipeId, ingredients, moldItem, output, processingTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MechanicalPressRecipe recipe) {
            buffer.writeInt(recipe.inputItems.size());
            for (Ingredient ingredient : recipe.inputItems) {
                ingredient.toNetwork(buffer);
            }

            recipe.getMoldItem().toNetwork(buffer);
            buffer.writeItemStack(recipe.getResultItem(RegistryAccess.EMPTY), false);
            buffer.writeInt(recipe.getProcessingTime());
        }
    }
}
