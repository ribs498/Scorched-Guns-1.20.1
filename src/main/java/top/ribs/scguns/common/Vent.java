package top.ribs.scguns.common;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.annotation.Optional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Vent implements INBTSerializable<CompoundTag> {
    protected Activation activation = new Activation();
    protected Power power = new Power();
    protected Production production = new Production();
    protected Placement placement = new Placement();
    protected Particles particles = new Particles();

    public Activation getActivation() {
        return this.activation;
    }

    public Power getPower() {
        return this.power;
    }

    public Production getProduction() {
        return this.production;
    }

    public Placement getPlacement() {
        return this.placement;
    }

    public Particles getParticles() {
        return this.particles;
    }

    public static class Activation implements INBTSerializable<CompoundTag> {
        private ResourceLocation baseBlock = new ResourceLocation("minecraft:magma_block");
        private boolean requiresWaterlogged = false;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString("BaseBlock", this.baseBlock.toString());
            tag.putBoolean("RequiresWaterlogged", this.requiresWaterlogged);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("BaseBlock", Tag.TAG_STRING)) {
                this.baseBlock = new ResourceLocation(tag.getString("BaseBlock"));
            }
            if (tag.contains("RequiresWaterlogged", Tag.TAG_ANY_NUMERIC)) {
                this.requiresWaterlogged = tag.getBoolean("RequiresWaterlogged");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (!this.baseBlock.equals(new ResourceLocation("minecraft:magma_block"))) {
                object.addProperty("baseBlock", this.baseBlock.toString());
            }
            if (this.requiresWaterlogged) {
                object.addProperty("requiresWaterlogged", true);
            }
            return object;
        }

        public Activation copy() {
            Activation activation = new Activation();
            activation.baseBlock = this.baseBlock;
            activation.requiresWaterlogged = this.requiresWaterlogged;
            return activation;
        }

        public ResourceLocation getBaseBlock() {
            return this.baseBlock;
        }

        public boolean requiresWaterlogged() {
            return this.requiresWaterlogged;
        }

        public void setBaseBlock(ResourceLocation baseBlock) {
            this.baseBlock = baseBlock;
        }

        public void setRequiresWaterlogged(boolean requiresWaterlogged) {
            this.requiresWaterlogged = requiresWaterlogged;
        }
    }

    public static class Power implements INBTSerializable<CompoundTag> {
        private int maxPower = 5;
        private int baseTickInterval = 100;
        private int tickWiggleRoom = 60;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("MaxPower", this.maxPower);
            tag.putInt("BaseTickInterval", this.baseTickInterval);
            tag.putInt("TickWiggleRoom", this.tickWiggleRoom);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("MaxPower", Tag.TAG_ANY_NUMERIC)) {
                this.maxPower = tag.getInt("MaxPower");
            }
            if (tag.contains("BaseTickInterval", Tag.TAG_ANY_NUMERIC)) {
                this.baseTickInterval = tag.getInt("BaseTickInterval");
            }
            if (tag.contains("TickWiggleRoom", Tag.TAG_ANY_NUMERIC)) {
                this.tickWiggleRoom = tag.getInt("TickWiggleRoom");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.maxPower != 5) object.addProperty("maxPower", this.maxPower);
            if (this.baseTickInterval != 100) object.addProperty("baseTickInterval", this.baseTickInterval);
            if (this.tickWiggleRoom != 60) object.addProperty("tickWiggleRoom", this.tickWiggleRoom);
            return object;
        }

        public Power copy() {
            Power power = new Power();
            power.maxPower = this.maxPower;
            power.baseTickInterval = this.baseTickInterval;
            power.tickWiggleRoom = this.tickWiggleRoom;
            return power;
        }

        public int getMaxPower() {
            return this.maxPower;
        }

        public int getBaseTickInterval() {
            return this.baseTickInterval;
        }

        public int getTickWiggleRoom() {
            return this.tickWiggleRoom;
        }

        public void setMaxPower(int maxPower) {
            this.maxPower = maxPower;
        }

        public void setBaseTickInterval(int baseTickInterval) {
            this.baseTickInterval = baseTickInterval;
        }

        public void setTickWiggleRoom(int tickWiggleRoom) {
            this.tickWiggleRoom = tickWiggleRoom;
        }
    }

    public static class Production implements INBTSerializable<CompoundTag> {
        private List<OutputItem> outputs = new ArrayList<>();
        private float productionChance = 0.4f;

        public static class OutputItem implements INBTSerializable<CompoundTag> {
            private ResourceLocation item;
            private int weight = 1;

            @Override
            public CompoundTag serializeNBT() {
                CompoundTag tag = new CompoundTag();
                tag.putString("Item", this.item.toString());
                tag.putInt("Weight", this.weight);
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                if (tag.contains("Item", Tag.TAG_STRING)) {
                    this.item = new ResourceLocation(tag.getString("Item"));
                }
                if (tag.contains("Weight", Tag.TAG_ANY_NUMERIC)) {
                    this.weight = tag.getInt("Weight");
                }
            }

            public JsonObject toJsonObject() {
                JsonObject object = new JsonObject();
                object.addProperty("item", this.item.toString());
                if (this.weight != 1) {
                    object.addProperty("weight", this.weight);
                }
                return object;
            }

            public OutputItem copy() {
                OutputItem output = new OutputItem();
                output.item = this.item;
                output.weight = this.weight;
                return output;
            }

            @Nullable
            public Item getItem() {
                return ForgeRegistries.ITEMS.getValue(this.item);
            }

            public int getWeight() {
                return this.weight;
            }

            public void setItem(ResourceLocation item) {
                this.item = item;
            }

            public void setWeight(int weight) {
                this.weight = weight;
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            CompoundTag outputsTag = new CompoundTag();
            for (int i = 0; i < this.outputs.size(); i++) {
                outputsTag.put("Output" + i, this.outputs.get(i).serializeNBT());
            }
            tag.put("Outputs", outputsTag);
            tag.putFloat("ProductionChance", this.productionChance);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Outputs", Tag.TAG_COMPOUND)) {
                CompoundTag outputsTag = tag.getCompound("Outputs");
                this.outputs.clear();
                int i = 0;
                while (outputsTag.contains("Output" + i)) {
                    OutputItem output = new OutputItem();
                    output.deserializeNBT(outputsTag.getCompound("Output" + i));
                    this.outputs.add(output);
                    i++;
                }
            }
            if (tag.contains("ProductionChance", Tag.TAG_ANY_NUMERIC)) {
                this.productionChance = tag.getFloat("ProductionChance");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (!this.outputs.isEmpty()) {
                com.google.gson.JsonArray outputsArray = new com.google.gson.JsonArray();
                for (OutputItem output : this.outputs) {
                    outputsArray.add(output.toJsonObject());
                }
                object.add("outputs", outputsArray);
            }
            if (this.productionChance != 0.4f) {
                object.addProperty("productionChance", this.productionChance);
            }
            return object;
        }

        public Production copy() {
            Production production = new Production();
            for (OutputItem output : this.outputs) {
                production.outputs.add(output.copy());
            }
            production.productionChance = this.productionChance;
            return production;
        }

        public List<OutputItem> getOutputs() {
            return this.outputs;
        }

        public float getProductionChance() {
            return this.productionChance;
        }

        public void clearOutputs() {
            this.outputs.clear();
        }

        public void addOutput(OutputItem output) {
            this.outputs.add(output);
        }

        public void setProductionChance(float productionChance) {
            this.productionChance = productionChance;
        }
    }

    public static class Placement implements INBTSerializable<CompoundTag> {
        private boolean enabled = false;
        @Optional
        private ResourceLocation blockToPlace;
        private int radius = 8;
        private float placementChance = 0.35f;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Enabled", this.enabled);
            if (this.blockToPlace != null) {
                tag.putString("BlockToPlace", this.blockToPlace.toString());
            }
            tag.putInt("Radius", this.radius);
            tag.putFloat("PlacementChance", this.placementChance);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Enabled", Tag.TAG_ANY_NUMERIC)) {
                this.enabled = tag.getBoolean("Enabled");
            }
            if (tag.contains("BlockToPlace", Tag.TAG_STRING)) {
                this.blockToPlace = new ResourceLocation(tag.getString("BlockToPlace"));
            }
            if (tag.contains("Radius", Tag.TAG_ANY_NUMERIC)) {
                this.radius = tag.getInt("Radius");
            }
            if (tag.contains("PlacementChance", Tag.TAG_ANY_NUMERIC)) {
                this.placementChance = tag.getFloat("PlacementChance");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.enabled) {
                object.addProperty("enabled", true);
            }
            if (this.blockToPlace != null) {
                object.addProperty("blockToPlace", this.blockToPlace.toString());
            }
            if (this.radius != 8) {
                object.addProperty("radius", this.radius);
            }
            if (this.placementChance != 0.35f) {
                object.addProperty("placementChance", this.placementChance);
            }
            return object;
        }

        public Placement copy() {
            Placement placement = new Placement();
            placement.enabled = this.enabled;
            placement.blockToPlace = this.blockToPlace;
            placement.radius = this.radius;
            placement.placementChance = this.placementChance;
            return placement;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        @Nullable
        public ResourceLocation getBlockToPlace() {
            return this.blockToPlace;
        }

        public int getRadius() {
            return this.radius;
        }

        public float getPlacementChance() {
            return this.placementChance;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setBlockToPlace(ResourceLocation blockToPlace) {
            this.blockToPlace = blockToPlace;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public void setPlacementChance(float placementChance) {
            this.placementChance = placementChance;
        }
    }

    public static class Particles implements INBTSerializable<CompoundTag> {
        private boolean showActive = true;
        @Optional
        private ResourceLocation activeSound;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("ShowActive", this.showActive);
            if (this.activeSound != null) {
                tag.putString("ActiveSound", this.activeSound.toString());
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("ShowActive", Tag.TAG_ANY_NUMERIC)) {
                this.showActive = tag.getBoolean("ShowActive");
            }
            if (tag.contains("ActiveSound", Tag.TAG_STRING)) {
                this.activeSound = new ResourceLocation(tag.getString("ActiveSound"));
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (!this.showActive) {
                object.addProperty("showActive", false);
            }
            if (this.activeSound != null) {
                object.addProperty("activeSound", this.activeSound.toString());
            }
            return object;
        }

        public Particles copy() {
            Particles particles = new Particles();
            particles.showActive = this.showActive;
            particles.activeSound = this.activeSound;
            return particles;
        }

        public boolean showActive() {
            return this.showActive;
        }

        @Nullable
        public ResourceLocation getActiveSound() {
            return this.activeSound;
        }

        public void setShowActive(boolean showActive) {
            this.showActive = showActive;
        }

        public void setActiveSound(ResourceLocation activeSound) {
            this.activeSound = activeSound;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("Activation", this.activation.serializeNBT());
        tag.put("Power", this.power.serializeNBT());
        tag.put("Production", this.production.serializeNBT());
        tag.put("Placement", this.placement.serializeNBT());
        tag.put("Particles", this.particles.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Activation", Tag.TAG_COMPOUND)) {
            this.activation.deserializeNBT(tag.getCompound("Activation"));
        }
        if (tag.contains("Power", Tag.TAG_COMPOUND)) {
            this.power.deserializeNBT(tag.getCompound("Power"));
        }
        if (tag.contains("Production", Tag.TAG_COMPOUND)) {
            this.production.deserializeNBT(tag.getCompound("Production"));
        }
        if (tag.contains("Placement", Tag.TAG_COMPOUND)) {
            this.placement.deserializeNBT(tag.getCompound("Placement"));
        }
        if (tag.contains("Particles", Tag.TAG_COMPOUND)) {
            this.particles.deserializeNBT(tag.getCompound("Particles"));
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.add("activation", this.activation.toJsonObject());
        object.add("power", this.power.toJsonObject());
        object.add("production", this.production.toJsonObject());
        object.add("placement", this.placement.toJsonObject());
        object.add("particles", this.particles.toJsonObject());
        return object;
    }

    public static Vent create(CompoundTag tag) {
        Vent vent = new Vent();
        vent.deserializeNBT(tag);
        return vent;
    }

    public Vent copy() {
        Vent vent = new Vent();
        vent.activation = this.activation.copy();
        vent.power = this.power.copy();
        vent.production = this.production.copy();
        vent.placement = this.placement.copy();
        vent.particles = this.particles.copy();
        return vent;
    }
}