package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.common.exosuit.ExoSuitFlightHandler;
import top.ribs.scguns.common.exosuit.ExoSuitPowerManager;

public class C2SMessageJetpackState extends PlayMessage<C2SMessageJetpackState> {
    private boolean isActive;

    public C2SMessageJetpackState() {}

    public C2SMessageJetpackState(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public void encode(C2SMessageJetpackState message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isActive);
    }

    @Override
    public C2SMessageJetpackState decode(FriendlyByteBuf buffer) {
        return new C2SMessageJetpackState(buffer.readBoolean());
    }

    @Override
    public void handle(C2SMessageJetpackState message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if (player != null && !player.isSpectator()) {
                if (ExoSuitPowerManager.isPowerEnabled(player, "utility")) {
                    ExoSuitFlightHandler.setJetpackActive(player, message.isActive);
                } else {
                    ExoSuitFlightHandler.setJetpackActive(player, false);
                }
            }
        });
        context.setHandled(true);
    }
}