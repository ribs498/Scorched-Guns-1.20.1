package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.common.exosuit.ExoSuitData;
import top.ribs.scguns.common.exosuit.ExoSuitPowerManager;
import top.ribs.scguns.common.exosuit.ExoSuitUpgrade;
import top.ribs.scguns.common.exosuit.ExoSuitUpgradeManager;
import top.ribs.scguns.item.animated.ExoSuitItem;
import top.ribs.scguns.item.exosuit.NightVisionModuleItem;
import top.ribs.scguns.item.exosuit.TargetTrackerModuleItem;
import top.ribs.scguns.item.exosuit.GasMaskModuleItem;
import top.ribs.scguns.item.exosuit.RebreatherModuleItem;

/**
 * Network message to toggle ExoSuit power components
 */
public class C2SMessageToggleExoSuitPower extends PlayMessage<C2SMessageToggleExoSuitPower> {

    public enum PowerType {
        HELMET_HUD("hud", "HUD Module"),
        BOOTS_MOBILITY("mobility", "Mobility Enhancement"),
        JETPACK("jetpack", "Jetpack"); // Add this line

        private final String upgradeType;
        private final String displayName;

        PowerType(String upgradeType, String displayName) {
            this.upgradeType = upgradeType;
            this.displayName = displayName;
        }

        public String getUpgradeType() {
            return upgradeType;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private PowerType powerType;

    public C2SMessageToggleExoSuitPower() {
    }

    public C2SMessageToggleExoSuitPower(PowerType powerType) {
        this.powerType = powerType;
    }

    @Override
    public void encode(C2SMessageToggleExoSuitPower message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.powerType);
    }

    @Override
    public C2SMessageToggleExoSuitPower decode(FriendlyByteBuf buffer) {
        C2SMessageToggleExoSuitPower message = new C2SMessageToggleExoSuitPower();
        message.powerType = buffer.readEnum(PowerType.class);
        return message;
    }

    @Override
    public void handle(C2SMessageToggleExoSuitPower message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if (player == null) return;

            if (!ExoSuitPowerManager.canUpgradeFunction(player, message.powerType.getUpgradeType())) {
                String moduleTranslationKey = getSpecificModuleTranslationKey(player, message.powerType.getUpgradeType());

                Component feedbackMessage = Component.translatable("exosuit.message.prefix")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.translatable("exosuit.message.not_available",
                                        Component.translatable(moduleTranslationKey))
                                .withStyle(ChatFormatting.RED));

                player.sendSystemMessage(feedbackMessage, true);

                // Play error sound
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.5f, 0.8f);
                return;
            }

            // Toggle the power state
            boolean newState = ExoSuitPowerManager.togglePower(player, message.powerType.getUpgradeType());

            // Get the specific module name for feedback
            String moduleTranslationKey = getSpecificModuleTranslationKey(player, message.powerType.getUpgradeType());

            // Send feedback to player
            String statusKey = newState ? "exosuit.message.enabled" : "exosuit.message.disabled";
            ChatFormatting statusColor = newState ? ChatFormatting.GREEN : ChatFormatting.RED;

            Component feedbackMessage = Component.translatable("exosuit.message.prefix")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.translatable(statusKey,
                                    Component.translatable(moduleTranslationKey))
                            .withStyle(statusColor));

            player.sendSystemMessage(feedbackMessage, true);

            // Play appropriate sound
            if (newState) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 1.2f);
            } else {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 0.8f);
            }
        });
        context.setHandled(true);
    }

    /**
     * Gets the specific translation key for the currently equipped module
     */
    private static String getSpecificModuleTranslationKey(ServerPlayer player, String upgradeType) {
        if ("hud".equals(upgradeType)) {
            // Find which HUD module is equipped
            ItemStack hudModule = findHudModule(player);
            if (!hudModule.isEmpty()) {
                if (hudModule.getItem() instanceof NightVisionModuleItem) {
                    return "exosuit.upgrade.night_vision";
                } else if (hudModule.getItem() instanceof TargetTrackerModuleItem) {
                    return "exosuit.upgrade.target_tracker";
                } else if (hudModule.getItem() instanceof GasMaskModuleItem) {
                    return "exosuit.upgrade.gas_mask";
                } else if (hudModule.getItem() instanceof RebreatherModuleItem) {
                    return "exosuit.upgrade.rebreather";
                }
            }
            // Fallback to generic HUD if no specific module found
            return "exosuit.upgrade.hud";
        }

        // For other upgrade types, use the existing system
        return "exosuit.upgrade." + upgradeType;
    }

    /**
     * Finds the currently equipped HUD module in the player's helmet
     */
    private static ItemStack findHudModule(ServerPlayer player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.HELMET) {
                for (int slot = 0; slot < 4; slot++) {
                    ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
                    if (!upgradeItem.isEmpty()) {
                        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                        if (upgrade != null && upgrade.getType().equals("hud")) {
                            return upgradeItem;
                        }
                    }
                }
                break;
            }
        }
        return ItemStack.EMPTY;
    }
}