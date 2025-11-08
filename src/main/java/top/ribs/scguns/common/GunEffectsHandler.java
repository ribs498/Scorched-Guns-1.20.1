package top.ribs.scguns.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.GameEventTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
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
        return !Config.COMMON.aggroMobs.exemptEntities.get()
                .contains(EntityType.getKey(entity.getType()).toString());
    };

    public static void handleGunEffects(ServerPlayer player, ItemStack heldItem, Gun modifiedGun) {
        if (player.isCreative() ||
                (!Config.COMMON.aggroMobs.enabled.get() && !Config.COMMON.fleeingMobs.enabled.get())) {
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

        if (!isSilenced && world instanceof ServerLevel serverLevel) {
            triggerSculkSensor(serverLevel, player);
        }

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

    private static void triggerSculkSensor(ServerLevel level, ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();
        level.gameEvent(GameEvent.PROJECTILE_SHOOT, playerPos, GameEvent.Context.of(player));
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
        if (!isSilenced) {
            if (shouldEntityFlee(entity)) {
                applyKnockbackEffect(entity, player);
            }
            if (shouldEntityAggro(entity)) {
                handleAggroBehavior(entity, player);
            }
        }
    }

    private static boolean shouldEntityFlee(LivingEntity entity) {
        return Config.COMMON.fleeingMobs.enabled.get() &&
                FLEEING_ENTITIES.test(entity) &&
                !isTamedMob(entity) &&
                entity.getPassengers().isEmpty();
    }

    private static boolean shouldEntityAggro(LivingEntity entity) {
        return Config.COMMON.aggroMobs.enabled.get() && HOSTILE_ENTITIES.test(entity);
    }

    private static boolean isTamedMob(LivingEntity entity) {
        if (entity instanceof TamableAnimal tamableAnimal) {
            return tamableAnimal.isTame();
        }
        if (entity instanceof AbstractHorse horse) {
            return horse.isTamed();
        }
        return false;
    }

    private static void applyKnockbackEffect(LivingEntity entity, ServerPlayer player) {
        double deltaX = entity.getX() - player.getX();
        double deltaZ = entity.getZ() - player.getZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (distance > 0) {
            double knockbackStrength = getKnockbackStrength(entity);
            double normalizedX = deltaX / distance;
            double normalizedZ = deltaZ / distance;

            entity.knockback(knockbackStrength, -normalizedX, -normalizedZ);
        }
    }

    private static double getKnockbackStrength(LivingEntity entity) {
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

    private static void cleanupOldEntries(long currentTime) {
        long expireTime = currentTime - (EFFECT_COOLDOWN_MS * 20);
        lastEffectTime.entrySet().removeIf(entry -> entry.getValue() < expireTime);
        shotCounter.clear();
    }
}