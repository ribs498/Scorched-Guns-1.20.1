package top.ribs.scguns.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import top.ribs.scguns.common.SulfurGasCloud;

public class SulfurGasCloudEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(SulfurGasCloudEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(SulfurGasCloudEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BASE_DURATION = SynchedEntityData.defineId(SulfurGasCloudEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BASE_AMPLIFIER = SynchedEntityData.defineId(SulfurGasCloudEntity.class, EntityDataSerializers.INT);

    private int ticksExisted = 0;

    private static final float DAMAGE_PHASE_RATIO = 0.85f;

    public SulfurGasCloudEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public SulfurGasCloudEntity(EntityType<?> entityType, Level level, Vec3 position, float radius, int duration, int baseDuration, int baseAmplifier) {
        this(entityType, level);
        this.setPos(position.x, position.y, position.z);
        this.entityData.set(RADIUS, radius);
        this.entityData.set(DURATION, duration);
        this.entityData.set(BASE_DURATION, baseDuration);
        this.entityData.set(BASE_AMPLIFIER, baseAmplifier);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(RADIUS, 6.0F);
        this.entityData.define(DURATION, 600);
        this.entityData.define(BASE_DURATION, 100);
        this.entityData.define(BASE_AMPLIFIER, 2);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            float radius = this.entityData.get(RADIUS);
            int duration = this.entityData.get(DURATION);
            int baseDuration = this.entityData.get(BASE_DURATION);
            int baseAmplifier = this.entityData.get(BASE_AMPLIFIER);

            Vec3 center = this.position();

            float lifecycleProgress = (float) ticksExisted / duration;

            float intensity = calculateIntensity(lifecycleProgress);

            SulfurGasCloud.spawnEnhancedGasCloud(this.level(), center, radius, intensity, this.random, ticksExisted);

            int damagePhaseTicks = (int)(duration * DAMAGE_PHASE_RATIO);
            if (ticksExisted < damagePhaseTicks) {
                float damageMultiplier = calculateDamageMultiplier(lifecycleProgress);

                int scaledDuration = (int)(baseDuration * damageMultiplier);
                SulfurGasCloud.applyGasEffects(this.level(), center, radius, scaledDuration, baseAmplifier);
            }

            SulfurGasCloud.checkAndHandleFireExplosion(this.level(), center, radius);

            ticksExisted++;
            if (ticksExisted >= duration) {
                this.discard();
            }
        }
    }

    private float calculateIntensity(float progress) {
        if (progress < 0.1f) {
            return Mth.clamp(progress / 0.1f, 0.0f, 1.0f);
        } else if (progress < 0.6f) {
            return 1.0f;
        } else {
            float fadeProgress = (progress - 0.6f) / 0.4f;
            return Mth.clamp(1.0f - fadeProgress, 0.0f, 1.0f);
        }
    }

    private float calculateDamageMultiplier(float progress) {
        if (progress < 0.15f) {
            return Mth.clamp(progress / 0.15f, 0.3f, 1.0f);
        } else if (progress < 0.5f) {
            return 1.0f;
        } else if (progress < DAMAGE_PHASE_RATIO) {
            float fadeProgress = (progress - 0.5f) / (DAMAGE_PHASE_RATIO - 0.5f);
            return Mth.clamp(1.0f - (fadeProgress * 0.7f), 0.3f, 1.0f);
        } else {
            return 0.0f;
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("Radius")) {
            this.entityData.set(RADIUS, compound.getFloat("Radius"));
        }
        if (compound.contains("Duration")) {
            this.entityData.set(DURATION, compound.getInt("Duration"));
        }
        if (compound.contains("BaseDuration")) {
            this.entityData.set(BASE_DURATION, compound.getInt("BaseDuration"));
        }
        if (compound.contains("BaseAmplifier")) {
            this.entityData.set(BASE_AMPLIFIER, compound.getInt("BaseAmplifier"));
        }
        if (compound.contains("TicksExisted")) {
            this.ticksExisted = compound.getInt("TicksExisted");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("Radius", this.entityData.get(RADIUS));
        compound.putInt("Duration", this.entityData.get(DURATION));
        compound.putInt("BaseDuration", this.entityData.get(BASE_DURATION));
        compound.putInt("BaseAmplifier", this.entityData.get(BASE_AMPLIFIER));
        compound.putInt("TicksExisted", this.ticksExisted);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}