package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import top.ribs.scguns.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessagePreFireSound extends PlayMessage<C2SMessagePreFireSound>
{

    public C2SMessagePreFireSound() {}

    public C2SMessagePreFireSound(Player player)
    {
    }

    @Override
    public void encode(C2SMessagePreFireSound message, FriendlyByteBuf buffer)
    {

    }

    @Override
    public C2SMessagePreFireSound decode(FriendlyByteBuf buffer)
    {
        return new C2SMessagePreFireSound();
    }

    @Override
    public void handle(C2SMessagePreFireSound message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null)
            {
                ServerPlayHandler.handlePreFireSound(player);
            }
        });
        context.setHandled(true);
    }
}