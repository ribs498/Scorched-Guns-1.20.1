package top.ribs.scguns.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

import java.util.Comparator;
import java.util.List;

public class ShulkshotProjectileEntity extends ProjectileEntity {
    private static final int DEFAULT_HOMING_DELAY = 10; // Ticks before homing starts
    private static final double INITIAL_SPEED = 1.5;
    private static final double MAX_SPEED = 2.5;
    private static final double ACCELERATION = 0.05;
    private static final double TURN_SPEED = 0.08;
    private static final double PREDICTION_FACTOR = 0.7;

    private Mob target;
    private Vec3 lastTargetPos;
    private Vec3 targetVelocity = Vec3.ZERO;
    private int homingDelay;
    private Vec3 currentDirection;

    public ShulkshotProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.homingDelay = DEFAULT_HOMING_DELAY;
    }

    public ShulkshotProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.homingDelay = DEFAULT_HOMING_DELAY;

        Vec3 shooterDirection = shooter.getLookAngle();

        if (modifiedGun.getGeneral().getProjectileAmount() > 1) {
            float spread = modifiedGun.getGeneral().getSpread();
            float yawSpread = (this.random.nextFloat() - 0.5F) * spread;
            float pitchSpread = (this.random.nextFloat() - 0.5F) * spread;

            shooterDirection = applySpread(shooterDirection, yawSpread, pitchSpread);
        }

        this.currentDirection = shooterDirection.normalize();

        double offsetDistance = 1.5;
        this.setPos(
                shooter.getX() + shooterDirection.x * offsetDistance,
                shooter.getEyeY() - 0.1 + shooterDirection.y * offsetDistance,
                shooter.getZ() + shooterDirection.z * offsetDistance
        );

        this.setDeltaMovement(shooterDirection.scale(INITIAL_SPEED));
    }

    /**
     * Apply spread to the direction vector for multiple pellet support
     */
    private Vec3 applySpread(Vec3 direction, float yawSpread, float pitchSpread) {
        double currentYaw = Math.atan2(-direction.x, direction.z);
        double currentPitch = Math.asin(-direction.y);

        double newYaw = currentYaw + Math.toRadians(yawSpread);
        double newPitch = currentPitch + Math.toRadians(pitchSpread);

        double cosYaw = Math.cos(newYaw);
        double sinYaw = Math.sin(newYaw);
        double cosPitch = Math.cos(newPitch);
        double sinPitch = Math.sin(newPitch);

        return new Vec3(
                -sinYaw * cosPitch,
                -sinPitch,
                cosYaw * cosPitch
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (this.homingDelay > 0) {
                this.homingDelay--;
            } else {
                if (this.target == null || !this.target.isAlive()) {
                    this.findNewTarget();
                }

                if (this.target != null) {
                    updateTargetVelocity();
                    this.updateHomingMovement();
                }
            }
        }
        if (this.level().isClientSide) {
            addTrailingParticles();
        }

        this.updateHeading();
    }

    private void updateTargetVelocity() {
        Vec3 currentTargetPos = new Vec3(
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5,
                target.getZ()
        );

        if (lastTargetPos != null) {
            targetVelocity = currentTargetPos.subtract(lastTargetPos);
        }
        lastTargetPos = currentTargetPos;
    }

    private Vec3 getPredictedTargetPosition() {
        if (target == null) return null;
        Vec3 projectilePos = this.position();
        Vec3 targetPos = new Vec3(
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5,
                target.getZ()
        );
        double distance = projectilePos.distanceTo(targetPos);
        double currentSpeed = this.getDeltaMovement().length();
        double timeToIntercept = distance / currentSpeed;
        Vec3 predictedPos = targetPos.add(
                targetVelocity.scale(timeToIntercept * PREDICTION_FACTOR)
        );
        double leadMultiplier = Math.min(distance / 10.0, 2.0);
        return predictedPos.add(targetVelocity.scale(leadMultiplier));
    }

    private void updateHomingMovement() {
        Vec3 currentVelocity = this.getDeltaMovement();
        double currentSpeed = currentVelocity.length();

        Vec3 predictedTargetPos = getPredictedTargetPosition();
        if (predictedTargetPos == null) return;

        Vec3 toTarget = predictedTargetPos.subtract(this.position()).normalize();
        Vec3 currentDirectionNorm = currentVelocity.normalize();

        this.currentDirection = currentDirectionNorm.add(toTarget.scale(TURN_SPEED)).normalize();

        double targetSpeed = Math.min(MAX_SPEED, currentSpeed + ACCELERATION);
        double newSpeed = currentSpeed + (targetSpeed > currentSpeed ? ACCELERATION : -ACCELERATION);
        newSpeed = Mth.clamp(newSpeed, 0, targetSpeed);
        this.setDeltaMovement(this.currentDirection.scale(newSpeed));
    }

    private void addTrailingParticles() {
        for (int i = 0; i < 2; i++) {
            double offset = 0.1;
            Vec3 particlePos = this.position().add(
                    this.random.nextGaussian() * offset,
                    this.random.nextGaussian() * offset,
                    this.random.nextGaussian() * offset
            );

            this.level().addParticle(
                    ParticleTypes.PORTAL,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0
            );
        }
        Vec3 currentVel = this.getDeltaMovement();
        if (target != null && currentVel.length() > 0.5) {
            Vec3 toTarget = target.position().subtract(this.position()).normalize();
            double turnAngle = Math.acos(currentVel.normalize().dot(toTarget));

            if (turnAngle > Math.PI / 4) {
                this.level().addParticle(
                        ParticleTypes.WITCH,
                        this.getX(), this.getY(), this.getZ(),
                        0, 0, 0
                );
            }
        }
    }

    private void findNewTarget() {
        double searchRadius = 16.0;
        List<Mob> potentialTargets = this.level().getEntitiesOfClass(
                Mob.class,
                this.getBoundingBox().inflate(searchRadius),
                entity -> entity.isAlive() &&
                        !entity.isSpectator() &&
                        entity != this.getShooter() &&
                        this.hasLineOfSight(entity)
        );

        if (!potentialTargets.isEmpty()) {
            potentialTargets.sort(Comparator.comparingDouble(entity ->
                    entity.distanceToSqr(this.getX(), this.getY(), this.getZ())
            ));
            this.target = potentialTargets.get(0);
            this.lastTargetPos = target.position();
        }
    }

    private boolean hasLineOfSight(Entity target) {
        Vec3 vec3 = new Vec3(this.getX(), this.getY() + this.getEyeHeight(), this.getZ());
        Vec3 vec31 = new Vec3(target.getX(), target.getY() + target.getEyeHeight(), target.getZ());
        return this.level().clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public void updateHeading() {
        Vec3 motion = this.getDeltaMovement();
        double horizontalDistance = motion.horizontalDistance();
        this.setYRot((float)(Mth.atan2(motion.x, motion.z) * (180F / Math.PI)));
        this.setXRot((float)(Mth.atan2(motion.y, horizontalDistance) * (180F / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }
}