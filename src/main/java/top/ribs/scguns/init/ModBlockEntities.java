package top.ribs.scguns.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.blockentity.*;


public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "scguns");

    public static final RegistryObject<BlockEntityType<PolarGeneratorBlockEntity>> POLAR_GENERATOR =
            BLOCK_ENTITIES.register("polar_generator", () ->
                    BlockEntityType.Builder.of(PolarGeneratorBlockEntity::new,
                            ModBlocks.POLAR_GENERATOR.get()).build(null));
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


    public static final RegistryObject<BlockEntityType<VentCollectorBlockEntity>> VENT_COLLECTOR = BLOCK_ENTITIES.register("vent_collector",
            () -> BlockEntityType.Builder.of(VentCollectorBlockEntity::new, ModBlocks.VENT_COLLECTOR.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

