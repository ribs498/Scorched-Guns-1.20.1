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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid = "scguns")
public class GunnerMobConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, MobGunnerData> GUNNER_MOBS = new HashMap<>();
    private static final ResourceLocation CONFIG_LOCATION = new ResourceLocation("scguns", "entity/gunner_mobs.json");

    public record MobGunnerData(float spawnChance, List<Item> allowedWeapons, List<ArmorPiece> allowedArmor,
                                int aiDifficulty, float weaponDropChance) {

        public Item getRandomWeapon(RandomSource random) {
            if (allowedWeapons.isEmpty()) return null;
            return allowedWeapons.get(random.nextInt(allowedWeapons.size()));
        }
    }

    public record ArmorPiece(Item item, String slot, float spawnChance) {
    }

    public static void loadConfig(ResourceManager resourceManager) {
        GUNNER_MOBS.clear();
        try {
            Resource resource = resourceManager.getResource(CONFIG_LOCATION).orElse(null);
            if (resource != null) {
                try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(reader, JsonObject.class);

                    if (json != null && json.has("mobs")) {
                        JsonObject mobsObj = json.getAsJsonObject("mobs");
                        for (Map.Entry<String, JsonElement> entry : mobsObj.entrySet()) {
                            String entityId = entry.getKey();
                            JsonObject mobData = entry.getValue().getAsJsonObject();

                            MobGunnerData gunnerData = parseMobData(mobData);
                            if (gunnerData != null) {
                                ResourceLocation entityKey = new ResourceLocation(entityId);
                                GUNNER_MOBS.put(entityKey, gunnerData);
                            }
                        }
                    }

                    LOGGER.info("Loaded gunner mob config: {} mob types configured", GUNNER_MOBS.size());
                }
            } else {
                LOGGER.warn("Gunner mob config not found at {}", CONFIG_LOCATION);
                loadDefaults();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load gunner mob config at {}", CONFIG_LOCATION, e);
            loadDefaults();
        }
    }

    private static MobGunnerData parseMobData(JsonObject mobData) {
        try {
            float spawnChance = mobData.has("spawn_chance") ?
                    mobData.get("spawn_chance").getAsFloat() : 0.3f;

            int aiDifficulty = mobData.has("ai_difficulty") ?
                    mobData.get("ai_difficulty").getAsInt() : 2;

            float weaponDropChance = mobData.has("weapon_drop_chance") ?
                    mobData.get("weapon_drop_chance").getAsFloat() : 0.085f;

            List<Item> weapons = new ArrayList<>();
            if (mobData.has("weapons")) {
                JsonArray weaponsArray = mobData.getAsJsonArray("weapons");
                for (JsonElement weaponElement : weaponsArray) {
                    String weaponId = weaponElement.getAsString();
                    Item weapon = ForgeRegistries.ITEMS.getValue(new ResourceLocation(weaponId));
                    if (weapon != null) {
                        weapons.add(weapon);
                    } else {
                        LOGGER.warn("Unknown weapon item: {}", weaponId);
                    }
                }
            }

            List<ArmorPiece> armor = new ArrayList<>();
            if (mobData.has("armor")) {
                JsonArray armorArray = mobData.getAsJsonArray("armor");
                for (JsonElement armorElement : armorArray) {
                    JsonObject armorObj = armorElement.getAsJsonObject();
                    String itemId = armorObj.get("item").getAsString();
                    String slot = armorObj.get("slot").getAsString();
                    float chance = armorObj.has("chance") ?
                            armorObj.get("chance").getAsFloat() : 1.0f;

                    Item armorItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                    if (armorItem != null) {
                        armor.add(new ArmorPiece(armorItem, slot, chance));
                    } else {
                        LOGGER.warn("Unknown armor item: {}", itemId);
                    }
                }
            }

            return new MobGunnerData(spawnChance, weapons, armor, aiDifficulty, weaponDropChance);

        } catch (Exception e) {
            LOGGER.error("Error parsing mob gunner data", e);
            return null;
        }
    }

    private static void loadDefaults() {
        LOGGER.info("Loading default gunner mob configuration");
    }

    public static MobGunnerData getGunnerData(EntityType<?> entityType) {
        return GUNNER_MOBS.get(ForgeRegistries.ENTITY_TYPES.getKey(entityType));
    }

    public static boolean canSpawnAsGunner(EntityType<?> entityType) {
        return GUNNER_MOBS.containsKey(ForgeRegistries.ENTITY_TYPES.getKey(entityType));
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