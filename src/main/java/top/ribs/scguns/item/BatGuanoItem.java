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

public class BatGuanoItem extends Item {
    private static final float BASE_SUCCESS_CHANCE = 0.95f;
    private static final float ADJACENT_SPREAD_CHANCE = 0.45f;
    private static final float CASCADE_CHANCE = 0.15f;

    public BatGuanoItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack itemstack = context.getItemInHand();

        BlockState blockstate = world.getBlockState(blockpos);

        if (blockstate.getBlock() instanceof BonemealableBlock) {
            if (applyGuanoFertilizer(itemstack, world, blockpos, player)) {
                if (!world.isClientSide) {
                    world.levelEvent(1505, blockpos, 0);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }

        if (blockstate.is(ModBlocks.BAT_GUANO_LAYER.get())) {
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
            BlockState newState = ModBlocks.BAT_GUANO_LAYER.get().defaultBlockState();
            world.setBlock(blockposAbove, newState, 2);
            if (player == null || !player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    public static boolean applyGuanoFertilizer(ItemStack stack, Level world, BlockPos pos, Player player) {
        BlockState blockstate = world.getBlockState(pos);
        if (blockstate.getBlock() instanceof BonemealableBlock bonemealableblock) {
            if (bonemealableblock.isValidBonemealTarget(world, pos, blockstate, world.isClientSide)) {
                if (world instanceof ServerLevel serverLevel) {
                    RandomSource random = world.getRandom();

                    if (random.nextFloat() < BASE_SUCCESS_CHANCE && bonemealableblock.isBonemealSuccess(world, random, pos, blockstate)) {
                        bonemealableblock.performBonemeal(serverLevel, random, pos, blockstate);
                    }

                    if (random.nextFloat() < ADJACENT_SPREAD_CHANCE) {
                        spreadToAdjacent(serverLevel, pos, random, CASCADE_CHANCE);
                    }

                    stack.shrink(1);
                }
                return true;
            }
        }
        return false;
    }

    private static void spreadToAdjacent(ServerLevel world, BlockPos centerPos, RandomSource random, float cascadeChance) {
        Direction[] directions = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);

        for (Direction direction : directions) {
            BlockPos adjacentPos = centerPos.relative(direction);
            BlockState adjacentState = world.getBlockState(adjacentPos);

            if (adjacentState.getBlock() instanceof BonemealableBlock adjacentBonemeal) {
                if (adjacentBonemeal.isValidBonemealTarget(world, adjacentPos, adjacentState, false)) {
                    if (adjacentBonemeal.isBonemealSuccess(world, random, adjacentPos, adjacentState)) {
                        adjacentBonemeal.performBonemeal(world, random, adjacentPos, adjacentState);
                        world.levelEvent(1505, adjacentPos, 0);

                        if (random.nextFloat() < cascadeChance) {
                            spreadToAdjacent(world, adjacentPos, random, cascadeChance * 0.5f);
                        }
                    }
                }
            }
        }
    }
}