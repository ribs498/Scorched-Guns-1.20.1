package top.ribs.scguns.entity.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.init.ModTags;

import java.util.ArrayList;
import java.util.List;

public class PlayerGunProgression {

    private static final String NBT_KEY = "SCGunsProgression";
    private static final String TIER_KEY = "CurrentTier";
    private static final String RAID_LEVEL_KEY = "RaidLevel";

    public enum GunTier {
        NONE(0, null, 0),
        ANTIQUE(1, "antique_gun_tier", 1),
        FRONTIER(2, "frontier_gun_tier", 1),
        COPPER(3, "copper_gun_tier", 2),
        IRON(4, "iron_gun_tier", 3),
        WRECKER(5, "wrecker_gun_tier", 3),
        OCEAN(5, "ocean_gun_tier", 3),
        DIAMOND_STEEL(6, "diamond_steel_gun_tier", 4),
        TREATED_BRASS(6, "treated_brass_gun_tier", 4),
        PIGLIN(6, "piglin_gun_tier", 4),
        DEEP_DARK(6, "deep_dark_gun_tier", 4),
        END(7, "end_gun_tier", 5),
        SCORCHED(7, "scorched_gun_tier", 5);

        private final int level;
        private final String tagName;
        private final int raidLevel;

        GunTier(int level, String tagName, int raidLevel) {
            this.level = level;
            this.tagName = tagName;
            this.raidLevel = raidLevel;
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

        public static GunTier fromLevel(int level) {
            for (GunTier tier : values()) {
                if (tier.level == level) {
                    return tier;
                }
            }
            return NONE;
        }

        public List<GunTier> getAvailableMobTiers() {
            List<GunTier> tiers = new ArrayList<>();

            switch (this) {
                case NONE, ANTIQUE:
                    break;
                case FRONTIER:
                    tiers.add(ANTIQUE);
                    break;
                case COPPER:
                    tiers.add(FRONTIER);
                    tiers.add(ANTIQUE);
                    break;
                case IRON:
                case WRECKER:
                case OCEAN:
                    tiers.add(COPPER);
                    tiers.add(FRONTIER);
                    tiers.add(ANTIQUE);
                    break;
                case DIAMOND_STEEL:
                case TREATED_BRASS:
                case PIGLIN:
                case DEEP_DARK:
                    tiers.add(IRON);
                    tiers.add(COPPER);
                    tiers.add(FRONTIER);
                    tiers.add(ANTIQUE);
                    break;
                case END:
                case SCORCHED:
                    tiers.add(DIAMOND_STEEL);
                    tiers.add(TREATED_BRASS);
                    tiers.add(IRON);
                    tiers.add(COPPER);
                    tiers.add(FRONTIER);
                    tiers.add(ANTIQUE);
                    break;
            }

            return tiers;
        }
    }

    private GunTier currentTier;
    private int currentRaidLevel;

    public PlayerGunProgression() {
        this.currentTier = GunTier.NONE;
        this.currentRaidLevel = 0;
    }

    public GunTier getCurrentTier() {
        return currentTier;
    }

    public int getCurrentRaidLevel() {
        return currentRaidLevel;
    }

    public List<GunTier> getAvailableMobTiers() {
        return currentTier.getAvailableMobTiers();
    }

    public boolean updateTier(GunTier newTier) {
        if (newTier.getLevel() > currentTier.getLevel()) {
            currentTier = newTier;
            currentRaidLevel = newTier.getRaidLevel();
            return true;
        }
        return false;
    }

    public void setTier(GunTier tier) {
        this.currentTier = tier;
        this.currentRaidLevel = tier.getRaidLevel();
    }

    public void setRaidLevel(int level) {
        this.currentRaidLevel = Math.max(0, level);
    }

    public boolean checkAndUpdateFromItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        for (GunTier tier : GunTier.values()) {
            if (tier == GunTier.NONE) continue;

            if (ModTags.Items.isInTierTag(stack, tier)) {
                return updateTier(tier);
            }
        }

        return false;
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TIER_KEY, currentTier.getLevel());
        tag.putString("TierName", currentTier.name());
        tag.putInt(RAID_LEVEL_KEY, currentRaidLevel);
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        if (tag.contains("TierName")) {
            try {
                currentTier = GunTier.valueOf(tag.getString("TierName"));
                currentRaidLevel = currentTier.getRaidLevel();
            } catch (IllegalArgumentException e) {
                currentTier = GunTier.NONE;
                currentRaidLevel = 0;
            }
        } else if (tag.contains(TIER_KEY)) {
            int level = tag.getInt(TIER_KEY);
            currentTier = GunTier.fromLevel(level);
            currentRaidLevel = currentTier.getRaidLevel();
        } else {
            currentTier = GunTier.NONE;
            currentRaidLevel = 0;
        }

        if (tag.contains(RAID_LEVEL_KEY)) {
            currentRaidLevel = tag.getInt(RAID_LEVEL_KEY);
        }
    }

    public static PlayerGunProgression get(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        PlayerGunProgression progression = new PlayerGunProgression();

        if (persistentData.contains(NBT_KEY)) {
            progression.loadNBT(persistentData.getCompound(NBT_KEY));
        }

        return progression;
    }

    public static void save(Player player, PlayerGunProgression progression) {
        CompoundTag persistentData = player.getPersistentData();
        persistentData.put(NBT_KEY, progression.saveNBT());
    }

    public static boolean updateAndSave(Player player, ItemStack stack) {
        PlayerGunProgression progression = get(player);
        boolean updated = progression.checkAndUpdateFromItem(stack);

        if (updated) {
            save(player, progression);
        }

        return updated;
    }
}