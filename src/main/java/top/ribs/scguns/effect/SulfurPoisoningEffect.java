package top.ribs.scguns.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.RandomSource;
import top.ribs.scguns.init.ModEffects;

public class SulfurPoisoningEffect extends MobEffect {

    public SulfurPoisoningEffect(MobEffectCategory typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn);

        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.JUMP_STRENGTH, "9107DE5E-7CE8-4030-940E-514C1F160892", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, "A107DE5E-7CE8-4030-940E-514C1F160893", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        MobEffectInstance effect = entity.getEffect(ModEffects.SULFUR_POISONING.get());
        if (effect == null) return;

        int duration = effect.getDuration();
        int originalDuration = getOriginalDuration(effect);
        float intensityRatio = calculateIntensityRatio(duration, originalDuration);

        int damageInterval = Math.max(20, (int)(40 - (amplifier * 10 * intensityRatio)));
        if (entity.tickCount % damageInterval == 0) {
            float damage = (1.0f + amplifier * 0.5f) * intensityRatio;
            if (damage > 0.2f) {
                entity.hurt(entity.damageSources().magic(), damage);
            }
        }
        if (entity instanceof Player player) {
            handlePlayerEffects(player, amplifier, intensityRatio, entity.getRandom());
        }
        if (intensityRatio > 0.3f) {
            applyIntensePhaseEffects(entity, amplifier, intensityRatio, entity.getRandom());
        }

        super.applyEffectTick(entity, amplifier);
    }

    private void handlePlayerEffects(Player player, int amplifier, float intensityRatio, RandomSource random) {
        if (intensityRatio > 0.5f) {
            if (player.tickCount % (int)(60 / intensityRatio) == 0) {
                player.getFoodData().addExhaustion(0.15f + (amplifier * 0.1f) * intensityRatio);
            }
        } else {
            if (player.tickCount % 100 == 0) {
                player.getFoodData().addExhaustion(0.05f + (amplifier * 0.03f) * intensityRatio);
            }
        }
    }

    private void applyIntensePhaseEffects(LivingEntity entity, int amplifier, float intensityRatio, RandomSource random) {
        if (intensityRatio < 0.7f) return;
        if (entity.tickCount % 200 == 0) {
            if (random.nextFloat() < 0.3f * intensityRatio) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, (int)(60 * intensityRatio), 0));
            }
        }
        if (intensityRatio > 0.8f && entity.tickCount % 300 == 0) {
            if (random.nextFloat() < 0.2f) {
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (int)(40 * intensityRatio), 0));
            }
        }
        if (intensityRatio > 0.9f && amplifier >= 2) {
            if (entity.tickCount % 400 == 0 && random.nextFloat() < 0.1f) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            }
        }
    }

    private float calculateIntensityRatio(int remainingDuration, int originalDuration) {
        if (originalDuration <= 0) return 1.0f;

        float ratio = (float) remainingDuration / originalDuration;
        if (ratio > 0.7f) {
            return 1.0f;
        } else if (ratio > 0.3f) {
            return 0.4f + (ratio - 0.3f) * 1.5f;
        } else {
            return Math.max(0.1f, ratio * 1.33f);
        }
    }

    private int getOriginalDuration(MobEffectInstance effect) {
        int currentDuration = effect.getDuration();
        if (currentDuration > 1000) return Math.max(1200, currentDuration + 200);
        if (currentDuration > 500) return Math.max(800, currentDuration + 150);
        return Math.max(400, currentDuration + 100);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    public static boolean hasFireVulnerability(LivingEntity entity) {
        return entity.hasEffect(ModEffects.SULFUR_POISONING.get());
    }

    public static float getFireDamageMultiplier(LivingEntity entity) {
        if (!hasFireVulnerability(entity)) {
            return 1.0f;
        }

        MobEffectInstance effect = entity.getEffect(ModEffects.SULFUR_POISONING.get());
        if (effect == null) return 1.0f;

        int amplifier = effect.getAmplifier();
        SulfurPoisoningEffect poisonEffect = (SulfurPoisoningEffect) effect.getEffect();
        float intensityRatio = poisonEffect.calculateIntensityRatio(
                effect.getDuration(),
                poisonEffect.getOriginalDuration(effect)
        );

        float baseMultiplier = 1.3f + (amplifier * 0.3f);
        return 1.0f + (baseMultiplier - 1.0f) * intensityRatio;
    }
}