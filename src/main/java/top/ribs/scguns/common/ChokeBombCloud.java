package top.ribs.scguns.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.init.ModTags;

import java.util.List;

public class ChokeBombCloud {

    private static final double PARTICLE_RENDER_DISTANCE = 256.0;
    private static final int BASE_CLOUD_PARTICLES = 20;
    private static final int BASE_ASH_PARTICLES = 10;

    public static void spawnChokeCloudParticles(Level level, Vec3 center, double radius, float intensity, RandomSource random) {
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        List<ServerPlayer> nearbyPlayers = getNearbyPlayers(serverLevel, center, PARTICLE_RENDER_DISTANCE);

        int cloudParticles = Math.round(BASE_CLOUD_PARTICLES * intensity);
        int ashParticles = Math.round(BASE_ASH_PARTICLES * intensity);

        for (int i = 0; i < cloudParticles; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double sphereRadius = Math.sqrt(random.nextDouble()) * radius;
            double x = center.x + Math.cos(angle) * sphereRadius;
            double z = center.z + Math.sin(angle) * sphereRadius;
            double y = center.y + (random.nextDouble() - 0.5) * (radius * 0.5);

            double speed = 0.002 + random.nextDouble() * 0.005;
            double xSpeed = (random.nextDouble() - 0.5) * speed;
            double ySpeed = random.nextDouble() * speed * 0.5;
            double zSpeed = (random.nextDouble() - 0.5) * speed;

            for (ServerPlayer player : nearbyPlayers) {
                serverLevel.sendParticles(player, ParticleTypes.SNOWFLAKE,
                        true, x, y, z, 1, xSpeed, ySpeed, zSpeed, 0.1);
            }
        }

        for (int i = 0; i < ashParticles; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double ashRadius = Math.sqrt(random.nextDouble()) * (radius * 1.1);
            double x = center.x + Math.cos(angle) * ashRadius;
            double z = center.z + Math.sin(angle) * ashRadius;
            double y = center.y + 0.1 + random.nextDouble() * 0.3;

            double speed = 0.001 + random.nextDouble() * 0.002;
            double xSpeed = (random.nextDouble() - 0.5) * speed;
            double ySpeed = random.nextDouble() * speed * 0.5;
            double zSpeed = (random.nextDouble() - 0.5) * speed;

            for (ServerPlayer player : nearbyPlayers) {
                serverLevel.sendParticles(player, ParticleTypes.WHITE_ASH,
                        true, x, y, z, 1, xSpeed, ySpeed, zSpeed, 0.1);
            }
        }
    }

    private static List<ServerPlayer> getNearbyPlayers(ServerLevel serverLevel, Vec3 center, double renderDistance) {
        AABB searchArea = new AABB(
                center.subtract(renderDistance, renderDistance, renderDistance),
                center.add(renderDistance, renderDistance, renderDistance)
        );
        return serverLevel.getEntitiesOfClass(ServerPlayer.class, searchArea);
    }

    public static void applyChokeEffects(Level level, Vec3 center, double radius) {
        if (level.isClientSide) return;

        double radiusSquared = radius * radius;

        AABB effectArea = new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius));
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, effectArea);

        for (LivingEntity entity : entities) {
            double distanceSquared = entity.distanceToSqr(center);
            if (distanceSquared > radiusSquared) continue;

            if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }

            if (entity instanceof Player player && top.ribs.scguns.common.exosuit.ExoSuitGasMaskHandler.hasProtection(player)) {
                continue;
            }

            ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.is(ModTags.Items.GAS_MASK)) {
                continue;
            }

            if (entity.getAirSupply() > 0) {
                entity.setAirSupply(entity.getAirSupply() - 5);
            }

            if (entity.getAirSupply() <= 0) {
                entity.hurt(entity.damageSources().drown(), 2.0F);
            }

            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, true));
        }
    }

    public static void extinguishFireInArea(Level level, Vec3 center, double radius) {
        BlockPos centerPos = BlockPos.containing(center);
        int blockRadius = (int) Math.ceil(radius);

        for (BlockPos checkPos : BlockPos.betweenClosed(
                centerPos.offset(-blockRadius, -1, -blockRadius),
                centerPos.offset(blockRadius, 1, blockRadius))) {

            if (center.distanceTo(Vec3.atCenterOf(checkPos)) <= radius) {
                BlockState blockState = level.getBlockState(checkPos);
                if (blockState.is(Blocks.FIRE) || blockState.is(Blocks.SOUL_FIRE)) {
                    level.setBlock(checkPos, Blocks.AIR.defaultBlockState(), 3);
                } else if (blockState.is(Blocks.CAMPFIRE) || blockState.is(Blocks.SOUL_CAMPFIRE)) {
                    level.setBlock(checkPos, blockState.setValue(BlockStateProperties.LIT, false), 3);
                }
            }
        }

        AABB effectArea = new AABB(centerPos).inflate(radius);
        List<LivingEntity> affectedEntities = level.getEntitiesOfClass(LivingEntity.class, effectArea);
        for (LivingEntity affectedEntity : affectedEntities) {
            if (affectedEntity.isOnFire()) {
                affectedEntity.clearFire();
            }
        }
    }

    public static boolean isChokeBombActive(Level level, Vec3 position, double checkRadius) {
        if (level.isClientSide) return false;

        AABB searchArea = new AABB(
                position.subtract(checkRadius, checkRadius, checkRadius),
                position.add(checkRadius, checkRadius, checkRadius)
        );

        List<? extends Entity> allEntities = level.getEntities((Entity) null, searchArea);

        for (Entity entity : allEntities) {
            if (entity.getClass().getSimpleName().equals("ThrowableChokeBombEntity")) {
                try {
                    java.lang.reflect.Field cloudActiveField = entity.getClass().getDeclaredField("cloudActive");
                    cloudActiveField.setAccessible(true);
                    if (cloudActiveField.getBoolean(entity)) {
                        return true;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }

        return false;
    }

    public static void removeGasCloudsInArea(Level level, Vec3 center, double radius) {
        if (level.isClientSide) return;

        AABB searchArea = new AABB(
                center.subtract(radius, radius, radius),
                center.add(radius, radius, radius)
        );

        List<? extends Entity> allEntities = level.getEntities((Entity) null, searchArea);

        for (Entity entity : allEntities) {
            if (entity.getClass().getSimpleName().equals("ThrowableGasGrenadeEntity")) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }
}