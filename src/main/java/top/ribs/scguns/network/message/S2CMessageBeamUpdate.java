package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.client.network.ClientPlayHandler;

import java.util.UUID;

public class S2CMessageBeamUpdate extends PlayMessage<S2CMessageBeamUpdate> {

    private UUID playerId;
    private Vec3 startPos;
    private Vec3 endPos;

    // Default constructor required for decoding
    public S2CMessageBeamUpdate() {}

    // Constructor to initialize the beam update message
    public S2CMessageBeamUpdate(UUID playerId, Vec3 startPos, Vec3 endPos) {
        this.playerId = playerId;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    // Encode method to write data to the packet buffer
    @Override
    public void encode(S2CMessageBeamUpdate message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.playerId);
        buffer.writeDouble(message.startPos.x);
        buffer.writeDouble(message.startPos.y);
        buffer.writeDouble(message.startPos.z);
        buffer.writeDouble(message.endPos.x);
        buffer.writeDouble(message.endPos.y);
        buffer.writeDouble(message.endPos.z);
    }

    // Decode method to read data from the packet buffer
    @Override
    public S2CMessageBeamUpdate decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        Vec3 startPos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        Vec3 endPos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        return new S2CMessageBeamUpdate(playerId, startPos, endPos);
    }

    // Handle the message on the client side
    @Override
    public void handle(S2CMessageBeamUpdate message, MessageContext context) {
        context.execute(() -> ClientPlayHandler.handleBeamUpdate(message));
        context.setHandled(true);
    }

    // Getters
    public UUID getPlayerId() { return playerId; }
    public Vec3 getStartPos() { return startPos; }
    public Vec3 getEndPos() { return endPos; }
}
