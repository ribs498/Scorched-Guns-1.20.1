package top.ribs.scguns.event;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.GunItem;

import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "scguns")
public class HeavyWeaponEventHandler {
    private static final UUID HEAVY_WEAPON_MODIFIER_UUID = UUID.fromString("ff624994-88ae-4002-b5c9-b41b6d658030");
    private static final double LIGHTWEIGHT_REDUCTION_PER_LEVEL = 0.2D;

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            updateSpeedAttribute(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            updateSpeedAttribute(player);
        }
    }

    private static void updateSpeedAttribute(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        movementSpeed.removeModifier(HEAVY_WEAPON_MODIFIER_UUID);

        // Get the effective speed modifier from either hand
        float speedModifier = getEffectiveSpeedModifier(mainHandItem);
        float offHandSpeedModifier = getEffectiveSpeedModifier(offHandItem);

        // Use the more impactful modifier (further from 1.0)
        if (Math.abs(1.0F - offHandSpeedModifier) > Math.abs(1.0F - speedModifier)) {
            speedModifier = offHandSpeedModifier;
        }

        // Only apply if there's actually a modification needed
        if (speedModifier != 1.0F) {
            // Apply lightweight enchantment bonus if the item is a heavy weapon
            if (speedModifier < 1.0F && (isHeavyWeapon(mainHandItem) || isHeavyWeapon(offHandItem))) {
                ItemStack heavyWeapon = isHeavyWeapon(mainHandItem) ? mainHandItem : offHandItem;
                int lightweightLevel = heavyWeapon.getEnchantmentLevel(ModEnchantments.LIGHTWEIGHT.get());
                double reduction = LIGHTWEIGHT_REDUCTION_PER_LEVEL * lightweightLevel;
                speedModifier = (float) Math.min(1.0F, speedModifier + reduction);
            }

            AttributeModifier modifier = new AttributeModifier(
                    HEAVY_WEAPON_MODIFIER_UUID,
                    "Weapon Speed Modifier",
                    speedModifier - 1.0D,
                    AttributeModifier.Operation.MULTIPLY_BASE
            );
            movementSpeed.addTransientModifier(modifier);
        }
    }

    private static float getEffectiveSpeedModifier(ItemStack stack) {
        if (stack.isEmpty()) return 1.0F;
        if (stack.getItem() instanceof GunItem gunItem) {
            return gunItem.getGunProperties().getGeneral().getSpeedModifier();
        }
        return 1.0F;
    }

    private static boolean isHeavyWeapon(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.is(ModTags.Items.HEAVY_WEAPON);
    }
}