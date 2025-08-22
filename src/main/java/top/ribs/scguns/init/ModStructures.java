package top.ribs.scguns.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.world.CogChambers;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> REGISTRY;
    public static final RegistryObject<StructureType<CogChambers>> CHAMBER;

    private static <T extends Structure> StructureType<T> stuff(Codec<T> codec) {
        return () -> codec;
    }

    static {
        REGISTRY = DeferredRegister.create(Registries.STRUCTURE_TYPE, "scguns");
        CHAMBER = REGISTRY.register("cogchambers", () -> stuff(CogChambers.CODEC));
    }
}