package top.ribs.scguns.event;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.BoundingBoxManager;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageReload;

import static top.ribs.scguns.ScorchedGuns.LOGGER;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PausedGameEvent {
    private static boolean wasGamePaused = false;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        boolean isPaused = mc.isPaused();
        if (!wasGamePaused && isPaused && mc.player != null) {
            if (mc.level != null) {
                TemporaryLightManager.cleanup(mc.level);
            }
            ItemStack stack = mc.player.getMainHandItem();
            if (!(stack.getItem() instanceof GunItem)) {
                wasGamePaused = isPaused;
                return;
            }

            CompoundTag nbtCompound = stack.getOrCreateTag();
            if (!nbtCompound.getBoolean("scguns:IsReloading")) {
                wasGamePaused = true;
                return;
            }
            Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
            boolean isManualReload = gun.getReloads().getReloadType() == ReloadType.MANUAL;
            if (isManualReload && nbtCompound.contains(AnimatedGunItem.RELOAD_STATE)) {
                wasGamePaused = true;
                return;
            }
            if (stack.getItem() instanceof AnimatedGunItem gunItem) {
                if (!isManualReload) {
                    gunItem.cleanupReloadState(nbtCompound);

                    AnimationController<GeoAnimatable> controller = gunItem
                            .getAnimatableInstanceCache()
                            .getManagerForId(GeoItem.getId(stack))
                            .getAnimationControllers()
                            .get("controller");

                    if (controller != null) {
                        controller.forceAnimationReset();
                        controller.tryTriggerAnimation(gunItem.isInCarbineMode(stack) ?
                                "carbine_idle" : "idle");
                    }
                    ModSyncedDataKeys.RELOADING.setValue(mc.player, false);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                }
            }
        }

        wasGamePaused = isPaused;
    }
    @SubscribeEvent
    public static void onWorldUnload(net.minecraftforge.event.level.LevelEvent.Unload event) {
        BoundingBoxManager.clearDynamicBoxCache();
        TemporaryLightManager.cleanup((Level) event.getLevel());
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        BoundingBoxManager.clearDynamicBoxCache();
        if (event.getPlayer() != null) {
            TemporaryLightManager.cleanup(event.getPlayer().level());
        }
    }

    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save event) {
        if (event.getLevel().isClientSide()) {
            TemporaryLightManager.cleanup((Level) event.getLevel());
        }
    }
}