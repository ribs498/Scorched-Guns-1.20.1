package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.interfaces.IDamageable;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBlood;
import top.ribs.scguns.network.message.S2CMessageProjectileHitBlock;
import top.ribs.scguns.network.message.S2CMessageProjectileHitEntity;
import top.ribs.scguns.particles.TrailData;
import top.ribs.scguns.util.GunEnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

public class ShatterRoundProjectileEntity extends ProjectileEntity {

    private static final int SHRAPNEL_COUNT = 20;
    private static final float SHRAPNEL_RANGE = 5.0f;
    private static final float SHRAPNEL_DAMAGE_MULTIPLIER = 0.3f;

    private boolean hasDetonated = false;

    public ShatterRoundProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ShatterRoundProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        if (hasDetonated) return;
        super.onHitEntity(entity, hitVec, startVec, endVec, headshot);
        this.explode(hitVec);
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        if (hasDetonated) return;

        PacketHandler.getPlayChannel().sendToTrackingChunk(() -> this.level().getChunkAt(pos),
                new S2CMessageProjectileHitBlock(x, y, z, pos, face));

        Block block = state.getBlock();
        primeTNT(state, pos);
        if (block instanceof DoorBlock) {
            boolean isOpen = state.getValue(DoorBlock.OPEN);
            if (!isOpen) {
                this.level().setBlock(pos, state.setValue(DoorBlock.OPEN, true), 10);
                this.level().playSound(null, pos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        if (block instanceof IDamageable) {
            ((IDamageable) block).onBlockDamaged(this.level(), state, pos, this, this.getDamage(), (int) Math.ceil(this.getDamage() / 2.0) + 1);
        }
        this.explode(new Vec3(x, y, z));
    }

    @Override
    protected void onExpired() {
        if (!hasDetonated) {
            this.explode(this.position());
        }
    }

    private void explode(Vec3 explosionPos) {
        if (hasDetonated || this.level().isClientSide()) {
            return;
        }
        hasDetonated = true;
        this.createCentralExplosion(explosionPos);
        this.createExplosionEffects(explosionPos);
        this.fireShrapnel(explosionPos);
        this.remove(RemovalReason.KILLED);
    }
    private void createCentralExplosion(Vec3 pos) {
        if (this.level().isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) this.level();
        this.level().playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL,
                2.0f, 0.8f + this.random.nextFloat() * 0.4f);
        this.level().playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.NEUTRAL,
                1.5f, 1.2f + this.random.nextFloat() * 0.3f);
        for (int i = 0; i < 40; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double pitch = (this.random.nextDouble() - 0.5) * Math.PI * 0.5;
            double speed = 0.3 + this.random.nextDouble() * 0.4;

            double offsetX = Math.cos(angle) * Math.cos(pitch) * speed;
            double offsetY = Math.sin(pitch) * speed;
            double offsetZ = Math.sin(angle) * Math.cos(pitch) * speed;
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    pos.x, pos.y, pos.z,
                    1, offsetX, offsetY, offsetZ, 0.02);

        }
        serverLevel.sendParticles(ParticleTypes.FLASH,
                pos.x, pos.y, pos.z,
                1, 0.1, 0.1, 0.1, 0.0);

    }

    private void createExplosionEffects(Vec3 pos) {
        this.level().playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.CALCITE_BREAK, SoundSource.NEUTRAL,
                1.0f, 1.0f + this.random.nextFloat() * 0.4f);

        ServerLevel serverLevel = (ServerLevel) this.level();
    }

    private void fireShrapnel(Vec3 origin) {
        float shrapnelDamage = this.getDamage() * SHRAPNEL_DAMAGE_MULTIPLIER;

        for (int i = 0; i < SHRAPNEL_COUNT; i++) {
            Vec3 direction = generateRandomDirection();
            Vec3 endPos = origin.add(direction.scale(SHRAPNEL_RANGE));
            this.traceShrapnelRay(origin, endPos, shrapnelDamage, i);
        }
    }

    private Vec3 generateRandomDirection() {
        float x, y, z;
        float lengthSquared;

        do {
            x = this.random.nextFloat() * 2.0f - 1.0f;
            y = this.random.nextFloat() * 2.0f - 1.0f;
            z = this.random.nextFloat() * 2.0f - 1.0f;
            lengthSquared = x * x + y * y + z * z;
        } while (lengthSquared > 1.0f || lengthSquared < 0.001f);

        float length = Mth.sqrt(lengthSquared);
        return new Vec3(x / length, y / length, z / length);
    }

    private void traceShrapnelRay(Vec3 start, Vec3 end, float damage, int rayIndex) {
        List<Entity> hitEntities = this.findEntitiesAlongRay(start, end);

        Vec3 traceEnd = end;
        boolean hitSomething = false;
        Entity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;
        Vec3 closestHitPos = null;

        for (Entity entity : hitEntities) {
            if (this.isValidShrapnelTarget(entity)) {
                Vec3 hitPos = this.getEntityHitPosition(entity, start, end);
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
            this.damageEntityWithShrapnel(closestEntity, closestHitPos, damage);
            traceEnd = closestHitPos;
            hitSomething = true;
        }

        if (!hitSomething) {
            BlockHitResult blockHit = this.level().clip(new ClipContext(
                    start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (blockHit.getType() != HitResult.Type.MISS) {
                traceEnd = blockHit.getLocation();
            }
        }
        this.createShrapnelTracer(start, traceEnd);
    }

    private List<Entity> findEntitiesAlongRay(Vec3 start, Vec3 end) {
        AABB searchBox = new AABB(start, end).inflate(1.0);
        return this.level().getEntities(this, searchBox, entity ->
                entity != null &&
                        entity.isPickable() &&
                        !entity.isSpectator() &&
                        entity != this.shooter &&
                        entity.getId() != this.shooterId
        );
    }

    private boolean isValidShrapnelTarget(Entity entity) {
        if (entity == this.shooter || entity.getId() == this.shooterId) {
            return false;
        }

        if (this.shooter instanceof Player && entity instanceof Player) {
            if (this.shooter.getUUID().equals(entity.getUUID())) {
                return false;
            }
        }

        if (this.shooter != null) {
            double distance = entity.position().distanceTo(this.shooter.position());
            return !(distance < 1.0);
        }

        return true;
    }

    private Vec3 getEntityHitPosition(Entity entity, Vec3 start, Vec3 end) {
        AABB boundingBox = entity.getBoundingBox();
        return boundingBox.clip(start, end).orElse(null);
    }

    private void damageEntityWithShrapnel(Entity entity, Vec3 hitPos, float damage) {
        ResourceLocation advantage = this.getProjectile().getAdvantage();
        damage *= advantageMultiplier(entity);

        if (entity instanceof LivingEntity livingTarget) {
            damage = applyProjectileProtection(livingTarget, damage);
            damage = calculateArmorBypassDamage(livingTarget, damage);
        }

        DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, this.shooter);

        if (!(entity.getType().is(ModTags.Entities.GHOST) &&
                !advantage.equals(ModTags.Entities.UNDEAD.location()))) {
            entity.hurt(source, damage);
        }

        PacketHandler.getPlayChannel().sendToTracking(() -> entity,
                new S2CMessageBlood(hitPos.x, hitPos.y, hitPos.z, entity.getType()));

        entity.invulnerableTime = Math.min(entity.invulnerableTime, 3);
    }

    private void createShrapnelTracer(Vec3 start, Vec3 end) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        Vec3 direction = end.subtract(start);
        double distance = direction.length();

        if (distance < 0.1) return;

        direction = direction.normalize();

        boolean isEnchanted = this.getWeapon() != null && this.getWeapon().isEnchanted();
        TrailData trailData = new TrailData(isEnchanted);

        int maxSegments = Math.min(12, (int) (distance * 1.5));

        for (int i = 1; i <= maxSegments; i++) {
            double progress = (double) i / maxSegments;
            Vec3 particlePos = start.add(direction.scale(distance * progress));

            double densityFactor = Math.max(0.2, 1.0 - (progress * 0.8));
            int particlesAtThisPoint = Math.max(1, (int) (4 * densityFactor));

            double spreadRadius = 0.02 + (progress * 0.1);

            for (int j = 0; j < particlesAtThisPoint; j++) {
                double offsetX = (this.random.nextDouble() - 0.5) * spreadRadius;
                double offsetY = (this.random.nextDouble() - 0.5) * spreadRadius;
                double offsetZ = (this.random.nextDouble() - 0.5) * spreadRadius;

                serverLevel.sendParticles(trailData,
                        particlePos.x + offsetX,
                        particlePos.y + offsetY,
                        particlePos.z + offsetZ,
                        1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide && !hasDetonated) {
            if (this.tickCount % 3 == 0) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.1;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.1;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.1;

                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0, 0, 0);
            }
        }
    }
}