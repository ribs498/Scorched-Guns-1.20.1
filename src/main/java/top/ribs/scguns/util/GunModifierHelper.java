package top.ribs.scguns.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.interfaces.IGunModifier;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;

import java.util.Collection;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class GunModifierHelper
{
    public static double getModifiedMouseSensitivity(ItemStack weapon, double baseSensitivity) {
        double sensitivity = baseSensitivity;
        for(int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers) {
                sensitivity = modifier.modifyMouseSensitivity(sensitivity);
            }
        }
        return Math.max(sensitivity, 0.01);
    }

    private static final IGunModifier[] EMPTY = {};

    public static IGunModifier[] getModifiers(ItemStack weapon, IAttachment.Type type)
    {
        ItemStack stack = Gun.getAttachment(type, weapon);
        if(!stack.isEmpty() && stack.getItem() instanceof IAttachment<?> attachment)
        {
            return attachment.getProperties().getModifiers();
        }
        return EMPTY;
    }

    public static int getModifiedProjectileLife(ItemStack weapon, int life)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                life = modifier.modifyProjectileLife(life);
            }
        }
        return life;
    }

    public static double getModifiedProjectileGravity(ItemStack weapon, double gravity) {
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                gravity = modifier.modifyProjectileGravity(gravity);
            }
        }
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                gravity += modifier.additionalProjectileGravity();
            }
        }
        return gravity;
    }

    public static float getModifiedSpread(ItemStack weapon, float spread) {
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                spread = modifier.modifyProjectileSpread(spread);
            }
        }

        return spread;
    }

    public static float getModifiedSpread(Player player, ItemStack weapon, float spread) {

        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                spread = modifier.modifyProjectileSpread(spread);
            }
        }

        spread = GunEnchantmentHelper.getHotBarrelSpread(player, weapon, spread);

        spread = ExoSuitRecoilHelper.getModifiedSpread(player, spread);

        return spread;
    }

    public static double getModifiedProjectileSpeed(ItemStack weapon, double speed)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                speed = modifier.modifyProjectileSpeed(speed);
            }
        }
        return speed;
    }

    public static float getFireSoundVolume(ItemStack weapon)
    {
        float volume = 1.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                volume = modifier.modifyFireSoundVolume(volume);
            }
        }
        return Mth.clamp(volume, 0.0F, 16.0F);
    }

    public static double getMuzzleFlashScale(ItemStack weapon, double scale)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                scale = modifier.modifyMuzzleFlashScale(scale);
            }
        }
        return scale;
    }

    public static float getRecoilModifier(ItemStack weapon) {
        float recoilReduction = 1.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers) {
                recoilReduction *= Mth.clamp(modifier.recoilModifier(), 0.0F, 1.0F);
            }
        }
        return 1.0F - recoilReduction;
    }

    public static float getRecoilModifier(Player player, ItemStack weapon) {
        float baseModifier = getRecoilModifier(weapon);
        float enchantmentIncrease = GunEnchantmentHelper.getRecoilModifier(player, weapon) - 1.0F;

        return baseModifier + enchantmentIncrease;
    }

    public static float getKickReduction(ItemStack weapon) {
        float kickReduction = 1.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers) {
                kickReduction *= Mth.clamp(modifier.kickModifier(), 0.0F, 1.0F);
            }
        }
        return 1.0F - kickReduction;
    }

    public static float getKickReduction(Player player, ItemStack weapon) {
        float baseModifier = getKickReduction(weapon);
        float enchantmentIncrease = GunEnchantmentHelper.getKickModifier(player, weapon) - 1.0F;
        return baseModifier + enchantmentIncrease;
    }

    public static boolean isSilencedFire(ItemStack weapon) {
        if (weapon.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(weapon);
            if (gun.getGeneral().isSilenced()) {
                return true;
            }
        }
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                if (modifier.silencedFire()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static double getModifiedFireSoundRadius(ItemStack weapon, double radius)
    {
        double minRadius = radius;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                double newRadius = modifier.modifyFireSoundRadius(radius);
                if(newRadius < minRadius)
                {
                    minRadius = newRadius;
                }
            }
        }
        return Mth.clamp(minRadius, 0.0, Double.MAX_VALUE);
    }

    public static float getAdditionalDamage(ItemStack weapon, boolean meleeOnly) {
        float additionalDamage = 0.0F;
        for (IAttachment.Type type : IAttachment.Type.values()) {
            IGunModifier[] modifiers = getModifiers(weapon, type);
            for (IGunModifier modifier : modifiers) {
                if (meleeOnly || modifier.isMeleeOnly()) {
                    additionalDamage += modifier.additionalDamage();
                }
            }
        }
        return additionalDamage;
    }

    public static float getModifiedProjectileDamage(ItemStack weapon, float damage) {
        float finalDamage = damage;
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                if (modifier.isMeleeOnly()) {
                    finalDamage = modifier.modifyProjectileDamage(finalDamage);
                }
            }
        }
        return finalDamage;
    }

    public static float getModifiedDamage(ItemStack weapon, Gun modifiedGun, float damage) {
        float finalDamage = damage;
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                if (modifier.isMeleeOnly()) {
                    finalDamage = modifier.modifyProjectileDamage(finalDamage);
                }
            }
        }
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                if (modifier.isMeleeOnly()) {
                    finalDamage += modifier.additionalDamage();
                }
            }
        }
        return finalDamage;
    }

    public static double getModifiedAimDownSightSpeed(ItemStack weapon, double speed)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                speed = modifier.modifyAimDownSightSpeed(speed);
            }
        }
        return Mth.clamp(speed, 0.01, Double.MAX_VALUE);
    }

    public static int getModifiedRate(ItemStack weapon, int rate)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                rate = modifier.modifyFireRate(rate);
            }
        }
        return Mth.clamp(rate, 0, Integer.MAX_VALUE);
    }

    public static float getCriticalChance(ItemStack weapon)
    {
        float chance = 0F;

        if (weapon.getItem() instanceof GunItem gunItem) {
            chance += gunItem.getModifiedGun(weapon).getGeneral().getCriticalChance();
        }
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                chance += modifier.criticalChance();
            }
        }
        chance += GunEnchantmentHelper.getPuncturingChance(weapon);

        return Mth.clamp(chance, 0F, 1F);
    }

    public static int getModifiedAmmoCapacity(ItemStack weapon, Gun modifiedGun) {
        int baseCapacity = modifiedGun.getReloads().getMaxAmmo();
        for (IGunModifier modifier : getModifiers(weapon, IAttachment.Type.MAGAZINE)) {
            baseCapacity = modifier.modifyAmmoCapacity(baseCapacity);
        }
        return baseCapacity;
    }

    public static double getModifiedReloadSpeed(ItemStack weapon, double reloadSpeed) {
        for (IGunModifier modifier : GunModifierHelper.getModifiers(weapon, IAttachment.Type.MAGAZINE)) {
            reloadSpeed = modifier.modifyReloadSpeed(reloadSpeed);
        }
        return reloadSpeed;
    }

    public static double getModifiedDrawSpeed(ItemStack weapon, double speed)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                speed = modifier.modifyDrawSpeed(speed);
            }
        }
        return Mth.clamp(speed, 0.01, Double.MAX_VALUE);
    }
}