package top.ribs.scguns.util;


import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModItems;

public class ModCauldronInteraction {
    public static void register() {
        CauldronInteraction.EMPTY.put(ModItems.VICIOUS_ACID_BUCKET.get(), (state, level, pos, player, hand, stack) -> {
            if (!level.isClientSide) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                level.setBlockAndUpdate(pos, ModBlocks.VICIOUS_ACID_CAULDRON.get().defaultBlockState());
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        CauldronInteraction.EMPTY.put(Items.BUCKET, (state, level, pos, player, hand, stack) -> {
            if (level.getBlockState(pos).getBlock() == ModBlocks.VICIOUS_ACID_CAULDRON.get()) {
                if (!level.isClientSide) {
                    player.setItemInHand(hand, new ItemStack(ModItems.VICIOUS_ACID_BUCKET.get()));
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                    level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        });
    }
}
