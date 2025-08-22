package top.ribs.scguns.client.handler;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.exosuit.ExoSuitPouchHandler;
import top.ribs.scguns.common.exosuit.ExoSuitUpgrade;
import top.ribs.scguns.common.exosuit.ExoSuitUpgradeManager;
import top.ribs.scguns.item.animated.ExoSuitItem;
import top.ribs.scguns.item.exosuit.GasMaskModuleItem;
import top.ribs.scguns.item.exosuit.RebreatherModuleItem;
import top.ribs.scguns.item.exosuit.TargetTrackerModuleItem;
import top.ribs.scguns.item.exosuit.EnergyUpgradeItem;

import java.util.List;


@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ExoSuitUpgradeTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(stack);
        if (upgrade != null) {
            List<Component> tooltip = event.getToolTip();
            addUpgradeTooltip(tooltip, upgrade, stack);
        }
    }

    private static void addUpgradeTooltip(List<Component> tooltip, ExoSuitUpgrade upgrade, ItemStack stack) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.scguns.exosuit.upgrade.header").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        String upgradeType = upgrade.getType();
        String formattedType = formatUpgradeType(upgradeType);
        tooltip.add(Component.translatable("tooltip.scguns.exosuit.upgrade.type")
                .append(formattedType)
                .withStyle(ChatFormatting.GRAY));

        addSpecialFunctionality(tooltip, stack, upgrade);

        ExoSuitUpgrade.Effects effects = upgrade.getEffects();
        boolean hasEffects = false;

        if (effects.getArmorBonus() > 0) {
            tooltip.add(Component.translatable("tooltip.scguns.upgrade.effects").withStyle(ChatFormatting.AQUA));
            hasEffects = true;
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.upgrade.stat.armor", effects.getArmorBonus())
                    .withStyle(ChatFormatting.BLUE));
        }

        if (effects.getArmorToughness() > 0) {
            if (!hasEffects) {
                tooltip.add(Component.translatable("tooltip.scguns.upgrade.effects").withStyle(ChatFormatting.AQUA));
                hasEffects = true;
            }
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.upgrade.stat.armor_toughness", effects.getArmorToughness())
                    .withStyle(ChatFormatting.BLUE));
        }

        if (effects.getKnockbackResistance() > 0) {
            if (!hasEffects) {
                tooltip.add(Component.translatable("tooltip.scguns.upgrade.effects").withStyle(ChatFormatting.AQUA));
                hasEffects = true;
            }
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.upgrade.stat.knockback_resistance", (int)(effects.getKnockbackResistance() * 100))
                    .withStyle(ChatFormatting.BLUE));
        }

        // Movement effects
        if (effects.getSpeedModifier() != 0) {
            if (!hasEffects) {
                tooltip.add(Component.translatable("tooltip.scguns.upgrade.effects").withStyle(ChatFormatting.AQUA));
                hasEffects = true;
            }
            String speedKey = effects.getSpeedModifier() > 0 ? "tooltip.scguns.exosuit.upgrade.stat.speed_positive" : "tooltip.scguns.exosuit.upgrade.stat.speed_negative";
            tooltip.add(Component.translatable(speedKey, Math.abs((int)(effects.getSpeedModifier() * 100)))
                    .withStyle(effects.getSpeedModifier() > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
        }

        if (effects.getJumpBoost() > 0) {
            if (!hasEffects) {
                tooltip.add(Component.translatable("tooltip.scguns.upgrade.effects").withStyle(ChatFormatting.AQUA));
                hasEffects = true;
            }
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.upgrade.stat.jump", (int)(effects.getJumpBoost() * 100))
                    .withStyle(ChatFormatting.GREEN));
        }

        if (effects.getFallDamageReduction() > 0) {
            if (!hasEffects) {
                tooltip.add(Component.translatable("tooltip.scguns.upgrade.effects").withStyle(ChatFormatting.AQUA));
                hasEffects = true;
            }
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.upgrade.stat.fall_damage_reduction", (int)(effects.getFallDamageReduction() * 100))
                    .withStyle(ChatFormatting.GREEN));
        }
        if (effects.getRecoilAngleReduction() > 0) {
            if (!hasEffects) {
                tooltip.add(Component.translatable("tooltip.scguns.upgrade.effects").withStyle(ChatFormatting.AQUA));
                hasEffects = true;
            }
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.upgrade.stat.recoil_angle", (int)(effects.getRecoilAngleReduction() * 100))
                    .withStyle(ChatFormatting.YELLOW));
        }

        if (effects.getRecoilKickReduction() > 0) {
            if (!hasEffects) {
                tooltip.add(Component.translatable("tooltip.scguns.upgrade.effects").withStyle(ChatFormatting.AQUA));
                hasEffects = true;
            }
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.upgrade.stat.recoil_kick", (int)(effects.getRecoilKickReduction() * 100))
                    .withStyle(ChatFormatting.YELLOW));
        }

        if (effects.getSpreadReduction() > 0) {
            if (!hasEffects) {
                tooltip.add(Component.translatable("tooltip.scguns.upgrade.effects").withStyle(ChatFormatting.AQUA));
                hasEffects = true;
            }
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.upgrade.stat.spread_reduction", (int)(effects.getSpreadReduction() * 100))
                    .withStyle(ChatFormatting.YELLOW));
        }
        if (effects.hasNightVision()) {
            if (!hasEffects) {
                tooltip.add(Component.translatable("tooltip.scguns.upgrade.effects").withStyle(ChatFormatting.AQUA));
            }
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.upgrade.stat.night_vision")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (stack.getItem() instanceof EnergyUpgradeItem energyUpgrade) {
            addEnergyTooltip(tooltip, energyUpgrade);
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.scguns.upgrade.install_hint")
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
    }

    private static void addSpecialFunctionality(List<Component> tooltip, ItemStack stack, ExoSuitUpgrade upgrade) {
        if (stack.getItem() instanceof RebreatherModuleItem) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.header").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.rebreather.water_breathing")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.rebreather.unlimited_exploration")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.rebreather.automatic")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else if (stack.getItem() instanceof TargetTrackerModuleItem) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.header").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.target_tracker.highlights")
                    .withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.target_tracker.accuracy")
                    .withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.target_tracker.range")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.target_tracker.toggleable")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else if (stack.getItem() instanceof GasMaskModuleItem) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.header").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.gas_mask.toxic_protection")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.gas_mask.environmental_filter")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.gas_mask.sulfur_vents")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.gas_mask.automatic")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else if (upgrade.getType().equals("pouches")) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.header").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            int storageSize = upgrade.getDisplay().getStorageSize();
            String containerType = upgrade.getDisplay().getContainerType();

            tooltip.add(Component.translatable("tooltip.scguns.exosuit.functionality.pouches.storage", storageSize)
                    .withStyle(ChatFormatting.YELLOW));

            String containerTypeKey = switch (containerType.toLowerCase()) {
                case "dispenser" -> "tooltip.scguns.exosuit.functionality.pouches.type.dispenser";
                case "chest" -> "tooltip.scguns.exosuit.functionality.pouches.type.chest";
                case "double_chest" -> "tooltip.scguns.exosuit.functionality.pouches.type.double_chest";
                default -> "tooltip.scguns.exosuit.functionality.pouches.type.unknown";
            };
            tooltip.add(Component.translatable(containerTypeKey)
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

            addPouchContentStatus(tooltip, stack);
        }
    }

    private static void addPouchContentStatus(List<Component> tooltip, ItemStack pouchStack) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // Find equipped chestplate
        ItemStack chestplate = ItemStack.EMPTY;
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE) {
                chestplate = armorStack;
                break;
            }
        }
    }

    private static void addEnergyTooltip(List<Component> tooltip, EnergyUpgradeItem energyUpgrade) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.scguns.exosuit.energy.header").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));

        String consumptionKey = switch (energyUpgrade.getConsumptionType()) {
            case PER_TICK -> "tooltip.scguns.exosuit.energy.consumption.per_tick";
            case PER_USE -> "tooltip.scguns.exosuit.energy.consumption.per_use";
            case PER_SECOND -> "tooltip.scguns.exosuit.energy.consumption.per_second";
            case ACTIVATION -> "tooltip.scguns.exosuit.energy.consumption.activation";
        };

        tooltip.add(Component.translatable("tooltip.scguns.exosuit.energy.consumption.label")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(consumptionKey, energyUpgrade.getEnergyConsumption())
                        .withStyle(ChatFormatting.YELLOW)));

        if (!energyUpgrade.canFunctionWithoutPower()) {
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.energy.requires_power")
                    .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.translatable("tooltip.scguns.exosuit.energy.limited_function")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
        }
    }

    private static String formatUpgradeType(String type) {
        return switch (type) {
            case "plating" -> Component.translatable("tooltip.scguns.exosuit.upgrade.plating").getString();
            case "hud" -> Component.translatable("tooltip.scguns.exosuit.upgrade.hud").getString();
            case "breathing" -> Component.translatable("tooltip.scguns.exosuit.upgrade.breathing").getString();
            case "pauldron" -> Component.translatable("tooltip.scguns.exosuit.upgrade.pauldron").getString();
            case "power_core" -> Component.translatable("tooltip.scguns.exosuit.upgrade.power_core").getString();
            case "utility" -> Component.translatable("tooltip.scguns.exosuit.upgrade.utility").getString();
            case "pouches" -> Component.translatable("tooltip.scguns.exosuit.upgrade.pouches").getString();
            case "knee_guard" -> Component.translatable("tooltip.scguns.exosuit.upgrade.knee_guard").getString();
            case "mobility" -> Component.translatable("tooltip.scguns.exosuit.upgrade.mobility").getString();
            default -> type.substring(0, 1).toUpperCase() + type.substring(1).replace("_", " ");
        };
    }
}