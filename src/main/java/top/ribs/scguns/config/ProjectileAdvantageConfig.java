package top.ribs.scguns.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = "scguns")
public class ProjectileAdvantageConfig {
    private static final Map<String, AdvantageData> ADVANTAGE_MAP = new HashMap<>();
    private static final ResourceLocation CONFIG_LOCATION = new ResourceLocation("scguns", "entity/advantages.json");

    public record AdvantageData(float multiplier, boolean causesFire, int fireDuration, Set<String> targetTags) {
    }

    public static void loadConfig(ResourceManager resourceManager) {
        ADVANTAGE_MAP.clear();
        try {
            Resource resource = resourceManager.getResource(CONFIG_LOCATION).orElse(null);
            if (resource != null) {
                try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(reader, JsonObject.class);

                    if (json != null && json.has("advantages")) {
                        JsonObject advantages = json.getAsJsonObject("advantages");
                        for (Map.Entry<String, JsonElement> entry : advantages.entrySet()) {
                            String advantageKey = entry.getKey();
                            JsonObject advantageObj = entry.getValue().getAsJsonObject();

                            float multiplier = advantageObj.has("multiplier") ?
                                    advantageObj.get("multiplier").getAsFloat() : 1.0f;
                            boolean causesFire = advantageObj.has("causes_fire") && advantageObj.get("causes_fire").getAsBoolean();
                            int fireDuration = advantageObj.has("fire_duration") ?
                                    advantageObj.get("fire_duration").getAsInt() : 2;

                            Set<String> targetTags = new HashSet<>();
                            if (advantageObj.has("target_tags")) {
                                for (JsonElement tagElement : advantageObj.getAsJsonArray("target_tags")) {
                                    targetTags.add(tagElement.getAsString());
                                }
                            }

                            ADVANTAGE_MAP.put(advantageKey, new AdvantageData(multiplier, causesFire, fireDuration, targetTags));
                        }
                    }
                }
            } else {
                loadDefaults();
            }
        } catch (Exception e) {
            loadDefaults();
        }
    }

    private static void loadDefaults() {
        ADVANTAGE_MAP.put("scguns:undead", new AdvantageData(1.25f, true, 2,
                Set.of("scguns:undead", "scguns:wither", "scguns:ghost")));
        ADVANTAGE_MAP.put("scguns:heavy", new AdvantageData(1.25f, false, 0,
                Set.of("scguns:heavy", "scguns:very_heavy")));
        ADVANTAGE_MAP.put("scguns:very_heavy", new AdvantageData(1.25f, false, 0,
                Set.of("scguns:heavy", "scguns:very_heavy")));
        ADVANTAGE_MAP.put("scguns:fire", new AdvantageData(1.25f, false, 0,
                Set.of("scguns:fire")));
        ADVANTAGE_MAP.put("scguns:illager", new AdvantageData(1.5f, false, 0,
                Set.of("scguns:illager")));
        ADVANTAGE_MAP.put("scguns:water", new AdvantageData(1.65f, false, 0,
                Set.of("scguns:water")));
        ADVANTAGE_MAP.put("scguns:bot", new AdvantageData(1.5f, false, 0,
                Set.of("scguns:bot")));
        ADVANTAGE_MAP.put("scguns:wither", new AdvantageData(1.25f, false, 0,
                Set.of("scguns:wither")));
    }

    public static AdvantageData getAdvantageData(String advantageKey) {
        return ADVANTAGE_MAP.get(advantageKey);
    }

    public static Map<String, AdvantageData> getAllAdvantages() {
        return new HashMap<>(ADVANTAGE_MAP);
    }
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(@NotNull ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                loadConfig(resourceManager);
            }
        });
    }
}