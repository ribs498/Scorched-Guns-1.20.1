package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import top.ribs.scguns.event.GunReloadEvent;
import top.ribs.scguns.init.ModSyncedDataKeys;

/**
 * Author: MrCrayfish
 */
public class C2SMessageReload extends PlayMessage<C2SMessageReload>
{
    private boolean reload;

    public C2SMessageReload() {}

    public C2SMessageReload(boolean reload)
    {
        this.reload = reload;
    }

    @Override
    public void encode(C2SMessageReload message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.reload);
    }

    @Override
    public C2SMessageReload decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageReload(buffer.readBoolean());
    }
    @Override
    public void handle(C2SMessageReload message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if (player != null && !player.isSpectator()) {
               // System.out.println("Handling C2SMessageReload: " + message.reload);
                ModSyncedDataKeys.RELOADING.setValue(player, message.reload);

                if (!message.reload) {
                   // System.out.println("Stopping reload for player: " + player.getName().getString());
                    return;
                }

                ItemStack gun = player.getMainHandItem();
                if (MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Pre(player, gun))) {
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                   // System.out.println("Reload event canceled for player: " + player.getName().getString());
                    return;
                }

                MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post(player, gun));
               // System.out.println("Reload event processed for player: " + player.getName().getString());
            }
        });
        context.setHandled(true);
    }

}
