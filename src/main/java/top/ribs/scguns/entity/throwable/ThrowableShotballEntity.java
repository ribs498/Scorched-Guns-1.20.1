package top.ribs.scguns.entity.throwable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;

public class ThrowableShotballEntity extends ThrowableItemEntity {
    private static final int MAX_BOUNCES = 3;
    private static final float BOUNCE_VELOCITY_RETENTION = 0.8F;
    private static final float MIN_BOUNCE_VELOCITY = 0.05F;
    private static final float BASE_DAMAGE = 9.0F;
    private static final float DAMAGE_REDUCTION_PER_BOUNCE = 0.85F;
    private int bouncesLeft;
    private float currentDamageMultiplier = 1.0F;

    public ThrowableShotballEntity(EntityType<? extends ThrowableItemEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.bouncesLeft = MAX_BOUNCES;
    }

    public ThrowableShotballEntity(Level world, LivingEntity entity) {
        super(ModEntities.THROWABLE_SHOTBALL.get(), world, entity);
        this.setShouldBounce(true);
        this.setGravityVelocity(0.04F);
        this.setItem(new ItemStack(ModItems.SHOTBALL.get()));
        this.setMaxLife(20 * 8);
        this.bouncesLeft = MAX_BOUNCES;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (this.bouncesLeft <= 0 && this.getDeltaMovement().length() < MIN_BOUNCE_VELOCITY) {
            this.spawnDeathParticles(this.position());
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        switch (result.getType()) {
            case BLOCK:
                BlockHitResult blockResult = (BlockHitResult) result;
                if (this.shouldBounce && this.bouncesLeft > 0) {
                    BlockState state = this.level().getBlockState(blockResult.getBlockPos());
                    double speed = this.getDeltaMovement().length();

                    if (speed > MIN_BOUNCE_VELOCITY) {
                        this.level().playSound(null, blockResult.getLocation().x, blockResult.getLocation().y, blockResult.getLocation().z,
                                SoundEvents.STONE_HIT, SoundSource.NEUTRAL,
                                0.8F, 1.2F + (this.random.nextFloat() - 0.5F) * 0.4F);

                        this.spawnBounceParticles(blockResult.getLocation());

                        this.bounce(blockResult.getDirection());
                        this.bouncesLeft--;
                        this.currentDamageMultiplier *= DAMAGE_REDUCTION_PER_BOUNCE;

                        this.setDeltaMovement(this.getDeltaMovement().add(
                                (this.random.nextDouble() - 0.5) * 0.1,
                                (this.random.nextDouble() - 0.5) * 0.05,
                                (this.random.nextDouble() - 0.5) * 0.1
                        ));
                    } else {
                        this.spawnDeathParticles(this.position());
                        this.remove(RemovalReason.KILLED);
                    }
                } else {
                    this.spawnDeathParticles(blockResult.getLocation());
                    this.remove(RemovalReason.KILLED);
                }
                break;
            case ENTITY:
                EntityHitResult entityResult = (EntityHitResult) result;
                Entity entity = entityResult.getEntity();

                if (this.shouldBounce && this.bouncesLeft > 0) {
                    double speed = this.getDeltaMovement().length();
                    if (speed > 0.1) {
                        float damage = BASE_DAMAGE * this.currentDamageMultiplier * Math.min(1.0F, (float)(speed / 1.5));
                        entity.hurt(entity.damageSources().thrown(this, this.getOwner()), damage);

                        this.level().playSound(null, entityResult.getLocation().x, entityResult.getLocation().y, entityResult.getLocation().z,
                                SoundEvents.SLIME_BLOCK_HIT, SoundSource.NEUTRAL,
                                0.6F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.4F);

                        this.spawnEntityHitParticles(entityResult.getLocation());
                    }

                    // Bounce off entity
                    this.bounce(Direction.getNearest(this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z()).getOpposite());
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 0.8, 0.6)); // Reduce momentum more on entity hit
                    this.bouncesLeft--;
                    this.currentDamageMultiplier *= DAMAGE_REDUCTION_PER_BOUNCE;
                } else {
                    if (this.getDeltaMovement().length() > 0.1) {
                        float damage = BASE_DAMAGE * this.currentDamageMultiplier;
                        entity.hurt(entity.damageSources().thrown(this, this.getOwner()), damage);
                        this.spawnEntityHitParticles(entityResult.getLocation());
                    }
                    this.remove(RemovalReason.KILLED);
                }
                break;
            default:
                break;
        }
    }

    @Override
    void bounce(Direction direction) {
        switch (direction.getAxis()) {
            case X:
                this.setDeltaMovement(this.getDeltaMovement().multiply(-BOUNCE_VELOCITY_RETENTION, 0.85, 0.85));
                break;
            case Y:
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.85, -BOUNCE_VELOCITY_RETENTION * 0.7, 0.85));
                if (this.getDeltaMovement().y() < this.getGravity() * 2) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0.1, 1));
                }
                break;
            case Z:
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.85, 0.85, -BOUNCE_VELOCITY_RETENTION));
                break;
        }
    }

    @Override
    public void onDeath() {
        // Spawn final particles when the shotball comes to rest
        spawnFinalParticles(this.position());
        // Spawn black dissipation particles when destroyed/expired
        spawnDeathParticles(this.position());
    }

    public int getBouncesLeft() {
        return this.bouncesLeft;
    }

    public float getCurrentDamage() {
        return BASE_DAMAGE * this.currentDamageMultiplier;
    }

    /**
     * Spawn bounce particles similar to the projectile version when bouncing off blocks
     */
    private void spawnBounceParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            // Just electric sparks for clean bounce effect
            for (int i = 0; i < 4; i++) {
                double velocityX = (this.random.nextDouble() - 0.5) * 0.4;
                double velocityY = this.random.nextDouble() * 0.4;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.4;

                // Electric spark particles for the "shotball" effect
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        position.x, position.y, position.z,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.04
                );
            }
        }
    }

    /**
     * Spawn particles when hitting an entity
     */
    private void spawnEntityHitParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            // Just electric sparks for clean entity hit effect
            for (int i = 0; i < 5; i++) {
                double velocityX = (this.random.nextDouble() - 0.5) * 0.3;
                double velocityY = (this.random.nextDouble() - 0.5) * 0.3;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.3;

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        position.x, position.y, position.z,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.06
                );
            }
        }
    }

    /**
     * Spawn particles when the shotball finally comes to rest or expires
     */
    private void spawnFinalParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            // Smaller final impact effect
            for (int i = 0; i < 4; i++) {
                double velocityX = (this.random.nextDouble() - 0.5) * 0.2;
                double velocityY = this.random.nextDouble() * 0.2;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.2;

                serverLevel.sendParticles(ParticleTypes.CRIT,
                        position.x, position.y, position.z,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.04
                );

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        position.x, position.y, position.z,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.02
                );
            }

            // Final dust cloud when coming to rest on ground
            if (this.getDeltaMovement().y() <= 0.01) {
                for (int i = 0; i < 3; i++) {
                    double dustX = position.x + (this.random.nextDouble() - 0.5) * 0.4;
                    double dustY = position.y;
                    double dustZ = position.z + (this.random.nextDouble() - 0.5) * 0.4;

                    serverLevel.sendParticles(ParticleTypes.POOF,
                            dustX, dustY, dustZ,
                            1,
                            0, 0.05, 0,
                            0.01
                    );
                }
            }
        }
    }

    /**
     * Spawn black dissipation particles when the shotball is destroyed or expires
     */
    private void spawnDeathParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            // Black smoke particles for dissipation effect
            for (int i = 0; i < 6; i++) {
                double velocityX = (this.random.nextDouble() - 0.5) * 0.3;
                double velocityY = this.random.nextDouble() * 0.4 + 0.1; // Upward bias
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.3;

                // Large smoke particles
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        position.x + (this.random.nextDouble() - 0.5) * 0.3,
                        position.y + (this.random.nextDouble() - 0.5) * 0.2,
                        position.z + (this.random.nextDouble() - 0.5) * 0.3,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.02
                );
            }

            // Regular smoke for density
            for (int i = 0; i < 4; i++) {
                double velocityX = (this.random.nextDouble() - 0.5) * 0.2;
                double velocityY = this.random.nextDouble() * 0.3 + 0.05;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.2;

                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        position.x + (this.random.nextDouble() - 0.5) * 0.4,
                        position.y + (this.random.nextDouble() - 0.5) * 0.3,
                        position.z + (this.random.nextDouble() - 0.5) * 0.4,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.01
                );
            }

            // Play a subtle dissipation sound
            this.level().playSound(null, position.x, position.y, position.z,
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL,
                    0.3F, 1.8F + (this.random.nextFloat() - 0.5F) * 0.4F);
        }
    }
}