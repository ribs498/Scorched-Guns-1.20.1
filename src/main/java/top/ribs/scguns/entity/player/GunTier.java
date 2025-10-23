package top.ribs.scguns.entity.player;

import java.util.ArrayList;
import java.util.List;

public class GunTier {
    private final String id;
    private final int level;
    private final String tagName;
    private final int raidLevel;
    private final List<String> previousTierIds;

    public GunTier(String id, int level, String tagName, int raidLevel) {
        this.id = id;
        this.level = level;
        this.tagName = tagName;
        this.raidLevel = raidLevel;
        this.previousTierIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public String getTagName() {
        return tagName;
    }

    public int getRaidLevel() {
        return raidLevel;
    }

    public List<String> getPreviousTierIds() {
        return new ArrayList<>(previousTierIds);
    }

    public GunTier addPreviousTier(String tierId) {
        if (!this.previousTierIds.contains(tierId)) {
            this.previousTierIds.add(tierId);
        }
        return this;
    }

    public List<GunTier> getAvailableMobTiers() {
        List<GunTier> tiers = new ArrayList<>();
        for (String id : previousTierIds) {
            GunTier tier = GunTierRegistry.getTier(id);
            if (tier != null) {
                tiers.add(tier);
            }
        }
        return tiers;
    }

    @Override
    public String toString() {
        return "GunTier{id='" + id + "', level=" + level + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GunTier other)) return false;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}