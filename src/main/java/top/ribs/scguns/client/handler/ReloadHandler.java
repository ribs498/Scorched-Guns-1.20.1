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
import top.ribs.scguns.network.message.C2SMessageGunLoaded;
import top.ribs.scguns.network.message.C2SMessageLeftOverAmmo;
import top.ribs.scguns.network.message.C2SMessageReload;
import top.ribs.scguns.network.message.C2SMessageUnload;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
//scguns
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
                tag.putBoolean("scguns:ReloadComplete", true);
                if (!tag.getBoolean("scguns:IsPlayingReloadStop")) {
                    tag.putBoolean("scguns:IsPlayingReloadStop", true);
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    if (item instanceof AnimatedGunItem) {
                        long id = GeoItem.getId(stack);
                        AnimationController<GeoAnimatable> animationController = ((AnimatedGunItem)item)
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
                    this.setReloading(false);
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

        if (KeyBinds.KEY_RELOAD.isDown() && event.getAction() == GLFW.GLFW_PRESS) {
            this.setReloading(!ModSyncedDataKeys.RELOADING.getValue(player));
            if (player.getMainHandItem().getItem() instanceof GunItem)
                HUDRenderHandler.updateReserveAmmo(player);
        }
        if (KeyBinds.KEY_UNLOAD.consumeClick() && event.getAction() == GLFW.GLFW_PRESS) {
            this.setReloading(false);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload());
            if (player.getMainHandItem().getItem() instanceof GunItem)
                HUDRenderHandler.stageReserveAmmoUpdate();
        }

    }
    public void setReloading(boolean reloading) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (reloading) {
            if (stack.getItem() instanceof GunItem gunItem) {
                CompoundTag tag = stack.getTag();
                if (tag != null && !tag.contains("IgnoreAmmo", Tag.TAG_BYTE)) {
                    if (stack.getItem() instanceof AnimatedGunItem && tag.getBoolean("IsReloading")) {
                        return;
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

                    // Rest of the existing reload code...
                    if (stack.getItem() instanceof AnimatedGunItem) {
                        tag.putBoolean("IsReloading", true);
                        tag.remove("ReloadComplete");
                        long id = GeoItem.getId(stack);
                        AnimationController<GeoAnimatable> controller = ((AnimatedGunItem)stack.getItem())
                                .getAnimatableInstanceCache()
                                .getManagerForId(id)
                                .getAnimationControllers()
                                .get("controller");

                        ReloadType reloadType = gun.getReloads().getReloadType();
                        if (reloadType == ReloadType.MAG_FED) {
                            tag.putBoolean("IsMagReload", true);
                            controller.tryTriggerAnimation("reload");
                        } else if (reloadType == ReloadType.MANUAL) {
                            tag.putBoolean("IsManualReload", true);
                            controller.tryTriggerAnimation("reload_start");
                        }
                    }

                    MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post(player, stack));
                }
            }
        } else {
            if (!(stack.getItem() instanceof AnimatedGunItem)) {
                ModSyncedDataKeys.RELOADING.setValue(player, false);
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