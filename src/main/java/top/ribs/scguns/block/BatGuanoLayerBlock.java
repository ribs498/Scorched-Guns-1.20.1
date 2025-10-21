package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModParticleTypes;

import javax.annotation.Nullable;

public class BatGuanoLayerBlock extends SnowLayerBlock {

    public BatGuanoLayerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.BAT_GUANO.get());
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, world, pos, oldState, isMoving);
        if (!state.is(oldState.getBlock())) {
            world.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return super.canSurvive(state, world, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState blockstate = context.getLevel().getBlockState(pos);

        if (blockstate.is(this)) {
            int layers = blockstate.getValue(LAYERS);
            return blockstate.setValue(LAYERS, Math.min(8, layers + 1));
        } else {
            return this.defaultBlockState().setValue(LAYERS, 1);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            double d0 = pos.getX() + 0.5D;
            double d1 = pos.getY() + 0.3D;
            double d2 = pos.getZ() + 0.5D;
            double offsetX = (random.nextDouble() - 0.5D) * 0.3D;
            double offsetZ = (random.nextDouble() - 0.5D) * 0.3D;
            double offsetY = random.nextDouble() * 0.1D;

            level.addParticle(ModParticleTypes.SULFUR_DUST.get(),
                    d0 + offsetX,
                    d1 + offsetY,
                    d2 + offsetZ,
                    0.0D, 0.03D, 0.0D);
        }
    }
}