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
import top.ribs.scguns.init.ModBlocks;

public class SulfurDustItem extends Item {
    public SulfurDustItem(Properties properties) {
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
            if (applyVolatileFertilizer(itemstack, world, blockpos, player)) {
                if (!world.isClientSide) {
                    world.levelEvent(1505, blockpos, 0);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }

        if (blockstate.is(ModBlocks.SULFUR_LAYER.get())) {
            int i = blockstate.getValue(SnowLayerBlock.LAYERS);
            if (i < 8) {
                world.setBlock(blockpos, blockstate.setValue(SnowLayerBlock.LAYERS, i + 1), 2);
                if (player == null || !player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }

        BlockPos blockposAbove = blockpos.above();
        BlockState stateAbove = world.getBlockState(blockposAbove);
        if (stateAbove.isAir() && blockstate.isFaceSturdy(world, blockpos, Direction.UP)) {
            BlockState newState = ModBlocks.SULFUR_LAYER.get().defaultBlockState();
            world.setBlock(blockposAbove, newState, 2);
            if (player == null || !player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    public static boolean applyVolatileFertilizer(ItemStack stack, Level world, BlockPos pos, Player player) {
        BlockState blockstate = world.getBlockState(pos);
        if (blockstate.getBlock() instanceof BonemealableBlock bonemealableblock) {
            if (bonemealableblock.isValidBonemealTarget(world, pos, blockstate, world.isClientSide)) {
                if (world instanceof ServerLevel serverLevel) {
                    RandomSource random = world.getRandom();

                    if (random.nextBoolean()) {
                        int stages = 3 + random.nextInt(2);
                        for (int i = 0; i < stages; i++) {
                            BlockState currentState = world.getBlockState(pos);
                            if (currentState.getBlock() instanceof BonemealableBlock currentBonemeal) {
                                if (currentBonemeal.isValidBonemealTarget(world, pos, currentState, false)) {
                                    if (currentBonemeal.isBonemealSuccess(world, random, pos, currentState)) {
                                        currentBonemeal.performBonemeal(serverLevel, random, pos, currentState);

                                        if (i > 0) {
                                            world.levelEvent(1505, pos, 0);
                                        }
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                    } else {
                        world.destroyBlock(pos, true);
                        world.levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(blockstate));
                    }

                    stack.shrink(1);
                }
                return true;
            }
        }
        return false;
    }
}