package top.ribs.scguns.common.exosuit;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.exosuit.RabbitModuleItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExoSuitRabbitHandler {

    private static final int REFRESH_INTERVAL = 100;
    private static final double MOVEMENT_THRESHOLD = 0.01;
    private static final Map<UUID, Vec3> previousPositions = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;

        if (player.tickCount % 20 != 0) {
            return;
        }

        handleRabbitModuleEnergy(player);
    }

    private static void handleRabbitModuleEnergy(Player player) {
        if (!ExoSuitPowerManager.isPowerEnabled(player, "mobility")) {
            return;
        }

        if (!hasRabbitModule(player)) {
            return;
        }
        if (!isPlayerMoving(player)) {
            return;
        }

        if (!ExoSuitPowerManager.canConsumeEnergy(player, "mobility", REFRESH_INTERVAL)) {
            return;
        }

        if (!ExoSuitPowerManager.canUpgradeFunction(player, "mobility")) {
            return;
        }

        ItemStack rabbitUpgrade = findRabbitModule(player);
        if (!rabbitUpgrade.isEmpty() &&
                rabbitUpgrade.getItem() instanceof RabbitModuleItem rabbitModule) {
            if (!rabbitModule.canFunctionWithoutPower()) {
                ExoSuitPowerManager.consumeEnergyForUpgrade(player, "mobility", rabbitUpgrade);
            }
        }
    }

    /**
     * Check if the player is currently moving by comparing their current position
     * with their previous position
     */
    private static boolean isPlayerMoving(Player player) {
        UUID playerId = player.getUUID();
        Vec3 currentPos = player.position();
        Vec3 previousPos = previousPositions.get(playerId);

        previousPositions.put(playerId, currentPos);

        if (previousPos == null) {
            return false;
        }

        double distanceMoved = currentPos.distanceTo(previousPos);

        boolean positionChanged = distanceMoved > MOVEMENT_THRESHOLD;
        boolean hasVelocity = player.getDeltaMovement().lengthSqr() > MOVEMENT_THRESHOLD * MOVEMENT_THRESHOLD;
        boolean isWalking = player.isSprinting() || player.isSwimming() || player.isCrouching() || player.zza != 0 || player.xxa != 0;

        return positionChanged || hasVelocity || isWalking;
    }

    private static boolean hasRabbitModule(Player player) {
        return !findRabbitModule(player).isEmpty();
    }

    private static ItemStack findRabbitModule(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof top.ribs.scguns.item.animated.ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.BOOTS) {

                for (int slot = 0; slot < 4; slot++) {
                    ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
                    if (!upgradeItem.isEmpty()) {
                        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                        if (upgrade != null && upgrade.getType().equals("mobility") &&
                                upgradeItem.getItem() instanceof RabbitModuleItem) {
                            return upgradeItem;
                        }
                    }
                }
                break;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void onPlayerLogout(Player player) {
        previousPositions.remove(player.getUUID());
    }
}