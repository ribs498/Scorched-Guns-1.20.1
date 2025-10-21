package top.ribs.scguns.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidType;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModFluids;
import top.ribs.scguns.init.ModItems;

public abstract class ViciousAcidFluid extends FlowingFluid {

    @Override
    public Fluid getFlowing() {
        return ModFluids.VICIOUS_ACID_FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return ModFluids.VICIOUS_ACID_SOURCE.get();
    }

    @Override
    public Item getBucket() {
        return ModItems.VICIOUS_ACID_BUCKET.get();
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        Block.dropResources(state, level, pos, level.getBlockEntity(pos));
    }

    private static void reactWithSurroundings(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) return;

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            FluidState adjacentFluid = level.getFluidState(adjacentPos);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentFluid.is(FluidTags.LAVA) || adjacentState.is(Blocks.FIRE)) {
                level.setBlock(adjacentPos, Blocks.AIR.defaultBlockState(), 3);
                level.explode(null, adjacentPos.getX() + 0.5, adjacentPos.getY() + 0.5, adjacentPos.getZ() + 0.5,
                        2.0F, Level.ExplosionInteraction.BLOCK);
                return;
            }

            if (adjacentFluid.is(FluidTags.WATER)) {
                level.setBlock(adjacentPos, Blocks.PRISMARINE.defaultBlockState(), 3);
                level.playSound(null, adjacentPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                        0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

                for (int i = 0; i < 8; i++) {
                    ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE,
                            adjacentPos.getX() + 0.5, adjacentPos.getY() + 1.0, adjacentPos.getZ() + 0.5,
                            1, 0.3, 0.3, 0.3, 0.0);
                }
            }
        }
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return ModBlocks.VICIOUS_ACID_BLOCK.get().defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == ModFluids.VICIOUS_ACID_SOURCE.get() ||
                fluid == ModFluids.VICIOUS_ACID_FLOWING.get();
    }

    @Override
    public FluidType getFluidType() {
        return ModFluids.VICIOUS_ACID_FLUID_TYPE.get();
    }

    public static class Source extends ViciousAcidFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        protected boolean canBeReplacedWith(FluidState pState, BlockGetter pLevel, BlockPos pPos, Fluid pFluid, Direction pDirection) {
            return false;
        }

        @Override
        public void tick(Level level, BlockPos pos, FluidState state) {
            reactWithSurroundings(level, pos);
            super.tick(level, pos, state);
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends ViciousAcidFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        protected boolean canBeReplacedWith(FluidState pState, BlockGetter pLevel, BlockPos pPos, Fluid pFluid, Direction pDirection) {
            return false;
        }

        @Override
        public void tick(Level level, BlockPos pos, FluidState state) {
            reactWithSurroundings(level, pos);
            super.tick(level, pos, state);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}