package top.ribs.scguns.entity.projectile;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

import javax.annotation.Nullable;
import java.util.*;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;


public class ShulkshotProjectileEntity extends ProjectileEntity {

    private LivingEntity target;
    private int homingDelay;
    private static final int DEFAULT_HOMING_DELAY = 15;
    private static final double MAX_TURN_RATE = 0.25;
    private static final double ACCELERATION = 0.15;
    private static final double MAX_HOMING_SPEED = 0.8;
    private static final double INITIAL_SPEED = 1.2;
    private static final double PREDICTION_FACTOR = 0.8;
    private static final double MIN_DISTANCE_FOR_SHARP_TURN = 10.0;
    private Vec3 currentDirection;
    private Vec3 lastTargetPos;
    private Vec3 targetVelocity = Vec3.ZERO;

    public ShulkshotProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.homingDelay = DEFAULT_HOMING_DELAY;
    }

    public ShulkshotProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.homingDelay = DEFAULT_HOMING_DELAY;

        Vec3 shooterDirection = shooter.getLookAngle();
        this.currentDirection = shooterDirection.normalize();

        double offsetDistance = 1.5;
        this.setPos(
                shooter.getX() + shooterDirection.x * offsetDistance,
                shooter.getEyeY() - 0.1 + shooterDirection.y * offsetDistance,
                shooter.getZ() + shooterDirection.z * offsetDistance
        );

        this.setDeltaMovement(shooterDirection.scale(INITIAL_SPEED));
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
        double angle = Math.acos(this.currentDirection.dot(toTarget));

        double distanceToTarget = this.position().distanceTo(predictedTargetPos);
        double adjustedTurnRate = MAX_TURN_RATE;
        if (distanceToTarget < MIN_DISTANCE_FOR_SHARP_TURN) {
            adjustedTurnRate = MAX_TURN_RATE * (1.0 + (MIN_DISTANCE_FOR_SHARP_TURN - distanceToTarget) / MIN_DISTANCE_FOR_SHARP_TURN);

            if (angle > Math.PI / 2) {
                adjustedTurnRate *= 1.5;
            }
        }

        Vec3 newDirection;
        if (angle > adjustedTurnRate) {
            double turnAmount = adjustedTurnRate / angle;
            newDirection = this.currentDirection.scale(1 - turnAmount)
                    .add(toTarget.scale(turnAmount))
                    .normalize();
        } else {
            newDirection = toTarget;
        }
        this.currentDirection = newDirection;
        double targetSpeed = MAX_HOMING_SPEED;
        if (angle > Math.PI / 3) {
            targetSpeed *= 0.8;
        }
        double newSpeed = currentSpeed + (currentSpeed < targetSpeed ? ACCELERATION : -ACCELERATION);
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