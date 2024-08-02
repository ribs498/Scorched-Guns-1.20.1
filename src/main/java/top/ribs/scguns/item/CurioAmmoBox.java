package top.ribs.scguns.item;

import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;

public class CurioAmmoBox implements ICurio {
    private final ItemStack stack;

    public CurioAmmoBox(ItemStack stack) {
        this.stack = stack;
    }

    @Nonnull
    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public void curioTick(SlotContext slotContext) {
        // Add your ticking logic here
    }

    // Implement other methods as needed
}

