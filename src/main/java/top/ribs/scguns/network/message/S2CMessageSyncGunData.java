package top.ribs.scguns.network.message;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class S2CMessageSyncGunData {
    private final Map<ResourceLocation, CompoundTag> gunData;

    public S2CMessageSyncGunData(Map<ResourceLocation, CompoundTag> gunData) {
        this.gunData = gunData;
    }

    public S2CMessageSyncGunData(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        this.gunData = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            CompoundTag tag = buffer.readNbt();
            this.gunData.put(id, tag);
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.gunData.size());
        for (Map.Entry<ResourceLocation, CompoundTag> entry : this.gunData.entrySet()) {
            buffer.writeResourceLocation(entry.getKey());
            buffer.writeNbt(entry.getValue());
        }
    }

    public Map<ResourceLocation, CompoundTag> getGunData() {
        return gunData;
    }
}
