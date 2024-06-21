package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.common.network.ServerPlayHandler;

public class C2SMessageBurst extends PlayMessage<C2SMessageBurst> {

    @Override
    public void encode(C2SMessageBurst message, FriendlyByteBuf buffer) {
    }

    @Override
    public C2SMessageBurst decode(FriendlyByteBuf buffer) {
        return new C2SMessageBurst();
    }

    @Override
    public void handle(C2SMessageBurst message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null)
            {
                ServerPlayHandler.handleBurst(player);
            }
        });
        context.setHandled(true);
    }

}
