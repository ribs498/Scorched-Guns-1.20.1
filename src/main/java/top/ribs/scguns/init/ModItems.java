package top.ribs.scguns.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.Attachments;
import top.ribs.scguns.common.GunModifiers;
import top.ribs.scguns.item.*;
import top.ribs.scguns.item.ammo_boxes.*;

import top.ribs.scguns.item.animated.*;
import top.ribs.scguns.item.attachment.impl.Barrel;
import top.ribs.scguns.item.attachment.impl.Magazine;
import top.ribs.scguns.item.attachment.impl.Stock;
import top.ribs.scguns.item.attachment.impl.UnderBarrel;

import java.lang.reflect.Constructor;

public class ModItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);

    public static RegistryObject<Item> ANTHRALITE_KNIFE;
    public static RegistryObject<GunItem> GALE;
    public static RegistryObject<GunItem> UMAX_PISTOL;
    public static RegistryObject<Item> VENTURI;
    public static RegistryObject<Item> SCRATCHES;

    public static void registerItems() {
        SCRATCHES = REGISTER.register("scratches", () -> {
            Item.Properties properties = new Item.Properties().stacksTo(1).durability(560);
            if (!ScorchedGuns.shouldUseEnergyGuns()) {
                return new AnimatedAirGunItem(properties,
                        "scratches",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get()
                );
            } else {
                return new AnimatedEnergyGunItem(properties,
                        "scratches",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        4800
                );
            }
        });
        GALE = REGISTER.register("gale", () -> {
            Item.Properties properties = new Item.Properties().stacksTo(1).durability(560);
            if (!ScorchedGuns.shouldUseEnergyGuns()) {
                return new AnimatedAirGunItem(properties,
                        "gale",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get()
                );
            } else {
                return new AnimatedEnergyGunItem(properties,
                        "gale",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        4800
                );
            }
        });
        UMAX_PISTOL = REGISTER.register("umax_pistol", () -> {
            Item.Properties properties = new Item.Properties().stacksTo(1).durability(560);
            if (!ScorchedGuns.shouldUseEnergyGuns()) {
                return new AnimatedAirGunItem(properties,
                        "umax_pistol",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get()
                );
            } else {
                return new AnimatedEnergyGunItem(properties,
                        "umax_pistol",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        4800
                );
            }
        });


        VENTURI = REGISTER.register("venturi", () -> {
            Item.Properties properties = new Item.Properties().stacksTo(1).durability(560);
            if (!ScorchedGuns.shouldUseEnergyGuns()) {
                return new AnimatedAirGunItem(properties,
                        "venturi",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get()
                );
            } else {
                return new AnimatedEnergyGunItem(properties,
                        "venturi",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        4800
                );
            }
        });


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
    /**
     * Creates either an AirGunItem or EnergyGunItem based on whether Create is loaded
     * @param durability The durability for both gun types
     * @param energyCapacity The energy capacity (only used for EnergyGunItem)
     * @return The appropriate GunItem instance
     */
    private static GunItem createGunItem(int durability, int energyCapacity) {
        Item.Properties properties = new Item.Properties().stacksTo(1).durability(durability);

        if (!ScorchedGuns.shouldUseEnergyGuns()) {
            return new AirGunItem(properties);
        } else {
            return new EnergyGunItem(properties, energyCapacity);
        }
    }

    public static final RegistryObject<AnimatedGunItem> M3_CARABINE = REGISTER.register("m3_carabine",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "m3_carabine",                               // Model path
                    ModSounds.MAG_OUT.get(),        // Reload sound mag out
                    ModSounds.MAG_IN.get(),         // Reload sound mag in
                    ModSounds.RELOAD_END.get(),           // Reload sound end
                    ModSounds.COPPER_GUN_JAM.get(),      // Ejector sound pull
                    ModSounds.COPPER_GUN_JAM.get()    // Ejector sound release
            )
    );
    public static final RegistryObject<AnimatedGunItem> MAKESHIFT_RIFLE = REGISTER.register("makeshift_rifle",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "makeshift_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> LOCKEWOOD = REGISTER.register("lockewood",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "lockewood",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> GRANDLE = REGISTER.register("grandle",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "grandle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> DEFENDER_PISTOL = REGISTER.register("defender_pistol",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "defender_pistol",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> TRENCHUR = REGISTER.register("trenchur",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "trenchur",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> AUVTOMAG = REGISTER.register("auvtomag",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "auvtomag",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> GREASER_SMG = REGISTER.register("greaser_smg",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "greaser_smg",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> BOOMSTICK = REGISTER.register("boomstick",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(400),
                    "boomstick",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> INERTIAL = REGISTER.register("inertial",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "inertial",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> M22_WALTZ = REGISTER.register("m22_waltz",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "m22_waltz",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedUnderWaterGunItem> FLOUNDERGAT = REGISTER.register("floundergat",
            () -> new AnimatedUnderWaterGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "floundergat",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedDiamondSteelGunItem> KRAUSER = REGISTER.register("krauser",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "krauser",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedDiamondSteelGunItem> UPPERCUT = REGISTER.register("uppercut",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "uppercut",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedDiamondSteelGunItem> PRUSH_GUN = REGISTER.register("prush_gun",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "prush_gun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedDiamondSteelGunItem> SOUL_DRUMMER = REGISTER.register("soul_drummer",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "soul_drummer",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedSilencedDiamondSteelFirearm> VALORA = REGISTER.register("valora",
            () -> new AnimatedSilencedDiamondSteelFirearm(
                    new Item.Properties().stacksTo(1).durability(800),
                    "valora",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> RUSTY_GNAT = REGISTER.register("rusty_gnat",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(400),
                    "rusty_gnat",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> CALLWELL = REGISTER.register("callwell",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "callwell",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> CALLWELL_CONVERSION = REGISTER.register("callwell_conversion",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "callwell_conversion",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> COMBAT_SHOTGUN = REGISTER.register("combat_shotgun",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "combat_shotgun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> FLINTLOCK_PISTOL = REGISTER.register("flintlock_pistol",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "flintlock_pistol",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<NonUnderwaterAnimatedGunItem> HANDCANNON = REGISTER.register("handcannon",
            () -> new NonUnderwaterAnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "handcannon",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<NonUnderwaterAnimatedGunItem> MUSKET = REGISTER.register("musket",
            () -> new NonUnderwaterAnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "musket",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<NonUnderwaterAnimatedGunItem> REPEATING_MUSKET = REGISTER.register("repeating_musket",
            () -> new NonUnderwaterAnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "repeating_musket",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<NonUnderwaterAnimatedGunItem> BLUNDERBUSS = REGISTER.register("blunderbuss",
            () -> new NonUnderwaterAnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "blunderbuss",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedDiamondSteelGunItem> MAS_55 = REGISTER.register("mas_55",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "mas_55",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> COGLOADER = REGISTER.register("cogloader",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "cogloader",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> SAKETINI = REGISTER.register("saketini",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "saketini",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> SCRAPPER = REGISTER.register("scrapper",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(400),
                    "scrapper",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> BRAWLER = REGISTER.register("brawler",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "brawler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> WINNIE = REGISTER.register("winnie",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "winnie",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> BRUISER = REGISTER.register("bruiser",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(400),
                    "bruiser",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedDiamondSteelGunItem> CYCLONE = REGISTER.register("cyclone",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "cyclone",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedDiamondSteelGunItem> PLASGUN = REGISTER.register("plasgun",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "plasgun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> ROCKET_RIFLE = REGISTER.register("rocket_rifle",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "rocket_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> MARLIN = REGISTER.register("marlin",
            () -> new AnimatedUnderWaterGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "marlin",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> GAUSS_RIFLE = REGISTER.register("gauss_rifle",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "gauss_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> NIAMI = REGISTER.register("niami",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "niami",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> IRON_SPEAR = REGISTER.register("iron_spear",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "iron_spear",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> IRON_JAVELIN = REGISTER.register("iron_javelin",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "iron_javelin",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );

    public static final RegistryObject<AnimatedGunItem> LLR_DIRECTOR = REGISTER.register("llr_director",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(400),
                    "llr_director",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> PAX = REGISTER.register("pax",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "pax",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> BIG_BORE = REGISTER.register("big_bore",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(6),
                    "big_bore",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> HOWLER = REGISTER.register("howler",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "howler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> HOWLER_CONVERSION = REGISTER.register("howler_conversion",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "howler_conversion",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> PULSAR = REGISTER.register("pulsar",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "pulsar",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> ARC_WORKER = REGISTER.register("arc_worker",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(400),
                    "arc_worker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> LASER_MUSKET = REGISTER.register("laser_musket",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(512),
                    "laser_musket",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> PLASMABUSS = REGISTER.register("plasmabuss",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(512),
                    "plasmabuss",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> JACKHAMMER = REGISTER.register("jackhammer",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "jackhammer",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> SEQUOIA = REGISTER.register("sequoia",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "sequoia",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> ULTRA_KNIGHT_HAWK = REGISTER.register("ultra_knight_hawk",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(7),
                    "ultra_knight_hawk",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> SUPER_SHOTGUN = REGISTER.register("super_shotgun",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "super_shotgun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> BOMB_LANCE = REGISTER.register("bomb_lance",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "bomb_lance",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> DOZIER_RL = REGISTER.register("dozier_rl",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(512),
                    "dozier_rl",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> DARK_MATTER = REGISTER.register("dark_matter",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1500),
                    "dark_matter",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> MK43_RIFLE = REGISTER.register("mk43_rifle",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "mk43_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> GYROJET_PISTOL = REGISTER.register("gyrojet_pistol",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(560),
                    "gyrojet_pistol",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> THUNDERHEAD = REGISTER.register("thunderhead",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(600),
                    "thunderhead",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> GATTALER = REGISTER.register("gattaler",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1000),
                    "gattaler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> CR4K_MINING_LASER = REGISTER.register("cr4k_mining_laser",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1200),
                    "cr4k_mining_laser",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> SHARD_CULLER = REGISTER.register("shard_culler",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1000),
                    "shard_culler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> SPITFIRE = REGISTER.register("spitfire",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "spitfire",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> WALTZ_CONVERSION = REGISTER.register("waltz_conversion",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "waltz_conversion",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> OSGOOD_50 = REGISTER.register("osgood_50",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "osgood_50",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> FREYR = REGISTER.register("freyr",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(700),
                    "freyr",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> VULCANIC_REPEATER = REGISTER.register("vulcanic_repeater",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(700),
                    "vulcanic_repeater",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> PYROCLASTIC_FLOW = REGISTER.register("pyroclastic_flow",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(700),
                    "pyroclastic_flow",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> MANGALITSA = REGISTER.register("mangalitsa",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(700),
                    "mangalitsa",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> BLASPHEMY = REGISTER.register("blasphemy",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(700),
                    "blasphemy",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> WHISPERS = REGISTER.register("whispers",
            () -> new AnimatedSilencedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "whispers",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> SCULK_RESONATOR = REGISTER.register("sculk_resonator",
            () -> new AnimatedSilencedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "sculk_resonator",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> ECHOES_2 = REGISTER.register("echoes_2",
            () -> new AnimatedSilencedGunItem(
                    new Item.Properties().stacksTo(1).durability(900),
                    "echoes_2",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> RAYGUN = REGISTER.register("raygun",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1200),
                    "raygun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> CARAPICE = REGISTER.register("carapice",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1200),
                    "carapice",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> SHELLURKER = REGISTER.register("shellurker",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1200),
                    "shellurker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> LONE_WONDER = REGISTER.register("lone_wonder",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1200),
                    "lone_wonder",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> RAT_KING_AND_QUEEN = REGISTER.register("rat_king_and_queen",
            () -> new AnimatedDualWieldGunItem(
                    new Item.Properties().stacksTo(1).durability(1500),
                    "rat_king_and_queen",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> LOCUST = REGISTER.register("locust",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(1500),
                    "locust",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedGunItem> NEWBORN_CYST = REGISTER.register("newborn_cyst",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(1500),
                    "newborn_cyst",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );

    public static final RegistryObject<AnimatedScorchedGunItem> ASTELLA = REGISTER.register("astella",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(1500),
                    "astella",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedScorchedGunItem> PRIMA_MATERIA = REGISTER.register("prima_materia",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(1500),
                    "prima_materia",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedScorchedEnergyGunItem> NERVEPINCH = REGISTER.register("nervepinch",
            () -> new AnimatedScorchedEnergyGunItem(
                    new Item.Properties().stacksTo(1).durability(1500),
                    "nervepinch",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    5000
            )
    );
    public static final RegistryObject<AnimatedScorchedGunItem> EARTHS_CORPSE = REGISTER.register("earths_corpse",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(1500),
                    "earths_corpse",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final RegistryObject<AnimatedScorchedGunItem> FLAYED_GOD = REGISTER.register("flayed_god",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(1500),
                    "flayed_god",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );


   public static final RegistryObject<PickaxeItem> ANTHRALITE_PICKAXE = REGISTER.register("anthralite_pickaxe", () -> new PickaxeItem(ModTiers.ANTHRALITE, 1, -2.8F, new Item.Properties()));
    public static final RegistryObject<SwordItem> ANTHRALITE_SWORD = REGISTER.register("anthralite_sword", () -> new SwordItem(ModTiers.ANTHRALITE, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<AxeItem> ANTHRALITE_AXE = REGISTER.register("anthralite_axe", () -> new AxeItem(ModTiers.ANTHRALITE, 5, -3.0F, new Item.Properties()));
    public static final RegistryObject<ShovelItem> ANTHRALITE_SHOVEL = REGISTER.register("anthralite_shovel", () -> new ShovelItem(ModTiers.ANTHRALITE, 1.5F, -3.0F, new Item.Properties()));
    public static final RegistryObject<HoeItem> ANTHRALITE_HOE = REGISTER.register("anthralite_hoe", () -> new HoeItem(ModTiers.ANTHRALITE, -3, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> RANGE_FINDER = REGISTER.register("range_finder", () -> new RangeFinderItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ANTHRALITE_HELMET = REGISTER.register("anthralite_helmet", () -> new AnthraliteArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_CHESTPLATE = REGISTER.register("anthralite_chestplate", () -> new AnthraliteArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_LEGGINGS = REGISTER.register("anthralite_leggings", () -> new AnthraliteArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_BOOTS = REGISTER.register("anthralite_boots", () -> new AnthraliteArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final RegistryObject<Item> RIDGETOP = REGISTER.register("ridgetop", () -> new RidgetopArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> BRASS_MASK = REGISTER.register("brass_mask", () -> new BrassMaskArmorItem(ModArmorMaterials.TREATED_BRASS, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> ADRIEN_HELM = REGISTER.register("adrien_helm", () -> new AdrienArmorItem(ModArmorMaterials.ADRIEN, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> ADRIEN_CHESTPLATE = REGISTER.register("adrien_chestplate", () -> new AdrienArmorItem(ModArmorMaterials.ADRIEN, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> ADRIEN_LEGGINGS = REGISTER.register("adrien_leggings", () -> new AdrienArmorItem(ModArmorMaterials.ADRIEN, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> ADRIEN_BOOTS = REGISTER.register("adrien_boots", () -> new AdrienArmorItem(ModArmorMaterials.ADRIEN, ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_RESPIRATOR = REGISTER.register("anthralite_respirator", () -> new AnthraliteGasMaskArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> NETHERITE_RESPIRATOR = REGISTER.register("netherite_respirator", () -> new NetheriteGasMaskArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_STEEL_HELMET = REGISTER.register("diamond_steel_helmet", () -> new ArmorItem(ModArmorMaterials.DIAMOND_STEEL, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_STEEL_CHESTPLATE = REGISTER.register("diamond_steel_chestplate", () -> new ArmorItem(ModArmorMaterials.DIAMOND_STEEL, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_STEEL_LEGGINGS = REGISTER.register("diamond_steel_leggings", () -> new ArmorItem(ModArmorMaterials.DIAMOND_STEEL, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_STEEL_BOOTS = REGISTER.register("diamond_steel_boots", () -> new ArmorItem(ModArmorMaterials.DIAMOND_STEEL, ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final RegistryObject<Item> TREATED_BRASS_HELMET = REGISTER.register("treated_brass_helmet", () -> new ArmorItem(ModArmorMaterials.TREATED_BRASS, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> TREATED_BRASS_CHESTPLATE = REGISTER.register("treated_brass_chestplate", () -> new ArmorItem(ModArmorMaterials.TREATED_BRASS, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> TREATED_BRASS_LEGGINGS = REGISTER.register("treated_brass_leggings", () -> new ArmorItem(ModArmorMaterials.TREATED_BRASS, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> TREATED_BRASS_BOOTS = REGISTER.register("treated_brass_boots", () -> new ArmorItem(ModArmorMaterials.TREATED_BRASS, ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final RegistryObject<Item> COPPER_BLUEPRINT = REGISTER.register("copper_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> IRON_BLUEPRINT = REGISTER.register("iron_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TREATED_BRASS_BLUEPRINT = REGISTER.register("treated_brass_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DIAMOND_STEEL_BLUEPRINT = REGISTER.register("diamond_steel_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PIGLIN_BLUEPRINT = REGISTER.register("piglin_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> OCEAN_BLUEPRINT = REGISTER.register("ocean_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> DEEP_DARK_BLUEPRINT = REGISTER.register("deep_dark_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> END_BLUEPRINT = REGISTER.register("end_blueprint", () -> new BlueprintItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> STANDARD_BULLET = REGISTER.register("standard_bullet", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_BULLET = REGISTER.register("hardened_bullet", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SCORCHED_BLUEPRINT = REGISTER.register("scorched_blueprint", () -> new GlintedBlueprintItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> NITRO_POWDER = REGISTER.register("nitro_powder", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NITRO_POWDER_DUST = REGISTER.register("nitro_powder_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NITER_DUST = REGISTER.register("niter_dust", () -> new NiterDustItem(new Item.Properties()));
    public static final RegistryObject<Item> SHEOL = REGISTER.register("sheol", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SCAMP_CONTROLLER = REGISTER.register("scamp_controller", () -> new ScampControllerItem(new Item.Properties()));
    public static final RegistryObject<Item> PEAL = REGISTER.register("peal", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PEAL_DUST = REGISTER.register("peal_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> VEHEMENT_COAL = REGISTER.register("vehement_coal", () -> new FuelItem(new Item.Properties(), 4800));
    public static final RegistryObject<Item> SHEOL_DUST = REGISTER.register("sheol_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SULFUR_CHUNK = REGISTER.register("sulfur_chunk", () -> new FuelItem(new Item.Properties(), 800));
    public static final RegistryObject<Item> COMPOSITE_FILTER = REGISTER.register("composite_filter", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SULFUR_DUST = REGISTER.register("sulfur_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PHOSPHOR_DUST = REGISTER.register("phosphor_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BUCKSHOT = REGISTER.register("buckshot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NITRO_BUCKSHOT = REGISTER.register("nitro_buckshot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_PHOSPHOR = REGISTER.register("raw_phosphor", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GUNPOWDER_DUST = REGISTER.register("gunpowder_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_ANTHRALITE = REGISTER.register("raw_anthralite", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CRUSHED_RAW_ANTHRALITE = REGISTER.register("crushed_raw_anthralite", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_DUST = REGISTER.register("anthralite_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CLUMP_ANTHRALITE = REGISTER.register("clump_anthralite", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SHARD_ANTHRALITE = REGISTER.register("shard_anthralite", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIRTY_DUST_ANTHRALITE = REGISTER.register("dirty_dust_anthralite", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MASS_PRODUCTION_MUSIC_DISC = REGISTER.register("music_disc_mass_production",
            () -> new RecordItem(12, ModSounds.MASS_PRODUCTION, new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 2580));
    public static final RegistryObject<Item> MASS_DESTRUCTION_MUSIC_DISC = REGISTER.register("music_disc_mass_destruction",
            () -> new RecordItem(15, ModSounds.MASS_DESTRUCTION, new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 6280));
    public static final RegistryObject<Item> MASS_DESTRUCTION_EXTENDED_MUSIC_DISC = REGISTER.register("music_disc_mass_destruction_extended",
            () -> new RecordItem(20, ModSounds.MASS_DESTRUCTION_EXTENDED, new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 8320));

    public static final RegistryObject<Item> TEAM_LOG = REGISTER.register("team_log", () -> new TeamLogItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENEMY_LOG = REGISTER.register("enemy_log", () -> new EnemyLogItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ANTHRALITE_INGOT = REGISTER.register("anthralite_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANTHRALITE_NUGGET = REGISTER.register("anthralite_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANCIENT_BRASS = REGISTER.register("ancient_brass", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TREATED_IRON_BLEND = REGISTER.register("treated_iron_blend", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TREATED_IRON_INGOT = REGISTER.register("treated_iron_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TREATED_IRON_NUGGET = REGISTER.register("treated_iron_nugget", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TREATED_BRASS_BLEND = REGISTER.register("treated_brass_blend", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TREATED_BRASS_INGOT = REGISTER.register("treated_brass_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_STEEL_BLEND = REGISTER.register("diamond_steel_blend", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_STEEL_INGOT = REGISTER.register("diamond_steel_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SCORCHED_BLEND = REGISTER.register("scorched_blend", () -> new ScorchedItem(new Item.Properties()));
    public static final RegistryObject<Item> SCORCHED_INGOT = REGISTER.register("scorched_ingot", () -> new ScorchedItem(new Item.Properties()));
    public static final RegistryObject<Item> CHARGED_AMETHYST_SHARD = REGISTER.register("charged_amethyst_shard", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> EMPTY_TANK = REGISTER.register("empty_tank", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> EMPTY_CORE = REGISTER.register("empty_core", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENERGY_CORE = REGISTER.register("energy_core", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DEPLETED_ENERGY_CORE = REGISTER.register("depleted_energy_core", () -> new Item(new Item.Properties()));
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
    public static final RegistryObject<Item> SMALL_CASING_MOLD = REGISTER.register("small_casing_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<Item> MEDIUM_CASING_MOLD = REGISTER.register("medium_casing_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<Item> LARGE_CASING_MOLD = REGISTER.register("large_casing_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<Item> BULLET_MOLD = REGISTER.register("bullet_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<Item> DISC_MOLD = REGISTER.register("disc_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(64)));
    public static final RegistryObject<Item> GUN_PARTS_MOLD = REGISTER.register("gun_parts_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(32)));
    public static final RegistryObject<Item> COPPER_DISC = REGISTER.register("copper_disc", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SMALL_COPPER_CASING = REGISTER.register("small_copper_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_COPPER_CASING = REGISTER.register("medium_copper_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SMALL_IRON_CASING = REGISTER.register("small_iron_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LARGE_IRON_CASING = REGISTER.register("large_iron_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> EMPTY_CELL = REGISTER.register("empty_cell", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SHULKER_CASING = REGISTER.register("shulker_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SMALL_DIAMOND_STEEL_CASING = REGISTER.register("small_diamond_steel_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_DIAMOND_STEEL_CASING = REGISTER.register("medium_diamond_steel_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SMALL_BRASS_CASING = REGISTER.register("small_brass_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_BRASS_CASING = REGISTER.register("medium_brass_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LARGE_BRASS_CASING = REGISTER.register("large_brass_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> POWDER_AND_BALL = REGISTER.register("powder_and_ball", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> GRAPESHOT = REGISTER.register("grapeshot", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> COMPACT_COPPER_ROUND = REGISTER.register("compact_copper_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> HOG_ROUND = REGISTER.register("hog_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> STANDARD_COPPER_ROUND = REGISTER.register("standard_copper_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> COMPACT_ADVANCED_ROUND = REGISTER.register("compact_advanced_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> RAMROD_ROUND = REGISTER.register("ramrod_round", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_ROUND = REGISTER.register("advanced_round",
            () -> new TooltipAmmo(new Item.Properties(), 2));
    public static final RegistryObject<Item> KRAHG_ROUND = REGISTER.register("krahg_round",
            () -> new TooltipAmmo(new Item.Properties(), 4));
    public static final RegistryObject<Item> BEOWULF_ROUND = REGISTER.register("beowulf_round",
            () -> new TooltipAmmo(new Item.Properties(), 2));
    public static final RegistryObject<Item> GIBBS_ROUND = REGISTER.register("gibbs_round",
            () -> new TooltipAmmo(new Item.Properties(), 2));
    public static final RegistryObject<Item> SHOTGUN_SHELL = REGISTER.register("shotgun_shell", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> BEARPACK_SHELL = REGISTER.register("bearpack_shell", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> BLAZE_FUEL = REGISTER.register("blaze_fuel",
            () -> new FuelAmmoItem(
                    new Item.Properties(),
                    3200,
                    ModItems.EMPTY_TANK,
                    new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0),
                    new MobEffectInstance(MobEffects.WEAKNESS, 100, 0)
            ));
    public static final RegistryObject<Item> ENERGY_CELL = REGISTER.register("energy_cell", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> SCULK_CELL = REGISTER.register("sculk_cell",
            () -> new TooltipAmmo(new Item.Properties(), 6));
    public static final RegistryObject<Item> SHOCK_CELL = REGISTER.register("shock_cell",
            () -> new TooltipAmmo(new Item.Properties(), "tooltip.scguns.arcing"));
    public static final RegistryObject<Item> MICROJET = REGISTER.register("microjet", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> SHULKSHOT = REGISTER.register("shulkshot",
            () -> new TooltipAmmo(new Item.Properties(), "tooltip.scguns.homing"));

    public static final RegistryObject<Item> UNFINISHED_COMPACT_COPPER_ROUND = REGISTER.register("unfinished_compact_copper_round", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_HOG_ROUND = REGISTER.register("unfinished_hog_round", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_STANDARD_COPPER_ROUND = REGISTER.register("unfinished_standard_copper_round", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_COMPACT_ADVANCED_ROUND = REGISTER.register("unfinished_compact_advanced_round", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_RAMROD_ROUND = REGISTER.register("unfinished_ramrod_round", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_ADVANCED_ROUND = REGISTER.register("unfinished_advanced_round", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_KRAHG_ROUND = REGISTER.register("unfinished_krahg_round", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_BEOWULF_ROUND = REGISTER.register("unfinished_beowulf_round", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_GIBBS_ROUND = REGISTER.register("unfinished_gibbs_round", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_SHOTGUN_SHELL = REGISTER.register("unfinished_shotgun_shell", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_BEARPACK_SHELL = REGISTER.register("unfinished_bearpack_shell", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_ENERGY_CELL = REGISTER.register("unfinished_energy_cell", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_SCULK_CELL = REGISTER.register("unfinished_sculk_cell", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_MICROJET = REGISTER.register("unfinished_microjet", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_SHULKSHOT = REGISTER.register("unfinished_shulkshot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_ROCKET = REGISTER.register("unfinished_rocket", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_GUN_PARTS = REGISTER.register("unfinished_gun_parts", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_HEAVY_GUN_PARTS = REGISTER.register("unfinished_heavy_gun_parts", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_PLASMA_CORE = REGISTER.register("unfinished_plasma_core", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNFINISHED_OSBORNE_SLUG = REGISTER.register("unfinished_osborne_slug", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ROCKET = REGISTER.register("rocket", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> OSBORNE_SLUG = REGISTER.register("osborne_slug", () -> new AmmoItem(new Item.Properties().stacksTo(4)));
    public static final RegistryObject<Item> PEBBLES = REGISTER.register("pebbles", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HARDENED_PEBBLES = REGISTER.register("hardened_pebbles", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NETHERITE_SCRAP_CHUNK = REGISTER.register("netherite_scrap_chunk", () -> new ScorchedItem(new Item.Properties()));

    public static final RegistryObject<Item> PLASMA = REGISTER.register("plasma", () -> new FuelItem(new Item.Properties(), 2400));
    public static final RegistryObject<Item> PLASMA_NUGGET = REGISTER.register("plasma_nugget", () -> new FuelItem(new Item.Properties(), 480));
    public static final RegistryObject<Item> PISTOL_AMMO_BOX = REGISTER.register("pistol_ammo_box", () -> new PistolAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RIFLE_AMMO_BOX = REGISTER.register("rifle_ammo_box", () -> new RifleAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHOTGUN_AMMO_BOX = REGISTER.register("shotgun_ammo_box", () -> new ShotgunAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MAGNUM_AMMO_BOX = REGISTER.register("magnum_ammo_box", () -> new MagnumAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENERGY_AMMO_BOX = REGISTER.register("energy_ammo_box", () -> new EnergyAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> EMPTY_CASING_POUCH = REGISTER.register("empty_casing_pouch", () -> new EmptyCasingPouchItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ROCKET_AMMO_BOX = REGISTER.register("rocket_ammo_box", () -> new RocketAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPECIAL_AMMO_BOX = REGISTER.register("special_ammo_box", () -> new SpecialAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CREATIVE_AMMO_BOX = REGISTER.register("creative_ammo_box", () -> new CreativeAmmoBoxItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> DISHES_POUCH = REGISTER.register("dishes_pouch", () -> new DishesPouch(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ROCK_POUCH = REGISTER.register("rock_pouch", () -> new RockPouch(new Item.Properties().stacksTo(1)));


    // Projectiles And Throwables
    public static final RegistryObject<Item> GRENADE = REGISTER.register("grenade", () -> new GrenadeItem(new Item.Properties().stacksTo(16), 20 * 4));
    public static final RegistryObject<Item> STUN_GRENADE = REGISTER.register("stun_grenade", () -> new StunGrenadeItem(new Item.Properties().stacksTo(16), 72000));
    public static final RegistryObject<Item> MOLOTOV_COCKTAIL = REGISTER.register("molotov_cocktail", () -> new MolotovCocktailItem(new Item.Properties().stacksTo(16), 72000));
    public static final RegistryObject<Item> CHOKE_BOMB = REGISTER.register("choke_bomb", () -> new ChokeBombItem(new Item.Properties().stacksTo(16), 72000));
    public static final RegistryObject<Item> SWARM_BOMB = REGISTER.register("swarm_bomb", () -> new SwarmBombItem(new Item.Properties().stacksTo(16), 72000));
    public static final RegistryObject<Item> GAS_GRENADE = REGISTER.register("gas_grenade", () -> new GasGrenadeItem(new Item.Properties().stacksTo(16), 72000));


    // Medical Items
    public static final RegistryObject<Item> BASIC_POULTICE = REGISTER.register("basic_poultice",
            () -> new HealingBandageItem(new Item.Properties().stacksTo(16), 6, (MobEffectInstance) null));

    public static final RegistryObject<Item> HONEY_SULFUR_POULTICE = REGISTER.register("honey_sulfur_poultice",
            () -> new HealingBandageItem(new Item.Properties().stacksTo(16), 9, new MobEffectInstance(MobEffects.REGENERATION, 100, 0)));

    public static final RegistryObject<Item> ENCHANTED_BANDAGE = REGISTER.register("enchanted_bandage", () -> new GlintedHealingBandageItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), 12, new MobEffectInstance(MobEffects.REGENERATION, 100, 1), new MobEffectInstance(MobEffects.ABSORPTION, 400, 0)));

    public static final RegistryObject<Item> DRAGON_SALVE = REGISTER.register("dragon_salve", () -> new GlintedHealingBandageItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), 16, new MobEffectInstance(MobEffects.REGENERATION, 200, 1), new MobEffectInstance(MobEffects.ABSORPTION, 700, 0)));

    public static final RegistryObject<Item> COLD_PACK = REGISTER.register("cold_pack", () -> new ColdPackItem(new Item.Properties().stacksTo(16)));

    // Scope Attachments
    public static final RegistryObject<Item> LASER_SIGHT = REGISTER.register("laser_sight", () -> new LaserSightItem(Attachments.LASER_SIGHT, new Item.Properties().stacksTo(1).durability(1300)));
    public static final RegistryObject<Item> LONG_SCOPE = REGISTER.register("long_scope", () -> new ScopeItem(Attachments.LONG_SCOPE, new Item.Properties().stacksTo(1).durability(1600)));
    public static final RegistryObject<Item> MEDIUM_SCOPE = REGISTER.register("medium_scope", () -> new ScopeItem(Attachments.MEDIUM_SCOPE, new Item.Properties().stacksTo(1).durability(1400)));
    public static final RegistryObject<Item> REFLEX_SIGHT = REGISTER.register("reflex_sight", () -> new ScopeItem(Attachments.REFLEX_SIGHT, new Item.Properties().stacksTo(1).durability(1200)));
    // Stock Attachments
    public static final RegistryObject<Item> LIGHT_STOCK = REGISTER.register("light_stock", () -> new StockItem(Stock.create(GunModifiers.LIGHT_STOCK_MODIFIER), new Item.Properties().stacksTo(1).durability(1300), false));
    public static final RegistryObject<Item> WEIGHTED_STOCK = REGISTER.register("weighted_stock", () -> new StockItem(Stock.create(GunModifiers.WEIGHTED_STOCK_MODIFIER), new Item.Properties().stacksTo(1).durability(1700)));
    public static final RegistryObject<Item> WOODEN_STOCK = REGISTER.register("wooden_stock", () -> new StockItem(Stock.create(GunModifiers.WOODEN_STOCK_MODIFIER), new Item.Properties().stacksTo(1).durability(1500), false));
    // Barrel Attachments
    public static final RegistryObject<Item> SILENCER = REGISTER.register("silencer", () -> new BarrelItem(Barrel.create(0.0F, GunModifiers.SILENCER_MODIFIER, GunModifiers.SILENCED, GunModifiers.REDUCED_DAMAGE), new Item.Properties().stacksTo(1).durability(500)));
    public static final RegistryObject<Item> ADVANCED_SILENCER = REGISTER.register("advanced_silencer", () -> new BarrelItem(Barrel.create(0.0F, GunModifiers.ADVANCED_SILENCER_MODIFIER, GunModifiers.SILENCED), new Item.Properties().stacksTo(1).durability(1200)));
    public static final RegistryObject<Item> MUZZLE_BRAKE = REGISTER.register("muzzle_brake", () -> new BarrelItem(Barrel.create(0.0F, GunModifiers.MUZZLE_BRAKE_MODIFIER), new Item.Properties().stacksTo(1).durability(900)));
    public static final RegistryObject<Item> EXTENDED_BARREL = REGISTER.register("extended_barrel", () -> new ExtendedBarrelItem(Barrel.create(0.0F, GunModifiers.EXTENDED_BARREL_MODIFIER), new Item.Properties().stacksTo(1).durability(700)));

    // Under Barrel Attachments
    public static final RegistryObject<Item> LIGHT_GRIP = REGISTER.register("light_grip", () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.LIGHT_RECOIL), new Item.Properties().stacksTo(1).durability(1400)));
    public static final RegistryObject<Item> VERTICAL_GRIP = REGISTER.register("vertical_grip", () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.REDUCED_RECOIL), new Item.Properties().stacksTo(1).durability(1600)));

    public static final RegistryObject<Item> IRON_BAYONET = REGISTER.register("iron_bayonet", () -> new BayonetItem(UnderBarrel.create(GunModifiers.IRON_BAYONET_DAMAGE), new Item.Properties().stacksTo(1).durability(560), 1.5f, -3.0f));
    public static final RegistryObject<Item> ANTHRALITE_BAYONET = REGISTER.register("anthralite_bayonet", () -> new BayonetItem(UnderBarrel.create(GunModifiers.ANTHRALITE_BAYONET_DAMAGE), new Item.Properties().stacksTo(1).durability(512), 2.0f, -3.0f));
    public static final RegistryObject<Item> DIAMOND_BAYONET = REGISTER.register("diamond_bayonet", () -> new BayonetItem(UnderBarrel.create(GunModifiers.DIAMOND_BAYONET_DAMAGE), new Item.Properties().stacksTo(1).durability(1024), 3.0f, -3.0f));
    public static final RegistryObject<Item> NETHERITE_BAYONET = REGISTER.register("netherite_bayonet", () -> new BayonetItem(UnderBarrel.create(GunModifiers.NETHERITE_BAYONET_DAMAGE), new Item.Properties().stacksTo(1).durability(1500), 4.0f, -3.0f));

    //Magazines
    public static final RegistryObject<Item> EXTENDED_MAG = REGISTER.register("extended_mag", () -> new MagazineItem(Magazine.create(GunModifiers.EXTENDED_MAG_MODIFIER), new Item.Properties().stacksTo(1).durability(1700)));
    public static final RegistryObject<Item> SPEED_MAG = REGISTER.register("speed_mag", () -> new MagazineItem(Magazine.create(GunModifiers.SPEED_MAG_MODIFIER), new Item.Properties().stacksTo(1).durability(1500)));
    public static final RegistryObject<Item> PLUS_P_MAG = REGISTER.register("plus_p_mag", () -> new MagazineItem(Magazine.create(GunModifiers.INCREASED_DAMAGE, GunModifiers.PLUS_P_MAG), new Item.Properties().stacksTo(1).durability(900)));
    //ITEMS
    public static final RegistryObject<Item> REPAIR_KIT = REGISTER.register("repair_kit", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SCAMP_PACKAGE = REGISTER.register("scamp_package", () -> new ScampPackageItem(new Item.Properties().stacksTo(1)));
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
    public static final RegistryObject<Item> ZOMBIFIED_HORNLIN_SPAWN_EGG = REGISTER.register("zombified_hornlin_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.ZOMBIFIED_HORNLIN, 0xe67973, 0x9c3f69, new Item.Properties()));
    public static final RegistryObject<Item> BLUNDERER_SPAWN_EGG = REGISTER.register("blunderer_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.BLUNDERER, 0x32663c, 0x98a2a2, new Item.Properties()));


    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }


}
