package top.ribs.scguns.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
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
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack resultStack = event.getResultStack();
        Level level = player.level();

        // Check if the resulting item is a bowl, bottle, or similar dish item
        if (isDishItem(resultStack)) {
            if (addDishToPouch(player, resultStack)) {
                // Prevent the item from being added to the inventory by clearing the result stack
                event.setResultStack(ItemStack.EMPTY);
            }
        }
    }

    private static boolean isDishItem(ItemStack stack) {
        // Define the logic to check if the stack is a dish item, e.g., bowl or bottle
        return stack.is(Items.BOWL) || stack.is(Items.GLASS_BOTTLE);
    }

    private static boolean addDishToPouch(Player player, ItemStack dishStack) {
        // Similar logic to adding casings to the casing pouch
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof DishesPouch) {
                int insertedItems = DishesPouch.add(itemStack, dishStack);
                if (insertedItems > 0) {
                    dishStack.shrink(insertedItems);
                    return true;
                }
            }
        }

        // Check Curios slots if using Curios API
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
