package top.ribs.scguns.common.exosuit;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import top.ribs.scguns.item.animated.ExoSuitItem;
import top.ribs.scguns.item.exosuit.EnergyUpgradeItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages power consumption and distribution for ExoSuit components
 */
public class ExoSuitPowerManager {

    private static final String POWER_STATES_TAG = "ExoSuitPowerStates";

    private static final Map<UUID, Map<String, Integer>> playerCooldowns = new HashMap<>();

    public static boolean consumeEnergy(Player player, String upgradeType, int energyRequired) {
        ItemStack chestplate = getEquippedChestplate(player);
        if (chestplate.isEmpty()) {
            return false;
        }

        ItemStack powerCore = findPowerCore(chestplate);
        if (powerCore.isEmpty()) {
            return false;
        }

        return powerCore.getCapability(ForgeCapabilities.ENERGY)
                .map(energyStorage -> {
                    if (energyStorage.getEnergyStored() >= energyRequired) {
                        energyStorage.extractEnergy(energyRequired, false);
                        return true;
                    } else {
                        sendPowerShortageNotification(player, upgradeType);
                        return false;
                    }
                }).orElse(false);
    }
    public static boolean consumeEnergyForUpgrade(Player player, String upgradeType, ItemStack upgradeItem) {
        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
        if (upgrade == null) {
            return false;
        }

        int energyRequired = (int) upgrade.getEffects().getEnergyUse();
        return consumeEnergy(player, upgradeType, energyRequired);
    }
    public static boolean canUpgradeFunction(Player player, String upgradeType) {
        ItemStack armorPiece = getArmorPieceForUpgradeType(player, upgradeType);
        if (armorPiece.isEmpty()) {
            return false;
        }

        ItemStack upgradeItem = findUpgradeByType(armorPiece, upgradeType);
        if (upgradeItem.isEmpty()) {
            return false;
        }

        if (upgradeItem.getItem() instanceof EnergyUpgradeItem energyUpgrade) {
            if (energyUpgrade.canFunctionWithoutPower()) {
                return true;
            }

            ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
            if (upgrade == null) {
                return false;
            }

            int energyRequired = (int) upgrade.getEffects().getEnergyUse();

            ItemStack chestplate = getEquippedChestplate(player);
            if (chestplate.isEmpty()) {
                return false;
            }

            ItemStack powerCore = findPowerCore(chestplate);
            if (powerCore.isEmpty()) {
                return false;
            }

            return powerCore.getCapability(ForgeCapabilities.ENERGY)
                    .map(energyStorage -> energyStorage.getEnergyStored() >= energyRequired)
                    .orElse(false);
        }

        return true;
    }

    public static boolean isPowerEnabled(Player player, String upgradeType) {
        ItemStack armorPiece = getArmorPieceForUpgradeType(player, upgradeType);
        if (armorPiece.isEmpty()) {
            return false;
        }

        CompoundTag powerStates = getPowerStates(armorPiece);
        return powerStates.getBoolean(upgradeType);
    }

    /**
     * Sets the power state for a specific upgrade type
     */
    public static void setPowerEnabled(Player player, String upgradeType, boolean enabled) {
        ItemStack armorPiece = getArmorPieceForUpgradeType(player, upgradeType);
        if (armorPiece.isEmpty()) {
            return;
        }

        CompoundTag powerStates = getPowerStates(armorPiece);
        powerStates.putBoolean(upgradeType, enabled);
        setPowerStates(armorPiece, powerStates);
    }

    /**
     * Toggles the power state for a specific upgrade type
     */
    public static boolean togglePower(Player player, String upgradeType) {
        boolean currentState = isPowerEnabled(player, upgradeType);
        boolean newState = !currentState;
        setPowerEnabled(player, upgradeType, newState);
        return newState;
    }

    /**
     * Checks if enough time has passed since last power consumption for rate limiting
     */
    public static boolean canConsumeEnergy(Player player, String upgradeType, int cooldownTicks) {
        UUID playerId = player.getUUID();
        Map<String, Integer> upgradeCooldowns = playerCooldowns.computeIfAbsent(playerId, k -> new HashMap<>());

        int lastConsumption = upgradeCooldowns.getOrDefault(upgradeType, 0);
        int currentTick = player.tickCount;

        if (currentTick - lastConsumption >= cooldownTicks) {
            upgradeCooldowns.put(upgradeType, currentTick);
            return false;
        }

        return true;
    }

    /**
     * Gets the armor piece that should contain the specified upgrade type
     */
    private static ItemStack getArmorPieceForUpgradeType(Player player, String upgradeType) {
        return switch (upgradeType) {
            case "hud", "breathing", "night_vision" -> getEquippedHelmet(player);
            case "pauldron", "power_core", "utility" -> getEquippedChestplate(player);
            case "knee_guard" -> getEquippedLeggings(player);
            case "mobility" -> getEquippedBoots(player);
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack getEquippedHelmet(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.HELMET) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
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

    private static ItemStack getEquippedLeggings(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.LEGGINGS) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack getEquippedBoots(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.BOOTS) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack findPowerCore(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("power_core")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack findUpgradeByType(ItemStack armorPiece, String upgradeType) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorPiece, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals(upgradeType)) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static CompoundTag getPowerStates(ItemStack armorPiece) {
        CompoundTag upgradeData = ExoSuitData.getUpgradeData(armorPiece);
        return upgradeData.getCompound(POWER_STATES_TAG);
    }

    private static void setPowerStates(ItemStack armorPiece, CompoundTag powerStates) {
        CompoundTag upgradeData = ExoSuitData.getUpgradeData(armorPiece);
        upgradeData.put(POWER_STATES_TAG, powerStates);
        ExoSuitData.setUpgradeData(armorPiece, upgradeData);
    }

    /**
     * Clean up cooldown data for players who are no longer online
     */
    public static void cleanupPlayerData(UUID playerId) {
        playerCooldowns.remove(playerId);
    }

    /**
     * Sends a power shortage notification to the player
     */
    private static void sendPowerShortageNotification(Player player, String upgradeType) {
        if (canConsumeEnergy(player, upgradeType + "_notification", 800)) {
            return;
        }

        Component feedbackMessage = Component.translatable("exosuit.message.prefix")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("exosuit.message.power_shortage",
                                Component.translatable("exosuit.upgrade." + upgradeType))
                        .withStyle(ChatFormatting.RED));

        player.sendSystemMessage(feedbackMessage);

        // Play low power sound
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.3f, 0.6f);
    }
}