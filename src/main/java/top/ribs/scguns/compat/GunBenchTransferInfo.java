package top.ribs.scguns.compat;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Ingredient;
import top.ribs.scguns.client.screen.GunBenchMenu;
import top.ribs.scguns.client.screen.GunBenchRecipe;
import top.ribs.scguns.client.screen.ModMenuTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GunBenchTransferInfo implements IRecipeTransferInfo<GunBenchMenu, GunBenchRecipe> {

    @Override
    public Class<GunBenchMenu> getContainerClass() {
        return GunBenchMenu.class;
    }

    @Override
    public Optional<MenuType<GunBenchMenu>> getMenuType() {
        return Optional.of(ModMenuTypes.GUN_BENCH.get());
    }

    @Override
    public RecipeType<GunBenchRecipe> getRecipeType() {
        return GunBenchCategory.GUN_BENCH_TYPE;
    }

    @Override
    public boolean canHandle(GunBenchMenu container, GunBenchRecipe recipe) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(GunBenchMenu container, GunBenchRecipe recipe) {
        List<Slot> slots = new ArrayList<>();
        NonNullList<Ingredient> ingredients = recipe.getIngredients();

        for (int i = 0; i < ingredients.size(); i++) {
            if (!ingredients.get(i).isEmpty()) {
                slots.add(container.getSlot(i));
            }
        }

        if (!recipe.getBlueprint().isEmpty()) {
            slots.add(container.getSlot(GunBenchMenu.SLOT_BLUEPRINT));
        }

        return slots;
    }

    @Override
    public List<Slot> getInventorySlots(GunBenchMenu container, GunBenchRecipe recipe) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 12; i < container.slots.size(); i++) {
            slots.add(container.getSlot(i));
        }
        return slots;
    }
}