package top.ribs.scguns.common.exosuit;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import top.ribs.scguns.item.animated.ExoSuitItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExoSuitEffectsHandler {

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

    private static final String EXOSUIT_TAG = "ExoSuitEffect";
    private static final Map<UUID, Map<String, Boolean>> activeExoSuitEffects = new HashMap<>();

    public static void applyExoSuitEffects(Player player) {
        removeExoSuitEffects(player);

        Map<String, Boolean> playerEffects = activeExoSuitEffects.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        playerEffects.clear();

        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit) {
                applyArmorPieceEffects(player, armorStack, exosuit, playerEffects);
            }
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

    public static void removeExoSuitEffects(Player player) {
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

        Map<String, Boolean> playerEffects = activeExoSuitEffects.get(player.getUUID());
        if (playerEffects == null) {
            return;
        }

        if (Boolean.TRUE.equals(playerEffects.get("nightVision"))) {
            removeExoSuitEffect(player, MobEffects.NIGHT_VISION);
        }

        if (Boolean.TRUE.equals(playerEffects.get("jumpBoost"))) {
            removeExoSuitEffect(player, MobEffects.JUMP);
        }

        if (Boolean.TRUE.equals(playerEffects.get("waterBreathing"))) {
            removeExoSuitEffect(player, MobEffects.WATER_BREATHING);
        }

        playerEffects.clear();
    }

    private static void removeExoSuitEffect(Player player, net.minecraft.world.effect.MobEffect effect) {
        if (!player.hasEffect(effect)) {
            return;
        }

        MobEffectInstance currentEffect = player.getEffect(effect);
        if (currentEffect == null) {
            return;
        }

        int maxDuration = effect == MobEffects.NIGHT_VISION ? 400 : 200;

        if (currentEffect.getDuration() <= maxDuration && !currentEffect.isAmbient() && !currentEffect.isVisible() && currentEffect.getAmplifier() <= 1) {
            player.removeEffect(effect);
        }
    }

    private static void applyArmorPieceEffects(Player player, ItemStack armorStack, ExoSuitItem exosuit, Map<String, Boolean> playerEffects) {
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

        if (totalEffects.hasNightVision() && shouldApplyNightVision(player, armorStack)) {
            applyExoSuitEffect(player, MobEffects.NIGHT_VISION, 400, 0);
            playerEffects.put("nightVision", true);
        }

        if (totalEffects.getJumpBoost() > 0 && shouldApplyJumpBoost(player, armorStack)) {
            int amplifier = Math.max(0, (int) (totalEffects.getJumpBoost() * 5) - 1);
            applyExoSuitEffect(player, MobEffects.JUMP, 200, amplifier);
            playerEffects.put("jumpBoost", true);
        }

        if (shouldApplyWaterBreathing(player, armorStack)) {
            applyExoSuitEffect(player, MobEffects.WATER_BREATHING, 200, 0);
            playerEffects.put("waterBreathing", true);
        }
    }

    private static void applyExoSuitEffect(Player player, net.minecraft.world.effect.MobEffect effect, int duration, int amplifier) {
        MobEffectInstance currentEffect = player.getEffect(effect);

        if (currentEffect == null || currentEffect.getDuration() < 40) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false, false));
        }
    }

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

    private static UUID[] getUUIDsForArmorType(net.minecraft.world.item.ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> new UUID[]{HELMET_ARMOR_UUID, HELMET_TOUGHNESS_UUID, HELMET_KNOCKBACK_UUID, HELMET_SPEED_UUID};
            case CHESTPLATE -> new UUID[]{CHEST_ARMOR_UUID, CHEST_TOUGHNESS_UUID, CHEST_KNOCKBACK_UUID, CHEST_SPEED_UUID};
            case LEGGINGS -> new UUID[]{LEGS_ARMOR_UUID, LEGS_TOUGHNESS_UUID, LEGS_KNOCKBACK_UUID, LEGS_SPEED_UUID};
            case BOOTS -> new UUID[]{BOOTS_ARMOR_UUID, BOOTS_TOUGHNESS_UUID, BOOTS_KNOCKBACK_UUID, BOOTS_SPEED_UUID};
        };
    }

    private static void addAttributeModifier(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                             UUID uuid, String name, double value, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            if (instance.getModifier(uuid) == null) {
                AttributeModifier modifier = new AttributeModifier(uuid, name, value, operation);
                instance.addPermanentModifier(modifier);
            }
        }
    }

    private static void removeAttributeModifier(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID uuid) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(uuid);
        }
    }

    public static void cleanupPlayerData(UUID playerId) {
        activeExoSuitEffects.remove(playerId);
    }
}
