package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A basic item class that implements {@link IAmmo} to indicate this item is ammunition
 *
 * Author: MrCrayfish
 */
public class FuelAmmoItem extends Item implements IAmmo {
    private final int burnTime;
    private final List<MobEffectInstance> potionEffects;
    private final Supplier<Item> containerItem;

    public FuelAmmoItem(Properties properties, int burnTime, Supplier<Item> containerItem, MobEffectInstance... potionEffects) {
        super(properties);
        this.burnTime = burnTime;
        this.containerItem = containerItem;
        this.potionEffects = Arrays.stream(potionEffects).filter(Objects::nonNull).toList();
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return new ItemStack(containerItem.get());
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return this.burnTime;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(world, player, hand);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
        if (entityLiving instanceof Player player && !world.isClientSide) {
            for (MobEffectInstance effect : potionEffects) {
                if (effect != null) {
                    player.addEffect(new MobEffectInstance(effect));
                }
            }

            if (!player.getAbilities().instabuild) {
                ItemStack containerStack = new ItemStack(containerItem.get());
                if (stack.getCount() > 1) {
                    stack.shrink(1);
                    if (!player.getInventory().add(containerStack)) {
                        player.drop(containerStack, false);
                    }
                    return stack;
                } else {
                    return containerStack;
                }
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
        return UseAnim.DRINK;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        if (!potionEffects.isEmpty()) {
            tooltip.add(Component.translatable("item.tooltip.fuel_effects").withStyle(ChatFormatting.GRAY));
            for (MobEffectInstance effect : potionEffects) {
                if (effect != null) {
                    effect.getEffect();
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