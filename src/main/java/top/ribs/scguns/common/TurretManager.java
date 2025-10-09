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
public class TurretManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<ResourceLocation, Turret> TURRET_CONFIGS = new HashMap<>();
    private static final String TURRET_FOLDER = "turrets";

    @Nullable
    public static Turret getTurret(ResourceLocation id) {
        return TURRET_CONFIGS.get(id);
    }

    public static void loadTurretConfig(ResourceManager resourceManager, ResourceLocation turretId) {
        ResourceLocation location = new ResourceLocation(
                turretId.getNamespace(),
                TURRET_FOLDER + "/" + turretId.getPath() + ".json"
        );

        try {
            Resource resource = resourceManager.getResource(location).orElse(null);
            if (resource != null) {
                try (InputStream stream = resource.open();
                     InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    Turret turret = parseTurret(json);
                    if (turret != null) {
                        TURRET_CONFIGS.put(turretId, turret);
                        LOGGER.info("Successfully loaded turret config: {}", turretId);
                    }
                }
            } else {
                LOGGER.warn("No JSON found for turret: {}", turretId);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load turret config: {}", location, e);
        }
    }

    private static Turret parseTurret(JsonObject json) {
        try {
            Turret turret = new Turret();

            if (json.has("targeting")) {
                parseTargeting(json.getAsJsonObject("targeting"), turret.getTargeting());
            }
            if (json.has("combat")) {
                parseCombat(json.getAsJsonObject("combat"), turret.getCombat());
            }
            if (json.has("ammunition")) {
                parseAmmunition(json.getAsJsonObject("ammunition"), turret.getAmmunition());
            }
            if (json.has("behavior")) {
                parseBehavior(json.getAsJsonObject("behavior"), turret.getBehavior());
            }
            if (json.has("display")) {
                parseDisplay(json.getAsJsonObject("display"), turret.getDisplay());
            }

            return turret;
        } catch (Exception e) {
            LOGGER.error("Error parsing turret config", e);
            return null;
        }
    }

    private static void parseTargeting(JsonObject json, Turret.Targeting targeting) {
        if (json.has("range")) {
            targeting.setRange(json.get("range").getAsDouble());
        }
        if (json.has("verticalRange")) {
            targeting.setVerticalRange(json.get("verticalRange").getAsDouble());
        }
        if (json.has("minFiringDistance")) {
            targeting.setMinFiringDistance(json.get("minFiringDistance").getAsDouble());
        }
        if (json.has("rotationSpeed")) {
            targeting.setRotationSpeed(json.get("rotationSpeed").getAsFloat());
        }
        if (json.has("positionSmoothing")) {
            targeting.setPositionSmoothing(json.get("positionSmoothing").getAsFloat());
        }
        if (json.has("maxPitch")) {
            targeting.setMaxPitch(json.get("maxPitch").getAsFloat());
        }
        if (json.has("minPitch")) {
            targeting.setMinPitch(json.get("minPitch").getAsFloat());
        }
        if (json.has("predictionMultiplier")) {
            targeting.setPredictionMultiplier(json.get("predictionMultiplier").getAsInt());
        }
        if (json.has("requiresLineOfSight")) {
            targeting.setRequiresLineOfSight(json.get("requiresLineOfSight").getAsBoolean());
        }
    }

    private static void parseCombat(JsonObject json, Turret.Combat combat) {
        if (json.has("cooldown")) {
            combat.setCooldown(json.get("cooldown").getAsInt());
        }
        if (json.has("inaccuracy")) {
            combat.setInaccuracy(json.get("inaccuracy").getAsFloat());
        }
        if (json.has("pelletCount")) {
            combat.setPelletCount(json.get("pelletCount").getAsInt());
        }
        if (json.has("spreadAngle")) {
            combat.setSpreadAngle(json.get("spreadAngle").getAsFloat());
        }
        if (json.has("recoilMax")) {
            combat.setRecoilMax(json.get("recoilMax").getAsFloat());
        }
        if (json.has("recoilSpeed")) {
            combat.setRecoilSpeed(json.get("recoilSpeed").getAsFloat());
        }
        if (json.has("damageModifier")) {
            combat.setDamageModifier(json.get("damageModifier").getAsInt());
        }
        if (json.has("projectileSpeed")) {
            combat.setProjectileSpeed(json.get("projectileSpeed").getAsDouble());
        }
        if (json.has("fireSound")) {
            combat.setFireSound(new ResourceLocation(json.get("fireSound").getAsString()));
        }
    }

    private static void parseAmmunition(JsonObject json, Turret.Ammunition ammunition) {
        if (json.has("acceptedAmmo")) {
            ammunition.clearAcceptedAmmo();
            var ammoArray = json.getAsJsonArray("acceptedAmmo");
            for (var element : ammoArray) {
                JsonObject ammoObj = element.getAsJsonObject();
                Turret.Ammunition.AmmoType ammo = new Turret.Ammunition.AmmoType();

                if (ammoObj.has("item")) {
                    ammo.setItem(new ResourceLocation(ammoObj.get("item").getAsString()));
                }
                if (ammoObj.has("bulletType")) {
                    ammo.setBulletType(new ResourceLocation(ammoObj.get("bulletType").getAsString()));
                }
                if (ammoObj.has("casingType")) {
                    ammo.setCasingType(new ResourceLocation(ammoObj.get("casingType").getAsString()));
                }
                if (ammoObj.has("damage")) {
                    ammo.setDamage(ammoObj.get("damage").getAsDouble());
                }

                ammunition.addAmmoType(ammo);
            }
        }

        if (json.has("casingEjectChance")) {
            ammunition.setCasingEjectChance(json.get("casingEjectChance").getAsFloat());
        }
    }

    private static void parseBehavior(JsonObject json, Turret.Behavior behavior) {
        if (json.has("restingYaw")) {
            behavior.setRestingYaw(json.get("restingYaw").getAsFloat());
        }
        if (json.has("restingPitch")) {
            behavior.setRestingPitch(json.get("restingPitch").getAsFloat());
        }
        if (json.has("disableTime")) {
            behavior.setDisableTime(json.get("disableTime").getAsInt());
        }
        if (json.has("hasOpenAnimation")) {
            behavior.setHasOpenAnimation(json.get("hasOpenAnimation").getAsBoolean());
        }
    }

    private static void parseDisplay(JsonObject json, Turret.Display display) {
        if (json.has("muzzleLength")) {
            display.setMuzzleLength(json.get("muzzleLength").getAsDouble());
        }
        if (json.has("muzzleOffsetY")) {
            display.setMuzzleOffsetY(json.get("muzzleOffsetY").getAsDouble());
        }
    }

    public static void clearAll() {
        TURRET_CONFIGS.clear();
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                clearAll();
                loadTurretConfig(resourceManager, new ResourceLocation(Reference.MOD_ID, "auto_turret"));
                loadTurretConfig(resourceManager, new ResourceLocation(Reference.MOD_ID, "basic_turret"));
                loadTurretConfig(resourceManager, new ResourceLocation(Reference.MOD_ID, "shotgun_turret"));
                loadTurretConfig(resourceManager, new ResourceLocation(Reference.MOD_ID, "sniper_turret"));
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                LOGGER.info("Loaded {} turret configurations", TURRET_CONFIGS.size());
            }
        });
    }
}