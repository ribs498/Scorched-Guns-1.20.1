package top.ribs.scguns.event;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.init.ModItems;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = "scguns")
public class ArmorBoostEventHandler {

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyArmorBoost(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyArmorBoost(player);
        }
    }

    private static void applyArmorBoost(Player player) {
        AttributeInstance armorAttribute = player.getAttributes().getInstance(Attributes.ARMOR);
        if (armorAttribute == null) return;
        double currentArmorValue = armorAttribute.getBaseValue();
        boolean holdingSpecialItem = isSpecialItem(player.getMainHandItem()) || isSpecialItem(player.getOffhandItem());
        if (holdingSpecialItem && currentArmorValue != 4) {
            armorAttribute.setBaseValue(4);
        } else if (!holdingSpecialItem && currentArmorValue != 0) {
            armorAttribute.setBaseValue(0);
        }
    }

    private static boolean isSpecialItem(ItemStack itemStack) {
        return itemStack.getItem() == ModItems.SHELLURKER.get();
    }

}