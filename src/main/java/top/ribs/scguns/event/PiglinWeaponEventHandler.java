package top.ribs.scguns.event;

import net.minecraft.nbt.CompoundTag;
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
import top.ribs.scguns.init.ModTags;

@Mod.EventBusSubscriber(modid = "scguns")
public class PiglinWeaponEventHandler {

    private static final String COOLDOWN_TAG = "PiglinWeaponCooldown";

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyLavaResistance(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyLavaResistance(player);
        }
    }

    private static void applyLavaResistance(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        boolean holdingSpecialItem = isPiglinWeapon(mainHandItem) || isPiglinWeapon(offHandItem);
        boolean isInLava = player.isEyeInFluid(FluidTags.LAVA);

        int cooldown = getCooldown(player);

        if (holdingSpecialItem && isInLava && cooldown <= 0) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, false, false, true)); // 20 seconds of Fire Resistance
            setCooldown(player, 600);
        } else if (cooldown > 0) {
            reduceCooldown(player);
        }
    }

    private static boolean isPiglinWeapon(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.is(ModTags.Items.PIGLIN_GUN);
    }

    private static int getCooldown(Player player) {
        CompoundTag tag = player.getPersistentData();
        return tag.contains(COOLDOWN_TAG) ? tag.getInt(COOLDOWN_TAG) : 0;
    }

    private static void setCooldown(Player player, int cooldown) {
        player.getPersistentData().putInt(COOLDOWN_TAG, cooldown);
    }

    private static void reduceCooldown(Player player) {
        int cooldown = getCooldown(player);
        if (cooldown > 0) {
            setCooldown(player, cooldown - 1);
        }
    }
}