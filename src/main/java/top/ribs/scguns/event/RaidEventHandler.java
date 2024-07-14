package top.ribs.scguns.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.BlundererEntity;
import top.ribs.scguns.init.ModEntities;
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class RaidEventHandler {

    @SubscribeEvent
    public static void onRaidSpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Raider raider) {
            Level level = event.getLevel();
            if (level instanceof ServerLevel serverLevel) {
                Raid raid = serverLevel.getRaidAt(raider.blockPosition());
                if (raid != null) {
                    int wave = raid.getGroupsSpawned();
                    if (shouldAddBlunderer(raid, wave)) {
                        if (!isBlundererAlreadyInRaid(raid)) {
                            BlundererEntity blunderer = ModEntities.BLUNDERER.get().create(serverLevel);
                            if (blunderer != null) {
                                BlockPos pos = raider.blockPosition();
                                blunderer.setPos(pos.getX(), pos.getY(), pos.getZ());
                                raid.joinRaid(wave, blunderer, pos, false);
                                serverLevel.addFreshEntity(blunderer);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean shouldAddBlunderer(Raid raid, int wave) {
        return wave > 1; // Example: add Blunderer from wave 2 onwards
    }

    private static boolean isBlundererAlreadyInRaid(Raid raid) {
        for (Raider raider : raid.getAllRaiders()) {
            if (raider instanceof BlundererEntity) {
                return true;
            }
        }
        return false;
    }
}


