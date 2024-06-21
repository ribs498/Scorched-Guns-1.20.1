package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.init.ModBlocks;

import javax.annotation.Nullable;

public class GeothermalVentBlock extends Block implements SimpleWaterloggedBlock {
    public static final EnumProperty<GeothermalVentType> VENT_TYPE = EnumProperty.create("vent_type", GeothermalVentType.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private final float growthSpeed;
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public GeothermalVentBlock(Properties properties, float growthSpeed) {
        super(properties.strength(0.5F).sound(SoundType.SAND).randomTicks());
        this.growthSpeed = growthSpeed;
        this.registerDefaultState(this.stateDefinition.any().setValue(VENT_TYPE, GeothermalVentType.BASE).setValue(WATERLOGGED, false).setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VENT_TYPE, WATERLOGGED, ACTIVE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.updateState(level.getBlockState(pos.below()), level.getBlockState(pos.above()))
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
                .setValue(ACTIVE, isActive(level, pos));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        boolean isActive = isActive((Level) level, pos);
        return this.updateState(level.getBlockState(pos.below()), level.getBlockState(pos.above()))
                .setValue(WATERLOGGED, state.getValue(WATERLOGGED))
                .setValue(ACTIVE, isActive);
    }

    private BlockState updateState(BlockState belowState, BlockState aboveState) {
        if (belowState.getBlock() instanceof GeothermalVentBlock) {
            if (aboveState.getBlock() instanceof GeothermalVentBlock) {
                return this.defaultBlockState().setValue(VENT_TYPE, GeothermalVentType.MIDDLE);
            } else if (aboveState.getBlock() instanceof VentCollectorBlock) {
                return this.defaultBlockState().setValue(VENT_TYPE, GeothermalVentType.TOP);
            } else {
                return this.defaultBlockState().setValue(VENT_TYPE, GeothermalVentType.TOP);
            }
        } else {
            return this.defaultBlockState().setValue(VENT_TYPE, GeothermalVentType.BASE);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(state, world, pos, random);
        if (!world.isClientSide) {
            if (random.nextFloat() < calculateGrowthProbability()) {
                this.checkAndGrowNiterLayer(world, pos, random);
            }
        }
    }

    private float calculateGrowthProbability() {
        return Math.min(1.0F, growthSpeed / 100.0F);
    }

    private boolean hasWaterAround(LevelReader world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (direction != Direction.UP && direction != Direction.DOWN) {
                BlockPos adjacentPos = pos.relative(direction);
                if (world.getFluidState(adjacentPos).is(FluidTags.WATER)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkAndGrowNiterLayer(ServerLevel world, BlockPos pos, RandomSource random) {
        boolean isWaterNearby = hasWaterAround(world, pos);
        if (isWaterNearby) {
            for (int i = 0; i < 2; i++) {
                BlockPos randomPos = pos.offset(random.nextInt(7) - 3, random.nextInt(3) - 1, random.nextInt(7) - 3);
                if (canPlaceNiterLayer(world, randomPos)) {
                    BlockState aboveState = world.getBlockState(randomPos.above());
                    if (aboveState.is(ModBlocks.NITER_LAYER.get())) {
                        int layers = aboveState.getValue(NiterLayerBlock.LAYERS);
                        if (layers < NiterLayerBlock.MAX_HEIGHT) {
                            world.setBlock(randomPos.above(), aboveState.setValue(NiterLayerBlock.LAYERS, layers + 1), 3);
                        }
                    } else {
                        world.setBlock(randomPos.above(), ModBlocks.NITER_LAYER.get().defaultBlockState().setValue(NiterLayerBlock.LAYERS, 1), 3);
                    }
                }
            }
        }
    }

    private boolean canPlaceNiterLayer(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockState aboveState = world.getBlockState(pos.above());
        boolean canPlace = state.isFaceSturdy(world, pos, Direction.UP) && (aboveState.isAir() || aboveState.getFluidState().is(FluidTags.WATER));
        return canPlace;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        level.setBlock(pos, state.setValue(ACTIVE, isActive(level, pos)), 3);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        boolean isActive = isActive(level, pos);
        level.setBlock(pos, state.setValue(ACTIVE, isActive), 3);
    }

    @Override
    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        boolean isTop = state.getValue(VENT_TYPE) == GeothermalVentType.TOP;
        boolean isBaseWithoutTop = state.getValue(VENT_TYPE) == GeothermalVentType.BASE && !isVentAbove(level, pos);
        if ((isTop || isBaseWithoutTop) && state.getValue(ACTIVE)) {
            if (random.nextInt(20) == 0) {
                level.playLocalSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
            }

            // Check if there's a VentCollectorBlock on top
            BlockPos abovePos = pos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            if (aboveState.getBlock() instanceof VentCollectorBlock) {
                return; // Stop particle emission if VentCollectorBlock is on top
            }

            // Emit eruption particles
            for (int i = 0; i < random.nextInt(2) + 2; ++i) {
                double offsetX = random.nextDouble() * 0.05 - 0.025;
                double offsetY = 0.05 + random.nextDouble() * 0.05;
                double offsetZ = random.nextDouble() * 0.05 - 0.025;
                level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
            }

            // Emit smoke particles
            for (int i = 0; i < random.nextInt(2) + 2; ++i) {
                double offsetX = random.nextDouble() * 0.2 - 0.1;
                double offsetY = 0.05 + random.nextDouble() * 0.05;
                double offsetZ = random.nextDouble() * 0.2 - 0.1;
                level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
            }

            // Emit bubble particles
            for (int i = 0; i < random.nextInt(2) + 2; ++i) {
                double offsetX = random.nextDouble() * 0.2 - 0.1;
                double offsetY = 0.05 + random.nextDouble() * 0.05;
                double offsetZ = random.nextDouble() * 0.2 - 0.1;
                level.addParticle(ParticleTypes.BUBBLE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
            }

            // Emit campfire signal smoke particles
            for (int i = 0; i < random.nextInt(2) + 1; ++i) {
                double offsetX = random.nextDouble() * 0.05 - 0.025;
                double offsetY = 0.2 + random.nextDouble() * 0.2;
                double offsetZ = random.nextDouble() * 0.05 - 0.025;
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
            }
        }
    }

    private boolean isVentAbove(Level level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        return level.getBlockState(abovePos).getBlock() instanceof GeothermalVentBlock;
    }

    private boolean isActive(Level level, BlockPos pos) {
        BlockPos basePos = getBasePos(level, pos);
        return level.getBlockState(basePos.below()).is(Blocks.MAGMA_BLOCK) && level.getFluidState(basePos).is(FluidTags.WATER);
    }

    private BlockPos getBasePos(Level level, BlockPos pos) {
        while (level.getBlockState(pos.below()).getBlock() instanceof GeothermalVentBlock) {
            pos = pos.below();
        }
        return pos;
    }

    public enum GeothermalVentType implements StringRepresentable {
        BASE("base"),
        MIDDLE("middle"),
        TOP("top");

        private final String name;

        GeothermalVentType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}


