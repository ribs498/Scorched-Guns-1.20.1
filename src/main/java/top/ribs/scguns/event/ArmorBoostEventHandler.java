package top.ribs.scguns.event;

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
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        if (isSpecialItem(mainHandItem) || isSpecialItem(offHandItem)) {
            Objects.requireNonNull(player.getAttributes().getInstance(Attributes.ARMOR)).setBaseValue(4);
        } else {
            Objects.requireNonNull(player.getAttributes().getInstance(Attributes.ARMOR)).setBaseValue(0);
        }
    }

    private static boolean isSpecialItem(ItemStack itemStack) {
        return itemStack.getItem() == ModItems.SHELLURKER.get();
    }
}