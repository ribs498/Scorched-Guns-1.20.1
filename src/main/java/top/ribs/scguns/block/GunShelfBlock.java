package top.ribs.scguns.block;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import top.ribs.scguns.blockentity.GunShelfBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GunShelfBlock extends Block implements EntityBlock {

    public static final List<Block> GUN_SHELF_BLOCKS = new ArrayList<>();

    protected static final VoxelShape SHAPE_NORTH = Block.box(0D, 1.0D, 14.0D, 16.0D, 12.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(0D, 1.0D, 0.0D, 16.0D, 12.0D, 2.0D);
    protected static final VoxelShape SHAPE_WEST = Block.box(14.0D, 1.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    protected static final VoxelShape SHAPE_EAST = Block.box(0.0D, 1.0D, 0.0D, 2.0D, 12.0D, 16.0D);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public GunShelfBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));

        GUN_SHELF_BLOCKS.add(this);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType pathType) {
        return true;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return !worldIn.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).isAir();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (context.getClickedFace() == Direction.UP || context.getClickedFace() == Direction.DOWN)
            return state.setValue(FACING, Direction.NORTH);
        return state.setValue(FACING, context.getClickedFace());
    }
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return facing == stateIn.getValue(FACING).getOpposite() && !stateIn.canSurvive(worldIn, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        if (target.getLocation().y() >= pos.getY() + 0.25) {
            if (world.getBlockEntity(pos) instanceof GunShelfBlockEntity tile) {
                ItemStack i = tile.getItem(0);
                if (!i.isEmpty()) return i;
            }
        }
        return super.getCloneItemStack(state, target, world, pos, player);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (!worldIn.isClientSide) {
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            if (blockEntity instanceof GunShelfBlockEntity tile) {
                ItemStack heldItem = player.getItemInHand(handIn);

                if (tile.isEmpty() && !heldItem.isEmpty()) {
                    ItemStack itemToPlace = heldItem.split(1);
                    tile.setDisplayedItem(itemToPlace);
                    worldIn.sendBlockUpdated(pos, state, state, 3);
                    return InteractionResult.SUCCESS;
                } else if (!tile.isEmpty()) {
                    ItemStack displayedItem = tile.getDisplayedItem();
                    if (!player.getInventory().add(displayedItem)) {
                        player.drop(displayedItem, false);
                    }
                    tile.setDisplayedItem(ItemStack.EMPTY);
                    worldIn.sendBlockUpdated(pos, state, state, 3);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GunShelfBlockEntity(pos, state);
    }
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
        };
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        return worldIn.getBlockEntity(pos) instanceof MenuProvider menuProvider ? menuProvider : null;
    }
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof GunShelfBlockEntity tile) {
                Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), tile.getDisplayedItem());
                tile.setDisplayedItem(ItemStack.EMPTY);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }
    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof Container tile)
            return tile.isEmpty() ? 0 : 15;
        else
            return 0;
    }
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);
    }

}
