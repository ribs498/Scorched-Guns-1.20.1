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
import net.minecraft.world.effect.MobEffects;
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
import top.ribs.scguns.effect.PlasmaExplosion;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBlood;
import top.ribs.scguns.network.message.S2CMessageProjectileHitEntity;
import top.ribs.scguns.util.GunEnchantmentHelper;

public class RamrodProjectileEntity extends ProjectileEntity {
    private static final float SHIELD_DISABLE_CHANCE = 0.40f;
    private static final float SHIELD_DAMAGE_PENETRATION = 0.35f;
    private static final float HEADSHOT_EFFECT_DURATION_MULTIPLIER = 1.5f;

    // New weakness effect constants
    private static final float WEAKNESS_CHANCE = 0.75f;
    private static final int WEAKNESS_DURATION_TICKS = 100;
    private static final int WEAKNESS_AMPLIFIER = 0;

    public RamrodProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public RamrodProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide && (this.tickCount > 1 && this.tickCount < this.life)) {
            if (this.tickCount % 2 == 0) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                double velocityX = (this.random.nextDouble() - 0.5) * 0.1;
                double velocityY = (this.random.nextDouble() - 0.5) * 0.1;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.1;
                this.level().addParticle(ParticleTypes.ENCHANTED_HIT, true,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        velocityX,
                        velocityY,
                        velocityZ);
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
                applyEffect(livingEntity, SHIELD_DAMAGE_PENETRATION, headshot);
                applyWeaknessEffect(livingEntity, SHIELD_DAMAGE_PENETRATION, headshot);
            }
        } else {
            entity.hurt(source, damage);
            if (entity instanceof LivingEntity livingEntity) {
                applyEffect(livingEntity, 1.0f, headshot);
                applyWeaknessEffect(livingEntity, 1.0f, headshot);
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
        spawnExplosionParticles(hitVec);
    }

    private void applyWeaknessEffect(LivingEntity target, float powerMultiplier, boolean headshot) {

        float effectiveChance = WEAKNESS_CHANCE * powerMultiplier;

        if (headshot) {
            effectiveChance = Math.min(1.0f, effectiveChance * 1.1f);
        }

        if (this.random.nextFloat() < effectiveChance) {
            int duration = WEAKNESS_DURATION_TICKS;

            if (headshot) {
                duration = (int)(duration * HEADSHOT_EFFECT_DURATION_MULTIPLIER);
            }

            duration = (int)(duration * powerMultiplier);

            target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    duration,
                    WEAKNESS_AMPLIFIER
            ));
        }
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
        spawnExplosionParticles(new Vec3(x, y + 0.1, z));
    }

    @Override
    public void onExpired() {
        spawnExplosionParticles(new Vec3(this.getX(), this.getY() + 0.1, this.getZ()));
    }

    private void spawnExplosionParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            int particleCount = 5;
            serverLevel.sendParticles(ModParticleTypes.RAMROD_IMPACT.get(), position.x, position.y, position.z, particleCount, 0, 0, 0, 0.1);
            for (int i = 0; i < particleCount; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.2;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.2;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.2;
                double speedX = (this.random.nextDouble() - 0.5) * 0.5;
                double speedY = (this.random.nextDouble() - 0.5) * 0.5;
                double speedZ = (this.random.nextDouble() - 0.5) * 0.5;
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, position.x + offsetX, position.y + offsetY, position.z + offsetZ, 1, speedX, speedY, speedZ, 0.1);
            }
        }
    }
}