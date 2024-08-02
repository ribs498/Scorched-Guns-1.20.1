package top.ribs.scguns.client.screen;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;

public class LightningBatteryRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;
    private final int processingTime;
    private final int energyUse;

    public LightningBatteryRecipe(ResourceLocation id, Ingredient input, ItemStack output, int processingTime, int energyUse) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.processingTime = processingTime;
        this.energyUse = energyUse;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return input.test(container.getItem(0));
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
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.copy();
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public Ingredient getInput() {
        return input;
    }

    public int getEnergyUse() {
        return energyUse;
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

    public static class Type implements RecipeType<LightningBatteryRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "lightning_battery";
    }

    public static class Serializer implements RecipeSerializer<LightningBatteryRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation("scguns", "lightning_battery");

        @Override
        public LightningBatteryRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient input = Ingredient.fromJson(json.get("ingredients").getAsJsonArray().get(0));
            ItemStack output = CraftingHelper.getItemStack(json.getAsJsonObject("result"), true);
            int processingTime = json.get("processingTime").getAsInt();
            int requiredEnergy = json.get("requiredEnergy").getAsInt();
            return new LightningBatteryRecipe(recipeId, input, output, processingTime, requiredEnergy);
        }

        @Override
        public LightningBatteryRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            ItemStack output = buffer.readItem();
            int processingTime = buffer.readInt();
            int requiredEnergy = buffer.readInt();
            return new LightningBatteryRecipe(recipeId, input, output, processingTime, requiredEnergy);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LightningBatteryRecipe recipe) {
            recipe.getInput().toNetwork(buffer);
            buffer.writeItem(recipe.getResultItem(RegistryAccess.EMPTY));
            buffer.writeInt(recipe.getProcessingTime());
            buffer.writeInt(recipe.getEnergyUse());
        }
    }
}
