package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;

@Mod.EventBusSubscriber(modid = "scguns", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ReloadFOVHandler {

    private static float preReloadFOV = 1.0f;
    private static boolean wasReloading = false;
    private static int reloadEndCooldown = 0;
    private static final int COOLDOWN_DURATION = 3;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (reloadEndCooldown > 0) {
            reloadEndCooldown--;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onComputeFOV(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean holdingGun = (mainHand.getItem() instanceof GunItem) || (offHand.getItem() instanceof GunItem);
        boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);

        if (wasReloading && !holdingGun) {
            wasReloading = false;
            reloadEndCooldown = 0;
            return;
        }

        if (holdingGun) {
            if (isReloading) {
                if (!wasReloading) {
                    preReloadFOV = event.getFovModifier();
                    wasReloading = true;
                    reloadEndCooldown = 0;
                }
                event.setNewFovModifier(preReloadFOV);
            } else if (wasReloading) {
                wasReloading = false;
                reloadEndCooldown = COOLDOWN_DURATION;
                event.setNewFovModifier(preReloadFOV);
            } else if (reloadEndCooldown > 0) {
                event.setNewFovModifier(preReloadFOV);
            }
        } else {
            reloadEndCooldown = 0;
        }
    }
}