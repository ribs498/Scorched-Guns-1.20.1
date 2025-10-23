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
public class EliteTierConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, EliteData> ELITE_TIERS = new HashMap<>();
    private static final ResourceLocation CONFIG_LOCATION = new ResourceLocation("scguns", "entity/elite_tiers.json");

    public record ArmorPiece(Item item, String slot, float chance) {
        public static ArmorPiece fromJson(JsonObject json) {
            String itemId = json.get("item").getAsString();
            String slot = json.get("slot").getAsString();
            float chance = json.has("chance") ? json.get("chance").getAsFloat() : 1.0f;

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item == null) {
                LOGGER.warn("Unknown armor item: {}", itemId);
                return null;
            }

            return new ArmorPiece(item, slot, chance);
        }
    }

    public record EliteData(List<Item> eliteWeapons, List<ArmorPiece> armor) {
        @Nullable
        public Item getRandomWeapon(RandomSource random) {
            if (eliteWeapons.isEmpty()) return null;
            return eliteWeapons.get(random.nextInt(eliteWeapons.size()));
        }

        public boolean hasEliteWeapons() {
            return !eliteWeapons.isEmpty();
        }
    }

    public static void loadConfig(ResourceManager resourceManager) {
        ELITE_TIERS.clear();
        try {
            Resource resource = resourceManager.getResource(CONFIG_LOCATION).orElse(null);
            if (resource != null) {
                try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(reader, JsonObject.class);

                    if (json != null) {
                        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                            String tierIdOrName = entry.getKey();
                            JsonObject tierData = entry.getValue().getAsJsonObject();

                            GunTier tier = GunTierRegistry.getTier(tierIdOrName.toLowerCase());
                            if (tier == null) {
                                LOGGER.warn("Unknown tier in elite config: {}", tierIdOrName);
                                continue;
                            }

                            List<Item> weapons = new ArrayList<>();
                            if (tierData.has("weapons")) {
                                JsonArray weaponsArray = tierData.getAsJsonArray("weapons");
                                for (JsonElement weaponElement : weaponsArray) {
                                    String weaponId = weaponElement.getAsString();
                                    Item weapon = ForgeRegistries.ITEMS.getValue(new ResourceLocation(weaponId));
                                    if (weapon != null) {
                                        weapons.add(weapon);
                                    } else {
                                        LOGGER.warn("Unknown elite weapon for tier {}: {}", tierIdOrName, weaponId);
                                    }
                                }
                            }

                            List<ArmorPiece> armor = new ArrayList<>();
                            if (tierData.has("armor")) {
                                JsonArray armorArray = tierData.getAsJsonArray("armor");
                                for (JsonElement armorElement : armorArray) {
                                    ArmorPiece piece = ArmorPiece.fromJson(armorElement.getAsJsonObject());
                                    if (piece != null) {
                                        armor.add(piece);
                                    }
                                }
                            }

                            ELITE_TIERS.put(tier.getId(), new EliteData(weapons, armor));
                        }
                    }

                    LOGGER.info("Loaded elite tier config: {} elite tiers configured", ELITE_TIERS.size());
                }
            } else {
                LOGGER.warn("Elite tier config not found at {}", CONFIG_LOCATION);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load elite tier config at {}", CONFIG_LOCATION, e);
        }
    }

    @Nullable
    public static EliteData getEliteData(GunTier tier) {
        if (tier == null) return null;
        return ELITE_TIERS.get(tier.getId());
    }

    public static boolean hasEliteData(GunTier tier) {
        if (tier == null) return false;
        EliteData data = ELITE_TIERS.get(tier.getId());
        return data != null && data.hasEliteWeapons();
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