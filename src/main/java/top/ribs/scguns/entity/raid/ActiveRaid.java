package top.ribs.scguns.entity.raid;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.config.RaidConfig;

import javax.annotation.Nullable;
import java.util.*;

public class ActiveRaid {
    private static final int BOSS_REVALIDATION_INTERVAL = 100;
    private static final int TARGET_UPDATE_INTERVAL = 40;
    private static final int BOSS_VALIDATION_TICKS = 600;

    private final UUID raidId;
    private final Integer raidLevel;
    private final RaidConfig.RaidData config;
    private final ServerLevel level;
    private final Vec3 spawnCenter;
    private final long startTime;

    private UUID bossUUID;
    private UUID mountUUID;
    private UUID targetPlayerUUID;
    private final Set<UUID> henchmenUUIDs;
    private int spawnTimer;
    private int totalHenchmenSpawned;
    private boolean isActive;
    private boolean bossConfirmed;

    private ServerBossEvent bossBar;
    private int ticksSinceLoad = 0;
    private int ticksSinceLastValidation = 0;
    private int ticksSinceTargetUpdate = 0;
    private int ticksSinceStart = 0;

    public ActiveRaid(Integer raidLevel, RaidConfig.RaidData config, ServerLevel level, Vec3 spawnCenter, long startTime) {
        this.raidId = UUID.randomUUID();
        this.raidLevel = raidLevel;
        this.config = config;
        this.level = level;
        this.spawnCenter = spawnCenter;
        this.startTime = startTime;
        this.henchmenUUIDs = new HashSet<>();
        this.spawnTimer = config.henchmen().spawnIntervalTicks();
        this.totalHenchmenSpawned = 0;
        this.isActive = true;
        this.bossConfirmed = false;
        this.mountUUID = null;
        this.targetPlayerUUID = null;
        this.ticksSinceStart = 0;
        this.ticksSinceLoad = 0;
        this.ticksSinceLastValidation = 0;
        this.ticksSinceTargetUpdate = 0;

        createBossBar();
    }

    public static ActiveRaid restore(RaidSaveData.ActiveRaidData data, RaidConfig.RaidData config, ServerLevel level) {
        ActiveRaid raid = new ActiveRaid(data.raidLevel(), config, level, data.spawnCenter(), data.startTime());
        raid.bossUUID = data.bossUUID();
        raid.mountUUID = data.mountUUID();
        raid.targetPlayerUUID = data.targetPlayerUUID();
        raid.henchmenUUIDs.addAll(data.henchmenUUIDs());
        raid.spawnTimer = data.spawnTimer();
        raid.totalHenchmenSpawned = data.totalSpawned();
        raid.isActive = data.isActive();
        raid.bossConfirmed = false;
        raid.ticksSinceLoad = 0;

        long elapsedTime = level.getGameTime() - data.startTime();
        raid.ticksSinceStart = (int) Math.min(elapsedTime, Integer.MAX_VALUE);

        return raid;
    }

    private void createBossBar() {
        Component title;

        String bossName = config.boss().customName();
        if (bossName != null && bossName.startsWith("translation:")) {
            title = Component.translatable(bossName.substring(12));
        } else if (bossName != null) {
            title = Component.literal(bossName);
        } else {
            title = Component.literal("Raid Boss: " + config.raidId());
        }

        this.bossBar = new ServerBossEvent(title, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
        this.bossBar.setProgress(1.0f);
        this.bossBar.setVisible(true);
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public ServerBossEvent getBossBar() {
        return bossBar;
    }

    public void tick() {
        if (!isActive) return;

        ticksSinceLoad++;
        ticksSinceLastValidation++;
        ticksSinceTargetUpdate++;
        ticksSinceStart++;

        int timeoutMinutes = Config.COMMON.raids.raidTimeoutMinutes.get();
        if (timeoutMinutes > 0) {
            int timeoutTicks = timeoutMinutes * 60 * 20;

            if (ticksSinceStart >= timeoutTicks) {
                announceToNearbyPlayers(
                        Component.translatable("raid.scguns.timeout")
                                .withStyle(ChatFormatting.RED),
                        Math.max(256.0, config.spawnConditions().searchRadius())
                );
                endRaid(false);
                return;
            }
        }

        if (!validateBoss()) return;

        if (ticksSinceLastValidation >= BOSS_REVALIDATION_INTERVAL) {
            revalidateRaidState();
            ticksSinceLastValidation = 0;
        }

        if (ticksSinceTargetUpdate >= TARGET_UPDATE_INTERVAL) {
            updateMobTargets();
            ticksSinceTargetUpdate = 0;
        }

        LivingEntity boss = getBoss();
        if (boss == null || !boss.isAlive()) {
            endRaid(bossConfirmed);
            return;
        }

        if (mountUUID != null) {
            Entity mount = getMount();
            if (mount == null || !mount.isAlive()) {
                mountUUID = null;
            }
        }

        updateBossBar();

        if (level.getGameTime() % 20 == 0) {
            updateBossBarPlayers();
        }

        if (bossConfirmed && spawnTimer > 0) {
            spawnTimer--;
        }
    }

    private void updateMobTargets() {
        ServerPlayer targetPlayer = getTargetPlayer(level);
        if (targetPlayer == null || targetPlayer.isRemoved() || targetPlayer.isSpectator() || targetPlayer.isCreative() || !targetPlayer.isAlive()) {
            targetPlayer = findNewTargetPlayer();
            if (targetPlayer != null) {
                targetPlayerUUID = targetPlayer.getUUID();
            }
        }

        if (targetPlayer == null) return;

        LivingEntity boss = getBoss();
        if (boss instanceof PathfinderMob pathfinderBoss && pathfinderBoss.getTarget() == null) {
            pathfinderBoss.setTarget(targetPlayer);

            if (boss instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin abstractPiglin) {
                try {
                    var brain = abstractPiglin.getBrain();
                    brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT);
                    brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT, targetPlayer.getUUID());
                    brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.UNIVERSAL_ANGER);
                    brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.UNIVERSAL_ANGER, true);
                    brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, targetPlayer);
                    brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_VISIBLE_PLAYER);
                    brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_VISIBLE_PLAYER, targetPlayer);
                    abstractPiglin.setLastHurtByMob(targetPlayer);
                } catch (Exception ignored) {}
            }
        }

        for (UUID henchmanUUID : henchmenUUIDs) {
            Entity entity = level.getEntity(henchmanUUID);
            if (entity instanceof PathfinderMob pathfinderMob && pathfinderMob.getTarget() == null) {
                pathfinderMob.setTarget(targetPlayer);

                if (entity instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin abstractPiglin) {
                    try {
                        var brain = abstractPiglin.getBrain();
                        brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT);
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT, targetPlayer.getUUID());
                        brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.UNIVERSAL_ANGER);
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.UNIVERSAL_ANGER, true);
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, targetPlayer);
                        brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_VISIBLE_PLAYER);
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_VISIBLE_PLAYER, targetPlayer);
                        abstractPiglin.setLastHurtByMob(targetPlayer);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    @Nullable
    private ServerPlayer findNewTargetPlayer() {
        List<ServerPlayer> nearbyPlayers = level.getPlayers(player ->
                !player.isSpectator() &&
                        !player.isCreative() &&
                        player.isAlive() &&
                        player.position().distanceTo(spawnCenter) <= config.spawnConditions().searchRadius()
        );

        if (nearbyPlayers.isEmpty()) return null;

        ServerPlayer closest = null;
        double closestDist = Double.MAX_VALUE;

        for (ServerPlayer player : nearbyPlayers) {
            double dist = player.position().distanceTo(spawnCenter);
            if (dist < closestDist) {
                closestDist = dist;
                closest = player;
            }
        }

        return closest;
    }

    private boolean validateBoss() {
        if (bossUUID == null) {
            endRaid(false);
            return false;
        }

        if (bossConfirmed) return true;

        LivingEntity boss = getBoss();
        if (boss != null && boss.isAlive()) {
            bossConfirmed = true;
            ticksSinceLoad = 0;

            boss.setPos(spawnCenter.x, spawnCenter.y, spawnCenter.z);

            return true;
        }

        if (ticksSinceLoad >= BOSS_VALIDATION_TICKS) {
            endRaid(false);
            return false;
        }

        if (ticksSinceLoad % 20 == 0) {
            net.minecraft.world.level.ChunkPos chunkPos = new net.minecraft.world.level.ChunkPos(
                    new BlockPos((int)spawnCenter.x, (int)spawnCenter.y, (int)spawnCenter.z)
            );
            level.setChunkForced(chunkPos.x, chunkPos.z, true);
        }

        return false;
    }

    private void revalidateRaidState() {
        henchmenUUIDs.removeIf(uuid -> {
            Entity entity = level.getEntity(uuid);
            if (!(entity instanceof LivingEntity livingEntity)) return true;
            return !livingEntity.isAlive();
        });
    }

    public void updateBossBarPlayers() {
        if (bossBar == null) return;

        LivingEntity boss = getBoss();
        if (boss == null || !boss.isAlive()) return;

        List<ServerPlayer> nearbyPlayers = level.getPlayers(player -> {
            if (!player.isAlive() || player.isRemoved() || player.isSpectator()) return false;
            double distance = player.position().distanceTo(boss.position());
            return distance <= 128;
        });

        List<ServerPlayer> currentPlayers = new ArrayList<>(bossBar.getPlayers());
        for (ServerPlayer player : currentPlayers) {
            if (!nearbyPlayers.contains(player) || !player.isAlive() || player.isRemoved()) {
                bossBar.removePlayer(player);
            }
        }

        for (ServerPlayer player : nearbyPlayers) {
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }
    }

    private void updateBossBar() {
        if (bossBar == null) return;

        LivingEntity boss = getBoss();
        if (boss != null && boss.isAlive()) {
            float healthPercent = boss.getHealth() / boss.getMaxHealth();
            bossBar.setProgress(Math.max(0.0f, Math.min(1.0f, healthPercent)));
        } else if (bossConfirmed) {
            bossBar.setProgress(0.0f);
        }
    }

    public void onBossDefeated() {
        endRaid(true);
    }

    public UUID getRaidId() { return raidId; }
    public Integer getRaidLevel() { return raidLevel; }
    public RaidConfig.RaidData getConfig() { return config; }
    public Vec3 getSpawnCenter() { return spawnCenter; }
    public boolean isActive() { return isActive; }
    public long getStartTime() { return startTime; }
    public int getSpawnTimer() { return spawnTimer; }
    public int getTotalHenchmenSpawned() { return totalHenchmenSpawned; }
    public boolean isBossConfirmed() { return bossConfirmed; }

    public void setBossUUID(UUID bossUUID) {
        this.bossUUID = bossUUID;
        this.bossConfirmed = false;
    }

    @Nullable
    public UUID getBossUUID() { return bossUUID; }

    public void setMountUUID(UUID mountUUID) {
        this.mountUUID = mountUUID;
    }

    @Nullable
    public UUID getMountUUID() { return mountUUID; }

    public void setTargetPlayer(UUID playerUUID) {
        this.targetPlayerUUID = playerUUID;
    }

    @Nullable
    public UUID getTargetPlayerUUID() {
        return targetPlayerUUID;
    }

    @Nullable
    public ServerPlayer getTargetPlayer(ServerLevel level) {
        if (targetPlayerUUID == null) return null;
        return level.getServer().getPlayerList().getPlayer(targetPlayerUUID);
    }

    @Nullable
    public LivingEntity getBoss() {
        if (bossUUID == null) return null;
        Entity entity = level.getEntity(bossUUID);
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }

    @Nullable
    public Entity getMount() {
        if (mountUUID == null) return null;
        return level.getEntity(mountUUID);
    }

    public void addHenchman(UUID uuid) {
        henchmenUUIDs.add(uuid);
        totalHenchmenSpawned++;
    }

    public void removeHenchman(UUID uuid) {
        henchmenUUIDs.remove(uuid);
    }

    public int getAliveHenchmenCount() {
        revalidateRaidState();
        return henchmenUUIDs.size();
    }

    public Set<UUID> getHenchmenUUIDs() {
        return new HashSet<>(henchmenUUIDs);
    }

    public boolean canSpawnMoreHenchmen() {
        return getAliveHenchmenCount() < config.henchmen().maxAlive();
    }

    public boolean shouldSpawnHenchmen() {
        return spawnTimer <= 0 && canSpawnMoreHenchmen() && isActive && bossConfirmed;
    }

    public void resetSpawnTimer() {
        spawnTimer = config.henchmen().spawnIntervalTicks();
    }

    public void endRaid(boolean bossDefeated) {
        if (!isActive) return;

        isActive = false;

        if (bossDefeated) {
            announceToNearbyPlayers(Component.translatable("raid.scguns.defeated"), 64);
        } else {
            announceToNearbyPlayers(Component.translatable("raid.scguns.failed"), 64);
        }
        if (bossBar != null) {
            bossBar.setVisible(false);
            bossBar.removeAllPlayers();
        }
        cleanupBoss(bossDefeated);
        cleanupHenchmen(bossDefeated);
        cleanupMount(bossDefeated);
    }

    private void cleanupBoss(boolean wasBossDefeated) {
        if (bossUUID == null) return;

        Entity entity = level.getEntity(bossUUID);
        if (entity instanceof Mob boss) {
            boss.removeTag("RaidBoss");
            boss.removeTag("RaidMember_" + raidId);

            if (!wasBossDefeated && boss.isAlive()) {
                boss.discard();
                //LOGGER.info("Despawned raid boss for failed raid {}", raidId);
            }
        }
    }

    private void cleanupHenchmen(boolean wasBossDefeated) {
        for (UUID uuid : new HashSet<>(henchmenUUIDs)) {
            Entity entity = level.getEntity(uuid);
            if (entity instanceof Mob mob) {
                mob.removeTag("RaidHenchman");
                mob.removeTag("RaidMember_" + raidId);

                if (!wasBossDefeated && mob.isAlive()) {
                    mob.discard();
                }
            }
        }
        henchmenUUIDs.clear();
    }

    private void cleanupMount(boolean wasBossDefeated) {
        if (mountUUID != null) {
            Entity entity = level.getEntity(mountUUID);
            if (entity instanceof Mob mob) {
                mob.removeTag("RaidMount");
                mob.removeTag("RaidMember_" + raidId);

                if (!wasBossDefeated && mob.isAlive()) {
                    mob.discard();
                }
            }
        }
    }

    public void announceToNearbyPlayers(Component message, double radius) {
        List<ServerPlayer> nearbyPlayers = level.getPlayers(player ->
                player.position().distanceTo(spawnCenter) <= radius
        );

        for (ServerPlayer player : nearbyPlayers) {
            player.sendSystemMessage(message);
        }
    }

    public long getRaidDuration() {
        return level.getGameTime() - startTime;
    }

    public int getRemainingTicks() {
        int timeoutMinutes = Config.COMMON.raids.raidTimeoutMinutes.get();
        if (timeoutMinutes <= 0) return -1;

        int timeoutTicks = timeoutMinutes * 60 * 20;
        return Math.max(0, timeoutTicks - ticksSinceStart);
    }

    public int getRemainingMinutes() {
        int remainingTicks = getRemainingTicks();
        if (remainingTicks < 0) return -1;
        return remainingTicks / (60 * 20);
    }

    public void setBossConfirmed(boolean b) {
        this.bossConfirmed = b;
    }
}