package top.ribs.scguns.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.common.exosuit.ExoSuitData;
import top.ribs.scguns.common.exosuit.ExoSuitTargetTrackerHandler;
import top.ribs.scguns.common.exosuit.ExoSuitUpgrade;
import top.ribs.scguns.common.exosuit.ExoSuitUpgradeManager;
import top.ribs.scguns.item.animated.ExoSuitItem;

public class ExoSuitRecoilHelper {
    public static float getTotalRecoilAngleReduction(Player player) {
        float totalReduction = 0.0f;

        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem) {
                totalReduction += getRecoilAngleReductionFromPiece(armorStack);
            }
        }
        return Math.min(totalReduction, 0.8f);
    }

    public static float getTotalRecoilKickReduction(Player player) {
        float totalReduction = 0.0f;

        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem) {
                totalReduction += getRecoilKickReductionFromPiece(armorStack);
            }
        }
        return Math.min(totalReduction, 0.8f);
    }

    private static float getRecoilAngleReductionFromPiece(ItemStack armorStack) {
        float totalReduction = 0.0f;

        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null) {
                    totalReduction += upgrade.getEffects().getRecoilAngleReduction();
                }
            }
        }

        return totalReduction;
    }
    private static float getRecoilKickReductionFromPiece(ItemStack armorStack) {
        float totalReduction = 0.0f;

        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null) {
                    totalReduction += upgrade.getEffects().getRecoilKickReduction();
                }
            }
        }

        return totalReduction;
    }

    public static float getModifiedRecoilAngle(Player player, float baseRecoilAngle) {
        float reduction = getTotalRecoilAngleReduction(player);
        return baseRecoilAngle * (1.0f - reduction);
    }

    public static float getTotalSpreadReduction(Player player) {
        float totalReduction = 0.0f;
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem) {
                float pieceReduction = getSpreadReductionFromPiece(armorStack, player);
                totalReduction += pieceReduction;
            }
        }

        return Math.min(totalReduction, 0.8f);
    }

    private static float getSpreadReductionFromPiece(ItemStack armorStack, Player player) {
        float totalReduction = 0.0f;
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null) {
                    float spreadReduction = upgrade.getEffects().getSpreadReduction();
                    if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.TargetTrackerModuleItem) {
                        boolean isEnabled = ExoSuitTargetTrackerHandler.isTargetTrackerActive(player);
                        if (isEnabled) {
                            totalReduction += spreadReduction;
                        }
                    } else if (spreadReduction > 0) {
                        totalReduction += spreadReduction;
                    }
                }
            }
        }
        return totalReduction;
    }

    public static float getModifiedSpread(Player player, float baseSpread) {
        float reduction = getTotalSpreadReduction(player);

        return baseSpread * (1.0f - reduction);
    }
}