package top.ribs.scguns.common;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.SoundSource;
import top.ribs.scguns.Config;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.util.GunModifierHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Handles entity reactions to gunfire including mob aggro and animal panic
 * Uses a tag-based system for better mod compatibility with performance optimizations
 */
public class GunEffectsHandler {

    private static final Map<UUID, Long> lastEffectTime = new ConcurrentHashMap<>();
    private static final long EFFECT_COOLDOWN_MS = 250;

    private static final int MAX_ENTITIES_PER_SHOT = 25;

    private static final Map<UUID, Integer> shotCounter = new ConcurrentHashMap<>();

    private static final Predicate<LivingEntity> FLEEING_ENTITIES = entity -> {
        if (entity instanceof Animal) {
            return true;
        }
        if (entity.getType().is(ModTags.Entities.FLEEING_FROM_GUNS)) {
            return true;
        }
        return Config.COMMON.fleeingMobs.fleeingEntities.get()
                .contains(EntityType.getKey(entity.getType()).toString());
    };

    private static final Predicate<LivingEntity> HOSTILE_ENTITIES = entity -> {
        if (entity.getType().is(ModTags.Entities.AGGRO_FROM_GUNS)) {
            return true;
        }
        if (entity.getSoundSource() == SoundSource.HOSTILE) {
            return true;
        }

        if (entity.getType() == EntityType.PIGLIN ||
                entity.getType() == EntityType.PIGLIN_BRUTE ||
                entity.getType() == ModEntities.HORNLIN.get() ||
                entity.getType() == ModEntities.ZOMBIFIED_HORNLIN.get() ||
                entity.getType() == EntityType.ZOMBIFIED_PIGLIN ||
                entity.getType() == EntityType.ENDERMAN) {
            return true;
        }

        // Check exemption list
        return !Config.COMMON.aggroMobs.exemptEntities.get()
                .contains(EntityType.getKey(entity.getType()).toString());
    };

    public static void handleGunEffects(ServerPlayer player, ItemStack heldItem, Gun modifiedGun) {
        if (!Config.COMMON.aggroMobs.enabled.get() && !Config.COMMON.fleeingMobs.enabled.get()) {
            return;
        }

        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();

        Long lastTime = lastEffectTime.get(playerId);
        if (lastTime != null && (currentTime - lastTime) < EFFECT_COOLDOWN_MS) {
            return;
        }

        int fireRate = modifiedGun.getGeneral().getRate();
        if (fireRate < 6) {
            int shots = shotCounter.merge(playerId, 1, Integer::sum);
            if (shots % 2 != 0) {
                return;
            }
        }
        lastEffectTime.put(playerId, currentTime);

        Level world = player.level();
        boolean isSilenced = GunModifierHelper.isSilencedFire(heldItem);

        double effectRadius = getEffectRadius(isSilenced);
        List<LivingEntity> nearbyEntities = getOptimizedNearbyEntities(world, player, effectRadius);

        for (LivingEntity entity : nearbyEntities) {
            if (entity == player) continue;
            handleEntityReaction(entity, player, isSilenced);
        }
        if (player.tickCount % 1200 == 0) {
            cleanupOldEntries(currentTime);
        }
    }

    private static double getEffectRadius(boolean isSilenced) {
        if (isSilenced) {
            return Config.COMMON.fleeingMobs.silencedRange.get();
        } else {
            return Math.max(
                    Config.COMMON.aggroMobs.unsilencedRange.get(),
                    Config.COMMON.fleeingMobs.unsilencedRange.get()
            );
        }
    }

    /**
     * Performance optimized entity retrieval with limiting and distance sorting
     */
    private static List<LivingEntity> getOptimizedNearbyEntities(Level world, ServerPlayer player, double radius) {
        AABB searchArea = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, searchArea);

        if (entities.size() > MAX_ENTITIES_PER_SHOT) {
            return entities.stream()
                    .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                    .limit(MAX_ENTITIES_PER_SHOT)
                    .collect(Collectors.toList());
        }

        return entities;
    }

    private static void handleEntityReaction(LivingEntity entity, ServerPlayer player, boolean isSilenced) {
        if (!isSilenced && shouldEntityFlee(entity)) {
            handleFleeingBehavior(entity, player);
        }
        if (!isSilenced && shouldEntityAggro(entity)) {
            handleAggroBehavior(entity, player);
        }
    }

    private static boolean shouldEntityFlee(LivingEntity entity) {
        return Config.COMMON.fleeingMobs.enabled.get() &&
                FLEEING_ENTITIES.test(entity) &&
                !isTamedMob(entity);
    }

    private static boolean shouldEntityAggro(LivingEntity entity) {
        return Config.COMMON.aggroMobs.enabled.get() && HOSTILE_ENTITIES.test(entity);
    }

    /**
     * Checks if an entity is tamed by any player
     * Covers TamableAnimal (wolves, cats, parrots) and AbstractHorse (horses, donkeys, mules, llamas)
     */
    private static boolean isTamedMob(LivingEntity entity) {
        // Check for TamableAnimal (wolves, cats, parrots, etc.)
        if (entity instanceof TamableAnimal tamableAnimal) {
            return tamableAnimal.isTame();
        }

        // Check for AbstractHorse (horses, donkeys, mules, llamas, etc.)
        if (entity instanceof AbstractHorse horse) {
            return horse.isTamed();
        }

        // Add other mod-specific tamed entities here if needed
        // Example for other mods:
        // if (entity instanceof SomeModTamedEntity modEntity) {
        //     return modEntity.isTamed();
        // }

        return false;
    }

    private static void handleFleeingBehavior(LivingEntity entity, ServerPlayer player) {
        double deltaX = entity.getX() - player.getX();
        double deltaZ = entity.getZ() - player.getZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (distance > 0) {
            double knockbackStrength = getFleeKnockbackStrength(entity);
            double normalizedX = deltaX / distance;
            double normalizedZ = deltaZ / distance;

            entity.knockback(knockbackStrength, -normalizedX, -normalizedZ);

            if (entity instanceof net.minecraft.world.entity.Mob mob) {
                double fleeDistance = entity instanceof Animal ? 16.0 : 12.0;
                double fleeX = entity.getX() + normalizedX * fleeDistance;
                double fleeZ = entity.getZ() + normalizedZ * fleeDistance;

                mob.getNavigation().moveTo(fleeX, entity.getY(), fleeZ, 1.2);
            }
        }
    }

    private static double getFleeKnockbackStrength(LivingEntity entity) {
        if (entity.getType().is(ModTags.Entities.HEAVY)) {
            return 0.4;
        }
        if (entity.getType().is(ModTags.Entities.VERY_HEAVY)) {
            return 0.2;
        }
        return 0.8;
    }

    private static void handleAggroBehavior(LivingEntity entity, ServerPlayer player) {
        if (entity instanceof Monster monster) {
            float aggroChance = Config.COMMON.aggroMobs.aggroChance.get().floatValue();
            if (player.level().random.nextFloat() < aggroChance) {
                monster.setTarget(player);
                alertNearbyMobs(monster, player);
            }
        }
    }

    private static void alertNearbyMobs(Monster alertedMob, ServerPlayer player) {
        double chainRadius = Config.COMMON.aggroMobs.chainAggroRadius.get();
        float chainChance = Config.COMMON.aggroMobs.chainAggroChance.get().floatValue();

        EntityType<?> mobType = alertedMob.getType();
        List<LivingEntity> nearbyMobs = getOptimizedNearbyEntities(player.level(), player, chainRadius);

        for (LivingEntity entity : nearbyMobs) {
            if (entity.getType() == mobType && entity instanceof Monster nearbyMonster && entity != alertedMob) {
                if (player.level().random.nextFloat() < chainChance) {
                    nearbyMonster.setTarget(player);
                }
            }
        }
    }

    /**
     * Cleanup old entries to prevent memory leaks
     */
    private static void cleanupOldEntries(long currentTime) {
        long expireTime = currentTime - (EFFECT_COOLDOWN_MS * 20);

        lastEffectTime.entrySet().removeIf(entry -> entry.getValue() < expireTime);

        shotCounter.clear();
    }
}