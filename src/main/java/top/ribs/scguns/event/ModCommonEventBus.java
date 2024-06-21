package top.ribs.scguns.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.*;
import top.ribs.scguns.init.ModEntities;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCommonEventBus {

    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.COG_MINION.get(), CogMinionEntity.createAttributes().build());
        event.put(ModEntities.SUPPLY_SCAMP.get(), SupplyScampEntity.createAttributes().build());
        event.put(ModEntities.COG_KNIGHT.get(), CogKnightEntity.createAttributes().build());
        event.put(ModEntities.SKY_CARRIER.get(), SkyCarrierEntity.createAttributes().build());
        event.put(ModEntities.DISSIDENT.get(), DissidentEntity.createAttributes().build());
        event.put(ModEntities.REDCOAT.get(), RedcoatEntity.createAttributes().build());
        event.put(ModEntities.HIVE.get(), HiveEntity.createAttributes().build());
        event.put(ModEntities.SWARM.get(), SwarmEntity.createAttributes().build());

    }

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {

    }

}
