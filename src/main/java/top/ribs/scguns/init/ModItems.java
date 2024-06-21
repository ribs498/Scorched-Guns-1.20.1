package top.ribs.scguns.init;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.Attachments;
import top.ribs.scguns.common.GunModifiers;
import top.ribs.scguns.item.*;
import top.ribs.scguns.item.ammo_boxes.MagnumAmmoBoxItem;
import top.ribs.scguns.item.ammo_boxes.PistolAmmoBoxItem;

import top.ribs.scguns.item.ammo_boxes.RifleAmmoBoxItem;
import top.ribs.scguns.item.ammo_boxes.ShotgunAmmoBoxItem;
import top.ribs.scguns.item.attachment.impl.Barrel;
import top.ribs.scguns.item.attachment.impl.Magazine;
import top.ribs.scguns.item.attachment.impl.Stock;
import top.ribs.scguns.item.attachment.impl.UnderBarrel;

public class ModItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);
    public static final RegistryObject<Item> PISTOL_AMMO_BOX = REGISTER.register("pistol_ammo_box", () -> new PistolAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RIFLE_AMMO_BOX = REGISTER.register("rifle_ammo_box", () -> new RifleAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item>SHOTGUN_AMMO_BOX = REGISTER.register("shotgun_ammo_box", () -> new ShotgunAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item>MAGNUM_AMMO_BOX = REGISTER.register("magnum_ammo_box", () -> new MagnumAmmoBoxItem(new Item.Properties().stacksTo(1)));

    ////BLACK POWDER
    public static final RegistryObject<GunItem> FLINTLOCK_PISTOL = REGISTER.register("flintlock_pistol", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> MUSKET = REGISTER.register("musket", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> BLUNDERBUSS = REGISTER.register("blunderbuss", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> REPEATING_MUSKET = REGISTER.register("repeating_musket", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    ///COPPER
    public static final RegistryObject<GunItem> COPPER_SHOTGUN = REGISTER.register("copper_shotgun", () -> new GunItem(new Item.Properties().stacksTo(1).durability(256)));
    public static final RegistryObject<GunItem> COPPER_PISTOL = REGISTER.register("copper_pistol", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> COPPER_RIFLE = REGISTER.register("copper_rifle", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> COPPER_SMG = REGISTER.register("copper_smg", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> COPPER_MAGNUM = REGISTER.register("copper_magnum", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));

    public static final RegistryObject<GunItem> IRON_CARABINE = REGISTER.register("iron_carabine", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> IRON_SMG = REGISTER.register("iron_smg", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> IRON_SPEAR = REGISTER.register("iron_spear", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> DEFENDER_PISTOL = REGISTER.register("defender_pistol", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> COMBAT_SHOTGUN = REGISTER.register("combat_shotgun", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> GYROJET_PISTOL = REGISTER.register("gyrojet_pistol", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<GunItem> ROCKET_RIFLE = REGISTER.register("rocket_rifle", () -> new GunItem(new Item.Properties().stacksTo(1).durability(128)));


    public static final RegistryObject<PickaxeItem> ANTHRALITE_PICKAXE = REGISTER.register("anthralite_pickaxe", () -> new PickaxeItem(ModTiers.ANTHRALITE, 1, -2.8F, new Item.Properties()));
    public static final RegistryObject<SwordItem> ANTHRALITE_SWORD = REGISTER.register("anthralite_sword", () -> new SwordItem(ModTiers.ANTHRALITE, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<AxeItem> ANTHRALITE_AXE = REGISTER.register("anthralite_axe", () -> new AxeItem(ModTiers.ANTHRALITE, 5, -3.0F, new Item.Properties()));
    public static final RegistryObject<ShovelItem> ANTHRALITE_SHOVEL = REGISTER.register("anthralite_shovel", () -> new ShovelItem(ModTiers.ANTHRALITE, 1.5F, -3.0F, new Item.Properties()));
    public static final RegistryObject<HoeItem> ANTHRALITE_HOE = REGISTER.register("anthralite_hoe", () -> new HoeItem(ModTiers.ANTHRALITE, -3, -3.0F, new Item.Properties()));


    public static final RegistryObject<Item> ANTHRALITE_HELMET = REGISTER.register("anthralite_helmet", () -> new ArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_CHESTPLATE = REGISTER.register("anthralite_chestplate", () -> new ArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_LEGGINGS = REGISTER.register("anthralite_leggings", () -> new ArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_BOOTS = REGISTER.register("anthralite_boots", () -> new ArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.BOOTS, new Item.Properties()));






    public static final RegistryObject<Item> NITER_DUST = REGISTER.register("niter_dust", () -> new NiterDustItem(new Item.Properties()));
    public static final RegistryObject<Item> SULFUR_CHUNK = REGISTER.register("sulfur_chunk", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GRIT = REGISTER.register("grit", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GUNPOWDER_DUST = REGISTER.register("gunpowder_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_ANTHRALITE = REGISTER.register("raw_anthralite", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_INGOT = REGISTER.register("anthralite_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_NUGGET = REGISTER.register("anthralite_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANCIENT_BRASS= REGISTER.register("ancient_brass", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TREATED_BRASS_BLEND = REGISTER.register("treated_brass_blend", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TREATED_BRASS_INGOT = REGISTER.register("treated_brass_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_STEEL_BLEND = REGISTER.register("diamond_steel_blend", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_STEEL_INGOT = REGISTER.register("diamond_steel_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SCORCHED_INGOT = REGISTER.register("scorched_ingot", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GUN_GRIP = REGISTER.register("gun_grip", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GUN_BARREL = REGISTER.register("gun_barrel", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GUN_MAGAZINE = REGISTER.register("gun_magazine", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_GUN_FRAME = REGISTER.register("copper_gun_frame", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_GUN_PARTS = REGISTER.register("copper_gun_parts", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_GUN_FRAME = REGISTER.register("iron_gun_frame", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_GUN_PARTS = REGISTER.register("iron_gun_parts", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SMALL_CASING_MOLD = REGISTER.register("small_casing_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(64)));
    public static final RegistryObject<Item> MEDIUM_CASING_MOLD = REGISTER.register("medium_casing_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(64)));
    public static final RegistryObject<Item> LARGE_CASING_MOLD = REGISTER.register("large_casing_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(64)));
    public static final RegistryObject<Item> GUN_PARTS_MOLD = REGISTER.register("gun_parts_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(16)));

    public static final RegistryObject<Item> SMALL_COPPER_CASING = REGISTER.register("small_copper_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_COPPER_CASING = REGISTER.register("medium_copper_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SMALL_IRON_CASING = REGISTER.register("small_iron_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LARGE_IRON_CASING = REGISTER.register("large_iron_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SMALL_BRASS_CASING = REGISTER.register("small_brass_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_BRASS_CASING = REGISTER.register("medium_brass_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LARGE_BRASS_CASING = REGISTER.register("large_brass_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> EMPTY_SHELL = REGISTER.register("empty_shell", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> POWDER_AND_BALL= REGISTER.register("powder_and_ball", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> GRAPESHOT= REGISTER.register("grapeshot", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> COMPACT_COPPER_ROUND = REGISTER.register("compact_copper_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> STANDARD_COPPER_ROUND = REGISTER.register("standard_copper_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> COMPACT_ADVANCED_ROUND = REGISTER.register("compact_advanced_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> CASULL_ROUND = REGISTER.register("casull_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_ROUND = REGISTER.register("advanced_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> HEAVY_ROUND = REGISTER.register("heavy_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> BEOWULF_ROUND = REGISTER.register("beowulf_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> SHOTGUN_SHELL = REGISTER.register("shotgun_shell", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> BEARPACK_SHELL = REGISTER.register("bearpack_shell", () -> new AmmoItem(new Item.Properties()));
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


    // Scope Attachments
    public static final RegistryObject<Item> LONG_SCOPE = REGISTER.register("long_scope", () -> new ScopeItem(Attachments.LONG_SCOPE, new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<Item> MEDIUM_SCOPE = REGISTER.register("medium_scope", () -> new ScopeItem(Attachments.MEDIUM_SCOPE, new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<Item> REFLEX_SIGHT = REGISTER.register("reflex_sight", () -> new ScopeItem(Attachments.REFLEX_SIGHT, new Item.Properties().stacksTo(1).durability(800)));
    // Stock Attachments
    public static final RegistryObject<Item> LIGHT_STOCK = REGISTER.register("light_stock", () -> new StockItem(Stock.create(GunModifiers.BETTER_CONTROL), new Item.Properties().stacksTo(1).durability(600), false));

    public static final RegistryObject<Item> WEIGHTED_STOCK = REGISTER.register("weighted_stock", () -> new StockItem(Stock.create(GunModifiers.SUPER_STABILISED), new Item.Properties().stacksTo(1).durability(1000)));
    public static final RegistryObject<Item> WOODEN_STOCK = REGISTER.register("wooden_stock", () -> new StockItem(Stock.create(GunModifiers.BETTER_CONTROL), new Item.Properties().stacksTo(1).durability(600), false));
    // Barrel Attachments
    public static final RegistryObject<Item> SILENCER = REGISTER.register("silencer", () -> new BarrelItem(Barrel.create(0.0F, GunModifiers.SILENCED, GunModifiers.REDUCED_DAMAGE), new Item.Properties().stacksTo(1).durability(250)));
    public static final RegistryObject<Item> ADVANCED_SILENCER = REGISTER.register("advanced_silencer", () -> new BarrelItem(Barrel.create(0.0F, GunModifiers.SILENCED), new Item.Properties().stacksTo(1).durability(250)));
    public static final RegistryObject<Item> MUZZLE_BRAKE = REGISTER.register("muzzle_brake", () -> new BarrelItem(Barrel.create(0.0F, GunModifiers.STABILISED), new Item.Properties().stacksTo(1).durability(250)));

    // Under Barrel Attachments
    public static final RegistryObject<Item> LIGHT_GRIP = REGISTER.register("light_grip", () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.LIGHT_RECOIL), new Item.Properties().stacksTo(1).durability(600)));
    public static final RegistryObject<Item> VERTICAL_GRIP = REGISTER.register("vertical_grip", () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.REDUCED_RECOIL), new Item.Properties().stacksTo(1).durability(800)));
    public static final RegistryObject<Item> IRON_BAYONET = REGISTER.register("iron_bayonet", () -> new BayonetItem(UnderBarrel.create(GunModifiers.IRON_BAYONET_DAMAGE), new Item.Properties().stacksTo(1).durability(800), 4.0f, -2.4f));
    public static final RegistryObject<Item> DIAMOND_BAYONET = REGISTER.register("diamond_bayonet", () -> new BayonetItem(UnderBarrel.create(GunModifiers.DIAMOND_BAYONET_DAMAGE), new Item.Properties().stacksTo(1).durability(800), 5.0f, -2.4f));
    public static final RegistryObject<Item> NETHERITE_BAYONET = REGISTER.register("netherite_bayonet", () -> new BayonetItem(UnderBarrel.create(GunModifiers.NETHERITE_BAYONET_DAMAGE), new Item.Properties().stacksTo(1).durability(800), 6.0f, -2.4f));


    //Magazines
    public static final RegistryObject<Item> EXTENDED_MAG = REGISTER.register("extended_mag", () -> new MagazineItem(Magazine.create(GunModifiers.SLOW_RELOAD, GunModifiers.EXTENDED_MAG), new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<Item> SPEED_MAG = REGISTER.register("speed_mag", () -> new MagazineItem(Magazine.create(GunModifiers.FAST_RELOAD), new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<Item> PLUS_P_MAG = REGISTER.register("plus_p_mag", () -> new MagazineItem(Magazine.create(GunModifiers.INCREASED_DAMAGE, GunModifiers.PLUS_P_MAG), new Item.Properties().stacksTo(1).durability(128)));
//ITEMS

    public static final RegistryObject<Item> REPAIR_KIT = REGISTER.register("repair_kit", () -> new ToolTipItem(new Item.Properties()));

    // Mobs
    public static final RegistryObject<Item> COG_MINION_SPAWN_EGG = REGISTER.register("cog_minion_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.COG_MINION, 0x76501f, 0x7f8080, new Item.Properties()));
    public static final RegistryObject<Item> COG_KNIGHT_SPAWN_EGG = REGISTER.register("cog_knight_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.COG_KNIGHT, 0xf7cb6c, 0xbf8e55, new Item.Properties()));
    public static final RegistryObject<Item> SKY_CARRIER_SPAWN_EGG = REGISTER.register("sky_carrier_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.SKY_CARRIER, 0xffeb8c, 0x4f4f4f, new Item.Properties()));
    public static final RegistryObject<Item> SUPPLY_SCAMP_SPAWN_EGG = REGISTER.register("supply_scamp_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.SUPPLY_SCAMP, 0xffeb8c, 0x9f9b93, new Item.Properties()));
    public static final RegistryObject<Item> REDCOAT_SPAWN_EGG = REGISTER.register("redcoat_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.REDCOAT, 0xffeb8c, 0x9f9b93, new Item.Properties()));
    public static final RegistryObject<Item> DISSIDENT_SPAWN_EGG = REGISTER.register("dissident_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.DISSIDENT, 0x202428, 0xab6621, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
