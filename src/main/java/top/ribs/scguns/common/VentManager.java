package top.ribs.scguns.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ribs.scguns.Reference;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class VentManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<ResourceLocation, Vent> VENT_CONFIGS = new HashMap<>();
    private static final Map<ResourceLocation, VentCollectorConfig> COLLECTOR_CONFIGS = new HashMap<>();
    private static final String VENT_FOLDER = "vents";

    @Nullable
    public static Vent getVent(ResourceLocation id) {
        return VENT_CONFIGS.get(id);
    }

    @Nullable
    public static VentCollectorConfig getVentCollectorConfig(ResourceLocation id) {
        return COLLECTOR_CONFIGS.get(id);
    }

    public static void loadVentConfig(ResourceManager resourceManager, ResourceLocation ventId) {
        ResourceLocation location = new ResourceLocation(
                ventId.getNamespace(),
                VENT_FOLDER + "/" + ventId.getPath() + ".json"
        );

        try {
            Resource resource = resourceManager.getResource(location).orElse(null);
            if (resource != null) {
                try (InputStream stream = resource.open();
                     InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    Vent vent = parseVent(json);
                    if (vent != null) {
                        VENT_CONFIGS.put(ventId, vent);
                        LOGGER.info("Successfully loaded vent config: {}", ventId);
                    }
                }
            } else {
                LOGGER.warn("No JSON found for vent: {}", ventId);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load vent config: {}", location, e);
        }
    }

    public static void loadVentCollectorConfig(ResourceManager resourceManager, ResourceLocation collectorId) {
        ResourceLocation location = new ResourceLocation(
                collectorId.getNamespace(),
                VENT_FOLDER + "/" + collectorId.getPath() + ".json"
        );

        try {
            Resource resource = resourceManager.getResource(location).orElse(null);
            if (resource != null) {
                try (InputStream stream = resource.open();
                     InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    VentCollectorConfig config = parseVentCollector(json);
                    if (config != null) {
                        COLLECTOR_CONFIGS.put(collectorId, config);
                        LOGGER.info("Successfully loaded vent collector config: {}", collectorId);
                    }
                }
            } else {
                LOGGER.warn("No JSON found for vent collector: {}", collectorId);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load vent collector config: {}", location, e);
        }
    }

    private static Vent parseVent(JsonObject json) {
        try {
            Vent vent = new Vent();

            if (json.has("activation")) {
                parseActivation(json.getAsJsonObject("activation"), vent.getActivation());
            }
            if (json.has("power")) {
                parsePower(json.getAsJsonObject("power"), vent.getPower());
            }
            if (json.has("production")) {
                parseProduction(json.getAsJsonObject("production"), vent.getProduction());
            }
            if (json.has("placement")) {
                parsePlacement(json.getAsJsonObject("placement"), vent.getPlacement());
            }
            if (json.has("particles")) {
                parseParticles(json.getAsJsonObject("particles"), vent.getParticles());
            }

            return vent;
        } catch (Exception e) {
            LOGGER.error("Error parsing vent config", e);
            return null;
        }
    }

    private static VentCollectorConfig parseVentCollector(JsonObject json) {
        try {
            VentCollectorConfig config = new VentCollectorConfig();

            if (json.has("filters")) {
                parseFilters(json.getAsJsonObject("filters"), config.getFilters());
            }
            if (json.has("processing")) {
                parseProcessing(json.getAsJsonObject("processing"), config.getProcessing());
            }

            return config;
        } catch (Exception e) {
            LOGGER.error("Error parsing vent collector config", e);
            return null;
        }
    }

    private static void parseFilters(JsonObject json, VentCollectorConfig.Filters filters) {
        if (json.has("maxCharge")) {
            filters.setMaxCharge(json.get("maxCharge").getAsInt());
        }
        if (json.has("consumptionChance")) {
            filters.setConsumptionChance(json.get("consumptionChance").getAsFloat());
        }
        if (json.has("processCooldown")) {
            filters.setProcessCooldown(json.get("processCooldown").getAsInt());
        }
        if (json.has("filterItems")) {
            filters.clearFilterItems();
            var itemsArray = json.getAsJsonArray("filterItems");
            for (var element : itemsArray) {
                JsonObject itemObj = element.getAsJsonObject();
                VentCollectorConfig.Filters.FilterItem filterItem = new VentCollectorConfig.Filters.FilterItem();

                if (itemObj.has("tag")) {
                    filterItem.setIdentifier(new ResourceLocation(itemObj.get("tag").getAsString()));
                    filterItem.setIsTag(true);
                } else if (itemObj.has("item")) {
                    filterItem.setIdentifier(new ResourceLocation(itemObj.get("item").getAsString()));
                    filterItem.setIsTag(false);
                }

                if (itemObj.has("chargeAmount")) {
                    filterItem.setChargeAmount(itemObj.get("chargeAmount").getAsInt());
                }

                filters.addFilterItem(filterItem);
            }
        }
    }

    private static void parseProcessing(JsonObject json, VentCollectorConfig.Processing processing) {
        if (json.has("powerSpeedMultiplier")) {
            processing.setPowerSpeedMultiplier(json.get("powerSpeedMultiplier").getAsFloat());
        }
        if (json.has("pushCooldown")) {
            processing.setPushCooldown(json.get("pushCooldown").getAsInt());
        }
    }

    private static void parseActivation(JsonObject json, Vent.Activation activation) {
        if (json.has("baseBlock")) {
            activation.setBaseBlock(new ResourceLocation(json.get("baseBlock").getAsString()));
        }
        if (json.has("requiresWaterlogged")) {
            activation.setRequiresWaterlogged(json.get("requiresWaterlogged").getAsBoolean());
        }
    }

    private static void parsePower(JsonObject json, Vent.Power power) {
        if (json.has("maxPower")) {
            power.setMaxPower(json.get("maxPower").getAsInt());
        }
        if (json.has("baseTickInterval")) {
            power.setBaseTickInterval(json.get("baseTickInterval").getAsInt());
        }
        if (json.has("tickWiggleRoom")) {
            power.setTickWiggleRoom(json.get("tickWiggleRoom").getAsInt());
        }
    }

    private static void parseProduction(JsonObject json, Vent.Production production) {
        if (json.has("outputs")) {
            production.clearOutputs();
            var outputsArray = json.getAsJsonArray("outputs");
            for (var element : outputsArray) {
                JsonObject outputObj = element.getAsJsonObject();
                Vent.Production.OutputItem output = new Vent.Production.OutputItem();

                if (outputObj.has("item")) {
                    output.setItem(new ResourceLocation(outputObj.get("item").getAsString()));
                }
                if (outputObj.has("weight")) {
                    output.setWeight(outputObj.get("weight").getAsInt());
                }

                production.addOutput(output);
            }
        }

        if (json.has("productionChance")) {
            production.setProductionChance(json.get("productionChance").getAsFloat());
        }
    }

    private static void parsePlacement(JsonObject json, Vent.Placement placement) {
        if (json.has("enabled")) {
            placement.setEnabled(json.get("enabled").getAsBoolean());
        }
        if (json.has("blockToPlace")) {
            placement.setBlockToPlace(new ResourceLocation(json.get("blockToPlace").getAsString()));
        }
        if (json.has("radius")) {
            placement.setRadius(json.get("radius").getAsInt());
        }
        if (json.has("placementChance")) {
            placement.setPlacementChance(json.get("placementChance").getAsFloat());
        }
    }

    private static void parseParticles(JsonObject json, Vent.Particles particles) {
        if (json.has("showActive")) {
            particles.setShowActive(json.get("showActive").getAsBoolean());
        }
        if (json.has("activeSound")) {
            particles.setActiveSound(new ResourceLocation(json.get("activeSound").getAsString()));
        }
    }

    public static void clearAll() {
        VENT_CONFIGS.clear();
        COLLECTOR_CONFIGS.clear();
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                clearAll();
                loadVentConfig(resourceManager, new ResourceLocation(Reference.MOD_ID, "geothermal_vent"));
                loadVentConfig(resourceManager, new ResourceLocation(Reference.MOD_ID, "sulfur_vent"));

                loadVentCollectorConfig(resourceManager, new ResourceLocation(Reference.MOD_ID, "vent_collector"));

                VENT_CONFIGS.get(new ResourceLocation(Reference.MOD_ID, "geothermal_vent"));
                COLLECTOR_CONFIGS.get(new ResourceLocation(Reference.MOD_ID, "vent_collector"));

                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                LOGGER.info("Loaded {} vent configurations and {} collector configurations",
                        VENT_CONFIGS.size(), COLLECTOR_CONFIGS.size());
            }
        });
    }
}