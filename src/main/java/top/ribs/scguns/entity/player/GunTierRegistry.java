package top.ribs.scguns.entity.player;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class GunTierRegistry {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, GunTier> TIERS_BY_ID = new LinkedHashMap<>();
    private static final Map<Integer, List<GunTier>> TIERS_BY_LEVEL = new HashMap<>();
    private static boolean isLocked = false;

    public static GunTier register(String id, int level, @Nullable String tagName, int raidLevel) {
        if (isLocked) {
            throw new IllegalStateException("Cannot register tiers after initialization is complete!");
        }

        if (TIERS_BY_ID.containsKey(id)) {
            //LOGGER.warn("Tier with ID '{}' is already registered! Skipping duplicate registration.", id);
            return TIERS_BY_ID.get(id);
        }

        GunTier tier = new GunTier(id, level, tagName, raidLevel);
        TIERS_BY_ID.put(id, tier);
        TIERS_BY_LEVEL.computeIfAbsent(level, k -> new ArrayList<>()).add(tier);

        //LOGGER.debug("Registered gun tier: {} (level: {}, raid level: {})", id, level, raidLevel);
        return tier;
    }

    public static void lock() {
        isLocked = true;
        //LOGGER.info("Gun tier registry locked. Registered {} tiers.", TIERS_BY_ID.size());
    }

    @Nullable
    public static GunTier getTier(String id) {
        return TIERS_BY_ID.get(id);
    }

    @Nullable
    public static GunTier getTierByLevel(int level) {
        List<GunTier> tiersAtLevel = TIERS_BY_LEVEL.get(level);
        if (tiersAtLevel == null || tiersAtLevel.isEmpty()) {
            return null;
        }
        return tiersAtLevel.get(0);
    }

    public static List<GunTier> getTiersAtLevel(int level) {
        return TIERS_BY_LEVEL.getOrDefault(level, Collections.emptyList());
    }

    public static Collection<GunTier> getAllTiers() {
        return Collections.unmodifiableCollection(TIERS_BY_ID.values());
    }

    public static int getMaxLevel() {
        return TIERS_BY_LEVEL.keySet().stream()
                .max(Integer::compareTo)
                .orElse(0);
    }

    public static int getMaxRaidLevel() {
        return TIERS_BY_ID.values().stream()
                .mapToInt(GunTier::getRaidLevel)
                .max()
                .orElse(0);
    }

    @Nullable
    public static GunTier getHighestTier() {
        int maxLevel = getMaxLevel();
        if (maxLevel == 0) return null;

        List<GunTier> tiersAtMax = TIERS_BY_LEVEL.get(maxLevel);
        if (tiersAtMax == null || tiersAtMax.isEmpty()) return null;

        return tiersAtMax.stream()
                .max(Comparator.comparingInt(GunTier::getRaidLevel))
                .orElse(tiersAtMax.get(0));
    }

    public static List<GunTier> getTiersUpToLevel(int maxLevel) {
        List<GunTier> result = new ArrayList<>();
        for (int level = 0; level <= maxLevel; level++) {
            result.addAll(TIERS_BY_LEVEL.getOrDefault(level, Collections.emptyList()));
        }
        return result;
    }

    public static boolean isRegistered(String id) {
        return TIERS_BY_ID.containsKey(id);
    }

    static void clear() {
        if (isLocked) {
            LOGGER.warn("Attempting to clear locked tier registry. This should only happen during testing!");
        }
        TIERS_BY_ID.clear();
        TIERS_BY_LEVEL.clear();
        isLocked = false;
    }
}