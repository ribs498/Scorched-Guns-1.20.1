package top.ribs.scguns.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Reference.MOD_ID);


    public static final RegistryObject<SoundEvent> MASS_PRODUCTION = register("mass_production");
    public static final RegistryObject<SoundEvent> MASS_DESTRUCTION = register("mass_destruction");
    public static final RegistryObject<SoundEvent> MASS_DESTRUCTION_EXTENDED = register("mass_destruction_extended");
    public static final RegistryObject<SoundEvent> COPPER_GUN_JAM = register("item.rusty_gnat.copper_jam");
    public static final RegistryObject<SoundEvent> BLACKPOWDER_FIRE = register("item.blackpowder.fire");
    public static final RegistryObject<SoundEvent> AIRGUN_FIRE = register("item.airgun.fire");
    public static final RegistryObject<SoundEvent> BRUISER_SILENCED_FIRE = register("item.bruiser.silenced_fire");
    public static final RegistryObject<SoundEvent> MAKESHIFT_RIFLE_FIRE = register("item.makeshift_rifle.fire");
    public static final RegistryObject<SoundEvent> COWBOY_FIRE = register("item.cowboy.fire");
    public static final RegistryObject<SoundEvent> SHOCK_FIRE = register("item.shock.fire");
    public static final RegistryObject<SoundEvent> SHULKER_FIRE = register("item.shulker.fire");
    public static final RegistryObject<SoundEvent> SCULK_FIRE = register("item.sculk.fire");
    public static final RegistryObject<SoundEvent> SCORCHED_SNIPER_FIRE = register("item.scorched_sniper.fire");
    public static final RegistryObject<SoundEvent> SCORCHED_RIFLE_FIRE = register("item.scorched_rifle.fire");
    public static final RegistryObject<SoundEvent> RUSTY_GNAT_FIRE = register("item.rusty_gnat.fire");
    public static final RegistryObject<SoundEvent> RUSTY_GNAT_SILENCED_FIRE = register("item.rusty_gnat.silenced_fire");
    public static final RegistryObject<SoundEvent> BRASS_SHOTGUN_FIRE = register("item.brass_shotgun.fire");
    public static final RegistryObject<SoundEvent> BOOMSTICK_FIRE = register("item.boomstick.fire");
    public static final RegistryObject<SoundEvent> BOOMSTICK_SILENCED_FIRE = register("item.boomstick.silenced_fire");
    public static final RegistryObject<SoundEvent> COMBAT_SHOTGUN_FIRE = register("item.combat_shotgun.fire");
    public static final RegistryObject<SoundEvent> COMBAT_SHOTGUN_SILENCED_FIRE = register("item.combat_shotgun.silenced_fire");
    public static final RegistryObject<SoundEvent> BRUISER_FIRE = register("item.bruiser.fire");

    public static final RegistryObject<SoundEvent> MAKESHIFT_RIFLE_SILENCED_FIRE = register("item.makeshift_rifle.silenced_fire");
    public static final RegistryObject<SoundEvent> MAKESHIFT_RIFLE_COCK = register("item.makeshift_rifle.cock");
    public static final RegistryObject<SoundEvent> SCRAPPER_FIRE = register("item.scrapper.fire");
    public static final RegistryObject<SoundEvent> GREASER_SMG_FIRE = register("item.greaser_smg.fire");
    public static final RegistryObject<SoundEvent> GYROJET_FIRE = register("item.gyrojet.fire");
    public static final RegistryObject<SoundEvent> IRON_RIFLE_FIRE = register("item.iron_rifle.fire");

    public static final RegistryObject<SoundEvent> HEAVY_RIFLE_FIRE = register("item.heavy_rifle.fire");
    public static final RegistryObject<SoundEvent> BRASS_PISTOL_FIRE = register("item.brass_pistol.fire");
    public static final RegistryObject<SoundEvent> PLASMA_FIRE = register("item.plasma.fire");
    public static final RegistryObject<SoundEvent> GAUSS_FIRE = register("item.gauss.fire");
    public static final RegistryObject<SoundEvent> ROCKET_FIRE = register("item.rocket.fire");
    public static final RegistryObject<SoundEvent> ROCKET_RIFLE_FIRE = register("item.rocket_rifle.fire");
    public static final RegistryObject<SoundEvent> BRASS_REVOLVER = register("item.brass_revolver.fire");
    public static final RegistryObject<SoundEvent> GAUSS_PRE_FIRE = register("item.gauss.pre_fire");
    public static final RegistryObject<SoundEvent> GAUSS_RELOAD = register("item.gauss.reload");
    public static final RegistryObject<SoundEvent> RAYGUN_FIRE = register("item.raygun.fire");
    public static final RegistryObject<SoundEvent> FLAMETHROWER_FIRE = register("item.flamethrower.fire");
    public static final RegistryObject<SoundEvent> FLAMETHROWER_FIRE_2 = register("item.flamethrower.fire_2");
    public static final RegistryObject<SoundEvent> FLAMETHROWER_PRE_FIRE = register("item.flamethrower.pre_fire");
    public static final RegistryObject<SoundEvent> FLAMETHROWER_RELOAD = register("item.flamethrower.reload");
    public static final RegistryObject<SoundEvent> LASER_FIRE = register("item.laser.fire");
    public static final RegistryObject<SoundEvent> HEAVIER_FIRE = register("item.heavier_rifle.fire");
    public static final RegistryObject<SoundEvent> IRON_PISTOL_FIRE = register("item.iron_pistol.fire");
    public static final RegistryObject<SoundEvent> ITEM_PISTOL_RELOAD = register("item.pistol.reload");
    public static final RegistryObject<SoundEvent> ITEM_PISTOL_COCK = register("item.pistol.cock");
    public static final RegistryObject<SoundEvent> ITEM_GRENADE_PIN = register("item.grenade.pin");
    public static final RegistryObject<SoundEvent> ENTITY_STUN_GRENADE_EXPLOSION = register("entity.stun_grenade.explosion");
    public static final RegistryObject<SoundEvent> ENTITY_STUN_GRENADE_RING = register("entity.stun_grenade.ring");
    public static final RegistryObject<SoundEvent> UI_WEAPON_ATTACH = register("ui.weapon.attach");

    //bullet flyby sounds
    public static final RegistryObject<SoundEvent> BULLET_FLYBY = register("bullet.flyby1"); //TODO: Set this to an actual sound later.

    private static RegistryObject<SoundEvent> register(String key) {
        return REGISTER.register(key, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Reference.MOD_ID, key)));
    }
}
