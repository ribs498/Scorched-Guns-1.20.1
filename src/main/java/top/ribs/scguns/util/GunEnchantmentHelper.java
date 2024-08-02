package top.ribs.scguns.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
import java.util.Random;

/**
 * Author: MrCrayfish
 */
public class GunEnchantmentHelper
{

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
    public static double getAimDownSightSpeed(ItemStack weapon)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTWEIGHT.get(), weapon);
        return level > 0 ? 1.5 : 1.0;
    }

    public static double getProjectileSpeedModifier(ItemStack weapon) {
        int acceleratorLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR.get(), weapon);
        int heavyShotLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HEAVY_SHOT.get(), weapon);

        double speedModifier = 1.0;
        if (acceleratorLevel > 0) {
            speedModifier += 0.5 * acceleratorLevel;
        }

        if (heavyShotLevel > 0) {
            speedModifier -= 0.35 * heavyShotLevel;
        }
        return Mth.clamp(speedModifier, 0.1, 5.0);
    }

    public static int getRate(ItemStack weapon, Gun modifiedGun) {
        int baseRate = modifiedGun.getGeneral().getRate();

        int triggerFingerLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TRIGGER_FINGER.get(), weapon);
        int heavyShotLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HEAVY_SHOT.get(), weapon);

        float rateModifier = getRateModifier(triggerFingerLevel, heavyShotLevel);

        int modifiedRate = Math.round(baseRate * rateModifier);
        return Math.max(modifiedRate, 1);
    }

    private static float getRateModifier(int triggerFingerLevel, int heavyShotLevel) {
        float heavyShotModifier = 1.0f + (0.3f * heavyShotLevel);
        float triggerFingerModifier = 1.0f - (0.25f * triggerFingerLevel);
        float combinedModifier = heavyShotModifier * triggerFingerModifier;

        return Mth.clamp(combinedModifier, 0.5f, 2.0f);
    }
    public static float getRecoilModifier(ItemStack weapon) {
        int heavyShotLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HEAVY_SHOT.get(), weapon);
        return 1.0f + (0.15f * heavyShotLevel);
    }
    public static float getHeavyShotDamage(ItemStack weapon, float damage) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HEAVY_SHOT.get(), weapon);
        if (level > 0) {
            damage += damage * (0.05F * level);
        }
        return damage;
    }

    public static float getAcceleratorDamage(ItemStack weapon, float damage) {
        int acceleratorLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR.get(), weapon);
        if (acceleratorLevel > 0) {
            damage += damage * (0.1F * acceleratorLevel);
        }
        int heavyShotLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HEAVY_SHOT.get(), weapon);
        if (heavyShotLevel > 0) {
            damage += damage * (0.05F * heavyShotLevel);
        }
        return damage;
    }

    public static float getPuncturingChance(ItemStack weapon)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PUNCTURING.get(), weapon);
        return level * 0.05F;
    }
    public static ParticleOptions getParticle(ItemStack weapon)
    {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(weapon);
        if(enchantments.containsKey(ModEnchantments.FIRE_STARTER.get()))
        {
            return ParticleTypes.LAVA;
        }
        else if(enchantments.containsKey(ModEnchantments.PUNCTURING.get()))
    {
        return ParticleTypes.ENCHANTED_HIT;
    }
        else if(enchantments.containsKey(ModEnchantments.HEAVY_SHOT.get()))
        {
            return ParticleTypes.MYCELIUM;
        }
        else if(enchantments.containsKey(ModEnchantments.ELEMENTAL_POP.get()))
        {
            return ParticleTypes.CRIMSON_SPORE;
        }
        return new TrailData(weapon.isEnchanted());
    }
    private static final Map<MobEffect, Integer> ELEMENTAL_EFFECTS = new HashMap<>();

    static {
        ELEMENTAL_EFFECTS.put(MobEffects.POISON, 6);
        ELEMENTAL_EFFECTS.put(MobEffects.WITHER, 3);
        ELEMENTAL_EFFECTS.put(MobEffects.HEAL, 6);
        ELEMENTAL_EFFECTS.put(MobEffects.HARM, 6);
        ELEMENTAL_EFFECTS.put(MobEffects.REGENERATION, 5);
        ELEMENTAL_EFFECTS.put(MobEffects.FIRE_RESISTANCE, 5);
        ELEMENTAL_EFFECTS.put(MobEffects.LEVITATION, 3);
        ELEMENTAL_EFFECTS.put(MobEffects.MOVEMENT_SLOWDOWN, 7);
        ELEMENTAL_EFFECTS.put(MobEffects.WEAKNESS, 3);
        ELEMENTAL_EFFECTS.put(MobEffects.ABSORPTION, 3);
        ELEMENTAL_EFFECTS.put(MobEffects.DAMAGE_RESISTANCE, 6);
        ELEMENTAL_EFFECTS.put(MobEffects.INVISIBILITY, 3);

    }

    public static void applyElementalPopEffect(ItemStack weapon, LivingEntity target) {
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ELEMENTAL_POP.get(), weapon);

        if (enchantmentLevel > 0) {
            Random random = new Random();
            for (Map.Entry<MobEffect, Integer> entry : ELEMENTAL_EFFECTS.entrySet()) {
                MobEffect effect = entry.getKey();
                int baseChance = entry.getValue();
                int finalChance = baseChance + (enchantmentLevel * 3);

                if (random.nextInt(100) < finalChance) {
                    int duration = getRandomEffectDuration(effect, enchantmentLevel, random);
                    int amplifier = getRandomEffectAmplifier(effect, enchantmentLevel, random);

                    target.addEffect(new MobEffectInstance(effect, duration, amplifier));
                    triggerVisualSplashEffect(target, effect);
                    break;
                }
            }
        }
    }
    private static int getRandomEffectDuration(MobEffect effect, int enchantmentLevel, Random random) {
        int baseDuration = 60;
        int maxDuration = 200;
        if (effect.isInstantenous()) {
            return 1;
        }
        int duration = baseDuration + random.nextInt(maxDuration - baseDuration) + (enchantmentLevel * 20);
        return Math.min(duration, maxDuration);
    }

    private static int getRandomEffectAmplifier(MobEffect effect, int enchantmentLevel, Random random) {
        int baseAmplifier = 0;
        int maxAmplifier = 2;
        int amplifier = baseAmplifier + random.nextInt(enchantmentLevel + 1);
        return Math.min(amplifier, maxAmplifier);
    }
    private static void triggerVisualSplashEffect(LivingEntity target, MobEffect effect) {
        Level level = target.level();
        Vec3 position = target.position();
        int color = effect.getColor();
        double red = (color >> 16 & 255) / 255.0;
        double green = (color >> 8 & 255) / 255.0;
        double blue = (color & 255) / 255.0;
        for (int i = 0; i < 20; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
            level.addParticle(ParticleTypes.ENTITY_EFFECT, position.x + offsetX, position.y + offsetY, position.z + offsetZ, red, green, blue);
        }
    }
}