package top.ribs.scguns.entity.player;

public class GunTiers {
    public static final GunTier NONE;
    public static final GunTier ANTIQUE;
    public static final GunTier FRONTIER;
    public static final GunTier COPPER;
    public static final GunTier IRON;
    public static final GunTier WRECKER;
    public static final GunTier OCEAN;
    public static final GunTier DIAMOND_STEEL;
    public static final GunTier TREATED_BRASS;
    public static final GunTier PIGLIN;
    public static final GunTier DEEP_DARK;
    public static final GunTier END;
    public static final GunTier SCORCHED;

    static {
        NONE = GunTierRegistry.register("none", 0, null, 0);

        ANTIQUE = GunTierRegistry.register("antique", 1, "antique_gun_tier", 1);

        FRONTIER = GunTierRegistry.register("frontier", 2, "frontier_gun_tier", 1)
                .addPreviousTier("antique");

        COPPER = GunTierRegistry.register("copper", 3, "copper_gun_tier", 2)
                .addPreviousTier("frontier")
                .addPreviousTier("antique");

        IRON = GunTierRegistry.register("iron", 4, "iron_gun_tier", 3)
                .addPreviousTier("copper")
                .addPreviousTier("frontier")
                .addPreviousTier("antique");

        WRECKER = GunTierRegistry.register("wrecker", 5, "wrecker_gun_tier", 3)
                .addPreviousTier("copper")
                .addPreviousTier("frontier")
                .addPreviousTier("antique");

        OCEAN = GunTierRegistry.register("ocean", 5, "ocean_gun_tier", 3)
                .addPreviousTier("copper")
                .addPreviousTier("frontier")
                .addPreviousTier("antique");

        DIAMOND_STEEL = GunTierRegistry.register("diamond_steel", 6, "diamond_steel_gun_tier", 4)
                .addPreviousTier("iron")
                .addPreviousTier("copper")
                .addPreviousTier("frontier")
                .addPreviousTier("antique");

        TREATED_BRASS = GunTierRegistry.register("treated_brass", 6, "treated_brass_gun_tier", 4)
                .addPreviousTier("iron")
                .addPreviousTier("copper")
                .addPreviousTier("frontier")
                .addPreviousTier("antique");

        PIGLIN = GunTierRegistry.register("piglin", 6, "piglin_gun_tier", 4)
                .addPreviousTier("iron")
                .addPreviousTier("copper")
                .addPreviousTier("frontier")
                .addPreviousTier("antique");

        DEEP_DARK = GunTierRegistry.register("deep_dark", 6, "deep_dark_gun_tier", 4)
                .addPreviousTier("iron")
                .addPreviousTier("copper")
                .addPreviousTier("frontier")
                .addPreviousTier("antique");

        END = GunTierRegistry.register("end", 7, "end_gun_tier", 5)
                .addPreviousTier("diamond_steel")
                .addPreviousTier("treated_brass")
                .addPreviousTier("iron")
                .addPreviousTier("copper")
                .addPreviousTier("frontier")
                .addPreviousTier("antique");

        SCORCHED = GunTierRegistry.register("scorched", 7, "scorched_gun_tier", 5)
                .addPreviousTier("diamond_steel")
                .addPreviousTier("treated_brass")
                .addPreviousTier("iron")
                .addPreviousTier("copper")
                .addPreviousTier("frontier")
                .addPreviousTier("antique");
    }

    public static void init() {
    }
}