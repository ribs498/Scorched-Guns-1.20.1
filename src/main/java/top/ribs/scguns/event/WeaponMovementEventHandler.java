package top.ribs.scguns.event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.ExoSuitItem;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "scguns")
public class WeaponMovementEventHandler {
    private static final UUID HEAVY_WEAPON_MODIFIER_UUID = UUID.fromString("ff624994-88ae-4002-b5c9-b41b6d658030");
    private static final UUID RELOAD_SPEED_MODIFIER_UUID = UUID.fromString("aa815773-99bf-4113-c6d8-e52c7f769041");
    private static final UUID LIGHTWEIGHT_SPEED_MODIFIER_UUID = UUID.fromString("bb926884-10cf-5224-d7e9-f63d8e890152");

    private static final double LIGHTWEIGHT_REDUCTION_PER_LEVEL = 0.2D;
    private static final double LIGHTWEIGHT_SPEED_BONUS_PER_LEVEL = 0.05D; // 5% speed increase per level
    private static final double RELOAD_SPEED_PENALTY = 0.75D; // 75% normal speed while reloading
    private static final double LIGHTWEIGHT_RELOAD_BONUS_PER_LEVEL = 0.08D; // 8% speed increase per level
    private static final double SWIFT_SNEAK_RELOAD_BONUS_PER_LEVEL = 0.05D; // 5% speed increase per level

    private static int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 20;

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            EquipmentSlot slot = event.getSlot();
            if (slot == EquipmentSlot.MAINHAND ||
                    slot == EquipmentSlot.OFFHAND ||
                    slot == EquipmentSlot.LEGS) {
                updateSpeedAttribute(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        tickCounter++;
        if (tickCounter >= UPDATE_INTERVAL) {
            tickCounter = 0;
            updateSpeedAttribute(event.player);
        }
    }

    private static void updateSpeedAttribute(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        ItemStack legsItem = player.getItemBySlot(EquipmentSlot.LEGS);

        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        // Remove existing modifiers
        movementSpeed.removeModifier(HEAVY_WEAPON_MODIFIER_UUID);
        movementSpeed.removeModifier(RELOAD_SPEED_MODIFIER_UUID);
        movementSpeed.removeModifier(LIGHTWEIGHT_SPEED_MODIFIER_UUID);

        boolean hasExoSuitLegs = legsItem.getItem() instanceof ExoSuitItem;
        boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);

        boolean holdingGun = (mainHandItem.getItem() instanceof GunItem) || (offHandItem.getItem() instanceof GunItem);

        // If reloading but not holding a gun, force stop reloading
        if (isReloading && !holdingGun) {
            ModSyncedDataKeys.RELOADING.setValue(player, false);
            isReloading = false;
        }

        // Calculate base weapon speed modifiers
        float mainHandSpeedModifier = getEffectiveSpeedModifier(mainHandItem, hasExoSuitLegs);
        float offHandSpeedModifier = getEffectiveSpeedModifier(offHandItem, hasExoSuitLegs);
        float finalSpeedModifier = Math.min(mainHandSpeedModifier, offHandSpeedModifier);

        // Determine which weapon is the relevant one for enchantments
        ItemStack relevantWeapon = mainHandSpeedModifier < offHandSpeedModifier ? mainHandItem : offHandItem;

        // Apply lightweight enchantment for heavy weapons (penalty reduction)
        if (finalSpeedModifier < 1.0F && isHeavyWeapon(relevantWeapon)) {
            int lightweightLevel = relevantWeapon.getEnchantmentLevel(ModEnchantments.LIGHTWEIGHT.get());
            double reduction = LIGHTWEIGHT_REDUCTION_PER_LEVEL * lightweightLevel;
            finalSpeedModifier = (float) Math.min(1.0F, finalSpeedModifier + reduction);
        }

        // Apply heavy weapon modifier if needed
        if (finalSpeedModifier != 1.0F) {
            AttributeModifier modifier = new AttributeModifier(
                    HEAVY_WEAPON_MODIFIER_UUID,
                    "Weapon Speed Modifier",
                    finalSpeedModifier - 1.0D,
                    AttributeModifier.Operation.MULTIPLY_BASE
            );
            movementSpeed.addTransientModifier(modifier);
        }

        // Apply lightweight speed bonus for all guns with lightweight enchantment
        if (holdingGun) {
            ItemStack gunWithLightweight = null;
            int maxLightweightLevel = 0;

            // Find the gun with the highest lightweight level
            if (mainHandItem.getItem() instanceof GunItem) {
                int level = mainHandItem.getEnchantmentLevel(ModEnchantments.LIGHTWEIGHT.get());
                if (level > maxLightweightLevel) {
                    maxLightweightLevel = level;
                    gunWithLightweight = mainHandItem;
                }
            }
            if (offHandItem.getItem() instanceof GunItem) {
                int level = offHandItem.getEnchantmentLevel(ModEnchantments.LIGHTWEIGHT.get());
                if (level > maxLightweightLevel) {
                    maxLightweightLevel = level;
                    gunWithLightweight = offHandItem;
                }
            }

            if (gunWithLightweight != null && maxLightweightLevel > 0) {
                double speedBonus = 0.0D;

                if (isHeavyWeapon(gunWithLightweight)) {
                    // For heavy weapons: Level 2 gives 5% speed boost (level 1 only removes penalty)
                    if (maxLightweightLevel >= 2) {
                        speedBonus = LIGHTWEIGHT_SPEED_BONUS_PER_LEVEL;
                    }
                } else {
                    // For non-heavy weapons: 5% speed boost per level
                    speedBonus = LIGHTWEIGHT_SPEED_BONUS_PER_LEVEL * maxLightweightLevel;
                }

                if (speedBonus > 0.0D) {
                    AttributeModifier lightweightSpeedModifier = new AttributeModifier(
                            LIGHTWEIGHT_SPEED_MODIFIER_UUID,
                            "Lightweight Speed Bonus",
                            speedBonus,
                            AttributeModifier.Operation.MULTIPLY_BASE
                    );
                    movementSpeed.addTransientModifier(lightweightSpeedModifier);
                }
            }
        }

        // Apply reload speed penalty if reloading with a gun
        if (isReloading && holdingGun) {
            double reloadSpeedModifier = RELOAD_SPEED_PENALTY;

            // Get the gun item that's being reloaded (prefer main hand)
            ItemStack gunItem = mainHandItem.getItem() instanceof GunItem ? mainHandItem : offHandItem;

            // Apply lightweight enchantment bonus during reload
            int lightweightLevel = gunItem.getEnchantmentLevel(ModEnchantments.LIGHTWEIGHT.get());
            double lightweightBonus = LIGHTWEIGHT_RELOAD_BONUS_PER_LEVEL * lightweightLevel;

            // Apply swift sneak enchantment bonus during reload (from legs equipment)
            int swiftSneakLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SWIFT_SNEAK, legsItem);
            double swiftSneakBonus = SWIFT_SNEAK_RELOAD_BONUS_PER_LEVEL * swiftSneakLevel;

            // Calculate final reload speed (cap at 100% to prevent exceeding normal speed)
            reloadSpeedModifier = Math.min(1.0D, reloadSpeedModifier + lightweightBonus + swiftSneakBonus);

            AttributeModifier reloadModifier = new AttributeModifier(
                    RELOAD_SPEED_MODIFIER_UUID,
                    "Reload Speed Penalty",
                    reloadSpeedModifier - 1.0D,
                    AttributeModifier.Operation.MULTIPLY_BASE
            );
            movementSpeed.addTransientModifier(reloadModifier);
        }
    }

    private static float getEffectiveSpeedModifier(ItemStack stack, boolean hasExoSuitLegs) {
        if (stack.isEmpty()) return 1.0F;

        if (stack.getItem() instanceof GunItem gunItem) {
            float baseModifier = gunItem.getGunProperties().getGeneral().getSpeedModifier();

            if (hasExoSuitLegs && baseModifier < 1.0F && isHeavyWeapon(stack)) {
                return 1.0F;
            }

            return baseModifier;
        }

        return 1.0F;
    }

    private static boolean isHeavyWeapon(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.is(ModTags.Items.HEAVY_WEAPON);
    }
}