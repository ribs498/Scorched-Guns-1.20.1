package top.ribs.scguns.client.screen;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.item.BlueprintItem;

import java.util.Optional;

public class GunBenchMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerLevelAccess containerAccess;
    private final Player player;

    public static final int SLOT_GRIP = 8;
    public static final int SLOT_MAGAZINE = 9;
    public static final int SLOT_BARREL_1 = 6;
    public static final int SLOT_BARREL_2 = 7;
    public static final int SLOT_INTERNAL_1 = 4;
    public static final int SLOT_INTERNAL_2 = 5;
    public static final int SLOT_TOP_INTERNAL_1 = 0;
    public static final int SLOT_TOP_INTERNAL_2 = 1;
    public static final int SLOT_TOP_BARREL_1 = 2;
    public static final int SLOT_TOP_BARREL_2 = 3;
    public static final int SLOT_OUTPUT = 10;
    public static final int SLOT_BLUEPRINT = 11;

    public GunBenchMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, new SimpleContainer(12), ContainerLevelAccess.NULL);
    }

    public GunBenchMenu(int id, Inventory playerInventory, ContainerLevelAccess containerAccess) {
        this(id, playerInventory, new SimpleContainer(12), containerAccess);
    }

    public GunBenchMenu(int id, Inventory playerInventory, Container container, ContainerLevelAccess containerAccess) {
        super(ModMenuTypes.GUN_BENCH.get(), id);
        checkContainerSize(container, 12);
        this.container = container;
        this.containerAccess = containerAccess;
        this.player = playerInventory.player;
        container.startOpen(playerInventory.player);

        // Add custom slots with correct indexes
        this.addSlot(new Slot(container, SLOT_TOP_INTERNAL_1, 26, 17));
        this.addSlot(new Slot(container, SLOT_TOP_INTERNAL_2, 44, 17));
        this.addSlot(new Slot(container, SLOT_TOP_BARREL_1, 62, 17));
        this.addSlot(new Slot(container, SLOT_TOP_BARREL_2, 80, 17));
        this.addSlot(new Slot(container, SLOT_INTERNAL_1, 26, 35));
        this.addSlot(new Slot(container, SLOT_INTERNAL_2, 44, 35));
        this.addSlot(new Slot(container, SLOT_BARREL_1, 62, 35));
        this.addSlot(new Slot(container, SLOT_BARREL_2, 80, 35));
        this.addSlot(new Slot(container, SLOT_GRIP, 26, 53));
        this.addSlot(new Slot(container, SLOT_MAGAZINE, 62, 53));
        this.addSlot(new Slot(container, SLOT_BLUEPRINT, 116, 17) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof BlueprintItem;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                // When blueprint slot changes, attempt auto-crafting
                attemptAutoCrafting();
            }
        });
        this.addSlot(new Slot(container, SLOT_OUTPUT, 140, 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                consumeIngredients();
            }
        });

        // Add player inventory slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add hotbar slots
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.slotsChanged(container);
    }

    /**
     * Attempts to automatically populate the crafting grid when a blueprint with an active recipe is placed
     */
    private void attemptAutoCrafting() {
        containerAccess.execute((level, pos) -> {
            if (level.isClientSide) return;

            ItemStack blueprintStack = container.getItem(SLOT_BLUEPRINT);
            if (blueprintStack.isEmpty() || !(blueprintStack.getItem() instanceof BlueprintItem)) {
                return;
            }

            ResourceLocation activeRecipeId = BlueprintScreen.getActiveRecipe(blueprintStack);
            if (activeRecipeId == null) {
                return;
            }

            Optional<GunBenchRecipe> recipeOptional = level.getRecipeManager()
                    .getAllRecipesFor(GunBenchRecipe.Type.INSTANCE)
                    .stream()
                    .filter(recipe -> recipe.getId().equals(activeRecipeId))
                    .findFirst();

            if (recipeOptional.isEmpty()) {
                return;
            }

            GunBenchRecipe recipe = recipeOptional.get();

            for (int i = 0; i < 10; i++) {
                ItemStack existing = container.getItem(i);
                if (!existing.isEmpty()) {
                    if (!player.getInventory().add(existing)) {
                        player.drop(existing, false);
                    }
                    container.setItem(i, ItemStack.EMPTY);
                }
            }

            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (int slotIndex = 0; slotIndex < Math.min(ingredients.size(), 10); slotIndex++) {
                Ingredient ingredient = ingredients.get(slotIndex);
                if (ingredient.isEmpty()) continue;

                ItemStack foundItem = findAndRemoveIngredientFromInventory(ingredient);
                if (!foundItem.isEmpty()) {
                    container.setItem(slotIndex, foundItem);
                }
            }

            slotsChanged(container);
        });
    }

    /**
     * Searches player inventory for an item matching the ingredient and removes one if found
     */
    private ItemStack findAndRemoveIngredientFromInventory(Ingredient ingredient) {
        // Check main inventory first
        for (int i = 9; i < player.getInventory().getContainerSize(); i++) { // Skip hotbar initially
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ingredient.test(stack)) {
                ItemStack result = stack.copy();
                result.setCount(1);
                stack.shrink(1);
                return result;
            }
        }

        // Check hotbar if nothing found in main inventory
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ingredient.test(stack)) {
                ItemStack result = stack.copy();
                result.setCount(1);
                stack.shrink(1);
                return result;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void slotsChanged(Container container) {
        containerAccess.execute((level, pos) -> {
            if (!level.isClientSide) {
                SimpleContainer craftingContainer = new SimpleContainer(12);
                for (int i = 0; i < 12; i++) {
                    craftingContainer.setItem(i, container.getItem(i));
                }

                Optional<GunBenchRecipe> recipe = level.getRecipeManager().getRecipeFor(GunBenchRecipe.Type.INSTANCE, craftingContainer, level);

                if (recipe.isPresent()) {
                    ItemStack result = recipe.get().assemble(craftingContainer, level.registryAccess());
                    container.setItem(SLOT_OUTPUT, result);
                } else {
                    container.setItem(SLOT_OUTPUT, ItemStack.EMPTY);
                }
            }
        });
    }

    private void consumeIngredients() {
        containerAccess.execute((level, pos) -> {
            if (!level.isClientSide) {
                SimpleContainer craftingContainer = new SimpleContainer(12);
                for (int i = 0; i < 12; i++) {
                    craftingContainer.setItem(i, container.getItem(i));
                }

                Optional<GunBenchRecipe> recipeOptional = level.getRecipeManager().getRecipeFor(GunBenchRecipe.Type.INSTANCE, craftingContainer, level);
                if (recipeOptional.isPresent()) {
                    GunBenchRecipe recipe = recipeOptional.get();
                    NonNullList<Ingredient> ingredients = recipe.getIngredients();

                    for (int i = 0; i < ingredients.size(); i++) {
                        Ingredient requiredIngredient = ingredients.get(i);
                        ItemStack stackInSlot = container.getItem(i);

                        if (!requiredIngredient.isEmpty() && !stackInSlot.isEmpty()) {
                            stackInSlot.shrink(1);
                            container.setItem(i, stackInSlot);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        slotsChanged(this.container);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index >= this.container.getContainerSize()) {
                if (itemstack1.getItem() instanceof BlueprintItem) {
                    boolean moveSuccess = this.moveItemStackTo(itemstack1, SLOT_BLUEPRINT, SLOT_BLUEPRINT + 1, false);

                    if (!moveSuccess) {
                        if (!this.moveItemStackTo(itemstack1, 0, SLOT_GRIP + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                else if (!this.moveItemStackTo(itemstack1, 0, this.container.getContainerSize(), false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(itemstack1, this.container.getContainerSize(), this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}