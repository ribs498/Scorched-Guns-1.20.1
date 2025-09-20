package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import top.ribs.scguns.client.network.ClientPlayHandler;

public class S2CMessageDualWieldShotCount extends PlayMessage<S2CMessageDualWieldShotCount> {
    private int entityId;
    private int shotCount;

    public S2CMessageDualWieldShotCount() {}

    public S2CMessageDualWieldShotCount(int entityId, int shotCount) {
        this.entityId = entityId;
        this.shotCount = shotCount;
    }

    @Override
    public void encode(S2CMessageDualWieldShotCount message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeInt(message.shotCount);
    }

    @Override
    public S2CMessageDualWieldShotCount decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        int shotCount = buffer.readInt();
        return new S2CMessageDualWieldShotCount(entityId, shotCount);
    }

    @Override
    public void handle(S2CMessageDualWieldShotCount message, MessageContext context) {
        context.execute(() -> ClientPlayHandler.handleMessageDualWieldShotCount(message));
        context.setHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public int getShotCount() {
        return this.shotCount;
    }
}