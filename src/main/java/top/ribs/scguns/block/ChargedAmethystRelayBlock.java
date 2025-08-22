package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.blockentity.ChargedAmethystRelayBlockEntity;
import top.ribs.scguns.init.ModBlockEntities;

public class ChargedAmethystRelayBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<NoteBlockInstrument> INSTRUMENT = BlockStateProperties.NOTEBLOCK_INSTRUMENT;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final int LISTEN_RADIUS = 20;

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public ChargedAmethystRelayBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(POWERED, Boolean.FALSE)
                .setValue(INSTRUMENT, NoteBlockInstrument.HARP)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (direction == null) return false;
        Direction facing = state.getValue(FACING);
        return direction.getOpposite() == facing;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, INSTRUMENT, FACING);
    }

    private BlockState setInstrument(Level level, BlockPos pos, BlockState state) {
        NoteBlockInstrument instrumentAbove = level.getBlockState(pos.above()).instrument();
        if (instrumentAbove.worksAboveNoteBlock()) {
            return state.setValue(INSTRUMENT, instrumentAbove);
        } else {
            NoteBlockInstrument instrumentBelow = level.getBlockState(pos.below()).instrument();
            NoteBlockInstrument finalInstrument = instrumentBelow.worksAboveNoteBlock() ? NoteBlockInstrument.HARP : instrumentBelow;
            return state.setValue(INSTRUMENT, finalInstrument);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.setInstrument(context.getLevel(), context.getClickedPos(), this.defaultBlockState()
                .setValue(POWERED, Boolean.FALSE)
                .setValue(FACING, context.getHorizontalDirection().getOpposite()));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            NoteBlockInstrument currentInstrument = state.getValue(INSTRUMENT);

            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    currentInstrument.getSoundEvent().value(), SoundSource.BLOCKS, 3.0F, 1.0F);
            if (level instanceof ServerLevel serverLevel) {
                double colorOffset = getInstrumentColorOffset(currentInstrument);
                serverLevel.sendParticles(ParticleTypes.NOTE,
                        pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                        1, colorOffset, 0.0, 0.0, 0.0);
            }

            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    private double getInstrumentColorOffset(NoteBlockInstrument instrument) {
        int instrumentIndex = instrument.ordinal();
        int totalInstruments = NoteBlockInstrument.values().length;
        return (double) instrumentIndex / totalInstruments;
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction facing, BlockState facingState,
                                  net.minecraft.world.level.LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        boolean flag = facing.getAxis() == net.minecraft.core.Direction.Axis.Y;
        return flag ? this.setInstrument((Level)level, currentPos, state) : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        BlockState newState = this.setInstrument(level, pos, state);
        if (!newState.equals(state)) {
            level.setBlock(pos, newState, 3);
            if (level.getBlockEntity(pos) instanceof ChargedAmethystRelayBlockEntity relay) {
                relay.onInstrumentTuned(newState.getValue(INSTRUMENT));
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChargedAmethystRelayBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.CHARGED_AMETHYST_RELAY.get(), ChargedAmethystRelayBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return state.getValue(POWERED);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (!blockState.getValue(POWERED)) return 0;
        Direction facing = blockState.getValue(FACING);
        return side.getOpposite() == facing ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (!blockState.getValue(POWERED)) return 0;
        Direction facing = blockState.getValue(FACING);
        return side.getOpposite() == facing ? 15 : 0;
    }

    public void onInstrumentHeard(Level level, BlockPos relayPos, NoteBlockInstrument playedInstrument) {
        BlockState state = level.getBlockState(relayPos);
        if (state.getBlock() == this) {
            NoteBlockInstrument tunedInstrument = state.getValue(INSTRUMENT);
            if (playedInstrument == tunedInstrument) {
                if (level.getBlockEntity(relayPos) instanceof ChargedAmethystRelayBlockEntity relay) {
                    relay.activateFromInstrument();
                }
                level.setBlock(relayPos, state.setValue(POWERED, true), 3 | 4);
                level.sendBlockUpdated(relayPos, state, state.setValue(POWERED, true), 3);
                level.scheduleTick(relayPos, this, 20);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, false), 3 | 4);
            level.sendBlockUpdated(pos, state, state.setValue(POWERED, false), 3);

            if (level.getBlockEntity(pos) instanceof ChargedAmethystRelayBlockEntity relay) {
                relay.deactivate();
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ChargedAmethystRelayBlockEntity relay) {
            relay.deactivate();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}