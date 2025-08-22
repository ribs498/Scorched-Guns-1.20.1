package top.ribs.scguns.effect;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.entity.projectile.RocketEntity;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.world.ProjectileExplosion;

import java.util.*;

public class RocketExplosion extends ProjectileExplosion {
    private final float customDamage;
    private final float explosionRadius;
    private final Level world;
    private final double x, y, z;
    private final ExplosionDamageCalculator context;

    public RocketExplosion(Level level, Entity entity, DamageSource damageSource, ExplosionDamageCalculator explosionDamageCalculator,
                           double x, double y, double z, float radius, float customDamage, boolean fire, BlockInteraction blockInteraction) {
        super(level, entity, damageSource, explosionDamageCalculator, x, y, z, radius, fire, blockInteraction);
        this.customDamage = customDamage;
        this.explosionRadius = radius;
        this.world = level;
        this.x = x;
        this.y = y;
        this.z = z;
        this.context = explosionDamageCalculator != null ? explosionDamageCalculator : new ExplosionDamageCalculator();

    }

    @Override
    public void explode() {
        // Play the explosion sound at the beginning of the explosion
        playExplosionSound();

        Set<BlockPos> set = Sets.newHashSet();
        for(int x = 0; x < 16; x++) {
            for(int y = 0; y < 16; y++) {
                for(int z = 0; z < 16; z++) {
                    if(x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15) {
                        double d0 = (float) x / 15.0F * 2.0F - 1.0F;
                        double d1 = (float) y / 15.0F * 2.0F - 1.0F;
                        double d2 = (float) z / 15.0F * 2.0F - 1.0F;
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = this.explosionRadius * (0.7F + this.world.random.nextFloat() * 0.6F);
                        double blockX = this.x;
                        double blockY = this.y;
                        double blockZ = this.z;

                        for(; f > 0.0F; f -= 0.225F) {
                            BlockPos pos = BlockPos.containing(blockX, blockY, blockZ);
                            BlockState blockState = this.world.getBlockState(pos);
                            FluidState fluidState = this.world.getFluidState(pos);

                            Optional<Float> optional = this.context.getBlockExplosionResistance(this, this.world, pos, blockState, fluidState);
                            if(optional.isPresent()) {
                                f -= (optional.get() + 0.3F) * 0.3F;
                            }

                            if(f > 0.0F && this.context.shouldBlockExplode(this, this.world, pos, blockState, f)) {
                                set.add(pos);
                            }

                            blockX += d0 * 0.3F;
                            blockY += d1 * 0.3F;
                            blockZ += d2 * 0.3F;
                        }
                    }
                }
            }
        }

        this.getToBlow().addAll(set);

        float damageRadius = this.explosionRadius * 2.0F;
        int minX = Mth.floor(this.x - damageRadius - 1.0D);
        int maxX = Mth.floor(this.x + damageRadius + 1.0D);
        int minY = Mth.floor(this.y - damageRadius - 1.0D);
        int maxY = Mth.floor(this.y + damageRadius + 1.0D);
        int minZ = Mth.floor(this.z - damageRadius - 1.0D);
        int maxZ = Mth.floor(this.z + damageRadius + 1.0D);

        List<Entity> entities = this.world.getEntitiesOfClass(Entity.class, new AABB(minX, minY, minZ, maxX, maxY, maxZ));

        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, this, entities, damageRadius);

        Vec3 explosionPos = new Vec3(this.x, this.y, this.z);
        for(Entity entity : entities) {
            if(entity.ignoreExplosion()) {
                continue;
            }

            double distance = Math.sqrt(entity.distanceToSqr(explosionPos));
            double maxDamageDistance = this.explosionRadius;
            double maxDistance = this.explosionRadius * 2.0;

            if(distance >= maxDistance) {
                continue;
            }

            float damage;
            if (distance <= maxDamageDistance) {
                damage = this.customDamage;
            } else {
                float falloffMultiplier = 1.0f - (float)((distance - maxDamageDistance) / (maxDistance - maxDamageDistance));
                damage = this.customDamage * falloffMultiplier;
            }

            if (entity instanceof LivingEntity livingEntity) {
                damage = applyBlastProtection(livingEntity, damage);
            }
            if(damage > 0) {
                entity.hurt(this.getDamageSource(), damage);

                double deltaX = entity.getX() - this.x;
                double deltaY = entity.getEyeY() - this.y;
                double deltaZ = entity.getZ() - this.z;
                double distanceToExplosion = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                if(distanceToExplosion != 0.0D) {
                    deltaX /= distanceToExplosion;
                    deltaY /= distanceToExplosion;
                    deltaZ /= distanceToExplosion;
                } else {
                    deltaX = 0.0;
                    deltaY = 1.0;
                    deltaZ = 0.0;
                }

                double knockbackStrength = Math.max(0, (1.0D - distance / maxDistance) * 0.5);
                entity.setDeltaMovement(entity.getDeltaMovement().add(deltaX * knockbackStrength, deltaY * knockbackStrength, deltaZ * knockbackStrength));

                if(entity instanceof Player player) {
                    if(!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                        this.getHitPlayers().put(player, new Vec3(deltaX * knockbackStrength, deltaY * knockbackStrength, deltaZ * knockbackStrength));
                    }
                }
            }
        }
    }

    /**
     * Plays the explosion sound effect
     */
    private void playExplosionSound() {
        if (!this.world.isClientSide) {
            float volume = Math.min(4.0F, this.explosionRadius * 0.6F);
            float pitch = 0.8F + this.world.random.nextFloat() * 0.4F;

            this.world.playSound(null, this.x, this.y, this.z,
                    SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                    volume, pitch);

//            if (this.explosionRadius >= 3.0F) {
//                this.world.playSound(null, this.x, this.y, this.z,
//                        SoundEvents.WITHER_DEATH, SoundSource.BLOCKS,
//                        volume * 0.5F, 0.5F + this.world.random.nextFloat() * 0.2F);
//            }
        }
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

    @Override
    protected float getEntityDamageAmount(Entity entity, double distance) {
        double maxDistance = this.explosionRadius * 2.0;
        if (distance >= maxDistance) {
            return 0.0f;
        }
        float falloffMultiplier = 1.0f - (float)(distance / maxDistance);
        return this.customDamage * falloffMultiplier;
    }

    @Override
    public void finalizeExplosion(boolean spawnParticles) {
        super.finalizeExplosion(spawnParticles);

        if (spawnParticles && !this.world.isClientSide) {
            spawnCustomRocketParticles();
        }
    }

    private void spawnCustomRocketParticles() {
        ServerLevel serverLevel = (ServerLevel) this.world;
        double sizeMultiplier = this.explosionRadius / 4.0;

        BlockPos explosionPos = BlockPos.containing(this.x, this.y, this.z);
        BlockState blockAtExplosion = this.world.getBlockState(explosionPos);

        double adjustedY;
        if (!blockAtExplosion.isAir()) {
            adjustedY = explosionPos.getY() + 1.0;
        } else {
            adjustedY = this.y + 0.2;
        }

        double renderDistance = 128.0;
        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class,
                new AABB(this.x - renderDistance, this.y - renderDistance, this.z - renderDistance,
                        this.x + renderDistance, this.y + renderDistance, this.z + renderDistance));

        for (ServerPlayer player : nearbyPlayers) {
            serverLevel.sendParticles(player, ModParticleTypes.ROCKET_EXPLOSION.get(),
                    true,
                    this.x, adjustedY, this.z,
                    1,
                    0.1, 0.1, 0.1,
                    sizeMultiplier);
        }

        for (int burstWave = 0; burstWave < 3; burstWave++) {
            int particlesInBurst = 8 + burstWave * 4;
            double burstRadius = this.explosionRadius * (0.3 + burstWave * 0.15);

            for (int i = 0; i < particlesInBurst; i++) {
                double angle = (i / (double)particlesInBurst) * 2 * Math.PI;
                double distance = burstRadius * (0.5 + this.world.random.nextDouble() * 0.5);

                double burstX = Math.cos(angle) * distance;
                double burstZ = Math.sin(angle) * distance;
                double burstY = (this.world.random.nextDouble() - 0.3) * this.explosionRadius * 0.1;

                double speedX = Math.cos(angle) * (0.3 + this.world.random.nextDouble() * 0.4);
                double speedY = 0.2 + this.world.random.nextDouble() * 0.3;
                double speedZ = Math.sin(angle) * (0.3 + this.world.random.nextDouble() * 0.4);

                for (ServerPlayer player : nearbyPlayers) {
                    serverLevel.sendParticles(player, ParticleTypes.FLAME,
                            true,
                            this.x + burstX, adjustedY + burstY, this.z + burstZ,
                            2,
                            speedX, speedY, speedZ,
                            0.1);

                    if (i % 2 == 0) {
                        serverLevel.sendParticles(player, ParticleTypes.LAVA,
                                true,
                                this.x + burstX, adjustedY + burstY, this.z + burstZ,
                                1,
                                speedX * 0.5, speedY * 0.5, speedZ * 0.5,
                                0.05);
                    }
                }
            }
        }
        for (int scatter = 0; scatter < 25; scatter++) {
            double scatterRadius = this.explosionRadius * 1.2;
            double scatterAngle = this.world.random.nextDouble() * 2 * Math.PI;
            double scatterDistance = this.world.random.nextDouble() * scatterRadius;

            double scatterX = Math.cos(scatterAngle) * scatterDistance;
            double scatterZ = Math.sin(scatterAngle) * scatterDistance;
            double scatterY = (this.world.random.nextDouble() - 0.5) * this.explosionRadius * 0.3;

            double scatterSpeedX = (this.world.random.nextDouble() - 0.5) * 0.6;
            double scatterSpeedY = this.world.random.nextDouble() * 0.4;
            double scatterSpeedZ = (this.world.random.nextDouble() - 0.5) * 0.6;

            if (this.world.random.nextBoolean()) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        this.x + scatterX, adjustedY + scatterY, this.z + scatterZ,
                        1,
                        scatterSpeedX, scatterSpeedY, scatterSpeedZ, 0.1);
            } else {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        this.x + scatterX, adjustedY + scatterY, this.z + scatterZ,
                        1,
                        scatterSpeedX, scatterSpeedY, scatterSpeedZ, 0.05);
            }
        }

        createSimpleDelayedEffects(serverLevel, adjustedY, sizeMultiplier);
    }

    private void createSimpleDelayedEffects(ServerLevel serverLevel, double adjustedY, double sizeMultiplier) {
        for (int i = 0; i < 6; i++) {
            double angle = (i / 6.0) * 2 * Math.PI;
            double distance = this.explosionRadius * (0.6 + this.world.random.nextDouble() * 0.4);
            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;
            double offsetY = (this.world.random.nextDouble() - 0.5) * this.explosionRadius * 0.1;

            DelayedRocketParticle delayedParticle = new DelayedRocketParticle(
                    this.world,
                    this.x + offsetX,
                    adjustedY + offsetY,
                    this.z + offsetZ,
                    sizeMultiplier * 0.6,
                    2 + this.world.random.nextInt(3),
                    angle,
                    0
            );

            this.world.addFreshEntity(delayedParticle);
        }
    }

    public static class DelayedRocketParticle extends Entity {
        private final double particleSize;
        private final int spawnDelay;
        private final double angle;
        private final int waveIndex;
        private int ticksAlive = 0;

        public DelayedRocketParticle(Level world, double x, double y, double z, double size, int delay, double angle, int wave) {
            super(EntityType.MARKER, world);
            this.setPos(x, y, z);
            this.particleSize = size;
            this.spawnDelay = delay;
            this.angle = angle;
            this.waveIndex = wave;
            this.noPhysics = true;
        }

        @Override
        public void tick() {
            super.tick();
            this.ticksAlive++;

            if (this.ticksAlive == this.spawnDelay && !this.level().isClientSide()) {
                ServerLevel serverLevel = (ServerLevel) this.level();

                double renderDistance = 128.0;
                List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class,
                        new AABB(this.getX() - renderDistance, this.getY() - renderDistance, this.getZ() - renderDistance,
                                this.getX() + renderDistance, this.getY() + renderDistance, this.getZ() + renderDistance));

                for (ServerPlayer player : nearbyPlayers) {
                    serverLevel.sendParticles(player, ParticleTypes.ASH,
                            true,
                            this.getX(), this.getY(), this.getZ(),
                            1,
                            0, 0, 0,
                            this.particleSize);
                }

                double speedX = Math.cos(this.angle) * (0.1 + this.level().random.nextDouble() * 0.15);
                double speedY = 0.1 + this.level().random.nextDouble() * 0.2;
                double speedZ = Math.sin(this.angle) * (0.1 + this.level().random.nextDouble() * 0.15);

                if (this.waveIndex == 0 || this.level().random.nextDouble() < 0.3) {
                    for (ServerPlayer player : nearbyPlayers) {
                        serverLevel.sendParticles(player, ParticleTypes.FLAME,
                                true,
                                this.getX(), this.getY(), this.getZ(),
                                1,
                                speedX, speedY, speedZ,
                                0.1);
                    }
                }
                if (this.level().random.nextDouble() < 0.4) {
                    serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            this.getX(), this.getY(), this.getZ(),
                            1,
                            speedX * 0.3, speedY * 0.3, speedZ * 0.3, 0.1);
                }

                if (this.waveIndex <= 1 && this.level().random.nextDouble() < 0.2) {
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                            this.getX(), this.getZ(), this.getZ(),
                            3,
                            speedX * 0.2, speedY * 0.2, speedZ * 0.2, 0.05);
                }

                this.discard();
            }

            if (this.ticksAlive > 20) {
                this.discard();
            }
        }

        @Override
        protected void defineSynchedData() {}

        @Override
        protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}

        @Override
        protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}
    }
}