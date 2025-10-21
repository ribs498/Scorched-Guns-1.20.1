package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class TooltipItem extends Item {
    private final String tooltipKey;
    private final String secondaryTooltipKey;

    public TooltipItem(Properties properties, String tooltipKey) {
        this(properties, tooltipKey, null);
    }

    public TooltipItem(Properties properties, String tooltipKey, @Nullable String secondaryTooltipKey) {
        super(properties);
        this.tooltipKey = tooltipKey;
        this.secondaryTooltipKey = secondaryTooltipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (tooltipKey != null && !tooltipKey.isEmpty()) {
            tooltip.add(Component.translatable(tooltipKey)
                    .withStyle(ChatFormatting.GRAY)
                    .withStyle(ChatFormatting.ITALIC));
        }

        if (secondaryTooltipKey != null && !secondaryTooltipKey.isEmpty()) {
            tooltip.add(Component.translatable(secondaryTooltipKey)
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .withStyle(ChatFormatting.ITALIC));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }
}