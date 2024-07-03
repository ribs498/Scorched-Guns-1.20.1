package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.init.ModItems;

public class EmptyBlasphemyItem extends Item {
    public EmptyBlasphemyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof SpawnerBlockEntity) {
            BaseSpawner spawner = ((SpawnerBlockEntity) blockEntity).getSpawner();
            if (isBlazeSpawner(spawner, level, pos)) {
                return captureBlaze(context, player);
            }
        }
        return super.useOn(context);
    }
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target.getType() == EntityType.BLAZE) {
            InteractionResult result = captureBlaze(player.level(), target.blockPosition(), player, stack, hand);
            target.discard();
            return result;
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }
    private boolean isBlazeSpawner(BaseSpawner spawner, Level level, BlockPos pos) {
        RandomSource random = level.random;
        return spawner.getOrCreateDisplayEntity(level, random, pos) != null &&
                spawner.getOrCreateDisplayEntity(level, random, pos).getType() == EntityType.BLAZE;
    }
    private InteractionResult captureBlaze(Level level, BlockPos pos, Player player, ItemStack stack, InteractionHand hand) {
        if (!level.isClientSide) {
            level.playSound(null, pos, SoundEvents.BLAZE_HURT, SoundSource.HOSTILE, 1.0F, 1.0F);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1.0F, 1.0F);
            ItemStack blasphemyItem = new ItemStack(ModItems.BLASPHEMY.get());
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            if (stack.isEmpty()) {
                player.setItemInHand(hand, blasphemyItem);
            } else {
                player.getInventory().placeItemBackInInventory(blasphemyItem);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult captureBlaze(UseOnContext context, Player player) {
        return captureBlaze(context.getLevel(), context.getClickedPos(), player, context.getItemInHand(), context.getHand());
    }
}
