package top.ribs.scguns.event;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import top.ribs.scguns.init.ModParticleTypes;

import java.util.List;

public class GasExplosion {
    private final Level level;
    private final BlockPos centerPos;
    private final float radius;
    private final int duration;
    private final RandomSource random;

    public GasExplosion(Level level, BlockPos centerPos, float radius, int duration) {
        this.level = level;
        this.centerPos = centerPos;
        this.radius = radius;
        this.duration = duration;
        this.random = level.random;
    }

    public void explode() {
        // Trigger the explosion sound and particle effects
        level.playSound(null, centerPos.getX(), centerPos.getY(), centerPos.getZ(), SoundEvents.CAT_HISS, SoundSource.BLOCKS, 2.0F, 1.0F);

        // Apply gas effects
        for (int i = 0; i < duration; i++) {
            level.scheduleTick(centerPos, level.getBlockState(centerPos).getBlock(), i * 20);
        }
    }

    public void tick(int tickCount) {
        if (tickCount >= duration) return;
        spawnGasCloudParticles();
        applyEffectsToEntities();
    }

    private void spawnGasCloudParticles() {
        for (int i = 0; i < 20; i++) {
            double xOffset = random.nextGaussian() * radius;
            double yOffset = random.nextGaussian() * (radius / 2);
            double zOffset = random.nextGaussian() * radius;
            double x = centerPos.getX() + 0.5 + xOffset;
            double y = centerPos.getY() + 0.5 + yOffset;
            double z = centerPos.getZ() + 0.5 + zOffset;
            level.addParticle(ModParticleTypes.SULFUR_DUST.get(), x, y, z, 0, 0, 0);
        }
    }

    private void applyEffectsToEntities() {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(centerPos).inflate(radius));
        for (LivingEntity entity : entities) {
            if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) continue;

            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        }
    }
}

