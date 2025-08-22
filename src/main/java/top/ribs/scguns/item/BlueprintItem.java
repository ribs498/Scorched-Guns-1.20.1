// Enhanced BlueprintItem.java
package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.client.screen.BlueprintScreen;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageClearBlueprintRecipe;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import java.util.List;

public class BlueprintItem extends Item {

    public BlueprintItem(Properties properties) {
        this(properties, "");
    }

    public BlueprintItem(Properties properties, String blueprintType) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copy();
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (level.isClientSide) {
            if (player.isShiftKeyDown()) {
                clearBlueprintRecipe(hand);
            } else {
                openBlueprintScreen(itemstack, player, hand);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @OnlyIn(Dist.CLIENT)
    private void openBlueprintScreen(ItemStack blueprintStack, Player player, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new BlueprintScreen(blueprintStack, player, hand));
    }

    @OnlyIn(Dist.CLIENT)
    private void clearBlueprintRecipe(InteractionHand hand) {
        PacketHandler.getPlayChannel().sendToServer(new C2SMessageClearBlueprintRecipe(hand));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        pTooltipComponents.add(Component.translatable("item.scguns.blueprint.tooltip.right_click")
                .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));

        if (pLevel != null && pLevel.isClientSide) {
            String activeRecipeName = BlueprintScreen.getActiveRecipeName(pStack);
            if (activeRecipeName != null) {
                pTooltipComponents.add(Component.translatable("item.scguns.blueprint.tooltip.active_recipe", activeRecipeName)
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));

                pTooltipComponents.add(Component.translatable("item.scguns.blueprint.tooltip.shift_right_click_clear")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
            }
        }
    }
}

