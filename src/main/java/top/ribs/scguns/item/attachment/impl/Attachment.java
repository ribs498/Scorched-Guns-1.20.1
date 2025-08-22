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
import top.ribs.scguns.common.GunModifiers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public abstract class Attachment
{
    protected IGunModifier[] modifiers;
    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("0.#");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

    Attachment(IGunModifier... modifiers)
    {
        this.modifiers = modifiers;
    }

    public IGunModifier[] getModifiers()
    {
        return this.modifiers;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void addInformationEvent(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        if(stack.getItem() instanceof IAttachment<?>)
        {
            IAttachment<?> attachment = (IAttachment<?>) stack.getItem();
            List<Component> enhancedTooltips = generateEnhancedTooltips(attachment);

            if (!enhancedTooltips.isEmpty()) {
                event.getToolTip().add(Component.translatable("tooltip.scguns.attachment.stats").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
                event.getToolTip().addAll(enhancedTooltips);
            }
        }
    }

    private static List<Component> generateEnhancedTooltips(IAttachment<?> attachment) {
        List<Component> tooltips = new ArrayList<>();
        IGunModifier[] modifiers = attachment.getProperties().getModifiers();

        AttachmentStats stats = analyzeAttachmentStats(modifiers);

        addDamageTooltips(tooltips, stats);
        addAccuracyTooltips(tooltips, stats);
        addHandlingTooltips(tooltips, stats);
        addSpecialTooltips(tooltips, stats);

        return tooltips;
    }

    private static AttachmentStats analyzeAttachmentStats(IGunModifier[] modifiers) {
        AttachmentStats stats = new AttachmentStats();

        float baseDamage = 10.0f;
        float baseSpread = 2.0f;
        double baseSpeed = 20.0;
        double baseAdsSpeed = 1.0;
        float baseRecoil = 1.0f;
        float baseKick = 1.0f;
        int baseRate = 10;
        double baseReloadSpeed = 1.0;
        int baseAmmoCapacity = 30;

        for (IGunModifier modifier : modifiers) {
            stats.additionalDamage += modifier.additionalDamage();
            baseDamage = modifier.modifyProjectileDamage(baseDamage);
            stats.criticalChance += modifier.criticalChance();

            baseSpread = modifier.modifyProjectileSpread(baseSpread);
            baseSpeed = modifier.modifyProjectileSpeed(baseSpeed);

            baseAdsSpeed = modifier.modifyAimDownSightSpeed(baseAdsSpeed);
            baseRate = modifier.modifyFireRate(baseRate);
            baseReloadSpeed = modifier.modifyReloadSpeed(baseReloadSpeed);
            stats.ammoCapacity = modifier.modifyAmmoCapacity(baseAmmoCapacity);
            if (modifier == GunModifiers.EXTENDED_BARREL_MODIFIER) {
                baseRecoil *= 1.15F;
                baseKick *= 1.2F;
            } else {
                baseRecoil *= modifier.recoilModifier();
                baseKick *= modifier.kickModifier();
            }
            if (modifier.silencedFire()) stats.silenced = true;
        }

        stats.damageMultiplier = (baseDamage - 10.0f) / 10.0f;
        stats.spreadReduction = (2.0f - baseSpread) / 2.0f;
        stats.speedMultiplier = (baseSpeed - 20.0) / 20.0;
        stats.adsSpeedMultiplier = (baseAdsSpeed - 1.0);
        stats.recoilReduction = (1.0f - baseRecoil);
        stats.kickReduction = (1.0f - baseKick);
        stats.fireRateChange = (10 - baseRate) / 10.0f;
        stats.reloadSpeedChange = (1.0 - baseReloadSpeed);
        stats.capacityMultiplier = (stats.ammoCapacity - baseAmmoCapacity) / (float)baseAmmoCapacity;

        return stats;
    }

    private static void addDamageTooltips(List<Component> tooltips, AttachmentStats stats) {
        if (stats.additionalDamage != 0) {
            String damageText = (stats.additionalDamage > 0 ? "+" : "") + DECIMAL_FORMAT.format(stats.additionalDamage / 2.0);
            Component tooltip = Component.translatable("tooltip.scguns.attachment.damage.additional", damageText)
                    .withStyle(stats.additionalDamage > 0 ? ChatFormatting.GREEN : ChatFormatting.RED);
            tooltips.add(tooltip);
        }

        if (Math.abs(stats.damageMultiplier) > 0.001f) {
            String percentText = (stats.damageMultiplier > 0 ? "+" : "") + PERCENTAGE_FORMAT.format(stats.damageMultiplier * 100) + "%";
            Component tooltip = Component.translatable("tooltip.scguns.attachment.damage.multiplier", percentText)
                    .withStyle(stats.damageMultiplier > 0 ? ChatFormatting.GREEN : ChatFormatting.RED);
            tooltips.add(tooltip);
        }

        if (stats.criticalChance > 0) {
            String critText = "+" + PERCENTAGE_FORMAT.format(stats.criticalChance * 100) + "%";
            Component tooltip = Component.translatable("tooltip.scguns.attachment.critical_chance", critText)
                    .withStyle(ChatFormatting.YELLOW);
            tooltips.add(tooltip);
        }
    }

    private static void addAccuracyTooltips(List<Component> tooltips, AttachmentStats stats) {
        if (Math.abs(stats.spreadReduction) > 0.001f) {
            String spreadText = (stats.spreadReduction > 0 ? "-" : "+") + PERCENTAGE_FORMAT.format(Math.abs(stats.spreadReduction * 100)) + "%";
            Component tooltip = Component.translatable("tooltip.scguns.attachment.spread", spreadText)
                    .withStyle(stats.spreadReduction > 0 ? ChatFormatting.GREEN : ChatFormatting.RED);
            tooltips.add(tooltip);
        }

        if (Math.abs(stats.speedMultiplier) > 0.001) {
            String speedText = (stats.speedMultiplier > 0 ? "+" : "") + PERCENTAGE_FORMAT.format(stats.speedMultiplier * 100) + "%";
            Component tooltip = Component.translatable("tooltip.scguns.attachment.projectile_speed", speedText)
                    .withStyle(stats.speedMultiplier > 0 ? ChatFormatting.GREEN : ChatFormatting.RED);
            tooltips.add(tooltip);
        }
    }

    private static void addHandlingTooltips(List<Component> tooltips, AttachmentStats stats) {
        if (Math.abs(stats.adsSpeedMultiplier) > 0.001) {
            String adsText = (stats.adsSpeedMultiplier > 0 ? "+" : "") + PERCENTAGE_FORMAT.format(stats.adsSpeedMultiplier * 100) + "%";
            Component tooltip = Component.translatable("tooltip.scguns.attachment.ads_speed", adsText)
                    .withStyle(stats.adsSpeedMultiplier > 0 ? ChatFormatting.GREEN : ChatFormatting.RED);
            tooltips.add(tooltip);
        }

        if (Math.abs(stats.recoilReduction) > 0.001f) {
            String recoilText = (stats.recoilReduction > 0 ? "-" : "+") + PERCENTAGE_FORMAT.format(Math.abs(stats.recoilReduction * 100)) + "%";
            Component tooltip = Component.translatable("tooltip.scguns.attachment.recoil", recoilText)
                    .withStyle(stats.recoilReduction > 0 ? ChatFormatting.GREEN : ChatFormatting.RED);
            tooltips.add(tooltip);
        }

        if (Math.abs(stats.kickReduction) > 0.001f) {
            String kickText = (stats.kickReduction > 0 ? "-" : "+") + PERCENTAGE_FORMAT.format(Math.abs(stats.kickReduction * 100)) + "%";
            Component tooltip = Component.translatable("tooltip.scguns.attachment.kick", kickText)
                    .withStyle(stats.kickReduction > 0 ? ChatFormatting.GREEN : ChatFormatting.RED);
            tooltips.add(tooltip);
        }

        if (Math.abs(stats.fireRateChange) > 0.001f) {
            String rateText = (stats.fireRateChange > 0 ? "+" : "") + PERCENTAGE_FORMAT.format(stats.fireRateChange * 100) + "%";
            Component tooltip = Component.translatable("tooltip.scguns.attachment.fire_rate", rateText)
                    .withStyle(stats.fireRateChange > 0 ? ChatFormatting.GREEN : ChatFormatting.RED);
            tooltips.add(tooltip);
        }

        if (Math.abs(stats.reloadSpeedChange) > 0.001) {
            String reloadText = (stats.reloadSpeedChange > 0 ? "+" : "") + PERCENTAGE_FORMAT.format(stats.reloadSpeedChange * 100) + "%";
            Component tooltip = Component.translatable("tooltip.scguns.attachment.reload_speed", reloadText)
                    .withStyle(stats.reloadSpeedChange > 0 ? ChatFormatting.GREEN : ChatFormatting.RED);
            tooltips.add(tooltip);
        }

        if (Math.abs(stats.capacityMultiplier) > 0.001) {
            String capacityText = (stats.capacityMultiplier > 0 ? "+" : "") + PERCENTAGE_FORMAT.format(stats.capacityMultiplier * 100) + "%";
            Component tooltip = Component.translatable("tooltip.scguns.attachment.ammo_capacity", capacityText)
                    .withStyle(stats.capacityMultiplier > 0 ? ChatFormatting.GREEN : ChatFormatting.RED);
            tooltips.add(tooltip);
        }
    }

    private static void addSpecialTooltips(List<Component> tooltips, AttachmentStats stats) {
        if (stats.silenced) {
            Component tooltip = Component.translatable("tooltip.scguns.attachment.silenced")
                    .withStyle(ChatFormatting.AQUA);
            tooltips.add(tooltip);
        }
    }
    private static class AttachmentStats {
        float additionalDamage = 0f;
        float damageMultiplier = 0f;
        float criticalChance = 0f;
        float spreadReduction = 0f;
        double speedMultiplier = 0.0;
        double adsSpeedMultiplier = 0.0;
        float recoilReduction = 0f;
        float kickReduction = 0f;
        float fireRateChange = 0f;
        double reloadSpeedChange = 0.0;
        int ammoCapacity = 30;
        float capacityMultiplier = 0f;
        boolean silenced = false;
    }
}