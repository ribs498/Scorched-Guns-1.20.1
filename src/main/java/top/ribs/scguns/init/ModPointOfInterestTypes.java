package top.ribs.scguns.init;

import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModPointOfInterestTypes
{
    public static final DeferredRegister<PoiType> REGISTER = DeferredRegister.create(ForgeRegistries.POI_TYPES, Reference.MOD_ID);


    private static RegistryObject<PoiType> register(String name, RegistryObject<Block> block, int maxFreeTickets) {
        List<RegistryObject<Block>> blocks = new ArrayList<>();
        blocks.add(block);
        return register(name, blocks, maxFreeTickets);
    }

    private static RegistryObject<PoiType> register(String name, Supplier<List<RegistryObject<Block>>> supplier, int maxFreeTickets) {
        return register(name, supplier.get(), maxFreeTickets);
    }

    private static RegistryObject<PoiType> register(String name, List<RegistryObject<Block>> blocks, int maxFreeTickets) {
        return REGISTER.register(name, () -> {
            Set<BlockState> blockStates = new HashSet<>();
            for (RegistryObject<Block> block : blocks) {
                blockStates.addAll(block.get().getStateDefinition().getPossibleStates());
            }
            return new PoiType(blockStates, maxFreeTickets, 1);
        });
    }
}
