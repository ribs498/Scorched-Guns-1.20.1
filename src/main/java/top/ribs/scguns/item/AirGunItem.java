package top.ribs.scguns.item;

import com.simibubi.create.content.equipment.armor.BacktankUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import top.ribs.scguns.common.Gun;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class AirGunItem extends GunItem {

    public AirGunItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        Boolean result = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            List<ItemStack> backtanks = BacktankUtil.getAllWithAir(getClientPlayer());
            return !backtanks.isEmpty() || stack.isDamaged();
        });
        return result != null && result;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        Integer width = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            List<ItemStack> backtanks = BacktankUtil.getAllWithAir(getClientPlayer());
            if (!backtanks.isEmpty()) {
                ItemStack backtank = backtanks.get(0);
                int maxAir = BacktankUtil.maxAir(backtank);
                float air = BacktankUtil.getAir(backtank);
                return Math.round(13.0F * air / maxAir); // Dynamically calculate based on air level
            } else {
                return Math.round(13.0F - (float) stack.getDamageValue() * 13.0F / (float) stack.getMaxDamage());
            }
        });
        return width != null ? width : 0; // Fallback to 0 if not on client
    }

    @Override
    public int getBarColor(ItemStack stack) {
        Integer color = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            List<ItemStack> backtanks = BacktankUtil.getAllWithAir(getClientPlayer());
            if (!backtanks.isEmpty()) {
                return BacktankUtil.getBarColor(backtanks.get(0), 1); // Get color based on backtank's air
            }
            if (stack.getDamageValue() >= (stack.getMaxDamage() / 1.5)) {
                return Objects.requireNonNull(ChatFormatting.RED.getColor());
            }
            float f = Math.max(0.0F, (stack.getMaxDamage() - (float) stack.getDamageValue()) / (float) stack.getMaxDamage());
            return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
        });
        return color != null ? color : Mth.hsvToRgb(1.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        Gun gun = this.getModifiedGun(stack);
        int airUsage = gun.getGeneral().getEnergyUse();

        tooltip.add(Component.translatable("info.airgun.air_usage")
                .append(": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(airUsage)).withStyle(ChatFormatting.WHITE)));

        if (world != null && world.isClientSide) {
            Player player = getClientPlayer();
            if (player != null) {
                List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
                if (backtanks.isEmpty()) {
                    tooltip.add(Component.translatable("info.airgun.requires_airtank")
                            .withStyle(ChatFormatting.RED));
                }
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

}
