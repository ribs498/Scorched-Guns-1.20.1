package top.ribs.scguns.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class BeamHandlerCommon {
    public static class BeamMiningManager {
        private static final Map<UUID, Integer> playerBreakingIds = new HashMap<>();
        private static final Map<BlockPos, MiningProgress> miningProgress = new HashMap<>();
        private static int nextBreakerId = 1;
        private static final long RESET_TIMEOUT = 1000;

        private static class MiningProgress {
            float progress;
            long lastUpdate;
            UUID minerId;
            int breakerId;
            boolean isActive;
            int lastStage;

            public MiningProgress(UUID minerId) {
                this.progress = 0f;
                this.lastUpdate = System.currentTimeMillis();
                this.minerId = minerId;
                this.breakerId = playerBreakingIds.computeIfAbsent(minerId, k -> nextBreakerId++);
                this.isActive = true;
                this.lastStage = -1;
            }
        }

        public static void updateBlockMining(Level world, BlockPos pos, ServerPlayer player, Gun modifiedGun) {
            if (!modifiedGun.getGeneral().canMine()) {
                return;
            }

            BlockState state = world.getBlockState(pos);
            if (state.isAir()) {
                return;
            }

            float hardness = state.getDestroySpeed(world, pos);
            if (hardness < 0) {
                return;
            }

            MiningProgress progress = miningProgress.computeIfAbsent(pos, k -> new MiningProgress(player.getUUID()));

            if (!progress.minerId.equals(player.getUUID())) {
                return;
            }

            progress.isActive = true;
            progress.lastUpdate = System.currentTimeMillis();

            float miningSpeed = modifiedGun.getGeneral().getMiningSpeed();
            float progressIncrement = miningSpeed / (hardness * 10);
            progress.progress += progressIncrement;

            int newStage = Math.min((int) (progress.progress * 10.0F), 9);

            if (newStage != progress.lastStage) {
                progress.lastStage = newStage;
                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.destroyBlockProgress(progress.breakerId, pos, newStage);
                }
            }

            if (progress.progress >= 1.0F) {
                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.destroyBlockProgress(progress.breakerId, pos, -1);
                }
                miningProgress.remove(pos);

                if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                    world.removeBlock(pos, false);
                } else {
                    BlockState blockState = world.getBlockState(pos);
                    BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(pos) : null;
                    Block.dropResources(blockState, world, pos, blockEntity, player, player.getMainHandItem());
                    world.removeBlock(pos, false);
                    world.levelEvent(2001, pos, Block.getId(blockState));
                }
            }
        }

        public static void tickMiningProgress(Level world) {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<BlockPos, MiningProgress>> iterator = miningProgress.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<BlockPos, MiningProgress> entry = iterator.next();
                MiningProgress progress = entry.getValue();
                if (!progress.isActive && (currentTime - progress.lastUpdate) >= RESET_TIMEOUT) {
                    if (world instanceof ServerLevel serverLevel) {
                        serverLevel.destroyBlockProgress(progress.breakerId, entry.getKey(), -1);
                    }
                    iterator.remove();
                } else if (progress.isActive || (currentTime - progress.lastUpdate) < 50) {
                    progress.isActive = false;
                }
            }
        }
    }

    // You can add any other common functionality here that needs to work on both client and server
    public static void resetMiningProgress() {
        BeamMiningManager.miningProgress.clear();
        BeamMiningManager.playerBreakingIds.clear();
        BeamMiningManager.nextBreakerId = 1;
    }
}
