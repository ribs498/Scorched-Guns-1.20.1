package top.ribs.scguns.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.event.BatPoopEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class BatGlowberryGoal extends Goal {
    private final Bat bat;
    private final double speedModifier;
    private final int searchRange;

    @Nullable
    private BlockPos targetBerry;
    private int eatCooldown = 0;
    private boolean shouldRoostAfterEating = false;

    public BatGlowberryGoal(Bat bat, double speedModifier, int searchRange) {
        this.bat = bat;
        this.speedModifier = speedModifier;
        this.searchRange = searchRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (BatPoopEvent.isWellFed(this.bat)) {
            return false;
        }

        if (eatCooldown > 0) {
            eatCooldown--;
            return false;
        }

        if (this.bat.isResting() && !shouldRoostAfterEating) {
            return false;
        }

        if (shouldRoostAfterEating) {
            return true;
        }

        if (this.bat.getRandom().nextInt(20) != 0) {
            return false;
        }

        this.targetBerry = findNearbyGlowBerry();
        return this.targetBerry != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (shouldRoostAfterEating) {
            return !this.bat.isResting();
        }

        return this.targetBerry != null &&
                !BatPoopEvent.isWellFed(this.bat) &&
                this.bat.level().getBlockState(this.targetBerry).getBlock() instanceof CaveVines &&
                CaveVines.hasGlowBerries(this.bat.level().getBlockState(this.targetBerry));
    }

    @Override
    public void start() {
        if (this.bat.isResting()) {
            this.bat.setResting(false);
        }
    }

    @Override
    public void tick() {
        if (shouldRoostAfterEating) {
            tryToRoost();
            return;
        }

        if (this.targetBerry == null) return;

        Vec3 targetVec = Vec3.atCenterOf(this.targetBerry);
        double distance = this.bat.position().distanceTo(targetVec);

        if (distance < 1.5) {
            eatBerry();
        } else {
            moveTowardsBerry(targetVec);
        }
    }

    @Override
    public void stop() {
        this.targetBerry = null;
        shouldRoostAfterEating = false;
    }

    private void eatBerry() {
        Level level = this.bat.level();
        assert this.targetBerry != null;
        BlockState state = level.getBlockState(this.targetBerry);

        if (CaveVines.hasGlowBerries(state)) {
            BlockState newState = state.setValue(CaveVines.BERRIES, false);
            level.setBlock(this.targetBerry, newState, 3);

            level.playSound(null, this.bat.blockPosition(),
                    SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.NEUTRAL,
                    1.0F, 1.0F);

            BatPoopEvent.setWellFed(this.bat);

            shouldRoostAfterEating = true;
            eatCooldown = 1200;

            this.targetBerry = null;
        }
    }

    private void tryToRoost() {
        BlockPos batPos = this.bat.blockPosition();
        BlockPos abovePos = batPos.above();

        if (this.bat.level().getBlockState(abovePos).isRedstoneConductor(this.bat.level(), abovePos)) {
            this.bat.setResting(true);
            shouldRoostAfterEating = false;
            return;
        }
        BlockPos roostSpot = findNearbyRoostingSpot();
        if (roostSpot != null) {
            moveTowardsPosition(Vec3.atCenterOf(roostSpot));
        }
    }

    private void moveTowardsBerry(Vec3 target) {
        Vec3 batPos = this.bat.position();
        Vec3 direction = target.subtract(batPos).normalize().scale(speedModifier);

        Vec3 currentMotion = this.bat.getDeltaMovement();
        Vec3 newMotion = currentMotion.add(
                (direction.x - currentMotion.x) * 0.1,
                (direction.y - currentMotion.y) * 0.1,
                (direction.z - currentMotion.z) * 0.1
        );

        this.bat.setDeltaMovement(newMotion);

        float yaw = (float)(Math.atan2(newMotion.z, newMotion.x) * (180.0 / Math.PI)) - 90.0F;
        this.bat.setYRot(yaw);
    }

    private void moveTowardsPosition(Vec3 target) {
        moveTowardsBerry(target);
    }

    @Nullable
    private BlockPos findNearbyGlowBerry() {
        BlockPos batPos = this.bat.blockPosition();

        for (int x = -searchRange; x <= searchRange; x++) {
            for (int y = -searchRange; y <= searchRange; y++) {
                for (int z = -searchRange; z <= searchRange; z++) {
                    BlockPos checkPos = batPos.offset(x, y, z);
                    BlockState state = this.bat.level().getBlockState(checkPos);

                    if (state.getBlock() instanceof CaveVines && CaveVines.hasGlowBerries(state)) {
                        return checkPos;
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    private BlockPos findNearbyRoostingSpot() {
        BlockPos batPos = this.bat.blockPosition();

        for (int x = -5; x <= 5; x++) {
            for (int y = -3; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos checkPos = batPos.offset(x, y, z);
                    BlockPos abovePos = checkPos.above();

                    if (this.bat.level().getBlockState(abovePos).isRedstoneConductor(
                            this.bat.level(), abovePos) &&
                            this.bat.level().getBlockState(checkPos).isAir()) {
                        return checkPos;
                    }
                }
            }
        }

        return null;
    }
}