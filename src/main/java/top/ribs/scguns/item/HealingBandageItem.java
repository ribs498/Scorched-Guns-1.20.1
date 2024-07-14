package top.ribs.scguns.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class HealingBandageItem extends Item {
    int healingAmount;
    List<MobEffectInstance> potionEffects;

    public HealingBandageItem(Item.Properties properties, int healingAmount, MobEffectInstance... potionEffects) {
        super(properties);
        this.healingAmount = healingAmount;
        this.potionEffects = Arrays.asList(potionEffects);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(world, player, hand);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
        if (entityLiving instanceof Player player && !world.isClientSide) {
            player.heal(healingAmount);
            for (MobEffectInstance effect : potionEffects) {
                if (effect != null) {
                    player.addEffect(new MobEffectInstance(effect));
                }
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }
}