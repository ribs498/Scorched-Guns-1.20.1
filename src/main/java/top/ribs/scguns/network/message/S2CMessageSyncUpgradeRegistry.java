package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import top.ribs.scguns.client.network.ClientPlayHandler;

import java.util.HashMap;
import java.util.Map;

public class S2CMessageSyncUpgradeRegistry extends PlayMessage<S2CMessageSyncUpgradeRegistry> {

    private Map<ResourceLocation, CompoundTag> upgradeData;

    public S2CMessageSyncUpgradeRegistry() {
        this.upgradeData = new HashMap<>();
    }

    public S2CMessageSyncUpgradeRegistry(Map<ResourceLocation, CompoundTag> upgradeData) {
        this.upgradeData = upgradeData;
    }

    @Override
    public void encode(S2CMessageSyncUpgradeRegistry message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.upgradeData.size());
        for (Map.Entry<ResourceLocation, CompoundTag> entry : message.upgradeData.entrySet()) {
            buffer.writeResourceLocation(entry.getKey());
            buffer.writeNbt(entry.getValue());
        }
    }

    @Override
    public S2CMessageSyncUpgradeRegistry decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        Map<ResourceLocation, CompoundTag> upgradeData = new HashMap<>();

        for (int i = 0; i < size; i++) {
            ResourceLocation itemId = buffer.readResourceLocation();
            CompoundTag tag = buffer.readNbt();
            upgradeData.put(itemId, tag);
        }

        return new S2CMessageSyncUpgradeRegistry(upgradeData);
    }

    @Override
    public void handle(S2CMessageSyncUpgradeRegistry message, MessageContext context) {
        context.execute(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPlayHandler.handleSyncUpgradeRegistry(message)));
        context.setHandled(true);
    }

    public Map<ResourceLocation, CompoundTag> getUpgradeData() {
        return upgradeData;
    }
}