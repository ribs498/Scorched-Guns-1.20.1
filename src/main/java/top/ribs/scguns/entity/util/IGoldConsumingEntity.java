package top.ribs.scguns.entity.util;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Interface for entities that can consume gold items
 */
public interface IGoldConsumingEntity {

    boolean isEatingGold();

    boolean isPreparingToEat();

    ItemStack getHeldFoodItem();

    ItemEntity getTargetGoldItem();

    void setTargetGoldItem(ItemEntity item);

    float getAccumulatedGoldValue();

    void setAccumulatedGoldValue(float value);

    void addAccumulatedGoldValue(float value);

    int getGoldEatingCooldown();

    void setGoldEatingCooldown(int ticks);

    int getSlagProductionCooldown();

    void setSlagProductionCooldown(int ticks);
}