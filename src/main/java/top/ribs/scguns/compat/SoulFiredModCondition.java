package top.ribs.scguns.compat;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.fml.ModList;

public class SoulFiredModCondition implements ICondition {
    private static final ResourceLocation NAME = new ResourceLocation("scguns", "soul_fired_mod_loaded");

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test(IContext iContext) {
        return ModList.get().isLoaded("soul_fire_d");
    }

    public static class Serializer implements IConditionSerializer<SoulFiredModCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, SoulFiredModCondition value) {
            // No extra data to write
        }

        @Override
        public SoulFiredModCondition read(JsonObject json) {
            return new SoulFiredModCondition();
        }

        @Override
        public ResourceLocation getID() {
            return SoulFiredModCondition.NAME;
        }
    }
}