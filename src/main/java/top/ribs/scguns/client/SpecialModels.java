package top.ribs.scguns.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum SpecialModels {
    MACERATOR_BASE("macerator/macerator"),
    MACERATOR_WHEEL_1("macerator/macerator_wheel_1"),
    MACERATOR_WHEEL_2("macerator/macerator_wheel_2"),
    MACERATOR_WHEEL_3("macerator/macerator_wheel_3"),
    MACERATOR_WHEEL_4("macerator/macerator_wheel_4"),


    MECHANICAL_PRESS_PRESS("mechanical_press/press"),

    COPPER_PISTOL("gun/copper_pistol"),
    COPPER_PISTOL_MAIN("copper_pistol/main"),
    COPPER_PISTOL_BOLT("copper_pistol/bolt"),
    COPPER_PISTOL_MAGAZINE("copper_pistol/magazine"),
    COPPER_PISTOL_SILENCER("copper_pistol/silencer"),
    COPPER_PISTOL_MUZZLE_BRAKE("copper_pistol/muzzle_brake"),
    COPPER_PISTOL_ADVANCED_SILENCER("copper_pistol/advanced_silencer"),
    COPPER_PISTOL_STOCK_LIGHT("copper_pistol/stock_light"),
    COPPER_PISTOL_STOCK_HEAVY("copper_pistol/stock_weighted"),
    COPPER_PISTOL_STOCK_WOODEN("copper_pistol/stock_wooden"),

///FLINTLOCK
    FLINTLOCK_PISTOL("gun/flintlock_pistol"),
    FLINTLOCK_PISTOL_MAIN("flintlock_pistol/main"),
    FLINTLOCK_PISTOL_HAMMER("flintlock_pistol/hammer"),
    FLINTLOCK_PISTOL_STOCK_WEIGHTED("flintlock_pistol/heavy_stock"),
    FLINTLOCK_PISTOL_STOCK_LIGHT("flintlock_pistol/light_stock"),
    FLINTLOCK_PISTOL_STOCK_WOODEN("flintlock_pistol/wooden_stock"),
///MUSKET
    MUSKET_MAIN("musket/main"),
    MUSKET_HAMMER("musket/hammer"),
    MUSKET_STOCK_WEIGHTED("musket/heavy_stock"),
    MUSKET_STOCK_LIGHT("musket/light_stock"),
    MUSKET_STOCK_WOODEN("musket/wooden_stock"),
    MUSKET_IRON_BAYONET("musket/iron_bayonet"),
    MUSKET_DIAMOND_BAYONET("musket/diamond_bayonet"),
    MUSKET_NETHERITE_BAYONET("musket/netherite_bayonet"),
    MUSKET_LIGHT_GRIP("musket/light_grip"),
    MUSKET_VERTICAL_GRIP("musket/vert_grip"),
    MUSKET_FLASH("musket/flash"),
///BLUNDERBUSS
    BLUNDERBUSS_MAIN("blunderbuss/main"),
    BLUNDERBUSS_HAMMER("blunderbuss/hammer"),
    BLUNDERBUSS_IRON_BAYONET("blunderbuss/iron_bayonet"),
    BLUNDERBUSS_DIAMOND_BAYONET("blunderbuss/diamond_bayonet"),
    BLUNDERBUSS_NETHERITE_BAYONET("blunderbuss/netherite_bayonet"),
    BLUNDERBUSS_LIGHT_GRIP("blunderbuss/light_grip"),
    BLUNDERBUSS_VERTICAL_GRIP("blunderbuss/vert_grip"),
///REPEATINGMUSKET
    REPEATING_MUSKET_MAIN("repeating_musket/main"),
    REPEATING_MUSKET_HAMMER("repeating_musket/hammer"),
    REPEATING_MUSKET_MAGAZINE("repeating_musket/magazine"),
    REPEATING_MUSKET_IRON_BAYONET("repeating_musket/iron_bayonet"),
    REPEATING_MUSKET_DIAMOND_BAYONET("repeating_musket/diamond_bayonet"),
    REPEATING_MUSKET_NETHERITE_BAYONET("repeating_musket/netherite_bayonet"),
    REPEATING_MUSKET_LIGHT_GRIP("repeating_musket/grip_light"),
    REPEATING_MUSKET_VERTICAL_GRIP("repeating_musket/grip_vert"),

    ///COPPERMAGNUM
    COPPER_MAGNUM_MAIN("copper_magnum/main"),
    COPPER_MAGNUM_BARREL("copper_magnum/barrel"),
    COPPER_MAGNUM_SILENCER("copper_magnum/silencer"),
    COPPER_MAGNUM_ADVANCED_SILENCER("copper_magnum/advanced_silencer"),
    COPPER_MAGNUM_MUZZLE_BRAKE("copper_magnum/muzzle_brake"),


    ///COPPER
    COPPER_SHOTGUN_MAIN("copper_shotgun/main"),
    COPPER_SHOTGUN_BARREL("copper_shotgun/barrel"),
    COPPER_SHOTGUN_STOCK_WEIGHTED("copper_shotgun/stock_weighted"),
    COPPER_SHOTGUN_STOCK_LIGHT("copper_shotgun/stock_light"),
    COPPER_SHOTGUN_STOCK_WOODEN("copper_shotgun/stock_wooden"),
    COPPER_SHOTGUN_SILENCER("copper_shotgun/silencer"),
    COPPER_SHOTGUN_ADVANCED_SILENCER("copper_shotgun/advanced_silencer"),
    COPPER_SHOTGUN_MUZZLE_BRAKE("copper_shotgun/muzzle_brake"),
    COPPER_SHOTGUN_GRIP_LIGHT("copper_shotgun/grip_light"),
    COPPER_SHOTGUN_GRIP_VERTICAL("copper_shotgun/grip_vertical"),
    COPPER_SHOTGUN_IRON_BAYONET("copper_shotgun/iron_bayonet"),
    COPPER_SHOTGUN_DIAMOND_BAYONET("copper_shotgun/diamond_bayonet"),
    COPPER_SHOTGUN_NETHERITE_BAYONET("copper_shotgun/netherite_bayonet"),

    //COPPERSMG
    COPPER_SMG("gun/copper_smg"),
    COPPER_SMG_MAIN("copper_smg/main"),
    COPPER_SMG_BOLT("copper_smg/bolt"),
    COPPER_SMG_BARREL("copper_smg/barrel"),
    COPPER_SMG_STOCK_WEIGHTED("copper_smg/stock_weighted"),
    COPPER_SMG_STOCK_LIGHT("copper_smg/stock_light"),
    COPPER_SMG_STOCK_WOODEN("copper_smg/stock_wooden"),
    COPPER_SMG_SILENCER("copper_smg/silencer"),
    COPPER_SMG_ADVANCED_SILENCER("copper_smg/advanced_silencer"),
    COPPER_SMG_MUZZLE_BRAKE("copper_smg/muzzle_brake"),
    COPPER_SMG_STANDARD_MAG("copper_smg/stan_mag"),
    COPPER_SMG_EXTENDED_MAG("copper_smg/ext_mag"),
///COPPERRIFLE
    COPPER_RIFLE("gun/copper_rifle"),
    COPPER_RIFLE_MAIN("copper_rifle/main"),
    COPPER_RIFLE_EJECTOR("copper_rifle/ejector"),
    COPPER_RIFLE_STOCK_LIGHT("copper_rifle/stock_light"),
    COPPER_RIFLE_STOCK_WEIGHTED("copper_rifle/stock_weighted"),
    COPPER_RIFLE_STOCK_WOODEN("copper_rifle/stock_wooden"),
    COPPER_RIFLE_SILENCER("copper_rifle/silencer"),
    COPPER_RIFLE_ADVANCED_SILENCER("copper_rifle/advanced_silencer"),
    COPPER_RIFLE_MUZZLE_BRAKE("copper_rifle/muzzle_brake"),
    COPPER_RIFLE_VERTICAL_GRIP("copper_rifle/vertical_grip"),
    COPPER_RIFLE_LIGHT_GRIP("copper_rifle/light_grip"),
    COPPER_RIFLE_IRON_BAYONET("copper_rifle/iron_bayonet"),
    COPPER_RIFLE_DIAMOND_BAYONET("copper_rifle/diamond_bayonet"),
    COPPER_RIFLE_NETHERITE_BAYONET("copper_rifle/netherite_bayonet"),
    COPPER_RIFLE_STANDARD_MAG("copper_rifle/stan_mag"),
    COPPER_RIFLE_EXTENDED_MAG("copper_rifle/ext_mag"),
////IRONCARABINE
    IRON_CARABINE_MAIN("iron_carabine/main"),
    IRON_CARABINE_SIGHTS("iron_carabine/sights"),
    IRON_CARABINE_NO_SIGHTS("iron_carabine/no_sights"),
    IRON_CARABINE_BOLT("iron_carabine/bolt"),
    IRON_CARABINE_STOCK_LIGHT("iron_carabine/light_stock"),
    IRON_CARABINE_STOCK_HEAVY("iron_carabine/heavy_stock"),
    IRON_CARABINE_STOCK_WOODEN("iron_carabine/wooden_stock"),
    IRON_CARABINE_SILENCER("iron_carabine/silencer"),
    IRON_CARABINE_ADVANCED_SILENCER("iron_carabine/advanced_silencer"),
    IRON_CARABINE_MUZZLE_BRAKE("iron_carabine/muzzle_brake"),
    IRON_CARABINE_GRIP_LIGHT("iron_carabine/light_grip"),
    IRON_CARABINE_GRIP_VERTICAL("iron_carabine/tact_grip"),
    IRON_CARABINE_IRON_BAYONET("iron_carabine/iron_bayonet"),
    IRON_CARABINE_DIAMOND_BAYONET("iron_carabine/diamond_bayonet"),
    IRON_CARABINE_NETHERITE_BAYONET("iron_carabine/netherite_bayonet"),
    IRON_CARABINE_STANDARD_MAG("iron_carabine/stan_mag"),
    IRON_CARABINE_EXTENDED_MAG("iron_carabine/ext_mag"),
    IRON_CARABINE_PLUS_P_MAG("iron_carabine/plus_mag"),
    IRON_CARABINE_SPEED_MAG("iron_carabine/speed_mag"),

    IRON_SMG_MAIN("iron_smg/main"),
    IRON_SMG_SIGHTS("iron_smg/sights"),
    IRON_SMG_NO_SIGHTS("iron_smg/no_sights"),
    IRON_SMG_BOLT("iron_smg/bolt"),
    IRON_SMG_STOCK_LIGHT("iron_smg/light_stock"),
    IRON_SMG_STOCK_HEAVY("iron_smg/heavy_stock"),
    IRON_SMG_STOCK_WOODEN("iron_smg/wooden_stock"),
    IRON_SMG_SILENCER("iron_smg/silencer"),
    IRON_SMG_ADVANCED_SILENCER("iron_smg/advanced_silencer"),
    IRON_SMG_MUZZLE_BRAKE("iron_smg/muzzle_brake"),
    IRON_SMG_STANDARD_MAG("iron_smg/stan_mag"),
    IRON_SMG_EXTENDED_MAG("iron_smg/ext_mag"),
///DEFENDERPISTOL
    IRON_DEFENDER_MAIN("defender_pistol/main"),
    IRON_DEFENDER_RECEIVER("defender_pistol/receiver"),
    IRON_DEFENDER_SILENCER("defender_pistol/silencer"),
    IRON_DEFENDER_ADVANCED_SILENCER("defender_pistol/advanced_silencer"),
    IRON_DEFENDER_MUZZLE_BRAKE("defender_pistol/muzzle_brake"),
    IRON_DEFENDER_EXTENDED_MAG("defender_pistol/ext_mag"),
    IRON_DEFENDER_STANDARD_MAG("defender_pistol/stan_mag"),

    ///IRONSPEAR
    IRON_SPEAR_MAIN("iron_spear/main"),
    IRON_SPEAR_SIGHTS("iron_spear/sights"),
    IRON_SPEAR_NO_SIGHTS("iron_spear/no_sights"),
    IRON_SPEAR_BOLT("iron_spear/bolt"),
    IRON_SPEAR_STOCK_LIGHT("iron_spear/light_stock"),
    IRON_SPEAR_STOCK_HEAVY("iron_spear/heavy_stock"),
    IRON_SPEAR_STOCK_WOODEN("iron_spear/wooden_stock"),
    IRON_SPEAR_SILENCER("iron_spear/silencer"),
    IRON_SPEAR_ADVANCED_SILENCER("iron_spear/advanced_silencer"),
    IRON_SPEAR_MUZZLE_BRAKE("iron_spear/muzzle_brake"),
    IRON_SPEAR_TACT_GRIP("iron_spear/tact_grip"),
    IRON_SPEAR_LIGHT_GRIP("iron_spear/light_grip"),
    IRON_SPEAR_IRON_BAYONET("iron_spear/iron_bayonet"),
    IRON_SPEAR_DIAMOND_BAYONET("iron_spear/diamond_bayonet"),
    IRON_SPEAR_NETHERITE_BAYONET("iron_spear/netherite_bayonet"),
    IRON_SPEAR_STANDARD_MAG("iron_spear/stan_mag"),
    IRON_SPEAR_EXTENDED_MAG("iron_spear/ext_mag"),
///COMBATSHOTGUN
    COMBAT_SHOTGUN_MAIN("combat_shotgun/main"),
    COMBAT_SHOTGUN_SIGHTS("combat_shotgun/sights"),
    COMBAT_SHOTGUN_NO_SIGHTS("combat_shotgun/no_sights"),
    COMBAT_SHOTGUN_BOLT("combat_shotgun/bolt"),
    COMBAT_SHOTGUN_STOCK_LIGHT("combat_shotgun/light_stock"),
    COMBAT_SHOTGUN_STOCK_HEAVY("combat_shotgun/heavy_stock"),
    COMBAT_SHOTGUN_STOCK_WOODEN("combat_shotgun/wooden_stock"),
    COMBAT_SHOTGUN_SILENCER("combat_shotgun/silencer"),
    COMBAT_SHOTGUN_ADVANCED_SILENCER("combat_shotgun/advanced_silencer"),
    COMBAT_SHOTGUN_MUZZLE_BRAKE("combat_shotgun/muzzle_brake"),
    COMBAT_SHOTGUN_GRIP_LIGHT("combat_shotgun/light_grip"),
    COMBAT_SHOTGUN_GRIP_VERTICAL("combat_shotgun/tact_grip"),
    COMBAT_SHOTGUN_IRON_BAYONET("combat_shotgun/iron_bayonet"),
    COMBAT_SHOTGUN_DIAMOND_BAYONET("combat_shotgun/diamond_bayonet"),
    COMBAT_SHOTGUN_NETHERITE_BAYONET("combat_shotgun/netherite_bayonet"),
    COMBAT_SHOTGUN_STANDARD_MAG("combat_shotgun/stan_mag"),
    COMBAT_SHOTGUN_EXTENDED_MAG("combat_shotgun/ext_mag"),
    ///GYROJETPISTOL
    GYROJET_PISTOL_MAIN("gyrojet_pistol/main"),
    GYROJET_PISTOL_STOCK_HEAVY("gyrojet_pistol/heavy_stock"),
    GYROJET_PISTOL_STOCK_LIGHT("gyrojet_pistol/light_stock"),
    GYROJET_PISTOL_STOCK_WOODEN("gyrojet_pistol/wooden_stock"),
    GYROJET_PISTOL_FLAME_RIGHT("gyrojet_pistol/flame_right"),
    GYROJET_PISTOL_FLAME_LEFT("gyrojet_pistol/flame_left"),
    ///ROCKETRIFLE
    ROCKET_RIFLE_MAIN("rocket_rifle/main"),
    ROCKET_RIFLE_STOCK_HEAVY("rocket_rifle/heavy_stock"),
    ROCKET_RIFLE_STOCK_LIGHT("rocket_rifle/light_stock"),
    ROCKET_RIFLE_STOCK_WOODEN("rocket_rifle/wooden_stock"),
    ROCKET_RIFLE_GRIP_LIGHT("rocket_rifle/light_grip"),
    ROCKET_RIFLE_GRIP_VERTICAL("rocket_rifle/tact_grip"),
    ROCKET_RIFLE_IRON_BAYONET("rocket_rifle/iron_bayonet"),
    ROCKET_RIFLE_DIAMOND_BAYONET("rocket_rifle/diamond_bayonet"),
    ROCKET_RIFLE_NETHERITE_BAYONET("rocket_rifle/netherite_bayonet"),





    FLAME("flame");

    /**
     * The location of an item model in the [MOD_ID]/models/special/[NAME] folder
     */
    private final ResourceLocation modelLocation;

    /**
     * Cached model
     */
    private BakedModel cachedModel;

    /**
     * Sets the model's location
     *
     * @param modelName name of the model file
     */
    SpecialModels(String modelName)
    {
        this.modelLocation = new ResourceLocation(Reference.MOD_ID, "special/" + modelName);
    }

    /**
     * Gets the model
     *
     * @return isolated model
     */
    public BakedModel getModel()
    {
        if(this.cachedModel == null)
        {
            this.cachedModel = Minecraft.getInstance().getModelManager().getModel(this.modelLocation);
        }
        return this.cachedModel;
    }

    /**
     * Registers the special models into the Forge Model Bakery. This is only called once on the
     * load of the game.
     */
    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event)
    {
        for(SpecialModels model : values())
        {
            event.register(model.modelLocation);
        }
    }

    /**
     * Clears the cached BakedModel since it's been rebuilt. This is needed since the models may
     * have changed when a resource pack was applied, or if resources are reloaded.
     */
    @SubscribeEvent
    public static void onBake(ModelEvent.BakingCompleted event)
    {
        for(SpecialModels model : values())
        {
            model.cachedModel = null;
        }
    }
}