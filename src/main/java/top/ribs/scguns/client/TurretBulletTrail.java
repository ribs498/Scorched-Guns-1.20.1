package top.ribs.scguns.client;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class TurretBulletTrail {
    private final int entityId;
    private Vec3 position;
    private Vec3 motion;
    private float yaw;
    private float pitch;
    private boolean dead;
    private final int trailColor;
    private final double trailLengthMultiplier;
    private int age;
    private final int maxAge;
    private final double trailThickness;

    public TurretBulletTrail(int entityId, Vec3 position, Vec3 motion, int trailColor,
                             double trailMultiplier, int maxAge, double trailThickness) {
        this.entityId = entityId;
        this.position = position;
        this.motion = motion;
        this.trailColor = trailColor;
        this.trailLengthMultiplier = trailMultiplier;
        this.maxAge = maxAge;
        this.trailThickness = trailThickness;
        this.updateYawPitch();
    }

    private void updateYawPitch() {
        float horizontalLength = Mth.sqrt((float) (this.motion.x * this.motion.x + this.motion.z * this.motion.z));
        this.yaw = (float) Math.toDegrees(Mth.atan2(this.motion.x, this.motion.z));
        this.pitch = (float) Math.toDegrees(Mth.atan2(this.motion.y, horizontalLength));
    }

    public void tick() {
        this.age++;
        this.position = this.position.add(this.motion);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public Vec3 getMotion() {
        return this.motion;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public boolean isDead() {
        return this.dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public int getAge() {
        return this.age;
    }

    public int getTrailColor() {
        return this.trailColor;
    }

    public double getTrailLengthMultiplier() {
        return this.trailLengthMultiplier;
    }

    public double getTrailThickness() {
        return this.trailThickness;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    @Override
    public int hashCode() {
        return this.entityId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TurretBulletTrail) {
            return ((TurretBulletTrail) obj).entityId == this.entityId;
        }
        return false;
    }
}