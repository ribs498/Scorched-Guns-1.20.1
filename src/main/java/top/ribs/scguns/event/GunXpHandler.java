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
import top.ribs.scguns.item.attachment.IAttachment;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class GunXpHandler {

    @SubscribeEvent
    public static void onPlayerXpPickup(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();

        if (mainHand.getItem() instanceof GunItem gunItem) {
            if (gunItem.hasMendingInAttachments(mainHand)) {
                for (IAttachment.Type type : IAttachment.Type.values()) {
                    ItemStack attachmentStack = Gun.getAttachment(type, mainHand);
                    if (!attachmentStack.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, attachmentStack) > 0) {
                        ExperienceOrb orb = event.getOrb();
                        int xpValue = orb.value;
                        int repairAmount = Math.min(xpValue * 2, attachmentStack.getDamageValue());
                        attachmentStack.setDamageValue(attachmentStack.getDamageValue() - repairAmount);
                        orb.value = xpValue - (repairAmount / 2);
                        break;
                    }
                }
            }
        }
    }
}
