package top.ribs.scguns.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.ammo_boxes.DishesPouch;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.*;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DishesEventHandler {

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack resultStack = event.getResultStack();

        // Regardless of what the food/drink was, check what item remains
        if (isDishItem(resultStack)) {
            if (addDishToPouch(player, resultStack)) {
                event.setResultStack(ItemStack.EMPTY);  // Clear if added to pouch
            }
        }
    }

    // This event catches items entering the inventory in real time
    @SubscribeEvent
    public static void onInventoryChanged(PlayerEvent.ItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack pickedItem = event.getStack();
        if (isDishItem(pickedItem)) {
            if (addDishToPouch(player, pickedItem)) {
                event.getStack().shrink(1); // "Catch" item before it enters inventory
                event.setCanceled(true);
            }
        }
    }

    // Checks if the item is considered a dish item
    private static boolean isDishItem(ItemStack stack) {
        return stack.is(Items.BOWL) || stack.is(Items.GLASS_BOTTLE) ||
                stack.getItem().getCraftingRemainingItem() == Items.BOWL ||
                stack.getItem().getCraftingRemainingItem() == Items.GLASS_BOTTLE;
    }

    // Tries to add the item to the Dishes Pouch, either from inventory or curios
    private static boolean addDishToPouch(Player player, ItemStack dishStack) {
        // First, check player's inventory
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof DishesPouch) {
                int insertedItems = DishesPouch.add(itemStack, dishStack);
                if (insertedItems > 0) {
                    dishStack.shrink(insertedItems);
                    return true;
                }
            }
        }

        // Then, check curios slots if available
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
