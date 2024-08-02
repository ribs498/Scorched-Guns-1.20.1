package top.ribs.scguns.event;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.init.ModItems;

@Mod.EventBusSubscriber(modid = "scguns")
public class OceanWeaponEventHandler {

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyDolphinGrace(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyDolphinGrace(player);
        }
    }

    private static void applyDolphinGrace(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        boolean holdingSpecialItem = isSpecialItem(mainHandItem) || isSpecialItem(offHandItem);
        boolean isInWater = player.isEyeInFluid(FluidTags.WATER);
        MobEffectInstance dolphinGraceEffect = player.getEffect(MobEffects.DOLPHINS_GRACE);

        if (holdingSpecialItem && isInWater) {
            if (dolphinGraceEffect == null || dolphinGraceEffect.getAmplifier() < 0 || dolphinGraceEffect.getDuration() <= 10) {
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, false, false, true));
            }
        } else {
            if (dolphinGraceEffect != null) {
                player.removeEffect(MobEffects.DOLPHINS_GRACE);
            }
        }
    }

    private static boolean isSpecialItem(ItemStack itemStack) {
        return itemStack.getItem() == ModItems.FLOUNDERGAT.get() ||
                itemStack.getItem() == ModItems.BOMB_LANCE.get() ||
                itemStack.getItem() == ModItems.SEQUOIA.get() ||
                itemStack.getItem() == ModItems.MARLIN.get();
    }
}