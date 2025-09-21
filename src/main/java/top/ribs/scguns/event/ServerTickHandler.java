package top.ribs.scguns.event;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.ribs.scguns.faction.patrol.GunnerPatrolSpawner;
import top.ribs.scguns.faction.raid.GunnerRaidSpawner;

public class ServerTickHandler {
    private static final GunnerPatrolSpawner spawner = new GunnerPatrolSpawner();
    private static final GunnerRaidSpawner raid = new GunnerRaidSpawner();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            spawner.tick(event.getServer().overworld(), true, false);
            raid.tick(event.getServer().overworld(), true, false);
        }
    }
}