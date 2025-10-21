package top.ribs.scguns.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;
import top.ribs.scguns.block.GeothermalVentBlock;
import top.ribs.scguns.block.VentBlock;

public class VentFeature extends Feature<VentFeatureConfiguration> {

    public VentFeature(Codec<VentFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<VentFeatureConfiguration> context) {
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        LevelAccessor world = context.level();
        VentFeatureConfiguration config = context.config();

        int height = Mth.nextInt(random, config.minHeight(), config.maxHeight());

        BlockPos magmaPos = pos.below();
        BlockState belowBlockState = world.getBlockState(magmaPos);

        if (config.shouldPlaceBaseBlock()) {
            boolean canPlaceBase = false;
            for (Block requiredBlock : config.requiredBelowBlocks()) {
                if (belowBlockState.is(requiredBlock) &&
                        world.getBlockState(magmaPos.below()).isFaceSturdy(world, magmaPos.below(), Direction.UP)) {
                    canPlaceBase = true;
                    break;
                }
            }

            if (canPlaceBase) {
                world.setBlock(magmaPos, config.baseBlock().defaultBlockState(), 3);
            } else if (!belowBlockState.is(config.baseBlock())) {
                return false;
            }
        } else {
            if (!belowBlockState.is(config.baseBlock())) {
                return false;
            }
        }
        boolean canBeActive = true;
        if (config.shouldCheckActiveLimit()) {
            canBeActive = countActiveVentsNearby(world, pos, config.checkRadius(),
                    config.maxActiveNearby(), config.getVentBlock()) < config.maxActiveNearby();
        }

        for (int i = 0; i < height; i++) {
            BlockPos currentPos = pos.above(i);
            VentBlock.VentType ventType = i == 0 ? VentBlock.VentType.BASE :
                    (i == height - 1 ? VentBlock.VentType.TOP : VentBlock.VentType.MIDDLE);

            BlockState state = config.getVentBlock().defaultBlockState()
                    .setValue(VentBlock.VENT_TYPE, ventType)
                    .setValue(VentBlock.ACTIVE, canBeActive);

            if (config.requiresWaterlogged() && state.hasProperty(GeothermalVentBlock.WATERLOGGED)) {
                state = state.setValue(GeothermalVentBlock.WATERLOGGED, true);
            }

            if (world.getBlockState(currentPos).canBeReplaced() &&
                    world.getBlockState(currentPos.below()).getBlock() != Blocks.AIR) {
                world.setBlock(currentPos, state, 3);
                if (config.requiresWaterlogged()) {
                    world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
                }
            } else {
                return false;
            }
        }

        return true;
    }

    private int countActiveVentsNearby(LevelAccessor level, BlockPos pos, int checkRadius,
                                       int maxActive, Block ventBlock) {
        int activeCount = 0;

        for (BlockPos checkPos : BlockPos.betweenClosed(
                pos.offset(-checkRadius, -checkRadius, -checkRadius),
                pos.offset(checkRadius, checkRadius, checkRadius))) {
            if (checkPos.equals(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(checkPos);
            if (state.is(ventBlock) && state.getValue(VentBlock.ACTIVE)) {
                activeCount++;
            }
            if (activeCount >= maxActive) {
                return activeCount;
            }
        }

        return activeCount;
    }
}