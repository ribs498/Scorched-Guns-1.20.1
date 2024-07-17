package top.ribs.scguns.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.Attachments;
import top.ribs.scguns.common.GunModifiers;
import top.ribs.scguns.item.*;
import top.ribs.scguns.item.ammo_boxes.*;

import top.ribs.scguns.item.attachment.impl.Barrel;
import top.ribs.scguns.item.attachment.impl.Magazine;
import top.ribs.scguns.item.attachment.impl.Stock;
import top.ribs.scguns.item.attachment.impl.UnderBarrel;
import vectorwing.farmersdelight.common.item.KnifeItem;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);

    public static final RegistryObject<Item> PISTOL_AMMO_BOX = REGISTER.register("pistol_ammo_box", () -> new PistolAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RIFLE_AMMO_BOX = REGISTER.register("rifle_ammo_box", () -> new RifleAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item>SHOTGUN_AMMO_BOX = REGISTER.register("shotgun_ammo_box", () -> new ShotgunAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item>MAGNUM_AMMO_BOX = REGISTER.register("magnum_ammo_box", () -> new MagnumAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item>ROCKET_AMMO_BOX = REGISTER.register("rocket_ammo_box", () -> new RocketAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item>SPECIAL_AMMO_BOX = REGISTER.register("special_ammo_box", () -> new SpecialAmmoBoxItem(new Item.Properties().stacksTo(1)));

    ////BLACK POWDER
    public static final RegistryObject<GunItem> FLINTLOCK_PISTOL = REGISTER.register("flintlock_pistol", () -> new NonUnderwaterGunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> HANDCANNON = REGISTER.register("handcannon", () -> new NonUnderwaterGunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> MUSKET = REGISTER.register("musket", () -> new NonUnderwaterGunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> BLUNDERBUSS = REGISTER.register("blunderbuss", () -> new NonUnderwaterGunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> REPEATING_MUSKET = REGISTER.register("repeating_musket", () -> new NonUnderwaterGunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> FLOUNDERGAT = REGISTER.register("floundergat", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<GunItem> LASER_MUSKET = REGISTER.register("laser_musket", () -> new GunItem(new Item.Properties().stacksTo(1).durability(256)));
    ///COPPER
    public static final RegistryObject<GunItem> BOOMSTICK = REGISTER.register("boomstick", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> SCRAPPER = REGISTER.register("scrapper", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> MAKESHIFT_RIFLE = REGISTER.register("makeshift_rifle", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> RUSTY_GNAT = REGISTER.register("rusty_gnat", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> BRUISER = REGISTER.register("bruiser", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> LLR_DIRECTOR = REGISTER.register("llr_director", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> M3_CARABINE = REGISTER.register("m3_carabine", () -> new GunItem(new Item.Properties().stacksTo(1).durability(256)));
    public static final RegistryObject<GunItem> GREASER_SMG = REGISTER.register("greaser_smg", () -> new GunItem(new Item.Properties().stacksTo(1).durability(256)));
    public static final RegistryObject<GunItem> IRON_SPEAR = REGISTER.register("iron_spear", () -> new GunItem(new Item.Properties().stacksTo(1).durability(256)));
    public static final RegistryObject<GunItem> DEFENDER_PISTOL = REGISTER.register("defender_pistol", () -> new GunItem(new Item.Properties().stacksTo(1).durability(256)));
    public static final RegistryObject<GunItem> COMBAT_SHOTGUN = REGISTER.register("combat_shotgun", () -> new GunItem(new Item.Properties().stacksTo(1).durability(256)));
    public static final RegistryObject<GunItem> AUVTOMAG = REGISTER.register("auvtomag", () -> new GunItem(new Item.Properties().stacksTo(1).durability(256)));
    public static final RegistryObject<GunItem> GYROJET_PISTOL = REGISTER.register("gyrojet_pistol", () -> new GunItem(new Item.Properties().stacksTo(1).durability(256)));
    public static final RegistryObject<GunItem> ROCKET_RIFLE = REGISTER.register("rocket_rifle", () -> new GunItem(new Item.Properties().stacksTo(1).durability(256)));
    public static final RegistryObject<GunItem> MARLIN = REGISTER.register("marlin", () -> new UnderwaterGunItem(new Item.Properties().stacksTo(1).durability(256)));

    public static final RegistryObject<GunItem> PRUSH_GUN = REGISTER.register("prush_gun", () -> new DiamondSteelGunItem(new Item.Properties().stacksTo(1).durability(700)));
    public static final RegistryObject<GunItem> INERTIAL = REGISTER.register("inertial", () -> new DiamondSteelGunItem(new Item.Properties().stacksTo(1).durability(700)));
    public static final RegistryObject<GunItem> COGLOADER = REGISTER.register("cogloader", () -> new GunItem(new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<GunItem> PLASGUN = REGISTER.register("plasgun", () -> new DiamondSteelGunItem(new Item.Properties().stacksTo(1).durability(700)));
    public static final RegistryObject<GunItem> GAUSS_RIFLE = REGISTER.register("gauss_rifle", () -> new GunItem(new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<GunItem> MAS_55 = REGISTER.register("mas_55", () -> new DiamondSteelGunItem(new Item.Properties().stacksTo(1).durability(700)));
    public static final RegistryObject<GunItem> DOZIER_RL = REGISTER.register("dozier_rl", () -> new GunItem(new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<GunItem> SPITFIRE = REGISTER.register("spitfire", () -> new NonUnderwaterGunItem(new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<GunItem> CYCLONE = REGISTER.register("cyclone", () -> new DiamondSteelGunItem(new Item.Properties().stacksTo(1).durability(700)));

    public static final RegistryObject<GunItem> OSGOOD_50 = REGISTER.register("osgood_50", () -> new GunItem(new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<GunItem> JACKHAMMER = REGISTER.register("jackhammer", () -> new GunItem(new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<GunItem> M22_WALTZ = REGISTER.register("m22_waltz", () -> new GunItem(new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<GunItem> HOWLER = REGISTER.register("howler", () -> new GunItem(new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<GunItem> HOWLER_CONVERSION = REGISTER.register("howler_conversion", () -> new GunItem(new Item.Properties().stacksTo(1).durability(700)));
    public static final RegistryObject<GunItem> LOCKEWOOD = REGISTER.register("lockewood", () -> new DiamondSteelGunItem(new Item.Properties().stacksTo(1).durability(700)));
    public static final RegistryObject<GunItem> ASTELLA = REGISTER.register("astella", () -> new ScorchedWeapon(new Item.Properties().stacksTo(1).durability(700)));

    public static final RegistryObject<GunItem> KRAUSER = REGISTER.register("krauser", () -> new DiamondSteelGunItem(new Item.Properties().stacksTo(1).durability(700)));

    public static final RegistryObject<GunItem> UPPERCUT = REGISTER.register("uppercut", () -> new DiamondSteelGunItem(new Item.Properties().stacksTo(1).durability(700)));
    public static final RegistryObject<GunItem> GATTALER = REGISTER.register("gattaler", () -> new GunItem(new Item.Properties().stacksTo(1).durability(800)));

    public static final RegistryObject<GunItem> RAYGUN = REGISTER.register("raygun", () -> new GunItem(new Item.Properties().stacksTo(1).durability(1500).rarity(Rarity.EPIC)));
    public static final RegistryObject<GunItem> SUPER_SHOTGUN = REGISTER.register("super_shotgun", () -> new GunItem(new Item.Properties().stacksTo(1).durability(500).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<GunItem> BLASPHEMY = REGISTER.register("blasphemy", () -> new NonUnderwaterGunItem(new Item.Properties().stacksTo(1).durability(500).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<GunItem> PYROCLASTIC_FLOW = REGISTER.register("pyroclastic_flow", () -> new GunItem(new Item.Properties().stacksTo(1).durability(500).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<GunItem> FREYR = REGISTER.register("freyr", () -> new GunItem(new Item.Properties().stacksTo(1).durability(500).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<GunItem> VULCANIC_REPEATER = REGISTER.register("vulcanic_repeater", () -> new GunItem(new Item.Properties().stacksTo(1).durability(500).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<GunItem> BOMB_LANCE = REGISTER.register("bomb_lance", () -> new UnderwaterGunItem(new Item.Properties().stacksTo(1).durability(256).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<GunItem> EARTHS_CORPSE = REGISTER.register("earths_corpse", () -> new ScorchedWeapon(new Item.Properties().stacksTo(1).durability(1500).rarity(Rarity.UNCOMMON)));
  public static final RegistryObject<GunItem> RAT_KING_AND_QUEEN = REGISTER.register("rat_king_and_queen", () -> new DualWieldGunItem(new Item.Properties().stacksTo(1).durability(1500).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<GunItem> LOCUST = REGISTER.register("locust", () -> new ScorchedWeapon(new Item.Properties().stacksTo(1).durability(1500).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<GunItem> NEWBORN_CYST = REGISTER.register("newborn_cyst", () -> new ScorchedWeapon(new Item.Properties().stacksTo(1).durability(1500).rarity(Rarity.UNCOMMON)));


    public static final RegistryObject<PickaxeItem> ANTHRALITE_PICKAXE = REGISTER.register("anthralite_pickaxe", () -> new PickaxeItem(ModTiers.ANTHRALITE, 1, -2.8F, new Item.Properties()));
    public static final RegistryObject<SwordItem> ANTHRALITE_SWORD = REGISTER.register("anthralite_sword", () -> new SwordItem(ModTiers.ANTHRALITE, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<AxeItem> ANTHRALITE_AXE = REGISTER.register("anthralite_axe", () -> new AxeItem(ModTiers.ANTHRALITE, 5, -3.0F, new Item.Properties()));
    public static final RegistryObject<ShovelItem> ANTHRALITE_SHOVEL = REGISTER.register("anthralite_shovel", () -> new ShovelItem(ModTiers.ANTHRALITE, 1.5F, -3.0F, new Item.Properties()));
    public static final RegistryObject<HoeItem> ANTHRALITE_HOE = REGISTER.register("anthralite_hoe", () -> new HoeItem(ModTiers.ANTHRALITE, -3, -3.0F, new Item.Properties()));
    public static RegistryObject<Item> ANTHRALITE_KNIFE;

    public static void registerItems() {
        if (ScorchedGuns.farmersDelightLoaded) {
            ANTHRALITE_KNIFE = REGISTER.register("anthralite_knife", () -> {
                try {
                    Class<?> knifeItemClass = Class.forName("vectorwing.farmersdelight.common.item.KnifeItem");
                    Constructor<?> constructor = knifeItemClass.getConstructor(Tier.class, float.class, float.class, Item.Properties.class);
                    return (Item) constructor.newInstance(ModTiers.ANTHRALITE, 0.5F, -2.0F, new Item.Properties());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create ANTHRALITE_KNIFE", e);
                }
            });
        }
    }
    public static final RegistryObject<Item> ANTHRALITE_HELMET = REGISTER.register("anthralite_helmet", () -> new ArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_CHESTPLATE = REGISTER.register("anthralite_chestplate", () -> new ArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_LEGGINGS = REGISTER.register("anthralite_leggings", () -> new ArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_BOOTS = REGISTER.register("anthralite_boots", () -> new ArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> BRASS_MASK = REGISTER.register("brass_mask", () -> new BrassMaskItem(new Item.Properties().stacksTo(1).durability(128), 3, 2.0F, 128, 15, 0.1F));

    public static final RegistryObject<Item> COPPER_BLUEPRINT = REGISTER.register("copper_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> IRON_BLUEPRINT = REGISTER.register("iron_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TREATED_BRASS_BLUEPRINT = REGISTER.register("treated_brass_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DIAMOND_STEEL_BLUEPRINT = REGISTER.register("diamond_steel_blueprint", () -> new BlueprintItem(new Item.Properties()));
    public static final RegistryObject<Item> PIGLIN_BLUEPRINT = REGISTER.register("piglin_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> OCEAN_BLUEPRINT = REGISTER.register("ocean_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> DEEP_DARK_BLUEPRINT = REGISTER.register("deep_dark_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> END_BLUEPRINT = REGISTER.register("end_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> SCORCHED_BLUEPRINT = REGISTER.register("scorched_blueprint", () -> new GlintedBlueprintItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> NITRO_POWDER = REGISTER.register("nitro_powder", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> NITER_DUST = REGISTER.register("niter_dust", () -> new NiterDustItem(new Item.Properties()));
    public static final RegistryObject<Item> SHEOL = REGISTER.register("sheol", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> VEHEMENT_COAL = REGISTER.register("vehement_coal", () -> new FuelItem(new Item.Properties(), 4800));
    public static final RegistryObject<Item> SHEOL_DUST = REGISTER.register("sheol_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SULFUR_CHUNK = REGISTER.register("sulfur_chunk", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BUCKSHOT = REGISTER.register("buckshot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GUNPOWDER_DUST = REGISTER.register("gunpowder_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_ANTHRALITE = REGISTER.register("raw_anthralite", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CRUSHED_RAW_ANTHRALITE = REGISTER.register("crushed_raw_anthralite", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_DUST = REGISTER.register("anthralite_dust", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ANTHRALITE_INGOT = REGISTER.register("anthralite_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_NUGGET = REGISTER.register("anthralite_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANCIENT_BRASS= REGISTER.register("ancient_brass", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TREATED_BRASS_BLEND = REGISTER.register("treated_brass_blend", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TREATED_BRASS_INGOT = REGISTER.register("treated_brass_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_STEEL_BLEND = REGISTER.register("diamond_steel_blend", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_STEEL_INGOT = REGISTER.register("diamond_steel_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SCORCHED_BLEND = REGISTER.register("scorched_blend", () -> new ScorchedItem(new Item.Properties()));
    public static final RegistryObject<Item> SCORCHED_INGOT = REGISTER.register("scorched_ingot", () -> new ScorchedItem(new Item.Properties()));
    public static final RegistryObject<Item> PLASMA_CORE = REGISTER.register("plasma_core", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NETHER_STAR_FRAGMENT = REGISTER.register("nether_star_fragment", () -> new NetherStarFragmentItem(new Item.Properties()));
    public static final RegistryObject<Item> EMPTY_BLASPHEMY = REGISTER.register("empty_blasphemy", () -> new EmptyBlasphemyItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GUN_GRIP = REGISTER.register("gun_grip", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GUN_BARREL = REGISTER.register("gun_barrel", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEAVY_GUN_BARREL = REGISTER.register("heavy_gun_barrel", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STONE_GUN_BARREL = REGISTER.register("stone_gun_barrel", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GUN_MAGAZINE = REGISTER.register("gun_magazine", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GUN_PARTS = REGISTER.register("gun_parts", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEAVY_GUN_PARTS = REGISTER.register("heavy_gun_parts", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FIRING_UNIT = REGISTER.register("firing_unit", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAPID_FIRING_UNIT = REGISTER.register("rapid_firing_unit", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_GUN_FRAME = REGISTER.register("copper_gun_frame", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SCORCHED_GUN_FRAME = REGISTER.register("scorched_gun_frame", () -> new ScorchedItem(new Item.Properties()));

    public static final RegistryObject<Item> IRON_GUN_FRAME = REGISTER.register("iron_gun_frame", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TREATED_BRASS_GUN_FRAME = REGISTER.register("treated_brass_gun_frame", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_STEEL_GUN_FRAME = REGISTER.register("diamond_steel_gun_frame", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLANK_MOLD = REGISTER.register("blank_mold", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SMALL_CASING_MOLD = REGISTER.register("small_casing_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(64)));
    public static final RegistryObject<Item> MEDIUM_CASING_MOLD = REGISTER.register("medium_casing_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(64)));
    public static final RegistryObject<Item> LARGE_CASING_MOLD = REGISTER.register("large_casing_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(64)));
    public static final RegistryObject<Item> GUN_PARTS_MOLD = REGISTER.register("gun_parts_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(16)));

    public static final RegistryObject<Item> SMALL_COPPER_CASING = REGISTER.register("small_copper_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_COPPER_CASING = REGISTER.register("medium_copper_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SMALL_IRON_CASING = REGISTER.register("small_iron_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LARGE_IRON_CASING = REGISTER.register("large_iron_casing", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SMALL_DIAMOND_STEEL_CASING = REGISTER.register("small_diamond_steel_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_DIAMOND_STEEL_CASING = REGISTER.register("medium_diamond_steel_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SMALL_BRASS_CASING = REGISTER.register("small_brass_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_BRASS_CASING = REGISTER.register("medium_brass_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LARGE_BRASS_CASING = REGISTER.register("large_brass_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> POWDER_AND_BALL= REGISTER.register("powder_and_ball", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> GRAPESHOT= REGISTER.register("grapeshot", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> COMPACT_COPPER_ROUND = REGISTER.register("compact_copper_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> HOG_ROUND = REGISTER.register("hog_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> STANDARD_COPPER_ROUND = REGISTER.register("standard_copper_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> COMPACT_ADVANCED_ROUND = REGISTER.register("compact_advanced_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> RAMROD_ROUND = REGISTER.register("ramrod_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_ROUND = REGISTER.register("advanced_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> KRAHG_ROUND = REGISTER.register("krahg_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> BEOWULF_ROUND = REGISTER.register("beowulf_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> GIBBS_ROUND = REGISTER.register("gibbs_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> SHOTGUN_SHELL = REGISTER.register("shotgun_shell", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> BEARPACK_SHELL = REGISTER.register("bearpack_shell", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> BLAZE_FUEL = REGISTER.register("blaze_fuel", () -> new FuelAmmoItem((new Item.Properties()), 2400));
    public static final RegistryObject<Item> ENERGY_CELL = REGISTER.register("energy_cell", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> MICROJET = REGISTER.register("microjet", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> ROCKET = REGISTER.register("rocket", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> OSBORNE_SLUG = REGISTER.register("osborne_slug", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> PEBBLES = REGISTER.register("pebbles", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NETHERITE_SCRAP_CHUNK = REGISTER.register("netherite_scrap_chunk", () -> new Item(new Item.Properties()));


    // Projectiles And Throwables
    public static final RegistryObject<Item> GRENADE = REGISTER.register("grenade", () -> new GrenadeItem(new Item.Properties().stacksTo(16), 20 * 4));
    public static final RegistryObject<Item> STUN_GRENADE = REGISTER.register("stun_grenade", () -> new StunGrenadeItem(new Item.Properties().stacksTo(16), 72000));
    public static final RegistryObject<Item> MOLOTOV_COCKTAIL = REGISTER.register("molotov_cocktail", () -> new MolotovCocktailItem(new Item.Properties().stacksTo(16), 72000));
    public static final RegistryObject<Item> CHOKE_BOMB = REGISTER.register("choke_bomb", () -> new ChokeBombItem(new Item.Properties().stacksTo(16), 72000));
    public static final RegistryObject<Item> SWARM_BOMB = REGISTER.register("swarm_bomb", () -> new SwarmBombItem(new Item.Properties().stacksTo(16), 72000));


    // Medical Items
    public static final RegistryObject<Item> BASIC_POULTICE = REGISTER.register("basic_poultice",
            () -> new HealingBandageItem(new Item.Properties().stacksTo(16), 6, (MobEffectInstance) null));

    public static final RegistryObject<Item> HONEY_SULFUR_POULTICE = REGISTER.register("honey_sulfur_poultice",
            () -> new HealingBandageItem(new Item.Properties().stacksTo(16), 9, new MobEffectInstance(MobEffects.REGENERATION, 100, 0)));

    public static final RegistryObject<Item> ENCHANTED_BANDAGE = REGISTER.register("enchanted_bandage", () -> new GlintedHealingBandageItem(new Item.Properties().stacksTo(16), 12, new MobEffectInstance(MobEffects.REGENERATION, 100, 1), new MobEffectInstance(MobEffects.ABSORPTION, 400, 0)));

 public static final RegistryObject<Item> DRAGON_SALVE = REGISTER.register("dragon_salve", () -> new GlintedHealingBandageItem(new Item.Properties().stacksTo(16), 16, new MobEffectInstance(MobEffects.REGENERATION, 200, 1), new MobEffectInstance(MobEffects.ABSORPTION, 600, 0)));

    public static final RegistryObject<Item> COLD_PACK = REGISTER.register("cold_pack", () -> new ColdPackItem(new Item.Properties().stacksTo(16)));

    // Scope Attachments
    public static final RegistryObject<Item> LASER_SIGHT = REGISTER.register("laser_sight", () -> new LaserSightItem(Attachments.LASER_SIGHT, new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<Item> LONG_SCOPE = REGISTER.register("long_scope", () -> new ScopeItem(Attachments.LONG_SCOPE, new Item.Properties().stacksTo(1).durability(1000)));
    public static final RegistryObject<Item> MEDIUM_SCOPE = REGISTER.register("medium_scope", () -> new ScopeItem(Attachments.MEDIUM_SCOPE, new Item.Properties().stacksTo(1).durability(900)));
    public static final RegistryObject<Item> REFLEX_SIGHT = REGISTER.register("reflex_sight", () -> new ScopeItem(Attachments.REFLEX_SIGHT, new Item.Properties().stacksTo(1).durability(800)));
    // Stock Attachments
    public static final RegistryObject<Item> LIGHT_STOCK = REGISTER.register("light_stock", () -> new StockItem(Stock.create(GunModifiers.BETTER_CONTROL), new Item.Properties().stacksTo(1).durability(600), false));

    public static final RegistryObject<Item> WEIGHTED_STOCK = REGISTER.register("weighted_stock", () -> new StockItem(Stock.create(GunModifiers.SUPER_STABILISED), new Item.Properties().stacksTo(1).durability(1000)));
    public static final RegistryObject<Item> WOODEN_STOCK = REGISTER.register("wooden_stock", () -> new StockItem(Stock.create(GunModifiers.BETTER_CONTROL), new Item.Properties().stacksTo(1).durability(600), false));
    // Barrel Attachments
    public static final RegistryObject<Item> SILENCER = REGISTER.register("silencer", () -> new BarrelItem(Barrel.create(0.0F, GunModifiers.SILENCED, GunModifiers.REDUCED_DAMAGE), new Item.Properties().stacksTo(1).durability(500)));

    public static final RegistryObject<Item> ADVANCED_SILENCER = REGISTER.register("advanced_silencer", () -> new BarrelItem(Barrel.create(0.0F, GunModifiers.SILENCED), new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<Item> MUZZLE_BRAKE = REGISTER.register("muzzle_brake", () -> new BarrelItem(Barrel.create(0.0F, GunModifiers.STABILISED), new Item.Properties().stacksTo(1).durability(450)));

    // Under Barrel Attachments
    public static final RegistryObject<Item> LIGHT_GRIP = REGISTER.register("light_grip", () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.LIGHT_RECOIL), new Item.Properties().stacksTo(1).durability(600)));
    public static final RegistryObject<Item> VERTICAL_GRIP = REGISTER.register("vertical_grip", () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.REDUCED_RECOIL), new Item.Properties().stacksTo(1).durability(800)));

    public static final RegistryObject<Item> IRON_BAYONET = REGISTER.register("iron_bayonet", () -> new BayonetItem(UnderBarrel.create(GunModifiers.IRON_BAYONET_DAMAGE), new Item.Properties().stacksTo(1).durability(256), 2.0f, -3.0f));
    public static final RegistryObject<Item> ANTHRALITE_BAYONET = REGISTER.register("anthralite_bayonet", () -> new BayonetItem(UnderBarrel.create(GunModifiers.ANTHRALITE_BAYONET_DAMAGE), new Item.Properties().stacksTo(1).durability(512), 3.0f, -3.0f));
    public static final RegistryObject<Item> DIAMOND_BAYONET = REGISTER.register("diamond_bayonet", () -> new BayonetItem(UnderBarrel.create(GunModifiers.DIAMOND_BAYONET_DAMAGE), new Item.Properties().stacksTo(1).durability(1024), 4.0f, -3.0f));
    public static final RegistryObject<Item> NETHERITE_BAYONET = REGISTER.register("netherite_bayonet", () -> new BayonetItem(UnderBarrel.create(GunModifiers.NETHERITE_BAYONET_DAMAGE), new Item.Properties().stacksTo(1).durability(1500), 5.0f, -3.0f));

    //Magazines
    public static final RegistryObject<Item> EXTENDED_MAG = REGISTER.register("extended_mag", () -> new MagazineItem(Magazine.create(GunModifiers.SLOW_RELOAD, GunModifiers.EXTENDED_MAG), new Item.Properties().stacksTo(1).durability(1000)));
    public static final RegistryObject<Item> SPEED_MAG = REGISTER.register("speed_mag", () -> new MagazineItem(Magazine.create(GunModifiers.FAST_RELOAD, GunModifiers.BETTER_CONTROL), new Item.Properties().stacksTo(1).durability(700)));
    public static final RegistryObject<Item> PLUS_P_MAG = REGISTER.register("plus_p_mag", () -> new MagazineItem(Magazine.create(GunModifiers.INCREASED_DAMAGE, GunModifiers.PLUS_P_MAG), new Item.Properties().stacksTo(1).durability(900)));
//ITEMS
    public static final RegistryObject<Item> REPAIR_KIT = REGISTER.register("repair_kit", () -> new Item(new Item.Properties()));
    // Mobs
    public static final RegistryObject<Item> COG_MINION_SPAWN_EGG = REGISTER.register("cog_minion_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.COG_MINION, 0x76501f, 0x7f8080, new Item.Properties()));
    public static final RegistryObject<Item> COG_KNIGHT_SPAWN_EGG = REGISTER.register("cog_knight_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.COG_KNIGHT, 0xf7cb6c, 0xbf8e55, new Item.Properties()));
    public static final RegistryObject<Item> SKY_CARRIER_SPAWN_EGG = REGISTER.register("sky_carrier_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.SKY_CARRIER, 0xffeb8c, 0x4f4f4f, new Item.Properties()));
    public static final RegistryObject<Item> SUPPLY_SCAMP_SPAWN_EGG = REGISTER.register("supply_scamp_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.SUPPLY_SCAMP, 0xffeb8c, 0x9f9b93, new Item.Properties()));
    public static final RegistryObject<Item> REDCOAT_SPAWN_EGG = REGISTER.register("redcoat_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.REDCOAT, 0xa02727, 0x74913a, new Item.Properties()));
    public static final RegistryObject<Item> DISSIDENT_SPAWN_EGG = REGISTER.register("dissident_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.DISSIDENT, 0x202428, 0xab6621, new Item.Properties()));
    public static final RegistryObject<Item> HIVE_SPAWN_EGG = REGISTER.register("hive_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.HIVE, 0x9c9a9a, 0x474545, new Item.Properties()));
    public static final RegistryObject<Item> SWARM_SPAWN_EGG = REGISTER.register("swarm_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.SWARM, 0x535050, 0x151515, new Item.Properties()));
    public static final RegistryObject<Item> HORNLIN_SPAWN_EGG = REGISTER.register("hornlin_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.HORNLIN, 0xa2593a, 0x9c3f69, new Item.Properties()));
    public static final RegistryObject<Item> BLUNDERER_SPAWN_EGG = REGISTER.register("blunderer_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.BLUNDERER, 0x32663c, 0x98a2a2, new Item.Properties()));


    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }


}
