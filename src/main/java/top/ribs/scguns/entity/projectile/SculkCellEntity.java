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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBlood;
import top.ribs.scguns.network.message.S2CMessageProjectileHitBlock;
import top.ribs.scguns.network.message.S2CMessageProjectileHitEntity;
import top.ribs.scguns.util.GunEnchantmentHelper;

public class SculkCellEntity extends ProjectileEntity {
    private static final float SHIELD_DISABLE_CHANCE = 0.30f;
    private static final float SHIELD_DAMAGE_PENETRATION = 0.70f;
    private static final float HEADSHOT_EFFECT_DURATION_MULTIPLIER = 1.5f;
    private static final float CRITICAL_EFFECT_MULTIPLIER = 1.25f;

    public SculkCellEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public SculkCellEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.setArmorBypassAmount(9.0F);
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide && this.tickCount < this.life) {
            if (this.tickCount % 2 == 0) {
                this.level().addParticle(ModParticleTypes.SONIC_BLAST.get(), true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }

            if (this.tickCount > 1 && this.tickCount % 4 == 0) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                this.level().addParticle(ParticleTypes.SCULK_SOUL, true, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        float damage = this.getDamage();
        float newDamage = this.getCriticalDamage(this.getWeapon(), this.random, damage);
        boolean critical = damage != newDamage;
        damage = newDamage;
        damage *= advantageMultiplier(entity);

        if (headshot) {
            damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
        }
        if (entity instanceof LivingEntity livingTarget) {
            damage = applyProjectileProtection(livingTarget, damage);
            damage = calculateArmorBypassDamage(livingTarget, damage);
        }
        if (entity instanceof LivingEntity livingTarget) {
            damage = calculateArmorBypassDamage(livingTarget, damage);
        }

        DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner());
        boolean blocked = ProjectileHelper.handleShieldHit(entity, this, damage, SHIELD_DISABLE_CHANCE);

        if (blocked) {
            float penetratingDamage = damage * SHIELD_DAMAGE_PENETRATION;
            entity.hurt(source, penetratingDamage);
            if (entity instanceof LivingEntity livingEntity) {
                applyEffect(livingEntity, SHIELD_DAMAGE_PENETRATION, headshot, critical);
            }
        } else {
            entity.hurt(source, damage);
            if (entity instanceof LivingEntity livingEntity) {
                applyEffect(livingEntity, 1.0f, headshot, critical);
            }
        }

        if(entity instanceof LivingEntity) {
            GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), (LivingEntity) entity);
        }

        if (this.shooter instanceof Player) {
            int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter, new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }
        PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z, entity.getType()));
    }

    private void applyEffect(LivingEntity target, float powerMultiplier, boolean headshot, boolean critical) {
        ResourceLocation effectLocation = this.getProjectile().getImpactEffect();
        if (effectLocation != null) {
            float effectChance = this.getProjectile().getImpactEffectChance() * powerMultiplier;
            if (headshot) {
                effectChance = Math.min(1.0f, effectChance * 1.25f);
            }
            if (critical) {
                effectChance = Math.min(1.0f, effectChance * 1.25f);
            }

            if (this.random.nextFloat() < effectChance) {
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                if (effect != null) {
                    int duration = this.getProjectile().getImpactEffectDuration();
                    if (headshot) {
                        duration = (int)(duration * HEADSHOT_EFFECT_DURATION_MULTIPLIER);
                    }
                    if (critical) {
                        duration = (int)(duration * CRITICAL_EFFECT_MULTIPLIER);
                    }
                    duration = (int)(duration * powerMultiplier);

                    int amplifier = this.getProjectile().getImpactEffectAmplifier();
                    if (critical) {
                        amplifier += 1;
                    }

                    target.addEffect(new MobEffectInstance(
                            effect,
                            duration,
                            amplifier
                    ));

                    if (!this.level().isClientSide) {
                        ServerLevel serverLevel = (ServerLevel) this.level();
                        for (int i = 0; i < 3; i++) {
                            serverLevel.sendParticles(
                                    ParticleTypes.SCULK_SOUL,
                                    target.getX() + (random.nextDouble() - 0.5),
                                    target.getY() + target.getBbHeight() * 0.5,
                                    target.getZ() + (random.nextDouble() - 0.5),
                                    1, 0, 0.05, 0, 0.1
                            );
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        PacketHandler.getPlayChannel().sendToTrackingChunk(() -> this.level().getChunkAt(pos), new S2CMessageProjectileHitBlock(x, y, z, pos, face));

        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            for (int i = 0; i < 5; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.3;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.3;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.3;
                serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, x + offsetX, y + offsetY, z + offsetZ, 1, 0, 0, 0, 0.05);
            }

            for (int i = 0; i < 2; i++) {
                serverLevel.sendParticles(ModParticleTypes.SONIC_BLAST.get(), x, y, z, 1, 0, 0, 0, 0);
            }
        }

        super.onHitBlock(state, pos, face, x, y, z);
    }

    @Override
    public void onExpired() {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            for (int i = 0; i < 8; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 1, 0, 0, 0, 0.02);
            }
        }
    }
}