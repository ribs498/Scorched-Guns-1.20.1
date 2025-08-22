package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.cache.HotBarrelCache;

public class S2CMessageHotBarrelSync extends PlayMessage<S2CMessageHotBarrelSync> {
    private int hotBarrelLevel;
    private String weaponDescriptionId;

    public S2CMessageHotBarrelSync() {}

    public S2CMessageHotBarrelSync(int hotBarrelLevel, String weaponDescriptionId) {
        this.hotBarrelLevel = hotBarrelLevel;
        this.weaponDescriptionId = weaponDescriptionId;
    }

    @Override
    public void encode(S2CMessageHotBarrelSync message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.hotBarrelLevel);
        buffer.writeUtf(message.weaponDescriptionId);
    }

    @Override
    public S2CMessageHotBarrelSync decode(FriendlyByteBuf buffer) {
        return new S2CMessageHotBarrelSync(buffer.readInt(), buffer.readUtf());
    }

    @Override
    public void handle(S2CMessageHotBarrelSync message, MessageContext context) {
        context.execute(() -> {
            Player player = context.getPlayer();
            if (player != null) {
                ItemStack heldItem = player.getMainHandItem();
                if (heldItem.getItem().getDescriptionId().equals(message.weaponDescriptionId)) {
                    HotBarrelCache.setHotBarrelLevel(player, heldItem, message.hotBarrelLevel);
                }
            }
        });
        context.setHandled(true);
    }
}