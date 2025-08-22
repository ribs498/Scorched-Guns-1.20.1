package top.ribs.scguns.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VehicleDetectionHandler {

    // Enhanced cache to track both vehicle state and type
    private static final Map<UUID, VehicleInfo> playersInVehicles = new HashMap<>();

    public static class VehicleInfo {
        public final String vehicleType;
        public final Entity vehicle;

        public VehicleInfo(String vehicleType, Entity vehicle) {
            this.vehicleType = vehicleType;
            this.vehicle = vehicle;
        }
    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Entity vehicle = event.getEntityBeingMounted();

        if (event.isMounting()) {
            if (isVehicle(vehicle)) {
                String vehicleType = getVehicleType(vehicle);
                playersInVehicles.put(player.getUUID(), new VehicleInfo(vehicleType, vehicle));

                ScorchedGuns.LOGGER.debug("Player {} mounted {}: {} - Gun poses will be adjusted",
                        player.getName().getString(), vehicleType, vehicle.getClass().getSimpleName());
            }
        } else {
            if (playersInVehicles.remove(player.getUUID()) != null) {
                ScorchedGuns.LOGGER.debug("Player {} dismounted - Gun poses restored to normal",
                        player.getName().getString());
            }
        }
    }

    /**
     * Check if player is in a vehicle
     */
    public static boolean isPlayerInVehicle(Player player) {
        return playersInVehicles.containsKey(player.getUUID()) ||
                (player.isPassenger() && isVehicle(player.getVehicle()));
    }

    /**
     * Get vehicle info for a player
     */
    public static VehicleInfo getPlayerVehicleInfo(Player player) {
        VehicleInfo cached = playersInVehicles.get(player.getUUID());
        if (cached != null) return cached;

        // Fallback check
        if (player.isPassenger() && isVehicle(player.getVehicle())) {
            Entity vehicle = player.getVehicle();
            return new VehicleInfo(getVehicleType(vehicle), vehicle);
        }

        return null;
    }

    /**
     * Get vehicle type for pose adjustments
     */
    public static String getVehicleTypeForPlayer(Player player) {
        VehicleInfo info = getPlayerVehicleInfo(player);
        return info != null ? info.vehicleType : null;
    }

    private static boolean isVehicle(Entity entity) {
        if (entity == null) return false;
        return entity instanceof Boat ||
                entity instanceof AbstractMinecart ||
                entity instanceof AbstractHorse ||
                entity.getClass().getSimpleName().toLowerCase().contains("vehicle");
    }

    private static String getVehicleType(Entity vehicle) {
        if (vehicle == null) return "unknown";
        if (vehicle instanceof Boat) return "boat";
        if (vehicle instanceof AbstractMinecart) return "minecart";
        if (vehicle instanceof AbstractHorse) return "horse";
        return vehicle.getClass().getSimpleName().toLowerCase();
    }

    public static void cleanup() {
        playersInVehicles.clear();
    }
}