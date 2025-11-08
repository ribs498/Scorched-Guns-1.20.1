package top.ribs.scguns.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.Config;
import top.ribs.scguns.entity.projectile.TraumaHookEntity;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModEntities;

import java.util.EnumSet;

public class TraumaUnitEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(TraumaUnitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TIMEOUT =
            SynchedEntityData.defineId(TraumaUnitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HOOKED_ENTITY_ID =
            SynchedEntityData.defineId(TraumaUnitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PRIMED =
            SynchedEntityData.defineId(TraumaUnitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final float LOW_HEALTH_THRESHOLD = 0.35f;
    private static final float EXPLOSION_DAMAGE = 8.0f;
    private static final float EXPLOSION_RADIUS = 3.5f;
    private int primeTicks = 0;
    private static final int HOOK_DURATION = 5;
    private int hookTicks = 0;
    private int oldSwell;
    private int swell;
    private static final int MAX_SWELL = 40;
    public TraumaUnitEntity(EntityType<? extends TraumaUnitEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
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
                effect == MobEffects.HEAL
        ) {
            return false;
        }

        return super.canBeAffected(pPotionEffect);
    }
    public final AnimationState idleAnimationState = new AnimationState();

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 24D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.5f)
                .add(Attributes.ARMOR, 3f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8f)
                .add(Attributes.ATTACK_DAMAGE, 6f);
    }
    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.isAttacking() && this.getAttackTimeout() > 0) {
                this.setAttackTimeout(this.getAttackTimeout() - 1);
                if (this.getAttackTimeout() <= 0) {
                    this.setAttacking(false);
                }
            }

            manageHookLifecycle();

            TraumaHookEntity hook = findActiveHook();
            if (hook != null && hook.getHookedIn() != null && hook.getHookedIn() instanceof LivingEntity hookedEntity) {
                if (getHookedEntity() == null) {
                    setHookedEntity(hookedEntity);
                    hookTicks = 0;
                }
            } else if (hook == null) {
                setHookedEntity(null);
            }

            LivingEntity hooked = getHookedEntity();
            if (hooked != null) {
                hookTicks++;
                double dist = distanceTo(hooked);

                if (hookTicks >= HOOK_DURATION || dist <= 2.0) {

                    setHookedEntity(null);
                    hookTicks = 0;
                    if (hook != null) {
                        hook.discard();
                    }
                    if (dist <= 2.5) {
                        doHurtTarget(hooked);
                    }
                } else {
                    Vec3 direction = position().subtract(hooked.position()).normalize();
                    double pullStrength = 1.0;
                    Vec3 pull = direction.scale(pullStrength).add(0, 0.15, 0);
                    hooked.setDeltaMovement(hooked.getDeltaMovement().add(pull));
                    hooked.hurtMarked = true;
                }
            }

            LivingEntity target = getTarget();
            if (target != null && isLowHealth()) {
                double dist = distanceTo(target);

                if (isPrimed()) {
                    if (this.swell >= MAX_SWELL) {
                        this.swell = MAX_SWELL;
                        explode();
                        return;
                    }
                } else if (dist <= 3.0) {
                    setPrimed(true);
                    this.level().broadcastEntityEvent(this, (byte) 5);
                    this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
                }
            } else {
                if (isPrimed() && !isLowHealth()) {
                    setPrimed(false);
                }
            }
        }

        if (this.isAlive()) {
            this.oldSwell = this.swell;
            if (isPrimed()) {
                this.swell++;
            } else if (this.swell > 0) {
                this.swell--;
            }

            if (this.swell < 0) {
                this.swell = 0;
            }
        }

        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }
    public float getSwelling(float partialTicks) {
        return Mth.lerp(partialTicks, (float)this.oldSwell, (float)this.swell) / (float)(MAX_SWELL - 2);
    }
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            float skeletonChance = 0.75f;
            float beaconChance = Math.min(Config.COMMON.gameplay.cogBeaconSpawnChance.get().floatValue(), 0.25f);

            float rand = this.random.nextFloat();

            if (rand < skeletonChance) {
                Skeleton skeleton = new Skeleton(EntityType.SKELETON, this.level());
                skeleton.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                skeleton.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                this.level().addFreshEntity(skeleton);
            }
            else if (rand < skeletonChance + beaconChance && source.getEntity() instanceof Player) {
                SignalBeaconEntity beacon = new SignalBeaconEntity(ModEntities.SIGNAL_BEACON.get(), this.level());
                beacon.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                this.level().addFreshEntity(beacon);
            }
        }
    }
    private TraumaHookEntity findActiveHook() {
        for (TraumaHookEntity hook : level().getEntitiesOfClass(TraumaHookEntity.class, getBoundingBox().inflate(20.0))) {
            if (hook.getOwner() == this && !hook.isRemoved()) {
                return hook;
            }
        }
        return null;
    }
    private void manageHookLifecycle() {
        TraumaHookEntity hook = findActiveHook();
        if (hook != null) {
            LivingEntity target = this.getTarget();
            if (target == null ||
                    this.distanceTo(target) < 3.0 ||
                    (hook.onGround() && hook.getHookedIn() == null && hook.tickCount > 20)) {

                if (!hook.isRetracting()) {
                }
            }
        }
    }
    private void setupAnimationStates() {
        if (!this.idleAnimationState.isStarted()) {
            this.idleAnimationState.start(this.tickCount);
        }
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
        if (attacking) {
            this.setAttackTimeout(15);
        }
    }

    public void setAttackTimeout(int timeout) {
        this.entityData.set(ATTACK_TIMEOUT, timeout);
    }

    public int getAttackTimeout() {
        return this.entityData.get(ATTACK_TIMEOUT);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setHookedEntity(@Nullable LivingEntity entity) {
        this.entityData.set(HOOKED_ENTITY_ID, entity == null ? 0 : entity.getId());
    }

    @Nullable
    public LivingEntity getHookedEntity() {
        int id = this.entityData.get(HOOKED_ENTITY_ID);
        if (id == 0) return null;
        Entity entity = this.level().getEntity(id);
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(ATTACK_TIMEOUT, 0);
        this.entityData.define(HOOKED_ENTITY_ID, 0);
        this.entityData.define(PRIMED, false);
    }
    public void setPrimed(boolean primed) {
        this.entityData.set(PRIMED, primed);
    }

    public boolean isPrimed() {
        return this.entityData.get(PRIMED);
    }

    public boolean isLowHealth() {
        return this.getHealth() / this.getMaxHealth() <= LOW_HEALTH_THRESHOLD;
    }

    private void explode() {
        if (!this.level().isClientSide) {
            this.level().getEntitiesOfClass(LivingEntity.class,
                            this.getBoundingBox().inflate(EXPLOSION_RADIUS),
                            entity -> entity != this && entity.isAlive())
                    .forEach(entity -> {
                        float distance = this.distanceTo(entity);
                        float damageFactor = 1.0f - (distance / EXPLOSION_RADIUS);
                        float damage = EXPLOSION_DAMAGE * Math.max(0.0f, damageFactor);

                        entity.hurt(this.damageSources().explosion(this, this), damage);

                        Vec3 direction = entity.position().subtract(this.position()).normalize();
                        entity.setDeltaMovement(entity.getDeltaMovement().add(
                                direction.x * 0.8 * damageFactor,
                                0.4 * damageFactor,
                                direction.z * 0.8 * damageFactor
                        ));
                    });

            this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                    2.5f, Level.ExplosionInteraction.NONE);

            this.playSound(SoundEvents.GENERIC_EXPLODE, 1.5F, 0.9F);
            this.level().broadcastEntityEvent(this, (byte) 6);
            this.discard();
        }
    }
    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 6) {
            for (int i = 0; i < 50; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = this.random.nextDouble() * 1.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;

                this.level().addParticle(ParticleTypes.EXPLOSION,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0.0, 0.0, 0.0);
            }

            for (int i = 0; i < 60; i++) {
                double velocityX = (this.random.nextDouble() - 0.5) * 0.3;
                double velocityY = this.random.nextDouble() * 0.4;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.3;

                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX(),
                        this.getY() + 0.5,
                        this.getZ(),
                        velocityX, velocityY, velocityZ);
            }
        } else {
            super.handleEntityEvent(pId);
        }
    }
    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f = this.getPose() == Pose.STANDING ? Math.min(pPartialTick * 6F, 1f) : 0f;
        this.walkAnimation.update(f, 0.2f);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new CastHookGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false) {
            @Override
            protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
                if (pDistToEnemySqr <= this.getAttackReachSqr(pEnemy) && this.getTicksUntilNextAttack() <= 0 && !TraumaUnitEntity.this.isAttacking()) {
                    TraumaUnitEntity.this.setAttacking(true);
                    this.resetAttackCooldown();
                    this.mob.swing(InteractionHand.MAIN_HAND);
                    this.mob.doHurtTarget(pEnemy);
                }
            }
        });
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.0, 30));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 3f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(TraumaUnitEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !((Player) player).isSpectator()));
    }

    private static class CastHookGoal extends Goal {
        private final TraumaUnitEntity mob;
        private int cooldown = 0;
        private LivingEntity lastTarget = null;

        public CastHookGoal(TraumaUnitEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }

            LivingEntity target = mob.getTarget();
            if (target == null) {
                return false;
            }

            double distance = mob.distanceTo(target);
            boolean inRange = distance > 3.0 && distance < 28.0;
            boolean noHookedEntity = mob.getHookedEntity() == null;
            boolean hasLineOfSight = mob.hasLineOfSight(target);
            boolean noActiveHook = mob.findActiveHook() == null;

            boolean newTarget = lastTarget != target;

            return inRange && noHookedEntity && hasLineOfSight && noActiveHook &&
                    (newTarget || mob.getRandom().nextFloat() < 0.3f);
        }

        @Override
        public void start() {
            LivingEntity target = mob.getTarget();
            if (target == null) {
                return;
            }

            lastTarget = target;

            Vec3 mobPos = mob.position().add(0, mob.getEyeHeight() * 0.8, 0);
            Vec3 forward = Vec3.directionFromRotation(0, mob.getYRot()).scale(0.5);
            Vec3 hookStartPos = mobPos.add(forward);

            TraumaHookEntity hook = new TraumaHookEntity(ModEntities.TRAUMA_HOOK.get(), mob, mob.level());
            hook.moveTo(hookStartPos.x, hookStartPos.y, hookStartPos.z, mob.getYRot(), 0);

            Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.5, 0);
            Vec3 targetVelocity = target.getDeltaMovement();

            double timeToTarget = hookStartPos.distanceTo(targetPos) / 1.25;
            Vec3 predictedPos = targetPos.add(targetVelocity.scale(timeToTarget * 0.7));

            Vec3 direction = predictedPos.subtract(hookStartPos);
            direction = direction.add(0, Math.min(direction.horizontalDistance() * 0.1, 2.0), 0);

            hook.shoot(direction.x, direction.y, direction.z, 1.5f, 0.5f);

            boolean added = mob.level().addFreshEntity(hook);
            if (added) {
                mob.setAttacking(true);
                mob.playSound(SoundEvents.FISHING_BOBBER_THROW, 0.8F, 0.8F + mob.getRandom().nextFloat() * 0.4F);
                cooldown = 40 + mob.getRandom().nextInt(40);
            } else {
                cooldown = 20;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    public boolean canBreatheUnderwater() {
        return true;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.onGround() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
    }
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HookTicks", this.hookTicks);
        tag.putInt("PrimeTicks", this.primeTicks);
        tag.putShort("Swell", (short)this.swell);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.hookTicks = tag.getInt("HookTicks");
        this.primeTicks = tag.getInt("PrimeTicks");
        this.swell = tag.getShort("Swell");
        this.oldSwell = this.swell;
    }
}