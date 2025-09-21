package top.ribs.scguns.faction;

import top.ribs.scguns.Config;

public class GunMobValues {
    public static boolean enabled = true;
    public static boolean elitesEnabled = true;
    public static double eliteChance = 0.3D;
    public static int minDays = 4;
    public static int initialChance = 1;
    public static int chanceIncrement = 1;
    public static int maxChance = 50;

    public static void init() {
        enabled = Config.COMMON.gunnerMobs.gunnerMobSpawning.get();
        elitesEnabled = Config.COMMON.gunnerMobs.eliteSpawning.get();
        eliteChance = Config.COMMON.gunnerMobs.eliteChance.get();
        maxChance = Config.COMMON.gunnerMobs.maxChance.get();
        minDays = Config.COMMON.gunnerMobs.minimunDays.get();
        initialChance = Config.COMMON.gunnerMobs.initialChance.get();
        chanceIncrement = Config.COMMON.gunnerMobs.chanceIncrement.get();
        maxChance = Config.COMMON.gunnerMobs.maxChance.get();
    }
}
