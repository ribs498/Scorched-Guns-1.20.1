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
import net.minecraft.world.effect.MobEffects;
import top.ribs.scguns.Config;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModTags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SulfurGasCloud {

    private static final int HELMET_DAMAGE_INTERVAL = 50;
    private static final float INNER_ZONE_RATIO = 0.25f;
    private static final double PARTICLE_RENDER_DISTANCE = 256.0;

    private static final int PERF_BASE_CLOUD_PARTICLES = 8;
    private static final int PERF_BASE_DUST_PARTICLES = 5;
    private static final int PERF_MAX_CLOUD_PARTICLES = 15;
    private static final int PERF_MAX_DUST_PARTICLES = 10;

    private static final int FULL_BASE_CLOUD_PARTICLES = 15;
    private static final int FULL_BASE_DUST_PARTICLES = 10;

    private static final int PARTICLE_SPAWN_INTERVAL = 3;
    private static final Map<BlockPos, Integer> activeGasClouds = new HashMap<>();
    private static final int CLOUD_PROXIMITY_THRESHOLD = 8;


    /**
     * Optimized particle spawning with smart throttling
     */
    public static void spawnCloudParticlesForced(ServerLevel serverLevel, Vec3 center, double radius, int particleCount, RandomSource random, int tickCount) {
        if (Config.CLIENT.display.enablePerformanceSulfurCloud.get()) {
            particleCount = Math.min(particleCount, PERF_MAX_CLOUD_PARTICLES);
        }

        if (tickCount % PARTICLE_SPAWN_INTERVAL != 0) {
            return;
        }

        BlockPos cloudPos = BlockPos.containing(center);
        float overlapReduction = calculateOverlapReduction(cloudPos);
        particleCount = Math.round(particleCount * overlapReduction);

        if (particleCount <= 0) return;

        List<ServerPlayer> nearbyPlayers = getNearbyPlayers(serverLevel, center, PARTICLE_RENDER_DISTANCE);
        if (nearbyPlayers.isEmpty()) return;

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
                        2,
                        xSpeed, ySpeed, zSpeed,
                        0.15);
            }
        }

        activeGasClouds.put(cloudPos, (int) serverLevel.getGameTime());
    }

    /**
     * Calculate particle reduction based on nearby clouds
     */
    private static float calculateOverlapReduction(BlockPos center) {
        int nearbyClouds = 0;

        for (Map.Entry<BlockPos, Integer> entry : activeGasClouds.entrySet()) {
            BlockPos cloudPos = entry.getKey();
            double distSq = center.distSqr(cloudPos);

            if (distSq > 0 && distSq < CLOUD_PROXIMITY_THRESHOLD * CLOUD_PROXIMITY_THRESHOLD) {
                nearbyClouds++;
            }
        }
        if (nearbyClouds == 0) return 1.0f;
        if (nearbyClouds == 1) return 0.75f;
        if (nearbyClouds == 2) return 0.5f;
        return 0.35f;
    }

    public static void cleanupCloudTracking(ServerLevel serverLevel) {
        long currentTime = serverLevel.getGameTime();
        activeGasClouds.entrySet().removeIf(entry -> currentTime - entry.getValue() > 400); // 20 seconds
    }

    public static void destroyNatureInArea(Level level, Vec3 center, double radius, RandomSource random) {
        if (level.isClientSide) return;

        if (random.nextInt(100) > 65) return;

        BlockPos centerPos = BlockPos.containing(center);
        int blockRadius = (int) Math.ceil(radius);

        List<BlockPos> blocksToDestroy = new java.util.ArrayList<>();

        for (BlockPos checkPos : BlockPos.betweenClosed(
                centerPos.offset(-blockRadius, -2, -blockRadius),
                centerPos.offset(blockRadius, 2, blockRadius))) {

            if (center.distanceTo(Vec3.atCenterOf(checkPos)) <= radius) {
                BlockState blockState = level.getBlockState(checkPos);

                if (shouldDestroyBlock(blockState)) {
                    blocksToDestroy.add(checkPos.immutable());
                }
            }
        }
        if (!blocksToDestroy.isEmpty()) {
            int destroyCount = Math.min(1 + random.nextInt(3), blocksToDestroy.size());
            for (int i = 0; i < destroyCount; i++) {
                BlockPos posToDestroy = blocksToDestroy.get(random.nextInt(blocksToDestroy.size()));
                destroyBlock(level, posToDestroy);
                blocksToDestroy.remove(posToDestroy);
            }
        }
    }

    private static boolean shouldDestroyBlock(BlockState blockState) {
        return blockState.is(net.minecraft.tags.BlockTags.FLOWERS) ||
                blockState.is(net.minecraft.tags.BlockTags.CROPS) ||
                blockState.is(net.minecraft.tags.BlockTags.SAPLINGS) ||
                blockState.is(net.minecraft.tags.BlockTags.SMALL_FLOWERS) ||
                blockState.is(net.minecraft.tags.BlockTags.TALL_FLOWERS) ||
                blockState.is(Blocks.GRASS) ||
                blockState.is(Blocks.TALL_GRASS) ||
                blockState.is(Blocks.FERN) ||
                blockState.is(Blocks.LARGE_FERN) ||
                blockState.is(Blocks.SEAGRASS) ||
                blockState.is(Blocks.TALL_SEAGRASS) ||
                blockState.is(Blocks.DEAD_BUSH) ||
                blockState.is(Blocks.VINE) ||
                blockState.is(Blocks.GLOW_LICHEN) ||
                blockState.is(Blocks.MOSS_CARPET) ||
                blockState.is(Blocks.MOSS_BLOCK) ||
                blockState.is(Blocks.SWEET_BERRY_BUSH) ||
                blockState.is(Blocks.SUGAR_CANE) ||
                blockState.is(Blocks.BAMBOO) ||
                blockState.is(Blocks.CACTUS) ||
                blockState.is(Blocks.KELP) ||
                blockState.is(Blocks.KELP_PLANT);
    }

    private static void destroyBlock(Level level, BlockPos pos) {
        level.getBlockState(pos);
        BlockState belowState = level.getBlockState(pos.below());
        if (belowState.is(Blocks.GRASS_BLOCK)) {
            level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 3);
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    public static void spawnDustParticlesForced(ServerLevel serverLevel, Vec3 center, double radius, int particleCount, RandomSource random, int tickCount) {
        if (Config.CLIENT.display.enablePerformanceSulfurCloud.get()) {
            particleCount = Math.min(particleCount, PERF_MAX_DUST_PARTICLES);
        }

        if (tickCount % 4 != 0) {
            return;
        }

        BlockPos cloudPos = BlockPos.containing(center);
        float overlapReduction = calculateOverlapReduction(cloudPos);
        particleCount = Math.round(particleCount * overlapReduction);

        if (particleCount <= 0) return;

        List<ServerPlayer> nearbyPlayers = getNearbyPlayers(serverLevel, center, PARTICLE_RENDER_DISTANCE);
        if (nearbyPlayers.isEmpty()) return;

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

        if (top.ribs.scguns.common.ChokeBombCloud.isChokeBombActive(level, center, radius * 2)) {
            return;
        }

        double radiusSquared = radius * radius;
        double innerRadiusSquared = radiusSquared * INNER_ZONE_RATIO;

        AABB effectArea = new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius));
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, effectArea);

        for (LivingEntity entity : entities) {
            double distanceSquared = entity.distanceToSqr(center);
            if (distanceSquared > radiusSquared) continue;

            if (entity.getType().is(ModTags.Entities.IGNORES_SULFUR_GAS)) {
                continue;
            }

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
            boolean isBot = entity.getType().is(ModTags.Entities.BOT);

            int amplifier = inInnerZone ? baseAmplifier + 1 : baseAmplifier;
            int duration = inInnerZone ? baseDuration * 2 : baseDuration;

            if (isBot) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));

                float damage = inInnerZone ? 1.5F : 0.75F;
                entity.hurt(entity.damageSources().magic(), damage);

                if (inInnerZone) {
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration / 2, 0));
                }
            } else {
                entity.addEffect(new MobEffectInstance(ModEffects.SULFUR_POISONING.get(), duration, amplifier));

                if (inInnerZone) {
                    entity.hurt(entity.damageSources().magic(), 1.0F);
                    entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0));
                }
            }
        }
    }

    public static boolean isInGasEffectArea(Level level, Vec3 position, int checkRadius) {
        BlockPos centerPos = BlockPos.containing(position);

        for (BlockPos checkPos : BlockPos.betweenClosed(
                centerPos.offset(-checkRadius, -checkRadius, -checkRadius),
                centerPos.offset(checkRadius, checkRadius, checkRadius))) {

            BlockState state = level.getBlockState(checkPos);
            if (state.getBlock() instanceof top.ribs.scguns.block.SulfurVentBlock) {
                boolean isActive = state.getValue(top.ribs.scguns.block.VentBlock.ACTIVE);
                boolean isBaseVent = state.getValue(top.ribs.scguns.block.VentBlock.VENT_TYPE) ==
                        top.ribs.scguns.block.VentBlock.VentType.BASE;

                if (isActive && isBaseVent) {
                    Vec3 ventPos = Vec3.atCenterOf(checkPos);
                    double distanceSquared = position.distanceToSqr(ventPos);
                    if (distanceSquared <= top.ribs.scguns.block.SulfurVentBlock.EFFECT_RADIUS_SQUARED) {
                        return true;
                    }
                }
            }
        }
        return false;
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

    public static void spawnEnhancedGasCloud(Level level, Vec3 center, double radius, float intensity, RandomSource random, int tickCount) {
        if (level.isClientSide) return;

        if (top.ribs.scguns.common.ChokeBombCloud.isChokeBombActive(level, center, radius * 2)) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        if (tickCount % 100 == 0) {
            cleanupCloudTracking(serverLevel);
        }

        int baseCloudParticles;
        int baseDustParticles;

        if (Config.CLIENT.display.enablePerformanceSulfurCloud.get()) {
            baseCloudParticles = PERF_BASE_CLOUD_PARTICLES;
            baseDustParticles = PERF_BASE_DUST_PARTICLES;
        } else {
            baseCloudParticles = FULL_BASE_CLOUD_PARTICLES;
            baseDustParticles = FULL_BASE_DUST_PARTICLES;
        }

        int cloudParticles = Math.round(baseCloudParticles * intensity);
        int dustParticles = Math.round(baseDustParticles * intensity);

        spawnCloudParticlesForced(serverLevel, center, radius, cloudParticles, random, tickCount);
        spawnDustParticlesForced(serverLevel, center, radius, dustParticles, random, tickCount);
    }

    public static void spawnEnhancedGasCloud(Level level, Vec3 center, double radius, float intensity, RandomSource random) {
        spawnEnhancedGasCloud(level, center, radius, intensity, random, 0);
    }

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