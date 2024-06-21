package top.ribs.scguns.item.attachment.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.interfaces.IGunModifier;
import top.ribs.scguns.item.attachment.IAttachment;

import java.util.ArrayList;
import java.util.List;

/**
 * The base attachment object
 *
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public abstract class Attachment
{
    protected IGunModifier[] modifiers;
    private List<Component> perks = null;

    Attachment(IGunModifier... modifiers)
    {
        this.modifiers = modifiers;
    }

    public IGunModifier[] getModifiers()
    {
        return this.modifiers;
    }

    void setPerks(List<Component> perks)
    {
        if(this.perks == null)
        {
            this.perks = perks;
        }
    }

    List<Component> getPerks()
    {
        return this.perks;
    }

    /* Determines the perks of attachments and caches them */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void addInformationEvent(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        if(stack.getItem() instanceof IAttachment<?>)
        {
            IAttachment<?> attachment = (IAttachment<?>) stack.getItem();
            List<Component> perks = attachment.getProperties().getPerks();
            if(perks != null && perks.size() > 0)
            {
                event.getToolTip().add(Component.translatable("perk.scguns.title").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
                event.getToolTip().addAll(perks);
                return;
            }

            IGunModifier[] modifiers = attachment.getProperties().getModifiers();
            List<Component> positivePerks = new ArrayList<>();
            List<Component> negativePerks = new ArrayList<>();

            /* Test for fire sound volume */
            float inputSound = 1.0F;
            float outputSound = inputSound;
            for(IGunModifier modifier : modifiers)
            {
                outputSound = modifier.modifyFireSoundVolume(outputSound);
            }
            if(outputSound > inputSound)
            {
                addPerk(negativePerks, false, "perk.scguns.fire_volume.negative");
            }
            else if(outputSound < inputSound)
            {
                addPerk(positivePerks, true, "perk.scguns.fire_volume.positive");
            }

            /* Test for silenced */
            for(IGunModifier modifier : modifiers)
            {
                if(modifier.silencedFire())
                {
                    addPerk(positivePerks, true, "perk.scguns.silenced.positive");
                    break;
                }
            }

            /* Test for sound radius */
            double inputRadius = 10.0;
            double outputRadius = inputRadius;
            for(IGunModifier modifier : modifiers)
            {
                outputRadius = modifier.modifyFireSoundRadius(outputRadius);
            }
            if(outputRadius > inputRadius)
            {
                addPerk(negativePerks, false, "perk.scguns.sound_radius.negative");
            }
            else if(outputRadius < inputRadius)
            {
                addPerk(positivePerks, true, "perk.scguns.sound_radius.positive");
            }

            /* Test for additional damage */
            float additionalDamage = 0.0F;
            for(IGunModifier modifier : modifiers)
            {
                additionalDamage += modifier.additionalDamage();
            }
            if(additionalDamage > 0.0F)
            {
                addPerk(positivePerks, true, "perk.scguns.additional_damage.positive", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage / 2.0));
            }
            else if(additionalDamage < 0.0F)
            {
                addPerk(negativePerks, false, "perk.scguns.additional_damage.negative", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage / 2.0));
            }

            /* Test for modified damage */
            float inputDamage = 10.0F;
            float outputDamage = inputDamage;
            for(IGunModifier modifier : modifiers)
            {
                outputDamage = modifier.modifyProjectileDamage(outputDamage);
            }
            if(outputDamage > inputDamage)
            {
                addPerk(positivePerks, true, "perk.scguns.modified_damage.positive");
            }
            else if(outputDamage < inputDamage)
            {
                addPerk(negativePerks, false, "perk.scguns.modified_damage.negative");
            }

            /* Test for modified damage */
            double inputSpeed = 10.0;
            double outputSpeed = inputSpeed;
            for(IGunModifier modifier : modifiers)
            {
                outputSpeed = modifier.modifyProjectileSpeed(outputSpeed);
            }
            if(outputSpeed > inputSpeed)
            {
                addPerk(positivePerks, true, "perk.scguns.projectile_speed.positive");
            }
            else if(outputSpeed < inputSpeed)
            {
                addPerk(negativePerks, false, "perk.scguns.projectile_speed.negative");
            }

            /* Test for modified projectile spread */
            float inputSpread = 10.0F;
            float outputSpread = inputSpread;
            for(IGunModifier modifier : modifiers)
            {
                outputSpread = modifier.modifyProjectileSpread(outputSpread);
            }
            if(outputSpread > inputSpread)
            {
                addPerk(negativePerks, false, "perk.scguns.projectile_spread.negative");
            }
            else if(outputSpread < inputSpread)
            {
                addPerk(positivePerks, true, "perk.scguns.projectile_spread.positive");
            }

            /* Test for modified projectile life */
            int inputLife = 100;
            int outputLife = inputLife;
            for(IGunModifier modifier : modifiers)
            {
                outputLife = modifier.modifyProjectileLife(outputLife);
            }
            if(outputLife > inputLife)
            {
                addPerk(positivePerks, true, "perk.scguns.projectile_life.positive");
            }
            else if(outputLife < inputLife)
            {
                addPerk(negativePerks, false, "perk.scguns.projectile_life.negative");
            }

            /* Test for modified recoil */
            float inputRecoil = 10.0F;
            float outputRecoil = inputRecoil;
            for(IGunModifier modifier : modifiers)
            {
                outputRecoil *= modifier.recoilModifier();
            }
            if(outputRecoil > inputRecoil)
            {
                addPerk(negativePerks, false, "perk.scguns.recoil.negative");
            }
            else if(outputRecoil < inputRecoil)
            {
                addPerk(positivePerks, true, "perk.scguns.recoil.positive");
            }

            /* Test for aim down sight speed */
            double inputAdsSpeed = 10.0;
            double outputAdsSpeed = inputAdsSpeed;
            for(IGunModifier modifier : modifiers)
            {
                outputAdsSpeed = modifier.modifyAimDownSightSpeed(outputAdsSpeed);
            }
            if(outputAdsSpeed > inputAdsSpeed)
            {
                addPerk(positivePerks, true, "perk.scguns.ads_speed.positive");
            }
            else if(outputAdsSpeed < inputAdsSpeed)
            {
                addPerk(negativePerks, false, "perk.scguns.ads_speed.negative");
            }

            /* Test for fire rate */
            int inputRate = 10;
            int outputRate = inputRate;
            for(IGunModifier modifier : modifiers)
            {
                outputRate = modifier.modifyFireRate(outputRate);
            }
            if(outputRate > inputRate)
            {
                addPerk(negativePerks, false, "perk.scguns.rate.negative");
            }
            else if(outputRate < inputRate)
            {
                addPerk(positivePerks, true, "perk.scguns.rate.positive");
            }

            /* Test for modified reload speed */
            double inputReloadSpeed = 10.0;  // Example base reload speed
            double outputReloadSpeed = inputReloadSpeed;
            for (IGunModifier modifier : modifiers) {
                outputReloadSpeed = modifier.modifyReloadSpeed(outputReloadSpeed);
            }
            if (outputReloadSpeed < inputReloadSpeed) {
                addPerk(positivePerks, true, "perk.scguns.reload_speed.positive");
            } else if (outputReloadSpeed > inputReloadSpeed) {
                addPerk(negativePerks, false, "perk.scguns.reload_speed.negative");
            }
            /* Test for modified ammo capacity */
            int baseAmmoCapacity = 30; // Example base ammo capacity, adjust as needed for your mod
            int outputAmmoCapacity = baseAmmoCapacity;
            for (IGunModifier modifier : modifiers) {
                outputAmmoCapacity = modifier.modifyAmmoCapacity(baseAmmoCapacity);
            }
            if (outputAmmoCapacity > baseAmmoCapacity) {
                addPerk(positivePerks, true, "perk.scguns.ammo_capacity.increase", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((outputAmmoCapacity - baseAmmoCapacity)));
            } else if (outputAmmoCapacity < baseAmmoCapacity) {
                addPerk(negativePerks, false, "perk.scguns.ammo_capacity.decrease", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((baseAmmoCapacity - outputAmmoCapacity)));
            }


            // Combine and set perks
            positivePerks.addAll(negativePerks);
            attachment.getProperties().setPerks(positivePerks);
            if (positivePerks.size() > 0) {
                event.getToolTip().add(Component.translatable("perk.scguns.title").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
                event.getToolTip().addAll(positivePerks);
            }
        }
    }

    private static void addPerk(List<Component> components, boolean positive, String id, Object... params) {
        components.add(Component.translatable(positive ? "perk.scguns.entry.positive" : "perk.scguns.entry.negative", Component.translatable(id, params).withStyle(ChatFormatting.WHITE)).withStyle(positive ? ChatFormatting.DARK_AQUA : ChatFormatting.GOLD));
    }
}
