package top.ribs.scguns.event;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import top.ribs.scguns.Config;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TemporaryLightManager {
    private static final Map<Long, LightData> temporaryLights = new ConcurrentHashMap<>();
    private static final int DEFAULT_LIGHT_DURATION = 4;
    private static final int BEAM_LIGHT_DURATION = 20;
    private static final int LIGHT_LEVEL = 7;
    private static final int FORCED_CLEANUP_INTERVAL = 1200; // 1 minute
    private static int cleanupCounter = 0;

    private static class LightData {
        int remainingTicks;
        final BlockState previousState;
        final ResourceKey<Level> dimension;

        LightData(int ticks, BlockState previousState, ResourceKey<Level> dimension) {
            this.remainingTicks = ticks;
            this.previousState = previousState;
            this.dimension = dimension;
        }
    }

    public static void addTemporaryLight(Level level, BlockPos pos, boolean isBeamWeapon) {
        if (!Config.CLIENT.display.fireLights.get() || level.isClientSide) {
            return;
        }

        long posKey = pos.asLong();
        BlockState currentState = level.getBlockState(pos);

        // Only place light in air blocks
        if (!currentState.isAir()) {
            return;
        }

        int duration = isBeamWeapon ? BEAM_LIGHT_DURATION : DEFAULT_LIGHT_DURATION;

        // Update existing light
        if (temporaryLights.containsKey(posKey)) {
            LightData data = temporaryLights.get(posKey);
            if (data.dimension == level.dimension()) {
                if (isBeamWeapon) {
                    data.remainingTicks = Math.min(data.remainingTicks + duration, BEAM_LIGHT_DURATION * 2);
                } else {
                    data.remainingTicks = duration;
                }
            }
            return;
        }

        // Create new light
        level.setBlock(pos, Blocks.LIGHT.defaultBlockState()
                .setValue(LightBlock.LEVEL, LIGHT_LEVEL), 3);
        temporaryLights.put(posKey, new LightData(duration, currentState, level.dimension()));
    }

    public static void tickLights(Level level) {
        if (level.isClientSide) return;

        cleanupCounter++;
        if (cleanupCounter >= FORCED_CLEANUP_INTERVAL) {
            forceCleanup(level);
            cleanupCounter = 0;
            return;
        }

        Iterator<Map.Entry<Long, LightData>> iterator = temporaryLights.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, LightData> entry = iterator.next();
            long posKey = entry.getKey();
            LightData data = entry.getValue();
            BlockPos pos = BlockPos.of(posKey);

            // Skip if not in this dimension
            if (data.dimension != level.dimension()) {
                continue;
            }

            // Skip if chunk isn't loaded
            if (!level.hasChunkAt(pos)) {
                continue;
            }

            data.remainingTicks--;

            if (data.remainingTicks <= 0) {
                BlockState currentState = level.getBlockState(pos);
                if (currentState.is(Blocks.LIGHT)) {
                    level.setBlock(pos, data.previousState, 3);
                }
                iterator.remove();
            }
        }
    }

    private static void forceCleanup(Level level) {
        Iterator<Map.Entry<Long, LightData>> iterator = temporaryLights.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, LightData> entry = iterator.next();
            long posKey = entry.getKey();
            LightData data = entry.getValue();

            if (data.dimension != level.dimension()) {
                continue;
            }

            BlockPos pos = BlockPos.of(posKey);
            if (level.hasChunkAt(pos)) {
                BlockState currentState = level.getBlockState(pos);
                if (currentState.is(Blocks.LIGHT)) {
                    level.setBlock(pos, data.previousState, 3);
                }
                iterator.remove();
            }
        }
    }

    public static void cleanup(Level level) {
        if (level.isClientSide) return;

        Iterator<Map.Entry<Long, LightData>> iterator = temporaryLights.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, LightData> entry = iterator.next();
            BlockPos pos = BlockPos.of(entry.getKey());
            LightData data = entry.getValue();

            if (data.dimension == level.dimension() && level.hasChunkAt(pos)) {
                BlockState currentState = level.getBlockState(pos);
                if (currentState.is(Blocks.LIGHT)) {
                    level.setBlock(pos, data.previousState, 3);
                }
            }
            iterator.remove();
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cleanup(player.level());
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) {
            cleanup((Level) event.getLevel());
        }
    }
}