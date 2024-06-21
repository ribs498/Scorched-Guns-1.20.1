package top.ribs.scguns.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.client.*;
import top.ribs.scguns.item.GunItem;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEventsBus {
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.COG_MINION_LAYER, CogMinionModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.COG_KNIGHT_LAYER, CogKnightModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SKY_CARRIER_LAYER, SkyCarrierModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SUPPLY_SCAMP_LAYER, SupplyScampModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.REDCOAT_LAYER, RedcoatModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.DISSIDENT_LAYER, DissidentModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.HIVE_LAYER, HiveModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SWARM_LAYER, SwarmModel::createBodyLayer);

    }
}

