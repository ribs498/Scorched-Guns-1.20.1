package top.ribs.scguns.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;
import top.ribs.scguns.fluid.ViciousAcidFluid;
import top.ribs.scguns.fluid.ViciousAcidFluidType;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Reference.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, Reference.MOD_ID);

    public static final RegistryObject<FluidType> VICIOUS_ACID_FLUID_TYPE = FLUID_TYPES.register("vicious_acid",
            () -> new ViciousAcidFluidType(
                    new ResourceLocation(Reference.MOD_ID, "block/vicious_acid_still"),
                    new ResourceLocation(Reference.MOD_ID, "block/vicious_acid_flow")
            ));

    public static final RegistryObject<FlowingFluid> VICIOUS_ACID_SOURCE = FLUIDS.register("vicious_acid_source",
            ViciousAcidFluid.Source::new);

    public static final RegistryObject<FlowingFluid> VICIOUS_ACID_FLOWING = FLUIDS.register("vicious_acid_flowing",
            ViciousAcidFluid.Flowing::new);
}