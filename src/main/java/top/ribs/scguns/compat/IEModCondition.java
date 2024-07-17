package top.ribs.scguns.compat;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.fml.ModList;

public class IEModCondition implements ICondition {
    private static final ResourceLocation NAME = new ResourceLocation("scguns", "immersiveengineering_mod_loaded");

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test(IContext iContext) {
        return ModList.get().isLoaded("immersiveengineering");
    }

    public static class Serializer implements IConditionSerializer<IEModCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, IEModCondition value) {
            // No extra data to write
        }

        @Override
        public IEModCondition read(JsonObject json) {
            return new IEModCondition();
        }

        @Override
        public ResourceLocation getID() {
            return IEModCondition.NAME;
        }
    }
}