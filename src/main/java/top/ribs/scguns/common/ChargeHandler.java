package top.ribs.scguns.common;


import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.GunItem;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChargeHandler {
    private static final Map<UUID, Integer> playerChargeTime = new HashMap<>();
    private static final Map<UUID, Integer> playerMaxChargeTime = new HashMap<>();
    private static final Map<UUID, Float> lastChargeProgress = new HashMap<>();

    public static int getChargeTime(UUID playerId) {
        return playerChargeTime.getOrDefault(playerId, 0);
    }

    public static void updateChargeTime(Player player, ItemStack weapon, boolean isCharging) {
        if (!(weapon.getItem() instanceof GunItem gunItem)) {
            return;
        }

        UUID playerId = player.getUUID();
        Gun modifiedGun = gunItem.getModifiedGun(weapon);
        int maxChargeTime = modifiedGun.getGeneral().getFireTimer();
        playerMaxChargeTime.put(playerId, maxChargeTime);

        if (isCharging) {
            int currentCharge = playerChargeTime.getOrDefault(playerId, 0);
            currentCharge++;
            if (currentCharge > maxChargeTime) {
                currentCharge = maxChargeTime;
            }
            playerChargeTime.put(playerId, currentCharge);

            float progress = maxChargeTime > 0 ? Math.min(1.0f, (float)currentCharge / maxChargeTime) : 0f;
            lastChargeProgress.put(playerId, progress);
        } else {
            int previousCharge = playerChargeTime.getOrDefault(playerId, 0);
            if (previousCharge > 0) {
            }
            playerChargeTime.remove(playerId);
        }
    }

    public static float getChargeProgress(@Nullable Player player, ItemStack weapon) {
        if (player == null || !(weapon.getItem() instanceof GunItem)) {
            return 0f;
        }
        UUID playerId = player.getUUID();
        return lastChargeProgress.getOrDefault(playerId, 0f);
    }

    public static void clearLastChargeProgress(UUID playerId) {
        lastChargeProgress.remove(playerId);
    }

}