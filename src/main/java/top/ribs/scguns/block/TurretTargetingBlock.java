package top.ribs.scguns.block;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TurretTargetingBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");

    private static final VoxelShape SHAPE_CENTERED = Block.box(2.0, 0.0, 2.0, 14.0, 7.0, 14.0);
    private static final VoxelShape SHAPE_CONNECTED_NORTH = Block.box(2.0, 0.0, 6.0, 14.0, 7.0, 16.0);
    private static final VoxelShape SHAPE_CONNECTED_SOUTH = Block.box(2.0, 0.0, 0.0, 14.0, 7.0, 10.0);
    private static final VoxelShape SHAPE_CONNECTED_EAST = Block.box(0.0, 0.0, 2.0, 10.0, 7.0, 14.0);
    private static final VoxelShape SHAPE_CONNECTED_WEST = Block.box(6.0, 0.0, 2.0, 16.0, 7.0, 14.0);

    public TurretTargetingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CONNECTED, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CONNECTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockGetter world = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite();
        boolean isConnected = isAdjacentToTurret(world, pos);

        if (isConnected && world instanceof Level level) {
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return this.defaultBlockState().setValue(FACING, facing).setValue(CONNECTED, isConnected);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        boolean connected = state.getValue(CONNECTED);

        if (connected) {
            return switch (facing) {
                case NORTH -> SHAPE_CONNECTED_NORTH;
                case SOUTH -> SHAPE_CONNECTED_SOUTH;
                case EAST -> SHAPE_CONNECTED_EAST;
                case WEST -> SHAPE_CONNECTED_WEST;
                default -> SHAPE_CENTERED;
            };
        } else {
            return SHAPE_CENTERED;
        }
    }

    // New method for auto-connecting to turrets after placement
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, neighborPos, isMoving);
        if (!world.isClientSide) {
            boolean isConnected = isAdjacentToTurret(world, pos);
            Direction correctFacing = getCorrectFacingForTurret(world, pos);

            if (isConnected) {
                world.setBlock(pos, state.setValue(CONNECTED, true).setValue(FACING, correctFacing), 3);
            } else {
                world.setBlock(pos, state.setValue(CONNECTED, false), 3);
            }
        }
    }

    // Method to find the correct facing based on nearby turret
    private Direction getCorrectFacingForTurret(BlockGetter world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (world.getBlockState(neighborPos).getBlock() instanceof BasicTurretBlock ||
                    world.getBlockState(neighborPos).getBlock() instanceof ShotgunTurretBlock ||
                    world.getBlockState(neighborPos).getBlock() instanceof AutoTurretBlock) {
                return direction.getOpposite(); // Face towards the turret
            }
        }
        return Direction.NORTH; // Default to North if no turret found
    }

    private boolean isAdjacentToTurret(BlockGetter world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (world.getBlockState(neighborPos).getBlock() instanceof BasicTurretBlock ||
                    world.getBlockState(neighborPos).getBlock() instanceof ShotgunTurretBlock ||
                    world.getBlockState(neighborPos).getBlock() instanceof AutoTurretBlock) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }
}
