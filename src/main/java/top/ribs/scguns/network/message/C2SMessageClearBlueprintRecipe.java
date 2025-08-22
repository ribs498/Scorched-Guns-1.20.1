package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.BlueprintItem;

public class C2SMessageClearBlueprintRecipe extends PlayMessage<C2SMessageClearBlueprintRecipe> {
    private InteractionHand hand;

    public C2SMessageClearBlueprintRecipe() {}

    public C2SMessageClearBlueprintRecipe(InteractionHand hand) {
        this.hand = hand;
    }

    @Override
    public void encode(C2SMessageClearBlueprintRecipe message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
    }

    @Override
    public C2SMessageClearBlueprintRecipe decode(FriendlyByteBuf buffer) {
        return new C2SMessageClearBlueprintRecipe(buffer.readEnum(InteractionHand.class));
    }

    @Override
    public void handle(C2SMessageClearBlueprintRecipe message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if (player != null) {
                ItemStack blueprint = player.getItemInHand(message.hand);

                if (blueprint.getItem() instanceof BlueprintItem) {
                    if (blueprint.hasTag() && blueprint.getTag().contains("ActiveRecipe")) {
                        blueprint.getTag().remove("ActiveRecipe");

                        if (blueprint.getTag().isEmpty()) {
                            blueprint.setTag(null);
                        }
                    }
                }
            }
        });
        context.setHandled(true);
    }
}