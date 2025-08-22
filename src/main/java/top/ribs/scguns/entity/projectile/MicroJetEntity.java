package top.ribs.scguns.entity.projectile;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import top.ribs.scguns.effect.CustomExplosion;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.util.GunEnchantmentHelper;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class MicroJetEntity extends ProjectileEntity {
    public static final float EXPLOSION_DAMAGE_MULTIPLIER = 2.0F;
    private static final float SHIELD_DISABLE_CHANCE = 0.75f;
    private static final float SHIELD_DAMAGE_PENETRATION = 0.2f;
    private static final float HEADSHOT_EFFECT_DURATION_MULTIPLIER = 1.5f;
    private static final float AREA_EFFECT_DURATION_MULTIPLIER = 0.75f;

    public MicroJetEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public MicroJetEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }
    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide) {
            for (int i = 2; i > 0; i--) {
                this.level().addParticle(ModParticleTypes.ROCKET_TRAIL.get(), true,
                        this.getX() - (this.getDeltaMovement().x() / i),
                        this.getY() - (this.getDeltaMovement().y() / i),
                        this.getZ() - (this.getDeltaMovement().z() / i),
                        0, 0, 0);
            }
            if (this.level().random.nextInt(4) == 0) {
                this.level().addParticle(ParticleTypes.SMALL_FLAME, true,
                        this.getX(), this.getY(), this.getZ(),
                        0, 0, 0);
                this.level().addParticle(ParticleTypes.SMOKE, true,
                        this.getX(), this.getY(), this.getZ(),
                        0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        float damage = this.getDamage();
        if (headshot) {
            damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
        }
        if (entity instanceof LivingEntity livingTarget) {
            damage = applyBlastProtection(livingTarget, damage);
        }
        boolean wasAlive = entity instanceof LivingEntity && entity.isAlive();
        DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner());
        boolean blocked = ProjectileHelper.handleShieldHit(entity, this, damage, SHIELD_DISABLE_CHANCE);

        if (blocked) {
            float penetratingDamage = damage * SHIELD_DAMAGE_PENETRATION;
            entity.hurt(source, penetratingDamage);
            if (entity instanceof LivingEntity livingEntity) {
                applyEffect(livingEntity, SHIELD_DAMAGE_PENETRATION, headshot);
            }
        } else {
            entity.hurt(source, damage);
            if (entity instanceof LivingEntity livingEntity) {
                applyEffect(livingEntity, 1.0f, headshot);
            }
        }

        if(entity instanceof LivingEntity) {
            GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), (LivingEntity) entity);
        }
        applyAreaEffects(hitVec);
        if (wasAlive && entity instanceof LivingEntity livingEntity && !livingEntity.isAlive()) {
            checkForDiamondSteelBonus(livingEntity, hitVec);
        }
        createMiniExplosion(this, 1.0f);
    }
    private float applyBlastProtection(LivingEntity target, float damage) {
        int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, target);

        if (protectionLevel > 0) {
            float reduction = protectionLevel * 0.08f;
            reduction = Math.min(reduction, 0.8f);
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

    private void applyAreaEffects(Vec3 center) {
        if (!this.level().isClientSide()) {
            ResourceLocation effectLocation = this.getProjectile().getImpactEffect();
            if (effectLocation != null) {
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                if (effect != null) {
                    List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
                            LivingEntity.class,
                            new AABB(center.x - 1.0, center.y - 1.0, center.z - 1.0,
                                    center.x + 1.0, center.y + 1.0, center.z + 1.0)
                    );

                    float areaEffectChance = this.getProjectile().getImpactEffectChance() * 0.4f; // 60% reduced chance in area

                    for (LivingEntity entity : nearbyEntities) {
                        if (entity != this.getShooter()) {
                            applyEffect(entity, AREA_EFFECT_DURATION_MULTIPLIER * 0.4f, false);

                            double distance = entity.position().distanceTo(center);
                            float distanceMultiplier = (float)(1.0 - (distance));
                            float finalChance = areaEffectChance * Math.max(0, distanceMultiplier);

                            if (this.random.nextFloat() < finalChance) {
                                applyEffect(entity, AREA_EFFECT_DURATION_MULTIPLIER, false);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        Vec3 hitPos = new Vec3(x, y, z);
        applyAreaEffects(hitPos);
        createMiniExplosion(this, 1.0f);
    }

    @Override
    public void onExpired() {
        Vec3 pos = this.position();
        applyAreaEffects(pos);
        createMiniExplosion(this, 1.0f);
    }
    public static void createMiniExplosion(Entity entity, float radius) {
        Level world = entity.level();
        if (world.isClientSide)
            return;
        CustomExplosion explosion = new CustomExplosion(world, entity, entity.getX(), entity.getY(), entity.getZ(), radius, false, CustomExplosion.CustomBlockInteraction.NONE) {
        };
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion))
            return;
        explosion.explode();
        explosion.finalizeExplosion(true);

        for (ServerPlayer player : ((ServerLevel) world).players()) {
            if (player.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) < 4096) {
                player.connection.send(new ClientboundExplodePacket(entity.getX(), entity.getY(), entity.getZ(), radius, explosion.getToBlow(), explosion.getHitPlayers().get(player)));
            }
        }
    }
}
