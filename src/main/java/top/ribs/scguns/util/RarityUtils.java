package top.ribs.scguns.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;


public class RarityUtils {

    private static final Map<ResourceLocation, Rarity> ITEM_RARITY_MAP = new HashMap<>();

    static {
        // Add your custom items and their rarities here
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "floundergat"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "marlin"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "bomb_lance"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "ocean_blueprint"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "sequoia"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "hullbreaker"), Constants.OCEANIC);

        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "super_shotgun"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "blasphemy"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "freyr"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "pyroclastic_flow"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "vulcanic_repeater"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "piglin_blueprint"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "mangalitsa"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "trotters"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "hog_round"), Constants.PIGLISH);

        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "whispers"), Constants.DEEP_DARK);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "echoes_2"), Constants.DEEP_DARK);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "sculk_resonator"), Constants.DEEP_DARK);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "deep_dark_blueprint"), Constants.DEEP_DARK);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "forlorn_hope"), Constants.DEEP_DARK);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "sculk_cell"), Constants.DEEP_DARK);

        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "end_blueprint"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "lone_wonder"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "raygun"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "dark_matter"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "shellurker"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "carapice"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "weevil"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "shulkshot"), Constants.ENDISH);

        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "scorched_blueprint"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "scorched_ingot"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "earths_corpse"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "rat_king_and_queen"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "locust"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "newborn_cyst"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "astella"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "flayed_god"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "nervepinch"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "prima_materia"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "terra_incognita"), Constants.SCORCHED);

        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "advanced_exo_suit_core"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "the_pact"), Constants.SCORCHED);

        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "felix_memorial"), Constants.PIGLISH);

        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "ultra_knight_hawk"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "big_bore"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "osborne_slug"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "ribs_glory"), Constants.PIGLISH);

    }



    public static Rarity GetRarityFromResourceLocation(ResourceLocation location, Rarity oldRarity) {
        return ITEM_RARITY_MAP.getOrDefault(location, oldRarity);
    }

    public static Rarity GetRarityFromItem(Item item, Rarity old) {
        var items = ForgeRegistries.ITEMS;
        if (items.containsValue(item)) {
            return GetRarityFromResourceLocation(items.getKey(item), old);
        }
        return old;
    }
}
