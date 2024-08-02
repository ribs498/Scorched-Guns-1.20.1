package top.ribs.scguns.event;


import net.minecraft.world.entity.EquipmentSlot;
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
                if (itemStack.getItem() instanceof AdrienHelmItem) {
                    totalArmor += ((AdrienHelmItem) itemStack.getItem()).getDefense();
                }
                if (itemStack.getItem() instanceof BrassMaskItem) {
                    totalArmor += ((BrassMaskItem) itemStack.getItem()).getDefense();
                }
                if (itemStack.getItem() instanceof RidgetopItem) {
                    totalArmor += ((RidgetopItem) itemStack.getItem()).getDefense();
                }
                if (itemStack.getItem() instanceof AnthraliteRespiratorItem) {
                    totalArmor += ((AnthraliteRespiratorItem) itemStack.getItem()).getDefense();
                }
                if (itemStack.getItem() instanceof NetheriteRespiratorItem) {
                    totalArmor += ((NetheriteRespiratorItem) itemStack.getItem()).getDefense();
                }

            }
        }
        double currentArmorValue = Objects.requireNonNull(player.getAttributes().getInstance(Attributes.ARMOR)).getBaseValue();
        if (totalArmor > currentArmorValue) {
            player.getAttributes().getInstance(Attributes.ARMOR).setBaseValue(totalArmor);
        }
    }

}

