package top.ribs.scguns.entity.projectile;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.*;
import top.ribs.scguns.util.GunEnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

public class ShotballProjectileEntity extends ProjectileEntity {
    private static final int MAX_BOUNCES = 3;
    private static final float FIRST_BOUNCE_VELOCITY_BOOST = 1.15F;
    private static final float FIRST_BOUNCE_DAMAGE_BOOST = 1.1F;
    private static final float BOUNCE_VELOCITY_RETENTION = 0.7F;
    private static final float DAMAGE_REDUCTION_PER_BOUNCE = 0.85F;
    private static final float MIN_BOUNCE_VELOCITY = 0.01F;
    private static final int RIDER_IMMUNITY_TICKS = 3; // Adjust as needed
    private int immunityTicks;



    private int bouncesLeft;
    private float currentDamageMultiplier = 1.0F;

    public ShotballProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.bouncesLeft = MAX_BOUNCES;
    }

    public ShotballProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.bouncesLeft = MAX_BOUNCES;
        this.immunityTicks = RIDER_IMMUNITY_TICKS;
    }

    @Override
    public void tick() {
        if (this.immunityTicks > 0) {
            this.immunityTicks--;
        }
        this.updateHeading();
        this.onProjectileTick();

        if (!this.level().isClientSide()) {
            Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            this.handleCustomCollisions(startVec, endVec);
        }

        double nextPosX = this.getX() + this.getDeltaMovement().x();
        double nextPosY = this.getY() + this.getDeltaMovement().y();
        double nextPosZ = this.getZ() + this.getDeltaMovement().z();
        this.setPos(nextPosX, nextPosY, nextPosZ);

        if (this.projectile.isGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, this.modifiedGravity, 0));
        }

        if (this.tickCount >= this.life) {
            if (this.isAlive()) {
                this.onExpired();
            }
            this.remove(RemovalReason.KILLED);
        }
    }
    private boolean isShooterRelatedEntity(Entity entity) {
        if (this.shooter == null) {
            return false;
        }

        if (this.immunityTicks > 0) {
            if (this.shooter.isPassenger() && this.shooter.getVehicle() == entity) {
                return true;
            }

            if (entity.isPassenger() && entity.getVehicle() == this.shooter) {
                return true;
            }

            Entity shooterVehicle = this.shooter.getVehicle();
            if (shooterVehicle != null) {
                if (shooterVehicle.getVehicle() == entity || entity.getVehicle() == shooterVehicle) {
                    return true;
                }
            }
        }

        return false;
    }
    private void handleCustomCollisions(Vec3 startVec, Vec3 endVec) {
        BlockHitResult blockResult = this.level().clip(new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        List<EntityResult> entityResults = this.findShotballEntitiesOnPath(startVec, endVec);

        double blockDistance = Double.MAX_VALUE;
        double entityDistance = Double.MAX_VALUE;

        if (blockResult.getType() != HitResult.Type.MISS) {
            blockDistance = startVec.distanceToSqr(blockResult.getLocation());
        }

        EntityResult closestEntity = null;
        if (!entityResults.isEmpty()) {
            for (EntityResult entityResult : entityResults) {

                double dist = startVec.distanceToSqr(entityResult.getHitPos());
                if (dist < entityDistance) {
                    entityDistance = dist;
                    closestEntity = entityResult;
                }
            }
        }

        if (blockDistance < entityDistance && blockResult.getType() != HitResult.Type.MISS) {
            this.handleBlockCollision(blockResult);
        } else if (closestEntity != null) {
           this.handleEntityCollision(closestEntity);
        }
    }

    private List<EntityResult> findShotballEntitiesOnPath(Vec3 startVec, Vec3 endVec) {
        List<EntityResult> hitEntities = new ArrayList<>();
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);

        for (Entity entity : entities) {
            if (isShooterRelatedEntity(entity)) {
                continue;
            }

            EntityResult result = this.getHitResult(entity, startVec, endVec);
            if (result == null) {
                continue;
            }
            hitEntities.add(result);
        }
        return hitEntities;
    }

    private void handleBlockCollision(BlockHitResult result) {
        Vec3 hitVec = result.getLocation();
        BlockPos pos = result.getBlockPos();
        BlockState state = this.level().getBlockState(pos);
        Direction face = result.getDirection();

        if (state.getBlock() instanceof top.ribs.scguns.interfaces.IDamageable damageable) {
            damageable.onBlockDamaged(this.level(), state, pos, this, this.getDamage() * currentDamageMultiplier,
                    (int) Math.ceil(this.getDamage() * currentDamageMultiplier / 2.0) + 1);
        }

        if (bouncesLeft > 0 && canBounce() && !state.canBeReplaced()) {
            bounce(face, hitVec);
            bouncesLeft--;

            if (bouncesLeft == MAX_BOUNCES - 2) {
                currentDamageMultiplier *= FIRST_BOUNCE_DAMAGE_BOOST;
            } else {
                currentDamageMultiplier *= DAMAGE_REDUCTION_PER_BOUNCE;
            }

            if (this.level() instanceof ServerLevel && this.projectile.isVisible()) {
                sendBounceTrailUpdate();
            }
            this.level().playSound(null, hitVec.x, hitVec.y, hitVec.z, SoundEvents.STONE_HIT, SoundSource.NEUTRAL,
                    0.8F, 1.2F + (this.random.nextFloat() - 0.5F) * 0.4F);

            spawnBounceParticles(hitVec);

        } else {
            PacketHandler.getPlayChannel().sendToTrackingChunk(
                    () -> this.level().getChunkAt(pos),
                    new S2CMessageProjectileHitBlock(hitVec.x, hitVec.y, hitVec.z, pos, face)
            );

            spawnDeathParticles(this.position());
            this.remove(RemovalReason.KILLED);
        }
    }

    private void handleEntityCollision(EntityResult entityResult) {
        Entity entity = entityResult.getEntity();
        Vec3 hitVec = entityResult.getHitPos();
        boolean headshot = entityResult.isHeadshot();
        float damage = this.getDamage() * currentDamageMultiplier;
        float newDamage = this.getCriticalDamage(this.getWeapon(), this.random, damage);
        boolean critical = damage != newDamage;
        damage = newDamage;
        damage *= advantageMultiplier(entity);

        if (headshot) {
            damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
        }

        if (entity instanceof LivingEntity livingTarget) {
            damage = applyProjectileProtection(livingTarget, damage);
            damage = calculateArmorBypassDamage(livingTarget, damage);
        }

        DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, this.shooter);
        boolean blocked = ProjectileHelper.handleShieldHit(entity, this, damage);

        if (!blocked) {
            if (!(entity.getType().is(ModTags.Entities.GHOST) &&
                    !this.getProjectile().getAdvantage().equals(ModTags.Entities.UNDEAD.location()))) {
                if (damage > 0) {
                    entity.hurt(source, damage);
                }

                if (entity instanceof LivingEntity livingEntity) {
                    ResourceLocation effectLocation = this.projectile.getImpactEffect();
                    if (effectLocation != null) {
                        float effectChance = this.projectile.getImpactEffectChance();
                        if (this.random.nextFloat() < effectChance) {
                            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                            if (effect != null) {
                                livingEntity.addEffect(new MobEffectInstance(
                                        effect,
                                        this.projectile.getImpactEffectDuration(),
                                        this.projectile.getImpactEffectAmplifier()
                                ));
                            }
                        }
                    }
                }
            }
        }

        if (entity instanceof LivingEntity livingEntity) {
            GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), livingEntity);
        }

        if (this.shooter instanceof Player) {
            int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL :
                    headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT :
                            S2CMessageProjectileHitEntity.HitType.NORMAL;
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter,
                    new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }

        PacketHandler.getPlayChannel().sendToTracking(() -> entity,
                new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z, entity.getType()));

        if (bouncesLeft > 0 && canBounce()) {
            bounceOffEntity(entity, hitVec);
            bouncesLeft--;

            if (bouncesLeft == MAX_BOUNCES - 2) {
                currentDamageMultiplier *= FIRST_BOUNCE_DAMAGE_BOOST;
            } else {
                currentDamageMultiplier *= DAMAGE_REDUCTION_PER_BOUNCE;
            }

            this.level().playSound(null, hitVec.x, hitVec.y, hitVec.z, SoundEvents.SLIME_BLOCK_HIT, SoundSource.NEUTRAL,
                    0.6F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.4F);

            spawnBounceParticles(hitVec);
        } else {
            spawnDeathParticles(this.position());
            this.remove(RemovalReason.KILLED);
        }

        entity.invulnerableTime = 0;
    }

    private void bounce(Direction face, Vec3 hitPos) {
        Vec3 velocity = this.getDeltaMovement();
        Vec3 newVelocity = switch (face.getAxis()) {
            case X -> new Vec3(-velocity.x, velocity.y, velocity.z);
            case Y -> new Vec3(velocity.x, -velocity.y, velocity.z);
            case Z -> new Vec3(velocity.x, velocity.y, -velocity.z);
        };

        newVelocity = newVelocity.scale(BOUNCE_VELOCITY_RETENTION);

        if (bouncesLeft == MAX_BOUNCES - 1) {
            newVelocity = newVelocity.scale(FIRST_BOUNCE_VELOCITY_BOOST);
        }

        newVelocity = newVelocity.add(
                (this.random.nextDouble() - 0.5) * 0.05,
                (this.random.nextDouble() - 0.5) * 0.05,
                (this.random.nextDouble() - 0.5) * 0.05
        );

        this.setDeltaMovement(newVelocity);
        Vec3 offset = Vec3.atLowerCornerOf(face.getNormal()).scale(0.2);
        this.setPos(hitPos.add(offset));
    }

    private void bounceOffEntity(Entity entity, Vec3 hitPos) {
        Vec3 velocity = this.getDeltaMovement();
        Vec3 entityCenter = entity.getBoundingBox().getCenter();
        Vec3 bounceDirection = this.position().subtract(entityCenter).normalize();

        Vec3 newVelocity = bounceDirection.scale(velocity.length() * BOUNCE_VELOCITY_RETENTION);

        if (bouncesLeft == MAX_BOUNCES - 1) {
            newVelocity = newVelocity.scale(FIRST_BOUNCE_VELOCITY_BOOST);
        }

        newVelocity = newVelocity.add(
                (this.random.nextDouble() - 0.5) * 0.1,
                (this.random.nextDouble() - 0.5) * 0.1,
                (this.random.nextDouble() - 0.5) * 0.1
        );

        this.setDeltaMovement(newVelocity);
        this.setPos(hitPos.add(bounceDirection.scale(0.3)));
    }

    private boolean canBounce() {
        double currentSpeed = this.getDeltaMovement().length();
        return currentSpeed > MIN_BOUNCE_VELOCITY;
    }

    private void spawnBounceParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            for (int i = 0; i < 8; i++) {
                double velocityX = (this.random.nextDouble() - 0.5) * 0.5;
                double velocityY = this.random.nextDouble() * 0.5;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.5;

                serverLevel.sendParticles(ParticleTypes.CRIT,
                        position.x, position.y, position.z,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.1
                );

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        position.x, position.y, position.z,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.05
                );
            }
        }
    }

    private void spawnDeathParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            for (int i = 0; i < 5; i++) {
                double velocityX = (this.random.nextDouble() - 0.5) * 0.3;
                double velocityY = this.random.nextDouble() * 0.4 + 0.1;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.3;

                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        position.x + (this.random.nextDouble() - 0.5) * 0.2,
                        position.y + (this.random.nextDouble() - 0.5) * 0.15,
                        position.z + (this.random.nextDouble() - 0.5) * 0.2,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.02
                );
            }

            for (int i = 0; i < 3; i++) {
                double velocityX = (this.random.nextDouble() - 0.5) * 0.2;
                double velocityY = this.random.nextDouble() * 0.3 + 0.05;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.2;

                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        position.x + (this.random.nextDouble() - 0.5) * 0.3,
                        position.y + (this.random.nextDouble() - 0.5) * 0.2,
                        position.z + (this.random.nextDouble() - 0.5) * 0.3,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.01
                );
            }

            this.level().playSound(null, position.x, position.y, position.z,
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL,
                    0.2F, 2.0F + (this.random.nextFloat() - 0.5F) * 0.3F);
        }
    }

    private void sendBounceTrailUpdate() {
        if (this.shooter instanceof ServerPlayer player) {
            Gun.Projectile projectileProps = this.getProjectile();
            ProjectileEntity[] bounceArray = {this};

            ParticleOptions data = GunEnchantmentHelper.getParticle(player.getMainHandItem());

            S2CMessageBulletTrail messageBulletTrail = new S2CMessageBulletTrail(
                    bounceArray,
                    projectileProps,
                    player.getId(),
                    data);

            double radius = Config.COMMON.network.projectileTrackingRange.get();
            PacketHandler.getPlayChannel().sendToNearbyPlayers(
                    () -> LevelLocation.create(this.level(), this.getX(), this.getY(), this.getZ(), radius),
                    messageBulletTrail);
        }
    }

    @Override
    protected void onExpired() {
        spawnDeathParticles(this.position());
    }

    @Override
    public float getDamage() {
        return super.getDamage() * currentDamageMultiplier;
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
    }

    @Override
    public void onHit(HitResult result, Vec3 startVec, Vec3 endVec) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("BouncesLeft", this.bouncesLeft);
        compound.putFloat("CurrentDamageMultiplier", this.currentDamageMultiplier);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.bouncesLeft = compound.getInt("BouncesLeft");
        this.currentDamageMultiplier = compound.getFloat("CurrentDamageMultiplier");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(this.bouncesLeft);
        buffer.writeFloat(this.currentDamageMultiplier);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        super.readSpawnData(buffer);
        this.bouncesLeft = buffer.readInt();
        this.currentDamageMultiplier = buffer.readFloat();
    }
}