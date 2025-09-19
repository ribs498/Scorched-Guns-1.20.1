package top.ribs.scguns.common.exosuit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class ExoSuitUpgradeManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static ExoSuitUpgradeManager instance;

    private static final Map<ResourceLocation, ExoSuitUpgrade> itemUpgrades = new HashMap<>();

    public ExoSuitUpgradeManager() {
        super(GSON, "upgrades");
        instance = this;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        itemUpgrades.clear();

        pObject.forEach((resourceLocation, jsonElement) -> {
            try {
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    ResourceLocation itemId = getItemIdFromJson(jsonObject, resourceLocation);
                    if (ForgeRegistries.ITEMS.containsKey(itemId)) {
                        ExoSuitUpgrade upgrade = loadUpgradeFromJson(jsonObject);
                        if (upgrade != null) {
                            itemUpgrades.put(itemId, upgrade);
                        }
                    } else {
                        ForgeRegistries.ITEMS.getKeys().stream()
                                .filter(key -> key.toString().contains("heavy_armor_plate"))
                                .forEach(key -> System.err.println("  - " + key));
                    }
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private ResourceLocation getItemIdFromJson(JsonObject json, ResourceLocation fileLocation) {

        if (json.has("item")) {
            String itemStr = json.get("item").getAsString();
            return new ResourceLocation(itemStr);
        }
        String path = fileLocation.getPath();
        if (path.contains("/")) {
            String[] parts = path.split("/");
            if (parts.length >= 2) {
                String namespace = parts[parts.length - 2];
                String itemName = parts[parts.length - 1];
                ResourceLocation result = new ResourceLocation(namespace, itemName);
                return result;
            }
        }
        return new ResourceLocation(fileLocation.getNamespace(), fileLocation.getPath());
    }

    private ExoSuitUpgrade loadUpgradeFromJson(JsonObject json) {
        ExoSuitUpgrade upgrade = new ExoSuitUpgrade();

        try {
            if (json.has("type")) {
                upgrade.type = json.get("type").getAsString();
            }

            if (json.has("effects")) {
                JsonObject effects = json.getAsJsonObject("effects");
                ExoSuitUpgrade.Effects effectsData = upgrade.getEffects();

                if (effects.has("armorBonus")) {
                    effectsData.armorBonus = effects.get("armorBonus").getAsFloat();
                }
                if (effects.has("armorToughness")) {
                    effectsData.armorToughness = effects.get("armorToughness").getAsFloat();
                }
                if (effects.has("knockbackResistance")) {
                    effectsData.knockbackResistance = effects.get("knockbackResistance").getAsFloat();
                }
                if (effects.has("speedModifier")) {
                    effectsData.speedModifier = effects.get("speedModifier").getAsFloat();
                }
                if (effects.has("jumpBoost")) {
                    effectsData.jumpBoost = effects.get("jumpBoost").getAsFloat();
                }
                if (effects.has("fallDamageReduction")) {
                    effectsData.fallDamageReduction = effects.get("fallDamageReduction").getAsFloat();
                }
                if (effects.has("nightVision")) {
                    effectsData.nightVision = effects.get("nightVision").getAsBoolean();
                }
                if (effects.has("flight")) {
                    effectsData.flight = effects.get("flight").getAsBoolean();
                }
                if (effects.has("flightSpeed")) {
                    effectsData.flightSpeed = effects.get("flightSpeed").getAsFloat();
                }
                if (effects.has("energyUse")) {
                    effectsData.energyUse = effects.get("energyUse").getAsFloat();
                }
                if (effects.has("recoilAngleReduction")) {
                    effectsData.recoilAngleReduction = effects.get("recoilAngleReduction").getAsFloat();
                }
                if (effects.has("recoilKickReduction")) {
                    effectsData.recoilKickReduction = effects.get("recoilKickReduction").getAsFloat();
                }
                if (effects.has("spreadReduction")) {
                    effectsData.spreadReduction = effects.get("spreadReduction").getAsFloat();
                }
            }

            if (json.has("display")) {
                JsonObject display = json.getAsJsonObject("display");
                ExoSuitUpgrade.Display displayData = upgrade.getDisplay();

                if (display.has("model")) {
                    displayData.model = display.get("model").getAsString();
                }
                if (display.has("storageSize")) {
                    displayData.storageSize = display.get("storageSize").getAsInt();
                }
                if (display.has("gridWidth")) {
                    displayData.gridWidth = display.get("gridWidth").getAsInt();
                }
                if (display.has("gridHeight")) {
                    displayData.gridHeight = display.get("gridHeight").getAsInt();
                }
                if (display.has("containerType")) {
                    displayData.containerType = display.get("containerType").getAsString();
                }
            }

            return upgrade;

        } catch (Exception e) {;
            e.printStackTrace();
            return null;
        }
    }

    public static ExoSuitUpgrade getUpgradeForItem(Item item) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        return itemUpgrades.get(itemId);
    }

    public static ExoSuitUpgrade getUpgradeForItem(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return getUpgradeForItem(stack.getItem());
    }

    public static ExoSuitUpgrade getUpgradeForItemInSlot(ItemStack stack, String slotType) {
        if (stack.isEmpty()) return null;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String slotSpecificKey = itemId.toString() + "_" + slotType;
        ResourceLocation slotSpecificId = new ResourceLocation(slotSpecificKey);

        ExoSuitUpgrade slotSpecific = itemUpgrades.get(slotSpecificId);
        if (slotSpecific != null) {
            return slotSpecific;
        }

        return itemUpgrades.get(itemId);
    }

    public static boolean isUpgradeItem(Item item) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        return itemUpgrades.containsKey(itemId);
    }

    public static boolean isUpgradeItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return isUpgradeItem(stack.getItem());
    }

    public static ExoSuitUpgradeManager getInstance() {
        return instance;
    }
}