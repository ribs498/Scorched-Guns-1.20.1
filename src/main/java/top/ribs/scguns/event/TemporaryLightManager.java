package top.ribs.scguns.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.Config;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.block.TemporaryLightBlock;

public class TemporaryLightManager {
    private static final int DEFAULT_LIGHT_DURATION = 8;
    private static final int BEAM_LIGHT_DURATION = 20;
    private static final int LIGHT_LEVEL = 7;

    public static void addTemporaryLight(Level level, BlockPos pos, boolean isBeamWeapon) {

        if (level.isClientSide) return;

        try {
            if (Config.CLIENT == null || Config.CLIENT.display == null) return;

            boolean fireLightsEnabled;
            try {
                fireLightsEnabled = Config.CLIENT.display.fireLights.get();
            } catch (IllegalStateException e) {
                return;
            }

            if (!fireLightsEnabled) return;

            BlockState currentState = level.getBlockState(pos);
            if (!canPlaceLightAt(level, pos, currentState)) return;

            int duration = isBeamWeapon ? BEAM_LIGHT_DURATION : DEFAULT_LIGHT_DURATION;

            if (currentState.getBlock() instanceof TemporaryLightBlock) {
                int currentLifetime = currentState.getValue(TemporaryLightBlock.LIFETIME);
                int newLifetime;

                if (isBeamWeapon) {
                    newLifetime = Math.min(currentLifetime + duration, BEAM_LIGHT_DURATION * 2);
                } else {
                    newLifetime = duration;
                }

                BlockState newState = currentState.setValue(TemporaryLightBlock.LIFETIME, newLifetime);
                level.setBlock(pos, newState, 2);
                return;
            }

            BlockState lightState = ModBlocks.TEMPORARY_LIGHT.get().defaultBlockState()
                    .setValue(TemporaryLightBlock.LIGHT_LEVEL, LIGHT_LEVEL)
                    .setValue(TemporaryLightBlock.LIFETIME, duration);

            level.setBlock(pos, lightState, 3);
            level.sendBlockUpdated(pos, currentState, lightState, 3);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.scheduleTick(pos, ModBlocks.TEMPORARY_LIGHT.get(), 1);
            }

        } catch (Exception e) {
            ScorchedGuns.LOGGER.error("Error in addTemporaryLight: " + e.getMessage(), e);
        }
    }

    private static boolean canPlaceLightAt(Level level, BlockPos pos, BlockState currentState) {
        if (!level.hasChunkAt(pos)) return false;

        if (!currentState.getFluidState().isEmpty()) return false;

        if (currentState.isAir()) return true;
        if (currentState.getBlock() instanceof TemporaryLightBlock) return true;
        if (currentState.is(Blocks.LIGHT)) return true;

        return currentState.canBeReplaced();
    }


    public static void emergencyCleanup(Level level) {
        if (level == null || level.isClientSide) return;

        try {
            ScorchedGuns.LOGGER.info("Emergency cleanup called for temporary lights in dimension: " +
                    level.dimension().location());
        } catch (Exception e) {
            ScorchedGuns.LOGGER.error("Error during emergency cleanup: " + e.getMessage(), e);
        }
    }
}