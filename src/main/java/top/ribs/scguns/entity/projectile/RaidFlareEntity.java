package top.ribs.scguns.entity.projectile;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.config.RaidConfig;
import top.ribs.scguns.config.RaidFlareConfig;
import top.ribs.scguns.entity.raid.RaidManager;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageRaidFlareBurst;

import javax.annotation.Nullable;
import java.util.List;

public class RaidFlareEntity extends ThrowableProjectile {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EntityDataAccessor<String> RAID_ID =
            SynchedEntityData.defineId(RaidFlareEntity.class, EntityDataSerializers.STRING);

    private String raidId;
    private int ticksExisted = 0;
    private boolean hasBurst = false;

    public RaidFlareEntity(EntityType<? extends RaidFlareEntity> type, Level level) {
        super(type, level);
    }

    public RaidFlareEntity(Level level, LivingEntity shooter, String raidId) {
        super(ModEntities.RAID_FLARE.get(), shooter, level);
        this.raidId = raidId;
        this.entityData.set(RAID_ID, raidId);
    }

    public String getRaidId() {
        if (raidId == null || raidId.isEmpty()) {
            raidId = this.entityData.get(RAID_ID);
        }
        return raidId;
    }

    @Override
    protected void onHit(net.minecraft.world.phys.HitResult result) {
        if (!this.level().isClientSide && !hasBurst) {
            String currentRaidId = getRaidId();
            if (currentRaidId == null || currentRaidId.isEmpty()) return;

            RaidFlareConfig.FlareData flareData = RaidFlareConfig.getFlareData(currentRaidId);
            if (flareData == null) return;

            performBurst(flareData);
            hasBurst = true;
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);
        onHit(result);
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;

        String currentRaidId = getRaidId();
        if (currentRaidId == null || currentRaidId.isEmpty()) {
            if (!this.level().isClientSide) {
                this.discard();
            }
            return;
        }

        RaidFlareConfig.FlareData flareData = RaidFlareConfig.getFlareData(currentRaidId);
        if (flareData == null) {
            if (ticksExisted > 200 && !this.level().isClientSide) {
                this.discard();
            }
            return;
        }

        if (this.level().isClientSide && ticksExisted % 2 == 0) {
            spawnTrailParticles(flareData);
        }

        if (!this.level().isClientSide && ticksExisted >= flareData.burstDelay() && !hasBurst) {
            performBurst(flareData);
            hasBurst = true;
        }

        if (hasBurst && ticksExisted >= flareData.burstDelay() + 40 && !this.level().isClientSide) {
            this.discard();
        }

        Vec3 motion = this.getDeltaMovement();
        if (!this.onGround() && !hasBurst) {
            this.setDeltaMovement(motion.x * 0.99, motion.y - 0.04, motion.z * 0.99);
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    private void spawnTrailParticles(RaidFlareConfig.FlareData flareData) {
        for (RaidFlareConfig.ParticleEffect effect : flareData.trailParticles()) {
            ParticleOptions particle = getParticleType(effect.particleType());
            if (particle == null) continue;

            for (int i = 0; i < effect.count(); i++) {
                double offsetX = (random.nextDouble() - 0.5) * effect.spread();
                double offsetY = (random.nextDouble() - 0.5) * effect.spread();
                double offsetZ = (random.nextDouble() - 0.5) * effect.spread();

                level().addParticle(particle,
                        getX() + offsetX, getY() + offsetY, getZ() + offsetZ,
                        offsetX * effect.speed(), offsetY * effect.speed(), offsetZ * effect.speed());
            }
        }
    }

    private void performBurst(RaidFlareConfig.FlareData flareData) {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(flareData.burstSound()));
        if (sound != null) {
            level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.AMBIENT,
                    flareData.burstSoundVolume(), flareData.burstSoundPitch());
        }

        sendBurstParticlePacket(serverLevel, flareData);

        if (getOwner() instanceof net.minecraft.server.level.ServerPlayer player) {
            RaidConfig.RaidData config = RaidConfig.getRaidByRaidId(flareData.raidId());
            if (config != null) {
                RaidManager manager = RaidManager.get(serverLevel);
                manager.startRaidFromPlayer(config, serverLevel, player);
            }
        }
    }

    private void sendBurstParticlePacket(ServerLevel level, RaidFlareConfig.FlareData flareData) {
        java.util.List<S2CMessageRaidFlareBurst.ParticleData> particles = new java.util.ArrayList<>();
        for (RaidFlareConfig.ParticleEffect effect : flareData.burstParticles()) {
            particles.add(new S2CMessageRaidFlareBurst.ParticleData(
                    effect.particleType(),
                    effect.count(),
                    effect.spread(),
                    effect.speed(),
                    effect.color()
            ));
        }

        S2CMessageRaidFlareBurst message = createBurstMessage(flareData, particles);
        PacketHandler.getPlayChannel().sendToTrackingEntity(() -> this, message);
    }

    private @NotNull S2CMessageRaidFlareBurst createBurstMessage(RaidFlareConfig.FlareData flareData,
                                                                 List<S2CMessageRaidFlareBurst.ParticleData> particles) {
        String patternType = "default";
        double scale = 3.0;
        int repetitions = 1;

        if (flareData.pattern() != null) {
            patternType = flareData.pattern().patternType();
            scale = flareData.pattern().scale();
            repetitions = flareData.pattern().repetitions();
        }

        return new S2CMessageRaidFlareBurst(
                getX(), getY(), getZ(),
                patternType, scale, repetitions,
                particles
        );
    }

    @Nullable
    private ParticleOptions getParticleType(String particleId) {
        try {
            ResourceLocation location = new ResourceLocation(particleId);
            return (ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.get(location);
        } catch (Exception e) {
            return null;
        }
    }



    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("RaidId", getRaidId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("RaidId")) {
            this.raidId = tag.getString("RaidId");
            this.entityData.set(RAID_ID, this.raidId);
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(RAID_ID, "");
    }
}