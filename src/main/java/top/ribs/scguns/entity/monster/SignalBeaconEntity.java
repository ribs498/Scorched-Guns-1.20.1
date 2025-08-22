package top.ribs.scguns.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.init.ModEntities;

import java.util.*;

public class SignalBeaconEntity extends Mob {

    public SignalBeaconEntity(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.lifespan = LIFESPAN_TICKS;
    }

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private static final int LIFESPAN_TICKS = 100;
    private int lifespan;

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 12D)
                .add(Attributes.FOLLOW_RANGE, 0D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.1f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0f)
                .add(Attributes.ATTACK_DAMAGE, 0f);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (--this.lifespan <= 0) {
                spawnSkyCarriers();
                this.discard();
            }
        } else {
            setupAnimationStates();
        }

        if (this.tickCount % 20 == 0) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.NOTE_BLOCK_PLING.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
        }
    }


    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    private void spawnSkyCarriers() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        int count = 1 + this.random.nextInt(4);
        int successfulSpawns = 0;
        int maxAttempts = count * 5;

        Vec3 beaconPosition = this.position();

        for (int attempt = 0; attempt < maxAttempts && successfulSpawns < count; attempt++) {
            Vec3 spawnPos = findValidSpawnPosition(serverLevel);
            if (spawnPos == null) continue;

            SkyCarrierEntity skyCarrier = ModEntities.SKY_CARRIER.get().create(serverLevel);
            if (skyCarrier != null) {
                skyCarrier.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, this.random.nextFloat() * 360F, 0.0F);

                skyCarrier.setInitialTarget(beaconPosition);
                skyCarrier.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 1.0);

                serverLevel.addFreshEntity(skyCarrier);
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        skyCarrier.getX(), skyCarrier.getY(), skyCarrier.getZ(),
                        10, 0.5, 0.2, 0.2, 0.1);
                serverLevel.playSound(null, skyCarrier.getX(), skyCarrier.getY(), skyCarrier.getZ(),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);

                successfulSpawns++;
            }
        }
    }

    private Vec3 findValidSpawnPosition(ServerLevel serverLevel) {
        for (int radiusAttempt = 0; radiusAttempt < 3; radiusAttempt++) {
            double baseDistance = 20.0 + (radiusAttempt * 10.0);

            for (int positionAttempt = 0; positionAttempt < 8; positionAttempt++) {
                double distance = baseDistance + this.random.nextDouble() * 5.0;
                double angle = this.random.nextDouble() * 2.0 * Math.PI;
                double offsetX = Math.cos(angle) * distance;
                double offsetZ = Math.sin(angle) * distance;

                for (int heightOffset = 10; heightOffset >= -5; heightOffset -= 3) {
                    double spawnX = this.getX() + offsetX;
                    double spawnY = this.getY() + heightOffset;
                    double spawnZ = this.getZ() + offsetZ;

                    BlockPos spawnBlockPos = new BlockPos((int)spawnX, (int)spawnY, (int)spawnZ);

                    if (isValidSpawnPosition(serverLevel, spawnBlockPos, spawnX, spawnY, spawnZ)) {
                        return new Vec3(spawnX, spawnY, spawnZ);
                    }
                }
            }
        }
        return null;
    }

    private boolean isValidSpawnPosition(ServerLevel serverLevel, BlockPos blockPos, double exactX, double exactY, double exactZ) {
        AABB boundingBox = new AABB(exactX - 1.5, exactY - 1.0, exactZ - 1.5,
                exactX + 1.5, exactY + 2.0, exactZ + 1.5);
        for (BlockPos pos : BlockPos.betweenClosed(
                (int)(boundingBox.minX), (int)(boundingBox.minY), (int)(boundingBox.minZ),
                (int)(boundingBox.maxX), (int)(boundingBox.maxY), (int)(boundingBox.maxZ))) {

            if (!serverLevel.getBlockState(pos).isAir() &&
                    !serverLevel.getBlockState(pos).canBeReplaced() &&
                    serverLevel.getBlockState(pos).getBlock().defaultBlockState().blocksMotion()) {
                return false;
            }
        }

        BlockPos groundCheck = new BlockPos((int)exactX, (int)exactY - 5, (int)exactZ);
        for (int i = 0; i < 5; i++) {
            if (!serverLevel.getBlockState(groundCheck.below(i)).isAir()) {
                break; // Found ground
            }
            if (i == 4) {
                return false;
            }
        }
        List<Entity> entitiesInArea = serverLevel.getEntitiesOfClass(Entity.class, boundingBox);
        if (!entitiesInArea.isEmpty()) {
            return false;
        }

        return serverLevel.getWorldBorder().isWithinBounds(blockPos);
    }
}

