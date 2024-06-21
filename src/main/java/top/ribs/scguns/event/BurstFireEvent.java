package top.ribs.scguns.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BurstFireEvent {

    // Not on ModCommonEventBus because it breaks there? And if it ain't broke don't fix it I guess.
    private static int burstCount = 0;

    @SubscribeEvent
    public static void preShoot(GunFireEvent.Pre event)
    {

        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();
        if(heldItem.getItem() instanceof GunItem gunItem)
        {

            Gun gun = gunItem.getModifiedGun(heldItem);
            if (gun.getGeneral().getFireMode() == FireMode.BURST)
            {
                int serverModifier = event.getEntity().getServer() == null ? 2 : 1;
                if (burstCount / serverModifier >= gun.getGeneral().getBurstAmount())
                {
                    applyBurstTag(heldItem);

                    // Applies a cooldown after the burst has been fired.
                    /*ItemCooldowns tracker = player.getCooldowns();
                    tracker.addCooldown(heldItem.getItem(), 20);*/
                }

                if (isBursting(heldItem)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void postShoot(GunFireEvent.Post event) {

        if (!event.isClient()) {
            ItemStack heldItem = event.getStack();
            if(heldItem.getItem() instanceof GunItem gunItem)
            {
                Gun gun = gunItem.getModifiedGun(heldItem);
                if (gun.getGeneral().getFireMode() == FireMode.BURST)
                {
                    if (burstCount <= gun.getGeneral().getBurstAmount())
                    {
                        ++burstCount;
                    }
                }
            }
        }
    }

    public static boolean isBursting(ItemStack heldItem) {
        boolean burst = false;

        if(heldItem.getItem() instanceof GunItem gunItem)
        {
            CompoundTag tag = heldItem.getTag();

            Gun gun = gunItem.getModifiedGun(heldItem);
            if (tag != null && gun.getGeneral().getFireMode() == FireMode.BURST)
            {
                burst = tag.getBoolean("Bursting");
            }
        }
        return burst;
    }

    private static void applyBurstTag(ItemStack heldItem) {

        if(heldItem.getItem() instanceof GunItem gunItem)
        {
            Gun gun = gunItem.getModifiedGun(heldItem);
            if (gun.getGeneral().getFireMode() == FireMode.BURST)
            {
                CompoundTag tag = heldItem.getOrCreateTag();
                if (!isBursting(heldItem))
                {
                    tag.putBoolean("Bursting", true);
                }
            }
        }
    }

    public static void resetBurst(ItemStack heldItem) {

        if(heldItem.getItem() instanceof GunItem gunItem)
        {
            Gun gun = gunItem.getModifiedGun(heldItem);
            if (gun.getGeneral().getFireMode() == FireMode.BURST)
            {
                CompoundTag tag = heldItem.getOrCreateTag();
                burstCount = 0;
                if (isBursting(heldItem))
                {
                    tag.remove("Bursting");
                }
            }
        }
    }
}

