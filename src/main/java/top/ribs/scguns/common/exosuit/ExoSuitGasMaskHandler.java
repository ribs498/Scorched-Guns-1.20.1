package top.ribs.scguns.common.exosuit;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.block.SulfurVentBlock;
import top.ribs.scguns.common.SulfurGasCloud;
import top.ribs.scguns.item.exosuit.GasMaskModuleItem;

/**
 * Handles gas mask functionality for ExoSuit helmets
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExoSuitGasMaskHandler {

    private static final int CHECK_INTERVAL = 20;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;

        if (player.tickCount % CHECK_INTERVAL != 0) {
            return;
        }

        handleGasMask(player);
    }

    private static void handleGasMask(Player player) {
        if (hasGasMaskModule(player)) {
            return;
        }

        if (!isPlayerInGasArea(player)) {
            return;
        }

        ItemStack gasMaskUpgrade = findGasMaskModule(player);
        if (!gasMaskUpgrade.isEmpty() && gasMaskUpgrade.getItem() instanceof GasMaskModuleItem gasMaskModule) {
            if (!gasMaskModule.canFunctionWithoutPower()) {
                ExoSuitPowerManager.consumeEnergyForUpgrade(player, "breathing", gasMaskUpgrade);
            }
        }
    }

    private static boolean hasGasMaskModule(Player player) {
        return findGasMaskModule(player).isEmpty();
    }

    private static ItemStack findGasMaskModule(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof top.ribs.scguns.item.animated.ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.HELMET) {

                for (int slot = 0; slot < 4; slot++) {
                    ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
                    if (!upgradeItem.isEmpty()) {
                        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                        if (upgrade != null && upgrade.getType().equals("breathing") &&
                                upgradeItem.getItem() instanceof GasMaskModuleItem) {
                            return upgradeItem;
                        }
                    }
                }
                break;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isPlayerInGasArea(Player player) {
        Vec3 playerPos = player.position();
        return SulfurGasCloud.isInGasEffectArea(player.level(), playerPos, SulfurVentBlock.EFFECT_RADIUS);
    }

    public static boolean hasProtection(Player player) {
        if (hasGasMaskModule(player)) {
            return false;
        }

        ItemStack gasMaskUpgrade = findGasMaskModule(player);
        if (!gasMaskUpgrade.isEmpty() && gasMaskUpgrade.getItem() instanceof GasMaskModuleItem gasMaskModule) {
            if (gasMaskModule.canFunctionWithoutPower()) {
                return true;
            }

            return ExoSuitPowerManager.canUpgradeFunction(player, "breathing");
        }

        return false;
    }
}