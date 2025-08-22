package top.ribs.scguns.entity.monster;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScamplerEntity extends Monster {
    private int fuseTime = 25;
    private boolean hasIgnited = false;

    public ScamplerEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10D)
                .add(Attributes.FOLLOW_RANGE, 12D)
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6F);
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEFINED;
    }
    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance pPotionEffect) {
        MobEffect effect = pPotionEffect.getEffect();

        if (effect == MobEffects.POISON ||
                effect == MobEffects.WITHER ||
                effect == MobEffects.HUNGER ||
                effect == MobEffects.REGENERATION ||
                effect == MobEffects.SATURATION ||
                effect == MobEffects.CONFUSION ||
                effect == MobEffects.BLINDNESS ||
                effect == MobEffects.WEAKNESS ||
                effect == MobEffects.MOVEMENT_SLOWDOWN ||
                effect == MobEffects.DIG_SLOWDOWN ||
                effect == MobEffects.HARM ||
                effect == MobEffects.HEAL) {
            return false;
        }

        return super.canBeAffected(pPotionEffect);
    }
    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            LivingEntity target = this.getTarget();
            if (target != null && !hasIgnited) {
                double distanceToTarget = this.distanceToSqr(target);
                if (distanceToTarget <= 9.0D) {
                    ignite();
                }
            }

            if (hasIgnited) {
                fuseTime--;
                if (fuseTime <= 0) {
                    explode();
                } else if (fuseTime == 20 || fuseTime == 10) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.CREEPER_PRIMED, this.getSoundSource(), 1.0F, 0.8F);
                }
            }
        }
    }

    private void ignite() {
        hasIgnited = true;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FLINTANDSTEEL_USE, this.getSoundSource(), 1.0F, 1.0F);
    }

    private void explode() {
        if (!this.level().isClientSide()) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                    2.0F, Level.ExplosionInteraction.NONE);
            this.discard();
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false)); // Slightly slower attack
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.2, 8));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(ScamplerEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    public int getFuseTime() {
        return fuseTime;
    }

    public boolean isIgnited() {
        return hasIgnited;
    }
}