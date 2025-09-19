package top.ribs.scguns.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModTags;

import java.util.List;

public class SulfurGasCloud {

    private static final int HELMET_DAMAGE_INTERVAL = 50;
    private static final float INNER_ZONE_RATIO = 0.25f;
    private static final double PARTICLE_RENDER_DISTANCE = 256.0;

    public static void spawnCloudParticlesForced(ServerLevel serverLevel, Vec3 center, double radius, int particleCount, RandomSource random) {
        List<ServerPlayer> nearbyPlayers = getNearbyPlayers(serverLevel, center, PARTICLE_RENDER_DISTANCE);

        for (int i = 0; i < particleCount; i++) {
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
                serverLevel.sendParticles(player, ModParticleTypes.SULFUR_SMOKE.get(),
                        true,
                        x, y, z,
                        1,
                        xSpeed, ySpeed, zSpeed,
                        0.1);
            }
        }
    }

    public static void spawnDustParticles(Level level, Vec3 center, double radius, int particleCount, RandomSource random) {
        if (level.isClientSide) {
            for (int i = 0; i < particleCount; i++) {
                double angle = random.nextDouble() * 2 * Math.PI;
                double dustRadius = Math.sqrt(random.nextDouble()) * (radius * 1.2);
                double x = center.x + Math.cos(angle) * dustRadius;
                double z = center.z + Math.sin(angle) * dustRadius;
                double y = center.y + 0.1 + random.nextDouble() * 0.3;

                double speed = 0.001 + random.nextDouble() * 0.002;
                double xSpeed = (random.nextDouble() - 0.5) * speed;
                double ySpeed = random.nextDouble() * speed * 0.5;
                double zSpeed = (random.nextDouble() - 0.5) * speed;

                level.addParticle(ModParticleTypes.SULFUR_DUST.get(), x, y, z, xSpeed, ySpeed, zSpeed);
            }
        } else {
            spawnDustParticlesForced((ServerLevel) level, center, radius, particleCount, random);
        }
    }

    public static void spawnDustParticlesForced(ServerLevel serverLevel, Vec3 center, double radius, int particleCount, RandomSource random) {
        List<ServerPlayer> nearbyPlayers = getNearbyPlayers(serverLevel, center, PARTICLE_RENDER_DISTANCE);

        for (int i = 0; i < particleCount; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dustRadius = Math.sqrt(random.nextDouble()) * (radius * 1.2);
            double x = center.x + Math.cos(angle) * dustRadius;
            double z = center.z + Math.sin(angle) * dustRadius;
            double y = center.y + 0.1 + random.nextDouble() * 0.3;

            double speed = 0.001 + random.nextDouble() * 0.002;
            double xSpeed = (random.nextDouble() - 0.5) * speed;
            double ySpeed = random.nextDouble() * speed * 0.5;
            double zSpeed = (random.nextDouble() - 0.5) * speed;

            for (ServerPlayer player : nearbyPlayers) {
                serverLevel.sendParticles(player, ModParticleTypes.SULFUR_DUST.get(),
                        true,
                        x, y, z,
                        1,
                        xSpeed, ySpeed, zSpeed,
                        0.1);
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

    public static void applyGasEffects(Level level, Vec3 center, double radius, int baseDuration, int baseAmplifier) {
        if (level.isClientSide) return;

        double radiusSquared = radius * radius;
        double innerRadiusSquared = radiusSquared * INNER_ZONE_RATIO;

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
                damageGasMask(entity, helmet);
                continue;
            }
            boolean inInnerZone = distanceSquared <= innerRadiusSquared;

            int amplifier = inInnerZone ? baseAmplifier + 1 : baseAmplifier;
            int duration = inInnerZone ? baseDuration * 2 : baseDuration;

            entity.addEffect(new MobEffectInstance(ModEffects.SULFUR_POISONING.get(), duration, amplifier));

            if (inInnerZone) {
                entity.hurt(entity.damageSources().magic(), 1.0F);
                entity.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 60, 0));
            }
        }
    }

    private static void damageGasMask(LivingEntity entity, ItemStack helmet) {
        String lastDamageKey = "LastHelmetDamageTick";
        long lastDamage = entity.getPersistentData().getLong(lastDamageKey);

        if (lastDamage + HELMET_DAMAGE_INTERVAL <= entity.tickCount) {
            entity.getPersistentData().putLong(lastDamageKey, entity.tickCount);

            int unbreakingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, helmet);
            if (shouldDamageItem(unbreakingLevel, entity.getRandom())) {
                helmet.hurtAndBreak(1, entity, (e) -> e.broadcastBreakEvent(EquipmentSlot.HEAD));
            }
        }
    }

    private static boolean shouldDamageItem(int unbreakingLevel, RandomSource random) {
        if (unbreakingLevel > 0) {
            int chance = 1 + unbreakingLevel;
            return random.nextInt(chance) == 0;
        }
        return true;
    }

    public static void applyGasEffects(Level level, BlockPos pos, double radius, int baseDuration, int baseAmplifier) {
        Vec3 center = Vec3.atCenterOf(pos);
        applyGasEffects(level, center, radius, baseDuration, baseAmplifier);
    }

    public static void spawnEnhancedGasCloud(Level level, Vec3 center, double radius, float intensity, RandomSource random) {
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;

        int baseCloudParticles = 40;
        int baseDustParticles = 25;

        int cloudParticles = Math.round(baseCloudParticles * intensity);
        int dustParticles = Math.round(baseDustParticles * intensity);
        spawnCloudParticlesForced(serverLevel, center, radius, cloudParticles, random);
        spawnDustParticlesForced(serverLevel, center, radius, dustParticles, random);
    }

    // Fire detection and explosion methods (moved from SulfurVentBlock)
    public static boolean isFireInArea(Level level, Vec3 center, double radius) {
        BlockPos centerPos = BlockPos.containing(center);
        int blockRadius = (int) Math.ceil(radius);

        for (BlockPos checkPos : BlockPos.betweenClosed(
                centerPos.offset(-blockRadius, -1, -blockRadius),
                centerPos.offset(blockRadius, 1, blockRadius))) {

            if (center.distanceTo(Vec3.atCenterOf(checkPos)) <= radius) {
                BlockState blockState = level.getBlockState(checkPos);
                if (isFireSource(blockState)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isFireSource(BlockState blockState) {
        return blockState.is(Blocks.FIRE) ||
                blockState.is(Blocks.SOUL_FIRE) ||
                (blockState.is(Blocks.CAMPFIRE) && blockState.getValue(BlockStateProperties.LIT)) ||
                (blockState.is(Blocks.SOUL_CAMPFIRE) && blockState.getValue(BlockStateProperties.LIT));
    }

    public static void triggerGasExplosion(Level level, Vec3 center, double radius) {
        if (level.isClientSide) return;

        RandomSource random = level.random;
        for (int i = 0; i < 6; i++) {
            double xOffset = (random.nextDouble() - 0.5) * 2.0 * radius;
            double yOffset = (random.nextDouble() - 0.5) * 2.0 * radius;
            double zOffset = (random.nextDouble() - 0.5) * 2.0 * radius;

            Vec3 explosionPos = center.add(xOffset, yOffset, zOffset);
            level.explode(null, explosionPos.x, explosionPos.y, explosionPos.z, 4.0F, Level.ExplosionInteraction.NONE);
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
    }
    public static boolean isTemporaryLightInArea(Level level, Vec3 center, double radius) {
        BlockPos centerPos = BlockPos.containing(center);
        int blockRadius = (int) Math.ceil(radius);

        for (BlockPos checkPos : BlockPos.betweenClosed(
                centerPos.offset(-blockRadius, -1, -blockRadius),
                centerPos.offset(blockRadius, 1, blockRadius))) {

            if (center.distanceTo(Vec3.atCenterOf(checkPos)) <= radius) {
                BlockState blockState = level.getBlockState(checkPos);
                if (blockState.getBlock().getClass().getSimpleName().equals("TemporaryLightBlock")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkAndHandleFireExplosion(Level level, Vec3 center, double radius) {
        if (isFireInArea(level, center, radius) || isTemporaryLightInArea(level, center, radius)) {
            triggerGasExplosion(level, center, radius);
            extinguishFireInArea(level, center, radius);
            return true;
        }
        return false;
    }
}