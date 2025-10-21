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
import net.minecraft.tags.BlockTags;
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
import org.jetbrains.annotations.NotNull;
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
    private static final int RIDER_IMMUNITY_TICKS = 3;
    private int immunityTicks;
    private static final float BASE_KNOCKBACK = 3.0F;
    private static final float KNOCKBACK_MULTIPLIER_PER_BOUNCE = 0.9F;
    private static final float VERTICAL_KNOCKBACK_BOOST = 0.4F;
    private static final float SPLASH_KNOCKBACK_RADIUS = 5.0F;
    private static final float SPLASH_KNOCKBACK_FALLOFF = 0.5F;
    private static final float WOOD_BREAK_CHANCE_BASE = 0.95F;
    private static final float WOOD_BREAK_CHANCE_PER_BOUNCE = 0.25F;

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

    private float calculateWoodBreakChance() {
        int bouncesUsed = MAX_BOUNCES - bouncesLeft;
        return WOOD_BREAK_CHANCE_BASE - (bouncesUsed * WOOD_BREAK_CHANCE_PER_BOUNCE);
    }

    private void tryBreakWoodenBlock(BlockState state, BlockPos pos) {
        if (!Config.COMMON.gameplay.griefing.enableGlassBreaking.get()) {
            return;
        }

        if (!state.is(BlockTags.PLANKS) && !state.is(BlockTags.WOODEN_DOORS) &&
                !state.is(BlockTags.WOODEN_TRAPDOORS) && !state.is(BlockTags.WOODEN_FENCES)) {
            return;
        }

        float breakChance = calculateWoodBreakChance();
        if (this.random.nextFloat() < breakChance) {
            this.level().destroyBlock(pos, true);
            this.level().playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F, 0.8F);
        }
    }

    private void applyKnockback(Entity target, Vec3 hitPos) {
        if (!(target instanceof LivingEntity)) {
            return;
        }

        Vec3 knockbackDirection = target.position().subtract(this.position()).normalize();
        Vec3 knockbackVec = getVec3(knockbackDirection);

        target.push(knockbackVec.x, knockbackVec.y, knockbackVec.z);
        target.hurtMarked = true;
    }

    private @NotNull Vec3 getVec3(Vec3 knockbackDirection) {
        float knockbackStrength = BASE_KNOCKBACK * currentDamageMultiplier;

        int bouncesUsed = MAX_BOUNCES - bouncesLeft;
        knockbackStrength *= (float) Math.pow(KNOCKBACK_MULTIPLIER_PER_BOUNCE, bouncesUsed);

        return new Vec3(
                knockbackDirection.x * knockbackStrength,
                Math.max(knockbackDirection.y * knockbackStrength, 0) + VERTICAL_KNOCKBACK_BOOST,
                knockbackDirection.z * knockbackStrength
        );
    }

    private void applySplashKnockback(Entity primaryTarget, Vec3 hitPos, float primaryKnockbackStrength) {
        AABB searchBox = new AABB(hitPos.x - SPLASH_KNOCKBACK_RADIUS,
                hitPos.y - SPLASH_KNOCKBACK_RADIUS,
                hitPos.z - SPLASH_KNOCKBACK_RADIUS,
                hitPos.x + SPLASH_KNOCKBACK_RADIUS,
                hitPos.y + SPLASH_KNOCKBACK_RADIUS,
                hitPos.z + SPLASH_KNOCKBACK_RADIUS);

        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != primaryTarget && entity != this.shooter && entity.isAlive());

        for (LivingEntity nearbyEntity : nearbyEntities) {
            double distance = nearbyEntity.position().distanceTo(hitPos);

            if (distance <= SPLASH_KNOCKBACK_RADIUS) {
                float distanceFalloff = (float) (1.0 - (distance / SPLASH_KNOCKBACK_RADIUS));
                Vec3 splashDirection = nearbyEntity.position().subtract(hitPos).normalize();
                float splashStrength = primaryKnockbackStrength * SPLASH_KNOCKBACK_FALLOFF * distanceFalloff;

                Vec3 splashKnockback = new Vec3(
                        splashDirection.x * splashStrength,
                        Math.max(splashDirection.y * splashStrength, 0) + (VERTICAL_KNOCKBACK_BOOST * 0.7F),
                        splashDirection.z * splashStrength
                );

                nearbyEntity.push(splashKnockback.x, splashKnockback.y, splashKnockback.z);
                nearbyEntity.hurtMarked = true;

                if (distance <= SPLASH_KNOCKBACK_RADIUS * 0.6F) {
                    float splashDamage = this.getDamage() * 0.15F * distanceFalloff;
                    DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, this.shooter);
                    nearbyEntity.hurt(source, splashDamage);
                }
            }
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

        tryBreakWoodenBlock(state, pos);

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

                    float knockbackStrength = BASE_KNOCKBACK * currentDamageMultiplier;
                    int bouncesUsed = MAX_BOUNCES - bouncesLeft;
                    knockbackStrength *= (float) Math.pow(KNOCKBACK_MULTIPLIER_PER_BOUNCE, bouncesUsed);

                    applyKnockback(entity, hitVec);
                    applySplashKnockback(entity, hitVec, knockbackStrength);
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
                    data, true);

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