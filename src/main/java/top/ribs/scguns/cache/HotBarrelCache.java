package top.ribs.scguns.cache;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import top.ribs.scguns.init.ModEnchantments;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HotBarrelCache {

    public static class HotBarrelData {
        public float level; // Changed to float for smoother decay
        public int ticksSinceLastShot;
        public long lastUpdateTime;

        public HotBarrelData() {
            this.level = 0.0f;
            this.ticksSinceLastShot = 0;
            this.lastUpdateTime = System.currentTimeMillis();
        }
    }

    private static final Map<String, HotBarrelData> HOT_BARREL_CACHE = new ConcurrentHashMap<>();

    private static final float MAX_HOT_BARREL = 100.0f;
    private static final float DECAY_RATE_PER_TICK = 0.7f;
    private static final int DECAY_START_DELAY = 15;

    private static String generateKey(Player player, ItemStack stack) {
        return player.getUUID() + "_" +
                stack.getItem().getDescriptionId();
    }

    public static void increaseHotBarrel(Player player, ItemStack stack, int amount) {
        if (!hasHotBarrelEnchantment(stack)) return;

        String key = generateKey(player, stack);
        HotBarrelData data = HOT_BARREL_CACHE.computeIfAbsent(key, k -> new HotBarrelData());

        data.level = Math.min(MAX_HOT_BARREL, data.level + amount);
        data.ticksSinceLastShot = 0;
        data.lastUpdateTime = System.currentTimeMillis();

        HOT_BARREL_CACHE.put(key, data);
    }

    public static int getHotBarrelLevel(Player player, ItemStack stack) {
        if (!hasHotBarrelEnchantment(stack)) return 0;

        String key = generateKey(player, stack);
        HotBarrelData data = HOT_BARREL_CACHE.get(key);

        if (data == null) return 0;
        return Math.round(data.level);
    }

    public static float getSmoothHotBarrelLevel(Player player, ItemStack stack) {
        if (!hasHotBarrelEnchantment(stack)) return 0.0f;

        String key = generateKey(player, stack);
        HotBarrelData data = HOT_BARREL_CACHE.get(key);

        if (data == null || data.level <= 0.0f) return 0.0f;
        return Math.max(data.level, 0.0f);
    }
    public static void setHotBarrelLevel(Player player, ItemStack stack, int level) {
        if (!hasHotBarrelEnchantment(stack)) return;

        String key = generateKey(player, stack);
        HotBarrelData data = HOT_BARREL_CACHE.computeIfAbsent(key, k -> new HotBarrelData());

        data.level = Math.max(0.0f, Math.min(MAX_HOT_BARREL, level));
        data.lastUpdateTime = System.currentTimeMillis();

        HOT_BARREL_CACHE.put(key, data);
    }
    public static float getSmoothHotBarrelPercentage(Player player, ItemStack stack) {
        return getSmoothHotBarrelLevel(player, stack) / MAX_HOT_BARREL;
    }

    public static void tickHotBarrel(Player player, ItemStack stack) {
        if (!hasHotBarrelEnchantment(stack)) return;

        String key = generateKey(player, stack);
        HotBarrelData data = HOT_BARREL_CACHE.get(key);

        if (data == null || data.level <= 0.0f) return;

        data.ticksSinceLastShot++;

        if (data.ticksSinceLastShot >= DECAY_START_DELAY) {
            data.level = Math.max(data.level - DECAY_RATE_PER_TICK, 0.0f);
            if (data.level <= 0.0f) {
                HOT_BARREL_CACHE.remove(key);
                return;
            }
        }

        data.lastUpdateTime = System.currentTimeMillis();
    }

    public static void clearHotBarrel(Player player, ItemStack stack) {
        String key = generateKey(player, stack);
        HOT_BARREL_CACHE.remove(key);
    }

    public static boolean hasHotBarrelEnchantment(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOT_BARREL.get(), stack) > 0;
    }

    public static void cleanupOldEntries() {
        long currentTime = System.currentTimeMillis();
        long maxAge = 300000; // 5 minutes

        HOT_BARREL_CACHE.entrySet().removeIf(entry ->
                currentTime - entry.getValue().lastUpdateTime > maxAge
        );
    }
}