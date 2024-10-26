package top.ribs.scguns.event;


import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.item.*;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = "scguns", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArmorRemoveEventHandler {

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            EquipmentSlot changedSlot = event.getSlot();
            if (changedSlot.getType() == EquipmentSlot.Type.ARMOR) {
                updateArmorAttributes(player);
            }
        }
    }

    private static void updateArmorAttributes(Player player) {
        int totalArmor = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack itemStack = player.getItemBySlot(slot);

                if (itemStack.getItem() instanceof AdrienHelmItem ||
                        itemStack.getItem() instanceof BrassMaskItem ||
                        itemStack.getItem() instanceof RidgetopItem ||
                        itemStack.getItem() instanceof AnthraliteRespiratorItem ||
                        itemStack.getItem() instanceof NetheriteRespiratorItem) {

                    totalArmor += ((IArmorItem) itemStack.getItem()).getDefense();
                }
            }
        }

        AttributeInstance armorAttribute = player.getAttributes().getInstance(Attributes.ARMOR);
        if (armorAttribute != null && totalArmor > armorAttribute.getBaseValue()) {
            armorAttribute.setBaseValue(totalArmor);
        }
    }
    public interface IArmorItem {
        int getDefense();
    }


}

