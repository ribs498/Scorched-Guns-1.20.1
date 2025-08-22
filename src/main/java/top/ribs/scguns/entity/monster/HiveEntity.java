package top.ribs.scguns.entity.monster;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.init.ModEntities;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class HiveEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private int swarmSummonCooldown = 0;

    static final List<SwarmEntity> summonedSwarm = new ArrayList<>();

    public HiveEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public Level getEntityLevel() {
        return this.level();
    }
    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEAD;
    }
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
        if (swarmSummonCooldown > 0) {
            --swarmSummonCooldown;
        }
        summonedSwarm.removeIf(swarm -> !swarm.isAlive());
    }
    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }
    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;
        if (this.getPose() == Pose.STANDING) {
            f = Math.min(pPartialTick * 6F, 1f);
        } else {
            f = 0f;
        }
        this.walkAnimation.update(f, 0.2f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 3f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new HiveSummonGoal(this));
    }

    @Override
    public void die(@NotNull DamageSource cause) {
        super.die(cause);
        for (SwarmEntity swarm : summonedSwarm) {
            if (swarm != null) {
                swarm.discard();
            }
        }
        summonedSwarm.clear();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.1f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5f)
                .add(Attributes.ATTACK_DAMAGE, 2f);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean isHurt = super.hurt(source, amount);
        if (isHurt && swarmSummonCooldown <= 0) {
            summonSwarm();
        }
        return isHurt;
    }

    private boolean canSummonSwarm() {
        return summonedSwarm.isEmpty() || summonedSwarm.stream().noneMatch(SwarmEntity::isActive);
    }

    private void summonSwarm() {
        if (canSummonSwarm()) {
            SwarmEntity swarm = ModEntities.SWARM.get().create(this.level());
            if (swarm != null) {
                swarm.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                this.level().addFreshEntity(swarm);
                summonedSwarm.add(swarm);
                swarmSummonCooldown = 60;
                playSwarmSummonedSound();
            }
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        playAdditionalSound(SoundEvents.BEE_LOOP, 1.5F, getVoicePitch());
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        playAdditionalSound(SoundEvents.BEE_HURT, 1.5F, getVoicePitch());
        return SoundEvents.ZOMBIE_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        playAdditionalSound(SoundEvents.BEE_DEATH, 1.5F, getVoicePitch());
        return SoundEvents.ZOMBIE_DEATH;
    }

    private void playAdditionalSound(SoundEvent soundEvent, float volume, float pitch) {
        if (this.level().isClientSide()) {
            this.level().playSound(null, this.blockPosition(), soundEvent, SoundSource.HOSTILE, volume, pitch);
        }
    }

    private void playSwarmSummonedSound() {
        if (!this.level().isClientSide()) {
            SoundEvent soundEvent = SoundEvents.BEEHIVE_EXIT;
            this.level().playSound(null, this.blockPosition(), soundEvent, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 1.3F;
    }

    ///GOALS

    public static class HiveSummonGoal extends Goal {
        private final HiveEntity hiveEntity;

        public HiveSummonGoal(HiveEntity hiveEntity) {
            this.hiveEntity = hiveEntity;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return hiveEntity.getTarget() != null && hiveEntity.swarmSummonCooldown <= 0;
        }

        @Override
        public void start() {
            hiveEntity.summonSwarm();
        }

        @Override
        public void tick() {
            if (hiveEntity.swarmSummonCooldown <= 0 && hiveEntity.canSummonSwarm()) {
                hiveEntity.summonSwarm();
            }
        }
    }
}

