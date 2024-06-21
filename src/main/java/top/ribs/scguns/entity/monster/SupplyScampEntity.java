package top.ribs.scguns.entity.monster;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class SupplyScampEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> PANICKING =
            SynchedEntityData.defineId(SupplyScampEntity.class, EntityDataSerializers.BOOLEAN);

    public SupplyScampEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(true);
    }
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState panicAnimationState = new AnimationState();
    public int panicAnimationTimeout = 0;

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        if (this.isPanicked()) {
            if (panicAnimationTimeout <= 0) {
                panicAnimationTimeout = 50;
                panicAnimationState.start(this.tickCount);
            }
            --panicAnimationTimeout;
        } else {
            panicAnimationState.stop();
        }
    }

    public boolean isPanicked() {
        return this.entityData.get(PANICKING);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 24D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0f)
                .add(Attributes.ARMOR, 6f);
    }
    protected void registerGoals() {

        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.0, 30));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(1, new SupplyScampPanicGoal(this, 2.5));
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PANICKING, false);
    }
    @Override
    protected void updateWalkAnimation(float partialTick) {
        float f = (this.getPose() == Pose.STANDING) ? Math.min(partialTick * 6F, 1f) : 0f;
        this.walkAnimation.update(f, 0.2f);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    public class SupplyScampPanicGoal extends Goal {
        private final PanicGoal wrappedPanicGoal;
        private final SupplyScampEntity scamp;

        public SupplyScampPanicGoal(SupplyScampEntity scamp, double speedModifier) {
            this.scamp = scamp;
            this.wrappedPanicGoal = new PanicGoal(scamp, speedModifier);
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return wrappedPanicGoal.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return wrappedPanicGoal.canContinueToUse();
        }

        @Override
        public void start() {
            wrappedPanicGoal.start();
            scamp.setPanicking(true);
        }

        @Override
        public void stop() {
            wrappedPanicGoal.stop();
            scamp.setPanicking(false);
        }
    }

    private void setPanicking(boolean b) {
        this.entityData.set(PANICKING, b);
    }

}
