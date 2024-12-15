package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.animated.AnimatedGunItem;

public class C2SMessageManualReloadEnd extends PlayMessage<C2SMessageManualReloadEnd> {
    public C2SMessageManualReloadEnd() {}

    @Override
    public void encode(C2SMessageManualReloadEnd message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageManualReloadEnd decode(FriendlyByteBuf buffer) {
        return new C2SMessageManualReloadEnd();
    }

    @Override
    public void handle(C2SMessageManualReloadEnd message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if (player != null && !player.isSpectator()) {
                ItemStack heldItem = player.getMainHandItem();
                if (!(heldItem.getItem() instanceof AnimatedGunItem)) {
                    return;
                }

                CompoundTag tag = heldItem.getOrCreateTag();
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                tag.remove("scguns:IsReloading");
            }
        });
        context.setHandled(true);
    }
}