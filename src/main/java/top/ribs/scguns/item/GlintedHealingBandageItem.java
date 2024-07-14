package top.ribs.scguns.item;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class GlintedHealingBandageItem extends HealingBandageItem{
    public GlintedHealingBandageItem(Properties properties, int healingAmount, MobEffectInstance... potionEffects) {
        super(properties, healingAmount, potionEffects);
        this.healingAmount = healingAmount;
        this.potionEffects = Arrays.asList(potionEffects);
    }


    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
