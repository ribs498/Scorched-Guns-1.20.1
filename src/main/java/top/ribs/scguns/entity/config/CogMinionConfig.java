package top.ribs.scguns.entity.config;

import javax.annotation.Nullable;
import java.util.List;

public class CogMinionConfig {

    private float spawnWithItemChance;
    private List<ItemSpawnData> items;
    public CogMinionConfig() { }

    public CogMinionConfig(float spawnWithItemChance, List<ItemSpawnData> items) {
        this.spawnWithItemChance = spawnWithItemChance;
        this.items = items;
    }

    public float getSpawnWithItemChance() {
        return spawnWithItemChance;
    }

    public void setSpawnWithItemChance(float spawnWithItemChance) {
        this.spawnWithItemChance = spawnWithItemChance;
    }

    public List<ItemSpawnData> getItems() {
        return items;
    }

    public void setItems(List<ItemSpawnData> items) {
        this.items = items;
    }

    public class ItemSpawnData {
        private String item;
        private float spawnChance;
        private float dropChance;
        @Nullable
        private Float minDurability;
        @Nullable private Float maxDurability;

        public ItemSpawnData() { }

        public ItemSpawnData(String item, float spawnChance, float dropChance, float minDurability, float maxDurability) {
            this.item = item;
            this.spawnChance = spawnChance;
            this.dropChance = dropChance;
            this.minDurability = minDurability;
            this.maxDurability = maxDurability;
        }

        // Getters and setters for all fields
        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public float getSpawnChance() {
            return spawnChance;
        }

        public void setSpawnChance(float spawnChance) {
            this.spawnChance = spawnChance;
        }

        public float getDropChance() {
            return dropChance;
        }
        public void setDropChance(float dropChance) {
            this.dropChance = dropChance;
        }
        @Nullable
        public Float getMinDurability() {
            return minDurability;
        }
        public void setMinDurability(@Nullable Float minDurability) {
            this.minDurability = minDurability;
        }
        @Nullable
        public Float getMaxDurability() {
            return maxDurability;
        }
        public void setMaxDurability(@Nullable Float maxDurability) {
            this.maxDurability = maxDurability;
        }
    }

}
