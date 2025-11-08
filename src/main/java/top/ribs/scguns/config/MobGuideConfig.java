package top.ribs.scguns.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
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
public class MobGuideConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<EntityType<?>, MobGuide> GUIDES = new HashMap<>();

    private static final List<String> GUIDE_FILES = Arrays.asList(
            "viventrum",
            "supply_scamp"
    );

    public record GuidePage(String type, String text, @Nullable String image) {
        public Component getTextComponent() {
            return Component.translatable(text);
        }

        public static GuidePage fromJson(JsonObject json) {
            String type = json.has("type") ? json.get("type").getAsString() : "text";
            String text = json.get("text").getAsString();
            String image = json.has("image") ? json.get("image").getAsString() : null;

            return new GuidePage(type, text, image);
        }
    }

    public record MobGuide(String id, String titleKey, List<GuidePage> pages) {

        public Component getTitle() {
            return Component.translatable(titleKey);
        }

        public int getPageCount() {
            return pages.size();
        }

        public GuidePage getPage(int index) {
            if (index < 0 || index >= pages.size()) {
                return null;
            }
            return pages.get(index);
        }

        public Iterable<? extends GuidePage> getPages() {
            return pages;
        }
    }

    public static void loadConfig(ResourceManager resourceManager) {
        GUIDES.clear();

        int loadedCount = 0;

        for (String guideName : GUIDE_FILES) {
            ResourceLocation location = new ResourceLocation("scguns", "guides/" + guideName + ".json");

            try {
                Resource resource = resourceManager.getResource(location).orElse(null);
                if (resource != null) {
                    try (InputStreamReader reader = new InputStreamReader(
                            resource.open(), StandardCharsets.UTF_8)) {

                        Gson gson = new Gson();
                        JsonObject json = gson.fromJson(reader, JsonObject.class);

                        if (json != null) {
                            String id = json.get("id").getAsString();
                            String titleKey = json.get("title").getAsString();

                            List<GuidePage> pages = new ArrayList<>();
                            if (json.has("pages")) {
                                JsonArray pagesArray = json.getAsJsonArray("pages");
                                for (JsonElement pageElement : pagesArray) {
                                    GuidePage page = GuidePage.fromJson(pageElement.getAsJsonObject());
                                    pages.add(page);
                                }
                            }

                            MobGuide guide = new MobGuide(id, titleKey, pages);

                            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(
                                    new ResourceLocation(id));

                            if (entityType != null) {
                                GUIDES.put(entityType, guide);
                                loadedCount++;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load mob guide: {}", guideName, e);
            }
        }
    }

    @Nullable
    public static MobGuide getGuide(EntityType<?> entityType) {
        return GUIDES.get(entityType);
    }

    public static boolean hasGuide(EntityType<?> entityType) {
        return GUIDES.containsKey(entityType);
    }

    public static Collection<MobGuide> getAllGuides() {
        return GUIDES.values();
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