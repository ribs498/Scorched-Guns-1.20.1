package top.ribs.scguns.entity.projectile;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class TraumaHookEntity extends FishingHook {
    private boolean isRetracting = false;
    private static final double GRAVITY = 0.03;
    private static final double AIR_RESISTANCE = 0.98;
    private static final double GROUND_FRICTION = 0.8;
    private static final int MAX_LIFETIME = 200;
    private static final double RETRACT_SPEED_BASE = 0.4;

    public TraumaHookEntity(EntityType<? extends TraumaHookEntity> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
    }

    public TraumaHookEntity(EntityType<? extends TraumaHookEntity> entityType, LivingEntity owner, Level level) {
        super(entityType, level);
        this.setOwner(owner);
        this.noCulling = true;
        this.moveTo(owner.getX(), owner.getEyeY() - 0.1, owner.getZ(), owner.getYRot(), owner.getXRot());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity && entity != this.getOwner() && !isRetracting) {
            super.onHitEntity(result);
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !isRetracting && entity instanceof LivingEntity && entity != this.getOwner();
    }

    @Override
    public void tick() {
        this.baseTick();

        if (this.level().isClientSide()) {
            if (!isRetracting && !this.onGround()) {
                Vec3 motion = this.getDeltaMovement();
                motion = motion.add(0.0, -GRAVITY, 0.0);
                motion = motion.scale(AIR_RESISTANCE);
                this.setDeltaMovement(motion);
                this.move(MoverType.SELF, motion);
            }
            return;
        }

        Entity owner = this.getOwner();
        if (owner == null || owner.isRemoved()) {
            this.discard();
            return;
        }

        if (!isRetracting) {
            boolean shouldRetract = false;

            if (this.tickCount > MAX_LIFETIME) {
                shouldRetract = true;
            }
            else if (this.onGround() && this.getHookedIn() == null && this.tickCount > 20) {
                shouldRetract = true;
            }
            else if (this.distanceTo(owner) > 32.0) {
                shouldRetract = true;
            }
            else if (!owner.isAlive()) {
                shouldRetract = true;
            }

            if (shouldRetract) {
                startRetraction();
            }
        }
        if (isRetracting) {
            handleRetraction();
        } else {
            handleNormalFlight();
        }
        updateRotation();
    }

    private void handleNormalFlight() {
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
            return;
        }
        Vec3 motion = this.getDeltaMovement();

        if (!this.onGround()) {
            motion = motion.add(0.0, -GRAVITY, 0.0);
            motion = motion.scale(AIR_RESISTANCE);

            this.setDeltaMovement(motion);
            this.move(MoverType.SELF, motion);
        } else {
            motion = motion.multiply(GROUND_FRICTION, 0.0, GROUND_FRICTION);
            this.setDeltaMovement(motion);
        }
    }

    private void startRetraction() {
        isRetracting = true;
        if (this.getHookedIn() != null) {
        }
    }

    private void handleRetraction() {
        Entity owner = this.getOwner();
        if (owner == null) {
            this.discard();
            return;
        }

        Vec3 ownerPos = new Vec3(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        Vec3 hookPos = this.position();
        double distanceToOwner = hookPos.distanceTo(ownerPos);

        if (distanceToOwner < 1.0) {
            this.discard();
            return;
        }
        Vec3 direction = ownerPos.subtract(hookPos).normalize();
        double retractSpeed = RETRACT_SPEED_BASE + (distanceToOwner * 0.05);
        retractSpeed = Math.min(retractSpeed, 1.5); // Cap the speed

        Vec3 retractVelocity = direction.scale(retractSpeed);
        this.setDeltaMovement(retractVelocity);
        this.move(MoverType.SELF, retractVelocity);
    }

    public void updateRotation() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.horizontalDistanceSqr() > 1.0E-7) {
            this.setYRot((float)(Math.atan2(motion.x, motion.z) * 180.0 / Math.PI));
            this.setXRot((float)(Math.atan2(motion.y, motion.horizontalDistance()) * 180.0 / Math.PI));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
    }

    public boolean isRetracting() {
        return isRetracting;
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 direction = new Vec3(x, y, z);
        direction = direction.normalize();

        // Add some randomness for inaccuracy
        direction = direction.add(
                this.random.triangle(0.0, 0.0172275 * inaccuracy),
                this.random.triangle(0.0, 0.0172275 * inaccuracy),
                this.random.triangle(0.0, 0.0172275 * inaccuracy)
        );

        direction = direction.normalize().scale(velocity);
        this.setDeltaMovement(direction);

        this.setYRot((float)(Math.atan2(direction.x, direction.z) * 180.0 / Math.PI));
        this.setXRot((float)(Math.atan2(direction.y, direction.horizontalDistance()) * 180.0 / Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    @Nullable
    public Player getPlayerOwner() {
        return null;
    }
}