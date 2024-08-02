package top.ribs.scguns.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArmorStandRemovalHandler {
    private static final Map<ArmorStand, Integer> armorStandsToRemove = new HashMap<>();

    public static void trackArmorStand(ArmorStand armorStand, int ticks) {
        armorStandsToRemove.put(armorStand, ticks);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !armorStandsToRemove.isEmpty()) {
            Iterator<Map.Entry<ArmorStand, Integer>> iterator = armorStandsToRemove.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<ArmorStand, Integer> entry = iterator.next();
                ArmorStand armorStand = entry.getKey();
                int ticksRemaining = entry.getValue();

                if (ticksRemaining <= 0) {
                    if (armorStand.isAlive()) {
                        armorStand.remove(Entity.RemovalReason.DISCARDED);
                    }
                    iterator.remove();
                } else {
                    entry.setValue(ticksRemaining - 1);
                }
            }
        }
    }
}