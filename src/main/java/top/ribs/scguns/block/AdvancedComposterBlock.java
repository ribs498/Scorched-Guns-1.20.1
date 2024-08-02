package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import top.ribs.scguns.blockentity.AdvancedComposterBlockEntity;
import top.ribs.scguns.blockentity.LightningBatteryBlockEntity;
import top.ribs.scguns.init.ModBlockEntities;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModTags;

import javax.annotation.Nullable;

public class AdvancedComposterBlock extends BaseEntityBlock {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 11);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape INSIDE = Block.box(2.0, 4.0, 2.0, 14.0, 14.0, 14.0);
    private static final VoxelShape SHAPE;

    static {
        SHAPE = Shapes.or(
                // Bottom
                Block.box(0, 0, 0, 16, 4, 16),
                // Sides
                Block.box(0, 4, 0, 2, 14, 16),
                Block.box(14, 4, 0, 16, 14, 16),
                Block.box(2, 4, 0, 14, 14, 2),
                Block.box(2, 4, 14, 14, 14, 16)
        );
    }

    public AdvancedComposterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0).setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return INSIDE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedComposterBlockEntity(pos, state);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, FACING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.ADVANCED_COMPOSTER.get(),
                AdvancedComposterBlock::tick);
    }

    private static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof AdvancedComposterBlockEntity composter) {
            composter.tick(level, pos, state);
        }
    }



    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getValue(LEVEL) == 7) {
            level.scheduleTick(pos, this, 20); // Schedule tick for processing
        }
    }


    public BlockState addItem(Player player, BlockState state, Level level, BlockPos pos, ItemStack stack) {
        int currentLevel = state.getValue(LEVEL);
        float chance = getCompostChance(stack);

        if ((currentLevel != 0 || chance > 0.0F) && level.random.nextDouble() < chance) {
            int newLevel = Math.min(7, currentLevel + 1);
            BlockState newState = state.setValue(LEVEL, newLevel);
            level.setBlock(pos, newState, 3);
            if (newLevel == 7) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AdvancedComposterBlockEntity) {
                    ((AdvancedComposterBlockEntity) blockEntity).startComposting();
                }
            }
            playComposterEffects(level, pos, state); // Add this line
            return newState;
        }
        return state;
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (state.getValue(LEVEL) < 7 && isCompostable(heldItem)) {
            if (!level.isClientSide) {
                BlockState newState = addItem(player, state, level, pos, heldItem);
                if (state != newState) {
                    player.awardStat(Stats.ITEM_USED.get(heldItem.getItem()));
                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (state.getValue(LEVEL) >= 8) {
            if (!level.isClientSide) {
                return extractProduce(player, state, level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    private InteractionResult extractProduce(Player player, BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AdvancedComposterBlockEntity composter) {
            boolean extracted = composter.extractOneItem(player);
            if (extracted) {
                level.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlock(pos, state.setValue(LEVEL, composter.getVisualLevel()), 3);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AdvancedComposterBlockEntity) {
                ((AdvancedComposterBlockEntity) blockEntity).drops();
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return blockState.getValue(LEVEL);
    }
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int currentLevel = state.getValue(LEVEL);
        if (currentLevel > 0) {
            double d0 = pos.getX() + 0.5D;
            double d1 = pos.getY() + 1.0D;
            double d2 = pos.getZ() + 0.5D;

            int particleChance = (currentLevel == 7) ? 10 : 5; // Higher chance at level 7 for fewer particles

            if (random.nextInt(particleChance) == 0) {
                double offsetX = (random.nextDouble() - 0.5D) * 0.5D;
                double offsetZ = (random.nextDouble() - 0.5D) * 0.5D;
                double offsetY = random.nextDouble() * 0.1D; // Small vertical offset

                level.addParticle(ModParticleTypes.SULFUR_DUST.get(),
                        d0 + offsetX,
                        d1 + offsetY,
                        d2 + offsetZ,
                        0.0D, 0.03D, 0.0D);
            }
        }
    }
    public void playComposterEffects(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide) {
            level.levelEvent(1500, pos, state.getValue(LEVEL) > 0 ? 1 : 0);
        }
    }

    public boolean isCompostable(ItemStack stack) {
        return stack.is(ModTags.Items.WEAK_COMPOST) ||
                stack.is(ModTags.Items.NORMAL_COMPOST) ||
                stack.is(ModTags.Items.STRONG_COMPOST);
    }

    private float getCompostChance(ItemStack stack) {
        if (stack.is(ModTags.Items.WEAK_COMPOST)) {
            return 0.4F;
        } else if (stack.is(ModTags.Items.NORMAL_COMPOST)) {
            return 0.6F;
        } else if (stack.is(ModTags.Items.STRONG_COMPOST)) {
            return 0.8F;
        }
        return 0.0F;
    }
}