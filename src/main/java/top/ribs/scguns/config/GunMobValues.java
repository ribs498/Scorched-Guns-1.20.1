package top.ribs.scguns.config;

import net.minecraft.world.Difficulty;
import top.ribs.scguns.Config;

public class GunMobValues {
    public static boolean enabled = true;
    public static double gunnerSpawnChance = 0.25D;
    public static boolean scaleToDifficulty = true;
    public static boolean elitesEnabled = true;
    public static double eliteChance = 0.15D;

    public static void init() {
        enabled = Config.COMMON.gunnerMobs.gunnerMobSpawning.get();
        gunnerSpawnChance = Config.COMMON.gunnerMobs.gunnerSpawnChance.get();
        scaleToDifficulty = Config.COMMON.gunnerMobs.scaleToDifficulty.get();
        elitesEnabled = Config.COMMON.gunnerMobs.eliteSpawning.get();
        eliteChance = Config.COMMON.gunnerMobs.eliteChance.get();
    }

    public static double getGunnerSpawnChance(Difficulty difficulty) {
        if (!scaleToDifficulty) {
            return gunnerSpawnChance;
        }

        return gunnerSpawnChance * getDifficultyMultiplier(difficulty);
    }

    public static double getEliteChance(Difficulty difficulty) {
        if (!scaleToDifficulty) {
            return eliteChance;
        }

        return eliteChance * getDifficultyMultiplier(difficulty);
    }

    private static double getDifficultyMultiplier(Difficulty difficulty) {
        return switch(difficulty) {
            case PEACEFUL -> 0.5;
            case EASY -> 0.75;
            case NORMAL -> 1.0;
            case HARD -> 1.5;
        };
    }
}