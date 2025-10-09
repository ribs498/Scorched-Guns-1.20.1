package top.ribs.scguns.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import top.ribs.scguns.entity.util.GoldConsumptionHelper;
import top.ribs.scguns.entity.util.IGoldConsumingEntity;

import java.util.EnumSet;

public class GoldSeekingGoal extends Goal {
    private final Mob entity;
    private final IGoldConsumingEntity goldConsumer;
    private final double speed;
    private final float searchRange;
    private ItemEntity targetGold;

    public GoldSeekingGoal(Mob entity, IGoldConsumingEntity goldConsumer, double speed, float searchRange) {
        this.entity = entity;
        this.goldConsumer = goldConsumer;
        this.speed = speed;
        this.searchRange = searchRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (goldConsumer.isEatingGold() || goldConsumer.isPreparingToEat()) {
            return false;
        }

        this.targetGold = this.findNearestGold();
        return this.targetGold != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (goldConsumer.isEatingGold() || goldConsumer.isPreparingToEat()) {
            return false;
        }

        return this.targetGold != null && !this.targetGold.isRemoved();
    }

    @Override
    public void start() {
        if (this.targetGold != null) {
            this.entity.getNavigation().moveTo(this.targetGold, this.speed);
        }
    }

    @Override
    public void stop() {
        this.targetGold = null;
        this.entity.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetGold != null) {
            if (this.entity.getNavigation().isDone()) {
                this.entity.getNavigation().moveTo(this.targetGold, this.speed);
            }
        }
    }

    private ItemEntity findNearestGold() {
        AABB searchArea = AABB.unitCubeFromLowerCorner(this.entity.position()).inflate(this.searchRange);

        return this.entity.level().getEntitiesOfClass(ItemEntity.class, searchArea)
                .stream()
                .filter(item -> !item.isRemoved())
                .filter(item -> GoldConsumptionHelper.isGoldItem(item.getItem()))
                .min((item1, item2) -> Double.compare(
                        this.entity.distanceToSqr(item1),
                        this.entity.distanceToSqr(item2)
                ))
                .orElse(null);
    }
}