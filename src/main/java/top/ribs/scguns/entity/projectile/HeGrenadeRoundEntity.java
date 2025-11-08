package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;

import java.util.List;

public class HeGrenadeRoundEntity extends ProjectileEntity {
    private static final float EXPLOSION_RADIUS = 5.0f;

    public HeGrenadeRoundEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public HeGrenadeRoundEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn,
                                LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        if (entity instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();

            boolean isBlockingMainHand = player.isBlocking() && mainHandItem.getItem() instanceof ShieldItem;
            boolean isBlockingOffHand = player.isBlocking() && offHandItem.getItem() instanceof ShieldItem;

            if (isBlockingMainHand || isBlockingOffHand) {
                ItemStack shield = isBlockingMainHand ? mainHandItem : offHandItem;
                InteractionHand hand = isBlockingMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

                player.getCooldowns().addCooldown(shield.getItem(), 100);
                player.stopUsingItem();
                player.level().broadcastEntityEvent(player, (byte)30);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1.0F,
                        0.8F + player.level().getRandom().nextFloat() * 0.4F);

                shield.hurtAndBreak(15, player, (p) -> p.broadcastBreakEvent(hand));
            }
        }

        float exactDamage = this.getDamage();
        createHeGrenadeExplosion(this, EXPLOSION_RADIUS, exactDamage);
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        float exactDamage = this.getDamage();
        createHeGrenadeExplosion(this, EXPLOSION_RADIUS, exactDamage);
    }

    @Override
    public void onExpired() {
        float exactDamage = this.getDamage();
        createHeGrenadeExplosion(this, EXPLOSION_RADIUS, exactDamage);
    }

    private void createHeGrenadeExplosion(Entity entity, float radius, float damage) {
        Level world = entity.level();
        if (world.isClientSide()) {
            return;
        }

        Vec3 explosionPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());

        playExplosionSound(world, explosionPos, radius);
        spawnExplosionParticles(world, explosionPos, radius);
        applyExplosionDamage(world, explosionPos, radius, damage, entity);
    }

    private void playExplosionSound(Level world, Vec3 pos, float radius) {
        float volume = Math.min(3.5F, radius * 0.5F);
        float pitch = 0.9F + world.random.nextFloat() * 0.3F;

        world.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                volume, pitch);
    }

    private void spawnExplosionParticles(Level world, Vec3 explosionPos, float radius) {
        ServerLevel serverLevel = (ServerLevel) world;
        double sizeMultiplier = radius / 3.5;

        BlockPos blockPos = BlockPos.containing(explosionPos.x, explosionPos.y, explosionPos.z);
        BlockState blockAtExplosion = world.getBlockState(blockPos);

        double adjustedY;
        if (!blockAtExplosion.isAir()) {
            adjustedY = blockPos.getY() + 1.0;
        } else {
            adjustedY = explosionPos.y + 0.2;
        }

        double renderDistance = 128.0;
        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class,
                new AABB(explosionPos.x - renderDistance, explosionPos.y - renderDistance, explosionPos.z - renderDistance,
                        explosionPos.x + renderDistance, explosionPos.y + renderDistance, explosionPos.z + renderDistance));

        for (ServerPlayer player : nearbyPlayers) {
            serverLevel.sendParticles(player, ModParticleTypes.GRENADE_EXPLOSION.get(),
                    true,
                    explosionPos.x, adjustedY, explosionPos.z,
                    1,
                    sizeMultiplier, 0.0, 0.0,
                    0.0);
        }

        for (int burstWave = 0; burstWave < 2; burstWave++) {
            int particlesInBurst = 6 + burstWave * 3;
            double burstRadius = radius * (0.3 + burstWave * 0.15);

            for (int i = 0; i < particlesInBurst; i++) {
                double angle = (i / (double)particlesInBurst) * 2 * Math.PI;
                double distance = burstRadius * (0.5 + world.random.nextDouble() * 0.5);

                double burstX = Math.cos(angle) * distance;
                double burstZ = Math.sin(angle) * distance;
                double burstY = (world.random.nextDouble() - 0.3) * radius * 0.1;

                double speedX = Math.cos(angle) * (0.2 + world.random.nextDouble() * 0.3);
                double speedY = 0.15 + world.random.nextDouble() * 0.25;
                double speedZ = Math.sin(angle) * (0.2 + world.random.nextDouble() * 0.3);

                for (ServerPlayer player : nearbyPlayers) {
                    serverLevel.sendParticles(player, ParticleTypes.FLAME,
                            true,
                            explosionPos.x + burstX, adjustedY + burstY, explosionPos.z + burstZ,
                            1,
                            speedX, speedY, speedZ,
                            0.08);
                }
            }
        }

        for (int scatter = 0; scatter < 15; scatter++) {
            double scatterRadius = radius * 1.0;
            double scatterAngle = world.random.nextDouble() * 2 * Math.PI;
            double scatterDistance = world.random.nextDouble() * scatterRadius;

            double scatterX = Math.cos(scatterAngle) * scatterDistance;
            double scatterZ = Math.sin(scatterAngle) * scatterDistance;
            double scatterY = (world.random.nextDouble() - 0.5) * radius * 0.25;

            double scatterSpeedX = (world.random.nextDouble() - 0.5) * 0.5;
            double scatterSpeedY = world.random.nextDouble() * 0.35;
            double scatterSpeedZ = (world.random.nextDouble() - 0.5) * 0.5;

            if (world.random.nextBoolean()) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        explosionPos.x + scatterX, adjustedY + scatterY, explosionPos.z + scatterZ,
                        1,
                        scatterSpeedX, scatterSpeedY, scatterSpeedZ, 0.08);
            } else {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        explosionPos.x + scatterX, adjustedY + scatterY, explosionPos.z + scatterZ,
                        1,
                        scatterSpeedX, scatterSpeedY, scatterSpeedZ, 0.04);
            }
        }
    }

    private void applyExplosionDamage(Level world, Vec3 explosionPos, float radius, float baseDamage, Entity sourceEntity) {
        float damageRadius = radius * 2.0F;
        int minX = Mth.floor(explosionPos.x - damageRadius - 1.0D);
        int maxX = Mth.floor(explosionPos.x + damageRadius + 1.0D);
        int minY = Mth.floor(explosionPos.y - damageRadius - 1.0D);
        int maxY = Mth.floor(explosionPos.y + damageRadius + 1.0D);
        int minZ = Mth.floor(explosionPos.z - damageRadius - 1.0D);
        int maxZ = Mth.floor(explosionPos.z + damageRadius + 1.0D);

        List<Entity> entities = world.getEntitiesOfClass(Entity.class, new AABB(minX, minY, minZ, maxX, maxY, maxZ));

        DamageSource damageSource = ModDamageTypes.Sources.projectile(world.registryAccess(), sourceEntity, this.getShooter());

        for (Entity entity : entities) {
            if (entity.ignoreExplosion()) {
                continue;
            }

            double distance = Math.sqrt(entity.distanceToSqr(explosionPos));
            double maxDamageDistance = radius;
            double maxDistance = radius * 2.0;

            if (distance >= maxDistance) {
                continue;
            }

            float damage;
            if (distance <= maxDamageDistance) {
                damage = baseDamage;
            } else {
                float falloffMultiplier = 1.0f - (float)((distance - maxDamageDistance) / (maxDistance - maxDamageDistance));
                damage = baseDamage * falloffMultiplier;
            }

            if (entity instanceof LivingEntity livingEntity) {
                damage = applyBlastProtection(livingEntity, damage);
            }

            if (damage > 0) {
                entity.hurt(damageSource, damage);

                double deltaX = entity.getX() - explosionPos.x;
                double deltaY = entity.getEyeY() - explosionPos.y;
                double deltaZ = entity.getZ() - explosionPos.z;
                double distanceToExplosion = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                if (distanceToExplosion != 0.0D) {
                    deltaX /= distanceToExplosion;
                    deltaY /= distanceToExplosion;
                    deltaZ /= distanceToExplosion;
                } else {
                    deltaX = 0.0;
                    deltaY = 1.0;
                    deltaZ = 0.0;
                }

                double knockbackStrength = Math.max(0, (1.0D - distance / maxDistance) * 0.4);
                entity.setDeltaMovement(entity.getDeltaMovement().add(
                        deltaX * knockbackStrength,
                        deltaY * knockbackStrength,
                        deltaZ * knockbackStrength));
            }
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
}