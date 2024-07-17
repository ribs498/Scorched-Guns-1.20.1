package top.ribs.scguns.compat;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.fml.ModList;

public class FarmersDelightModCondition implements ICondition {
    private static final ResourceLocation NAME = new ResourceLocation("scguns", "farmers_delight_mod_loaded");

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test(IContext iContext) {
        return ModList.get().isLoaded("farmersdelight");
    }

    public static class Serializer implements IConditionSerializer<FarmersDelightModCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, FarmersDelightModCondition value) {
            // No extra data to write
        }

        @Override
        public FarmersDelightModCondition read(JsonObject json) {
            return new FarmersDelightModCondition();
        }

        @Override
        public ResourceLocation getID() {
            return FarmersDelightModCondition.NAME;
        }
    }
}