package top.ribs.scguns.common.exosuit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

/**
 * Helper class for managing ExoSuit upgrade data
 */
public class ExoSuitData {

    // NBT keys for storing upgrade data
    private static final String UPGRADES_TAG = "ExoSuitUpgrades";
    private static final String HELMET_UPGRADES = "HelmetUpgrades";
    private static final String CHEST_UPGRADES = "ChestUpgrades";
    private static final String LEG_UPGRADES = "LegUpgrades";
    private static final String BOOT_UPGRADES = "BootUpgrades";

    /**
     * Gets the upgrade data compound from an exosuit piece
     */
    public static CompoundTag getUpgradeData(ItemStack exosuitPiece) {
        if (exosuitPiece.isEmpty()) {
            return new CompoundTag();
        }
        return exosuitPiece.getOrCreateTag().getCompound(UPGRADES_TAG);
    }

    /**
     * Sets upgrade data on an exosuit piece
     */
    public static void setUpgradeData(ItemStack exosuitPiece, CompoundTag upgradeData) {
        if (!exosuitPiece.isEmpty()) {
            exosuitPiece.getOrCreateTag().put(UPGRADES_TAG, upgradeData);
        }
    }

    /**
     * Checks if an exosuit piece has any upgrades installed
     */
    public static boolean hasUpgrades(ItemStack exosuitPiece) {
        CompoundTag upgrades = getUpgradeData(exosuitPiece);
        if (upgrades.contains("Upgrades")) {
            ListTag upgradeList = upgrades.getList("Upgrades", 10);
            return !upgradeList.isEmpty();
        }
        return false;
    }

    /**
     * Gets an upgrade item from a specific slot
     */
    public static ItemStack getUpgradeInSlot(ItemStack exosuitPiece, int slot) {
        CompoundTag upgradeData = getUpgradeData(exosuitPiece);

        if (upgradeData.contains("Upgrades")) {
            ListTag upgradeList = upgradeData.getList("Upgrades", 10);

            for (int i = 0; i < upgradeList.size(); i++) {
                CompoundTag slotTag = upgradeList.getCompound(i);
                if (slotTag.getInt("Slot") == slot) {
                    if (slotTag.contains("Item")) {
                        return ItemStack.of(slotTag.getCompound("Item"));
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

}