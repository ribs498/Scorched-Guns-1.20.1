package top.ribs.scguns.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.block.PrimedNitroKeg;
import top.ribs.scguns.entity.block.PrimedPowderKeg;
import top.ribs.scguns.entity.monster.*;
import top.ribs.scguns.entity.projectile.BrassBoltEntity;
import top.ribs.scguns.entity.projectile.*;
import top.ribs.scguns.entity.projectile.turret.TurretProjectileEntity;
import top.ribs.scguns.entity.throwable.*;

import java.util.function.BiFunction;

/**
 * Author: MrCrayfish
 */
public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Reference.MOD_ID);
    public static final RegistryObject<EntityType<PrimedPowderKeg>> PRIMED_POWDER_KEG = REGISTER.register("primed_powder_keg",
            () -> EntityType.Builder.<PrimedPowderKeg>of(PrimedPowderKeg::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("primed_powder_keg"));
    public static final RegistryObject<EntityType<PrimedNitroKeg>> PRIMED_NITRO_KEG = REGISTER.register("primed_nitro_keg",
            () -> EntityType.Builder.<PrimedNitroKeg>of(PrimedNitroKeg::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("primed_nitro_keg"));
    public static final RegistryObject<EntityType<TurretProjectileEntity>> TURRET_PROJECTILE = REGISTER.register("basic_turret", () ->
            EntityType.Builder.<TurretProjectileEntity>of(TurretProjectileEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).build("basic_turret"));

    public static final RegistryObject<EntityType<ProjectileEntity>> PROJECTILE = registerProjectile("projectile", ProjectileEntity::new);
    public static final RegistryObject<EntityType<BearPackShellProjectileEntity>> BEARPACK_SHELL_PROJECTILE = registerBasic("bearpack_shell_projectile", BearPackShellProjectileEntity::new);
    public static final RegistryObject<EntityType<OsborneSlugProjectileEntity>> OSBORNE_SLUG_PROJECTILE = registerBasic("osborne_slug_projectile", OsborneSlugProjectileEntity::new);
    public static final RegistryObject<EntityType<PlasmaProjectileEntity>> PLASMA_PROJECTILE = registerBasic("plasma_projectile", PlasmaProjectileEntity::new);
    public static final RegistryObject<EntityType<RamrodProjectileEntity>> RAMROD_PROJECTILE = registerBasic("ramrod_projectile", RamrodProjectileEntity::new);
    public static final RegistryObject<EntityType<HogRoundProjectileEntity>> HOG_ROUND_PROJECTILE = registerBasic("hog_round_projectile", HogRoundProjectileEntity::new);
    public static final RegistryObject<EntityType<BeowulfProjectileEntity>> BEOWULF_PROJECTILE = registerBasic("beowulf_projectile", BeowulfProjectileEntity::new);
    public static final RegistryObject<EntityType<BlazeRodProjectileEntity>> BLAZE_ROD_PROJECTILE = registerBasic("blaze_rod_projectile", BlazeRodProjectileEntity::new);
    public static final RegistryObject<EntityType<BasicBulletProjectileEntity>> BASIC_BULLET_PROJECTILE = registerBasic("basic_bullet_projectile", BasicBulletProjectileEntity::new);
    public static final RegistryObject<EntityType<HardenedBulletProjectileEntity>> HARDENED_BULLET_PROJECTILE = registerBasic("hardened_bullet_projectile", HardenedBulletProjectileEntity::new);
    public static final RegistryObject<EntityType<BuckshotProjectileEntity>> BUCKSHOT_PROJECTILE = registerBasic("buckshot_projectile", BuckshotProjectileEntity::new);
    public static final RegistryObject<EntityType<FireRoundEntity>> FIRE_ROUND_PROJECTILE = registerBasic("fire_round_projectile", FireRoundEntity::new);
    public static final RegistryObject<EntityType<GrenadeEntity>> GRENADE = registerBasic("grenade", GrenadeEntity::new);
    public static final RegistryObject<EntityType<RocketEntity>> ROCKET = registerBasic("rocket", RocketEntity::new);
    public static final RegistryObject<EntityType<MicroJetEntity>> MICROJET = registerBasic("microjet", MicroJetEntity::new);
    public static final RegistryObject<EntityType<ShulkshotProjectileEntity>> SHULKSHOT = registerBasic("shulkshot_projectile", ShulkshotProjectileEntity::new);
    public static final RegistryObject<EntityType<SculkCellEntity>> SCULK_CELL = registerBasic("sculk_cell", SculkCellEntity::new);
    public static final RegistryObject<EntityType<SyringeProjectileEntity>> SYRINGE_PROJECTILE = registerBasic("syringe_projectile", SyringeProjectileEntity::new);
    public static final RegistryObject<EntityType<KrahgRoundProjectileEntity>> KRAHG_ROUND_PROJECTILE = registerBasic("krahg_round_projectile", KrahgRoundProjectileEntity::new);
    public static final RegistryObject<EntityType<AdvancedRoundProjectileEntity>> ADVANCED_ROUND_PROJECTILE = registerBasic("advanced_round_projectile", AdvancedRoundProjectileEntity::new);
    public static final RegistryObject<EntityType<GibbsRoundProjectileEntity>> GIBBS_ROUND_PROJECTILE = registerBasic("gibbs_round_projectile", GibbsRoundProjectileEntity::new);
    public static final RegistryObject<EntityType<ThrowableGrenadeEntity>> THROWABLE_GRENADE = registerBasic("throwable_grenade", ThrowableGrenadeEntity::new);
    public static final RegistryObject<EntityType<ThrowableStunGrenadeEntity>> THROWABLE_STUN_GRENADE = registerBasic("throwable_stun_grenade", ThrowableStunGrenadeEntity::new);
    public static final RegistryObject<EntityType<ThrowableMolotovCocktailEntity>> THROWABLE_MOLOTOV_COCKTAIL = registerBasic("throwable_molotov_cocktail", ThrowableMolotovCocktailEntity::new);
    public static final RegistryObject<EntityType<ThrowableGasGrenadeEntity>> THROWABLE_GAS_GRENADE = registerBasic("throwable_gas_grenade", ThrowableGasGrenadeEntity::new);

    public static final RegistryObject<EntityType<ThrowableChokeBombEntity>> THROWABLE_CHOKE_BOMB = registerBasic("throwable_choke_bomb", ThrowableChokeBombEntity::new);
    public static final RegistryObject<EntityType<ThrowableSwarmBombEntity>> THROWABLE_SWARM_BOMB = registerBasic("throwable_swarm_bomb", ThrowableSwarmBombEntity::new);

    /* Mobs */
    public static final RegistryObject<EntityType<CogMinionEntity>> COG_MINION = REGISTER.register("cog_minion", () -> EntityType.Builder.of(CogMinionEntity::new, MobCategory.MONSTER).sized(0.8F, 2.0F).build("cog_minion"));
    public static final RegistryObject<EntityType<CogKnightEntity>> COG_KNIGHT = REGISTER.register("cog_knight", () -> EntityType.Builder.of(CogKnightEntity::new, MobCategory.MONSTER).sized(0.8F, 2.2F).build("cog_knight"));
    public static final RegistryObject<EntityType<SkyCarrierEntity>> SKY_CARRIER = REGISTER.register("sky_carrier", () -> EntityType.Builder.of(SkyCarrierEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("sky_carrier"));
    public static final RegistryObject<EntityType<HiveEntity>> HIVE = REGISTER.register("hive", () -> EntityType.Builder.of(HiveEntity::new, MobCategory.MONSTER).sized(0.8F, 2.0F).build("hive"));
    public static final RegistryObject<EntityType<SwarmEntity>> SWARM = REGISTER.register("swarm", () -> EntityType.Builder.of(SwarmEntity::new, MobCategory.MONSTER).sized(0.8F, 2.0F).build("swarm"));
    public static final RegistryObject<EntityType<RedcoatEntity>> REDCOAT = REGISTER.register("redcoat", () -> EntityType.Builder.of(RedcoatEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).build("redcoat"));
    public static final RegistryObject<EntityType<SupplyScampEntity>> SUPPLY_SCAMP = REGISTER.register("supply_scamp", () -> EntityType.Builder.of(SupplyScampEntity::new, MobCategory.CREATURE).sized(1.0F, 1.3F).build("supply_scamp"));
    public static final RegistryObject<EntityType<DissidentEntity>> DISSIDENT = REGISTER.register("dissident", () -> EntityType.Builder.of(DissidentEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("dissident"));
    public static final RegistryObject<EntityType<HornlinEntity>> HORNLIN = REGISTER.register("hornlin", () -> EntityType.Builder.of(HornlinEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("hornlin"));
    public static final RegistryObject<EntityType<ZombifiedHornlinEntity>> ZOMBIFIED_HORNLIN = REGISTER.register("zombified_hornlin", () -> EntityType.Builder.of(ZombifiedHornlinEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("zombified_hornlin"));
    public static final RegistryObject<EntityType<BlundererEntity>> BLUNDERER = REGISTER.register("blunderer", () -> EntityType.Builder.of(BlundererEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("blunderer"));
    public static final RegistryObject<EntityType<BrassBoltEntity>> BRASS_BOLT = REGISTER.register("brass_bolt", () ->
            EntityType.Builder.<BrassBoltEntity>of(BrassBoltEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .setTrackingRange(64)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("brass_bolt"));

    private static <T extends Entity> RegistryObject<EntityType<T>> registerBasic(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(100)
                .setUpdateInterval(1)
                .noSummon()
                .fireImmune()
                .noSave()
                .setShouldReceiveVelocityUpdates(true).build(id));
    }

    /**
     * Entity registration that prevents the entity from being sent and tracked by clients. Projectiles
     * are rendered separately from Minecraft's entity rendering system and their logic is handled
     * exclusively by the server, why send them to the client. Projectiles also have very short time
     * in the world and are spawned many times a tick. There is no reason to send unnecessary packets
     * when it can be avoided to drastically improve the performance of the game.
     *
     * @param id       the id of the projectile
     * @param function the factory to spawn the projectile for the server
     * @param <T>      an entity that is a projectile entity
     * @return A registry object containing the new entity type
     */
    private static <T extends ProjectileEntity> RegistryObject<EntityType<T>> registerProjectile(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(0)
                .noSummon()
                .fireImmune()
                .noSave()
                .setShouldReceiveVelocityUpdates(false)
                .setCustomClientFactory((spawnEntity, world) -> null)
                .build(id));
    }
}
