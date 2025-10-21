package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import top.ribs.scguns.block.NiterLayerBlock;
import top.ribs.scguns.init.ModBlocks;

public class NiterDustItem extends Item {
    public NiterDustItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack itemstack = context.getItemInHand();

        BlockState blockstate = world.getBlockState(blockpos);

        if (blockstate.getBlock() instanceof BonemealableBlock) {
            if (applyReliableFertilizer(itemstack, world, blockpos, player)) {
                if (!world.isClientSide) {
                    world.levelEvent(1505, blockpos, 0);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }

        if (blockstate.is(ModBlocks.NITER_LAYER.get())) {
            int i = blockstate.getValue(SnowLayerBlock.LAYERS);
            if (i < 8) {
                world.setBlock(blockpos, blockstate.setValue(SnowLayerBlock.LAYERS, i + 1).setValue(NiterLayerBlock.WATERLOGGED, blockstate.getValue(NiterLayerBlock.WATERLOGGED)), 2);
                if (player == null || !player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }

        BlockPos blockposAbove = blockpos.above();
        BlockState stateAbove = world.getBlockState(blockposAbove);
        FluidState fluidStateAbove = world.getFluidState(blockposAbove);

        if ((stateAbove.isAir() || fluidStateAbove.getType() == Fluids.WATER || fluidStateAbove.getType() == Fluids.FLOWING_WATER)
                && blockstate.isFaceSturdy(world, blockpos, Direction.UP)) {
            BlockState newState = ModBlocks.NITER_LAYER.get().defaultBlockState()
                    .setValue(NiterLayerBlock.WATERLOGGED, fluidStateAbove.getType() == Fluids.WATER || fluidStateAbove.getType() == Fluids.FLOWING_WATER);
            world.setBlock(blockposAbove, newState, 2);
            if (player == null || !player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    public static boolean applyReliableFertilizer(ItemStack stack, Level world, BlockPos pos, Player player) {
        BlockState blockstate = world.getBlockState(pos);
        if (blockstate.getBlock() instanceof BonemealableBlock bonemealableblock) {
            if (bonemealableblock.isValidBonemealTarget(world, pos, blockstate, world.isClientSide)) {
                if (world instanceof ServerLevel serverLevel) {
                    RandomSource random = world.getRandom();

                    if (bonemealableblock.isBonemealSuccess(world, random, pos, blockstate)) {
                        bonemealableblock.performBonemeal(serverLevel, random, pos, blockstate);
                    }

                    if (random.nextFloat() < 0.33f) {
                        Direction[] directions = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
                        Direction randomDirection = directions[random.nextInt(directions.length)];
                        BlockPos adjacentPos = pos.relative(randomDirection);
                        BlockState adjacentState = world.getBlockState(adjacentPos);

                        if (adjacentState.getBlock() instanceof BonemealableBlock adjacentBonemeal) {
                            if (adjacentBonemeal.isValidBonemealTarget(world, adjacentPos, adjacentState, false)) {
                                if (adjacentBonemeal.isBonemealSuccess(world, random, adjacentPos, adjacentState)) {
                                    adjacentBonemeal.performBonemeal(serverLevel, random, adjacentPos, adjacentState);
                                    world.levelEvent(1505, adjacentPos, 0);
                                }
                            }
                        }
                    }

                    stack.shrink(1);
                }
                return true;
            }
        }
        return false;
    }
}