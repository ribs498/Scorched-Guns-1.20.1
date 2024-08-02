package top.ribs.scguns.event;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.init.ModItems;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = "scguns")
public class HeavyWeaponEventHandler {

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            applySpeedDecrease(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            applySpeedDecrease(player);
        }
    }

    private static void applySpeedDecrease(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        boolean holdingSpecialItem = isSpecialItem(mainHandItem) || isSpecialItem(offHandItem);
        MobEffectInstance slownessEffect = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);

        if (holdingSpecialItem) {
            if (slownessEffect == null || slownessEffect.getAmplifier() < 0 || slownessEffect.getDuration() <= 20) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, false, true));
            }
        } else {
            if (slownessEffect != null) {
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            }
        }
    }

    private static boolean isSpecialItem(ItemStack itemStack) {
        return itemStack.getItem() == ModItems.GATTALER.get() ||
                itemStack.getItem() == ModItems.THUNDERHEAD.get() ||
                itemStack.getItem() == ModItems.SPITFIRE.get() ||
                itemStack.getItem() == ModItems.CYCLONE.get();
    }
}