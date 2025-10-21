package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.client.handler.GunRenderingHandler;

public class S2CMessageEntityMuzzleFlash extends PlayMessage<S2CMessageEntityMuzzleFlash> {
    private int entityId;
    private float randomValue;
    private Vec3 flashPosition;
    private boolean useEnchantedTexture;

    public S2CMessageEntityMuzzleFlash() {
        this.flashPosition = Vec3.ZERO;
        this.useEnchantedTexture = true;
    }

    // Updated constructor
    public S2CMessageEntityMuzzleFlash(int entityId, float randomValue, Vec3 flashPosition, boolean useEnchantedTexture) {
        this.entityId = entityId;
        this.randomValue = randomValue;
        this.flashPosition = flashPosition;
        this.useEnchantedTexture = useEnchantedTexture;
    }

    @Override
    public void encode(S2CMessageEntityMuzzleFlash message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeFloat(message.randomValue);
        buffer.writeDouble(message.flashPosition.x);
        buffer.writeDouble(message.flashPosition.y);
        buffer.writeDouble(message.flashPosition.z);
        buffer.writeBoolean(message.useEnchantedTexture);
    }

    @Override
    public S2CMessageEntityMuzzleFlash decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        float randomValue = buffer.readFloat();
        Vec3 flashPosition = new Vec3(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
        boolean useEnchantedTexture = buffer.readBoolean();
        return new S2CMessageEntityMuzzleFlash(entityId, randomValue, flashPosition, useEnchantedTexture);
    }

    @Override
    public void handle(S2CMessageEntityMuzzleFlash message, MessageContext context) {
        context.execute(() -> {
            GunRenderingHandler.get().showMuzzleFlashForPlayer(message.entityId);
            GunRenderingHandler.entityIdToRandomValue.put(message.entityId, message.randomValue);
            GunRenderingHandler.entityIdToFlashPosition.put(message.entityId, message.flashPosition);
            GunRenderingHandler.entityIdToUseEnchantedTexture.put(message.entityId, message.useEnchantedTexture);
        });
        context.setHandled(true);
    }
}