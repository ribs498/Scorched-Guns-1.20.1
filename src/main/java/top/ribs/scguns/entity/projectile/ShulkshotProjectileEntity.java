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
    private static final int DEFAULT_HOMING_DELAY = 3;
    private static final int RETARGET_INTERVAL = 10;
    private static final double BASE_SPEED = 0.8;
    private static final double TURN_SPEED = 0.15;
    private static final double CLOSE_PROXIMITY_THRESHOLD = 3.0;

    private Mob target;
    private Vec3 targetDirection;
    private int ticksSinceLastSearch = 0;


    public ShulkshotProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ShulkshotProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);

        Vec3 shooterDirection = shooter.getLookAngle();

        if (modifiedGun.getProjectile().getProjectileAmount() > 1) {
            float spread = modifiedGun.getProjectile().getSpread();
            float yawSpread = (this.random.nextFloat() - 0.5F) * spread;
            float pitchSpread = (this.random.nextFloat() - 0.5F) * spread;
            shooterDirection = applySpread(shooterDirection, yawSpread, pitchSpread);
        }

        this.targetDirection = shooterDirection.normalize();

        double offsetDistance = 1.5;
        this.setPos(
                shooter.getX() + shooterDirection.x * offsetDistance,
                shooter.getEyeY() - 0.1 + shooterDirection.y * offsetDistance,
                shooter.getZ() + shooterDirection.z * offsetDistance
        );

        this.setDeltaMovement(this.targetDirection.scale(BASE_SPEED));
    }
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
            this.ticksSinceLastSearch++;

            // Initial target acquisition
            if (this.tickCount == DEFAULT_HOMING_DELAY && this.target == null) {
                this.findNewTarget();
                this.ticksSinceLastSearch = 0;
            }

            if (this.target == null && this.ticksSinceLastSearch >= RETARGET_INTERVAL) {
                this.findNewTarget();
                this.ticksSinceLastSearch = 0;
            }

            if (this.ticksSinceLastSearch >= RETARGET_INTERVAL / 2) {
                Mob nearbyMob = findNearbyMob();
                if (nearbyMob != null) {
                    this.target = nearbyMob;
                    this.ticksSinceLastSearch = 0;
                }
            }

            if (this.target != null && this.target.isAlive()) {
                this.updateHomingMovement();
            } else if (this.target != null && !this.target.isAlive()) {
                this.target = null;
            }
        }

        if (this.level().isClientSide) {
            addTrailingParticles();
        }

        this.updateHeading();
    }

    private void updateHomingMovement() {
        Vec3 targetPos = new Vec3(
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5,
                target.getZ()
        );

        Vec3 toTarget = targetPos.subtract(this.position()).normalize();

        this.targetDirection = this.targetDirection.lerp(toTarget, TURN_SPEED).normalize();

        this.setDeltaMovement(this.targetDirection.scale(BASE_SPEED));
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
        double searchRadius = 20.0;
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
        }
    }

    private Mob findNearbyMob() {
        List<Mob> nearbyMobs = this.level().getEntitiesOfClass(
                Mob.class,
                this.getBoundingBox().inflate(CLOSE_PROXIMITY_THRESHOLD),
                entity -> entity.isAlive() &&
                        !entity.isSpectator() &&
                        entity != this.getShooter() &&
                        entity != this.target &&
                        this.hasLineOfSight(entity)
        );

        if (!nearbyMobs.isEmpty()) {
            nearbyMobs.sort(Comparator.comparingDouble(entity ->
                    entity.distanceToSqr(this.getX(), this.getY(), this.getZ())
            ));
            return nearbyMobs.get(0);
        }

        return null;
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