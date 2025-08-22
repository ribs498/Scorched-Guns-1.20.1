package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.BlueprintItem;

public class C2SMessageSetBlueprintRecipe extends PlayMessage<C2SMessageSetBlueprintRecipe> {
    private InteractionHand hand;
    private String recipeId;

    public C2SMessageSetBlueprintRecipe() {}

    public C2SMessageSetBlueprintRecipe(InteractionHand hand, String recipeId) {
        this.hand = hand;
        this.recipeId = recipeId;
    }

    @Override
    public void encode(C2SMessageSetBlueprintRecipe message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.recipeId);
    }

    @Override
    public C2SMessageSetBlueprintRecipe decode(FriendlyByteBuf buffer) {
        return new C2SMessageSetBlueprintRecipe(buffer.readEnum(InteractionHand.class), buffer.readUtf());
    }

    @Override
    public void handle(C2SMessageSetBlueprintRecipe message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if (player != null) {
                ItemStack blueprint = player.getItemInHand(message.hand);

                if (blueprint.getItem() instanceof BlueprintItem) {
                    blueprint.getOrCreateTag().putString("ActiveRecipe", message.recipeId);
                }
            }
        });
        context.setHandled(true);
    }
}