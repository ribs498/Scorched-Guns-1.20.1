package top.ribs.scguns.init;

import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;

/**
 * Registry for custom paintings
 */
public class ModPaintings {

    public static final DeferredRegister<PaintingVariant> REGISTER =
            DeferredRegister.create(ForgeRegistries.PAINTING_VARIANTS, Reference.MOD_ID);
    public static final RegistryObject<PaintingVariant> THE_COLLECTIVE = REGISTER.register("the_collective",
            () -> new PaintingVariant(64, 32));
}