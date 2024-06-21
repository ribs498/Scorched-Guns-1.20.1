package top.ribs.scguns.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Reference.MOD_ID);

    public static final RegistryObject<SoundEvent> COPPER_GUN_JAM = register("item.copper_smg.copper_jam");
    public static final RegistryObject<SoundEvent> COPPER_SMG_FIRE = register("item.copper_smg.fire");
    public static final RegistryObject<SoundEvent> COPPER_SMG_SILENCED_FIRE = register("item.copper_smg.silenced_fire");
    public static final RegistryObject<SoundEvent> COPPER_SMG_ENCHANTED_FIRE = register("item.copper_smg.enchanted_fire");
    public static final RegistryObject<SoundEvent> COPPER_SHOTGUN_FIRE = register("item.copper_shotgun.fire");
    public static final RegistryObject<SoundEvent> COPPER_SHOTGUN_SILENCED_FIRE = register("item.copper_shotgun.silenced_fire");
    public static final RegistryObject<SoundEvent> COPPER_SHOTGUN_ENCHANTED_FIRE = register("item.copper_shotgun.enchanted_fire");
    public static final RegistryObject<SoundEvent> COPPER_MAGNUM_FIRE = register("item.copper_magnum.fire");
    public static final RegistryObject<SoundEvent> COPPER_MAGNUM_SILENCED_FIRE = register("item.copper_magnum.silenced_fire");
    public static final RegistryObject<SoundEvent> COPPER_RIFLE_FIRE = register("item.copper_rifle.fire");
    public static final RegistryObject<SoundEvent> COPPER_RIFLE_SILENCED_FIRE = register("item.copper_rifle.silenced_fire");
    public static final RegistryObject<SoundEvent> COPPER_RIFLE_ENCHANTED_FIRE = register("item.copper_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> COPPER_RIFLE_COCK = register("item.copper_rifle.cock");
    public static final RegistryObject<SoundEvent> COPPER_PISTOL_FIRE = register("item.copper_pistol.fire");
    public static final RegistryObject<SoundEvent> IRON_SMG_FIRE = register("item.iron_smg.fire");
    public static final RegistryObject<SoundEvent> GYROJET_FIRE = register("item.gyrojet.fire");
    public static final RegistryObject<SoundEvent> IRON_RIFLE_FIRE = register("item.iron_rifle.fire");
    public static final RegistryObject<SoundEvent> IRON_RIFLE_ENCHANTED_FIRE = register("item.iron_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> BLACKPOWDER_FIRE = register("item.blackpowder.fire");

    public static final RegistryObject<SoundEvent> IRON_PISTOL_FIRE = register("item.iron_pistol.fire");
    public static final RegistryObject<SoundEvent> ITEM_PISTOL_RELOAD = register("item.pistol.reload");
    public static final RegistryObject<SoundEvent> ITEM_PISTOL_COCK = register("item.pistol.cock");
    public static final RegistryObject<SoundEvent> ITEM_GRENADE_PIN = register("item.grenade.pin");
    public static final RegistryObject<SoundEvent> ENTITY_STUN_GRENADE_EXPLOSION = register("entity.stun_grenade.explosion");
    public static final RegistryObject<SoundEvent> ENTITY_STUN_GRENADE_RING = register("entity.stun_grenade.ring");
    public static final RegistryObject<SoundEvent> UI_WEAPON_ATTACH = register("ui.weapon.attach");

    private static RegistryObject<SoundEvent> register(String key) {
        return REGISTER.register(key, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Reference.MOD_ID, key)));
    }
}
