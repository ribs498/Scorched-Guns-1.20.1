package top.ribs.scguns.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

/**
 * Author: MrCrayfish
 */
public class ShellCatcherEnchantment extends GunEnchantment
{
    public ShellCatcherEnchantment()
    {
        super(Rarity.UNCOMMON, EnchantmentTypes.SHELL_CATCHER_COMPATIBLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}, Type.WEAPON);
    }
    @Override
    public int getMaxLevel()
    {
        return 3;
    }
    @Override
    public int getMinCost(int level)
    {
        return 15;
    }

    @Override
    public int getMaxCost(int level)
    {
        return this.getMinCost(level) + 20;
    }
}

