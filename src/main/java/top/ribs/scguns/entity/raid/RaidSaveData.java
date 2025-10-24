package top.ribs.scguns.entity.raid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class RaidSaveData extends SavedData {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String DATA_NAME = "scguns_raid_save_data";

    private final Map<UUID, ActiveRaidData> activeRaidData = new HashMap<>();
    private final Map<ResourceLocation, ScheduledRaidData> scheduledRaids = new HashMap<>();
    private final Map<ResourceLocation, Long> lastRaidDayByDimension = new HashMap<>();

    public record ActiveRaidData(
            UUID raidId,
            String configRaidId,
            @Nullable Integer raidLevel,
            Vec3 spawnCenter,
            long startTime,
            UUID bossUUID,
            @Nullable UUID mountUUID,
            @Nullable UUID targetPlayerUUID,
            Set<UUID> henchmenUUIDs,
            int spawnTimer,
            int totalSpawned,
            boolean isActive,
            boolean bossConfirmed
    ) {
        public ActiveRaidData(UUID raidId, String configRaidId, @Nullable Integer raidLevel, Vec3 spawnCenter,
                              long startTime, UUID bossUUID, @Nullable UUID mountUUID, @Nullable UUID targetPlayerUUID,
                              Set<UUID> henchmenUUIDs, int spawnTimer, int totalSpawned,
                              boolean isActive, boolean bossConfirmed) {
            this.raidId = raidId;
            this.configRaidId = configRaidId;
            this.raidLevel = raidLevel;
            this.spawnCenter = spawnCenter;
            this.startTime = startTime;
            this.bossUUID = bossUUID;
            this.mountUUID = mountUUID;
            this.targetPlayerUUID = targetPlayerUUID;
            this.henchmenUUIDs = new HashSet<>(henchmenUUIDs);
            this.spawnTimer = spawnTimer;
            this.totalSpawned = totalSpawned;
            this.isActive = isActive;
            this.bossConfirmed = bossConfirmed;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("RaidId", raidId);
            tag.putString("ConfigRaidId", configRaidId);

            if (raidLevel != null) {
                tag.putInt("RaidLevel", raidLevel);
            }

            tag.putDouble("SpawnX", spawnCenter.x);
            tag.putDouble("SpawnY", spawnCenter.y);
            tag.putDouble("SpawnZ", spawnCenter.z);
            tag.putLong("StartTime", startTime);
            tag.putUUID("BossUUID", bossUUID);

            if (mountUUID != null) {
                tag.putUUID("MountUUID", mountUUID);
            }

            if (targetPlayerUUID != null) {
                tag.putUUID("TargetPlayerUUID", targetPlayerUUID);
            }

            ListTag henchmenList = new ListTag();
            for (UUID uuid : henchmenUUIDs) {
                CompoundTag henchmanTag = new CompoundTag();
                henchmanTag.putUUID("UUID", uuid);
                henchmenList.add(henchmanTag);
            }
            tag.put("Henchmen", henchmenList);

            tag.putInt("SpawnTimer", spawnTimer);
            tag.putInt("TotalSpawned", totalSpawned);
            tag.putBoolean("IsActive", isActive);
            tag.putBoolean("BossConfirmed", bossConfirmed);

            return tag;
        }

        @Nullable
        public static ActiveRaidData load(CompoundTag tag) {
            try {
                UUID raidId = tag.getUUID("RaidId");
                String configRaidId = tag.getString("ConfigRaidId");

                Integer raidLevel = null;
                if (tag.contains("RaidLevel")) {
                    raidLevel = tag.getInt("RaidLevel");
                }

                Vec3 spawnCenter = new Vec3(tag.getDouble("SpawnX"), tag.getDouble("SpawnY"), tag.getDouble("SpawnZ"));
                long startTime = tag.getLong("StartTime");
                UUID bossUUID = tag.getUUID("BossUUID");
                UUID mountUUID = tag.contains("MountUUID") ? tag.getUUID("MountUUID") : null;
                UUID targetPlayerUUID = tag.contains("TargetPlayerUUID") ? tag.getUUID("TargetPlayerUUID") : null;

                Set<UUID> henchmenUUIDs = new HashSet<>();
                ListTag henchmenList = tag.getList("Henchmen", Tag.TAG_COMPOUND);
                for (int i = 0; i < henchmenList.size(); i++) {
                    CompoundTag henchmanTag = henchmenList.getCompound(i);
                    henchmenUUIDs.add(henchmanTag.getUUID("UUID"));
                }

                int spawnTimer = tag.getInt("SpawnTimer");
                int totalSpawned = tag.getInt("TotalSpawned");
                boolean isActive = tag.getBoolean("IsActive");
                boolean bossConfirmed = tag.getBoolean("BossConfirmed");

                return new ActiveRaidData(raidId, configRaidId, raidLevel, spawnCenter, startTime, bossUUID,
                        mountUUID, targetPlayerUUID, henchmenUUIDs, spawnTimer, totalSpawned, isActive, bossConfirmed);
            } catch (Exception e) {
                LOGGER.warn("Failed to load active raid data: {}", e.getMessage());
                return null;
            }
        }
    }

    public record ScheduledRaidData(
            ResourceLocation dimension,
            UUID targetPlayerUUID,
            String raidId,
            long scheduledDay
    ) {
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Dimension", dimension.toString());
            tag.putUUID("PlayerUUID", targetPlayerUUID);
            tag.putString("RaidId", raidId);
            tag.putLong("ScheduledDay", scheduledDay);
            return tag;
        }

        @Nullable
        public static ScheduledRaidData load(CompoundTag tag) {
            try {
                ResourceLocation dimension = new ResourceLocation(tag.getString("Dimension"));
                UUID playerUUID = tag.getUUID("PlayerUUID");
                String raidId = tag.getString("RaidId");
                long scheduledDay = tag.getLong("ScheduledDay");

                return new ScheduledRaidData(dimension, playerUUID, raidId, scheduledDay);
            } catch (Exception e) {
                LOGGER.warn("Failed to load scheduled raid data: {}", e.getMessage());
                return null;
            }
        }
    }

    public static RaidSaveData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                RaidSaveData::load,
                RaidSaveData::new,
                DATA_NAME
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag activeRaidsList = new ListTag();
        for (ActiveRaidData data : activeRaidData.values()) {
            activeRaidsList.add(data.save());
        }
        tag.put("ActiveRaids", activeRaidsList);

        ListTag scheduledRaidsList = new ListTag();
        for (ScheduledRaidData data : scheduledRaids.values()) {
            scheduledRaidsList.add(data.save());
        }
        tag.put("ScheduledRaids", scheduledRaidsList);

        CompoundTag lastRaidDaysTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Long> entry : lastRaidDayByDimension.entrySet()) {
            lastRaidDaysTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("LastRaidDays", lastRaidDaysTag);

        return tag;
    }

    public static RaidSaveData load(CompoundTag tag) {
        RaidSaveData data = new RaidSaveData();

        if (tag.contains("ActiveRaids")) {
            ListTag activeRaidsList = tag.getList("ActiveRaids", Tag.TAG_COMPOUND);
            for (int i = 0; i < activeRaidsList.size(); i++) {
                ActiveRaidData raidData = ActiveRaidData.load(activeRaidsList.getCompound(i));
                if (raidData != null) {
                    data.activeRaidData.put(raidData.raidId, raidData);
                }
            }
        }

        if (tag.contains("ScheduledRaids")) {
            ListTag scheduledRaidsList = tag.getList("ScheduledRaids", Tag.TAG_COMPOUND);
            for (int i = 0; i < scheduledRaidsList.size(); i++) {
                ScheduledRaidData raidData = ScheduledRaidData.load(scheduledRaidsList.getCompound(i));
                if (raidData != null) {
                    data.scheduledRaids.put(raidData.dimension, raidData);
                }
            }
        }

        if (tag.contains("LastRaidDays")) {
            CompoundTag lastRaidDaysTag = tag.getCompound("LastRaidDays");
            for (String key : lastRaidDaysTag.getAllKeys()) {
                try {
                    ResourceLocation dimension = new ResourceLocation(key);
                    long lastDay = lastRaidDaysTag.getLong(key);
                    data.lastRaidDayByDimension.put(dimension, lastDay);
                } catch (Exception e) {
                    LOGGER.warn("Failed to load last raid day for dimension: {}", key);
                }
            }
        }

        return data;
    }

    public void saveActiveRaid(ActiveRaid raid) {
        ActiveRaidData data = new ActiveRaidData(
                raid.getRaidId(),
                raid.getConfig().raidId(),
                raid.getRaidLevel(),
                raid.getSpawnCenter(),
                raid.getStartTime(),
                raid.getBossUUID(),
                raid.getMountUUID(),
                raid.getTargetPlayerUUID(),
                raid.getHenchmenUUIDs(),
                raid.getSpawnTimer(),
                raid.getTotalHenchmenSpawned(),
                raid.isActive(),
                raid.isBossConfirmed()
        );
        activeRaidData.put(raid.getRaidId(), data);
        setDirty();
    }

    public void removeActiveRaid(UUID raidId) {
        if (activeRaidData.remove(raidId) != null) {
            setDirty();
        }
    }

    public Collection<ActiveRaidData> getActiveRaidData() {
        return new ArrayList<>(activeRaidData.values());
    }

    public void scheduleRaid(ResourceLocation dimension, ServerPlayer player, String raidId, long day) {
        ScheduledRaidData data = new ScheduledRaidData(dimension, player.getUUID(), raidId, day);
        scheduledRaids.put(dimension, data);
        setDirty();
    }

    @Nullable
    public ScheduledRaidData getScheduledRaid(ResourceLocation dimension) {
        return scheduledRaids.get(dimension);
    }

    public void removeScheduledRaid(ResourceLocation dimension) {
        if (scheduledRaids.remove(dimension) != null) {
            setDirty();
        }
    }

    public void setLastRaidDay(ResourceLocation dimension, long day) {
        lastRaidDayByDimension.put(dimension, day);
        setDirty();
    }

    public long getLastRaidDay(ResourceLocation dimension) {
        return lastRaidDayByDimension.getOrDefault(dimension, -1000L);
    }

    public boolean canScheduleRaid(ResourceLocation dimension, long currentDay, int minDaysBetween) {
        long lastRaidDay = getLastRaidDay(dimension);
        return currentDay - lastRaidDay >= minDaysBetween;
    }

    public void cleanupInvalidRaids(ServerLevel level) {
        Iterator<Map.Entry<UUID, ActiveRaidData>> iterator = activeRaidData.entrySet().iterator();
        int removed = 0;

        while (iterator.hasNext()) {
            Map.Entry<UUID, ActiveRaidData> entry = iterator.next();
            ActiveRaidData data = entry.getValue();

            if (!data.isActive) {
                iterator.remove();
                removed++;
                continue;
            }

            if (level.getEntity(data.bossUUID) == null && data.bossConfirmed) {
                iterator.remove();
                removed++;
            }
        }

        if (removed > 0) {
            setDirty();
        }
    }
}