package top.ribs.scguns.init;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.ribs.scguns.entity.projectile.BrassBoltEntity;

public class BrassBoltItem extends ArrowItem {
    public BrassBoltItem(Properties pProperties) {
        super(pProperties);
    }

    public AbstractArrow createArrow(Level pLevel, ItemStack pStack, LivingEntity pShooter) {
        return new BrassBoltEntity(pLevel, pShooter);
    }
}



