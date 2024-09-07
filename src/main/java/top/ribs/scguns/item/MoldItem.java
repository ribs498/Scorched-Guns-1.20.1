package top.ribs.scguns.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class MoldItem extends Item {

    public MoldItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isRepairable(@NotNull ItemStack stack) {
        return false;
    }

    // This method indicates that the item has a remaining item after crafting
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    // This method returns the remaining item after crafting (in this case, a damaged version of itself)
    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack remainingItem = stack.copy(); // Create a copy of the current item
        remainingItem.setDamageValue(remainingItem.getDamageValue() + 1); // Increase damage value
        if (remainingItem.getDamageValue() >= remainingItem.getMaxDamage()) {
            return ItemStack.EMPTY; // If fully damaged, return an empty stack (destroyed)
        }
        return remainingItem; // Return the damaged item
    }
}
