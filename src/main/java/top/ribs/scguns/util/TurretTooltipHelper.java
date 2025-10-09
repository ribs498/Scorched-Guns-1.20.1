package top.ribs.scguns.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.ribs.scguns.common.Turret;
import top.ribs.scguns.common.TurretManager;

import javax.annotation.Nullable;
import java.util.List;

public class TurretTooltipHelper {

    public static void addTurretTooltip(ResourceLocation turretId, ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Turret turret = TurretManager.getTurret(turretId);
        if (turret == null) return;

        Turret.Targeting targeting = turret.getTargeting();
        Turret.Combat combat = turret.getCombat();
        Turret.Ammunition ammunition = turret.getAmmunition();

        tooltip.add(Component.translatable("info.scguns.turret.range")
                .append(": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1f", targeting.getRange())).withStyle(ChatFormatting.WHITE)));

        double shotsPerSecond = 20.0 / combat.getCooldown();
        tooltip.add(Component.translatable("info.scguns.turret.fire_rate")
                .append(": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f", shotsPerSecond)).withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("info.scguns.turret.shots_per_second").withStyle(ChatFormatting.WHITE)));

        if (combat.getPelletCount() > 1) {
            tooltip.add(Component.translatable("info.scguns.turret.pellets")
                    .append(": ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(combat.getPelletCount())).withStyle(ChatFormatting.WHITE)));
        }

        if (combat.getSpreadAngle() > 0) {
            tooltip.add(Component.translatable("info.scguns.turret.spread")
                    .append(": ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.1f", combat.getSpreadAngle())).withStyle(ChatFormatting.WHITE))
                    .append(Component.translatable("info.scguns.turret.degrees").withStyle(ChatFormatting.WHITE)));
        }

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("info.scguns.turret.accepted_ammo").withStyle(ChatFormatting.GRAY));

            for (Turret.Ammunition.AmmoType ammoType : ammunition.getAcceptedAmmo()) {
                Item ammoItem = ammoType.getItem();
                if (ammoItem != null) {
                    double damage = ammoType.getDamage();

                    tooltip.add(Component.literal("  ")
                            .append(Component.translatable(ammoItem.getDescriptionId()).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                            .append(Component.translatable("info.scguns.damage").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(": " + String.format("%.1f", damage)).withStyle(ChatFormatting.WHITE)));
                }
            }
        } else {
            tooltip.add(Component.translatable("info.scguns.turret.shift_ammo").withStyle(ChatFormatting.GRAY));
        }
    }
}