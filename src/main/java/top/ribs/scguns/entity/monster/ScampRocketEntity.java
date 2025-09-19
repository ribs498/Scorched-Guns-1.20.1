package top.ribs.scguns.entity.monster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class ScampRocketEntity extends Projectile {
    private static final EntityDataAccessor<Boolean> HAS_EXPLODED = SynchedEntityData.defineId(ScampRocketEntity.class, EntityDataSerializers.BOOLEAN);

    private int life = 0;
    private double damage = 8.0D;
    private float explosionRadius = 3.0F;

    public ScampRocketEntity(EntityType<? extends ScampRocketEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ScampRocketEntity(EntityType<? extends ScampRocketEntity> pEntityType, Level pLevel, LivingEntity pShooter) {
        this(pEntityType, pLevel);
        this.setOwner(pShooter);
        this.setPos(pShooter.getX(), pShooter.getEyeY() - 0.1, pShooter.getZ());
    }

    public void setDamage(double pDamage) {
        this.damage = pDamage;
    }

    public double getDamage() {
        return this.damage;
    }

    public void setExplosionRadius(float radius) {
        this.explosionRadius = radius;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(HAS_EXPLODED, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.entityData.get(HAS_EXPLODED)) {
            return;
        }

        Vec3 vec3 = this.getDeltaMovement();
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

        if (hitresult.getType() != HitResult.Type.MISS) {
            this.onHit(hitresult);
        }

        double d0 = this.getX() + vec3.x;
        double d1 = this.getY() + vec3.y;
        double d2 = this.getZ() + vec3.z;
        this.setPos(d0, d1, d2);
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
        if (++this.life >= 200) {
            this.explode();
        }
    }

    @Override
    protected void onHit(@NotNull HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide && !this.entityData.get(HAS_EXPLODED)) {
            this.explode();
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        super.onHitEntity(pResult);
        Entity entity = pResult.getEntity();

        if (entity.equals(this.getOwner()) && this.life < 5) {
            return;
        }
        if (entity instanceof LivingEntity) {
            entity.hurt(this.damageSources().explosion(this, this.getOwner()), (float) this.damage);
        }
    }

    private void explode() {
        if (this.entityData.get(HAS_EXPLODED)) {
            return;
        }

        this.entityData.set(HAS_EXPLODED, true);

        if (!this.level().isClientSide) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), this.explosionRadius, Level.ExplosionInteraction.NONE);

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 1.0F, 1.0F);

            this.discard();
        } else {
            for (int i = 0; i < 20; i++) {
                double offsetX = this.random.nextGaussian() * 0.3D;
                double offsetY = this.random.nextGaussian() * 0.3D;
                double offsetZ = this.random.nextGaussian() * 0.3D;
                this.level().addParticle(ParticleTypes.EXPLOSION, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public boolean isPickable() {
        return !this.entityData.get(HAS_EXPLODED);
    }

    @Override
    public float getPickRadius() {
        return 1.0F;
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity pTarget) {
        return super.canHitEntity(pTarget) && !pTarget.noPhysics;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return pDistance < 16384.0D;
    }


    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("life", this.life);
        pCompound.putDouble("damage", this.damage);
        pCompound.putFloat("explosionRadius", this.explosionRadius);
        pCompound.putBoolean("hasExploded", this.entityData.get(HAS_EXPLODED));
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.life = pCompound.getInt("life");
        this.damage = pCompound.getDouble("damage");
        this.explosionRadius = pCompound.getFloat("explosionRadius");
        this.entityData.set(HAS_EXPLODED, pCompound.getBoolean("hasExploded"));
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        Vec3 vec3 = (new Vec3(pX, pY, pZ)).normalize().add(
                this.random.triangle(0.0D, 0.0172275D * (double)pInaccuracy),
                this.random.triangle(0.0D, 0.0172275D * (double)pInaccuracy),
                this.random.triangle(0.0D, 0.0172275D * (double)pInaccuracy)
        ).scale(pVelocity);

        this.setDeltaMovement(vec3);
        double d0 = vec3.horizontalDistance();
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (180D / Math.PI)));
        this.setXRot((float)(Mth.atan2(vec3.y, d0) * (180D / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }
}