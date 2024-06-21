package top.ribs.scguns.client.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import top.ribs.scguns.blockentity.MechanicalPressBlockEntity;
import top.ribs.scguns.init.ModBlocks;

public class MechanicalPressMenu extends AbstractContainerMenu {
    public final MechanicalPressBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    public MechanicalPressMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public MechanicalPressMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.MECHANICAL_PRESS_MENU.get(), id);
        checkContainerSize(inv, 6);
        blockEntity = ((MechanicalPressBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            int yLevel = 27;
            this.addSlot(new SlotItemHandler(handler, 0, 26, yLevel));
            this.addSlot(new SlotItemHandler(handler, 1, 44, yLevel));
            this.addSlot(new SlotItemHandler(handler, 2, 62, yLevel));
            this.addSlot(new SlotItemHandler(handler, 3, 44, 45) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return true;
                }
            });
            this.addSlot(new SlotItemHandler(handler, 4, 86, 62));
            this.addSlot(new SlotItemHandler(handler, 5, 114, 27) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        });

        addDataSlots(data);
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public boolean hasFuel() {
        return data.get(2) > 0;
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int progressArrowSize = 24;
        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    public int getScaledFuelProgress() {
        int fuelTime = this.data.get(2);
        int maxFuelTime = this.data.get(3);
        int fuelProgressSize = 14;
        return maxFuelTime != 0 && fuelTime != 0 ? fuelTime * fuelProgressSize / maxFuelTime : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            int fuelSlotIndex = VANILLA_SLOT_COUNT + 4;
            int outputSlotIndex = VANILLA_SLOT_COUNT + 5;
            int moldSlotIndex = VANILLA_SLOT_COUNT + 3;

            if (index == outputSlotIndex) {
                if (!this.moveItemStackTo(itemstack1, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else if (index >= VANILLA_FIRST_SLOT_INDEX && index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
                if (this.isFuel(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, fuelSlotIndex, fuelSlotIndex + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.isInputItem(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.isMoldItem(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, moldSlotIndex, moldSlotIndex + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= VANILLA_FIRST_SLOT_INDEX && index < VANILLA_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT) {
                    if (!this.moveItemStackTo(itemstack1, VANILLA_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= VANILLA_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT && index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
                    if (!this.moveItemStackTo(itemstack1, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(itemstack1, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
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


    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.MECHANICAL_PRESS.get());
    }
    private boolean isMoldItem(ItemStack stack) {
        if (level == null) return false;
        RecipeManager recipeManager = level.getRecipeManager();
        return recipeManager.getAllRecipesFor(MechanicalPressRecipe.Type.INSTANCE).stream()
                .anyMatch(recipe -> recipe.getMoldItem().test(stack));
    }

    private boolean isInputItem(ItemStack stack) {
        if (level == null) return false;
        RecipeManager recipeManager = level.getRecipeManager();
        return recipeManager.getAllRecipesFor(MechanicalPressRecipe.Type.INSTANCE).stream()
                .flatMap(recipe -> recipe.getIngredients().stream())
                .anyMatch(ingredient -> ingredient.test(stack));
    }

    private boolean isFuel(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
