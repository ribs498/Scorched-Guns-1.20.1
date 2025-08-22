package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import top.ribs.scguns.client.network.ClientPlayHandler;

public class S2CMessageStopReload extends PlayMessage<S2CMessageStopReload> {
    public S2CMessageStopReload() {}

    @Override
    public void encode(S2CMessageStopReload message, FriendlyByteBuf buffer) {
        // No data to encode
    }

    @Override
    public S2CMessageStopReload decode(FriendlyByteBuf buffer) {
        return new S2CMessageStopReload();
    }

    @Override
    public void handle(S2CMessageStopReload message, MessageContext context) {
        context.execute(() -> ClientPlayHandler.handleStopReload(message));
        context.setHandled(true);
    }
}