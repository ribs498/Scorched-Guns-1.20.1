package top.ribs.scguns.common.exosuit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.ExoSuitItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageSyncExoSuitUpgrades;
import top.ribs.scguns.network.message.S2CMessageSyncUpgradeRegistry;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handles ExoSuit effect application events and power management
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExoSuitEventHandler {

    private static int tickCounter = 0;
    private static final int UPDATE_FREQUENCY = 20;
    private static int syncCounter = 0;
    private static final int SYNC_INTERVAL = 200;
    /**
     * Updates ExoSuit effects periodically for all players
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            tickCounter++;

            if (tickCounter >= UPDATE_FREQUENCY) {
                tickCounter = 0;
                updatePlayerExoSuitEffects(event.player);
            }

            syncCounter++;
            if (syncCounter >= SYNC_INTERVAL) {
                syncCounter = 0;
                if (event.player instanceof ServerPlayer serverPlayer) {
                    periodicExoSuitSync(serverPlayer);
                }
            }
        }
    }
    private static void periodicExoSuitSync(ServerPlayer player) {
        List<ServerPlayer> nearbyPlayers = player.serverLevel().getEntitiesOfClass(
                ServerPlayer.class,
                player.getBoundingBox().inflate(128.0)
        );

        if (nearbyPlayers.size() <= 1) return;

        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit) {
                EquipmentSlot slot = getSlotForArmorType(exosuit);
                CompoundTag upgradeData = ExoSuitData.getUpgradeData(armorStack);

                for (ServerPlayer nearbyPlayer : nearbyPlayers) {
                    if (nearbyPlayer != player) {
                        PacketHandler.getPlayChannel().sendToPlayer(
                                () -> nearbyPlayer,
                                new S2CMessageSyncExoSuitUpgrades(player.getUUID(), slot, upgradeData)
                        );
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            updatePlayerExoSuitEffects(event.getEntity());
            Map<ResourceLocation, CompoundTag> upgradeData = ExoSuitUpgradeManager.serializeUpgrades();
            PacketHandler.getPlayChannel().sendToPlayer(
                    () -> serverPlayer,
                    new S2CMessageSyncUpgradeRegistry(upgradeData)
            );
            syncAllExoSuitPiecesToPlayer(serverPlayer);
        }
    }

    private static void syncAllExoSuitPiecesToPlayer(ServerPlayer joiningPlayer) {
        List<ServerPlayer> nearbyPlayers = joiningPlayer.serverLevel().getEntitiesOfClass(
                ServerPlayer.class,
                joiningPlayer.getBoundingBox().inflate(128.0)
        );

        for (ServerPlayer nearbyPlayer : nearbyPlayers) {
            for (ItemStack armorStack : nearbyPlayer.getArmorSlots()) {
                if (armorStack.getItem() instanceof ExoSuitItem) {
                    EquipmentSlot slot = getSlotForArmorType((ExoSuitItem) armorStack.getItem());
                    CompoundTag upgradeData = ExoSuitData.getUpgradeData(armorStack);

                    PacketHandler.getPlayChannel().sendToPlayer(
                            () -> joiningPlayer,
                            new S2CMessageSyncExoSuitUpgrades(nearbyPlayer.getUUID(), slot, upgradeData)
                    );
                }
            }
        }
    }

    private static EquipmentSlot getSlotForArmorType(ExoSuitItem exosuit) {
        return switch (exosuit.getType()) {
            case HELMET -> EquipmentSlot.HEAD;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case LEGGINGS -> EquipmentSlot.LEGS;
            case BOOTS -> EquipmentSlot.FEET;
        };
    }
    /**
     * Remove effects and clean up power data when player logs out
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (hasAnyExoSuitPiece(event.getEntity())) {
            ExoSuitEffectsHandler.removeExoSuitEffects(event.getEntity());
            ExoSuitNightVisionHandler.onPlayerLogout(event.getEntity());
        }
        ExoSuitPowerManager.cleanupPlayerData(event.getEntity().getUUID());
        ExoSuitEffectsHandler.cleanupPlayerData(event.getEntity().getUUID());
    }
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        boolean wasExoSuit = from.getItem() instanceof ExoSuitItem;
        boolean isExoSuit = to.getItem() instanceof ExoSuitItem;

        if (wasExoSuit || isExoSuit) {
            List<ServerPlayer> nearbyPlayers = player.serverLevel().getEntitiesOfClass(
                    ServerPlayer.class,
                    player.getBoundingBox().inflate(128.0)
            );

            CompoundTag upgradeData = isExoSuit ? ExoSuitData.getUpgradeData(to) : new CompoundTag();

            for (ServerPlayer nearbyPlayer : nearbyPlayers) {
                PacketHandler.getPlayChannel().sendToPlayer(
                        () -> nearbyPlayer,
                        new S2CMessageSyncExoSuitUpgrades(player.getUUID(), event.getSlot(), upgradeData)
                );
            }
            updatePlayerExoSuitEffects(player);
        }
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