package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import top.ribs.scguns.client.network.ClientPlayHandler;

import java.util.UUID;

public class S2CMessageStopBeam extends PlayMessage<S2CMessageStopBeam> {
    private UUID playerId;

    public S2CMessageStopBeam() {}

    public S2CMessageStopBeam(UUID playerId) {
        this.playerId = playerId;
    }

    @Override
    public void encode(S2CMessageStopBeam message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.playerId);
    }

    @Override
    public S2CMessageStopBeam decode(FriendlyByteBuf buffer) {
        return new S2CMessageStopBeam(buffer.readUUID());
    }

    @Override
    public void handle(S2CMessageStopBeam message, MessageContext context) {
        context.execute(() -> ClientPlayHandler.handleStopBeam(message));
        context.setHandled(true);
    }

    public UUID getPlayerId() {
        return playerId;
    }
}