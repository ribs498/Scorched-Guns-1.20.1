package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import top.ribs.scguns.Config;
import top.ribs.scguns.attributes.SCAttributes;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.event.GunReloadEvent;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.*;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import java.util.Objects;

public class ReloadHandler {
    private static ReloadHandler instance;

    public static ReloadHandler get() {
        if (instance == null) {
            instance = new ReloadHandler();
        }
        return instance;
    }

    private int startReloadTick;
    private int reloadTimer;
    private int prevReloadTimer;
    private int reloadingSlot;

    private ReloadHandler() {
    }

    public static void loaded(Player player) {
        Item item = player.getMainHandItem().getItem();
        if (item instanceof GunItem gunItem) {
            ItemStack stack = player.getMainHandItem();
            CompoundTag tag = stack.getOrCreateTag();
            Gun gun = gunItem.getModifiedGun(stack);
            if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                if (tag.getBoolean("scguns:ReloadComplete")) {
                    if (!tag.getBoolean("scguns:IsPlayingReloadStop")) {
                        tag.putBoolean("scguns:IsPlayingReloadStop", true);
                        tag.remove("InCriticalReloadPhase");
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        if (item instanceof AnimatedGunItem animatedGun) {
                            long id = GeoItem.getId(stack);
                            AnimationController<GeoAnimatable> animationController = animatedGun
                                    .getAnimatableInstanceCache()
                                    .getManagerForId(id)
                                    .getAnimationControllers()
                                    .get("controller");

                            if (animationController != null) {
                                if (animatedGun.isInCarbineMode(stack)) {
                                    animationController.tryTriggerAnimation("carbine_reload_stop");
                                } else {
                                    animationController.tryTriggerAnimation("reload_stop");
                                }
                            }
                        }
                    }
                }
            } else {
                if (tag.getBoolean("scguns:ReloadComplete")) {
                    tag.putBoolean("scguns:IsPlayingReloadStop", true);
                    tag.remove("InCriticalReloadPhase");
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    if (item instanceof AnimatedGunItem) {
                        long id = GeoItem.getId(stack);
                        AnimationController<GeoAnimatable> animationController = ((AnimatedGunItem) item)
                                .getAnimatableInstanceCache()
                                .getManagerForId(id)
                                .getAnimationControllers()
                                .get("controller");

                        if (animationController != null) {
                            animationController.tryTriggerAnimation("reload_stop");
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            this.prevReloadTimer = this.reloadTimer;
            if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                ItemStack stack = player.getMainHandItem();
                if (Minecraft.getInstance().isPaused() && stack.getItem() instanceof GunItem) {
                    Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                    if (gun.getReloads().getReloadType() != ReloadType.MANUAL) {
                        setReloading(false);
                        return;
                    }
                }
            }
            if (ModSyncedDataKeys.RELOADING.getValue(player) && Minecraft.getInstance().isPaused()) {
                setReloading(false);
            }
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageLeftOverAmmo());

            if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                if (this.reloadingSlot != player.getInventory().selected) {
                    setReloading(false);
                }
            }

            this.updateReloadTimer(player);
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof GunItem && !(stack.getItem() instanceof AnimatedGunItem)) {
                Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                if (gun.getReloads().getReloadType() == ReloadType.MAG_FED &&
                        this.reloadTimer <= 0 &&
                        ModSyncedDataKeys.RELOADING.getValue(player)) {
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageGunLoaded());
                    setReloading(false);
                }
            }

            HUDRenderHandler.updateReserveAmmo(player);
        }
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem) {
            CompoundTag tag = stack.getOrCreateTag();
            ((GunItem) stack.getItem()).getModifiedGun(stack);
            tag.getBoolean("InCriticalReloadPhase");

            if (KeyBinds.KEY_RELOAD.isDown() && event.getAction() == GLFW.GLFW_PRESS) {
                boolean currentlyReloading = ModSyncedDataKeys.RELOADING.getValue(player);
                if (currentlyReloading) {
                    return;
                }
                setReloading(true);
                HUDRenderHandler.updateReserveAmmo(player);
            }

            if (KeyBinds.KEY_UNLOAD.consumeClick() && event.getAction() == GLFW.GLFW_PRESS) {
                setReloading(false);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload());
                HUDRenderHandler.stageReserveAmmoUpdate();
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseButton event) {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem) {
            CompoundTag tag = stack.getOrCreateTag();
            Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
            boolean inCriticalPhase = tag.getBoolean("InCriticalReloadPhase");
            boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);

            if (!isReloading && inCriticalPhase) {
                tag.remove("InCriticalReloadPhase");
                inCriticalPhase = false;
            }

            if (KeyBinds.getAimMapping().isDown() && event.getAction() == GLFW.GLFW_PRESS &&
                    inCriticalPhase && gun.getReloads().getReloadType() != ReloadType.MANUAL) {
                return;
            }
            KeyBinds.getAimMapping().isDown();
        }
    }

    public void setReloading(boolean reloading) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        CompoundTag tag = stack.getOrCreateTag();

        boolean inCriticalPhase = tag.getBoolean("InCriticalReloadPhase");

        if (reloading) {
            if (stack.getItem() instanceof GunItem gunItem) {
                if (inCriticalPhase) {
                    return;
                }

                if (!tag.contains("IgnoreAmmo", Tag.TAG_BYTE)) {
                    if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                        return;
                    }

                    if (stack.getItem() instanceof AnimatedGunItem animatedGun) {
                        Gun gun = gunItem.getModifiedGun(stack);

                        // Force stop aiming for any reload
                        if (ModSyncedDataKeys.AIMING.getValue(player)) {
                            ModSyncedDataKeys.AIMING.setValue(player, false);
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(false));
                        }

                        if (!tag.getBoolean("IsDrawn")) {
                            tag.putBoolean("IsDrawn", true);
                            tag.putInt("DrawnTick", 15);
                        }

                        boolean hasReloadTags = tag.getBoolean("IsReloading") ||
                                tag.getBoolean("scguns:IsReloading") ||
                                tag.contains("scguns:ReloadState");

                        if (hasReloadTags && !ModSyncedDataKeys.RELOADING.getValue(player)) {
                            animatedGun.cleanupReloadState(tag);
                        }

                        if (tag.getBoolean("IsReloading") && ModSyncedDataKeys.RELOADING.getValue(player)) {
                            return;
                        }
                    }

                    Gun gun = gunItem.getModifiedGun(stack);

                    if (tag.getInt("AmmoCount") >= GunModifierHelper.getModifiedAmmoCapacity(stack, gun)) {
                        return;
                    }
                    if (Gun.findAmmo(player, gun.getProjectile().getItem()).stack().isEmpty()) {
                        return;
                    }

                    ResourceLocation preReloadSound = gun.getSounds().getPreReload();
                    if (preReloadSound != null) {
                        Config.SERVER.reloadMaxDistance.get();
                        player.playSound(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(preReloadSound)),
                                0.9F, 1.0F);
                    }

                    if (MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Pre(player, stack))) {
                        return;
                    }

                    ModSyncedDataKeys.RELOADING.setValue(player, true);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(true));
                    this.reloadingSlot = player.getInventory().selected;

                    if (stack.getItem() instanceof AnimatedGunItem animatedGun) {
                        tag.putBoolean("IsReloading", true);
                        tag.putBoolean("scguns:IsReloading", true);
                        tag.remove("ReloadComplete");
                        tag.remove("scguns:ReloadComplete");

                        if (gun.getReloads().getReloadType() != ReloadType.MANUAL) {
                            tag.putBoolean("InCriticalReloadPhase", true);
                        }

                        long id = GeoItem.getId(stack);
                        AnimationController<GeoAnimatable> controller = animatedGun
                                .getAnimatableInstanceCache()
                                .getManagerForId(id)
                                .getAnimationControllers()
                                .get("controller");

                        if (controller != null) {
                            ReloadType reloadType = gun.getReloads().getReloadType();
                            if (reloadType == ReloadType.MAG_FED || reloadType == ReloadType.SINGLE_ITEM) {
                                tag.putBoolean("IsMagReload", true);
                                if (animatedGun.isInCarbineMode(stack)) {
                                    controller.tryTriggerAnimation("carbine_reload");
                                } else {
                                    controller.tryTriggerAnimation("reload");
                                }
                            }
                            if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                                tag.putBoolean("IsManualReload", true);
                                tag.putString("scguns:ReloadState", "NONE");
                                if (animatedGun.isInCarbineMode(stack)) {
                                    controller.tryTriggerAnimation("carbine_reload_start");
                                } else {
                                    controller.tryTriggerAnimation("reload_start");
                                }
                            }
                        }
                    }

                    MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post(player, stack));
                }
            }
        } else {
            if (inCriticalPhase) {
                Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                if (gun.getReloads().getReloadType() != ReloadType.MANUAL) {
                    return;
                }
            }

            if (stack.getItem() instanceof AnimatedGunItem animatedGun) {
                Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);

                if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                    if (tag.getBoolean("scguns:IsReloading") && !tag.getBoolean("scguns:IsPlayingReloadStop")) {
                        tag.putString("scguns:ReloadState", "STOPPING");
                        tag.putBoolean("scguns:IsPlayingReloadStop", true);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                    } else {
                        animatedGun.cleanupReloadState(tag);
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        tag.remove("InCriticalReloadPhase");
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                    }
                } else {
                    if (!inCriticalPhase) {
                        animatedGun.cleanupReloadState(tag);
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        tag.remove("InCriticalReloadPhase");
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                    }
                }
            } else {
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                tag.remove("InCriticalReloadPhase");
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                this.reloadingSlot = -1;
            }
        }

        if (stack.getItem() instanceof GunItem) {
            HUDRenderHandler.updateReserveAmmo(player);
        }
    }

    private void updateReloadTimer(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (ModSyncedDataKeys.RELOADING.getValue(player)) {
            if (stack.getItem() instanceof AnimatedGunItem) {
                return;
            }

            if (this.startReloadTick == -1) {
                this.startReloadTick = player.tickCount + 5;
            }
            if (this.reloadTimer < 5) {
                this.reloadTimer++;
            }
        } else {
            if (this.startReloadTick != -1) {
                this.startReloadTick = -1;
            }
            if (this.reloadTimer > 0) {
                this.reloadTimer--;
            }
        }
    }

    public int getStartReloadTick() {
        return this.startReloadTick;
    }

    public int getReloadTimer() {
        return this.reloadTimer;
    }

    public float getReloadProgress(float partialTicks) {
        return (this.prevReloadTimer + (this.reloadTimer - this.prevReloadTimer) * partialTicks) / 5F;
    }
}