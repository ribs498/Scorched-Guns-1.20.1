package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import top.ribs.scguns.client.network.ClientPlayHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.animated.AnimatedGunItem;

public class S2CMessageStopReload extends PlayMessage<S2CMessageStopReload> {
    public S2CMessageStopReload() {}

    @Override
    public void encode(S2CMessageStopReload message, FriendlyByteBuf buffer) {
        // No data to encode
    }

    @Override
    public S2CMessageStopReload decode(FriendlyByteBuf buffer) {
        return new S2CMessageStopReload();
    }

    @Override
    public void handle(S2CMessageStopReload message, MessageContext context) {
        context.execute(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack heldItem = player.getMainHandItem();
                if (heldItem.getItem() instanceof AnimatedGunItem gunItem) {
                    CompoundTag tag = heldItem.getOrCreateTag();
                    tag.putString("scguns:ReloadState", "STOPPING");
                    tag.putBoolean("scguns:IsPlayingReloadStop", true);
                    tag.remove("InReloadLoop");
                    tag.remove("scguns:IsReloading");
                    ModSyncedDataKeys.RELOADING.setValue(player, false);

                    long id = GeoItem.getId(heldItem);
                    AnimationController<GeoAnimatable> animationController = gunItem.getAnimatableInstanceCache()
                            .getManagerForId(id)
                            .getAnimationControllers()
                            .get("controller");

                    if (animationController != null) {
                        animationController.stop();
                        animationController.setAnimationSpeed(1.0);
                        animationController.tryTriggerAnimation(gunItem.isInCarbineMode(heldItem) ? "carbine_reload_stop" : "reload_stop");
                    }
                }
            }
        });
        context.setHandled(true);
    }
}