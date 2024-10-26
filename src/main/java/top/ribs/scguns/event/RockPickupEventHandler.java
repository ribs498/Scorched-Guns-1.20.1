package top.ribs.scguns.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.ammo_boxes.RockPouch;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RockPickupEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        ItemStack pickedItem = event.getItem().getItem();

        if (isRockItem(pickedItem)) {
            if (addRockToPouch(player, pickedItem)) {
                event.setCanceled(true);
                event.getItem().setItem(ItemStack.EMPTY);
            }
        }
    }

    private static boolean isRockItem(ItemStack stack) {
        return stack.is(ItemTags.create(new ResourceLocation("scguns", "rocks")));
    }

    private static boolean addRockToPouch(Player player, ItemStack rockStack) {
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof RockPouch) {
                int insertedItems = RockPouch.add(itemStack, rockStack);
                if (insertedItems > 0) {
                    rockStack.shrink(insertedItems);
                    return true;
                }
            }
        }

        AtomicBoolean result = new AtomicBoolean(false);
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                ItemStack stack = curios.getStackInSlot(i);
                if (stack.getItem() instanceof RockPouch) {
                    int insertedItems = RockPouch.add(stack, rockStack);
                    if (insertedItems > 0) {
                        rockStack.shrink(insertedItems);
                        result.set(true);
                        break;
                    }
                }
            }
        });

        return result.get();
    }
}