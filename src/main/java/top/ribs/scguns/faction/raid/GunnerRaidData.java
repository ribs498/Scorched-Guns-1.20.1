package top.ribs.scguns.faction.raid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class GunnerRaidData extends SavedData {
    private static final String DATA_NAME = "GunnerRaidData";
    private int nextTick = 0;

    public static GunnerRaidData load(CompoundTag tag) {
        GunnerRaidData data = new GunnerRaidData();
        data.nextTick = tag.getInt("NextRaidTick");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("NextRaidTick", this.nextTick);
        return tag;
    }

    public static GunnerRaidData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(GunnerRaidData::load, GunnerRaidData::new, DATA_NAME);
    }

    public int getNextTick() {
        return this.nextTick;
    }

    public void setNextTick(int nextTick) {
        this.nextTick = nextTick;
        this.setDirty();
    }
}