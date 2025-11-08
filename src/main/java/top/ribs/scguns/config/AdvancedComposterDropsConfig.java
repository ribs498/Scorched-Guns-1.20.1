package top.ribs.scguns.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid = "scguns")
public class AdvancedComposterDropsConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation CONFIG_LOCATION = new ResourceLocation("scguns", "composter/advanced_composter_drops.json");

    private static ComposterLootTable LOOT_TABLE = new ComposterLootTable();

    /**
     * Represents a single drop entry with weight and stack size configuration
     */
    public record DropEntry(Item item, int weight, int minCount, int maxCount) {
        public ItemStack createStack(Random random) {
            int count = minCount;
            if (maxCount > minCount) {
                count = minCount + random.nextInt(maxCount - minCount + 1);
            }
            return new ItemStack(item, count);
        }

        public static DropEntry fromJson(JsonObject json) {
            String itemId = json.get("item").getAsString();
            int weight = json.has("weight") ? json.get("weight").getAsInt() : 1;
            int minCount = json.has("min_count") ? json.get("min_count").getAsInt() : 1;
            int maxCount = json.has("max_count") ? json.get("max_count").getAsInt() : minCount;

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item == null) {
                LOGGER.warn("Unknown item in composter drops: {}", itemId);
                return null;
            }

            return new DropEntry(item, weight, minCount, maxCount);
        }
    }

    /**
     * Represents the complete loot table with drop pools and global settings
     */
    public static class ComposterLootTable {
        private final List<DropEntry> drops = new ArrayList<>();
        private int totalWeight = 0;
        private int minDrops = 1;
        private int maxDrops = 3;

        public void addDrop(DropEntry entry) {
            drops.add(entry);
            totalWeight += entry.weight();
        }

        public void setDropRange(int min, int max) {
            this.minDrops = Math.max(1, min);
            this.maxDrops = Math.max(this.minDrops, max);
        }

        public List<ItemStack> generateDrops(Random random) {
            if (drops.isEmpty()) {
                LOGGER.warn("No drops configured for advanced composter!");
                return Collections.emptyList();
            }

            int dropCount = minDrops;
            if (maxDrops > minDrops) {
                dropCount = minDrops + random.nextInt(maxDrops - minDrops + 1);
            }

            List<ItemStack> result = new ArrayList<>();
            for (int i = 0; i < dropCount; i++) {
                DropEntry entry = selectWeightedDrop(random);
                if (entry != null) {
                    result.add(entry.createStack(random));
                }
            }

            return result;
        }

        @Nullable
        private DropEntry selectWeightedDrop(Random random) {
            if (drops.isEmpty() || totalWeight <= 0) {
                return null;
            }

            int randomWeight = random.nextInt(totalWeight);
            int currentWeight = 0;

            for (DropEntry entry : drops) {
                currentWeight += entry.weight();
                if (randomWeight < currentWeight) {
                    return entry;
                }
            }

            // Fallback (shouldn't happen with proper weights)
            return drops.get(drops.size() - 1);
        }

        public void clear() {
            drops.clear();
            totalWeight = 0;
            minDrops = 1;
            maxDrops = 3;
        }

        public boolean isEmpty() {
            return drops.isEmpty();
        }

        public int getDropCount() {
            return drops.size();
        }
    }

    /**
     * Load the composter drops configuration from JSON
     */
    public static void loadConfig(ResourceManager resourceManager) {
        LOOT_TABLE.clear();

        try {
            Resource resource = resourceManager.getResource(CONFIG_LOCATION).orElse(null);
            if (resource != null) {
                try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(reader, JsonObject.class);

                    if (json != null) {
                        // Load drop count range
                        if (json.has("min_drops") && json.has("max_drops")) {
                            int minDrops = json.get("min_drops").getAsInt();
                            int maxDrops = json.get("max_drops").getAsInt();
                            LOOT_TABLE.setDropRange(minDrops, maxDrops);
                        }

                        // Load drop entries
                        if (json.has("drops")) {
                            JsonArray dropsArray = json.getAsJsonArray("drops");
                            for (JsonElement dropElement : dropsArray) {
                                DropEntry entry = DropEntry.fromJson(dropElement.getAsJsonObject());
                                if (entry != null) {
                                    LOOT_TABLE.addDrop(entry);
                                }
                            }
                        }

                        LOGGER.info("Loaded advanced composter drops config: {} drops configured, total weight: {}",
                                LOOT_TABLE.getDropCount(), LOOT_TABLE.totalWeight);
                    }
                }
            } else {
                LOGGER.warn("Advanced composter drops config not found at {}", CONFIG_LOCATION);
                loadDefaultConfig();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load advanced composter drops config at {}", CONFIG_LOCATION, e);
            loadDefaultConfig();
        }
    }

    /**
     * Load a default configuration as fallback
     */
    private static void loadDefaultConfig() {
        LOGGER.info("Loading default composter drops configuration");
        LOOT_TABLE.clear();

        // Add some sensible defaults
        Item bonemeal = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:bone_meal"));
        if (bonemeal != null) {
            LOOT_TABLE.addDrop(new DropEntry(bonemeal, 100, 1, 3));
        }

        LOOT_TABLE.setDropRange(1, 3);
    }

    /**
     * Generate drops for the composter
     */
    public static List<ItemStack> generateDrops(Random random) {
        return LOOT_TABLE.generateDrops(random);
    }

    /**
     * Check if any drops are configured
     */
    public static boolean hasDropsConfigured() {
        return !LOOT_TABLE.isEmpty();
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