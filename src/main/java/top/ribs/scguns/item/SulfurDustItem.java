package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
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
import top.ribs.scguns.block.SulfurLayerBlock;
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
            if (applyCoinFlipEffect(itemstack, world, blockpos, player)) {
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
        if (world.getBlockState(blockposAbove).isAir()) {
            BlockState newState = ModBlocks.SULFUR_LAYER.get().defaultBlockState();
            world.setBlock(blockposAbove, newState, 2);
            if (player == null || !player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    public static boolean applyCoinFlipEffect(ItemStack stack, Level world, BlockPos pos, Player player) {
        BlockState blockstate = world.getBlockState(pos);
        if (blockstate.getBlock() instanceof BonemealableBlock bonemealableblock) {
            if (bonemealableblock.isValidBonemealTarget(world, pos, blockstate, world.isClientSide)) {
                if (world instanceof ServerLevel) {
                    RandomSource random = world.getRandom();

                    if (random.nextBoolean()) {
                        if (bonemealableblock.isBonemealSuccess(world, random, pos, blockstate)) {
                            bonemealableblock.performBonemeal((ServerLevel) world, random, pos, blockstate);
                            if (random.nextBoolean()) {
                                BlockState newState = world.getBlockState(pos);
                                if (newState.getBlock() instanceof BonemealableBlock newBonemealable &&
                                        newBonemealable.isBonemealSuccess(world, random, pos, newState)) {
                                    newBonemealable.performBonemeal((ServerLevel) world, random, pos, newState);
                                }
                            }
                        }
                    } else {
                        world.levelEvent(2001, pos, 0);
                    }
                    stack.shrink(1);
                }
                return true;
            }
        }
        return false;
    }
}