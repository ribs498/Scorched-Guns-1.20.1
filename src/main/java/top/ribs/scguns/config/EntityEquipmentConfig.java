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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ribs.scguns.util.GunCurseUtil;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid = "scguns")
public class EntityEquipmentConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, EquipmentData> CONFIGS = new HashMap<>();

    public record EquipmentEntry(
            Item item,
            float spawnWeight,
            float dropChance,
            @Nullable Float minDurability,
            @Nullable Float maxDurability
    ) {
        public ItemStack createItemStack(RandomSource random) {
            ItemStack stack = new ItemStack(item);

            if (stack.isDamageableItem() && minDurability != null && maxDurability != null) {
                float durabilityPercent = minDurability + random.nextFloat() * (maxDurability - minDurability);
                int damage = (int)(stack.getMaxDamage() * (1 - durabilityPercent));
                stack.setDamageValue(damage);
            }

            return stack;
        }
    }

    public record EquipmentData(
            float equipmentChance,
            List<EquipmentEntry> entries
    ) {
        @Nullable
        public EquipmentEntry selectRandom(RandomSource random) {
            if (entries.isEmpty()) return null;

            float totalWeight = 0;
            for (EquipmentEntry entry : entries) {
                totalWeight += entry.spawnWeight;
            }

            float roll = random.nextFloat() * totalWeight;
            float currentWeight = 0;

            for (EquipmentEntry entry : entries) {
                currentWeight += entry.spawnWeight;
                if (roll < currentWeight) {
                    return entry;
                }
            }

            return entries.get(entries.size() - 1);
        }
    }

    public static void loadConfig(ResourceManager resourceManager, String entityId) {
        ResourceLocation location = new ResourceLocation("scguns", "entity/equipment/" + entityId + ".json");

        try {
            Resource resource = resourceManager.getResource(location).orElse(null);
            if (resource != null) {
                try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(reader, JsonObject.class);

                    EquipmentData data = parseEquipmentData(json);
                    if (data != null) {
                        CONFIGS.put(entityId, data);
                        LOGGER.info("Loaded equipment config for {}: {} entries", entityId, data.entries.size());
                    }
                }
            } else {
                LOGGER.warn("Equipment config not found: {}", location);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load equipment config: {}", location, e);
        }
    }

    private static EquipmentData parseEquipmentData(JsonObject json) {
        float equipmentChance = json.has("equipment_chance") ?
                json.get("equipment_chance").getAsFloat() : 0.75f;

        List<EquipmentEntry> entries = new ArrayList<>();

        if (json.has("items")) {
            JsonArray itemsArray = json.getAsJsonArray("items");
            for (JsonElement element : itemsArray) {
                JsonObject itemObj = element.getAsJsonObject();
                EquipmentEntry entry = parseEntry(itemObj);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }

        return new EquipmentData(equipmentChance, entries);
    }

    @Nullable
    private static EquipmentEntry parseEntry(JsonObject json) {
        try {
            String itemId = json.get("item").getAsString();
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item == null) {
                LOGGER.warn("Unknown item: {}", itemId);
                return null;
            }

            float spawnWeight = json.has("weight") ? json.get("weight").getAsFloat() : 1.0f;
            float dropChance = json.has("drop_chance") ? json.get("drop_chance").getAsFloat() : 0.2f;
            Float minDurability = json.has("min_durability") ? json.get("min_durability").getAsFloat() : null;
            Float maxDurability = json.has("max_durability") ? json.get("max_durability").getAsFloat() : null;

            return new EquipmentEntry(item, spawnWeight, dropChance, minDurability, maxDurability);

        } catch (Exception e) {
            LOGGER.error("Error parsing equipment entry", e);
            return null;
        }
    }

    @Nullable
    public static EquipmentData getEquipmentData(String entityId) {
        return CONFIGS.get(entityId);
    }

    public static void equipEntity(net.minecraft.world.entity.Mob mob, String entityId) {
        EquipmentData data = getEquipmentData(entityId);
        if (data == null) return;

        if (mob.getRandom().nextFloat() >= data.equipmentChance) return;

        EquipmentEntry entry = data.selectRandom(mob.getRandom());
        if (entry == null) return;

        ItemStack stack = entry.createItemStack(mob.getRandom());
        GunCurseUtil.applyCurseIfRoll(stack, mob.getRandom());
        mob.setItemSlot(EquipmentSlot.MAINHAND, stack);
        mob.setDropChance(EquipmentSlot.MAINHAND, entry.dropChance);
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
                CONFIGS.clear();
                loadConfig(resourceManager, "cog_minion");
                loadConfig(resourceManager, "cog_knight");
                loadConfig(resourceManager, "adjudicator");
                loadConfig(resourceManager, "subjugator");
            }
        });
    }
}