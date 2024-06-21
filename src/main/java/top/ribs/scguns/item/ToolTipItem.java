package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ToolTipItem extends Item {
    public ToolTipItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("info.scguns.tooltip_item" + "." + this.asItem()).withStyle(ChatFormatting.GRAY));

    }
}
