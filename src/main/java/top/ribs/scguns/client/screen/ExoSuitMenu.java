package top.ribs.scguns.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.common.exosuit.ExoSuitData;
import top.ribs.scguns.common.exosuit.ExoSuitPouchHandler;
import top.ribs.scguns.common.exosuit.ExoSuitUpgrade;
import top.ribs.scguns.common.exosuit.ExoSuitUpgradeManager;
import top.ribs.scguns.item.animated.ExoSuitItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageSaveExoSuitUpgrades;

import java.util.ArrayList;
import java.util.List;

public class ExoSuitMenu extends AbstractContainerMenu {

    public static final int ARMOR_SLOT = 0;
    public static final int UPGRADE_SLOT_1 = 1;
    public static final int UPGRADE_SLOT_2 = 2;
    public static final int UPGRADE_SLOT_3 = 3;
    public static final int UPGRADE_SLOT_4 = 4;

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    public static final int EXOSUIT_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    private final Player player;
    private final InteractionHand hand;

    private final ItemStackHandler exosuitInventory = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            ExoSuitMenu.this.slotsChanged(null);

            if (slot >= UPGRADE_SLOT_1 && slot <= UPGRADE_SLOT_4) {
                saveUpgradesToServer();
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == ARMOR_SLOT) {
                return stack.getItem() instanceof ExoSuitItem;
            }
            return slot >= UPGRADE_SLOT_1 && slot <= UPGRADE_SLOT_4;
        }
    };

    public ExoSuitMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, extraData.readEnum(InteractionHand.class));
    }

    public ExoSuitMenu(int id, Inventory playerInv, InteractionHand hand) {
        super(ModMenuTypes.EXOSUIT_MENU.get(), id);
        this.player = playerInv.player;
        this.hand = hand;

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
        addExoSuitSlots();

        moveExoSuitToArmorSlot();

        loadUpgradesFromArmor();
    }
    private void moveExoSuitToArmorSlot() {
        ItemStack heldStack = player.getItemInHand(hand);
        if (!heldStack.isEmpty() && heldStack.getItem() instanceof ExoSuitItem) {
            this.exosuitInventory.setStackInSlot(ARMOR_SLOT, heldStack.copy());
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
    }
    private void loadUpgradesFromArmor() {
        ItemStack armorPiece = getArmorPiece();
        if (!armorPiece.isEmpty() && armorPiece.getItem() instanceof ExoSuitItem) {
            CompoundTag upgradeData = ExoSuitData.getUpgradeData(armorPiece);

            if (upgradeData.contains("Upgrades")) {
                ListTag upgradeList = upgradeData.getList("Upgrades", 10);

                for (int i = 0; i < upgradeList.size(); i++) {
                    CompoundTag slotTag = upgradeList.getCompound(i);
                    int slot = slotTag.getInt("Slot");

                    if (slot >= 0 && slot < 4 && slotTag.contains("Item")) {
                        ItemStack upgradeStack = ItemStack.of(slotTag.getCompound("Item"));
                        this.exosuitInventory.setStackInSlot(UPGRADE_SLOT_1 + slot, upgradeStack);
                    }
                }
            }
        }
    }

    private void saveUpgradesToServer() {
        if (player.level().isClientSide) {
            List<ItemStack> upgradeStacks = new ArrayList<>();
            for (int i = UPGRADE_SLOT_1; i <= UPGRADE_SLOT_4; i++) {
                upgradeStacks.add(this.exosuitInventory.getStackInSlot(i));
            }
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageSaveExoSuitUpgrades(upgradeStacks));
        }
    }
    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (player.level().isClientSide) {
            saveUpgradesToServer();
        }
        ItemStack armorPiece = this.exosuitInventory.getStackInSlot(ARMOR_SLOT);
        if (!armorPiece.isEmpty()) {
            if (player.getItemInHand(hand).isEmpty()) {
                player.setItemInHand(hand, armorPiece);
            } else {
                if (!player.getInventory().add(armorPiece)) {
                    player.drop(armorPiece, false);
                }
            }
        }
    }

    private void addExoSuitSlots() {
        this.addSlot(new ExoSuitArmorSlot(exosuitInventory, ARMOR_SLOT, 26, 35));

        this.addSlot(new ExoSuitUpgradeSlot(exosuitInventory, UPGRADE_SLOT_1, 98, 26));
        this.addSlot(new ExoSuitUpgradeSlot(exosuitInventory, UPGRADE_SLOT_2, 116, 26));
        this.addSlot(new ExoSuitUpgradeSlot(exosuitInventory, UPGRADE_SLOT_3, 98, 44));
        this.addSlot(new ExoSuitUpgradeSlot(exosuitInventory, UPGRADE_SLOT_4, 116, 44));
    }

    public ItemStack getArmorPiece() {
        return exosuitInventory.getStackInSlot(ARMOR_SLOT);
    }

    public int getAvailableUpgradeSlots() {
        ItemStack armor = getArmorPiece();
        if (armor.getItem() instanceof ExoSuitItem exosuit) {
            return exosuit.getMaxUpgradeSlots();
        }
        return 0;
    }

    public boolean isUpgradeSlotEnabled(int upgradeSlotIndex) {
        if (upgradeSlotIndex < UPGRADE_SLOT_1 || upgradeSlotIndex > UPGRADE_SLOT_4) {
            return false;
        }

        int slotNumber = upgradeSlotIndex - UPGRADE_SLOT_1 + 1;
        return slotNumber <= getAvailableUpgradeSlots();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index >= EXOSUIT_INVENTORY_FIRST_SLOT_INDEX && index < EXOSUIT_INVENTORY_FIRST_SLOT_INDEX + 5) {

                if (index == EXOSUIT_INVENTORY_FIRST_SLOT_INDEX + ARMOR_SLOT) {
                    return ItemStack.EMPTY;
                }

                if (!this.moveItemStackTo(itemstack1, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= VANILLA_FIRST_SLOT_INDEX && index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
                if (itemstack1.getItem() instanceof ExoSuitItem) {
                    return ItemStack.EMPTY;
                }
                else {
                    if (!this.moveItemStackTo(itemstack1, EXOSUIT_INVENTORY_FIRST_SLOT_INDEX + UPGRADE_SLOT_1, EXOSUIT_INVENTORY_FIRST_SLOT_INDEX + UPGRADE_SLOT_4 + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
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
        return true;
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
    private static class ExoSuitArmorSlot extends SlotItemHandler {
        public ExoSuitArmorSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ExoSuitItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            player.closeContainer();
        }
    }

    private class ExoSuitUpgradeSlot extends SlotItemHandler {
        public ExoSuitUpgradeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            if (!ExoSuitUpgradeManager.isUpgradeItem(stack)) {
                return false;
            }

            if (!isUpgradeSlotEnabled(this.getSlotIndex())) {
                return false;
            }

            ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(stack);
            if (upgrade != null) {
                ItemStack armorPiece = getArmorPiece();
                if (armorPiece.getItem() instanceof ExoSuitItem exosuit) {
                    return canUpgradeGoInSlot(upgrade, this.getSlotIndex() - UPGRADE_SLOT_1, exosuit.getType());
                }
            }

            return false;
        }
        @Override
        public boolean mayPickup(Player player) {
            ItemStack currentStack = getItem();
            if (currentStack.isEmpty()) {
                return true;
            }
            ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(currentStack);
            if (upgrade != null && upgrade.getType().equals("pouches")) {
                ItemStack armorPiece = getArmorPiece();
                if (!ExoSuitPouchHandler.canRemovePouch(armorPiece, currentStack)) {
                    return false;
                }
            }

            return super.mayPickup(player);
        }
        @Override
        public boolean isActive() {
            return isUpgradeSlotEnabled(this.getSlotIndex());
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            if (player.level().isClientSide) {
                saveUpgradesToServer();
            }
        }

        @Override
        public void set(ItemStack stack) {
            super.set(stack);

            if (!stack.isEmpty() && player.level().isClientSide) {
                ExoSuitUpgradeManager.getUpgradeForItem(stack);
            }
        }
    }

    private boolean canUpgradeGoInSlot(ExoSuitUpgrade upgrade, int slotIndex, ArmorItem.Type armorType) {
        String upgradeType = upgrade.getType();

        if (slotIndex == 0) {
            return upgradeType.equals("plating");
        }
        return switch (armorType) {
            case HELMET -> switch (slotIndex) {
                case 1 -> upgradeType.equals("hud");
                case 2 -> upgradeType.equals("breathing");
                default -> false;
            };
            case CHESTPLATE -> switch (slotIndex) {
                case 1 -> upgradeType.equals("pauldron");
                case 2 -> upgradeType.equals("power_core");
                case 3 -> upgradeType.equals("utility") || upgradeType.equals("pouches");
                default -> false;
            };
            case LEGGINGS -> switch (slotIndex) {
                case 1 -> upgradeType.equals("knee_guard") || upgradeType.equals("plating");
                case 2 -> upgradeType.equals("utility");
                default -> false;
            };
            case BOOTS -> switch (slotIndex) {
                case 1 -> upgradeType.equals("mobility");
                default -> false;
            };
        };

    }

}