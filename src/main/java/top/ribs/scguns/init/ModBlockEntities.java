package top.ribs.scguns.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.blockentity.MaceratorBlockEntity;
import top.ribs.scguns.blockentity.MechanicalPressBlockEntity;
import top.ribs.scguns.blockentity.VentCollectorBlockEntity;


public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "scguns");

    public static final RegistryObject<BlockEntityType<MaceratorBlockEntity>> MACERATOR =
            BLOCK_ENTITIES.register("macerator", () ->
                    BlockEntityType.Builder.of(MaceratorBlockEntity::new,
                            ModBlocks.MACERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<MechanicalPressBlockEntity>> MECHANICAL_PRESS =
            BLOCK_ENTITIES.register("mechanical_press", () ->
                    BlockEntityType.Builder.of(MechanicalPressBlockEntity::new,
                            ModBlocks.MECHANICAL_PRESS.get()).build(null));
    public static final RegistryObject<BlockEntityType<VentCollectorBlockEntity>> VENT_COLLECTOR = BLOCK_ENTITIES.register("vent_collector",
            () -> BlockEntityType.Builder.of((pos, state) -> new VentCollectorBlockEntity(pos, state, 25.0F), ModBlocks.VENT_COLLECTOR.get()).build(null)); // Set desired production speed here


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

