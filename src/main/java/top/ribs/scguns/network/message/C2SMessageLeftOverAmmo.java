package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessageLeftOverAmmo extends PlayMessage<C2SMessageLeftOverAmmo>
{
    @Override
    public void encode(C2SMessageLeftOverAmmo message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageLeftOverAmmo decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageLeftOverAmmo();
    }

    @Override
    public void handle(C2SMessageLeftOverAmmo message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.handleExtraAmmo(player);
            }
        });
        context.setHandled(true);
    }
}
