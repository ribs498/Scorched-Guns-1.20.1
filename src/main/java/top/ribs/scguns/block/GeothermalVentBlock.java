package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModBlocks;

import javax.annotation.Nullable;

public class GeothermalVentBlock extends VentBlock implements SimpleWaterloggedBlock {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape GEOTHERMAL_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public GeothermalVentBlock(Properties properties) {
        super(properties, new ResourceLocation(Reference.MOD_ID, "geothermal_vent"));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(VENT_TYPE, VentType.BASE)
                .setValue(WATERLOGGED, false)
                .setValue(ACTIVE, false)
                .setValue(VENT_POWER, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
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
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        BlockState updatedState = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        return updatedState.setValue(WATERLOGGED, state.getValue(WATERLOGGED));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return GEOTHERMAL_SHAPE;
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!world.isClientSide && state.getValue(ACTIVE)) {
            if (!hasVentCollectorAbove(world, pos)) {
                if (this.config != null && this.config.getPlacement().isEnabled()) {
                    if (shouldPlaceBlock(random) && hasWaterAround(world, pos)) {
                        placeLayerBlock(world, pos, random);
                    }
                }
            }
        }
        world.scheduleTick(pos, this, calculateNextTickInterval());
    }

    private boolean shouldPlaceBlock(RandomSource random) {
        if (this.config == null) return false;
        return random.nextFloat() < this.config.getPlacement().getPlacementChance();
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

    private void placeLayerBlock(ServerLevel world, BlockPos pos, RandomSource random) {
        if (this.config == null) return;

        ResourceLocation blockToPlace = this.config.getPlacement().getBlockToPlace();
        if (blockToPlace == null) return;

        Block layerBlock;

        if (blockToPlace.toString().equals("scguns:niter_layer")) {
            layerBlock = ModBlocks.NITER_LAYER.get();
        } else if (blockToPlace.toString().equals("scguns:sulfur_layer")) {
            layerBlock = ModBlocks.SULFUR_LAYER.get();
        } else {
            layerBlock = ForgeRegistries.BLOCKS.getValue(blockToPlace);
        }

        if (layerBlock == null || layerBlock == Blocks.AIR) {
            return;
        }

        int radius = this.config.getPlacement().getRadius();

        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = Math.sqrt(random.nextDouble()) * radius;
        int x = (int) Math.round(Math.cos(angle) * distance);
        int z = (int) Math.round(Math.sin(angle) * distance);
        int y = random.nextInt(3) - 1;

        BlockPos randomPos = pos.offset(x, y, z);
        if (canPlaceLayerBlock(world, randomPos)) {
            BlockPos abovePos = randomPos.above();
            BlockState currentState = world.getBlockState(abovePos);
            boolean isWater = currentState.getFluidState().is(FluidTags.WATER);

            if (currentState.isAir() || isWater) {
                BlockState layerState = layerBlock.defaultBlockState();
                if (layerState.hasProperty(NiterLayerBlock.LAYERS)) {
                    layerState = layerState.setValue(NiterLayerBlock.LAYERS, 1);
                }
                if (layerState.hasProperty(NiterLayerBlock.WATERLOGGED) && isWater) {
                    layerState = layerState.setValue(NiterLayerBlock.WATERLOGGED, true);
                }
                world.setBlock(abovePos, layerState, 3);
            }
        }
    }

    private boolean canPlaceLayerBlock(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockState aboveState = world.getBlockState(pos.above());
        return state.isFaceSturdy(world, pos, Direction.UP)
                && (aboveState.isAir() || aboveState.getFluidState().is(FluidTags.WATER))
                && !(state.getBlock() instanceof GeothermalVentBlock)
                && !(aboveState.getBlock() instanceof GeothermalVentBlock);
    }

    @Override
    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!isTopOrBaseWithoutTop(state, level, pos) || !state.getValue(ACTIVE)) {
            return;
        }

        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        if (aboveState.getBlock() instanceof VentCollectorBlock) {
            return;
        }

        if (!shouldShowParticles()) {
            return;
        }

        playAmbientSound(level, pos, random);

        // Large smoke particles
        for (int i = 0; i < random.nextInt(2) + 2; ++i) {
            double offsetX = random.nextDouble() * 0.05 - 0.025;
            double offsetY = 0.05 + random.nextDouble() * 0.05;
            double offsetZ = random.nextDouble() * 0.05 - 0.025;
            level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 1.0,
                    pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
        }

        // Smoke particles
        for (int i = 0; i < random.nextInt(2) + 2; ++i) {
            double offsetX = random.nextDouble() * 0.2 - 0.1;
            double offsetY = 0.05 + random.nextDouble() * 0.05;
            double offsetZ = random.nextDouble() * 0.2 - 0.1;
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.0,
                    pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
        }

        // Bubble particles
        for (int i = 0; i < random.nextInt(2) + 2; ++i) {
            double offsetX = random.nextDouble() * 0.2 - 0.1;
            double offsetY = 0.05 + random.nextDouble() * 0.05;
            double offsetZ = random.nextDouble() * 0.2 - 0.1;
            level.addParticle(ParticleTypes.BUBBLE, pos.getX() + 0.5, pos.getY() + 1.0,
                    pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
        }

        // Campfire cosy smoke particles
        for (int i = 0; i < random.nextInt(2) + 1; ++i) {
            double offsetX = random.nextDouble() * 0.05 - 0.025;
            double offsetY = 0.2 + random.nextDouble() * 0.2;
            double offsetZ = random.nextDouble() * 0.05 - 0.025;
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 1.0,
                    pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
        }
    }

    @Override
    protected boolean isActive(LevelAccessor level, BlockPos pos) {
        if (this.config == null) {
            reloadConfig();
            if (this.config == null) return false;
        }

        BlockPos basePos = getBasePos(level, pos);
        BlockState belowState = level.getBlockState(basePos.below());

        // Check if the block below matches config's base block
        ResourceLocation configBaseBlock = this.config.getActivation().getBaseBlock();
        Block baseBlock = ForgeRegistries.BLOCKS.getValue(configBaseBlock);
        if (baseBlock == null || !belowState.is(baseBlock)) {
            return false;
        }

        // Check waterlogged requirement
        if (this.config.getActivation().requiresWaterlogged()) {
            BlockState currentState = level.getBlockState(basePos);
            if (currentState.hasProperty(WATERLOGGED)) {
                return currentState.getValue(WATERLOGGED);
            }
            return false;
        }

        return true;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}