package top.ribs.scguns.common.exosuit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.item.animated.ExoSuitItem;

public class ExoSuitPouchHandler {

    private static final String POUCH_DATA_TAG = "PouchData";

    /**
     * Opens the appropriate pouch inventory for the player
     */
    public static void openPouchInventory(ServerPlayer player) {
        ItemStack chestplate = getEquippedChestplate(player);
        if (chestplate.isEmpty()) {
            return;
        }
        ItemStack pouchUpgrade = findPouchUpgrade(chestplate);
        if (pouchUpgrade.isEmpty()) {
            return;
        }

        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pouchUpgrade);
        if (upgrade == null) {
            return;
        }
        int storageSize = upgrade.getDisplay().getStorageSize();
        String containerType = upgrade.getDisplay().getContainerType();
        String pouchId = getPouchId(pouchUpgrade);
        openPouchContainer(player, chestplate, pouchId, storageSize, containerType, upgrade);
    }

    /**
     * Opens the pouch container based on type
     */
    private static void openPouchContainer(ServerPlayer player, ItemStack chestplate, String pouchId,
                                           int storageSize, String containerType, ExoSuitUpgrade upgrade) {

        ItemStackHandler pouchInventory = getOrCreatePouchInventory(chestplate, pouchId, storageSize);

        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.translatable("container.scguns.pouch", upgrade.getDisplay().getModel());
            }

            @Override
            public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {
                ItemStackHandlerContainer container = new ItemStackHandlerContainer(pouchInventory, chestplate, pouchId);

                return switch (containerType.toLowerCase()) {
                    case "chest" -> ChestMenu.threeRows(id, playerInventory, container);
                    case "double_chest" -> ChestMenu.sixRows(id, playerInventory, container);
                    case "dispenser" -> new DispenserMenu(id, playerInventory, container);
                    default -> {
                        // Default based on size
                        if (storageSize <= 9) {
                            yield new DispenserMenu(id, playerInventory, container);
                        } else if (storageSize <= 27) {
                            yield ChestMenu.threeRows(id, playerInventory, container);
                        } else {
                            yield ChestMenu.sixRows(id, playerInventory, container);
                        }
                    }
                };
            }
        };

        NetworkHooks.openScreen(player, menuProvider);
    }
    public static boolean canRemovePouch(ItemStack chestplate, ItemStack pouchUpgrade) {
        if (pouchUpgrade.isEmpty()) {
            return true;
        }

        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pouchUpgrade);
        if (upgrade == null || !upgrade.getType().equals("pouches")) {
            return true;
        }

        String pouchId = getPouchId(pouchUpgrade);
        CompoundTag pouchData = getPouchData(chestplate);

        if (!pouchData.contains(pouchId)) {
            return true;
        }
        ItemStackHandler handler = new ItemStackHandler(upgrade.getDisplay().getStorageSize());
        handler.deserializeNBT(pouchData.getCompound(pouchId));

        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private static ItemStackHandler getOrCreatePouchInventory(ItemStack chestplate, String pouchId, int size) {
        CompoundTag pouchData = getPouchData(chestplate);

        ItemStackHandler handler = new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                savePouchInventory(chestplate, pouchId, this);
            }
        };
        if (pouchData.contains(pouchId)) {
            handler.deserializeNBT(pouchData.getCompound(pouchId));
        }

        return handler;
    }
    private static void savePouchInventory(ItemStack chestplate, String pouchId, ItemStackHandler handler) {
        CompoundTag pouchData = getPouchData(chestplate);
        pouchData.put(pouchId, handler.serializeNBT());
        setPouchData(chestplate, pouchData);
    }
    private static ItemStack findPouchUpgrade(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("pouches")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static String getPouchId(ItemStack pouchUpgrade) {
        return pouchUpgrade.getItem().toString();
    }
    private static ItemStack getEquippedChestplate(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }
    private static CompoundTag getPouchData(ItemStack chestplate) {
        return chestplate.getOrCreateTag().getCompound(POUCH_DATA_TAG);
    }
    private static void setPouchData(ItemStack chestplate, CompoundTag pouchData) {
        chestplate.getOrCreateTag().put(POUCH_DATA_TAG, pouchData);
    }
        private record ItemStackHandlerContainer(ItemStackHandler handler, ItemStack chestplate,
                                                 String pouchId) implements Container {

        @Override
            public int getContainerSize() {
                return handler.getSlots();
            }

            @Override
            public boolean isEmpty() {
                for (int i = 0; i < handler.getSlots(); i++) {
                    if (!handler.getStackInSlot(i).isEmpty()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public @NotNull ItemStack getItem(int slot) {
                return handler.getStackInSlot(slot);
            }

            @Override
            public @NotNull ItemStack removeItem(int slot, int count) {
                ItemStack result = handler.extractItem(slot, count, false);
                setChanged();
                return result;
            }

            @Override
            public @NotNull ItemStack removeItemNoUpdate(int slot) {
                ItemStack stack = handler.getStackInSlot(slot);
                handler.setStackInSlot(slot, ItemStack.EMPTY);
                setChanged();
                return stack;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                handler.setStackInSlot(slot, stack);
                setChanged();
            }

            @Override
            public void setChanged() {
                savePouchInventory(chestplate, pouchId, handler);
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }

            @Override
            public void clearContent() {
                for (int i = 0; i < handler.getSlots(); i++) {
                    handler.setStackInSlot(i, ItemStack.EMPTY);
                }
                setChanged();
            }
        }
}