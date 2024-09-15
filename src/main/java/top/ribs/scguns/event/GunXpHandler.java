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
            // Use the hasMendingInAttachments method from GunItem class
            if (gunItem.hasMendingInAttachments(mainHand)) {
                // Check for each attachment with Mending
                for (IAttachment.Type type : IAttachment.Type.values()) {
                    ItemStack attachmentStack = Gun.getAttachment(type, mainHand);
                    if (!attachmentStack.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, attachmentStack) > 0) {
                        ExperienceOrb orb = event.getOrb();
                        int xpValue = orb.value;  // Directly access the 'value' field

                        // Calculate repair amount and repair attachment
                        int repairAmount = Math.min(xpValue * 2, attachmentStack.getDamageValue());
                        attachmentStack.setDamageValue(attachmentStack.getDamageValue() - repairAmount);

                        // Reduce XP in the orb by the amount used to repair
                        orb.value = xpValue - (repairAmount / 2);

                        // Stop processing after repairing one attachment
                        break;
                    }
                }
            }
        }
    }
}
