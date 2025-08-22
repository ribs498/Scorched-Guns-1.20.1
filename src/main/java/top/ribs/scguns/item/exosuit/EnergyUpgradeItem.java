package top.ribs.scguns.item.exosuit;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.ribs.scguns.common.exosuit.ExoSuitUpgrade;
import top.ribs.scguns.common.exosuit.ExoSuitUpgradeManager;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Base class for ExoSuit upgrades that consume energy from the power core
 */
public class EnergyUpgradeItem extends DamageableUpgradeItem {
    private final EnergyConsumptionType consumptionType;

    public EnergyUpgradeItem(Properties properties) {
        this(properties, EnergyConsumptionType.PER_TICK);
    }

    public EnergyUpgradeItem(Properties properties, EnergyConsumptionType consumptionType) {
        super(properties);
        this.consumptionType = consumptionType;
    }

    public int getEnergyConsumption() {
        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(this);
        if (upgrade != null) {
            return (int) upgrade.getEffects().getEnergyUse();
        }
        return 10;
    }

    public EnergyConsumptionType getConsumptionType() {
        return consumptionType;
    }

    public boolean canFunctionWithoutPower() {
        return false;
    }


    public enum EnergyConsumptionType {
        PER_TICK,     // Constant drain while active (like night vision)
        PER_USE,      // One-time cost per activation (like jetpack boost)
        PER_SECOND,   // Drain per second instead of per tick (easier to understand)
        ACTIVATION    // One-time cost to turn on/off
    }
}