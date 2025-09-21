package top.ribs.scguns.faction.scguns;

import java.util.Arrays;

public class FactionDataManager {
    private static final FactionData TERROR_ARMADA_WAVE_1 = new FactionData(
            "terror_armada_wave_1",
            6,
            Arrays.asList("minecraft:zombie", "minecraft:zombie_villager", "minecraft:husk"),
            Arrays.asList("scguns:greaser_smg", "scguns:combat_shotgun"),
            Arrays.asList("scguns:semi_auto_rifle"),
            Arrays.asList("scguns:drill")
    );

    private static final FactionData TERROR_ARMADA_WAVE_2 = new FactionData(
            "terror_armada_wave_2",
            8,
            Arrays.asList("minecraft:zombie", "minecraft:skeleton", "minecraft:stray"),
            Arrays.asList("scguns:greaser_smg", "scguns:combat_shotgun"),
            Arrays.asList("scguns:semi_auto_rifle"),
            Arrays.asList("scguns:drill")
    );

    private static final FactionData TERROR_ARMADA_WAVE_3 = new FactionData(
            "terror_armada_wave_3",
            8,
            Arrays.asList("minecraft:zombie", "minecraft:zombie_villager", "minecraft:stray", "minecraft:skeleton"),
            Arrays.asList("scguns:greaser_smg", "scguns:combat_shotgun"),
            Arrays.asList("scguns:semi_auto_rifle"),
            Arrays.asList("scguns:drill"));

    public static FactionData getTerrorArmadaWave1() {
        return TERROR_ARMADA_WAVE_1;
    }

    public static FactionData getTerrorArmadaWave2() {
        return TERROR_ARMADA_WAVE_2;
    }

    public static FactionData getTerrorArmadaWave3() {
        return TERROR_ARMADA_WAVE_3;
    }
}