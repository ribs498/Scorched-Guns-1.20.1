package top.ribs.scguns.event;

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
import top.ribs.scguns.item.ammo_boxes.DishesPouch;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DishesEventHandler {

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        ItemStack resultStack = event.getResultStack();
        if (isDishItem(resultStack)) {
            if (addDishToPouch(player, resultStack)) {
                event.setResultStack(ItemStack.EMPTY);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        ItemStack pickedItem = event.getItem().getItem();

        if (isDishItem(pickedItem)) {
            if (addDishToPouch(player, pickedItem)) {
                event.setCanceled(true);
                event.getItem().setItem(ItemStack.EMPTY);
            }
        }
    }

    private static boolean isDishItem(ItemStack stack) {
        return stack.is(Items.BOWL) || stack.is(Items.GLASS_BOTTLE) ||
                stack.getItem().getCraftingRemainingItem() == Items.BOWL ||
                stack.getItem().getCraftingRemainingItem() == Items.GLASS_BOTTLE;
    }

    private static boolean addDishToPouch(Player player, ItemStack dishStack) {
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof DishesPouch) {
                int insertedItems = DishesPouch.add(itemStack, dishStack);
                if (insertedItems > 0) {
                    dishStack.shrink(insertedItems);
                    return true;
                }
            }
        }

        AtomicBoolean result = new AtomicBoolean(false);
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                ItemStack stack = curios.getStackInSlot(i);
                if (stack.getItem() instanceof DishesPouch) {
                    int insertedItems = DishesPouch.add(stack, dishStack);
                    if (insertedItems > 0) {
                        dishStack.shrink(insertedItems);
                        result.set(true);
                        break;
                    }
                }
            }
        });

        return result.get();
    }
}