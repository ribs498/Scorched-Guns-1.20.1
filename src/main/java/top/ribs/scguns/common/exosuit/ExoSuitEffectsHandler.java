package top.ribs.scguns.common.exosuit;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import top.ribs.scguns.item.animated.ExoSuitItem;

import java.util.UUID;

/**
 * Handles applying and removing effects from ExoSuit upgrades
 * FIXED: Using completely unique UUIDs to avoid conflicts with vanilla armor
 */
public class ExoSuitEffectsHandler {

    // FIXED: Generated completely random, unique UUIDs that won't conflict with vanilla
    private static final UUID HELMET_ARMOR_UUID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final UUID HELMET_TOUGHNESS_UUID = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
    private static final UUID HELMET_KNOCKBACK_UUID = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
    private static final UUID HELMET_SPEED_UUID = UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");

    private static final UUID CHEST_ARMOR_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID CHEST_TOUGHNESS_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID CHEST_KNOCKBACK_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final UUID CHEST_SPEED_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");

    private static final UUID LEGS_ARMOR_UUID = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430c8");
    private static final UUID LEGS_TOUGHNESS_UUID = UUID.fromString("6ba7b815-9dad-11d1-80b4-00c04fd430c8");
    private static final UUID LEGS_KNOCKBACK_UUID = UUID.fromString("6ba7b816-9dad-11d1-80b4-00c04fd430c8");
    private static final UUID LEGS_SPEED_UUID = UUID.fromString("6ba7b817-9dad-11d1-80b4-00c04fd430c8");

    private static final UUID BOOTS_ARMOR_UUID = UUID.fromString("6ba7b818-9dad-11d1-80b4-00c04fd430c8");
    private static final UUID BOOTS_TOUGHNESS_UUID = UUID.fromString("6ba7b819-9dad-11d1-80b4-00c04fd430c8");
    private static final UUID BOOTS_KNOCKBACK_UUID = UUID.fromString("6ba7b81a-9dad-11d1-80b4-00c04fd430c8");
    private static final UUID BOOTS_SPEED_UUID = UUID.fromString("6ba7b81b-9dad-11d1-80b4-00c04fd430c8");

    /**
     * Applies all effects from equipped ExoSuit pieces
     */
    public static void applyExoSuitEffects(Player player) {
        // FIXED: Only remove ExoSuit effects, don't interfere with other systems
        removeExoSuitEffects(player);

        // FIXED: Only apply effects if player is wearing ExoSuit pieces
        boolean hasExoSuitPieces = false;
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit) {
                hasExoSuitPieces = true;
                applyArmorPieceEffects(player, armorStack, exosuit);
            }
        }

        // If no ExoSuit pieces, don't apply anything
        if (!hasExoSuitPieces) {
            return;
        }
    }

    public static ExoSuitUpgrade.Effects getTotalEffects(Player player) {
        ExoSuitUpgrade.Effects totalEffects = new ExoSuitUpgrade.Effects();

        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem) {
                ExoSuitUpgrade.Effects pieceEffects = calculateTotalEffects(player, armorStack);

                totalEffects.armorBonus += pieceEffects.getArmorBonus();
                totalEffects.armorToughness += pieceEffects.getArmorToughness();
                totalEffects.knockbackResistance += pieceEffects.getKnockbackResistance();
                totalEffects.speedModifier += pieceEffects.getSpeedModifier();
                totalEffects.jumpBoost += pieceEffects.getJumpBoost();
                totalEffects.fallDamageReduction += pieceEffects.getFallDamageReduction();
                totalEffects.nightVision = totalEffects.nightVision || pieceEffects.hasNightVision();
                totalEffects.flight = totalEffects.flight || pieceEffects.hasFlight();

                if (pieceEffects.getFlightSpeed() > totalEffects.flightSpeed) {
                    totalEffects.flightSpeed = pieceEffects.getFlightSpeed();
                }

                totalEffects.recoilAngleReduction += pieceEffects.getRecoilAngleReduction();
                totalEffects.recoilKickReduction += pieceEffects.getRecoilKickReduction();
                totalEffects.spreadReduction += pieceEffects.getSpreadReduction();
            }
        }

        return totalEffects;
    }

    /**
     * Removes all ExoSuit effects from a player
     */
    public static void removeExoSuitEffects(Player player) {
        // FIXED: Only remove our specific UUIDs, don't interfere with vanilla armor
        removeAttributeModifier(player, Attributes.ARMOR, HELMET_ARMOR_UUID);
        removeAttributeModifier(player, Attributes.ARMOR_TOUGHNESS, HELMET_TOUGHNESS_UUID);
        removeAttributeModifier(player, Attributes.KNOCKBACK_RESISTANCE, HELMET_KNOCKBACK_UUID);
        removeAttributeModifier(player, Attributes.MOVEMENT_SPEED, HELMET_SPEED_UUID);

        removeAttributeModifier(player, Attributes.ARMOR, CHEST_ARMOR_UUID);
        removeAttributeModifier(player, Attributes.ARMOR_TOUGHNESS, CHEST_TOUGHNESS_UUID);
        removeAttributeModifier(player, Attributes.KNOCKBACK_RESISTANCE, CHEST_KNOCKBACK_UUID);
        removeAttributeModifier(player, Attributes.MOVEMENT_SPEED, CHEST_SPEED_UUID);

        removeAttributeModifier(player, Attributes.ARMOR, LEGS_ARMOR_UUID);
        removeAttributeModifier(player, Attributes.ARMOR_TOUGHNESS, LEGS_TOUGHNESS_UUID);
        removeAttributeModifier(player, Attributes.KNOCKBACK_RESISTANCE, LEGS_KNOCKBACK_UUID);
        removeAttributeModifier(player, Attributes.MOVEMENT_SPEED, LEGS_SPEED_UUID);

        removeAttributeModifier(player, Attributes.ARMOR, BOOTS_ARMOR_UUID);
        removeAttributeModifier(player, Attributes.ARMOR_TOUGHNESS, BOOTS_TOUGHNESS_UUID);
        removeAttributeModifier(player, Attributes.KNOCKBACK_RESISTANCE, BOOTS_KNOCKBACK_UUID);
        removeAttributeModifier(player, Attributes.MOVEMENT_SPEED, BOOTS_SPEED_UUID);

        if (player.hasEffect(MobEffects.NIGHT_VISION)) {
            MobEffectInstance effect = player.getEffect(MobEffects.NIGHT_VISION);
            if (effect != null && effect.getDuration() > 50 && effect.getDuration() <= 400) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
        if (player.hasEffect(MobEffects.JUMP)) {
            MobEffectInstance effect = player.getEffect(MobEffects.JUMP);
            if (effect != null && effect.getDuration() > 50 && effect.getDuration() <= 200) {
                player.removeEffect(MobEffects.JUMP);
            }
        }
        if (player.hasEffect(MobEffects.WATER_BREATHING)) {
            MobEffectInstance effect = player.getEffect(MobEffects.WATER_BREATHING);
            if (effect != null && effect.getDuration() > 60 && effect.getDuration() <= 200) {
                player.removeEffect(MobEffects.WATER_BREATHING);
            }
        }
    }

    /**
     * Applies effects from a single armor piece
     */
    private static void applyArmorPieceEffects(Player player, ItemStack armorStack, ExoSuitItem exosuit) {
        ExoSuitUpgrade.Effects totalEffects = calculateTotalEffects(player, armorStack);

        UUID[] uuids = getUUIDsForArmorType(exosuit.getType());
        UUID armorUUID = uuids[0];
        UUID toughnessUUID = uuids[1];
        UUID knockbackUUID = uuids[2];
        UUID speedUUID = uuids[3];

        if (totalEffects.getArmorBonus() > 0) {
            addAttributeModifier(player, Attributes.ARMOR, armorUUID,
                    "ExoSuit Armor Bonus", totalEffects.getArmorBonus(),
                    AttributeModifier.Operation.ADDITION);
        }

        if (totalEffects.getArmorToughness() > 0) {
            addAttributeModifier(player, Attributes.ARMOR_TOUGHNESS, toughnessUUID,
                    "ExoSuit Armor Toughness", totalEffects.getArmorToughness(),
                    AttributeModifier.Operation.ADDITION);
        }

        if (totalEffects.getKnockbackResistance() > 0) {
            addAttributeModifier(player, Attributes.KNOCKBACK_RESISTANCE, knockbackUUID,
                    "ExoSuit Knockback Resistance", totalEffects.getKnockbackResistance(),
                    AttributeModifier.Operation.ADDITION);
        }

        if (totalEffects.getSpeedModifier() != 0) {
            addAttributeModifier(player, Attributes.MOVEMENT_SPEED, speedUUID,
                    "ExoSuit Speed Modifier", totalEffects.getSpeedModifier(),
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }

        if (totalEffects.hasNightVision()) {
            if (shouldApplyNightVision(player, armorStack)) {
                MobEffectInstance currentNightVision = player.getEffect(MobEffects.NIGHT_VISION);
                if (currentNightVision == null || currentNightVision.getDuration() < 40) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, false));
                }
            }
        }

        if (totalEffects.getJumpBoost() > 0) {
            if (shouldApplyJumpBoost(player, armorStack)) {
                int amplifier = Math.max(0, (int) (totalEffects.getJumpBoost() * 5) - 1);
                MobEffectInstance currentJump = player.getEffect(MobEffects.JUMP);
                if (currentJump == null || currentJump.getDuration() < 40) {
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 200, amplifier, false, false, false));
                }
            }
        }
        if (shouldApplyWaterBreathing(player, armorStack)) {
            MobEffectInstance currentWaterBreathing = player.getEffect(MobEffects.WATER_BREATHING);
            if (currentWaterBreathing == null || currentWaterBreathing.getDuration() < 60) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, false, false, false));
            }
        }
    }

    // ... Rest of the methods remain the same ...

    private static boolean shouldApplyNightVision(Player player, ItemStack armorStack) {
        if (!(armorStack.getItem() instanceof ExoSuitItem exosuit) ||
                exosuit.getType() != net.minecraft.world.item.ArmorItem.Type.HELMET) {
            return false;
        }

        if (!ExoSuitPowerManager.isPowerEnabled(player, "hud")) {
            return false;
        }

        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("hud") &&
                        upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.NightVisionModuleItem) {

                    return ExoSuitPowerManager.canUpgradeFunction(player, "hud");
                }
            }
        }
        return false;
    }

    private static boolean shouldApplyJumpBoost(Player player, ItemStack armorStack) {
        if (!(armorStack.getItem() instanceof ExoSuitItem exosuit) ||
                exosuit.getType() != net.minecraft.world.item.ArmorItem.Type.BOOTS) {
            return false;
        }

        if (!ExoSuitPowerManager.isPowerEnabled(player, "mobility")) {
            return false;
        }
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("mobility")) {
                    return ExoSuitPowerManager.canUpgradeFunction(player, "mobility");
                }
            }
        }
        return false;
    }

    private static boolean shouldApplyWaterBreathing(Player player, ItemStack armorStack) {
        if (!(armorStack.getItem() instanceof ExoSuitItem exosuit) ||
                exosuit.getType() != net.minecraft.world.item.ArmorItem.Type.HELMET) {
            return false;
        }
        if (!player.isInWater() && !player.isUnderWater()) {
            return false;
        }
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("breathing") &&
                        upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.RebreatherModuleItem) {
                    return ExoSuitPowerManager.canUpgradeFunction(player, "breathing");
                }
            }
        }
        return false;
    }

    private static ExoSuitUpgrade.Effects calculateTotalEffects(Player player, ItemStack armorStack) {
        ExoSuitUpgrade.Effects totalEffects = new ExoSuitUpgrade.Effects();
        String slotContext = getSlotContextForArmorType(armorStack);

        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = getUpgradeForSlotContext(upgradeItem, slot, slotContext);

                if (upgrade != null) {
                    ExoSuitUpgrade.Effects effects = upgrade.getEffects();
                    if (requiresPowerAndEnabled(player, upgrade, upgradeItem)) {
                        totalEffects.armorBonus += effects.getArmorBonus();
                        totalEffects.armorToughness += effects.getArmorToughness();
                        totalEffects.knockbackResistance += effects.getKnockbackResistance();
                        totalEffects.speedModifier += effects.getSpeedModifier();
                        totalEffects.jumpBoost += effects.getJumpBoost();
                        totalEffects.fallDamageReduction += effects.getFallDamageReduction();
                        totalEffects.nightVision = totalEffects.nightVision || effects.hasNightVision();
                        totalEffects.flight = totalEffects.flight || effects.hasFlight();
                        if (effects.getFlightSpeed() > totalEffects.flightSpeed) {
                            totalEffects.flightSpeed = effects.getFlightSpeed();
                        }
                    } else if (!isEnergyUpgrade(upgradeItem)) {
                        totalEffects.armorBonus += effects.getArmorBonus();
                        totalEffects.armorToughness += effects.getArmorToughness();
                        totalEffects.knockbackResistance += effects.getKnockbackResistance();
                        totalEffects.speedModifier += effects.getSpeedModifier();
                        totalEffects.jumpBoost += effects.getJumpBoost();
                        totalEffects.fallDamageReduction += effects.getFallDamageReduction();
                        totalEffects.flight = totalEffects.flight || effects.hasFlight();
                        if (effects.getFlightSpeed() > totalEffects.flightSpeed) {
                            totalEffects.flightSpeed = effects.getFlightSpeed();
                        }
                    }
                }
            }
        }

        return totalEffects;
    }
    private static ExoSuitUpgrade getUpgradeForSlotContext(ItemStack upgradeItem, int slotIndex, String armorContext) {
        String slotType = determineSlotType(armorContext, slotIndex);

        ExoSuitUpgrade slotSpecific = ExoSuitUpgradeManager.getUpgradeForItemInSlot(upgradeItem, slotType);
        if (slotSpecific != null) {
            return slotSpecific;
        }
        return ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
    }
    private static String determineSlotType(String armorContext, int slotIndex) {
        return switch (armorContext) {
            case "helmet" -> switch (slotIndex) {
                case 0 -> "plating";
                case 1 -> "hud";
                case 2 -> "breathing";
                default -> "unknown";
            };
            case "chestplate" -> switch (slotIndex) {
                case 0 -> "plating";
                case 1 -> "pauldron";
                case 2 -> "power_core";
                case 3 -> "utility";
                default -> "unknown";
            };
            case "leggings" -> switch (slotIndex) {
                case 0 -> "plating";
                case 1 -> "knee_guard";
                case 2 -> "utility";
                default -> "unknown";
            };
            case "boots" -> switch (slotIndex) {
                case 0 -> "plating";
                case 1 -> "mobility";
                default -> "unknown";
            };
            default -> "unknown";
        };
    }

    private static String getSlotContextForArmorType(ItemStack armorStack) {
        if (armorStack.getItem() instanceof ExoSuitItem exosuit) {
            return switch (exosuit.getType()) {
                case HELMET -> "helmet";
                case CHESTPLATE -> "chestplate";
                case LEGGINGS -> "leggings";
                case BOOTS -> "boots";
            };
        }
        return "unknown";
    }
    private static boolean requiresPowerAndEnabled(Player player, ExoSuitUpgrade upgrade, ItemStack upgradeItem) {
        String upgradeType = upgrade.getType();

        if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.EnergyUpgradeItem energyUpgrade) {
            boolean powerEnabled = ExoSuitPowerManager.isPowerEnabled(player, upgradeType);

            if (!powerEnabled) {
                return false;
            }
            if (!energyUpgrade.canFunctionWithoutPower()) {
                return ExoSuitPowerManager.canUpgradeFunction(player, upgradeType);
            }

            return true;
        }

        return true;
    }
    private static boolean isEnergyUpgrade(ItemStack upgradeItem) {
        return upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.EnergyUpgradeItem;
    }

    /**
     * Gets the appropriate UUIDs for an armor type
     */
    private static UUID[] getUUIDsForArmorType(net.minecraft.world.item.ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> new UUID[]{HELMET_ARMOR_UUID, HELMET_TOUGHNESS_UUID, HELMET_KNOCKBACK_UUID, HELMET_SPEED_UUID};
            case CHESTPLATE -> new UUID[]{CHEST_ARMOR_UUID, CHEST_TOUGHNESS_UUID, CHEST_KNOCKBACK_UUID, CHEST_SPEED_UUID};
            case LEGGINGS -> new UUID[]{LEGS_ARMOR_UUID, LEGS_TOUGHNESS_UUID, LEGS_KNOCKBACK_UUID, LEGS_SPEED_UUID};
            case BOOTS -> new UUID[]{BOOTS_ARMOR_UUID, BOOTS_TOUGHNESS_UUID, BOOTS_KNOCKBACK_UUID, BOOTS_SPEED_UUID};
        };
    }

    /**
     * Helper method to add an attribute modifier
     */
    private static void addAttributeModifier(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                             UUID uuid, String name, double value, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            // FIXED: Check if modifier already exists before adding
            if (instance.getModifier(uuid) == null) {
                AttributeModifier modifier = new AttributeModifier(uuid, name, value, operation);
                instance.addPermanentModifier(modifier);
            }
        }
    }

    /**
     * Helper method to remove an attribute modifier
     */
    private static void removeAttributeModifier(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID uuid) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(uuid);
        }
    }
}