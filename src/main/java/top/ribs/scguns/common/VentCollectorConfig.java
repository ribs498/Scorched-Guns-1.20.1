package top.ribs.scguns.common;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class VentCollectorConfig implements INBTSerializable<CompoundTag> {
    protected Filters filters = new Filters();
    protected Processing processing = new Processing();

    public Filters getFilters() {
        return this.filters;
    }

    public Processing getProcessing() {
        return this.processing;
    }

    public static class Filters implements INBTSerializable<CompoundTag> {
        private int maxCharge = 64;
        private float consumptionChance = 0.5f;
        private int processCooldown = 2;
        private final List<FilterItem> filterItems = new ArrayList<>();

        public static class FilterItem implements INBTSerializable<CompoundTag> {
            private ResourceLocation identifier;
            private boolean isTag = true;
            private int chargeAmount;

            @Override
            public CompoundTag serializeNBT() {
                CompoundTag tag = new CompoundTag();
                tag.putString("Identifier", this.identifier.toString());
                tag.putBoolean("IsTag", this.isTag);
                tag.putInt("ChargeAmount", this.chargeAmount);
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                if (tag.contains("Identifier", Tag.TAG_STRING)) {
                    this.identifier = new ResourceLocation(tag.getString("Identifier"));
                }
                if (tag.contains("IsTag", Tag.TAG_ANY_NUMERIC)) {
                    this.isTag = tag.getBoolean("IsTag");
                }
                if (tag.contains("ChargeAmount", Tag.TAG_ANY_NUMERIC)) {
                    this.chargeAmount = tag.getInt("ChargeAmount");
                }
            }

            public JsonObject toJsonObject() {
                JsonObject object = new JsonObject();
                if (this.isTag) {
                    object.addProperty("tag", this.identifier.toString());
                } else {
                    object.addProperty("item", this.identifier.toString());
                }
                object.addProperty("chargeAmount", this.chargeAmount);
                return object;
            }

            public FilterItem copy() {
                FilterItem item = new FilterItem();
                item.identifier = this.identifier;
                item.isTag = this.isTag;
                item.chargeAmount = this.chargeAmount;
                return item;
            }

            public ResourceLocation getIdentifier() {
                return this.identifier;
            }

            public boolean isTag() {
                return this.isTag;
            }

            public int getChargeAmount() {
                return this.chargeAmount;
            }

            public void setIdentifier(ResourceLocation identifier) {
                this.identifier = identifier;
            }

            public void setIsTag(boolean isTag) {
                this.isTag = isTag;
            }

            public void setChargeAmount(int chargeAmount) {
                this.chargeAmount = chargeAmount;
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("MaxCharge", this.maxCharge);
            tag.putFloat("ConsumptionChance", this.consumptionChance);
            tag.putInt("ProcessCooldown", this.processCooldown);

            CompoundTag itemsTag = new CompoundTag();
            for (int i = 0; i < this.filterItems.size(); i++) {
                itemsTag.put("FilterItem" + i, this.filterItems.get(i).serializeNBT());
            }
            tag.put("FilterItems", itemsTag);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("MaxCharge", Tag.TAG_ANY_NUMERIC)) {
                this.maxCharge = tag.getInt("MaxCharge");
            }
            if (tag.contains("ConsumptionChance", Tag.TAG_ANY_NUMERIC)) {
                this.consumptionChance = tag.getFloat("ConsumptionChance");
            }
            if (tag.contains("ProcessCooldown", Tag.TAG_ANY_NUMERIC)) {
                this.processCooldown = tag.getInt("ProcessCooldown");
            }
            if (tag.contains("FilterItems", Tag.TAG_COMPOUND)) {
                CompoundTag itemsTag = tag.getCompound("FilterItems");
                this.filterItems.clear();
                int i = 0;
                while (itemsTag.contains("FilterItem" + i)) {
                    FilterItem item = new FilterItem();
                    item.deserializeNBT(itemsTag.getCompound("FilterItem" + i));
                    this.filterItems.add(item);
                    i++;
                }
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.maxCharge != 64) {
                object.addProperty("maxCharge", this.maxCharge);
            }
            if (this.consumptionChance != 0.5f) {
                object.addProperty("consumptionChance", this.consumptionChance);
            }
            if (this.processCooldown != 2) {
                object.addProperty("processCooldown", this.processCooldown);
            }
            if (!this.filterItems.isEmpty()) {
                com.google.gson.JsonArray itemsArray = new com.google.gson.JsonArray();
                for (FilterItem item : this.filterItems) {
                    itemsArray.add(item.toJsonObject());
                }
                object.add("filterItems", itemsArray);
            }
            return object;
        }

        public Filters copy() {
            Filters filters = new Filters();
            filters.maxCharge = this.maxCharge;
            filters.consumptionChance = this.consumptionChance;
            filters.processCooldown = this.processCooldown;
            for (FilterItem item : this.filterItems) {
                filters.filterItems.add(item.copy());
            }
            return filters;
        }

        public int getMaxCharge() {
            return this.maxCharge;
        }

        public float getConsumptionChance() {
            return this.consumptionChance;
        }

        public int getProcessCooldown() {
            return this.processCooldown;
        }

        public List<FilterItem> getFilterItems() {
            return this.filterItems;
        }

        public void setMaxCharge(int maxCharge) {
            this.maxCharge = maxCharge;
        }

        public void setConsumptionChance(float consumptionChance) {
            this.consumptionChance = consumptionChance;
        }

        public void setProcessCooldown(int processCooldown) {
            this.processCooldown = processCooldown;
        }

        public void clearFilterItems() {
            this.filterItems.clear();
        }

        public void addFilterItem(FilterItem item) {
            this.filterItems.add(item);
        }
    }

    public static class Processing implements INBTSerializable<CompoundTag> {
        private float powerSpeedMultiplier = 0.35f;
        private int pushCooldown = 5;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("PowerSpeedMultiplier", this.powerSpeedMultiplier);
            tag.putInt("PushCooldown", this.pushCooldown);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("PowerSpeedMultiplier", Tag.TAG_ANY_NUMERIC)) {
                this.powerSpeedMultiplier = tag.getFloat("PowerSpeedMultiplier");
            }
            if (tag.contains("PushCooldown", Tag.TAG_ANY_NUMERIC)) {
                this.pushCooldown = tag.getInt("PushCooldown");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.powerSpeedMultiplier != 0.35f) {
                object.addProperty("powerSpeedMultiplier", this.powerSpeedMultiplier);
            }
            if (this.pushCooldown != 5) {
                object.addProperty("pushCooldown", this.pushCooldown);
            }
            return object;
        }

        public Processing copy() {
            Processing processing = new Processing();
            processing.powerSpeedMultiplier = this.powerSpeedMultiplier;
            processing.pushCooldown = this.pushCooldown;
            return processing;
        }

        public float getPowerSpeedMultiplier() {
            return this.powerSpeedMultiplier;
        }

        public int getPushCooldown() {
            return this.pushCooldown;
        }

        public void setPowerSpeedMultiplier(float powerSpeedMultiplier) {
            this.powerSpeedMultiplier = powerSpeedMultiplier;
        }

        public void setPushCooldown(int pushCooldown) {
            this.pushCooldown = pushCooldown;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("Filters", this.filters.serializeNBT());
        tag.put("Processing", this.processing.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Filters", Tag.TAG_COMPOUND)) {
            this.filters.deserializeNBT(tag.getCompound("Filters"));
        }
        if (tag.contains("Processing", Tag.TAG_COMPOUND)) {
            this.processing.deserializeNBT(tag.getCompound("Processing"));
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.add("filters", this.filters.toJsonObject());
        object.add("processing", this.processing.toJsonObject());
        return object;
    }

    public static VentCollectorConfig create(CompoundTag tag) {
        VentCollectorConfig config = new VentCollectorConfig();
        config.deserializeNBT(tag);
        return config;
    }

    public VentCollectorConfig copy() {
        VentCollectorConfig config = new VentCollectorConfig();
        config.filters = this.filters.copy();
        config.processing = this.processing.copy();
        return config;
    }
}