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
}
