package top.ribs.scguns.event;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
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
        event.put(ModEntities.BLUNDERER.get(), BlundererEntity.createAttributes().build());
        event.put(ModEntities.HIVE.get(), HiveEntity.createAttributes().build());
        event.put(ModEntities.SWARM.get(), SwarmEntity.createAttributes().build());
        event.put(ModEntities.HORNLIN.get(), HornlinEntity.createAttributes().build());
        event.put(ModEntities.ZOMBIFIED_HORNLIN.get(), ZombifiedHornlinEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
                ModEntities.COG_MINION.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                CogMinionEntity::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
        event.register(
                ModEntities.SUPPLY_SCAMP.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                SupplyScampEntity::checkAnimalSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
        event.register(
                ModEntities.BLUNDERER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BlundererEntity::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );

        event.register(
                ModEntities.COG_KNIGHT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                CogKnightEntity::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
        event.register(
                ModEntities.SKY_CARRIER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                SkyCarrierEntity::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
        event.register(
                ModEntities.DISSIDENT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                DissidentEntity::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
        event.register(
                ModEntities.REDCOAT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                RedcoatEntity::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );

        event.register(
                ModEntities.HIVE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                HiveEntity::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
        event.register(
                ModEntities.HORNLIN.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                HornlinEntity::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
        event.register(
                ModEntities.ZOMBIFIED_HORNLIN.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                HornlinEntity::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
    }

}