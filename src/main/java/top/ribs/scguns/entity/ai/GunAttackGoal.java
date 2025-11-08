package top.ribs.scguns.entity.ai;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModSounds;
import top.ribs.scguns.item.GunItem;

import static top.ribs.scguns.event.GunEventBus.ejectCasing;

public class GunAttackGoal<T extends PathfinderMob> extends Goal {
    protected final T shooter;
    protected final double speedModifier;
    protected int seeTime;
    protected int attackTime;
    protected final float attackRadiusSqr;
    protected double idealRange;
    protected double minRange;

    protected float accuracyModifier = 1.0F;

    protected int strafingTime = -1;
    protected boolean shouldStrafe = false;
    protected float strafeAmount = 0.0F;

    protected int aimingStabilityTimer = 0;
    protected static final int MIN_AIM_TIME = 10;

    protected int burstIntervalTimer = 0;
    protected int remainingBursts = 0;
    protected int burstResetTimer = 0;

    protected int reloadTick = 0;
    protected boolean isReloading = false;

    protected boolean isPanicked = false;
    protected int panickTimer = 0;

    protected AIType aiType;

    protected Vec3 lastKnownPosition;

    protected int burstAmount = 3;
    protected int burstTimer = 15;

    protected static final float ROTATION_SPEED = 15.0F;

    public GunAttackGoal(T shooter, ItemStack gunStack, float speedModifier, AIType aiType, int difficulty) {
        this.shooter = shooter;
        this.speedModifier = speedModifier;
        this.attackTime = -1;
        this.aiType = aiType;
        shooter.addTag("AI_" + aiType.name());

        if (gunStack.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(gunStack);
            this.idealRange = gun.getIdealAttackRange();
            this.minRange = gun.getMinAttackRange();
        } else {
            this.idealRange = 15.0;
            this.minRange = 8.0;
        }

        this.attackRadiusSqr = (float) (this.idealRange * this.idealRange);

        if (this.shooter.getTarget() != null) {
            this.lastKnownPosition = this.shooter.getTarget().position();
        }

        float baseAccuracy = switch(aiType) {
            case TACTICAL -> 2.5F;
            case SMART -> 2.2F;
            case DEFAULT -> 2.0F;
            case RECKLESS -> 1.2F;
            case COWARD -> 1.5F;
        };

        float difficultyBonus = 1.0F + ((difficulty - 1) * 0.3F);
        this.accuracyModifier = baseAccuracy * difficultyBonus;

        this.burstAmount = 2 + (difficulty / 2);

        float burstDelayMultiplier = getBurstDelayMultiplier(shooter.level().getDifficulty());
        float configBurstMultiplier = Config.COMMON.gameplay.mobBurstDelayMultiplier.get().floatValue();
        this.burstTimer = Math.max(1, (int)((30 - (difficulty * 4)) * burstDelayMultiplier * configBurstMultiplier));
    }


    private float getBurstDelayMultiplier(Difficulty difficulty) {
        return switch(difficulty) {
            case PEACEFUL -> 2.0F;
            case EASY -> 1.5F;
            case NORMAL -> 1.0F;
            case HARD -> 0.6F;
        };
    }

    @Override
    public boolean canUse() {
        return this.shooter.getTarget() != null && this.isHoldingGun() && !this.shooter.getTarget().isDeadOrDying();
    }

    protected boolean isHoldingGun() {
        return this.shooter.isHolding((itemStack) -> itemStack.getItem() instanceof GunItem);
    }

    @Override
    public void start() {
        super.start();
        this.shooter.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.shooter.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.shooter.stopUsingItem();
        this.reloadTick = 0;
        this.isReloading = false;
        this.strafingTime = -1;
        this.shouldStrafe = false;
        this.aimingStabilityTimer = 0;
        this.shooter.removeTag("AI_" + this.aiType.name());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.shooter.getTarget();
        ItemStack heldItem = this.shooter.getMainHandItem();

        if (this.shooter.hasEffect(ModEffects.BLINDED.get()) || this.shooter.hasEffect(ModEffects.DEAFENED.get()))
            this.isPanicked = true;

        if (target != null && heldItem.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);

            double distanceToTarget = this.shooter.distanceToSqr(target.getX(), target.getY(), target.getZ());
            boolean canSeeTarget = this.shooter.getSensing().hasLineOfSight(target);
            boolean sawTargetPreviously = this.seeTime > 0;

            if (canSeeTarget != sawTargetPreviously) {
                this.seeTime = 0;
            }

            if (this.isReloading) {
                ++this.seeTime;
            } else if (canSeeTarget) {
                this.lastKnownPosition = new Vec3(target.getX(), target.getY(), target.getZ());
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            if (this.aiType == AIType.COWARD &&
                    (this.shooter.getHealth() < (this.shooter.getMaxHealth() / 3) || this.shooter.invulnerableTime != 0) ||
                    this.shooter.hasEffect(ModEffects.BLINDED.get())) {
                this.isPanicked = true;
                this.panickTimer = 20;
            }

            if (this.isPanicked) {
                if (this.shooter.tickCount % 10 == 0) {
                    Vec3 vec3 = DefaultRandomPos.getPos(this.shooter, 5, 4);
                    if (vec3 != null) {
                        this.shooter.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, this.speedModifier * 1.5);
                    }
                }
                this.panickTimer--;
                if (this.panickTimer <= 0) {
                    this.isPanicked = false;
                }
                return;
            }

            if (heldItem.getTag().getInt("AmmoCount") <= 0) {
                if (!this.isReloading) {
                    if (this.aiType == AIType.TACTICAL) {
                        Vec3 coverLocation = findCoverLocation();
                        this.shooter.getNavigation().moveTo(coverLocation.x, coverLocation.y, coverLocation.z, this.speedModifier);
                    }
                    this.isReloading = true;
                    this.reloadTick = gun.getReloads().getReloadTimer();
                    this.shooter.level().playSound(null, this.shooter.getX(), this.shooter.getY(), this.shooter.getZ(),
                            ModSounds.ITEM_PISTOL_RELOAD.get(), SoundSource.HOSTILE, 1.0F, 1F);
                } else if (this.reloadTick == 0) {
                    heldItem.getTag().putInt("AmmoCount", gun.getReloads().getMaxAmmo());
                    this.shooter.level().playSound(null, this.shooter.getX(), this.shooter.getY(), this.shooter.getZ(),
                            ModSounds.ITEM_PISTOL_COCK.get(), SoundSource.HOSTILE, 1.0F, 1F);
                    this.isReloading = false;
                } else {
                    this.shooter.getNavigation().stop();
                    --this.reloadTick;
                }
                return;
            }

            boolean inRange = distanceToTarget <= this.attackRadiusSqr;
            boolean tooClose = distanceToTarget < (this.minRange * this.minRange);
            boolean isRetreating = false;
            boolean isMovingFast = this.shooter.getDeltaMovement().horizontalDistanceSqr() > 0.01;
            boolean isNavigating = !this.shooter.getNavigation().isDone();

            if (!inRange || !canSeeTarget) {
                if (this.shooter.tickCount % 20 == 0 || this.shooter.getNavigation().isDone()) {
                    if (this.aiType == AIType.RECKLESS) {
                        this.shooter.getNavigation().moveTo(target, this.speedModifier * 1.2);
                    } else {
                        this.shooter.getNavigation().moveTo(target, this.speedModifier);
                    }
                }
                this.shouldStrafe = false;
                this.strafingTime = -1;
                this.aimingStabilityTimer = 0;
            } else if (tooClose && this.aiType != AIType.RECKLESS) {
                Vec3 awayVector = this.shooter.position().subtract(target.position()).normalize();
                Vec3 retreatPos = this.shooter.position().add(awayVector.scale(2.0));
                this.shooter.getNavigation().moveTo(retreatPos.x, retreatPos.y, retreatPos.z, this.speedModifier * 0.8);
                this.shouldStrafe = false;
                this.strafingTime = -1;
                this.aimingStabilityTimer = 0;
                isRetreating = true;

                if (this.aiType == AIType.SMART && distanceToTarget > (this.minRange * this.minRange * 0.8)) {
                    this.shooter.getNavigation().stop();
                    isRetreating = false;
                }
            } else {
                this.shooter.getNavigation().stop();
                if (this.aiType != AIType.RECKLESS) {
                    if (this.strafingTime < 0) {
                        if (this.shooter.getRandom().nextFloat() < 0.2F) {
                            this.shouldStrafe = true;
                            this.strafingTime = 20 + this.shooter.getRandom().nextInt(20);
                            this.strafeAmount = this.shooter.getRandom().nextBoolean() ? 0.5F : -0.5F;
                        } else {
                            this.shouldStrafe = false;
                            this.strafingTime = 40 + this.shooter.getRandom().nextInt(40);
                        }
                    }

                    if (this.strafingTime > 0) {
                        --this.strafingTime;
                        if (this.shouldStrafe) {
                            this.shooter.getMoveControl().strafe(0.0F, this.strafeAmount * 0.5F);
                        }
                    }
                }
            }

            if (canSeeTarget && !isRetreating && !isMovingFast) {
                updateSmoothRotation(target);
                if (this.aimingStabilityTimer < MIN_AIM_TIME) {
                    this.aimingStabilityTimer++;
                }
            } else if (canSeeTarget && this.aiType == AIType.SMART && isNavigating) {
                this.aimingStabilityTimer = 0;
            } else {
                this.aimingStabilityTimer = 0;
            }

            boolean isStableAndAimed = this.aimingStabilityTimer >= MIN_AIM_TIME;
            boolean canShootWhileMoving = this.aiType == AIType.RECKLESS ||
                    (this.aiType != AIType.SMART && !isNavigating);
            boolean smartShouldShoot = this.aiType == AIType.SMART &&
                    !isNavigating &&
                    !isMovingFast &&
                    this.shooter.getNavigation().isDone();

            if (inRange && canSeeTarget && this.seeTime >= 5 && !isRetreating &&
                    (canShootWhileMoving || smartShouldShoot) && !isMovingFast && isStableAndAimed) {
                if (this.shooter.getMainHandItem().getTag().getInt("AmmoCount") > 0) {
                    if (--this.attackTime <= 0) {
                        float configBurstMultiplier = Config.COMMON.gameplay.mobBurstDelayMultiplier.get().floatValue();

                        if (remainingBursts <= 0 && burstResetTimer <= 0) {
                            remainingBursts = 1 + this.shooter.level().random.nextInt(this.burstAmount);
                            burstIntervalTimer = 1 + this.shooter.level().random.nextInt(this.burstTimer);
                            burstResetTimer = Math.max(5, (int)((40 + this.shooter.level().random.nextInt(40)) * configBurstMultiplier));
                        }

                        if (this.shooter.hasEffect(ModEffects.BLINDED.get()) && !this.aiType.equals(AIType.TACTICAL)) {
                            burstResetTimer = 0;
                        }

                        if (remainingBursts > 0 && --burstIntervalTimer <= 0) {
                            shoot(target, gun);
                            remainingBursts--;
                            burstIntervalTimer = 2 + this.shooter.level().random.nextInt(6);
                        }

                        if (remainingBursts <= 0) {
                            burstResetTimer--;
                        }
                    }
                }
            }
        }
    }

    private void updateSmoothRotation(LivingEntity target) {
        Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.8, 0);
        Vec3 shooterPos = this.shooter.position().add(0, this.shooter.getEyeHeight(), 0);
        Vec3 toTarget = targetPos.subtract(shooterPos);

        double horizontalDist = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        float desiredYaw = (float)(Math.atan2(toTarget.z, toTarget.x) * (180.0 / Math.PI)) - 90.0F;
        float desiredPitch = (float)(-(Math.atan2(toTarget.y, horizontalDist) * (180.0 / Math.PI)));

        float yawDiff = desiredYaw - this.shooter.getYRot();
        while (yawDiff > 180.0F) yawDiff -= 360.0F;
        while (yawDiff < -180.0F) yawDiff += 360.0F;

        float newYaw = this.shooter.getYRot() + Math.max(-ROTATION_SPEED, Math.min(ROTATION_SPEED, yawDiff));
        float newPitch = this.shooter.getXRot() + Math.max(-ROTATION_SPEED, Math.min(ROTATION_SPEED, desiredPitch - this.shooter.getXRot()));

        this.shooter.setYRot(newYaw);
        this.shooter.setXRot(newPitch);
        this.shooter.yBodyRot = newYaw;
        this.shooter.yBodyRotO = this.shooter.yBodyRot;
        this.shooter.yHeadRot = newYaw;
        this.shooter.yHeadRotO = this.shooter.yHeadRot;
    }

    private void shoot(LivingEntity target, Gun gun) {
        if (this.shooter.hasEffect(ModEffects.BLINDED.get())) {
            if (this.shooter.getRandom().nextBoolean()) {
                return;
            }
        }

        ItemStack heldItem = this.shooter.getMainHandItem();
        AIGunEvent.performGunAttack(this.shooter, target, heldItem, gun, this.accuracyModifier);

        int baseRate = gun.getGeneral().getRate();
        float configMultiplier = Config.COMMON.gameplay.mobFireRateMultiplier.get().floatValue();
        this.attackTime = (int)(baseRate * configMultiplier);

        consumeAmmo(heldItem);
        if (this.shooter.getMainHandItem().getItem() instanceof GunItem) {
            ejectCasing(this.shooter.level(), this.shooter, false);
        }
        ResourceLocation fireSound = gun.getSounds().getFire();
        if(fireSound != null) {
            double posX = this.shooter.getX();
            double posY = this.shooter.getY() + this.shooter.getEyeHeight();
            double posZ = this.shooter.getZ();
            float volume = Config.COMMON.gameplay.mobGunfireVolume.get();
            float pitch = 0.9F + this.shooter.level().random.nextFloat() * 0.2F;
            this.shooter.level().playSound(null, posX, posY, posZ, SoundEvent.createVariableRangeEvent(fireSound), SoundSource.HOSTILE, volume - 0.5F, pitch);
        }
    }

    private void consumeAmmo(ItemStack itemStack) {
        itemStack.getTag().putInt("AmmoCount", itemStack.getTag().getInt("AmmoCount") - 1);
    }

    private Vec3 findCoverLocation() {
        Vec3 targetPos = new Vec3(this.shooter.getTarget().getX(), this.shooter.getTarget().getY(), this.shooter.getTarget().getZ());
        Vec3 mobPos = this.shooter.position();
        return mobPos.add(mobPos.subtract(targetPos).normalize().scale(3));
    }
}