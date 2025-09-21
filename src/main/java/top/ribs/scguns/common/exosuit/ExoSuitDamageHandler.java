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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Handles damage distribution to ExoSuit components
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExoSuitDamageHandler {

    private static final Random RANDOM = new Random();

    private static final float PLATING_ABSORPTION = 0.8f;     // 80% of damage absorbed by plating
    private static final float COMPONENT_ABSORPTION = 0.3f;   // 30% of damage absorbed by other components
    private static final float EXOSUIT_ABSORPTION = 0.1f;     // 10% of damage goes to the ExoSuit frame itself

    private static final long DURABILITY_DAMAGE_COOLDOWN = 1000;
    private static final Map<UUID, Long> lastDurabilityDamage = new HashMap<>();

    private static final long CONTINUOUS_DAMAGE_COOLDOWN = 2000;
    private static final String[] CONTINUOUS_DAMAGE_SOURCES = {
            "slime", "magmaCube", "mob", "sweetBerryBush", "cactus", "hotFloor"
    };

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }
        List<ItemStack> exoSuitPieces = getEquippedExoSuitPieces(player);
        if (exoSuitPieces.isEmpty()) {
            return;
        }

        UUID playerUUID = player.getUUID();
        long currentTime = System.currentTimeMillis();

        long cooldownTime = isContinuousDamageSource(event.getSource().getMsgId())
                ? CONTINUOUS_DAMAGE_COOLDOWN
                : DURABILITY_DAMAGE_COOLDOWN;

        Long lastDamageTime = lastDurabilityDamage.get(playerUUID);
        if (lastDamageTime != null && (currentTime - lastDamageTime) < cooldownTime) {
            return;
        }

        lastDurabilityDamage.put(playerUUID, currentTime);
        cleanupOldEntries(currentTime);

        float damageToDistribute = event.getAmount();
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
        List<ItemStack> exoSuitPieces = getEquippedExoSuitPieces(player);
        if (exoSuitPieces.isEmpty()) {
            return;
        }

        float totalDamageReduction = calculatePlatingDamageReduction(exoSuitPieces, event.getAmount());

        if (totalDamageReduction > 0) {
            float newDamage = Math.max(0, event.getAmount() - totalDamageReduction);
            event.setAmount(newDamage);
        }
    }

    /**
     * Check if the damage source is from a continuous damage source like slimes
     */
    private static boolean isContinuousDamageSource(String damageSourceId) {
        if (damageSourceId == null) return false;

        String lowerSource = damageSourceId.toLowerCase();
        for (String continuousSource : CONTINUOUS_DAMAGE_SOURCES) {
            if (lowerSource.contains(continuousSource)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clean up old cooldown entries to prevent memory leaks
     */
    private static void cleanupOldEntries(long currentTime) {
        if (RANDOM.nextInt(100) == 0) {
            lastDurabilityDamage.entrySet().removeIf(entry ->
                    (currentTime - entry.getValue()) > Math.max(DURABILITY_DAMAGE_COOLDOWN, CONTINUOUS_DAMAGE_COOLDOWN) * 2
            );
        }
    }

    private static float calculatePlatingDamageReduction(List<ItemStack> exoSuitPieces, float damage) {
        float totalReduction = 0;
        int functionalPlatingCount = 0;

        for (ItemStack exoSuitPiece : exoSuitPieces) {
            List<ItemStack> upgradeItems = getUpgradeItems(exoSuitPiece);
            ItemStack plating = findUpgradeByType(upgradeItems, "plating");

            if (plating != null && isUpgradeFunctional(plating)) {
                functionalPlatingCount++;
            }
        }

        if (functionalPlatingCount > 0) {
            float baseReduction = damage * 0.15f;
            totalReduction = baseReduction * Math.min(functionalPlatingCount, 4);

            if (functionalPlatingCount > 1) {
                totalReduction *= (0.8f + (0.2f / functionalPlatingCount));
            }
        }

        return totalReduction;
    }

    private static List<ItemStack> getEquippedExoSuitPieces(Player player) {
        List<ItemStack> exoSuitPieces = new ArrayList<>();

        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem) {
                exoSuitPieces.add(armorStack);
            }
        }

        return exoSuitPieces;
    }

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
            int newDamage = upgradeItem.getDamageValue() + damage;
            int maxDamage = upgradeItem.getMaxDamage();

            upgradeItem.setDamageValue(Math.min(newDamage, maxDamage));

            if (upgradeItem.getDamageValue() >= maxDamage && maxDamage > 0) {
            }
        }
    }
    private static boolean isUpgradeFunctional(ItemStack upgradeItem) {
        return !upgradeItem.isEmpty() && !isUpgradeBroken(upgradeItem);
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
        return upgradeItem.getDamageValue() >= upgradeItem.getMaxDamage() && upgradeItem.getMaxDamage() > 0;
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
            CompoundTag slotTag = upgradeList.getCompound(i);

            if (slotTag.contains("Item")) {
                ItemStack upgradeStack = ItemStack.of(slotTag.getCompound("Item"));

                if (!upgradeStack.isEmpty() && !isUpgradeBroken(upgradeStack)) {
                    newUpgradeList.add(slotTag);
                } else if (!upgradeStack.isEmpty()) {
                    removedAny = true;
                }
            } else {
                newUpgradeList.add(slotTag);
            }
        }

        if (removedAny) {
            upgradeData.put("Upgrades", newUpgradeList);
            ExoSuitData.setUpgradeData(exoSuitPiece, upgradeData);
        }
    }
}