package top.ribs.scguns.util;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.cache.HotBarrelCache;
import top.ribs.scguns.common.*;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.item.GunItem;
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
    public static float getChargeDamage(ItemStack weapon, float damage, float chargeProgress) {
        if (!(weapon.getItem() instanceof GunItem gunItem)) {
            return damage;
        }

        Gun modifiedGun = gunItem.getModifiedGun(weapon);
        if (modifiedGun.getGeneral().getFireTimer() <= 0) {
            return damage;
        }

        float minDamagePercent = 0.85f;
        float fullChargeThreshold = 0.95f;

        float effectiveCharge = (chargeProgress >= fullChargeThreshold) ? 1.0f : chargeProgress;

        float normalizedCharge = effectiveCharge < fullChargeThreshold ?
                effectiveCharge / fullChargeThreshold : 1.0f;

        float damageReductionFactor = minDamagePercent +
                (float)(Math.sqrt(normalizedCharge) * (1.0f - minDamagePercent));

        return damage * damageReductionFactor;
    }

    public static int getRealReloadSpeed(ItemStack weapon)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        if (modifiedGun.getReloads().getReloadType() == ReloadType.MAG_FED)
            return getMagReloadSpeed(weapon);
        else
            return getReloadInterval(weapon);
    }

    /**
     * Gets reload interval without hot barrel modifications to prevent timing issues
     */
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
        }
        else if (reloadType == ReloadType.SINGLE_ITEM) {
            int bulletReloadTime = modifiedGun.getReloads().getReloadTimer();
            double interval = bulletReloadTime * decreaseFactor;
            interval = GunModifierHelper.getModifiedReloadSpeed(weapon, interval);
            return Math.max((int) Math.round(interval), 1);
        }
        else {
            int baseInterval = 10;
            double interval = baseInterval * decreaseFactor;
            interval = GunModifierHelper.getModifiedReloadSpeed(weapon, interval);
            return Math.max((int) Math.round(interval), 1);
        }
    }

    public static int getMagReloadSpeed(ItemStack weapon) {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);

        int baseSpeed = modifiedGun.getReloads().getReloadTimer();

        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.QUICK_HANDS.get(), weapon);
        double decreaseFactor = 1 - (0.25 * level);
        double speed = baseSpeed * decreaseFactor;
        speed = GunModifierHelper.getModifiedReloadSpeed(weapon, speed);
        return Math.max((int) Math.round(speed), 4);
    }

    public static double getAimDownSightSpeed(ItemStack weapon)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTWEIGHT.get(), weapon);
        return level > 0 ? 1.2 : 1.0;
    }

    public static double getProjectileSpeedModifier(ItemStack weapon) {
        int acceleratorLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR.get(), weapon);
        int heavyShotLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HEAVY_SHOT.get(), weapon);
        double speedModifier = 1.0;
        if (acceleratorLevel > 0) {
            speedModifier += 0.4 * acceleratorLevel;
        }
        if (heavyShotLevel > 0) {
            speedModifier -= 0.10 * heavyShotLevel;
        }
        return Mth.clamp(speedModifier, 0.1, 5.0);
    }

    public static int getRate(ItemStack weapon, Gun modifiedGun) {
        int baseRate = modifiedGun.getGeneral().getRate();
        int triggerFingerLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TRIGGER_FINGER.get(), weapon);
        int heavyShotLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HEAVY_SHOT.get(), weapon);
        float rateModifier = getRateModifier(triggerFingerLevel, heavyShotLevel);
        int modifiedRate = Math.round(baseRate * rateModifier);
        modifiedRate = getHotBarrelFireRate(weapon, modifiedRate);
        return Math.max(modifiedRate, 1);
    }

    public static int getHotBarrelFireRate(ItemStack weapon, int baseRate) {
        return baseRate;
    }

    public static float getRecoilModifier(ItemStack weapon) {
        int heavyShotLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HEAVY_SHOT.get(), weapon);
        return 1.0f + (0.35f * heavyShotLevel);
    }


    public static float getRecoilModifier(Player player, ItemStack weapon) {
        float baseModifier = getRecoilModifier(weapon);
        if (player != null) {
            baseModifier = getHotBarrelRecoil(player, weapon, baseModifier);
        }

        return baseModifier;
    }

    public static float getKickModifier(ItemStack weapon) {
        // Heavy Shot increases kick as well
        int heavyShotLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HEAVY_SHOT.get(), weapon);
        return 1.0f + (0.25f * heavyShotLevel); // Slightly less kick increase than recoil
    }

    public static float getKickModifier(Player player, ItemStack weapon) {
        float baseModifier = getKickModifier(weapon);

        if (player != null) {
            int hotBarrelLevel = HotBarrelCache.getHotBarrelLevel(player, weapon);
            float kickIncreaseFactor = 1.0f + (hotBarrelLevel / 100.0f) * 0.5f;
            baseModifier *= kickIncreaseFactor;
        }

        return baseModifier;
    }

    private static float getRateModifier(int triggerFingerLevel, int heavyShotLevel) {
        float heavyShotModifier = 1.0f + (0.27f * heavyShotLevel);
        float triggerFingerModifier = 1.0f - (0.12f * triggerFingerLevel);
        float combinedModifier = heavyShotModifier * triggerFingerModifier;
        return Mth.clamp(combinedModifier, 0.5f, 2.0f);
    }

    public static float getHeavyShotDamage(ItemStack weapon, float damage) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HEAVY_SHOT.get(), weapon);
        if (level > 0) {
            damage += damage * (0.1F * level);
        }
        return damage;
    }

    public static float getAcceleratorDamage(ItemStack weapon, float damage) {
        int acceleratorLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR.get(), weapon);
        if (acceleratorLevel > 0) {
            damage += damage * (0.06F * acceleratorLevel);
        }
        return damage;
    }

    public static float getHotBarrelDamage(Player player, ItemStack weapon, float baseDamage) {
        int hotBarrelLevel = HotBarrelCache.getHotBarrelLevel(player, weapon);
        float damageBoost = (hotBarrelLevel / 100.0f) * 0.35f;
        return baseDamage + (baseDamage * damageBoost);
    }

    public static int getHotBarrelFireRate(Player player, ItemStack weapon, int baseRate) {
        int hotBarrelLevel = HotBarrelCache.getHotBarrelLevel(player, weapon);
        float fireRateBoost = (hotBarrelLevel / 100.0f) * 0.25f;
        return Math.max((int) (baseRate * (1.0f - fireRateBoost)), 1);
    }

    public static float getHotBarrelRecoil(Player player, ItemStack weapon, float baseRecoil) {
        int hotBarrelLevel = HotBarrelCache.getHotBarrelLevel(player, weapon);
        float recoilIncreaseFactor = 1.0f + (hotBarrelLevel / 100.0f) * 0.75f;
        return baseRecoil * recoilIncreaseFactor;
    }

    public static float getHotBarrelSpread(Player player, ItemStack weapon, float baseSpread) {
        int hotBarrelLevel = HotBarrelCache.getHotBarrelLevel(player, weapon);
        float spreadIncrease = (hotBarrelLevel / 100.0f) * 1.5f;
        return baseSpread + (baseSpread * spreadIncrease);
    }

    public static boolean shouldSetOnFire(Player player, ItemStack weapon) {
        int hotBarrelLevel = HotBarrelCache.getHotBarrelLevel(player, weapon);
        return hotBarrelLevel >= 60;
    }

    public static ParticleOptions getParticle(ItemStack weapon) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(weapon);
        if (enchantments.containsKey(ModEnchantments.PUNCTURING.get())) {
            return ParticleTypes.ENCHANTED_HIT;
        } else if (enchantments.containsKey(ModEnchantments.HEAVY_SHOT.get())) {
            return ParticleTypes.MYCELIUM;
        } else if (enchantments.containsKey(ModEnchantments.ELEMENTAL_POP.get())) {
            return ParticleTypes.CRIMSON_SPORE;
        }
        return new TrailData(weapon.isEnchanted());
    }

    public static float getPuncturingChance(ItemStack weapon)
    {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PUNCTURING.get(), weapon);
        return level * 0.05F;
    }

    public static float getPuncturingArmorBypass(ItemStack weapon) {
        int puncturingLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PUNCTURING.get(), weapon);
        if (puncturingLevel > 0) {
            return 4.0f * puncturingLevel;
        }
        return 0.0f;
    }

    public static float getPuncturingDamageReduction(ItemStack weapon, LivingEntity target, float damage) {
        int puncturingLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PUNCTURING.get(), weapon);
        if (puncturingLevel > 0) {
            if (target == null || target.getArmorValue() < 10) {
                float reductionPercent = 0.05f * puncturingLevel;
                return damage * (1.0f - reductionPercent);
            }
        }
        return damage;
    }
    public static float getPuncturingDamageReductionForTooltip(ItemStack weapon, float damage) {
        int puncturingLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PUNCTURING.get(), weapon);
        if (puncturingLevel > 0) {
            float reductionPercent = 0.05f * puncturingLevel;
            return damage * (1.0f - reductionPercent);
        }
        return damage;
    }
    public static float getWaterProofDamage(ItemStack weapon, Player player, float damage) {
        int waterProofLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WATER_PROOF.get(), weapon);
        if (waterProofLevel > 0 && player != null && player.isUnderWater()) {
            return damage * 1.15f;
        }
        return damage;
    }
    private static final Map<MobEffect, Integer> ELEMENTAL_EFFECTS = new HashMap<>();

    static {
        ELEMENTAL_EFFECTS.put(MobEffects.MOVEMENT_SPEED, 5);
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
        ELEMENTAL_EFFECTS.put(MobEffects.BLINDNESS, 2);
        ELEMENTAL_EFFECTS.put(MobEffects.HUNGER, 4);
        ELEMENTAL_EFFECTS.put(MobEffects.DIG_SLOWDOWN, 4);
        ELEMENTAL_EFFECTS.put(MobEffects.CONFUSION, 2);
        ELEMENTAL_EFFECTS.put(MobEffects.WATER_BREATHING, 4);
        ELEMENTAL_EFFECTS.put(MobEffects.NIGHT_VISION, 3);
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
                    int amplifier = getRandomEffectAmplifier(enchantmentLevel, random);

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

    private static int getRandomEffectAmplifier(int enchantmentLevel, Random random) {
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

    public static int getQuickHands(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.QUICK_HANDS.get(), stack);
    }

    public static int getLightweight(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTWEIGHT.get(), stack);
    }
}