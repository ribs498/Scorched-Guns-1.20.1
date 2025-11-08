package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;

import java.util.List;

public class FireGrenadeRoundEntity extends ProjectileEntity {
    private static final float EXPLOSION_RADIUS = 3.5f;

    public FireGrenadeRoundEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public FireGrenadeRoundEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn,
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

        createFireGrenadeExplosion(this, EXPLOSION_RADIUS);
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        createFireGrenadeExplosion(this, EXPLOSION_RADIUS);
    }

    @Override
    public void onExpired() {
        createFireGrenadeExplosion(this, EXPLOSION_RADIUS);
    }

    private void createFireGrenadeExplosion(Entity entity, float radius) {
        Level world = entity.level();
        if (world.isClientSide()) {
            return;
        }

        Vec3 explosionPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());

        playExplosionSound(world, explosionPos, radius);
        spawnExplosionParticles(world, explosionPos, radius);
        applyFireDamageAndPlacement(world, explosionPos, radius, entity);
    }

    private void playExplosionSound(Level world, Vec3 pos, float radius) {
        float volume = Math.min(3.0F, radius * 0.5F);
        float pitch = 0.9F + world.random.nextFloat() * 0.3F;

        world.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                volume, pitch);

        world.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS,
                volume * 0.8F, pitch);
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
            serverLevel.sendParticles(player, ModParticleTypes.FIRE_GRENADE_EXPLOSION.get(),
                    true,
                    explosionPos.x, adjustedY, explosionPos.z,
                    1,
                    sizeMultiplier, 0.0, 0.0,
                    0.0);
        }

        for (int burstWave = 0; burstWave < 3; burstWave++) {
            int particlesInBurst = 8 + burstWave * 4;
            double burstRadius = radius * (0.3 + burstWave * 0.15);

            for (int i = 0; i < particlesInBurst; i++) {
                double angle = (i / (double)particlesInBurst) * 2 * Math.PI;
                double distance = burstRadius * (0.5 + world.random.nextDouble() * 0.5);

                double burstX = Math.cos(angle) * distance;
                double burstZ = Math.sin(angle) * distance;
                double burstY = (world.random.nextDouble() - 0.3) * radius * 0.15;

                double speedX = Math.cos(angle) * (0.25 + world.random.nextDouble() * 0.35);
                double speedY = 0.2 + world.random.nextDouble() * 0.3;
                double speedZ = Math.sin(angle) * (0.25 + world.random.nextDouble() * 0.35);

                for (ServerPlayer player : nearbyPlayers) {
                    serverLevel.sendParticles(player, ParticleTypes.FLAME,
                            true,
                            explosionPos.x + burstX, adjustedY + burstY, explosionPos.z + burstZ,
                            2,
                            speedX, speedY, speedZ,
                            0.1);
                }
            }
        }

        for (int scatter = 0; scatter < 20; scatter++) {
            double scatterRadius = radius * 1.2;
            double scatterAngle = world.random.nextDouble() * 2 * Math.PI;
            double scatterDistance = world.random.nextDouble() * scatterRadius;

            double scatterX = Math.cos(scatterAngle) * scatterDistance;
            double scatterZ = Math.sin(scatterAngle) * scatterDistance;
            double scatterY = (world.random.nextDouble() - 0.5) * radius * 0.3;

            double scatterSpeedX = (world.random.nextDouble() - 0.5) * 0.6;
            double scatterSpeedY = world.random.nextDouble() * 0.4;
            double scatterSpeedZ = (world.random.nextDouble() - 0.5) * 0.6;

            if (world.random.nextFloat() < 0.7) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        explosionPos.x + scatterX, adjustedY + scatterY, explosionPos.z + scatterZ,
                        1,
                        scatterSpeedX, scatterSpeedY, scatterSpeedZ, 0.1);
            } else {
                serverLevel.sendParticles(ParticleTypes.LAVA,
                        explosionPos.x + scatterX, adjustedY + scatterY, explosionPos.z + scatterZ,
                        1,
                        scatterSpeedX, scatterSpeedY, scatterSpeedZ, 0.05);
            }
        }
    }

    private void applyFireDamageAndPlacement(Level world, Vec3 explosionPos, float radius, Entity sourceEntity) {
        BlockPos centerPos = BlockPos.containing(explosionPos);
        AABB effectArea = new AABB(centerPos).inflate(radius);
        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class, effectArea);

        for (LivingEntity livingEntity : nearbyEntities) {
            double distance = livingEntity.distanceTo(sourceEntity);
            if (distance <= radius) {
                livingEntity.setSecondsOnFire(6);
                float damage = (float) (3.0F * (1.0 - distance / radius));
                livingEntity.hurt(world.damageSources().inFire(), damage);
            }
        }

        ServerLevel serverLevel = (ServerLevel) world;
        int radiusInt = (int) Math.ceil(radius);
        int radiusSquared = radiusInt * radiusInt;

        for (int x = -radiusInt; x <= radiusInt; x++) {
            for (int z = -radiusInt; z <= radiusInt; z++) {
                if (world.random.nextFloat() > 0.6f) {
                    continue;
                }

                BlockPos columnPos = centerPos.offset(x, 0, z);

                if (centerPos.distSqr(columnPos) <= radiusSquared) {
                    double distanceFromCenter = Math.sqrt(centerPos.distSqr(columnPos));
                    int delay = (int) (distanceFromCenter * 2);

                    scheduleFirePlacement(serverLevel, centerPos, columnPos, radiusInt, delay);
                }
            }
        }
    }

    private void scheduleFirePlacement(ServerLevel world, BlockPos centerPos, BlockPos columnPos, int radiusInt, int delay) {
        world.getServer().tell(new net.minecraft.server.TickTask(
                world.getServer().getTickCount() + delay,
                () -> {
                    for (int y = -radiusInt; y <= radiusInt; y++) {
                        BlockPos pos = centerPos.offset(
                                columnPos.getX() - centerPos.getX(),
                                y,
                                columnPos.getZ() - centerPos.getZ()
                        );
                        BlockState stateAtPos = world.getBlockState(pos);
                        BlockState stateBelow = world.getBlockState(pos.below());

                        if (stateAtPos.isAir() &&
                                (stateBelow.isFaceSturdy(world, pos.below(), Direction.UP) ||
                                        !stateBelow.isAir())) {
                            world.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                            break;
                        }
                    }
                }
        ));
    }
}