package top.ribs.scguns.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import top.ribs.scguns.entity.throwable.ThrowableShotballEntity;

public class ThrowableShotballItem extends AmmoItem {
    private static final int MAX_CHARGE_TIME = 60; // 3 seconds at 20 ticks per second

    public ThrowableShotballItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return MAX_CHARGE_TIME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        // Optional: Play charging sound effect
        int duration = this.getUseDuration(stack) - count;
        if (duration == 5) {
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_LOADING_START, SoundSource.PLAYERS, 0.5F, 1.2F, false);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        // Check if player is underwater - shotballs are too heavy to throw underwater effectively
        if (playerIn.isUnderWater()) {
            return InteractionResultHolder.fail(stack);
        }

        playerIn.startUsingItem(handIn);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        // Maximum charge throw
        if (!worldIn.isClientSide() && !entityLiving.isUnderWater()) {
            if (!(entityLiving instanceof Player) || !((Player) entityLiving).isCreative()) {
                stack.shrink(1);
            }

            ThrowableShotballEntity shotball = new ThrowableShotballEntity(worldIn, entityLiving);
            shotball.shootFromRotation(entityLiving, entityLiving.getXRot(), entityLiving.getYRot(),
                    0.0F, 1.5F, 0.5F); // Strong throw with max charge
            worldIn.addFreshEntity(shotball);

            // Play throw sound
            worldIn.playSound(null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 1.0F, 0.8F);

            if (entityLiving instanceof Player) {
                ((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
            }
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!worldIn.isClientSide() && !entityLiving.isUnderWater()) {
            int chargeDuration = this.getUseDuration(stack) - timeLeft;

            if (chargeDuration >= 5) { // Minimum charge time
                if (!(entityLiving instanceof Player) || !((Player) entityLiving).isCreative()) {
                    stack.shrink(1);
                }

                ThrowableShotballEntity shotball = new ThrowableShotballEntity(worldIn, entityLiving);

                // Calculate throw power based on charge time
                float power = Math.min(5.0F, 1.0F + (chargeDuration / (float) MAX_CHARGE_TIME));
                float accuracy = Math.max(0.5F, 1.0F - (chargeDuration / (float) MAX_CHARGE_TIME) * 0.5F);

                shotball.shootFromRotation(entityLiving, entityLiving.getXRot(), entityLiving.getYRot(),
                        0.0F, power, accuracy);
                worldIn.addFreshEntity(shotball);

                // Play throw sound - pitch varies with power
                worldIn.playSound(null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(),
                        SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 1.0F, 0.6F + (power * 0.4F));

                if (entityLiving instanceof Player) {
                    ((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }
}