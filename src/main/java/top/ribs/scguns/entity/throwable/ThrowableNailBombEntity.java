package top.ribs.scguns.entity.throwable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBlood;
import top.ribs.scguns.particles.TrailData;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ThrowableNailBombEntity extends ThrowableGrenadeEntity
{
    public float rotation;

    public ThrowableNailBombEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    public ThrowableNailBombEntity(Level world, LivingEntity entity, int timeLeft)
    {
        super(ModEntities.THROWABLE_NAIL_BOMB.get(), world, entity);
        this.setShouldBounce(false);
        this.setItem(new ItemStack(ModItems.NAIL_BOMB.get()));
        this.setMaxLife(20 * 3);
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    public void particleTick()
    {
        if (this.level().isClientSide)
        {
            this.level().addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY() + 0.25, this.getZ(), 0, 0, 0);
            this.level().addParticle(ParticleTypes.CRIT, true, this.getX(), this.getY() + 0.25, this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1);
        }
    }

    @Override
    public void onDeath()
    {
        double y = this.getY() + this.getType().getDimensions().height * 0.5;

        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1, 1);
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.5F, 0.8F);
        createShrapnelExplosion(this, 6.0F, false);
        this.spawnExplosionParticles(new Vec3(this.getX(), y, this.getZ()));
    }

    public static void createShrapnelExplosion(Entity entity, float radius, boolean damageBlocks) {
        if (entity.level().isClientSide()) {
            return;
        }

        Level world = entity.level();
        Vec3 origin = entity.position();
        world.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL,
                2.0f, 0.8f + world.random.nextFloat() * 0.4f);
        world.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.NEUTRAL,
                1.5f, 1.2f + world.random.nextFloat() * 0.3f);

        ServerLevel serverLevel = (ServerLevel) world;
        for (int i = 0; i < 40; i++) {
            double angle = world.random.nextDouble() * Math.PI * 2;
            double pitch = (world.random.nextDouble() - 0.5) * Math.PI * 0.5;
            double speed = 0.3 + world.random.nextDouble() * 0.4;

            double offsetX = Math.cos(angle) * Math.cos(pitch) * speed;
            double offsetY = Math.sin(pitch) * speed;
            double offsetZ = Math.sin(angle) * Math.cos(pitch) * speed;

            serverLevel.sendParticles(ParticleTypes.CRIT,
                    origin.x, origin.y, origin.z,
                    1, offsetX, offsetY, offsetZ, 0.02);
        }

        serverLevel.sendParticles(ParticleTypes.FLASH,
                origin.x, origin.y, origin.z,
                1, 0.1, 0.1, 0.1, 0.0);

        int shrapnelCount = 25;
        float baseDamage = 8.0f;

        for (int i = 0; i < shrapnelCount; i++) {
            Vec3 direction = generateRandomDirection(world.random);
            Vec3 endPos = origin.add(direction.scale(radius));
            traceShrapnelRay(world, entity, origin, endPos, baseDamage, i);
        }
    }

    private static Vec3 generateRandomDirection(net.minecraft.util.RandomSource random) {
        float x, y, z;
        float lengthSquared;

        do {
            x = random.nextFloat() * 2.0f - 1.0f;
            y = random.nextFloat() * 2.0f - 1.0f;
            z = random.nextFloat() * 2.0f - 1.0f;
            lengthSquared = x * x + y * y + z * z;
        } while (lengthSquared > 1.0f || lengthSquared < 0.001f);

        float length = net.minecraft.util.Mth.sqrt(lengthSquared);
        return new Vec3(x / length, y / length, z / length);
    }

    private static void traceShrapnelRay(Level world, Entity source, Vec3 start, Vec3 end, float damage, int rayIndex) {
        List<Entity> hitEntities = findEntitiesAlongRay(world, source, start, end);

        Vec3 traceEnd = end;
        boolean hitSomething = false;
        Entity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;
        Vec3 closestHitPos = null;

        for (Entity entity : hitEntities) {
            if (isValidShrapnelTarget(entity, source)) {
                Vec3 hitPos = getEntityHitPosition(entity, start, end);
                if (hitPos != null) {
                    double distance = start.distanceToSqr(hitPos);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestEntity = entity;
                        closestHitPos = hitPos;
                    }
                }
            }
        }

        if (closestEntity != null) {
            damageEntityWithShrapnel(world, closestEntity, closestHitPos, damage, source);
            traceEnd = closestHitPos;
            hitSomething = true;
        }

        if (!hitSomething) {
            BlockHitResult blockHit = world.clip(new ClipContext(
                    start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source));
            if (blockHit.getType() != HitResult.Type.MISS) {
                traceEnd = blockHit.getLocation();
            }
        }

        createShrapnelTracer(world, start, traceEnd);
    }

    private static List<Entity> findEntitiesAlongRay(Level world, Entity source, Vec3 start, Vec3 end) {
        AABB searchBox = new AABB(start, end).inflate(1.0);
        return world.getEntities(source, searchBox, entity ->
                entity != null &&
                        entity.isPickable() &&
                        !entity.isSpectator() &&
                        isValidShrapnelTarget(entity, source)
        );
    }

    private static boolean isValidShrapnelTarget(Entity entity, Entity source) {
        return entity != source;
    }

    private static Vec3 getEntityHitPosition(Entity entity, Vec3 start, Vec3 end) {
        AABB boundingBox = entity.getBoundingBox();
        return boundingBox.clip(start, end).orElse(null);
    }

    private static void damageEntityWithShrapnel(Level world, Entity entity, Vec3 hitPos, float damage, Entity source) {
        DamageSource damageSource;
        Entity thrower = null;
        if (source instanceof ThrowableItemEntity throwable) {
            thrower = throwable.getOwner();
        }

        if (thrower instanceof LivingEntity livingThrower) {
            damageSource = world.damageSources().explosion(source, livingThrower);
        } else {
            damageSource = world.damageSources().explosion(source, null);
        }

        entity.hurt(damageSource, damage);
        if (entity instanceof LivingEntity && !world.isClientSide()) {
            PacketHandler.getPlayChannel().sendToTracking(() -> entity,
                    new S2CMessageBlood(hitPos.x, hitPos.y, hitPos.z, entity.getType()));
        }

        entity.invulnerableTime = Math.min(entity.invulnerableTime, 3);
    }

    private static void createShrapnelTracer(Level world, Vec3 start, Vec3 end) {
        if (world.isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) world;
        Vec3 direction = end.subtract(start);
        double distance = direction.length();

        if (distance < 0.1) return;

        direction = direction.normalize();

        int maxSegments = Math.min(20, (int) (distance * 2.5));

        for (int i = 1; i <= maxSegments; i++) {
            double progress = (double) i / maxSegments;
            Vec3 particlePos = start.add(direction.scale(distance * progress));

            double densityFactor = Math.max(0.3, 1.0 - (progress * 0.6));
            int particlesAtThisPoint = Math.max(1, (int) (6 * densityFactor));
            double spreadRadius = 0.03 + (progress * 0.15);

            for (int j = 0; j < particlesAtThisPoint; j++) {
                double offsetX = (world.random.nextDouble() - 0.5) * spreadRadius;
                double offsetY = (world.random.nextDouble() - 0.5) * spreadRadius;
                double offsetZ = (world.random.nextDouble() - 0.5) * spreadRadius;

                TrailData trailData = new TrailData(false);
                serverLevel.sendParticles(trailData,
                        particlePos.x + offsetX,
                        particlePos.y + offsetY,
                        particlePos.z + offsetZ,
                        1, 0, 0, 0, 0);
            }
        }
    }

    private void spawnExplosionParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            for (int i = 0; i < 20; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                double speedX = (this.random.nextDouble() - 0.5) * 0.8;
                double speedY = (this.random.nextDouble() - 0.5) * 0.8;
                double speedZ = (this.random.nextDouble() - 0.5) * 0.8;
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        position.x + offsetX, position.y + offsetY, position.z + offsetZ,
                        1, speedX, speedY, speedZ, 0.1);

                serverLevel.sendParticles(ParticleTypes.CRIT,
                        position.x + offsetX, position.y + offsetY, position.z + offsetZ,
                        1, speedX * 1.5, speedY * 1.5, speedZ * 1.5, 0.2);
            }
            for (int i = 0; i < 10; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.8;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.6;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.8;
                double speedX = (this.random.nextDouble() - 0.5) * 0.3;
                double speedY = this.random.nextDouble() * 0.4;
                double speedZ = (this.random.nextDouble() - 0.5) * 0.3;

                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        position.x + offsetX, position.y + offsetY, position.z + offsetZ,
                        1, speedX, speedY, speedZ, 0.05);
            }
        }
    }
}