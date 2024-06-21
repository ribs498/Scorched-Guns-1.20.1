package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessageUnload extends PlayMessage<C2SMessageUnload>
{
    @Override
    public void encode(C2SMessageUnload message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageUnload decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageUnload();
    }

    @Override
    public void handle(C2SMessageUnload message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.handleUnload(player);
            }
        });
        context.setHandled(true);
    }
}
