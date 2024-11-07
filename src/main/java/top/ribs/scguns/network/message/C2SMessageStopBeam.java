package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.client.handler.BeamHandler;
import top.ribs.scguns.common.network.ServerPlayHandler;

public class C2SMessageStopBeam extends PlayMessage<C2SMessageStopBeam> {
    public C2SMessageStopBeam() {}

    @Override
    public void encode(C2SMessageStopBeam message, FriendlyByteBuf buffer) {
        // No data needs to be encoded for this message
    }

    @Override
    public C2SMessageStopBeam decode(FriendlyByteBuf buffer) {
        return new C2SMessageStopBeam();
    }

    @Override
    public void handle(C2SMessageStopBeam message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if (player != null) {
                ServerPlayHandler.handleStopBeam(player);
            }
        });
        context.setHandled(true);
    }
}