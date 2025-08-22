package top.ribs.scguns.entity.throwable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.SulfurGasCloud;
import top.ribs.scguns.event.GasExplosion;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModTags;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ThrowableGasGrenadeEntity extends ThrowableGrenadeEntity {
    private final float explosionRadius;
    private int remainingTicks;
    private final int delayTicks;

    public ThrowableGasGrenadeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.explosionRadius = 6.0f;
        this.remainingTicks = 800;
        this.delayTicks = 30;
    }

    public ThrowableGasGrenadeEntity(Level world, LivingEntity entity, int timeLeft, float radius) {
        super(ModEntities.THROWABLE_GAS_GRENADE.get(), world, entity);
        this.setShouldBounce(true);
        this.setItem(new ItemStack(ModItems.GAS_GRENADE.get()));
        this.setMaxLife(20 * 3);
        this.explosionRadius = radius;
        this.remainingTicks = 800;
        this.delayTicks = 20;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.remainingTicks > 0) {
            if (this.remainingTicks <= (800 - this.delayTicks)) {
                emitGasCloudParticles();
                applyGasEffects();
            }

            this.remainingTicks--;
        } else {
            this.remove(RemovalReason.KILLED);
        }
    }
    private void emitGasCloudParticles() {
        Vec3 center = this.position();
        float intensity = 0.8f;

        if (!this.level().isClientSide) {
            SulfurGasCloud.spawnEnhancedGasCloud(this.level(), center, this.explosionRadius, intensity, this.random);
        }
    }
    private void applyGasEffects() {
        if (!this.level().isClientSide) {
            Vec3 center = this.position();
            SulfurGasCloud.applyGasEffects(this.level(), center, this.explosionRadius, 500, 2);
        }
    }

    @Override
    public void onDeath() {
        double y = this.getY() + this.getType().getDimensions().height * 0.5;
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.CAT_HISS, SoundSource.BLOCKS, 2, 1);
    }
}
