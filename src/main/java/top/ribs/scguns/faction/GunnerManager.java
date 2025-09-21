package top.ribs.scguns.faction;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.Config;
import top.ribs.scguns.ScorchedGuns;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GunnerManager {
    private static GunnerManager instance;
    private final Map<String, Faction> factions = new HashMap<>();
    static List<String> gunnerMobsList = null;

    public static List<String> getConfigFactions() {
        if (gunnerMobsList == null) {
            gunnerMobsList = new ArrayList<>(Config.COMMON.gunnerMobs.factions.get());
            ScorchedGuns.LOGGER.atInfo().log("Registered the following Factions: {}", gunnerMobsList);
        }
        return gunnerMobsList;
    }

    public static GunnerManager getInstance() {
        if (instance == null) {
            instance = new GunnerManager(getConfigFactions());
        }
        return instance;
    }

    public GunnerManager(List<? extends String> factionConfig) {
        for (String entry : factionConfig) {
            String[] parts = entry.split("\\|");
            if (parts.length == 6) {
                String name = parts[0];
                int aiLevel = Integer.parseInt(parts[1]);
                List<String> mobs = Arrays.asList(parts[2].split(","));
                List<Item> closeGuns = Arrays.stream(parts[3].split(","))
                        .map(gunName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(gunName)))
                        .filter(Objects::nonNull)
                        .toList();
                List<Item> longGuns = Arrays.stream(parts[4].split(","))
                        .map(gunName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(gunName)))
                        .filter(Objects::nonNull)
                        .toList();
                List<Item> eliteGuns = Arrays.stream(parts[5].split(","))
                        .map(gunName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(gunName)))
                        .filter(Objects::nonNull)
                        .toList();

                factions.put(name, new Faction(name, aiLevel, mobs, closeGuns, longGuns, eliteGuns));
            }
        }
    }

    public Faction getFactionForMob(ResourceLocation entityType) {
        return factions.values().stream()
                .filter(f -> f.getMobs().contains(entityType.toString()))
                .findFirst().orElse(null);
    }

    public Faction getFactionByName(String factionName) {
        return getFactions().stream()
                .filter(f -> f.getName().equalsIgnoreCase(factionName))
                .findFirst()
                .orElse(null);
    }

    public String getRandomFactionName() {
        if (factions.isEmpty()) {
            return null;
        }

        List<String> factionNames = new ArrayList<>(factions.keySet());
        int randomIndex = ThreadLocalRandom.current().nextInt(factionNames.size());
        return factionNames.get(randomIndex);
    }

    public Collection<Faction> getFactions() {
        return factions.values();
    }
}