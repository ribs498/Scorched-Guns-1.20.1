package top.ribs.scguns.init;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
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
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_ORE).mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final RegistryObject<Block> DEEPSLATE_ANTHRALITE_ORE = register("deepslate_anthralite_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_IRON_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)));
    public static final RegistryObject<Block> SULFUR_BLOCK = register("sulfur_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)));
    public static final RegistryObject<Block> ANCIENT_BRASS_BLOCK = register("ancient_brass_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.RAW_COPPER_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)));
    public static final RegistryObject<Block> SULFUR_ORE = register("sulfur_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F), UniformInt.of(0, 2)));
    public static final RegistryObject<Block> DEEPSLATE_SULFUR_ORE = register("deepslate_sulfur_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.COAL_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE), UniformInt.of(0, 2)));
    public static final RegistryObject<Block> NETHER_SULFUR_ORE = register("nether_sulfur_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.NETHER_GOLD_ORE), UniformInt.of(0, 1)));
    public static final RegistryObject<Block> VEHEMENT_COAL_ORE = register("vehement_coal_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.NETHER_GOLD_ORE), UniformInt.of(0, 1)));

    public static final RegistryObject<Block> NITER_LAYER = register("niter",
            () -> new NiterLayerBlock(BlockBehaviour.Properties.copy(Blocks.SNOW)));
    public static final RegistryObject<Block> NITER_BLOCK = register("niter_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND)));

    public static final RegistryObject<Block> GEOTHERMAL_VENT = register("geothermal_vent",
            () -> new GeothermalVentBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .noOcclusion()
                    .randomTicks()));


    public static final RegistryObject<Block> VENT_COLLECTOR = register("vent_collector",
            () -> new VentCollectorBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(0.5F)
                    .noOcclusion()
                    .randomTicks()));



    public static final RegistryObject<Block> NITER_GLASS = register("niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> WHITE_NITER_GLASS = register("white_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> ORANGE_NITER_GLASS = register("orange_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> MAGENTA_NITER_GLASS = register("magenta_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> LIGHT_BLUE_NITER_GLASS = register("light_blue_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> YELLOW_NITER_GLASS = register("yellow_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> LIME_NITER_GLASS = register("lime_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> PINK_NITER_GLASS = register("pink_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> GRAY_NITER_GLASS = register("gray_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> LIGHT_GRAY_NITER_GLASS = register("light_gray_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> CYAN_NITER_GLASS = register("cyan_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> PURPLE_NITER_GLASS = register("purple_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> BLUE_NITER_GLASS = register("blue_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> BROWN_NITER_GLASS = register("brown_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> GREEN_NITER_GLASS = register("green_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> RED_NITER_GLASS = register("red_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> BLACK_NITER_GLASS = register("black_niter_glass",
            () -> new NiterGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));

    public static final RegistryObject<Block> ASGHARIAN_PILLAR = register("asgharian_pillar",
            () -> new AsgharianPillarBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(4.0F)));
    public static final RegistryObject<Block> ASGHARIAN_BRICKS = register("asgharian_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(4.0F)));

    public static final RegistryObject<Block> CRACKED_ASGHARIAN_BRICKS = register("cracked_asgharian_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(4.0F)));
    public static final RegistryObject<Block> ASGHARIAN_BRICK_SLAB = register("asgharian_brick_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(4.0F)));
    public static final RegistryObject<Block> ASGHARIAN_BRICK_STAIRS = register("asgharian_brick_stairs",
            () -> new StairBlock(() -> ModBlocks.ASGHARIAN_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(4.0F)));
    public static final RegistryObject<Block> ASGHARIAN_BRICK_WALL = register("asgharian_brick_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(4.0F)));

    public static final RegistryObject<Block> SANDBAG = register("sandbag",
            () -> new SandbagBlock(BlockBehaviour.Properties.copy(Blocks.SANDSTONE)
                    .strength(0.5F)
                    .noOcclusion()));
    public static final RegistryObject<Block> SUPPLY_CRATE = register("supply_crate",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)));


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
