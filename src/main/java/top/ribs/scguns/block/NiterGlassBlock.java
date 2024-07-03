package top.ribs.scguns.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class NiterGlassBlock extends Block {
    public static final BooleanProperty TRANSPARENT = BooleanProperty.create("transparent");

    public NiterGlassBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TRANSPARENT, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TRANSPARENT);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            boolean isPowered = world.hasNeighborSignal(pos);
            updateStateAndNeighbors(world, pos, state, isPowered);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean isPowered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return this.defaultBlockState().setValue(TRANSPARENT, isPowered);
    }
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        if (adjacentState.is(this)) {
            return true;
        }
        return super.skipRendering(state, adjacentState, direction);
    }

    private void updateStateAndNeighbors(Level world, BlockPos pos, BlockState initialState, boolean isPowered) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(pos);
        visited.add(pos);
        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            BlockState currentState = world.getBlockState(currentPos);
            BlockState newState = currentState.setValue(TRANSPARENT, isPowered);
            if (newState != currentState) {
                world.setBlock(currentPos, newState, 2);
            }
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(direction);
                if (!visited.contains(neighborPos) && world.getBlockState(neighborPos).getBlock() instanceof NiterGlassBlock) {
                    queue.add(neighborPos);
                    visited.add(neighborPos);
                }
            }
        }
    }
}


