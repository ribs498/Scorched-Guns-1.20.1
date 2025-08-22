package top.ribs.scguns.common.exosuit;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.ExoSuitItem;

import java.util.Objects;

/**
 * Handles ExoSuit effect application events and power management
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExoSuitEventHandler {

    private static int tickCounter = 0;
    private static final int UPDATE_FREQUENCY = 20;

    /**
     * Updates ExoSuit effects periodically for all players
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            tickCounter++;

            // Only update every UPDATE_FREQUENCY ticks to avoid performance issues
            if (tickCounter >= UPDATE_FREQUENCY) {
                tickCounter = 0;
                updatePlayerExoSuitEffects(event.player);
            }
        }
    }

    /**
     * Apply effects when player logs in
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        updatePlayerExoSuitEffects(event.getEntity());
    }

    /**
     * Remove effects and clean up power data when player logs out
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // FIXED: Only remove ExoSuit effects if they were applied
        if (hasAnyExoSuitPiece(event.getEntity())) {
            ExoSuitEffectsHandler.removeExoSuitEffects(event.getEntity());
            ExoSuitNightVisionHandler.onPlayerLogout(event.getEntity());
        }
        ExoSuitPowerManager.cleanupPlayerData(event.getEntity().getUUID());
    }

    /**
     * Apply effects when player respawns
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide) {
            Objects.requireNonNull(event.getEntity().level().getServer()).execute(() -> {
                updatePlayerExoSuitEffects(event.getEntity());
            });
        }
    }

    /**
     * Updates a player's ExoSuit effects based on currently equipped armor
     */
    private static void updatePlayerExoSuitEffects(Player player) {
        if (player.level().isClientSide) return;

        boolean hasExoSuit = hasAnyExoSuitPiece(player);

        if (hasExoSuit) {
            ExoSuitEffectsHandler.applyExoSuitEffects(player);
            initializePowerStatesIfNeeded(player);
        } else {
            // FIXED: Only remove effects if player previously had ExoSuit
            // This prevents interfering with other armor systems
            ExoSuitEffectsHandler.removeExoSuitEffects(player);
            ExoSuitNightVisionHandler.onPlayerLogout(player);
        }
    }

    /**
     * FIXED: Helper method to check if player has any ExoSuit pieces
     */
    private static boolean hasAnyExoSuitPiece(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initialize default power states for upgrades if they haven't been set
     */
    private static void initializePowerStatesIfNeeded(Player player) {
        if (hasUpgradeType(player, "hud") && !hasPowerState(player, "hud")) {
            ExoSuitPowerManager.setPowerEnabled(player, "hud", false);
        }

        if (hasUpgradeType(player, "mobility") && !hasPowerState(player, "mobility")) {
            ExoSuitPowerManager.setPowerEnabled(player, "mobility", false);
        }
    }

    private static boolean hasUpgradeType(Player player, String upgradeType) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem) {
                for (int slot = 0; slot < 4; slot++) {
                    ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
                    if (!upgradeItem.isEmpty()) {
                        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                        if (upgrade != null && upgrade.getType().equals(upgradeType)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasPowerState(Player player, String upgradeType) {
        return ExoSuitPowerManager.isPowerEnabled(player, upgradeType);
    }
}