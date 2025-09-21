package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import top.ribs.scguns.client.handler.GunRenderingHandler;

public class S2CMessageEntityMuzzleFlash extends PlayMessage<S2CMessageEntityMuzzleFlash> {
    private int entityId;
    private float randomValue;

    public S2CMessageEntityMuzzleFlash() {}

    public S2CMessageEntityMuzzleFlash(int entityId, float randomValue) {
        this.entityId = entityId;
        this.randomValue = randomValue;
    }

    @Override
    public void encode(S2CMessageEntityMuzzleFlash message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeFloat(message.randomValue);
    }

    @Override
    public S2CMessageEntityMuzzleFlash decode(FriendlyByteBuf buffer) {
        return new S2CMessageEntityMuzzleFlash(buffer.readInt(), buffer.readFloat());
    }

    @Override
    public void handle(S2CMessageEntityMuzzleFlash message, MessageContext context) {
        context.execute(() -> {
            GunRenderingHandler.get().showMuzzleFlashForPlayer(message.entityId);
            GunRenderingHandler.entityIdToRandomValue.put(message.entityId, message.randomValue);
        });
        context.setHandled(true);
    }
}