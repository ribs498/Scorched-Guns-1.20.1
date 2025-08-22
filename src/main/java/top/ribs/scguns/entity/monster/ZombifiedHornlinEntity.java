package top.ribs.scguns.entity.monster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.entity.projectile.BrassBoltEntity;
import top.ribs.scguns.init.ModSounds;

import java.util.UUID;

public class ZombifiedHornlinEntity extends Monster implements RangedAttackMob, NeutralMob {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(ZombifiedHornlinEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MUZZLE_FLASH_TIMER = SynchedEntityData.defineId(ZombifiedHornlinEntity.class, EntityDataSerializers.INT);

    // Neutral mob behavior attributes
    private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost", 0.05D, AttributeModifier.Operation.ADDITION);
    private static final UniformInt FIRST_ANGER_SOUND_DELAY = TimeUtil.rangeOfSeconds(0, 1);
    private int playFirstAngerSoundIn;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;
    private static final int ALERT_RANGE_Y = 10;
    private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(1, 2);
    private int ticksUntilNextAlert;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    public int attackAnimationTimeout = 0;
    private int idleAnimationTimeout = 0;

    private int shotsFired = 0;
    private int burstCooldown = 0;
    private int attackTime = -1;
    private boolean burstInterrupted = false;

    public ZombifiedHornlinEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEAD;
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30D)
                .add(Attributes.FOLLOW_RANGE, 26D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.5f)
                .add(Attributes.ARMOR, 2f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8f)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 10, 15.0F) {
            @Override
            public void stop() {
                super.stop();
                ZombifiedHornlinEntity.this.setAttacking(false);
            }

            @Override
            public void start() {
                super.start();
                ZombifiedHornlinEntity.this.setAttacking(true);
            }
        });
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        // Updated targeting goals with creative/spectator checks for neutral mob behavior
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (player) -> this.isAngryAt(player) && !((Player) player).isCreative() && !((Player) player).isSpectator()));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    public void triggerMuzzleFlash() {
        this.entityData.set(MUZZLE_FLASH_TIMER, 10);
    }

    public boolean isMuzzleFlashVisible() {
        return this.entityData.get(MUZZLE_FLASH_TIMER) > 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            setupAnimationStates();
            spawnSmokeParticles();
        }

        int currentTimer = this.entityData.get(MUZZLE_FLASH_TIMER);
        if (currentTimer > 0) {
            this.entityData.set(MUZZLE_FLASH_TIMER, currentTimer - 1);
        }

        if (this.isAttacking()) {
            if (this.burstCooldown > 0) {
                this.burstCooldown--;
            } else {
                if (--this.attackTime <= 0) {
                    this.attackTime = 9;
                    LivingEntity target = this.getTarget();
                    if (target != null && !this.burstInterrupted) {
                        this.performRangedAttack(target, 1.0F);
                        this.shotsFired++;

                        if (this.shotsFired >= 3 + this.random.nextInt(3)) {
                            this.burstCooldown = 40 + this.random.nextInt(60);
                            this.shotsFired = 0;
                            this.burstInterrupted = false;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        // Handle speed boost when angry (like zombified piglin)
        AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (this.isAngry()) {
            if (!this.isBaby() && !attributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING)) {
                attributeinstance.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }
            this.maybePlayFirstAngerSound();
        } else if (attributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING)) {
            attributeinstance.removeModifier(SPEED_MODIFIER_ATTACKING);
        }

        // Update persistent anger timer
        this.updatePersistentAnger((ServerLevel)this.level(), true);

        // Alert other zombified entities when targeting
        if (this.getTarget() != null) {
            this.maybeAlertOthers();
        }

        if (this.isAngry()) {
            this.lastHurtByPlayerTime = this.tickCount;
        }

        // If we don't have a target but are still angry at a player, try to re-target them
        if (this.getTarget() == null && this.isAngry() && this.getPersistentAngerTarget() != null) {
            Player persistentTarget = this.level().getPlayerByUUID(this.getPersistentAngerTarget());
            if (persistentTarget != null && this.distanceToSqr(persistentTarget) <= this.getAttributeValue(Attributes.FOLLOW_RANGE) * this.getAttributeValue(Attributes.FOLLOW_RANGE)) {
                this.setTarget(persistentTarget);
            }
        }

        super.customServerAiStep();
    }

    private void maybePlayFirstAngerSound() {
        if (this.playFirstAngerSoundIn > 0) {
            --this.playFirstAngerSoundIn;
            if (this.playFirstAngerSoundIn == 0) {
                this.playAngerSound();
            }
        }
    }

    private void maybeAlertOthers() {
        if (this.ticksUntilNextAlert > 0) {
            --this.ticksUntilNextAlert;
        } else {
            if (this.getSensing().hasLineOfSight(this.getTarget())) {
                this.alertOthers();
            }
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }
    }

    private void alertOthers() {
        double followRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB alertArea = AABB.unitCubeFromLowerCorner(this.position()).inflate(followRange, ALERT_RANGE_Y, followRange);

        // Alert other zombified hornlins
        this.level().getEntitiesOfClass(ZombifiedHornlinEntity.class, alertArea, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(entity -> entity != this)
                .filter(entity -> entity.getTarget() == null)
                .filter(entity -> !entity.isAlliedTo(this.getTarget()))
                .forEach(entity -> {
                    entity.setTarget(this.getTarget());
                    entity.startPersistentAngerTimer();
                    if (this.getTarget() instanceof Player) {
                        entity.setPersistentAngerTarget(this.getTarget().getUUID());
                    }
                });

        // Alert zombified piglins too (pack behavior) - make it more immediate
        this.level().getEntitiesOfClass(ZombifiedPiglin.class, alertArea, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(entity -> entity.getTarget() == null)
                .filter(entity -> !entity.isAlliedTo(this.getTarget()))
                .forEach(entity -> {
                    if (this.getTarget() instanceof Player player) {
                        // Set the player as the one who last hurt them
                        entity.setLastHurtByPlayer(player);
                        // Start their anger timer immediately
                        entity.startPersistentAngerTimer();
                        entity.setPersistentAngerTarget(player.getUUID());
                        // Set target immediately
                        entity.setTarget(this.getTarget());

                        // Force immediate alert state - use reflection to bypass the delay
                        try {
                            // Set their internal alert timer to 0 so they act immediately
                            java.lang.reflect.Field ticksUntilNextAlertField = entity.getClass().getDeclaredField("ticksUntilNextAlert");
                            ticksUntilNextAlertField.setAccessible(true);
                            ticksUntilNextAlertField.setInt(entity, 0);

                            // Set their first anger sound delay to 0
                            java.lang.reflect.Field playFirstAngerSoundInField = entity.getClass().getDeclaredField("playFirstAngerSoundIn");
                            playFirstAngerSoundInField.setAccessible(true);
                            playFirstAngerSoundInField.setInt(entity, 0);
                        } catch (Exception e) {
                            // Silently continue - not critical
                        }
                    }
                });
    }

    private void playAngerSound() {
        this.playSound(SoundEvents.ZOMBIFIED_PIGLIN_ANGRY, this.getSoundVolume() * 2.0F, this.getVoicePitch() * 1.8F);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.getTarget() == null && target != null) {
            this.playFirstAngerSoundIn = FIRST_ANGER_SOUND_DELAY.sample(this.random);
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }

        if (target instanceof Player) {
            this.setLastHurtByPlayer((Player) target);
        }

        super.setTarget(target);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        boolean result = super.hurt(damageSource, amount);

        // If hurt during a burst, interrupt it with some chance
        if (result && this.isAttacking() && this.burstCooldown == 0 && this.shotsFired > 0) {
            if (this.random.nextFloat() < 0.4F) { // 40% chance to interrupt burst
                this.burstInterrupted = true;
                this.burstCooldown = 15 + this.random.nextInt(15); // Shorter interrupt cooldown
                this.shotsFired = 0;
            }
        }

        return result;
    }

    private void spawnSmokeParticles() {
        if (this.isMuzzleFlashVisible()) {
            double offsetX = 0.0;
            double offsetY = this.getEyeHeight() - 0.5;
            double offsetZ = 0.0;

            double posX = this.getX() + offsetX;
            double posY = this.getY() + offsetY;
            double posZ = this.getZ() + offsetZ;
            RandomSource random = this.getRandom();

            for (int i = 0; i < 3; i++) {
                double particleOffsetX = random.nextGaussian() * 0.1;
                double particleOffsetY = random.nextGaussian() * 0.1;
                double particleOffsetZ = random.nextGaussian() * 0.1;
                this.level().addParticle(ParticleTypes.SMOKE, posX, posY, posZ, particleOffsetX, particleOffsetY, particleOffsetZ);
            }
        }
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
        if (this.isAttacking()) {
            if (attackAnimationTimeout <= 0) {
                attackAnimationTimeout = 20;
                attackAnimationState.start(this.tickCount);
            }
            --attackAnimationTimeout;
        } else {
            attackAnimationState.stop();
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(MUZZLE_FLASH_TIMER, 0);
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAngry() ? SoundEvents.ZOMBIFIED_PIGLIN_ANGRY : SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.ZOMBIFIED_PIGLIN_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F; // Slightly louder than normal
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.7F; // Lower pitch (0.7 base instead of 1.0)
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float distanceFactor) {
        BrassBoltEntity projectile = new BrassBoltEntity(this.level(), this);
        double offsetX = 0.0;
        double offsetY = this.getEyeHeight() - 0.5;
        double offsetZ = -0.5;
        double d0 = target.getX() - (this.getX() + offsetX);
        double d1 = target.getEyeY() - (this.getY() + offsetY);
        double d2 = target.getZ() - (this.getZ() + offsetZ);
        projectile.setPos(this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ);

        float inaccuracy = 0.15F;
        projectile.shoot(d0, d1, d2, 3.0F, inaccuracy);

        this.level().addFreshEntity(projectile);
        this.playSound(ModSounds.GREASER_SMG_FIRE.get(), 1.0F, this.getVoicePitch());
        this.triggerMuzzleFlash();
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    // NeutralMob implementation
    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.remainingPersistentAngerTime = time;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.addPersistentAngerSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readPersistentAngerSaveData(this.level(), tag);
    }

    @Override
    public boolean isPreventingPlayerRest(Player player) {
        return this.isAngryAt(player);
    }
}