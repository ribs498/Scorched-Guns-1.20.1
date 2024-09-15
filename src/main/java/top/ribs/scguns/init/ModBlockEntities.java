package top.ribs.scguns.init;

import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;
import top.ribs.scguns.blockentity.*;
import top.ribs.scguns.common.Gun;


public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "scguns");
    public static final RegistryObject<BlockEntityType<GunShelfBlockEntity>> GUN_SHELF_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("gun_shelf", () ->
                    BlockEntityType.Builder.of(GunShelfBlockEntity::new,
                            ModBlocks.GUN_SHELF.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdvancedComposterBlockEntity>> ADVANCED_COMPOSTER =
            BLOCK_ENTITIES.register("advanced_composter", () ->
                    BlockEntityType.Builder.of(AdvancedComposterBlockEntity::new,
                            ModBlocks.ADVANCED_COMPOSTER.get()).build(null));
    public static final RegistryObject<BlockEntityType<PowderKegBlockEntity>> POWDER_KEG =
            BLOCK_ENTITIES.register("powder_keg", () ->
                    BlockEntityType.Builder.of(PowderKegBlockEntity::new,
                            ModBlocks.POWDER_KEG.get()).build(null));
    public static final RegistryObject<BlockEntityType<NitroKegBlockEntity>> NITRO_KEG =
            BLOCK_ENTITIES.register("nitro_keg", () ->
                    BlockEntityType.Builder.of(NitroKegBlockEntity::new,
                            ModBlocks.NITRO_KEG.get()).build(null));


    public static final RegistryObject<BlockEntityType<GunBenchBlockEntity>> GUN_BENCH =
            BLOCK_ENTITIES.register("gun_bench", () ->
                    BlockEntityType.Builder.of(GunBenchBlockEntity::new,
                            ModBlocks.GUN_BENCH.get()).build(null));
    public static final RegistryObject<BlockEntityType<CryoniterBlockEntity>> CRYONITER =
            BLOCK_ENTITIES.register("cryoniter", () ->
                    BlockEntityType.Builder.of(CryoniterBlockEntity::new,
                            ModBlocks.CRYONITER.get()).build(null));
    public static final RegistryObject<BlockEntityType<ThermolithBlockEntity>> THERMOLITH =
            BLOCK_ENTITIES.register("thermolith", () ->
                    BlockEntityType.Builder.of(ThermolithBlockEntity::new,
                            ModBlocks.THERMOLITH.get()).build(null));

    public static final RegistryObject<BlockEntityType<PolarGeneratorBlockEntity>> POLAR_GENERATOR =
            BLOCK_ENTITIES.register("polar_generator", () ->
                    BlockEntityType.Builder.of(PolarGeneratorBlockEntity::new,
                            ModBlocks.POLAR_GENERATOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<PenetratorBlockEntity>> PENETRATOR =
            BLOCK_ENTITIES.register("penetrator", () ->
                    BlockEntityType.Builder.of(PenetratorBlockEntity::new,
                            ModBlocks.PENETRATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<MaceratorBlockEntity>> MACERATOR =
            BLOCK_ENTITIES.register("macerator", () ->
                    BlockEntityType.Builder.of(MaceratorBlockEntity::new,
                            ModBlocks.MACERATOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<PoweredMaceratorBlockEntity>> POWERED_MACERATOR =
            BLOCK_ENTITIES.register("powered_macerator", () ->
                    BlockEntityType.Builder.of(PoweredMaceratorBlockEntity::new,
                            ModBlocks.POWERED_MACERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<MechanicalPressBlockEntity>> MECHANICAL_PRESS =
            BLOCK_ENTITIES.register("mechanical_press", () ->
                    BlockEntityType.Builder.of(MechanicalPressBlockEntity::new,
                            ModBlocks.MECHANICAL_PRESS.get()).build(null));
    public static final RegistryObject<BlockEntityType<PoweredMechanicalPressBlockEntity>> POWERED_MECHANICAL_PRESS =
            BLOCK_ENTITIES.register("powered_mechanical_press", () ->
                    BlockEntityType.Builder.of(PoweredMechanicalPressBlockEntity::new,
                            ModBlocks.POWERED_MECHANICAL_PRESS.get()).build(null));
    public static final RegistryObject<BlockEntityType<LightningBatteryBlockEntity>> LIGHTNING_BATTERY =
            BLOCK_ENTITIES.register("lightning_battery", () ->
                    BlockEntityType.Builder.of(LightningBatteryBlockEntity::new,
                            ModBlocks.LIGHTNING_BATTERY.get()).build(null));
    public static final RegistryObject<BlockEntityType<BasicTurretBlockEntity>> BASIC_TURRET =
            BLOCK_ENTITIES.register("basic_turret", () ->
                    BlockEntityType.Builder.of(BasicTurretBlockEntity::new,
                            ModBlocks.BASIC_TURRET.get()).build(null));

    public static final RegistryObject<BlockEntityType<AutoTurretBlockEntity>> AUTO_TURRET =
            BLOCK_ENTITIES.register("auto_turret", () ->
                    BlockEntityType.Builder.of(AutoTurretBlockEntity::new,
                            ModBlocks.AUTO_TURRET.get()).build(null));
    public static final RegistryObject<BlockEntityType<ShotgunTurretBlockEntity>> SHOTGUN_TURRET =
            BLOCK_ENTITIES.register("shotgun_turret", () ->
                    BlockEntityType.Builder.of(ShotgunTurretBlockEntity::new,
                            ModBlocks.SHOTGUN_TURRET.get()).build(null));

    public static final RegistryObject<BlockEntityType<ShellCatcherModuleBlockEntity>> SHELL_CATCHER_MODULE =
            BLOCK_ENTITIES.register("shell_catcher_module", () ->
                    BlockEntityType.Builder.of(ShellCatcherModuleBlockEntity::new,
                            ModBlocks.SHELL_CATCHER_TURRET_MODULE.get()).build(null));
    public static final RegistryObject<BlockEntityType<AmmoModuleBlockEntity>> AMMO_MODULE =
            BLOCK_ENTITIES.register("ammo_module", () ->
                    BlockEntityType.Builder.of(AmmoModuleBlockEntity::new,
                            ModBlocks.AMMO_TURRET_MODULE.get()).build(null));
    public static final RegistryObject<BlockEntityType<VentCollectorBlockEntity>> VENT_COLLECTOR = BLOCK_ENTITIES.register("vent_collector",
            () -> BlockEntityType.Builder.of(VentCollectorBlockEntity::new, ModBlocks.VENT_COLLECTOR.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

