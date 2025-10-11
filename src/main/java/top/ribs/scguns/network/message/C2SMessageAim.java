package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;

public class C2SMessageAim extends PlayMessage<C2SMessageAim>
{
    private boolean aiming;

    public C2SMessageAim() {}

    public C2SMessageAim(boolean aiming)
    {
        this.aiming = aiming;
    }

    @Override
    public void encode(C2SMessageAim message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.aiming);
    }

    @Override
    public C2SMessageAim decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageAim(buffer.readBoolean());
    }
    @Override
    public void handle(C2SMessageAim message, MessageContext context) {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                boolean currentlyReloading = ModSyncedDataKeys.RELOADING.getValue(player);

                ItemStack heldItem = player.getMainHandItem();
                boolean inCriticalPhase = false;

                if(heldItem.getItem() instanceof GunItem) {
                    CompoundTag tag = heldItem.getOrCreateTag();
                    inCriticalPhase = tag.getBoolean("InCriticalReloadPhase");

                    Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
                    boolean isManualReload = gun.getReloads().getReloadType() == ReloadType.MANUAL;
                    String reloadState = tag.getString("scguns:ReloadState");

                    if(currentlyReloading || inCriticalPhase ||
                            (isManualReload && (!reloadState.isEmpty() && !reloadState.equals("NONE")))) {
                        ModSyncedDataKeys.AIMING.setValue(player, false);
                        return;
                    }
                }

                if(!currentlyReloading && !inCriticalPhase) {
                    ModSyncedDataKeys.AIMING.setValue(player, message.aiming);
                }
            }
        });
        context.setHandled(true);
    }
}