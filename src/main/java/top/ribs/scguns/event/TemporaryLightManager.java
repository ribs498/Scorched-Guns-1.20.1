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
import top.ribs.scguns.ScorchedGuns;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TemporaryLightManager {
    static final Map<Long, LightData> temporaryLights = new ConcurrentHashMap<>();
    private static final int DEFAULT_LIGHT_DURATION = 4;
    private static final int BEAM_LIGHT_DURATION = 20;
    private static final int LIGHT_LEVEL = 7;
    private static final int FORCED_CLEANUP_INTERVAL = 1200;
    private static int cleanupCounter = 0;

    public static class LightData {
        int remainingTicks;
        final BlockState previousState;
        final ResourceKey<Level> dimension;
        final long creationTime;

        LightData(int ticks, BlockState previousState, ResourceKey<Level> dimension) {
            this.remainingTicks = ticks;
            this.previousState = previousState;
            this.dimension = dimension;
            this.creationTime = System.currentTimeMillis();
        }
    }

    public static void addTemporaryLight(Level level, BlockPos pos, boolean isBeamWeapon) {
        if (level.isClientSide) {
            return;
        }

        try {
            if (Config.CLIENT == null || Config.CLIENT.display == null) {
                return;
            }

            boolean fireLightsEnabled = false;
            try {
                fireLightsEnabled = Config.CLIENT.display.fireLights.get();
            } catch (IllegalStateException e) {
                return;
            }

            if (!fireLightsEnabled) {
                return;
            }

            long posKey = pos.asLong();
            BlockState currentState = level.getBlockState(pos);
            if (currentState.is(Blocks.LIGHT)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                currentState = Blocks.AIR.defaultBlockState();
            }
            if (!currentState.isAir()) {
                return;
            }

            int duration = isBeamWeapon ? BEAM_LIGHT_DURATION : DEFAULT_LIGHT_DURATION;
            if (temporaryLights.containsKey(posKey)) {
                LightData data = temporaryLights.get(posKey);
                if (data.dimension == level.dimension()) {
                    if (System.currentTimeMillis() - data.creationTime > 30000) {
                        removeLight(level, pos);
                        temporaryLights.remove(posKey);
                    } else {
                        if (isBeamWeapon) {
                            data.remainingTicks = Math.min(data.remainingTicks + duration, BEAM_LIGHT_DURATION * 2);
                        } else {
                            data.remainingTicks = duration;
                        }
                    }
                }
                return;
            }
            BlockState lightBlock = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LIGHT_LEVEL);
            level.setBlock(pos, lightBlock, 3);
            level.sendBlockUpdated(pos, currentState, lightBlock, 3);
            temporaryLights.put(posKey, new LightData(duration, currentState, level.dimension()));
        } catch (Exception e) {
            ScorchedGuns.LOGGER.error("Error in addTemporaryLight: " + e.getMessage(), e);
            removeLight(level, pos);
        }
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

            if (data.dimension != level.dimension()) {
                continue;
            }

            if (!level.hasChunkAt(pos)) {
                continue;
            }

            if (System.currentTimeMillis() - data.creationTime > 30000) {
                removeLight(level, pos);
                iterator.remove();
                continue;
            }

            data.remainingTicks--;

            if (data.remainingTicks <= 0) {
                removeLight(level, pos);
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
                removeLight(level, pos);
                iterator.remove();
            }
        }
    }

    public static void cleanup(Level level) {
        if (level == null) return;
        if (level.isClientSide()) {
            cleanupClientLights(level);
            return;
        }
        Iterator<Map.Entry<Long, LightData>> iterator = temporaryLights.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, LightData> entry = iterator.next();
            BlockPos pos = BlockPos.of(entry.getKey());
            LightData data = entry.getValue();

            if (data.dimension == level.dimension()) {
                if (level.hasChunkAt(pos)) {
                    removeLight(level, pos);
                }
                iterator.remove();
            }
        }
        cleanupCounter = 0;
    }
    private static void cleanupClientLights(Level level) {
        temporaryLights.clear();
    }

    private static void removeLight(Level level, BlockPos pos) {
        try {
            if (level == null || !level.hasChunkAt(pos)) return;

            BlockState currentState = level.getBlockState(pos);
            if (currentState.is(Blocks.LIGHT)) {
                BlockState airState = Blocks.AIR.defaultBlockState();
                level.setBlock(pos, airState, 3);
                level.sendBlockUpdated(pos, currentState, airState, 3);
            }
        } catch (Exception e) {
            ScorchedGuns.LOGGER.error("Error removing light: " + e.getMessage(), e);
        }
    }
    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save event) {
        if (!event.getLevel().isClientSide()) {
            cleanup((Level) event.getLevel());
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