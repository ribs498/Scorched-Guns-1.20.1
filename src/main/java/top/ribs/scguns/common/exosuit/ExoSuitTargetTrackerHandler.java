package top.ribs.scguns.common.exosuit;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.exosuit.TargetTrackerModuleItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExoSuitTargetTrackerHandler {

    private static final int REFRESH_INTERVAL = 50;
    private static final double DETECTION_RADIUS = 16.0;
    private static final int GLOW_DURATION = 120;

    private static final Map<UUID, Long> playerLastUpdate = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;

        if (player.tickCount % 10 != 0) {
            return;
        }

        handleTargetTracker(player);
    }

    private static void handleTargetTracker(Player player) {
        boolean targetTrackerEnabled = ExoSuitPowerManager.isPowerEnabled(player, "hud");

        if (!targetTrackerEnabled) {
            removeEntityHighlights(player);
            return;
        }

        if (!hasTargetTrackerModule(player)) {
            removeEntityHighlights(player);
            return;
        }

        if (!ExoSuitPowerManager.canConsumeEnergy(player, "hud", REFRESH_INTERVAL)) {
            return;
        }

        if (!ExoSuitPowerManager.canUpgradeFunction(player, "hud")) {
            removeEntityHighlights(player);
            return;
        }

        ItemStack helmetUpgrade = findTargetTrackerModule(player);
        if (!helmetUpgrade.isEmpty() && helmetUpgrade.getItem() instanceof TargetTrackerModuleItem targetTrackerModule) {
            if (!targetTrackerModule.canFunctionWithoutPower()) {
                if (!ExoSuitPowerManager.consumeEnergyForUpgrade(player, "hud", helmetUpgrade)) {
                    removeEntityHighlights(player);
                    return;
                }
            }
        }

        highlightNearbyEntities(player);
        playerLastUpdate.put(player.getUUID(), player.level().getGameTime());
    }

    private static void highlightNearbyEntities(Player player) {
        AABB detectionArea = new AABB(
                player.getX() - DETECTION_RADIUS,
                player.getY() - DETECTION_RADIUS,
                player.getZ() - DETECTION_RADIUS,
                player.getX() + DETECTION_RADIUS,
                player.getY() + DETECTION_RADIUS,
                player.getZ() + DETECTION_RADIUS
        );

        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                detectionArea,
                entity -> entity != player && entity.isAlive() && !entity.isInvisible()
        );

        for (LivingEntity entity : nearbyEntities) {
            if (!entity.hasEffect(MobEffects.GLOWING)) {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        MobEffects.GLOWING,
                        GLOW_DURATION,
                        0,
                        false,
                        false,
                        false
                ));
            } else {
                net.minecraft.world.effect.MobEffectInstance currentGlow = entity.getEffect(MobEffects.GLOWING);
                if (currentGlow != null && currentGlow.getDuration() < 60) {
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            MobEffects.GLOWING,
                            GLOW_DURATION,
                            0,
                            false,
                            false,
                            false
                    ));
                }
            }
        }
    }

    private static void removeEntityHighlights(Player player) {

        playerLastUpdate.remove(player.getUUID());
    }

    public static boolean hasTargetTrackerModule(Player player) {
        return !findTargetTrackerModule(player).isEmpty();
    }

    public static boolean isTargetTrackerActive(Player player) {
        return hasTargetTrackerModule(player) &&
                ExoSuitPowerManager.isPowerEnabled(player, "hud") &&
                ExoSuitPowerManager.canUpgradeFunction(player, "hud");
    }

    private static ItemStack findTargetTrackerModule(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof top.ribs.scguns.item.animated.ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.HELMET) {
                for (int slot = 0; slot < 4; slot++) {
                    ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
                    if (!upgradeItem.isEmpty()) {
                        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                        if (upgrade != null && upgrade.getType().equals("hud") &&
                                upgradeItem.getItem() instanceof TargetTrackerModuleItem) {
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
        playerLastUpdate.remove(player.getUUID());
    }
    public static void onPlayerDeath(Player player) {
        removeEntityHighlights(player);
    }
}