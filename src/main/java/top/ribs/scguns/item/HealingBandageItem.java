package top.ribs.scguns.item;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.ScorchedGuns;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HealingBandageItem extends Item {
    int healingAmount;
    List<MobEffectInstance> potionEffects;

    public HealingBandageItem(Item.Properties properties, int healingAmount, MobEffectInstance... potionEffects) {
        super(properties);
        // Filter out null MobEffectInstances
        this.potionEffects = Arrays.stream(potionEffects).filter(Objects::nonNull).toList();
        this.healingAmount = healingAmount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(world, player, hand);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
        if (entityLiving instanceof Player player && !world.isClientSide) {
            // Heal the player
            player.heal(healingAmount);

            // Apply potion effects from the bandage
            for (MobEffectInstance effect : potionEffects) {
                if (effect != null) {
                    player.addEffect(new MobEffectInstance(effect));
                }
            }

            // Reduce the stack size unless the player is in creative mode
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
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.scguns.healing_bandage.heal", healingAmount).withStyle(ChatFormatting.GREEN));
        if (!potionEffects.isEmpty()) {
            for (MobEffectInstance effect : potionEffects) {
                if (effect != null && effect.getEffect() != null) {
                    Component effectName = Component.translatable(effect.getEffect().getDescriptionId()).withStyle(ChatFormatting.BLUE);
                    int durationInSeconds = effect.getDuration() / 20;
                    int minutes = durationInSeconds / 60;
                    int seconds = durationInSeconds % 60;
                    String formattedDuration = String.format(" (%02d:%02d)", minutes, seconds);
                    Component effectDuration = Component.literal(formattedDuration).withStyle(ChatFormatting.BLUE);
                    tooltip.add(Component.empty().append(effectName).append(effectDuration));
                }
            }
        }
        super.appendHoverText(stack, world, tooltip, flag);
    }


}
