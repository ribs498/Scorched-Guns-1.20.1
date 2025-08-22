package top.ribs.scguns.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "scguns")
public class BrassBoltConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, Double> DAMAGE_VALUES = new HashMap<>();
    private static final double DEFAULT_DAMAGE = 4.0; // Fallback damage
    private static final ResourceLocation CONFIG_LOCATION = new ResourceLocation("scguns", "entity/brass_bolt_damage.json");

    public static void loadConfig(ResourceManager resourceManager) {
        DAMAGE_VALUES.clear();
        try {
            Resource resource = resourceManager.getResource(CONFIG_LOCATION).orElse(null);
            if (resource != null) {
                try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(reader, JsonObject.class);
                    JsonObject damageValues = json.getAsJsonObject("damage_values");
                    if (damageValues != null) {
                        for (Map.Entry<String, com.google.gson.JsonElement> entry : damageValues.entrySet()) {
                            DAMAGE_VALUES.put(entry.getKey(), entry.getValue().getAsDouble());
                        }
                    }
                    LOGGER.info("Loaded brass bolt damage config: {}", DAMAGE_VALUES);
                }
            } else {
                LOGGER.warn("Brass bolt damage config not found at {}", CONFIG_LOCATION);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load brass bolt damage config at {}", CONFIG_LOCATION, e);
        }
    }

    public static double getDamageForEntity(EntityType<?> entityType) {
        String entityId = EntityType.getKey(entityType).toString();
        return DAMAGE_VALUES.getOrDefault(entityId, DEFAULT_DAMAGE);
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