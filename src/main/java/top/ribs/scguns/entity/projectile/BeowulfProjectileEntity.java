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
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBlood;
import top.ribs.scguns.network.message.S2CMessageProjectileHitEntity;
import top.ribs.scguns.util.GunEnchantmentHelper;

public class BeowulfProjectileEntity extends ProjectileEntity {
    private static final float SHIELD_DISABLE_CHANCE = 0.50f;
    private static final float SHIELD_DAMAGE_PENETRATION = 0.3f;

    // Beowulf Round bonuses - high tier expensive ammo
    private static final float BEOWULF_XP_MULTIPLIER = 0.75f;
    private static final int BEOWULF_LOOTING_LEVEL = 2;

    private static boolean eventRegistered = false;

    public BeowulfProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        registerLootingEventHandler();
    }

    public BeowulfProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        float armorBypass = 3.0F;
        float puncturingBypass = GunEnchantmentHelper.getPuncturingArmorBypass(weapon);
        armorBypass += puncturingBypass;
        this.setArmorBypassAmount(armorBypass);
        registerLootingEventHandler();
    }

    private static synchronized void registerLootingEventHandler() {
        if (!eventRegistered) {
            MinecraftForge.EVENT_BUS.register(BeowulfProjectileEntity.class);
            eventRegistered = true;
        }
    }

    @SubscribeEvent
    public static void onLootingLevel(LootingLevelEvent event) {
        // Check if the damage source is from a Beowulf Round
        if (event.getDamageSource() != null &&
                event.getDamageSource().getDirectEntity() instanceof BeowulfProjectileEntity) {
            // Add our Beowulf Round looting bonus
            event.setLootingLevel(event.getLootingLevel() + BEOWULF_LOOTING_LEVEL);
        }
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
                this.level().addParticle(ParticleTypes.SMALL_FLAME, true,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        velocityX,
                        velocityY,
                        velocityZ);
            }
        }
        if (this.level().isClientSide && (this.tickCount > 1 && this.tickCount < this.life)) {
            if (this.tickCount % 5 == 0) {
                this.level().addParticle(ModParticleTypes.BEOWULF_IMPACT.get(), true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        float damage = this.getDamage();
        float newDamage = this.getCriticalDamage(this.getWeapon(), this.random, damage);
        boolean critical = damage != newDamage;
        damage = newDamage;
        ResourceLocation advantage = this.getProjectile().getAdvantage();
        damage *= advantageMultiplier(entity);
        boolean wasAlive = entity instanceof LivingEntity && entity.isAlive();
        if (headshot) {
            damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
        }
        if (entity instanceof LivingEntity livingTarget) {
            damage = GunEnchantmentHelper.getPuncturingDamageReduction(this.getWeapon(), livingTarget, damage);
            damage = applyProjectileProtection(livingTarget, damage);
            damage = calculateArmorBypassDamage(livingTarget, damage);
        }
        DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner());
        boolean blocked = ProjectileHelper.handleShieldHit(entity, this, damage, SHIELD_DISABLE_CHANCE);

        if (blocked) {
            float penetratingDamage = damage * SHIELD_DAMAGE_PENETRATION;
            entity.hurt(source, penetratingDamage);
            if (entity instanceof LivingEntity livingEntity) {
                ResourceLocation effectLocation = this.getProjectile().getImpactEffect();
                if (effectLocation != null) {
                    float effectChance = this.getProjectile().getImpactEffectChance() * SHIELD_DAMAGE_PENETRATION;
                    if (this.random.nextFloat() < effectChance) {
                        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                        if (effect != null) {
                            int reducedDuration = (int)(this.getProjectile().getImpactEffectDuration() * SHIELD_DAMAGE_PENETRATION);
                            livingEntity.addEffect(new MobEffectInstance(
                                    effect,
                                    reducedDuration,
                                    this.getProjectile().getImpactEffectAmplifier()
                            ));
                        }
                    }
                }
            }
        } else {
            if (!(entity.getType().is(ModTags.Entities.GHOST) && !advantage.equals(ModTags.Entities.UNDEAD.location()))) {
                entity.hurt(source, damage);
                if (entity instanceof LivingEntity livingEntity) {
                    ResourceLocation effectLocation = this.getProjectile().getImpactEffect();
                    if (effectLocation != null) {
                        float effectChance = this.getProjectile().getImpactEffectChance();
                        if (headshot) {
                            effectChance = Math.min(1.0f, effectChance * 1.25f);
                        }

                        if (this.random.nextFloat() < effectChance) {
                            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                            if (effect != null) {
                                int duration = this.getProjectile().getImpactEffectDuration();
                                if (headshot) {
                                    duration = (int)(duration * 1.25f);
                                }

                                livingEntity.addEffect(new MobEffectInstance(
                                        effect,
                                        duration,
                                        this.getProjectile().getImpactEffectAmplifier()
                                ));
                            }
                        }
                    }
                }
            }
        }
        if(entity instanceof LivingEntity) {
            GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), (LivingEntity) entity);
        }

        if (this.shooter instanceof Player) {
            int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter, new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }
        if (wasAlive && entity instanceof LivingEntity livingEntity && !livingEntity.isAlive()) {
            checkForDiamondSteelBonus(livingEntity, hitVec);
            checkForBeowulfXPBonus(livingEntity, hitVec);
        }
        PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z, entity.getType()));
        spawnExplosionParticles(hitVec);
    }

    private void checkForBeowulfXPBonus(LivingEntity killedEntity, Vec3 position) {
        if (!this.level().isClientSide && this.getShooter() instanceof Player) {
            int baseXP = killedEntity.getExperienceReward();
            int beowulfXP = Math.round(baseXP * BEOWULF_XP_MULTIPLIER);

            if (beowulfXP > 0) {
                ExperienceOrb xpOrb = new ExperienceOrb(this.level(), position.x, position.y, position.z, beowulfXP);
                this.level().addFreshEntity(xpOrb);
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
            serverLevel.sendParticles(ModParticleTypes.BEOWULF_IMPACT.get(), position.x, position.y, position.z, particleCount, 0, 0, 0, 0.1);
            for (int i = 0; i < particleCount; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.2;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.2;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.2;
                double speedX = (this.random.nextDouble() - 0.5) * 0.5;
                double speedY = (this.random.nextDouble() - 0.5) * 0.5;
                double speedZ = (this.random.nextDouble() - 0.5) * 0.5;
                serverLevel.sendParticles(ParticleTypes.FLAME, position.x + offsetX, position.y + offsetY, position.z + offsetZ, 1, speedX, speedY, speedZ, 0.1);
            }
        }
    }
}