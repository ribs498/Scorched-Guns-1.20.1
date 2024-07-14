package top.ribs.scguns.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

/**
 * A basic item class that implements {@link IAmmo} to indicate this item is ammunition
 *
 * Author: MrCrayfish
 */
public class FuelAmmoItem extends Item implements IAmmo
{

    private int burnTime = 0;
    public FuelAmmoItem(Properties properties, int burnTime)
    {
        super(properties);
        this.burnTime = burnTime;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return this.burnTime;
    }
}
