package top.ribs.scguns.client.handler;

import java.util.HashMap;
import java.util.Map;

public class DualWieldShotTracker {
    private static DualWieldShotTracker instance;
    private final Map<Integer, Integer> entityShotCounts = new HashMap<>();

    public static DualWieldShotTracker get() {
        if (instance == null) {
            instance = new DualWieldShotTracker();
        }
        return instance;
    }

    public void incrementShotCount(int entityId) {
        int currentCount = entityShotCounts.getOrDefault(entityId, 0);
        entityShotCounts.put(entityId, currentCount + 1);
    }

    public int getShotCount(int entityId) {
        return entityShotCounts.getOrDefault(entityId, 0);
    }

    public boolean shouldUseAlternateAnimation(int entityId) {
        return getShotCount(entityId) % 2 == 1;
    }

    public void clearShotCount(int entityId) {
        entityShotCounts.remove(entityId);
    }

    public void clearAll() {
        entityShotCounts.clear();
    }
}