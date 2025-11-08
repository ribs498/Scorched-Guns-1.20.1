package top.ribs.scguns.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;
import top.ribs.scguns.world.NiterPatchConfiguration;
import top.ribs.scguns.world.NiterPatchFeature;
import top.ribs.scguns.world.VentFeature;
import top.ribs.scguns.world.VentFeatureConfiguration;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Reference.MOD_ID);

    public static final RegistryObject<Feature<VentFeatureConfiguration>> VENT_FEATURE = FEATURES.register("vent",
            () -> new VentFeature(VentFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NiterPatchConfiguration>> NITER_PATCH = FEATURES.register("niter_patch",
            () -> new NiterPatchFeature(NiterPatchConfiguration.CODEC));


    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}