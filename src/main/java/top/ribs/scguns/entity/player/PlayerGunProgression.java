package top.ribs.scguns.entity.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.init.ModTags;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PlayerGunProgression {

    private static final String NBT_KEY = "SCGunsProgression";
    private static final String TIER_KEY = "CurrentTier";
    private static final String RAID_LEVEL_KEY = "RaidLevel";

    private GunTier currentTier;
    private int currentRaidLevel;

    public PlayerGunProgression() {
        this.currentTier = GunTiers.NONE;
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
        if (newTier == null) return false;

        if (newTier.getLevel() > currentTier.getLevel()) {
            currentTier = newTier;
            currentRaidLevel = newTier.getRaidLevel();
            return true;
        }
        return false;
    }

    public void setTier(GunTier tier) {
        if (tier == null) {
            this.currentTier = GunTiers.NONE;
            this.currentRaidLevel = 0;
        } else {
            this.currentTier = tier;
            this.currentRaidLevel = tier.getRaidLevel();
        }
    }

    public void setRaidLevel(int level) {
        this.currentRaidLevel = Math.max(0, level);
    }

    public boolean checkAndUpdateFromItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        for (GunTier tier : GunTierRegistry.getAllTiers()) {
            if (tier.getTagName() == null) continue;

            if (ModTags.Items.isInTierTag(stack, tier)) {
                return updateTier(tier);
            }
        }

        return false;
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("TierId", currentTier.getId());
        tag.putInt(TIER_KEY, currentTier.getLevel());
        tag.putInt(RAID_LEVEL_KEY, currentRaidLevel);
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        if (tag.contains("TierId")) {
            String tierId = tag.getString("TierId");
            GunTier tier = GunTierRegistry.getTier(tierId);

            if (tier != null) {
                currentTier = tier;
            } else {
                currentTier = migrateLegacyTier(tag);
            }
        } else {
            currentTier = migrateLegacyTier(tag);
        }

        if (tag.contains(RAID_LEVEL_KEY)) {
            currentRaidLevel = tag.getInt(RAID_LEVEL_KEY);
        } else {
            currentRaidLevel = currentTier.getRaidLevel();
        }
    }

    @Nullable
    private GunTier migrateLegacyTier(CompoundTag tag) {
        if (tag.contains("TierName")) {
            String tierName = tag.getString("TierName").toLowerCase();
            GunTier tier = GunTierRegistry.getTier(tierName);
            if (tier != null) return tier;
        }

        if (tag.contains(TIER_KEY)) {
            int level = tag.getInt(TIER_KEY);
            GunTier tier = GunTierRegistry.getTierByLevel(level);
            if (tier != null) return tier;
        }

        return GunTiers.NONE;
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