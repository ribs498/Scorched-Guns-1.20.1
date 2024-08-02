package top.ribs.scguns.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;
import top.ribs.scguns.block.GeothermalVentBlock;
import top.ribs.scguns.block.SulfurVentBlock;
import top.ribs.scguns.init.ModBlocks;

public class SulfurVentFeature extends Feature<NoneFeatureConfiguration> {

    public SulfurVentFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        LevelAccessor world = context.level();
        int height = Mth.nextInt(random, 1, 3);

        BlockPos magmaPos = pos.below();
        BlockState belowBlockState = world.getBlockState(magmaPos);
        if (belowBlockState.is(Blocks.NETHERRACK) &&
                world.getBlockState(magmaPos.below()).isFaceSturdy(world, magmaPos.below(), Direction.UP)) {
            world.setBlock(magmaPos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        } else if (!belowBlockState.is(Blocks.MAGMA_BLOCK)) {
            return false;
        }
        boolean canBeActive = countActiveVentsNearby(world, pos) < SulfurVentBlock.MAX_ACTIVE_VENTS;
        for (int i = 0; i < height; i++) {
            BlockPos currentPos = pos.above(i);
            BlockState state = ModBlocks.SULFUR_VENT.get().defaultBlockState()
                    .setValue(SulfurVentBlock.VENT_TYPE, i == 0 ? SulfurVentBlock.SulfurVentType.BASE :
                            (i == height - 1 ? SulfurVentBlock.SulfurVentType.TOP : SulfurVentBlock.SulfurVentType.MIDDLE))
                    .setValue(SulfurVentBlock.ACTIVE, canBeActive);

            if (world.getBlockState(currentPos).canBeReplaced() && world.getBlockState(currentPos.below()).getBlock() != Blocks.AIR) {
                world.setBlock(currentPos, state, 3);
            } else {
                return false;
            }
        }

        return true;
    }

    private int countActiveVentsNearby(LevelAccessor level, BlockPos pos) {
        int activeCount = 0;

        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-SulfurVentBlock.CHECK_RADIUS, -SulfurVentBlock.CHECK_RADIUS, -SulfurVentBlock.CHECK_RADIUS),
                pos.offset(SulfurVentBlock.CHECK_RADIUS, SulfurVentBlock.CHECK_RADIUS, SulfurVentBlock.CHECK_RADIUS))) {
            if (checkPos.equals(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(checkPos);
            if (state.getBlock() instanceof SulfurVentBlock && state.getValue(SulfurVentBlock.ACTIVE)) {
                activeCount++;
            }
            if (activeCount >= SulfurVentBlock.MAX_ACTIVE_VENTS) {
                return activeCount;
            }
        }

        return activeCount;
    }
}