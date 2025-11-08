package top.ribs.scguns.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModItems;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class BatPoopEvent {

    private static final String WELL_FED_TAG = "scguns:well_fed";
    private static final String WELL_FED_TIMER_TAG = "scguns:well_fed_timer";
    private static final String LAST_POOP_TIME_TAG = "scguns:last_poop_time";
    private static final int POOP_COOLDOWN = 1000;
    private static final float BASE_POOP_CHANCE = 0.00055f;
    private static final float WELL_FED_MULTIPLIER = 2.5f;
    private static final int WELL_FED_DURATION = 2000;
    private static final int MAX_CHECK_DEPTH = 48;

    @SubscribeEvent
    public static void onBatTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Bat bat)) return;
        if (bat.level().isClientSide()) return;

        updateWellFedStatus(bat);

        if (!bat.isResting()) return;

        int lastPoopTime = bat.getPersistentData().getInt(LAST_POOP_TIME_TAG);
        if (bat.tickCount - lastPoopTime < POOP_COOLDOWN) {
            return;
        }

        float poopChance = BASE_POOP_CHANCE;
        if (isWellFed(bat)) {
            poopChance *= WELL_FED_MULTIPLIER;
        }

        if (bat.getRandom().nextFloat() < poopChance) {
            if (!hasFullGuanoLayerBelow(bat)) {
                dropGuano(bat);
                bat.getPersistentData().putInt(LAST_POOP_TIME_TAG, bat.tickCount);
            }
        }
    }

    private static boolean hasFullGuanoLayerBelow(Bat bat) {
        BlockPos startPos = bat.blockPosition().below();

        for (int i = 0; i < MAX_CHECK_DEPTH; i++) {
            BlockPos checkPos = startPos.below(i);
            BlockState state = bat.level().getBlockState(checkPos);

            if (!state.isAir()) {
                if (state.is(ModBlocks.BAT_GUANO_LAYER.get())) {
                    int layers = state.getValue(SnowLayerBlock.LAYERS);
                    return layers >= 8;
                }
                return false;
            }
        }

        return false;
    }

    private static void dropGuano(Bat bat) {
        ItemStack guanoStack = new ItemStack(ModItems.BAT_GUANO.get());
        ItemEntity guanoEntity = new ItemEntity(
                bat.level(),
                bat.getX(),
                bat.getY() - 0.3,
                bat.getZ(),
                guanoStack
        );

        guanoEntity.setDeltaMovement(
                (bat.getRandom().nextDouble() - 0.5) * 0.02,
                -0.1,
                (bat.getRandom().nextDouble() - 0.5) * 0.02
        );

        bat.level().addFreshEntity(guanoEntity);
    }

    public static void setWellFed(Bat bat) {
        bat.getPersistentData().putBoolean(WELL_FED_TAG, true);
        bat.getPersistentData().putInt(WELL_FED_TIMER_TAG, WELL_FED_DURATION);
    }

    public static boolean isWellFed(Bat bat) {
        return bat.getPersistentData().getBoolean(WELL_FED_TAG);
    }

    private static void updateWellFedStatus(Bat bat) {
        if (!isWellFed(bat)) return;

        int timer = bat.getPersistentData().getInt(WELL_FED_TIMER_TAG);
        timer--;

        if (timer <= 0) {
            bat.getPersistentData().putBoolean(WELL_FED_TAG, false);
            bat.getPersistentData().remove(WELL_FED_TIMER_TAG);
        } else {
            bat.getPersistentData().putInt(WELL_FED_TIMER_TAG, timer);
        }
    }
}