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
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.init.ModBlocks;

import javax.annotation.Nullable;
import java.util.Random;

public class GeothermalVentBlock extends Block implements SimpleWaterloggedBlock {
    public static final EnumProperty<GeothermalVentType> VENT_TYPE = EnumProperty.create("vent_type", GeothermalVentType.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    public static final IntegerProperty VENT_POWER = IntegerProperty.create("vent_power", 1, 5);
    public static final int MAX_VENT_POWER = 5;
    private static final int BASE_TICK_INTERVAL = 100;
    private static final int TICK_WIGGLE_ROOM = 90;
    private final Random random = new Random();

    public GeothermalVentBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(VENT_TYPE, GeothermalVentType.BASE)
                .setValue(WATERLOGGED, false)
                .setValue(ACTIVE, false)
                .setValue(VENT_POWER, 1));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VENT_TYPE, WATERLOGGED, ACTIVE, VENT_POWER);
    }
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = level.getFluidState(pos);
        boolean waterlogged = fluidState.getType() == Fluids.WATER;
        boolean isActive = isActive(level, pos);
        int ventPower = calculateVentPower(level, pos);
        return this.updateState(level.getBlockState(pos.below()), level.getBlockState(pos.above()))
                .setValue(WATERLOGGED, waterlogged)
                .setValue(ACTIVE, isActive)
                .setValue(VENT_POWER, ventPower);
    }
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (level instanceof Level) {
            boolean isActive = isActive(level, pos);
            int ventPower = calculateVentPower((Level) level, pos);
            return this.updateState(level.getBlockState(pos.below()), level.getBlockState(pos.above()))
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED))
                    .setValue(ACTIVE, isActive)
                    .setValue(VENT_POWER, ventPower);
        }

        return this.updateState(level.getBlockState(pos.below()), level.getBlockState(pos.above()))
                .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
    }
    private int calculateVentPower(LevelAccessor level, BlockPos pos) {
        BlockPos basePos = getBasePos(level, pos);
        int power = 1;
        BlockPos checkPos = basePos.above();

        while (level.getBlockState(checkPos).getBlock() instanceof GeothermalVentBlock && power < MAX_VENT_POWER) {
            power++;
            checkPos = checkPos.above();
        }

        return power;
    }
    private void updateVentPower(Level level, BlockPos pos) {
        BlockPos basePos = getBasePos(level, pos);
        int power = calculateVentPower(level, basePos);

        // Update the base block
        BlockState baseState = level.getBlockState(basePos);
        level.setBlock(basePos, baseState.setValue(VENT_POWER, power), 3);

        // Update all blocks above the base
        BlockPos checkPos = basePos.above();
        while (level.getBlockState(checkPos).getBlock() instanceof GeothermalVentBlock) {
            BlockState state = level.getBlockState(checkPos);
            level.setBlock(checkPos, state.setValue(VENT_POWER, power), 3);
            checkPos = checkPos.above();
        }
    }
    private BlockState updateState(BlockState belowState, BlockState aboveState) {
        if (belowState.getBlock() instanceof GeothermalVentBlock) {
            if (aboveState.getBlock() instanceof GeothermalVentBlock) {
                return this.defaultBlockState().setValue(VENT_TYPE, GeothermalVentType.MIDDLE);
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
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        boolean isActive = isActive(level, pos);
        int ventPower = calculateVentPower(level, pos);
        level.setBlock(pos, state.setValue(ACTIVE, isActive).setValue(VENT_POWER, ventPower), 3);
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        level.scheduleTick(pos, this, calculateNextTickInterval());
        updateVentPower(level, pos);
    }    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (!(newState.getBlock() instanceof GeothermalVentBlock)) {
            updateVentPower(level, pos.below());
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!world.isClientSide && state.getValue(ACTIVE)) {
            int growthAttempts = Math.max(1, Math.round(40.0F / 10.0F));
            for (int i = 0; i < growthAttempts; i++) {
                if (!hasVentCollectorAbove(world, pos) && random.nextFloat() < calculateGrowthProbability()) {
                    this.growNiterLayer(world, pos, random);
                }
            }
        }
        world.scheduleTick(pos, this, calculateNextTickInterval());
    }

    private int calculateNextTickInterval() {
        return BASE_TICK_INTERVAL + random.nextInt(TICK_WIGGLE_ROOM);
    }

    private float calculateGrowthProbability() {
        return 40.0F / 100.0F; // Direct conversion of growth speed to probability
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

    private void growNiterLayer(ServerLevel world, BlockPos pos, RandomSource random) {
        if (hasVentCollectorAbove(world, pos)) {
            return;
        }
        boolean isWaterNearby = hasWaterAround(world, pos);
        if (isWaterNearby) {
            int radius = 3;

            int x = random.nextInt(radius * 2 + 1) - radius;
            int z = random.nextInt(radius * 2 + 1) - radius;
            int y = random.nextInt(3) - 1;

            if (x * x + z * z <= radius * radius) {
                BlockPos randomPos = pos.offset(x, y, z);
                if (canPlaceNiterLayer(world, randomPos)) {
                    BlockPos abovePos = randomPos.above();
                    BlockState currentState = world.getBlockState(abovePos);
                    if (currentState.isAir() || currentState.getFluidState().is(FluidTags.WATER)) {
                        world.setBlock(abovePos, ModBlocks.NITER_LAYER.get().defaultBlockState().setValue(NiterLayerBlock.LAYERS, 1), 3);
                    }
                }
            }
        }
    }

    private boolean hasVentCollectorAbove(LevelReader world, BlockPos pos) {
        BlockPos topPos = getTopPos(world, pos);
        return world.getBlockState(topPos.above()).getBlock() instanceof VentCollectorBlock;
    }

    private BlockPos getTopPos(LevelReader world, BlockPos pos) {
        while (world.getBlockState(pos.above()).getBlock() instanceof GeothermalVentBlock) {
            pos = pos.above();
        }
        return pos;
    }

    private boolean canPlaceNiterLayer(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockState aboveState = world.getBlockState(pos.above());
        return state.isFaceSturdy(world, pos, Direction.UP)
                && (aboveState.isAir() || aboveState.getFluidState().is(FluidTags.WATER))
                && !(state.getBlock() instanceof GeothermalVentBlock)
                && !(aboveState.getBlock() instanceof GeothermalVentBlock);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        // Prevent the block from being pushed by pistons
        return PushReaction.DESTROY; // This will cause the block to break and drop as an item when pushed
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        // Check if the block is being pushed by a piston
        if (block instanceof PistonBaseBlock) {
            level.destroyBlock(pos, true); // Break the block and drop it as an item
        }

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

        // Check if the block is active before spawning particles
        if ((isTop || isBaseWithoutTop) && state.getValue(ACTIVE)) {
            // Play particles for active geothermal vent
            if (random.nextInt(20) == 0) {
                level.playLocalSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
            }
            BlockPos abovePos = pos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            if (aboveState.getBlock() instanceof VentCollectorBlock) {
                return;
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

    private boolean isActive(LevelAccessor level, BlockPos pos) {
        BlockPos basePos = getBasePos(level, pos);
        BlockState belowState = level.getBlockState(basePos.below());
        if (!belowState.is(Blocks.MAGMA_BLOCK)) {
            return false;
        }
        BlockState currentState = level.getBlockState(basePos);
        if (currentState.hasProperty(WATERLOGGED)) {
            return currentState.getValue(WATERLOGGED);
        }

        return false;
    }




    private BlockPos getBasePos(LevelAccessor level, BlockPos pos) {
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

