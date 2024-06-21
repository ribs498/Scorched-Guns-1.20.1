package top.ribs.scguns.util;

import net.minecraft.nbt.CompoundTag;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.BayonetItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.particles.TrailData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class GunEnchantmentHelper
{
    public static ParticleOptions getParticle(ItemStack weapon)
    {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(weapon);
        /*if(enchantments.containsKey(ModEnchantments.FIRE_STARTER.get()))
        {
            return ParticleTypes.LAVA;
        }
        else */if(enchantments.containsKey(ModEnchantments.PUNCTURING.get()))
    {
        return ParticleTypes.ENCHANTED_HIT;
    }
        return new TrailData(weapon.isEnchanted());
    }

    public static int getRealReloadSpeed(ItemStack weapon)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        if (modifiedGun.getReloads().getReloadType() == ReloadType.MAG_FED)
            return getMagReloadSpeed(weapon);
        else
            return getReloadInterval(weapon);
    }
    public static int getReloadInterval(ItemStack weapon) {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        ReloadType reloadType = modifiedGun.getReloads().getReloadType();

        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.QUICK_HANDS.get(), weapon);
        double decreaseFactor = 1 - (0.25 * level);

        if (reloadType == ReloadType.MANUAL) {
            int bulletReloadTime = modifiedGun.getReloads().getReloadTimer();
            double interval = bulletReloadTime * decreaseFactor;
            interval = GunModifierHelper.getModifiedReloadSpeed(weapon, interval);
            return Math.max((int) Math.round(interval), 1);
        } else {
            int baseInterval = 10;
            double interval = baseInterval * decreaseFactor;
            interval = GunModifierHelper.getModifiedReloadSpeed(weapon, interval);
            return Math.max((int) Math.round(interval), 1);
        }
    }


    public static int getMagReloadSpeed(ItemStack weapon) {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        int baseSpeed = modifiedGun.getReloads().getEmptyMagTimer() + modifiedGun.getReloads().getReloadTimer();
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.QUICK_HANDS.get(), weapon);
        double decreaseFactor = 1 - (0.25 * level);
        double speed = baseSpeed * decreaseFactor;
        speed = GunModifierHelper.getModifiedReloadSpeed(weapon, speed);
        return Math.max((int) Math.round(speed), 4);
    }



    public static int getRate(ItemStack weapon, Gun modifiedGun)
    {
        int rate = modifiedGun.getGeneral().getRate();
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TRIGGER_FINGER.get(), weapon);
        if(level > 0)
        {
            float newRate = rate * (0.25F * level);
            rate -= Mth.clamp(newRate, 0, rate);
        }
        return rate;
    }

    public static double getAimDownSightSpeed(ItemStack weapon)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTWEIGHT.get(), weapon);
        return level > 0 ? 1.5 : 1.0;
    }

    /*public static int getAmmoCapacity(ItemStack weapon, Gun modifiedGun)
    {
        int capacity = modifiedGun.getReloads().getMaxAmmo();
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.OVER_CAPACITY.get(), weapon);
        if(level > 0)
        {
            capacity += Math.max(level, (capacity / 2) * level);
        }
        return capacity;
    }*/

    public static double getProjectileSpeedModifier(ItemStack weapon)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR.get(), weapon);
        if(level > 0)
        {
            return 1.0 + 0.5 * level;
        }
        return 1.0;
    }

    public static float getAcceleratorDamage(ItemStack weapon, float damage)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR.get(), weapon);
        if(level > 0)
        {
            return damage + damage * (0.1F * level);
        }
        return damage;
    }

    public static float getPuncturingChance(ItemStack weapon)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PUNCTURING.get(), weapon);
        return level * 0.05F;
    }
}