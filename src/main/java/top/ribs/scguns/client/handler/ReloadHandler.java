package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import top.ribs.scguns.Reference;
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
import top.ribs.scguns.util.GunModifierHelper;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        resetReloadState();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            this.prevReloadTimer = this.reloadTimer;

            PacketHandler.getPlayChannel().sendToServer(new C2SMessageLeftOverAmmo());

            if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                if (this.reloadingSlot != player.getInventory().selected) {
                    this.setReloading(false);
                }
            }

            this.updateReloadTimer(player);

            if (this.reloadTimer == 0 && ModSyncedDataKeys.RELOADING.getValue(player)) {
                setReloading(false);
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
        if (KeyBinds.KEY_MELEE.consumeClick() && event.getAction() == GLFW.GLFW_PRESS) {
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageMeleeAttack());
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ReloadHandler handler = ReloadHandler.get();
            handler.setReloading(false);
            handler.resetReloadState();
            //System.out.println("Player " + player.getName().getString() + " died, resetting reload state.");
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        ReloadHandler handler = ReloadHandler.get();
        handler.resetReloadState();
        //System.out.println("Player " + player.getName().getString() + " respawned, reset reload state.");
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
                        HUDRenderHandler.updateReserveAmmo(player);
                        //System.out.println("Reloading started for player: " + player.getName().getString());
                    }
                }
            } else {
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                this.reloadingSlot = -1;
                resetReloadState();
                //System.out.println("Reloading stopped for player: " + player.getName().getString());
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
        //System.out.println("Reload timer updated for player: " + player.getName().getString() + ", Timer: " + this.reloadTimer);
    }

    public void resetReloadState() {
        this.startReloadTick = -1;
        this.reloadTimer = 0;
        this.prevReloadTimer = 0;
        this.reloadingSlot = -1;
        //System.out.println("Reload state reset.");
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
