package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class C2SMessageChargeSync extends PlayMessage<C2SMessageChargeSync> {
    private float chargeProgress;

    public C2SMessageChargeSync() {}

    public C2SMessageChargeSync(float chargeProgress) {
        this.chargeProgress = chargeProgress;
    }

    @Override
    public void encode(C2SMessageChargeSync message, FriendlyByteBuf buffer) {
        buffer.writeFloat(message.chargeProgress);
    }

    @Override
    public C2SMessageChargeSync decode(FriendlyByteBuf buffer) {
        return new C2SMessageChargeSync(buffer.readFloat());
    }

    @Override
    public void handle(C2SMessageChargeSync message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if(player != null) {
                player.getPersistentData().putFloat("ChargeProgress", message.chargeProgress);
            }
        });
        context.setHandled(true);
    }

    public float getChargeProgress() {
        return this.chargeProgress;
    }
}