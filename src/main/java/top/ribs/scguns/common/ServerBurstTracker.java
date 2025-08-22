package top.ribs.scguns.common;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.GunItem;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ServerBurstTracker {
    private static final Map<UUID, BurstData> burstDataMap = new WeakHashMap<>();

    public static class BurstData {
        public int burstCount = 0;
        public int weaponSlot = -1;
        public ItemStack weaponStack = ItemStack.EMPTY;

        public BurstData(int burstCount, int slot, ItemStack stack) {
            this.burstCount = burstCount;
            this.weaponSlot = slot;
            this.weaponStack = stack;
        }

        public boolean isSameWeapon(ServerPlayer player) {
            return player.getInventory().selected == weaponSlot &&
                    player.getInventory().getSelected() == weaponStack;
        }
    }

    public static int getBurstCount(ServerPlayer player) {
        BurstData data = burstDataMap.get(player.getUUID());
        if (data != null && data.isSameWeapon(player)) {
            return data.burstCount;
        }
        return 0;
    }

    public static void setBurstCount(ServerPlayer player, int count) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            burstDataMap.put(player.getUUID(),
                    new BurstData(count, player.getInventory().selected, heldItem));
        }
    }

    public static void decrementBurstCount(ServerPlayer player) {
        BurstData data = burstDataMap.get(player.getUUID());
        if (data != null && data.isSameWeapon(player)) {
            data.burstCount = Math.max(0, data.burstCount - 1);
        }
    }

    public static void clearBurstData(ServerPlayer player) {
        burstDataMap.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearBurstData(player);
        }
    }
}