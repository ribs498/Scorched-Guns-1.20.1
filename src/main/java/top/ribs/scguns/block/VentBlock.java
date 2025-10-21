package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.common.Vent;
import top.ribs.scguns.common.VentManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public abstract class VentBlock extends Block {
    public static final EnumProperty<VentType> VENT_TYPE = EnumProperty.create("vent_type", VentType.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final IntegerProperty VENT_POWER = IntegerProperty.create("vent_power", 1, 5);

    protected static final VoxelShape SHAPE_BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_MIDDLE_TOP = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    protected static final int BASE_TICK_INTERVAL = 100;

    protected final Random random = new Random();
    public final ResourceLocation ventId;
    public Vent config;

    public VentBlock(Properties properties, ResourceLocation ventId) {
        super(properties);
        this.ventId = ventId;
        this.config = VentManager.getVent(ventId);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(VENT_TYPE, VentType.BASE)
                .setValue(ACTIVE, false)
                .setValue(VENT_POWER, 1));
    }

    public void reloadConfig() {
        this.config = VentManager.getVent(this.ventId);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VENT_TYPE, ACTIVE, VENT_POWER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean isActive = isActive(level, pos);
        int ventPower = calculateVentPower(level, pos);
        return this.updateState(level.getBlockState(pos.below()), level.getBlockState(pos.above()))
                .setValue(ACTIVE, isActive)
                .setValue(VENT_POWER, ventPower);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                           LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level instanceof Level) {
            boolean isActive = isActive(level, pos);
            int ventPower = calculateVentPower((Level) level, pos);
            return this.updateState(level.getBlockState(pos.below()), level.getBlockState(pos.above()))
                    .setValue(ACTIVE, isActive)
                    .setValue(VENT_POWER, ventPower);
        }
        return this.updateState(level.getBlockState(pos.below()), level.getBlockState(pos.above()));
    }

    protected int calculateVentPower(LevelAccessor level, BlockPos pos) {
        if (this.config == null) {
            reloadConfig();
            if (this.config == null) return 1;
        }

        BlockPos basePos = getBasePos(level, pos);
        int power = 1;
        BlockPos checkPos = basePos.above();
        int maxPower = this.config.getPower().getMaxPower();

        while (level.getBlockState(checkPos).getBlock() instanceof VentBlock && power < maxPower) {
            power++;
            checkPos = checkPos.above();
        }

        return power;
    }

    protected void updateVentPower(Level level, BlockPos pos) {
        BlockPos basePos = getBasePos(level, pos);
        int power = calculateVentPower(level, basePos);

        BlockState baseState = level.getBlockState(basePos);
        level.setBlock(basePos, baseState.setValue(VENT_POWER, power), 3);

        BlockPos checkPos = basePos.above();
        while (level.getBlockState(checkPos).getBlock() instanceof VentBlock) {
            BlockState state = level.getBlockState(checkPos);
            level.setBlock(checkPos, state.setValue(VENT_POWER, power), 3);
            checkPos = checkPos.above();
        }
    }

    protected BlockState updateState(BlockState belowState, BlockState aboveState) {
        if (belowState.getBlock() instanceof VentBlock) {
            if (aboveState.getBlock() instanceof VentBlock) {
                return this.defaultBlockState().setValue(VENT_TYPE, VentType.MIDDLE);
            } else {
                return this.defaultBlockState().setValue(VENT_TYPE, VentType.TOP);
            }
        } else {
            return this.defaultBlockState().setValue(VENT_TYPE, VentType.BASE);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return state.getValue(VENT_TYPE) == VentType.BASE ? SHAPE_BASE : SHAPE_MIDDLE_TOP;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        if (block instanceof PistonBaseBlock) {
            level.destroyBlock(pos, true);
        }

        boolean isActive = isActive(level, pos);
        level.setBlock(pos, state.setValue(ACTIVE, isActive), 3);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        boolean isActive = isActive(level, pos);
        int ventPower = calculateVentPower(level, pos);
        level.setBlock(pos, state.setValue(ACTIVE, isActive).setValue(VENT_POWER, ventPower), 3);

        level.scheduleTick(pos, this, calculateNextTickInterval());
        updateVentPower(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (!(newState.getBlock() instanceof VentBlock)) {
            BlockState stateBelow = level.getBlockState(pos.below());
            if (stateBelow.getBlock() instanceof VentBlock)
                updateVentPower(level, pos.below());
        }
    }

    @Override
    public abstract void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random);

    protected int calculateNextTickInterval() {
        if (this.config == null) {
            reloadConfig();
            if (this.config == null) return BASE_TICK_INTERVAL;
        }
        return this.config.getPower().getBaseTickInterval() +
                random.nextInt(this.config.getPower().getTickWiggleRoom());
    }

    protected boolean hasVentCollectorAbove(LevelReader level, BlockPos pos) {
        BlockPos topPos = getTopPos(level, pos);
        return level.getBlockState(topPos.above()).getBlock() instanceof VentCollectorBlock;
    }

    protected BlockPos getTopPos(LevelReader level, BlockPos pos) {
        while (level.getBlockState(pos.above()).getBlock() instanceof VentBlock) {
            pos = pos.above();
        }
        return pos;
    }

    protected BlockPos getBasePos(LevelAccessor level, BlockPos pos) {
        while (level.getBlockState(pos.below()).getBlock() instanceof VentBlock) {
            if (pos.getY() <= 0) {
                break;
            }
            pos = pos.below();
        }
        return pos;
    }

    protected boolean isVentAbove(Level level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        return level.getBlockState(abovePos).getBlock() instanceof VentBlock;
    }

    protected abstract boolean isActive(LevelAccessor level, BlockPos pos);

    protected boolean isTopOrBaseWithoutTop(BlockState state, Level level, BlockPos pos) {
        return state.getValue(VENT_TYPE) == VentType.TOP ||
                (state.getValue(VENT_TYPE) == VentType.BASE && !isVentAbove(level, pos));
    }

    protected void playAmbientSound(Level level, BlockPos pos, RandomSource random) {
        if (this.config == null || !this.config.getParticles().showActive()) return;

        ResourceLocation soundLoc = this.config.getParticles().getActiveSound();
        if (soundLoc == null) return;

        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(soundLoc);
        if (sound == null) return;

        if (random.nextInt(20) == 0) {
            level.playLocalSound(
                    (double) pos.getX() + 0.5,
                    (double) pos.getY() + 0.5,
                    (double) pos.getZ() + 0.5,
                    sound,
                    SoundSource.BLOCKS,
                    0.5F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.6F,
                    false
            );
        }
    }

    public ItemStack selectRandomOutput(RandomSource random) {
        if (this.config == null) return ItemStack.EMPTY;

        List<Vent.Production.OutputItem> outputs = this.config.getProduction().getOutputs();
        if (outputs.isEmpty()) return ItemStack.EMPTY;

        // Calculate total weight
        int totalWeight = 0;
        for (Vent.Production.OutputItem output : outputs) {
            totalWeight += output.getWeight();
        }

        if (totalWeight <= 0) return ItemStack.EMPTY;

        // Weighted random selection
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (Vent.Production.OutputItem output : outputs) {
            currentWeight += output.getWeight();
            if (randomValue < currentWeight) {
                Item item = output.getItem();
                if (item != null) {
                    return new ItemStack(item, 1);
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public boolean shouldProduce(RandomSource random) {
        if (this.config == null) return false;
        return random.nextFloat() < this.config.getProduction().getProductionChance();
    }

    protected boolean shouldShowParticles() {
        if (this.config == null) return true;
        return this.config.getParticles().showActive();
    }

    public enum VentType implements StringRepresentable {
        BASE("base"),
        MIDDLE("middle"),
        TOP("top");

        private final String name;

        VentType(String name) {
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