package top.ribs.scguns.entity.projectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageProjectileHitEntity;
import top.ribs.scguns.util.GunEnchantmentHelper;

import java.util.List;

public class PlasmaProjectileEntity extends ProjectileEntity {

    private static final float SHIELD_DISABLE_CHANCE = 0.70f;
    private static final float SHIELD_DAMAGE_PENETRATION = 0.25f;
    private static final float HEADSHOT_EFFECT_DURATION_MULTIPLIER = 1.5f;

    private static final float SPLASH_DAMAGE_RADIUS = 2.0f;
    private static final float SPLASH_DAMAGE_FALLOFF = 0.7f;
    private static final float SPLASH_EFFECT_CHANCE_MULTIPLIER = 0.4f;
    private static final float FIRE_CHANCE = 0.30f;

    public PlasmaProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public PlasmaProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide && (this.tickCount > 1 && this.tickCount < this.life)) {
            if (this.tickCount % 2 == 0) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                this.level().addParticle(ModParticleTypes.GREEN_FLAME.get(), true, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
            }
            if (this.tickCount % 5 == 0) {
                this.level().addParticle(ModParticleTypes.PLASMA_RING.get(), true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        float directDamage = this.getDamage();
        float newDamage = this.getCriticalDamage(this.getWeapon(), this.random, directDamage);
        boolean critical = directDamage != newDamage;
        directDamage = newDamage;
        directDamage *= advantageMultiplier(entity);
        boolean wasAlive = entity instanceof LivingEntity && entity.isAlive();

        if (headshot) {
            directDamage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
        }
        if (entity instanceof LivingEntity livingTarget) {
            directDamage = applyProjectileProtection(livingTarget, directDamage);
        }

        DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner());
        boolean blocked = ProjectileHelper.handleShieldHit(entity, this, directDamage, SHIELD_DISABLE_CHANCE);

        if (blocked) {
            float penetratingDamage = directDamage * SHIELD_DAMAGE_PENETRATION;
            entity.hurt(source, penetratingDamage);
            if (entity instanceof LivingEntity livingEntity) {
                applyEffect(livingEntity, SHIELD_DAMAGE_PENETRATION, headshot);
            }
        } else {
            entity.hurt(source, directDamage);
            if (entity instanceof LivingEntity livingEntity) {
                applyEffect(livingEntity, 1.0f, headshot);
            }
        }
        if (entity instanceof LivingEntity && this.random.nextFloat() < FIRE_CHANCE) {
            entity.setSecondsOnFire(2);
        }
        if(entity instanceof LivingEntity) {
            GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), (LivingEntity) entity);
        }
        if (this.shooter instanceof Player) {
            int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter, new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }
        applySplashDamage(hitVec, directDamage * 0.6f);
        spawnPlasmaParticles(hitVec);

        if (wasAlive && entity instanceof LivingEntity livingEntity && !livingEntity.isAlive()) {
            checkForDiamondSteelBonus(livingEntity, hitVec);
        }
    }

    private void applySplashDamage(Vec3 center, float baseSplashDamage) {
        if (this.level().isClientSide()) {
            return;
        }

        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center.x - SPLASH_DAMAGE_RADIUS,
                        center.y - SPLASH_DAMAGE_RADIUS,
                        center.z - SPLASH_DAMAGE_RADIUS,
                        center.x + SPLASH_DAMAGE_RADIUS,
                        center.y + SPLASH_DAMAGE_RADIUS,
                        center.z + SPLASH_DAMAGE_RADIUS)
        );

        DamageSource splashSource = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getShooter());

        for (LivingEntity target : nearbyEntities) {
            if (target == this.getShooter()) {
                continue;
            }
            double distance = target.position().distanceTo(center);
            if (distance > SPLASH_DAMAGE_RADIUS) {
                continue;
            }
            float distanceRatio = (float) (distance / SPLASH_DAMAGE_RADIUS);
            float damageMultiplier = 1.0f - (distanceRatio * (1.0f - SPLASH_DAMAGE_FALLOFF));
            float splashDamage = baseSplashDamage * damageMultiplier;
            splashDamage = applyProjectileProtection(target, splashDamage);
            if (splashDamage > 0.5f) {
                target.hurt(splashSource, splashDamage);
                applyEffect(target, damageMultiplier * SPLASH_EFFECT_CHANCE_MULTIPLIER, false);
                float splashFireChance = FIRE_CHANCE * damageMultiplier * 0.5f;
                if (this.random.nextFloat() < splashFireChance) {
                    target.setSecondsOnFire(2);
                }
            }
        }
    }

    public float applyProjectileProtection(LivingEntity target, float damage) {
        int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROJECTILE_PROTECTION, target);

        if (protectionLevel > 0) {
            float reduction = protectionLevel * 0.07f;
            reduction = Math.min(reduction, 0.56f);
            damage *= (1.0f - reduction);
        }

        return damage;
    }
    private void applyEffect(LivingEntity target, float powerMultiplier, boolean headshot) {
        ResourceLocation effectLocation = this.getProjectile().getImpactEffect();
        if (effectLocation != null) {
            float effectChance = this.getProjectile().getImpactEffectChance() * powerMultiplier;
            if (headshot) {
                effectChance = Math.min(1.0f, effectChance * 1.25f);
            }
            if (this.random.nextFloat() < effectChance) {
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                if (effect != null) {
                    int duration = this.getProjectile().getImpactEffectDuration();
                    if (headshot) {
                        duration = (int)(duration * HEADSHOT_EFFECT_DURATION_MULTIPLIER);
                    }
                    duration = (int)(duration * powerMultiplier);

                    target.addEffect(new MobEffectInstance(
                            effect,
                            duration,
                            this.getProjectile().getImpactEffectAmplifier()
                    ));
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        Vec3 hitPos = new Vec3(x, y, z);
        applySplashDamage(hitPos, this.getDamage() * 0.4f);
        spawnPlasmaParticles(hitPos);
    }

    @Override
    public void onExpired() {
        Vec3 pos = this.position();
        applySplashDamage(pos, this.getDamage() * 0.3f);
        spawnPlasmaParticles(pos);
    }

    private void spawnPlasmaParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            serverLevel.sendParticles(ModParticleTypes.PLASMA_EXPLOSION.get(),
                    position.x, position.y, position.z, 1, 0, 0, 0, 0.1);
            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0) * 2 * Math.PI;
                double radius = 0.5 + this.random.nextDouble() * 1.5;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.5;

                double speedX = offsetX * 0.1;
                double speedY = (this.random.nextDouble() - 0.3) * 0.2;
                double speedZ = offsetZ * 0.1;

                serverLevel.sendParticles(ModParticleTypes.GREEN_FLAME.get(),
                        position.x + offsetX, position.y + offsetY, position.z + offsetZ,
                        1, speedX, speedY, speedZ, 0.05);
            }
            for (int i = 0; i < 11; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 3.0;
                double offsetY = (this.random.nextDouble() - 0.5);
                double offsetZ = (this.random.nextDouble() - 0.5) * 3.0;
                double speedX = (this.random.nextDouble() - 0.5) * 0.3;
                double speedY = (this.random.nextDouble() - 0.5) * 0.3;
                double speedZ = (this.random.nextDouble() - 0.5) * 0.3;

                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        position.x + offsetX, position.y + offsetY, position.z + offsetZ,
                        1, speedX, speedY, speedZ, 0.1);
            }
        }
    }
}