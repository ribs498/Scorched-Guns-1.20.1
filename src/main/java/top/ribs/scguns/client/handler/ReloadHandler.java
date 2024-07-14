package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.GunReloadEvent;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageLeftOverAmmo;
import top.ribs.scguns.network.message.C2SMessageMeleeAttack;
import top.ribs.scguns.network.message.C2SMessageReload;
import top.ribs.scguns.network.message.C2SMessageUnload;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;


/**
 * Author: MrCrayfish
 */
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

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        this.prevReloadTimer = this.reloadTimer;

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageLeftOverAmmo());

            if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                if (this.reloadingSlot != player.getInventory().selected) {
                    this.setReloading(false);
                }
            }

            this.updateReloadTimer(player);
            if (this.reloadTimer == 0) {
                HUDRenderHandler.updateReserveAmmo(player);
            }
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
        if (KeyBinds.KEY_MELEE.consumeClick() && event.getAction() == GLFW.GLFW_PRESS) {
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageMeleeAttack());
        }
    }

    public void setReloading(boolean reloading) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (reloading) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof GunItem) {
                    CompoundTag tag = stack.getTag();
                    if (tag != null && !tag.contains("IgnoreAmmo", Tag.TAG_BYTE)) {
                        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                        if (tag.getInt("AmmoCount") >= GunModifierHelper.getModifiedAmmoCapacity(stack, gun))
                            return;
                        if (MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Pre(player, stack)))
                            return;
                        ModSyncedDataKeys.RELOADING.setValue(player, true);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(true));
                        this.reloadingSlot = player.getInventory().selected;
                        MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post(player, stack));
                        HUDRenderHandler.updateReserveAmmo(player); // Update reserve ammo when reloading starts
                    }
                }
            } else {
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                this.reloadingSlot = -1;
            }
        }
    }

    private void updateReloadTimer(Player player) {
        if (ModSyncedDataKeys.RELOADING.getValue(player)) {
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

