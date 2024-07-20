package top.ribs.scguns.compat;

import com.mrcrayfish.backpacked.core.ModEnchantments;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess;
import net.minecraft.world.item.Item;
import top.ribs.scguns.common.AmmoContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import top.ribs.scguns.common.Gun;

/**
 * Author: MrCrayfish
 */
public class BackpackHelper
{
    public static AmmoContext findAmmo(Player player, Item id)
    {
        BackpackInventory inventory = ((BackpackedInventoryAccess) player).getBackpackedInventory();

        if(inventory == null)
            return AmmoContext.NONE;

        ItemStack backpack = inventory.getBackpackStack();

        if(backpack.isEmpty())
            return AmmoContext.NONE;

        if(EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.MARKSMAN.get(), backpack) <= 0)
            return AmmoContext.NONE;


        for(int i = 0; i < inventory.getContainerSize(); i++)
        {
            ItemStack stack = inventory.getItem(i);
            if(Gun.isAmmo(stack, id))
            {
                return new AmmoContext(stack, inventory);
            }
        }

        return AmmoContext.NONE;
    }
}