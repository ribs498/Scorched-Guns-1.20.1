package top.ribs.scguns.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ribs.scguns.entity.player.PlayerGunProgression;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid = "scguns")
public class TieredWeaponConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<PlayerGunProgression.GunTier, List<Item>> TIER_WEAPONS = new EnumMap<>(PlayerGunProgression.GunTier.class);
    private static final ResourceLocation CONFIG_LOCATION = new ResourceLocation("scguns", "entity/tier_weapons.json");

    public static void loadConfig(ResourceManager resourceManager) {
        TIER_WEAPONS.clear();
        try {
            Resource resource = resourceManager.getResource(CONFIG_LOCATION).orElse(null);
            if (resource != null) {
                try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(reader, JsonObject.class);

                    if (json != null) {
                        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                            String tierName = entry.getKey();
                            JsonArray weaponsArray = entry.getValue().getAsJsonArray();

                            try {
                                PlayerGunProgression.GunTier tier = PlayerGunProgression.GunTier.valueOf(tierName);
                                List<Item> weapons = new ArrayList<>();

                                for (JsonElement weaponElement : weaponsArray) {
                                    String weaponId = weaponElement.getAsString();
                                    Item weapon = ForgeRegistries.ITEMS.getValue(new ResourceLocation(weaponId));
                                    if (weapon != null) {
                                        weapons.add(weapon);
                                    } else {
                                        LOGGER.warn("Unknown weapon item for tier {}: {}", tierName, weaponId);
                                    }
                                }

                                TIER_WEAPONS.put(tier, weapons);
                            } catch (IllegalArgumentException e) {
                                LOGGER.error("Invalid tier name: {}", tierName);
                            }
                        }
                    }

                    LOGGER.info("Loaded tiered weapon config: {} tiers configured", TIER_WEAPONS.size());
                }
            } else {
                LOGGER.warn("Tiered weapon config not found at {}", CONFIG_LOCATION);
                loadDefaults();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load tiered weapon config at {}", CONFIG_LOCATION, e);
            loadDefaults();
        }
    }

    private static void loadDefaults() {
        LOGGER.info("Loading default tiered weapon configuration");
    }

    public static Item getRandomWeaponForTier(PlayerGunProgression.GunTier tier, RandomSource random) {
        List<Item> weapons = TIER_WEAPONS.get(tier);
        if (weapons == null || weapons.isEmpty()) {
            return null;
        }
        return weapons.get(random.nextInt(weapons.size()));
    }

    public static List<Item> getWeaponsForTier(PlayerGunProgression.GunTier tier) {
        return TIER_WEAPONS.getOrDefault(tier, Collections.emptyList());
    }

    public static boolean hasTierWeapons(PlayerGunProgression.GunTier tier) {
        List<Item> weapons = TIER_WEAPONS.get(tier);
        return weapons != null && !weapons.isEmpty();
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                loadConfig(resourceManager);
            }
        });
    }
}