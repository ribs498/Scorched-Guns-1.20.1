package top.ribs.scguns.event;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedSculkGunItem;
import top.ribs.scguns.item.attachment.IAttachment;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class GunXpHandler {
    private static final float SCULK_REPAIR_RATIO = 0.5F;

    @SubscribeEvent
    public static void onPlayerXpPickup(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();

        if (mainHand.getItem() instanceof GunItem gunItem) {
            ExperienceOrb orb = event.getOrb();
            int remainingXp = orb.value;
            if (mainHand.getItem() instanceof AnimatedSculkGunItem && mainHand.isDamaged() && remainingXp > 0) {
                int repairAmount = Math.min((int)(remainingXp * 2 * SCULK_REPAIR_RATIO), mainHand.getDamageValue());
                if (repairAmount > 0) {
                    mainHand.setDamageValue(mainHand.getDamageValue() - repairAmount);
                    remainingXp -= repairAmount;
                }
            }
            for (IAttachment.Type type : IAttachment.Type.values()) {
                ItemStack attachmentStack = Gun.getAttachment(type, mainHand);
                if (!attachmentStack.isEmpty() &&
                        EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, attachmentStack) > 0 &&
                        attachmentStack.isDamaged() &&
                        remainingXp > 0) {
                    int repairAmount = Math.min(remainingXp * 2, attachmentStack.getDamageValue());
                    if (repairAmount > 0) {
                        attachmentStack.setDamageValue(attachmentStack.getDamageValue() - repairAmount);
                        gunItem.onAttachmentChanged(mainHand);
                        remainingXp -= (repairAmount / 2);
                    }
                }
            }

            orb.value = remainingXp;
        }
    }
}