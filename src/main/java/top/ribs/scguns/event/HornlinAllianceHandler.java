package top.ribs.scguns.event;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.HornlinEntity;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class HornlinAllianceHandler {

    private static final double ALLIANCE_RANGE = 26.0;
    private static final int ALERT_RANGE_Y = 10;

    @SubscribeEvent
    public static void onPiglinHurt(LivingHurtEvent event) {
        if (event.getAmount() <= 0) return;
        if (!(event.getEntity() instanceof Piglin piglin)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (piglin.level().isClientSide) return;

        AABB quickCheck = AABB.unitCubeFromLowerCorner(piglin.position()).inflate(ALLIANCE_RANGE, ALERT_RANGE_Y, ALLIANCE_RANGE);
        if (piglin.level().getEntitiesOfClass(HornlinEntity.class, quickCheck, EntitySelector.NO_SPECTATORS).isEmpty()) {
            return;
        }

        piglin.level().getEntitiesOfClass(HornlinEntity.class, quickCheck, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(hornlin -> hornlin.getTarget() == null)
                .filter(hornlin -> !hornlin.isAlliedTo(player))
                .forEach(hornlin -> {
                    hornlin.setTarget(player);
                });
    }

    @SubscribeEvent
    public static void onHornlinHurt(LivingHurtEvent event) {
        if (event.getAmount() <= 0) return;
        if (!(event.getEntity() instanceof HornlinEntity hornlin)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (hornlin.level().isClientSide) return;

        AABB quickCheck = AABB.unitCubeFromLowerCorner(hornlin.position()).inflate(ALLIANCE_RANGE, ALERT_RANGE_Y, ALLIANCE_RANGE);

        hornlin.level().getEntitiesOfClass(HornlinEntity.class, quickCheck, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(entity -> entity != hornlin)
                .filter(entity -> entity.getTarget() == null)
                .filter(entity -> !entity.isAlliedTo(player))
                .forEach(entity -> {
                    entity.setTarget(player);
                });

        if (hornlin.level().getEntitiesOfClass(Piglin.class, quickCheck, EntitySelector.NO_SPECTATORS).isEmpty()) {
            return;
        }

        hornlin.level().getEntitiesOfClass(Piglin.class, quickCheck, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(entity -> entity.getTarget() == null)
                .filter(entity -> !entity.isAlliedTo(player))
                .forEach(entity -> {
                    try {
                        var brain = entity.getBrain();
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT, player.getUUID());
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, player);
                        entity.setTarget(player);
                    } catch (Exception e) {
                        entity.setTarget(player);
                    }
                });
    }
}