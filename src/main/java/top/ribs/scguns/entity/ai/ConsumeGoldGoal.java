package top.ribs.scguns.entity.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import top.ribs.scguns.entity.monster.HornlinEntity;
import top.ribs.scguns.entity.monster.ZombifiedHornlinEntity;
import top.ribs.scguns.entity.util.GoldConsumptionHelper;
import top.ribs.scguns.entity.util.IGoldConsumingEntity;
import top.ribs.scguns.init.ModItems;

import java.util.EnumSet;

public class ConsumeGoldGoal extends Goal {
    private final Mob entity;
    private final IGoldConsumingEntity goldConsumer;

    private int goldEatingTime = 0;
    private int eatingPreparationTime = 0;
    private ItemStack heldFoodItem = ItemStack.EMPTY;

    private static final int EATING_DURATION = 40;
    private static final int PREPARATION_DURATION = 15;
    private static final float PICKUP_RANGE = 1.5F;
    private static final int COOLDOWN_AFTER_EATING = 40;
    private static final int COOLDOWN_AFTER_CANCEL = 20;

    private static final float GOLD_VALUE_FOR_SLAG = 10F;
    private static final float POISON_GOLD_REDUCTION = 8.0F;
    private static final int SLAG_PRODUCTION_COOLDOWN = 150;

    public ConsumeGoldGoal(Mob entity, IGoldConsumingEntity goldConsumer) {
        this.entity = entity;
        this.goldConsumer = goldConsumer;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (goldConsumer.isEatingGold() || goldConsumer.isPreparingToEat()) {
            return true;
        }

        if (goldConsumer.getGoldEatingCooldown() > 0) {
            return false;
        }
        return findNearbyGold() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return goldConsumer.isEatingGold() ||
                goldConsumer.isPreparingToEat() ||
                this.eatingPreparationTime > 0;
    }

    @Override
    public void start() {
        this.entity.getNavigation().stop();
        if (!goldConsumer.isEatingGold() && !goldConsumer.isPreparingToEat()) {
            ItemEntity nearbyGold = findNearbyGold();
            if (nearbyGold != null) {
                prepareToEat(nearbyGold);
            }
        }
    }

    @Override
    public void stop() {
        if (goldConsumer.isEatingGold() || goldConsumer.isPreparingToEat()) {
            if (!this.heldFoodItem.isEmpty()) {
                this.entity.spawnAtLocation(this.heldFoodItem);
            }
            setEntityState(ItemStack.EMPTY, false, false);
        }
        this.resetEatingState();
    }

    @Override
    public void tick() {
        if (this.eatingPreparationTime > 0) {
            this.eatingPreparationTime--;
            this.entity.getNavigation().stop();

            if (this.eatingPreparationTime <= 0) {
                this.startEating();
            }
            return;
        }
        if (goldConsumer.isEatingGold()) {
            this.goldEatingTime--;

            if (this.goldEatingTime % 8 == 0) {
                GoldConsumptionHelper.showEatingParticles(this.entity);
            }

            if (this.goldEatingTime <= 0) {
                this.finishEating();
            }
            return;
        }
        this.handleSlagProduction();
    }

    private ItemEntity findNearbyGold() {
        AABB closeArea = new AABB(
                this.entity.getX() - PICKUP_RANGE,
                this.entity.getY() - 0.5,
                this.entity.getZ() - PICKUP_RANGE,
                this.entity.getX() + PICKUP_RANGE,
                this.entity.getY() + 1.5,
                this.entity.getZ() + PICKUP_RANGE
        );

        return this.entity.level().getEntitiesOfClass(ItemEntity.class, closeArea)
                .stream()
                .filter(item -> !item.isRemoved())
                .filter(item -> GoldConsumptionHelper.isGoldItem(item.getItem()))
                .filter(item -> this.entity.distanceTo(item) <= PICKUP_RANGE)
                .findFirst()
                .orElse(null);
    }

    private void prepareToEat(ItemEntity goldItem) {
        goldConsumer.setTargetGoldItem(goldItem);

        if (goldItem == null || goldItem.isRemoved()) {
            this.cancelEating();
            return;
        }

        ItemStack groundStack = goldItem.getItem();
        this.heldFoodItem = groundStack.copy();
        this.heldFoodItem.setCount(1);

        setEntityState(this.heldFoodItem, false, true);

        groundStack.shrink(1);
        if (groundStack.isEmpty()) {
            goldItem.discard();
        }

        this.eatingPreparationTime = PREPARATION_DURATION;
        this.entity.getNavigation().stop();
        this.entity.playSound(SoundEvents.ITEM_PICKUP, 0.8F, 1.2F + this.entity.getRandom().nextFloat() * 0.4F);
    }

    private void startEating() {
        setEntityState(this.heldFoodItem, true, false);
        this.goldEatingTime = EATING_DURATION;
        this.entity.playSound(SoundEvents.GENERIC_EAT, 0.8F, 1.0F + this.entity.getRandom().nextFloat() * 0.2F);
    }

    private void finishEating() {
        if (!this.heldFoodItem.isEmpty()) {
            this.applyFoodEffects(this.heldFoodItem);
        }

        this.entity.playSound(SoundEvents.PLAYER_BURP, 0.8F, 1.3F);
        setEntityState(ItemStack.EMPTY, false, false);

        this.resetEatingState();
        goldConsumer.setGoldEatingCooldown(COOLDOWN_AFTER_EATING);
    }

    private void applyFoodEffects(ItemStack foodStack) {
        if (GoldConsumptionHelper.isPoisonItem(foodStack)) {
            GoldConsumptionHelper.applyPoisonEffects(this.entity, foodStack);
            float currentGold = goldConsumer.getAccumulatedGoldValue();
            goldConsumer.setAccumulatedGoldValue(Math.max(0, currentGold - POISON_GOLD_REDUCTION));
        } else {
            float healthToRestore = GoldConsumptionHelper.getHealthFromGold(foodStack);
            float goldNuggetValue = GoldConsumptionHelper.getGoldNuggetValue(foodStack);

            this.entity.heal(healthToRestore);
            goldConsumer.addAccumulatedGoldValue(goldNuggetValue);
        }
    }

    private void cancelEating() {
        if (!this.heldFoodItem.isEmpty()) {
            this.entity.spawnAtLocation(this.heldFoodItem);
        }

        setEntityState(ItemStack.EMPTY, false, false);

        this.resetEatingState();
        goldConsumer.setGoldEatingCooldown(COOLDOWN_AFTER_CANCEL);
    }

    private void resetEatingState() {
        this.eatingPreparationTime = 0;
        this.goldEatingTime = 0;
        this.heldFoodItem = ItemStack.EMPTY;
        goldConsumer.setTargetGoldItem(null);
    }

    private void handleSlagProduction() {
        if (goldConsumer.getSlagProductionCooldown() > 0 ||
                goldConsumer.isEatingGold() ||
                goldConsumer.isPreparingToEat()) {
            return;
        }

        if (goldConsumer.getAccumulatedGoldValue() >= GOLD_VALUE_FOR_SLAG) {
            this.produceSlag();
        }
    }

    private void produceSlag() {
        float currentGold = goldConsumer.getAccumulatedGoldValue();
        goldConsumer.setAccumulatedGoldValue(Math.max(0, currentGold - GOLD_VALUE_FOR_SLAG));

        this.entity.playSound(SoundEvents.PLAYER_BURP, 1.2F, 0.5F + this.entity.getRandom().nextFloat() * 0.3F);
        GoldConsumptionHelper.showSlagProductionParticles(this.entity);

        ItemStack slagStack = new ItemStack(ModItems.AUREOUS_SLAG.get());
        this.entity.spawnAtLocation(slagStack);

        goldConsumer.setSlagProductionCooldown(SLAG_PRODUCTION_COOLDOWN);
    }

    private void setEntityState(ItemStack heldItem, boolean eating, boolean preparing) {
        if (entity instanceof HornlinEntity hornlin) {
            hornlin.setHeldFoodItem(heldItem);
            hornlin.setEatingGold(eating);
            hornlin.setPreparingToEat(preparing);
        } else if (entity instanceof ZombifiedHornlinEntity zombified) {
            zombified.setHeldFoodItem(heldItem);
            zombified.setEatingGold(eating);
            zombified.setPreparingToEat(preparing);
        }
    }
}