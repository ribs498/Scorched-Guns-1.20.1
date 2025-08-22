package top.ribs.scguns.common.exosuit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.ExoSuitItem;
import top.ribs.scguns.item.exosuit.DamageableUpgradeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles damage distribution to ExoSuit components
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExoSuitDamageHandler {

    private static final Random RANDOM = new Random();

    private static final float PLATING_ABSORPTION = 0.8f;     // 80% of damage absorbed by plating
    private static final float COMPONENT_ABSORPTION = 0.3f;   // 30% of damage absorbed by other components
    private static final float EXOSUIT_ABSORPTION = 0.1f;     // 10% of damage goes to the ExoSuit frame itself

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }

        // FIXED: Only proceed if player is wearing ExoSuit pieces
        List<ItemStack> exoSuitPieces = getEquippedExoSuitPieces(player);
        if (exoSuitPieces.isEmpty()) {
            return; // No ExoSuit armor = no special processing
        }

        float originalDamage = event.getAmount();
        float damageToDistribute = originalDamage;
        boolean componentsChanged = false;

        for (ItemStack exoSuitPiece : exoSuitPieces) {
            float remainingDamage = distributeDamageToComponents(exoSuitPiece, damageToDistribute / exoSuitPieces.size());

            if (remainingDamage != damageToDistribute / exoSuitPieces.size()) {
                componentsChanged = true;
            }

            if (RANDOM.nextFloat() < EXOSUIT_ABSORPTION) {
                exoSuitPiece.setDamageValue(exoSuitPiece.getDamageValue() + 1);
            }
        }

        if (componentsChanged) {
            player.level().getServer().execute(() -> {
                ExoSuitEffectsHandler.applyExoSuitEffects(player);
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }

        // FIXED: Only proceed if player is wearing ExoSuit pieces
        List<ItemStack> exoSuitPieces = getEquippedExoSuitPieces(player);
        if (exoSuitPieces.isEmpty()) {
            return; // No ExoSuit armor = no damage reduction processing
        }

        float totalDamageReduction = calculatePlatingDamageReduction(exoSuitPieces, event.getAmount());

        if (totalDamageReduction > 0) {
            float newDamage = Math.max(0, event.getAmount() - totalDamageReduction);
            event.setAmount(newDamage);
        }
    }

    private static float calculatePlatingDamageReduction(List<ItemStack> exoSuitPieces, float damage) {
        float totalReduction = 0;
        int functionalPlatingCount = 0;

        for (ItemStack exoSuitPiece : exoSuitPieces) {
            List<ItemStack> upgradeItems = getUpgradeItems(exoSuitPiece);
            ItemStack plating = findUpgradeByType(upgradeItems, "plating");

            if (plating != null && !plating.isEmpty() && !isUpgradeBroken(plating)) {
                functionalPlatingCount++;
            }
        }

        if (functionalPlatingCount > 0) {
            float baseReduction = damage * 0.15f; // Each piece provides 15% reduction
            totalReduction = baseReduction * Math.min(functionalPlatingCount, 4); // Cap at 4 pieces

            if (functionalPlatingCount > 1) {
                totalReduction *= (0.8f + (0.2f / functionalPlatingCount));
            }
        }

        return totalReduction;
    }

    private static List<ItemStack> getEquippedExoSuitPieces(Player player) {
        List<ItemStack> exoSuitPieces = new ArrayList<>();

        for (ItemStack armorStack : player.getArmorSlots()) {
            // FIXED: Only include actual ExoSuit items
            if (armorStack.getItem() instanceof ExoSuitItem) {
                exoSuitPieces.add(armorStack);
            }
        }

        return exoSuitPieces;
    }

    // Rest of the methods remain the same...
    private static float distributeDamageToComponents(ItemStack exoSuitPiece, float incomingDamage) {
        float remainingDamage = incomingDamage;
        List<ItemStack> upgradeItems = getUpgradeItems(exoSuitPiece);

        if (upgradeItems.isEmpty()) {
            return remainingDamage;
        }

        boolean needsUpdate = false;

        ItemStack plating = findUpgradeByType(upgradeItems, "plating");
        if (plating != null && !plating.isEmpty() && isUpgradeDamageable(plating) && !isUpgradeBroken(plating)) {
            float absorbedDamage = remainingDamage * PLATING_ABSORPTION;
            damageUpgradeItem(plating, (int) Math.ceil(absorbedDamage * 0.1f));
            remainingDamage -= absorbedDamage;

            if (isUpgradeBroken(plating)) {
                needsUpdate = true;
            }
        }
        int nonPlatingComponents = 0;
        for (ItemStack upgrade : upgradeItems) {
            if (upgrade != plating && !upgrade.isEmpty()) {
                ExoSuitUpgrade upgradeData = ExoSuitUpgradeManager.getUpgradeForItem(upgrade);
                if (upgradeData != null && !upgradeData.getType().equals("plating")) {
                    nonPlatingComponents++;
                }
            }
        }

        if (nonPlatingComponents > 0 && remainingDamage > 0) {
            for (ItemStack upgrade : upgradeItems) {
                if (upgrade != plating && !upgrade.isEmpty() && remainingDamage > 0) {
                    ExoSuitUpgrade upgradeData = ExoSuitUpgradeManager.getUpgradeForItem(upgrade);
                    if (upgradeData != null && !upgradeData.getType().equals("plating") &&
                            isUpgradeDamageable(upgrade) && !isUpgradeBroken(upgrade)) {

                        float componentDamage = remainingDamage * COMPONENT_ABSORPTION * (1.0f / nonPlatingComponents);
                        damageUpgradeItem(upgrade, (int) Math.ceil(componentDamage * 0.05f));
                        remainingDamage -= componentDamage;

                        if (isUpgradeBroken(upgrade)) {
                            needsUpdate = true;
                        }
                    }
                }
            }
        }

        if (needsUpdate) {
            removeBrokenComponents(exoSuitPiece);
        }

        return Math.max(0, remainingDamage);
    }

    private static List<ItemStack> getUpgradeItems(ItemStack exoSuitPiece) {
        List<ItemStack> upgrades = new ArrayList<>();

        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(exoSuitPiece, slot);
            if (!upgradeItem.isEmpty()) {
                upgrades.add(upgradeItem);
            }
        }

        return upgrades;
    }

    private static ItemStack findUpgradeByType(List<ItemStack> upgrades, String type) {
        for (ItemStack upgrade : upgrades) {
            ExoSuitUpgrade upgradeData = ExoSuitUpgradeManager.getUpgradeForItem(upgrade);
            if (upgradeData != null && upgradeData.getType().equals(type)) {
                return upgrade;
            }
        }
        return null;
    }

    private static void damageUpgradeItem(ItemStack upgradeItem, int damage) {
        if (!isUpgradeDamageable(upgradeItem) || damage <= 0) {
            return;
        }

        if (upgradeItem.getItem() instanceof DamageableUpgradeItem damageableUpgrade) {
            damageableUpgrade.onUpgradeDamaged(upgradeItem, damage);
        } else if (upgradeItem.isDamageableItem()) {
            upgradeItem.setDamageValue(Math.min(upgradeItem.getDamageValue() + damage, upgradeItem.getMaxDamage()));
        }
    }
    private static boolean isUpgradeDamageable(ItemStack upgradeItem) {
        if (upgradeItem.isEmpty()) {
            return false;
        }
        if (upgradeItem.getItem() instanceof DamageableUpgradeItem) {
            return true;
        }
        if (upgradeItem.isDamageableItem()) {
            return true;
        }
        return upgradeItem.getMaxDamage() > 0;
    }

    private static boolean isUpgradeBroken(ItemStack upgradeItem) {
        if (!isUpgradeDamageable(upgradeItem)) {
            return false;
        }

        if (upgradeItem.getItem() instanceof DamageableUpgradeItem damageableUpgrade) {
            return damageableUpgrade.isBroken(upgradeItem);
        }
        return upgradeItem.getDamageValue() >= upgradeItem.getMaxDamage();
    }

    private static void removeBrokenComponents(ItemStack exoSuitPiece) {
        CompoundTag upgradeData = ExoSuitData.getUpgradeData(exoSuitPiece);

        if (!upgradeData.contains("Upgrades")) {
            return;
        }

        ListTag upgradeList = upgradeData.getList("Upgrades", 10);
        ListTag newUpgradeList = new ListTag();
        boolean removedAny = false;

        for (int i = 0; i < upgradeList.size(); i++) {
            CompoundTag upgradeTag = upgradeList.getCompound(i);
            ItemStack upgradeStack = ItemStack.of(upgradeTag);

            if (!upgradeStack.isEmpty() && !isUpgradeBroken(upgradeStack)) {
                newUpgradeList.add(upgradeTag);
            } else if (!upgradeStack.isEmpty()) {
                removedAny = true;
            }
        }

        if (removedAny) {
            upgradeData.put("Upgrades", newUpgradeList);
            ExoSuitData.setUpgradeData(exoSuitPiece, upgradeData);
        }
    }
}