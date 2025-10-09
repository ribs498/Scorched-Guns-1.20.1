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

    public enum GunTier {
        NONE(0, null),
        ANTIQUE(1, "antique_gun_tier"),
        FRONTIER(2, "frontier_gun_tier"),
        COPPER(3, "copper_gun_tier"),
        IRON(4, "iron_gun_tier"),
        WRECKER(5, "wrecker_gun_tier"),
        OCEAN(5, "ocean_gun_tier"),
        DIAMOND_STEEL(6, "diamond_steel_gun_tier"),
        TREATED_BRASS(6, "treated_brass_gun_tier"),
        PIGLIN(6, "piglin_gun_tier"),
        DEEP_DARK(6, "deep_dark_gun_tier"),
        END(7, "end_gun_tier"),
        SCORCHED(7, "scorched_gun_tier");

        private final int level;
        private final String tagName;

        GunTier(int level, String tagName) {
            this.level = level;
            this.tagName = tagName;
        }

        public int getLevel() {
            return level;
        }

        public String getTagName() {
            return tagName;
        }

        public static GunTier fromLevel(int level) {
            for (GunTier tier : values()) {
                if (tier.level == level) {
                    return tier;
                }
            }
            return NONE;
        }

        /**
         * Gets all tiers available for mob spawning based on player progression.
         * Logic:
         * - FRONTIER unlocks ANTIQUE
         * - COPPER unlocks FRONTIER + ANTIQUE
         * - WRECKER/IRON/OCEAN unlocks COPPER
         * - DIAMOND_STEEL/TREATED_BRASS/PIGLIN/DEEP_DARK unlocks IRON
         * - END/SCORCHED unlocks DIAMOND_STEEL + TREATED_BRASS (no Piglin/Deep Dark)
         */
        public List<GunTier> getAvailableMobTiers() {
            List<GunTier> tiers = new ArrayList<>();

            switch (this) {
                case NONE:
                    break;
                case ANTIQUE:
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

        /**
         * Gets the highest tier that basic mobs can spawn with.
         */
        public GunTier getMaxMobTier() {
            return switch (this) {
                case NONE, ANTIQUE -> NONE;
                case FRONTIER -> ANTIQUE;
                case COPPER -> FRONTIER;
                case IRON, WRECKER, OCEAN -> COPPER;
                case DIAMOND_STEEL, TREATED_BRASS, PIGLIN, DEEP_DARK -> IRON;
                case END, SCORCHED -> TREATED_BRASS;
            };
        }

        public GunTier getMobSpawnTier() {
            List<GunTier> availableTiers = getAvailableMobTiers();
            if (availableTiers.isEmpty()) {
                return NONE;
            }
            return availableTiers.get(availableTiers.size() - 1);
        }
    }

    private GunTier currentTier;

    public PlayerGunProgression() {
        this.currentTier = GunTier.NONE;
    }

    public GunTier getCurrentTier() {
        return currentTier;
    }

    public GunTier getMaxMobTier() {
        return currentTier.getMaxMobTier();
    }

    public List<GunTier> getAvailableMobTiers() {
        return currentTier.getAvailableMobTiers();
    }

    public boolean canMobSpawnWithTier(GunTier tier) {
        return getAvailableMobTiers().contains(tier);
    }

    public boolean updateTier(GunTier newTier) {
        if (newTier.getLevel() > currentTier.getLevel()) {
            currentTier = newTier;
            return true;
        }
        return false;
    }

    public void setTier(GunTier tier) {
        this.currentTier = tier;
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
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        if (tag.contains("TierName")) {
            try {
                currentTier = GunTier.valueOf(tag.getString("TierName"));
            } catch (IllegalArgumentException e) {
                currentTier = GunTier.NONE;
            }
        } else if (tag.contains(TIER_KEY)) {
            int level = tag.getInt(TIER_KEY);
            currentTier = GunTier.fromLevel(level);
        } else {
            currentTier = GunTier.NONE;
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