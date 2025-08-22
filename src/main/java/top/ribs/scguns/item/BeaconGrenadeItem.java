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
import top.ribs.scguns.entity.throwable.ThrowableBeaconGrenadeEntity;

public class BeaconGrenadeItem extends AmmoItem {
    protected int maxCookTime;

    public BeaconGrenadeItem(Properties properties, int maxCookTime) {
        super(properties);
        this.maxCookTime = maxCookTime;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return this.maxCookTime;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        if (!this.canCook()) return;
        int duration = this.getUseDuration(stack) - count;
        if (duration == 10)
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0F, 1.0F, false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        playerIn.startUsingItem(handIn);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        if (this.canCook() && !worldIn.isClientSide() && !entityLiving.isUnderWater()) {
            if (!(entityLiving instanceof Player) || !((Player) entityLiving).isCreative())
                stack.shrink(1);
            ThrowableBeaconGrenadeEntity grenade = this.create(worldIn, entityLiving, 0);
            grenade.onDeath();
            if (entityLiving instanceof Player) {
                ((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
            }
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!worldIn.isClientSide() && !entityLiving.isUnderWater()) {
            int duration = this.getUseDuration(stack) - timeLeft;
            if (duration >= 10) {
                if (!(entityLiving instanceof Player) || !((Player) entityLiving).isCreative())
                    stack.shrink(1);
                ThrowableBeaconGrenadeEntity grenade = this.create(worldIn, entityLiving, this.maxCookTime - duration);
                grenade.shootFromRotation(entityLiving, entityLiving.getXRot(), entityLiving.getYRot(), 0.0F, Math.min(1.0F, duration / 10F), 1.0F);
                worldIn.addFreshEntity(grenade);
                this.onThrown(worldIn, grenade);
                if (entityLiving instanceof Player) {
                    ((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    public ThrowableBeaconGrenadeEntity create(Level world, LivingEntity entity, int timeLeft) {
        return new ThrowableBeaconGrenadeEntity(world, entity, timeLeft);
    }

    public boolean canCook() {
        return true;
    }

    protected void onThrown(Level world, ThrowableBeaconGrenadeEntity entity) {
    }
}