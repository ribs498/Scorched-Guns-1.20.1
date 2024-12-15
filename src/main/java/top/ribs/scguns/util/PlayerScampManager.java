package top.ribs.scguns.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerScampManager {
    private static final Map<UUID, PlayerScampData> PLAYER_DATA = new HashMap<>();

    public static class PlayerScampData {
        private UUID linkedScampId;
        private BlockPos containerPos;
        private boolean isDirty = false;  // For save/load handling

        public UUID getLinkedScampId() {
            return linkedScampId;
        }

        public void setLinkedScampId(UUID scampId) {
            this.linkedScampId = scampId;
            this.isDirty = true;
        }

        public BlockPos getContainerPos() {
            return containerPos;
        }

        public void setContainerPos(BlockPos pos) {
            this.containerPos = pos;
            this.isDirty = true;
        }

        public void saveToNBT(CompoundTag tag) {
            if (linkedScampId != null) {
                tag.putUUID("LinkedScampId", linkedScampId);
            }
            if (containerPos != null) {
                tag.putInt("ContainerX", containerPos.getX());
                tag.putInt("ContainerY", containerPos.getY());
                tag.putInt("ContainerZ", containerPos.getZ());
            }
        }

        public void loadFromNBT(CompoundTag tag) {
            if (tag.hasUUID("LinkedScampId")) {
                linkedScampId = tag.getUUID("LinkedScampId");
            }
            if (tag.contains("ContainerX")) {
                containerPos = new BlockPos(
                        tag.getInt("ContainerX"),
                        tag.getInt("ContainerY"),
                        tag.getInt("ContainerZ")
                );
            }
        }
    }

    public static PlayerScampData getOrCreatePlayerData(Player player) {
        return PLAYER_DATA.computeIfAbsent(player.getUUID(), k -> new PlayerScampData());
    }

    public static void init() {
        PLAYER_DATA.clear();
    }

    // Save data to player's NBT
    public static void savePlayerData(Player player) {
        PlayerScampData data = PLAYER_DATA.get(player.getUUID());
        if (data != null && data.isDirty) {
            CompoundTag persistentData = player.getPersistentData();
            CompoundTag scampData = new CompoundTag();
            data.saveToNBT(scampData);
            persistentData.put("ScampManagerData", scampData);
            data.isDirty = false;
        }
    }

    // Load data from player's NBT
    public static void loadPlayerData(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        if (persistentData.contains("ScampManagerData")) {
            PlayerScampData data = new PlayerScampData();
            data.loadFromNBT(persistentData.getCompound("ScampManagerData"));
            PLAYER_DATA.put(player.getUUID(), data);
        }
    }
}