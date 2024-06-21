package top.ribs.scguns.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;
import top.ribs.scguns.block.*;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Author: MrCrayfish
 */
public class ModBlocks {

    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MOD_ID);

public static final RegistryObject<Block> MACERATOR = register("macerator",
        () -> new MaceratorBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                .requiresCorrectToolForDrops()
                .strength(3.0F)
                .noOcclusion()));
    public static final RegistryObject<Block> MECHANICAL_PRESS = register("mechanical_press",
            () -> new MechanicalPressBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)
                    .noOcclusion()));
    public static final RegistryObject<Block> GUN_BENCH = register("gun_bench",
            () -> new GunBenchBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE)
                    .requiresCorrectToolForDrops()
                    .strength(2.5F)
                    .noOcclusion()));
    public static final RegistryObject<Block> ANTHRALITE_BLOCK = register("anthralite_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final RegistryObject<Block> RAW_ANTHRALITE_BLOCK = register("raw_anthralite_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final RegistryObject<Block> ANTHRALITE_ORE = register("anthralite_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_ORE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));

    public static final RegistryObject<Block> DEEPSLATE_ANTHRALITE_ORE = register("deepslate_anthralite_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_IRON_ORE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F)));
    public static final RegistryObject<Block> SULFUR_BLOCK = register("sulfur_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)));
    public static final RegistryObject<Block> SULFUR_ORE = register("sulfur_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COAL_ORE)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)));
    public static final RegistryObject<Block> DEEPSLATE_SULFUR_ORE = register("deepslate_sulfur_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_COAL_ORE)
                    .requiresCorrectToolForDrops()
                    .strength(2.5F)));
    public static final RegistryObject<Block> NETHER_SULFUR_ORE = register("nether_sulfur_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COAL_ORE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F)));

    public static final RegistryObject<Block> NITER_LAYER = register("niter",
            () -> new NiterLayerBlock(BlockBehaviour.Properties.copy(Blocks.SNOW)));
    public static final RegistryObject<Block> NITER_BLOCK = register("niter_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK)));

    public static final RegistryObject<Block> GEOTHERMAL_VENT = register("geothermal_vent",
            () -> new GeothermalVentBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .noOcclusion()
                    .randomTicks(), 10.0F));
    public static final RegistryObject<Block> VENT_COLLECTOR = register("vent_collector",
            () -> new VentCollectorBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .noOcclusion()
                    .randomTicks(), 25.0F));

    private static <T extends Block> RegistryObject<T> register(String id, Supplier<T> blockSupplier) {
        return register(id, blockSupplier, block1 -> new BlockItem(block1, new Item.Properties()));
    }
    private static <T extends Block> RegistryObject<T> register(String id, Supplier<T> blockSupplier, @Nullable Function<T, BlockItem> supplier) {
        RegistryObject<T> registryObject = REGISTER.register(id, blockSupplier);
        if (supplier != null) {
            ModItems.REGISTER.register(id, () -> supplier.apply(registryObject.get()));
        }
        return registryObject;
    }
}
