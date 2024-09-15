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
import top.ribs.scguns.event.GasExplosion;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.AnthraliteRespiratorItem;
import top.ribs.scguns.item.NetheriteRespiratorItem;

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
    protected void defineSynchedData() {
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
        if (this.level().isClientSide) {
            for (int i = 0; i < 15; i++) {
                double xOffset = random.nextGaussian() * explosionRadius;
                double yOffset = random.nextGaussian() * (explosionRadius / 2);
                double zOffset = random.nextGaussian() * explosionRadius;
                double x = this.getX() + xOffset;
                double y = this.getY() + yOffset;
                double z = this.getZ() + zOffset;
                this.level().addParticle(ModParticleTypes.SULFUR_DUST.get(), x, y, z, 0, 0, 0);
                this.level().addParticle(ModParticleTypes.SULFUR_SMOKE.get(), x, y, z, 0, 0, 0);
            }
        }
    }

    private void applyGasEffects() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, new AABB(this.blockPosition()).inflate(this.explosionRadius));
        for (LivingEntity entity : entities) {
            if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) continue;

            ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.is(ModTags.Items.GAS_MASK)) {
                int unbreakingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, helmet);
                if (entity.getPersistentData().getLong("LastHelmetDamageTick") + 50 <= entity.tickCount) {
                    entity.getPersistentData().putLong("LastHelmetDamageTick", entity.tickCount);

                    if (shouldDamageItem(unbreakingLevel, entity.getRandom())) {
                        helmet.hurtAndBreak(1, entity, (e) -> e.broadcastBreakEvent(EquipmentSlot.HEAD));
                    }
                }

                continue;
            }
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        }
    }

    private boolean shouldDamageItem(int unbreakingLevel, RandomSource random) {
        if (unbreakingLevel > 0) {
            int chance = 1 + unbreakingLevel;
            return random.nextInt(chance) == 0;
        }
        return true;
    }


    @Override
    public void onDeath() {
        double y = this.getY() + this.getType().getDimensions().height * 0.5;
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.CAT_HISS, SoundSource.BLOCKS, 2, 1);
    }
}
