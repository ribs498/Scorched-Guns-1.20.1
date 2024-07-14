package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import top.ribs.scguns.util.VoxelShapeHelper;

public class SandbagBlock extends Block {
    public static final EnumProperty<SandbagType> TYPE = EnumProperty.create("type", SandbagType.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE_SINGLE_NORTH_SOUTH = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 6.0D, 12.0D);
    private static final VoxelShape SHAPE_DOUBLE_NORTH_SOUTH = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 11.0D, 12.0D);
    private static final VoxelShape SHAPE_TRIPLE_NORTH_SOUTH = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 12.0D);

    private static final VoxelShape SHAPE_SINGLE_EAST_WEST = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 6.0D, 16.0D);
    private static final VoxelShape SHAPE_DOUBLE_EAST_WEST = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 11.0D, 16.0D);
    private static final VoxelShape SHAPE_TRIPLE_EAST_WEST = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);

    public SandbagBlock(Properties properties) {
        super(BlockBehaviour.Properties.copy(Blocks.SAND));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TYPE, SandbagType.SINGLE)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        switch (state.getValue(TYPE)) {
            case DOUBLE:
                return direction == Direction.NORTH || direction == Direction.SOUTH ? SHAPE_DOUBLE_NORTH_SOUTH : SHAPE_DOUBLE_EAST_WEST;
            case TRIPLE:
                return direction == Direction.NORTH || direction == Direction.SOUTH ? SHAPE_TRIPLE_NORTH_SOUTH : SHAPE_TRIPLE_EAST_WEST;
            case SINGLE:
            default:
                return direction == Direction.NORTH || direction == Direction.SOUTH ? SHAPE_SINGLE_NORTH_SOUTH : SHAPE_SINGLE_EAST_WEST;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level world = context.getLevel();
        BlockState state = world.getBlockState(pos);

        if (state.is(this)) {
            if (state.getValue(TYPE) == SandbagType.SINGLE) {
                return state.setValue(TYPE, SandbagType.DOUBLE);
            } else if (state.getValue(TYPE) == SandbagType.DOUBLE) {
                return state.setValue(TYPE, SandbagType.TRIPLE);
            }
        }

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(TYPE, SandbagType.SINGLE);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() == this.asItem()) {
            if (state.getValue(TYPE) == SandbagType.SINGLE) {
                world.setBlock(pos, state.setValue(TYPE, SandbagType.DOUBLE), 3);
                world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                if (!player.isCreative()) {
                    player.getItemInHand(hand).shrink(1);
                }
                return InteractionResult.SUCCESS;
            } else if (state.getValue(TYPE) == SandbagType.DOUBLE) {
                world.setBlock(pos, state.setValue(TYPE, SandbagType.TRIPLE), 3);
                world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                if (!player.isCreative()) {
                    player.getItemInHand(hand).shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        } else if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            if (state.getValue(TYPE) == SandbagType.TRIPLE) {
                world.setBlock(pos, state.setValue(TYPE, SandbagType.DOUBLE), 3);
            } else if (state.getValue(TYPE) == SandbagType.DOUBLE) {
                world.setBlock(pos, state.setValue(TYPE, SandbagType.SINGLE), 3);
            } else if (state.getValue(TYPE) == SandbagType.SINGLE) {
                world.removeBlock(pos, false);
            }
            if (!player.isCreative()) {
                Block.popResource(world, pos, new ItemStack(this));
            }
            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public enum SandbagType implements StringRepresentable {
        SINGLE("single"),
        DOUBLE("double"),
        TRIPLE("triple");

        private final String name;

        SandbagType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}