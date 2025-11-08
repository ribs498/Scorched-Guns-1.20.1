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

        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "m22_waltz"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "waltz_conversion"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "osgood_50"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "grandle_og"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "grandle"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "cogloader"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "gale"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "jr_wristbreaker"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "jackhammer"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "howler"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "howler_conversion"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "gauss_rifle"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "libertas"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "niami"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "spitfire"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "gattaler"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "thunderhead"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "scratches"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "hammer_gl"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "cr4k_mining_laser"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "dozier_rl"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "treated_brass_blueprint"), Constants.TREATED_BRASS);

        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "krauser"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "soul_drummer"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "uppercut"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "micina"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "valora"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "prush_gun"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "drill"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "drill_conversion"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "lockewood"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "zilk_45"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "rg_jigsaw"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "nailer"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "inertial"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "mas_55"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "mas_peddler"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "minksy"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "inquisitor"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "plasgun"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "truant"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "cyclone"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "shard_culler"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "diamond_steel_blueprint"), Constants.DIAMOND_STEEL);

        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "hyperbaria"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "floundergat"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "marlin"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "bomb_lance"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "ocean_blueprint"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "sequoia"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "hullbreaker"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "spirulida"), Constants.OCEANIC);

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
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "sterilizer"), Constants.SCORCHED);


        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "advanced_exo_suit_core"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "the_pact"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "music_disc_mass_destruction"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "music_disc_mass_destruction_extended"), Constants.SCORCHED);



        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "felix_memorial"), Constants.PIGLISH);

        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "kiln_gun"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "ultra_knight_hawk"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "whizzbanger"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "big_bore"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "osborne_slug"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "ribs_glory"), Constants.PIGLISH);

        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "rusty_medal"), Constants.RUSTY);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "labor_trophy"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "snapped_cogwheel"), Constants.WRECKER);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "ceremonial_cod"), Constants.DIAMOND_STEEL);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "cog_heart"), Constants.TREATED_BRASS);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "gold_idol"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "leviathan_tooth"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "sculk_tome"), Constants.DEEP_DARK);
        ITEM_RARITY_MAP.put(new ResourceLocation("scguns", "shulker_core"), Constants.ENDISH);


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
