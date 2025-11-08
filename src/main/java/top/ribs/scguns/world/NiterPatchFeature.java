package top.ribs.scguns.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import top.ribs.scguns.block.NiterLayerBlock;

public class NiterPatchFeature extends Feature<NiterPatchConfiguration> {

    public NiterPatchFeature(Codec<NiterPatchConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NiterPatchConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        NiterPatchConfiguration config = context.config();

        if (config.niterBlock() == null) {
            return false;
        }

        if (!isInCave(level, origin)) {
            return false;
        }

        BlockPos floorPos = findFloor(level, origin);
        if (floorPos == null) {
            return false;
        }

        int placed = 0;
        int attempts = config.spreadAttempts();

        for (int i = 0; i < attempts; i++) {
            int offsetX = random.nextInt(config.spreadRadius() * 2 + 1) - config.spreadRadius();
            int offsetZ = random.nextInt(config.spreadRadius() * 2 + 1) - config.spreadRadius();
            BlockPos targetPos = floorPos.offset(offsetX, 0, offsetZ);

            BlockPos foundFloor = findFloor(level, targetPos);
            if (foundFloor != null) {
                BlockPos placePos = foundFloor.above();
                BlockState placeAtState = level.getBlockState(placePos);

                if ((placeAtState.isAir() || placeAtState.canBeReplaced()) &&
                        level.getBlockState(foundFloor).isFaceSturdy(level, foundFloor, Direction.UP)) {

                    int layers = random.nextInt(config.maxLayers() - config.minLayers() + 1) + config.minLayers();
                    BlockState niterState = config.niterBlock().defaultBlockState();

                    if (niterState.hasProperty(NiterLayerBlock.LAYERS)) {
                        niterState = niterState.setValue(NiterLayerBlock.LAYERS, layers);
                        level.setBlock(placePos, niterState, 3);
                        placed++;
                    }
                }
            }
        }

        return placed > 0;
    }

    private boolean isInCave(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir() &&
                level.getBlockState(pos.above()).isAir();
    }

    private BlockPos findFloor(WorldGenLevel level, BlockPos startPos) {
        BlockPos.MutableBlockPos mutablePos = startPos.mutable();

        for (int y = 0; y < 10; y++) {
            BlockState currentState = level.getBlockState(mutablePos);
            BlockState aboveState = level.getBlockState(mutablePos.above());

            if (currentState.isFaceSturdy(level, mutablePos, Direction.UP) &&
                    (aboveState.isAir() || aboveState.canBeReplaced())) {
                return mutablePos.immutable();
            }
            mutablePos.move(Direction.DOWN);
        }

        mutablePos.set(startPos);
        for (int y = 0; y < 10; y++) {
            BlockState currentState = level.getBlockState(mutablePos);
            BlockState aboveState = level.getBlockState(mutablePos.above());

            if (currentState.isFaceSturdy(level, mutablePos, Direction.UP) &&
                    (aboveState.isAir() || aboveState.canBeReplaced())) {
                return mutablePos.immutable();
            }
            mutablePos.move(Direction.UP);
        }

        return null;
    }
}