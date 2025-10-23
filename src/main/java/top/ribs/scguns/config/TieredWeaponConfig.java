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
import top.ribs.scguns.entity.player.GunTier;
import top.ribs.scguns.entity.player.GunTierRegistry;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid = "scguns")
public class TieredWeaponConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, List<Item>> TIER_WEAPONS = new HashMap<>();
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
                            String tierIdOrName = entry.getKey();
                            JsonArray weaponsArray = entry.getValue().getAsJsonArray();

                            GunTier tier = GunTierRegistry.getTier(tierIdOrName.toLowerCase());
                            if (tier == null) {
                                LOGGER.warn("Unknown tier in tier_weapons.json: {}", tierIdOrName);
                                continue;
                            }

                            List<Item> weapons = new ArrayList<>();
                            for (JsonElement weaponElement : weaponsArray) {
                                String weaponId = weaponElement.getAsString();
                                Item weapon = ForgeRegistries.ITEMS.getValue(new ResourceLocation(weaponId));
                                if (weapon != null) {
                                    weapons.add(weapon);
                                } else {
                                    LOGGER.warn("Unknown weapon item for tier {}: {}", tierIdOrName, weaponId);
                                }
                            }

                            TIER_WEAPONS.put(tier.getId(), weapons);
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

    @Nullable
    public static Item getRandomWeaponForTier(GunTier tier, RandomSource random) {
        if (tier == null) return null;

        List<Item> weapons = TIER_WEAPONS.get(tier.getId());
        if (weapons == null || weapons.isEmpty()) {
            return null;
        }
        return weapons.get(random.nextInt(weapons.size()));
    }

    public static List<Item> getWeaponsForTier(GunTier tier) {
        if (tier == null) return Collections.emptyList();
        return TIER_WEAPONS.getOrDefault(tier.getId(), Collections.emptyList());
    }

    public static boolean hasTierWeapons(GunTier tier) {
        if (tier == null) return false;
        List<Item> weapons = TIER_WEAPONS.get(tier.getId());
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