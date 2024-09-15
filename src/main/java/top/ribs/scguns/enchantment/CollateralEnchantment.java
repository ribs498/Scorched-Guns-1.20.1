package top.ribs.scguns.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

/**
 * Author: MrCrayfish
 */
public class CollateralEnchantment extends GunEnchantment
{
    public CollateralEnchantment()
    {
        super(Rarity.RARE, EnchantmentTypes.COLLATERAL_COMPATIBLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}, Type.PROJECTILE);
    }
    @Override
    public int getMinCost(int level)
    {
        return 10;
    }

    @Override
    public int getMaxCost(int level)
    {
        return this.getMinCost(level) + 20;
    }
}
