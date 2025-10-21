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
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid = "scguns")
public class RaidFlareConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, FlareData> FLARES = new HashMap<>();

    public record ParticleEffect(
            String particleType,
            int count,
            double spread,
            double speed,
            int color
    ) {}

    public record FlarePattern(
            String patternType,
            List<Vec3Data> points,
            int repetitions,
            double scale
    ) {}

    public record Vec3Data(double x, double y, double z) {}

    public record FlareData(
            String raidId,
            int burstDelay,
            int duration,
            List<ParticleEffect> trailParticles,
            List<ParticleEffect> burstParticles,
            @Nullable FlarePattern pattern,
            String burstSound,
            float burstSoundVolume,
            float burstSoundPitch
    ) {}

    public static void loadFlareConfigs(ResourceManager resourceManager) {
        FLARES.clear();

        ResourceLocation flareFolder = new ResourceLocation("scguns", "flares");

        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources("flares",
                loc -> loc.getPath().endsWith(".json")).entrySet()) {

            ResourceLocation location = entry.getKey();
            String path = location.getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String flareId = fileName.replace("_flare.json", "").replace(".json", "");

            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(reader, JsonObject.class);

                FlareData flareData = parseFlareData(json, flareId);
                if (flareData != null) {
                    FLARES.put(flareData.raidId(), flareData);
                    LOGGER.info("Loaded flare config: {} -> raid: {}", flareId, flareData.raidId());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load flare config: {}", flareId, e);
            }
        }

        LOGGER.info("Loaded {} flare configurations", FLARES.size());
    }

    @Nullable
    private static FlareData parseFlareData(JsonObject json, String flareId) {
        try {
            String raidId = json.has("raid_id") ? json.get("raid_id").getAsString() : flareId;

            int burstDelay = json.has("burst_delay") ? json.get("burst_delay").getAsInt() : 40;
            int duration = json.has("duration") ? json.get("duration").getAsInt() : 200;

            List<ParticleEffect> trailParticles = parseParticleEffects(json, "trail_particles");
            List<ParticleEffect> burstParticles = parseParticleEffects(json, "burst_particles");

            FlarePattern pattern = null;
            if (json.has("pattern")) {
                pattern = parsePattern(json.getAsJsonObject("pattern"));
            }

            String burstSound = json.has("burst_sound") ?
                    json.get("burst_sound").getAsString() : "minecraft:entity.firework_rocket.large_blast";
            float burstSoundVolume = json.has("burst_sound_volume") ?
                    json.get("burst_sound_volume").getAsFloat() : 1.0f;
            float burstSoundPitch = json.has("burst_sound_pitch") ?
                    json.get("burst_sound_pitch").getAsFloat() : 1.0f;

            return new FlareData(raidId, burstDelay, duration, trailParticles, burstParticles,
                    pattern, burstSound, burstSoundVolume, burstSoundPitch);

        } catch (Exception e) {
            LOGGER.error("Error parsing flare data for: {}", flareId, e);
            return null;
        }
    }

    private static List<ParticleEffect> parseParticleEffects(JsonObject json, String key) {
        List<ParticleEffect> effects = new ArrayList<>();

        if (json.has(key)) {
            JsonArray effectsArray = json.getAsJsonArray(key);
            for (JsonElement element : effectsArray) {
                JsonObject effectObj = element.getAsJsonObject();

                String particleType = effectObj.get("particle").getAsString();
                int count = effectObj.has("count") ? effectObj.get("count").getAsInt() : 10;
                double spread = effectObj.has("spread") ? effectObj.get("spread").getAsDouble() : 0.3;
                double speed = effectObj.has("speed") ? effectObj.get("speed").getAsDouble() : 0.1;
                int color = effectObj.has("color") ?
                        Integer.parseInt(effectObj.get("color").getAsString().replace("#", ""), 16) : 0xFFFFFF;

                effects.add(new ParticleEffect(particleType, count, spread, speed, color));
            }
        }

        return effects;
    }

    @Nullable
    private static FlarePattern parsePattern(JsonObject json) {
        String patternType = json.has("type") ? json.get("type").getAsString() : "circle";
        int repetitions = json.has("repetitions") ? json.get("repetitions").getAsInt() : 1;
        double scale = json.has("scale") ? json.get("scale").getAsDouble() : 1.0;

        List<Vec3Data> points = new ArrayList<>();
        if (json.has("points")) {
            JsonArray pointsArray = json.getAsJsonArray("points");
            for (JsonElement element : pointsArray) {
                JsonObject pointObj = element.getAsJsonObject();
                points.add(new Vec3Data(
                        pointObj.get("x").getAsDouble(),
                        pointObj.get("y").getAsDouble(),
                        pointObj.get("z").getAsDouble()
                ));
            }
        }

        return new FlarePattern(patternType, points, repetitions, scale);
    }

    @Nullable
    public static FlareData getFlareData(String raidId) {
        return FLARES.get(raidId);
    }

    public static boolean hasFlareForRaid(String raidId) {
        return FLARES.containsKey(raidId);
    }

    public static Set<String> getAllRaidIds() {
        return FLARES.keySet();
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
                loadFlareConfigs(resourceManager);
            }
        });
    }
}