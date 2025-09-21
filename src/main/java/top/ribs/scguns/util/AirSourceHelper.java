
package top.ribs.scguns.util;

import com.simibubi.create.content.equipment.armor.BacktankUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.item.AirCanisterItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;

public class AirSourceHelper {

    public static List<ItemStack> findAirCanistersWithAir(Player player) {
        List<ItemStack> airCanisters = new ArrayList<>();
        for (ItemStack stack : player.getInventory().items) {
            if (isAirCanisterWithAir(stack)) {
                airCanisters.add(stack);
            }
        }
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                ItemStack stack = curios.getStackInSlot(i);
                if (isAirCanisterWithAir(stack)) {
                    airCanisters.add(stack);
                }
            }
        });

        return airCanisters;
    }
    public static AirSource getBestAirSource(Player player) {
        if (ScorchedGuns.createLoaded) {
            List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
            if (!backtanks.isEmpty()) {
                return new AirSource(AirSource.Type.CREATE_BACKTANK, backtanks.get(0));
            }
        }
        List<ItemStack> airCanisters = findAirCanistersWithAir(player);
        if (!airCanisters.isEmpty()) {
            return new AirSource(AirSource.Type.AIR_CANISTER, airCanisters.get(0));
        }

        return AirSource.NONE;
    }
    public static boolean consumeAir(Player player, float airCost) {
        AirSource airSource = getBestAirSource(player);

        switch (airSource.getType()) {
            case CREATE_BACKTANK:
                if (ScorchedGuns.createLoaded) {
                    if (BacktankUtil.hasAirRemaining(airSource.getStack()) &&
                            BacktankUtil.getAir(airSource.getStack()) >= airCost) {
                        BacktankUtil.consumeAir(player, airSource.getStack(), airCost);
                        return true;
                    }
                    return false;
                }
                break;

            case AIR_CANISTER:
                IEnergyStorage energyStorage = airSource.getStack().getCapability(ForgeCapabilities.ENERGY).orElse(null);
                if (energyStorage.getEnergyStored() >= airCost) {
                    int airExtracted = energyStorage.extractEnergy((int) airCost, false);
                    return airExtracted >= airCost;
                }
                return false;

            case NONE:
            default:
                return false;
        }

        return false;
    }
    public static AirInfo getAirInfo(Player player) {
        AirSource airSource = getBestAirSource(player);

        switch (airSource.getType()) {
            case CREATE_BACKTANK:
                if (ScorchedGuns.createLoaded) {
                    ItemStack backtank = airSource.getStack();
                    int maxAir = BacktankUtil.maxAir(backtank);
                    float air = BacktankUtil.getAir(backtank);
                    int barWidth = Math.round(13.0F * air / maxAir);
                    int barColor = BacktankUtil.getBarColor(backtank, 1);
                    return new AirInfo(barWidth, barColor, AirSource.Type.CREATE_BACKTANK);
                }
                break;

            case AIR_CANISTER:
                ItemStack canister = airSource.getStack();
                if (canister.getItem() instanceof AirCanisterItem airCanisterItem) {
                    int stored = airCanisterItem.getAirStored(canister);
                    int max = airCanisterItem.getMaxAirStored(canister);
                    int barWidth = max > 0 ? Math.round(13.0F * stored / max) : 0;

                    float ratio = max > 0 ? (float) stored / max : 0;
                    int barColor;
                    if (ratio < 0.25f) {
                        barColor = 0xFF4444; // Red
                    } else if (ratio < 0.5f) {
                        barColor = 0xFFAA00; // Orange  
                    } else {
                        barColor = 0x00AAFF; // Blue
                    }

                    return new AirInfo(barWidth, barColor, AirSource.Type.AIR_CANISTER);
                }
                break;
        }

        return AirInfo.NONE;
    }

    private static boolean isAirCanisterWithAir(ItemStack stack) {
        if (!(stack.getItem() instanceof AirCanisterItem)) {
            return false;
        }

        IEnergyStorage energyStorage = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        return energyStorage.getEnergyStored() > 0;
    }

    public static class AirSource {
        public enum Type {
            NONE,
            CREATE_BACKTANK,
            AIR_CANISTER
        }

        public static final AirSource NONE = new AirSource(Type.NONE, ItemStack.EMPTY);

        private final Type type;
        private final ItemStack stack;

        public AirSource(Type type, ItemStack stack) {
            this.type = type;
            this.stack = stack;
        }

        public Type getType() {
            return type;
        }

        public ItemStack getStack() {
            return stack;
        }

        public boolean isAvailable() {
            return type != Type.NONE && !stack.isEmpty();
        }
    }

    public record AirInfo(int barWidth, int barColor, AirSource.Type sourceType) {
            public static final AirInfo NONE = new AirInfo(0, 0x808080, AirSource.Type.NONE);

    }
}