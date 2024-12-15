package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.network.ClientPlayHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;

public class S2CMessageReload extends PlayMessage<S2CMessageReload> {
    private boolean reloading;

    public S2CMessageReload() {}

    public S2CMessageReload(boolean reloading) {
        this.reloading = reloading;
    }

    @Override
    public void encode(S2CMessageReload message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.reloading);
    }

    @Override
    public S2CMessageReload decode(FriendlyByteBuf buffer) {
        return new S2CMessageReload(buffer.readBoolean());
    }

    @Override
    public void handle(S2CMessageReload message, MessageContext context) {
        context.execute(() -> {
            ClientPlayHandler.handleReloadState(message.reloading);
        });
        context.setHandled(true);
    }

    public boolean isReloading() {
        return this.reloading;
    }
}