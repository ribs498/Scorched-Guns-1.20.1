package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.blockentity.VentCollectorBlockEntity;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nullable;
import java.util.Objects;

public class VentCollectorBlock extends Block implements EntityBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public VentCollectorBlock(Properties properties) {
        super(properties.strength(0.5F).sound(SoundType.METAL));
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH).setValue(ATTACHED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, ATTACHED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        BlockPos pos = context.getClickedPos().relative(context.getHorizontalDirection().getOpposite());
        boolean attached = context.getLevel().getBlockEntity(pos) != null;
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
                .setValue(ATTACHED, attached);
    }


    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (direction == state.getValue(FACING)) {
            boolean attached = hasInventory(level, neighborPos, direction.getOpposite());
            return state.setValue(ATTACHED, attached);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    private boolean hasInventory(LevelAccessor level, BlockPos pos, Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).isPresent();
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VentCollectorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, t) -> {
            if (t instanceof VentCollectorBlockEntity) {
                VentCollectorBlockEntity.tick(lvl, pos, st, (VentCollectorBlockEntity) t);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof VentCollectorBlockEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, (VentCollectorBlockEntity) blockEntity, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof VentCollectorBlockEntity) {
                ((VentCollectorBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof VentCollectorBlockEntity) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer((Container) blockEntity);
        }
        return 0;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockState belowState = level.getBlockState(pos.below());
        boolean isGeothermalVentBelow = belowState.getBlock() instanceof GeothermalVentBlock;
        boolean isSulfurVentBelow = belowState.getBlock() instanceof SulfurVentBlock;
        boolean isActive = (isGeothermalVentBelow && belowState.getValue(GeothermalVentBlock.ACTIVE)) ||
                (isSulfurVentBelow && belowState.getValue(SulfurVentBlock.ACTIVE));

        if (isActive) {
            if (random.nextInt(20) == 0) {
                level.playLocalSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            boolean hasFilterCharge = false;
            if (blockEntity instanceof VentCollectorBlockEntity ventCollector) {
                hasFilterCharge = ventCollector.getFilterCharge() > 0;
            }

            Direction facing = state.getValue(FACING);
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.55;
            double z = pos.getZ() + 0.7;

            if (facing == Direction.NORTH) {
                z += 0.45;
            } else if (facing == Direction.SOUTH) {
                z -= 0.45;
            } else if (facing == Direction.WEST) {
                x += 0.45;
            } else if (facing == Direction.EAST) {
                x -= 0.45;
            }

            for (int i = 0; i < random.nextInt(2) + 2; ++i) {
                double offsetX = random.nextDouble() * 0.05 - 0.025;
                double offsetY = 0.05 + random.nextDouble() * 0.05;
                double offsetZ = random.nextDouble() * 0.05 - 0.025;

                if (hasFilterCharge) {
                    level.addParticle(ParticleTypes.CLOUD, x, y, z, offsetX, offsetY, offsetZ);
                } else {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, offsetX, offsetY, offsetZ);
                }
            }
        }
    }

}
