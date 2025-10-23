package top.ribs.scguns.config;

import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.entity.ai.AIType;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid = "scguns")
public class RaidConfig {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, RaidData> RAIDS_BY_ID = new HashMap<>();
    private static final Map<Integer, List<RaidData>> RAIDS_BY_LEVEL = new HashMap<>();

    public record HealthConfig(@Nullable Float fixedHealth, @Nullable Float healthMultiplier) {
        public boolean useMultiplier() {
            return healthMultiplier != null;
        }
    }

    public record WeaponEntry(Item item, float dropChance, @Nullable CompoundTag nbt) {}
    public record ArmorEntry(Item item, String slot, float dropChance, @Nullable CompoundTag nbt) {}
    public record EffectEntry(MobEffect effect, int amplifier, int duration, boolean ambient, boolean visible) {}

    public record BossData(
            EntityType<?> entityType,
            @Nullable String customName,
            HealthConfig healthConfig,
            @Nullable WeaponEntry weapon,
            List<ArmorEntry> armor,
            List<EffectEntry> effects,
            int aiDifficulty,
            AIType aiType,
            @Nullable ResourceLocation specialLootTable,
            @Nullable MountData mount
    ) {}

    public record MountData(
            EntityType<?> entityType,
            HealthConfig healthConfig,
            List<ArmorEntry> armor,
            List<EffectEntry> effects,
            boolean mountDropsLoot
    ) {}

    public record HenchmanType(
            EntityType<?> entityType,
            float weight,
            HealthConfig healthConfig,
            List<Item> weapons,
            List<ArmorEntry> armor,
            List<EffectEntry> effects,
            int aiDifficulty,
            AIType aiType
    ) {}

    public record HenchmenData(
            List<HenchmanType> types,
            int maxAlive,
            int maxTotal,
            int spawnIntervalTicks,
            int spawnRadius,
            int spawnAttemptsPerWave
    ) {
        @Nullable
        public HenchmanType selectRandomType(net.minecraft.util.RandomSource random) {
            if (types.isEmpty()) return null;

            float totalWeight = 0;
            for (HenchmanType type : types) {
                totalWeight += type.weight;
            }

            float roll = random.nextFloat() * totalWeight;
            float currentWeight = 0;

            for (HenchmanType type : types) {
                currentWeight += type.weight;
                if (roll < currentWeight) {
                    return type;
                }
            }

            return types.get(types.size() - 1);
        }
    }

    public record SpawnConditions(
            int minPlayersNearby,
            int searchRadius,
            List<ResourceLocation> validDimensions,
            String announcementMessage
    ) {}

    public record RaidData(
            String raidId,
            @Nullable Integer raidLevel,
            BossData boss,
            HenchmenData henchmen,
            SpawnConditions spawnConditions
    ) {}

    public static void loadRaidConfigs(ResourceManager resourceManager) {
        RAIDS_BY_ID.clear();
        RAIDS_BY_LEVEL.clear();

        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources("raids",
                loc -> loc.getPath().endsWith(".json")).entrySet()) {

            ResourceLocation location = entry.getKey();
            String path = location.getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String raidId = fileName.replace("_raid.json", "").replace(".json", "");

            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(reader, JsonObject.class);

                RaidData raidData = parseRaidData(json);
                if (raidData != null) {
                    RAIDS_BY_ID.put(raidData.raidId(), raidData);

                    if (raidData.raidLevel() != null) {
                        RAIDS_BY_LEVEL.computeIfAbsent(raidData.raidLevel(), k -> new ArrayList<>())
                                .add(raidData);
                        LOGGER.info("Loaded progression raid: {} (Level: {})", raidData.raidId(), raidData.raidLevel());
                    } else {
                        LOGGER.info("Loaded custom raid: {}", raidData.raidId());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load raid config: {}", raidId, e);
            }
        }

        LOGGER.info("Loaded {} total raids ({} progression, {} custom)",
                RAIDS_BY_ID.size(), RAIDS_BY_LEVEL.values().stream().mapToInt(List::size).sum(),
                RAIDS_BY_ID.size() - RAIDS_BY_LEVEL.values().stream().mapToInt(List::size).sum());
    }

    @Nullable
    public static RaidData getRaidById(String raidId) {
        return RAIDS_BY_ID.get(raidId);
    }

    @Nullable
    public static RaidData getRaidByRaidId(String raidId) {
        return getRaidById(raidId);
    }

    public static List<RaidData> getRaidsAtLevel(int level) {
        return RAIDS_BY_LEVEL.getOrDefault(level, Collections.emptyList());
    }

    public static List<RaidData> getRaidsForLevel(int playerLevel) {
        List<RaidData> availableRaids = new ArrayList<>();

        for (int level = 1; level <= playerLevel; level++) {
            availableRaids.addAll(getRaidsAtLevel(level));
        }

        return availableRaids;
    }

    public static int getMaxRaidLevel() {
        return RAIDS_BY_LEVEL.keySet().stream()
                .max(Integer::compareTo)
                .orElse(0);
    }

    public static Collection<RaidData> getAllRaids() {
        return RAIDS_BY_ID.values();
    }

    public static Collection<RaidData> getProgressionRaids() {
        List<RaidData> progressionRaids = new ArrayList<>();
        for (RaidData raid : RAIDS_BY_ID.values()) {
            if (raid.raidLevel() != null) {
                progressionRaids.add(raid);
            }
        }
        return progressionRaids;
    }

    public static Collection<RaidData> getCustomRaids() {
        List<RaidData> customRaids = new ArrayList<>();
        for (RaidData raid : RAIDS_BY_ID.values()) {
            if (raid.raidLevel() == null) {
                customRaids.add(raid);
            }
        }
        return customRaids;
    }

    public static boolean hasRaidWithId(String raidId) {
        return RAIDS_BY_ID.containsKey(raidId);
    }

    @Nullable
    private static RaidData parseRaidData(JsonObject json) {
        try {
            String raidId = json.get("raid_id").getAsString();

            Integer raidLevel = null;
            if (json.has("raid_level")) {
                raidLevel = json.get("raid_level").getAsInt();
            }

            BossData boss = parseBossData(json.getAsJsonObject("boss"));
            HenchmenData henchmen = parseHenchmenData(json.getAsJsonObject("henchmen"));
            SpawnConditions conditions = parseSpawnConditions(json.getAsJsonObject("spawn_conditions"));

            return new RaidData(raidId, raidLevel, boss, henchmen, conditions);

        } catch (Exception e) {
            LOGGER.error("Error parsing raid data", e);
            return null;
        }
    }

    @Nullable
    private static BossData parseBossData(JsonObject json) {
        try {
            String entityId = json.get("entity_type").getAsString();
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityId));
            if (entityType == null) {
                LOGGER.warn("Unknown entity type: {}", entityId);
                return null;
            }

            String customName = json.has("custom_name") ? json.get("custom_name").getAsString() : null;
            HealthConfig healthConfig = parseHealthConfig(json);

            WeaponEntry weapon = null;
            if (json.has("weapon")) {
                JsonObject weaponObj = json.getAsJsonObject("weapon");
                Item weaponItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(weaponObj.get("item").getAsString()));
                float dropChance = weaponObj.has("drop_chance") ? weaponObj.get("drop_chance").getAsFloat() : 0.085f;
                CompoundTag nbt = null;
                if (weaponObj.has("nbt")) {
                    nbt = parseNBT(weaponObj.getAsJsonObject("nbt"));
                }
                weapon = new WeaponEntry(weaponItem, dropChance, nbt);
            }

            List<ArmorEntry> armor = new ArrayList<>();
            if (json.has("armor")) {
                JsonArray armorArray = json.getAsJsonArray("armor");
                for (JsonElement element : armorArray) {
                    JsonObject armorObj = element.getAsJsonObject();
                    Item armorItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(armorObj.get("item").getAsString()));
                    String slot = armorObj.get("slot").getAsString();
                    float dropChance = armorObj.has("drop_chance") ? armorObj.get("drop_chance").getAsFloat() : 0.085f;
                    CompoundTag nbt = null;
                    if (armorObj.has("nbt")) {
                        nbt = parseNBT(armorObj.getAsJsonObject("nbt"));
                    }
                    if (armorItem != null) {
                        armor.add(new ArmorEntry(armorItem, slot, dropChance, nbt));
                    }
                }
            }

            List<EffectEntry> effects = parseEffects(json);

            int aiDifficulty = json.has("ai_difficulty") ? json.get("ai_difficulty").getAsInt() : 3;
            AIType aiType = json.has("ai_type") ? AIType.valueOf(json.get("ai_type").getAsString()) : AIType.DEFAULT;

            ResourceLocation lootTable = json.has("special_loot_table") ?
                    new ResourceLocation(json.get("special_loot_table").getAsString()) : null;

            MountData mount = null;
            if (json.has("mount")) {
                mount = parseMountData(json.getAsJsonObject("mount"));
            }

            return new BossData(entityType, customName, healthConfig, weapon, armor, effects, aiDifficulty, aiType, lootTable, mount);

        } catch (Exception e) {
            LOGGER.error("Error parsing boss data", e);
            return null;
        }
    }

    @Nullable
    private static MountData parseMountData(JsonObject json) {
        try {
            String mountId = json.get("entity_type").getAsString();
            EntityType<?> mountType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(mountId));
            if (mountType == null) {
                LOGGER.warn("Unknown mount type: {}", mountId);
                return null;
            }

            HealthConfig healthConfig = parseHealthConfig(json);

            List<ArmorEntry> armor = new ArrayList<>();
            if (json.has("armor")) {
                JsonArray armorArray = json.getAsJsonArray("armor");
                for (JsonElement element : armorArray) {
                    JsonObject armorObj = element.getAsJsonObject();
                    Item armorItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(armorObj.get("item").getAsString()));
                    String slot = armorObj.get("slot").getAsString();
                    float dropChance = armorObj.has("drop_chance") ? armorObj.get("drop_chance").getAsFloat() : 0.085f;
                    CompoundTag nbt = null;
                    if (armorObj.has("nbt")) {
                        nbt = parseNBT(armorObj.getAsJsonObject("nbt"));
                    }
                    if (armorItem != null) {
                        armor.add(new ArmorEntry(armorItem, slot, dropChance, nbt));
                    }
                }
            }

            List<EffectEntry> effects = parseEffects(json);
            boolean dropsLoot = !json.has("drops_loot") || json.get("drops_loot").getAsBoolean();

            return new MountData(mountType, healthConfig, armor, effects, dropsLoot);

        } catch (Exception e) {
            LOGGER.error("Error parsing mount data", e);
            return null;
        }
    }

    @Nullable
    private static HenchmenData parseHenchmenData(JsonObject json) {
        try {
            List<HenchmanType> types = new ArrayList<>();

            if (json.has("types")) {
                JsonArray typesArray = json.getAsJsonArray("types");
                for (JsonElement element : typesArray) {
                    JsonObject typeObj = element.getAsJsonObject();

                    String entityId = typeObj.get("entity_type").getAsString();
                    EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityId));
                    if (entityType == null) continue;

                    float weight = typeObj.has("weight") ? typeObj.get("weight").getAsFloat() : 1.0f;
                    HealthConfig healthConfig = parseHealthConfig(typeObj);

                    List<Item> weapons = new ArrayList<>();
                    if (typeObj.has("weapons")) {
                        JsonArray weaponsArray = typeObj.getAsJsonArray("weapons");
                        for (JsonElement weaponElement : weaponsArray) {
                            Item weapon = ForgeRegistries.ITEMS.getValue(new ResourceLocation(weaponElement.getAsString()));
                            if (weapon != null) {
                                weapons.add(weapon);
                            }
                        }
                    }

                    List<ArmorEntry> armor = new ArrayList<>();
                    if (typeObj.has("armor")) {
                        JsonArray armorArray = typeObj.getAsJsonArray("armor");
                        for (JsonElement armorElement : armorArray) {
                            JsonObject armorObj = armorElement.getAsJsonObject();
                            Item armorItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(armorObj.get("item").getAsString()));
                            String slot = armorObj.get("slot").getAsString();
                            float chance = armorObj.has("chance") ? armorObj.get("chance").getAsFloat() : 0.5f;
                            CompoundTag nbt = null;
                            if (armorObj.has("nbt")) {
                                nbt = parseNBT(armorObj.getAsJsonObject("nbt"));
                            }
                            if (armorItem != null) {
                                armor.add(new ArmorEntry(armorItem, slot, chance, nbt));
                            }
                        }
                    }

                    List<EffectEntry> effects = parseEffects(typeObj);

                    int aiDifficulty = typeObj.has("ai_difficulty") ? typeObj.get("ai_difficulty").getAsInt() : 2;
                    AIType aiType = typeObj.has("ai_type") ? AIType.valueOf(typeObj.get("ai_type").getAsString()) : AIType.DEFAULT;

                    types.add(new HenchmanType(entityType, weight, healthConfig, weapons, armor, effects, aiDifficulty, aiType));
                }
            }

            int maxAlive = json.has("max_concurrent") ? json.get("max_concurrent").getAsInt() :
                    json.has("max_alive") ? json.get("max_alive").getAsInt() : 4;
            int maxTotal = json.has("max_total") ? json.get("max_total").getAsInt() : 15;
            int spawnInterval = json.has("spawn_interval_ticks") ? json.get("spawn_interval_ticks").getAsInt() : 200;
            int spawnRadius = json.has("spawn_radius") ? json.get("spawn_radius").getAsInt() : 20;
            int spawnAttempts = json.has("spawn_attempts_per_wave") ? json.get("spawn_attempts_per_wave").getAsInt() : 3;

            return new HenchmenData(types, maxAlive, maxTotal, spawnInterval, spawnRadius, spawnAttempts);

        } catch (Exception e) {
            LOGGER.error("Error parsing henchmen data", e);
            return null;
        }
    }
    @Nullable
    private static CompoundTag parseNBT(JsonObject json) {
        try {
            CompoundTag tag = new CompoundTag();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                if (value.isJsonPrimitive()) {
                    JsonPrimitive primitive = value.getAsJsonPrimitive();
                    if (primitive.isNumber()) {
                        if (primitive.getAsString().contains(".")) {
                            tag.putFloat(key, primitive.getAsFloat());
                        } else {
                            tag.putInt(key, primitive.getAsInt());
                        }
                    } else if (primitive.isString()) {
                        tag.putString(key, primitive.getAsString());
                    } else if (primitive.isBoolean()) {
                        tag.putBoolean(key, primitive.getAsBoolean());
                    }
                } else if (value.isJsonArray()) {
                    JsonArray array = value.getAsJsonArray();
                    switch (key) {
                        case "Enchantments" -> {
                            ListTag enchantments = getTags(array);
                            tag.put(key, enchantments);
                        }
                        case "Lore" -> {
                            ListTag loreList = new ListTag();
                            for (JsonElement elem : array) {
                                loreList.add(StringTag.valueOf(elem.getAsString()));
                            }
                            tag.put(key, loreList);
                        }
                        case "AttributeModifiers" -> {
                            ListTag modifiers = new ListTag();
                            for (JsonElement elem : array) {
                                if (elem.isJsonObject()) {
                                    modifiers.add(parseNBT(elem.getAsJsonObject()));
                                }
                            }
                            tag.put(key, modifiers);
                        }
                        default -> {
                            ListTag list = new ListTag();
                            for (JsonElement elem : array) {
                                if (elem.isJsonObject()) {
                                    list.add(parseNBT(elem.getAsJsonObject()));
                                } else if (elem.isJsonPrimitive()) {
                                    JsonPrimitive prim = elem.getAsJsonPrimitive();
                                    if (prim.isString()) {
                                        list.add(StringTag.valueOf(prim.getAsString()));
                                    } else if (prim.isNumber()) {
                                        CompoundTag numTag = new CompoundTag();
                                        numTag.putInt("value", prim.getAsInt());
                                        list.add(numTag);
                                    }
                                }
                            }
                            tag.put(key, list);
                        }
                    }
                } else if (value.isJsonObject()) {
                    tag.put(key, Objects.requireNonNull(parseNBT(value.getAsJsonObject())));
                }
            }

            return tag;
        } catch (Exception e) {
            LOGGER.error("Error parsing NBT data", e);
            return null;
        }
    }

    private static @NotNull ListTag getTags(JsonArray array) {
        ListTag enchantments = new ListTag();
        for (JsonElement elem : array) {
            if (elem.isJsonObject()) {
                JsonObject enchObj = elem.getAsJsonObject();
                CompoundTag enchTag = new CompoundTag();
                enchTag.putString("id", enchObj.get("id").getAsString());
                enchTag.putInt("lvl", enchObj.get("lvl").getAsInt());
                enchantments.add(enchTag);
            }
        }
        return enchantments;
    }

    @Nullable
    private static HealthConfig parseHealthConfig(JsonObject json) {
        Float fixedHealth = json.has("fixed_health") ? json.get("fixed_health").getAsFloat() : null;
        Float healthMultiplier = json.has("health_multiplier") ? json.get("health_multiplier").getAsFloat() : null;
        return new HealthConfig(fixedHealth, healthMultiplier);
    }

    private static List<EffectEntry> parseEffects(JsonObject json) {
        List<EffectEntry> effects = new ArrayList<>();

        if (json.has("effects")) {
            JsonArray effectsArray = json.getAsJsonArray("effects");
            for (JsonElement element : effectsArray) {
                JsonObject effectObj = element.getAsJsonObject();
                String effectId = effectObj.get("effect").getAsString();
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectId));

                if (effect != null) {
                    int amplifier = effectObj.has("amplifier") ? effectObj.get("amplifier").getAsInt() : 0;
                    int duration = effectObj.has("duration") ? effectObj.get("duration").getAsInt() : -1;
                    boolean ambient = effectObj.has("ambient") && effectObj.get("ambient").getAsBoolean();
                    boolean visible = !effectObj.has("visible") || effectObj.get("visible").getAsBoolean();

                    effects.add(new EffectEntry(effect, amplifier, duration, ambient, visible));
                }
            }
        }

        return effects;
    }

    @Nullable
    private static SpawnConditions parseSpawnConditions(JsonObject json) {
        try {
            int minPlayers = json.has("min_players_nearby") ? json.get("min_players_nearby").getAsInt() : 1;
            int searchRadius = json.has("search_radius") ? json.get("search_radius").getAsInt() : 64;

            List<ResourceLocation> validDimensions = new ArrayList<>();
            if (json.has("valid_dimensions")) {
                JsonArray dimsArray = json.getAsJsonArray("valid_dimensions");
                for (JsonElement element : dimsArray) {
                    validDimensions.add(new ResourceLocation(element.getAsString()));
                }
            } else {
                validDimensions.add(new ResourceLocation("minecraft:overworld"));
            }

            String announcement = json.has("announcement_message") ?
                    json.get("announcement_message").getAsString() : "Â§cÂ§lA raid approaches!";

            return new SpawnConditions(minPlayers, searchRadius, validDimensions, announcement);

        } catch (Exception e) {
            LOGGER.error("Error parsing spawn conditions", e);
            return null;
        }
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
                loadRaidConfigs(resourceManager);
            }
        });
    }
}