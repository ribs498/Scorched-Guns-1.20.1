package top.ribs.scguns.client.screen;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
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
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.container.getContainerSize()) {
                if (!this.moveItemStackTo(itemstack1, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.container.getContainerSize(), false)) {
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
