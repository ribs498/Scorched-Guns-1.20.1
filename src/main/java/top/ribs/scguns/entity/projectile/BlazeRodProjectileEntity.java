package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.item.GunItem;

import java.util.List;

public class BlazeRodProjectileEntity extends ProjectileEntity {
    private static final float PIERCE_CHANCE = 0.75F;
    private static final float SHATTER_CHANCE = 0.25F;
    private static final float POWDER_DROP_ON_SHATTER = 0.60F;
    private static final float ROD_DROP_NO_PIERCE = 0.95F;
    private static final float ROD_DROP_ONE_PIERCE = 0.75F;
    private static final float ROD_DROP_TWO_PIERCE = 0.50F;

    private static final float EXPLOSION_RADIUS = 2.5f;
    private static final float EXPLOSION_DAMAGE_FALLOFF = 0.7f;
    private static final int FIRE_DURATION = 3;

    private int remainingPenetrations = 2;
    private int mobsPierced = 0;
    private boolean hasShattered = false;
    private boolean pierceDecisionMade = false;
    private boolean willPierce = false;

    public BlazeRodProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public BlazeRodProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    public void tick() {
        super.onProjectileTick();

        if (!this.level().isClientSide()) {
            Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            BlockHitResult blockResult = rayTraceBlocks(this.level(),
                    new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this),
                    IGNORE_LEAVES);

            boolean hitBlock = false;
            if (blockResult.getType() != HitResult.Type.MISS) {
                endVec = blockResult.getLocation();
                hitBlock = true;
            }

            List<EntityResult> hitEntities = this.findEntitiesOnPath(startVec, endVec);
            boolean hitSomething = false;

            while (remainingPenetrations > 0 && hitEntities != null && !hitEntities.isEmpty()) {
                EntityResult closestEntity = null;
                double closestEntityDist = Double.MAX_VALUE;
                for (EntityResult entity : hitEntities) {
                    double dist = startVec.distanceToSqr(entity.getHitPos());
                    if (dist < closestEntityDist) {
                        closestEntityDist = dist;
                        closestEntity = entity;
                    }
                }

                if (closestEntity != null) {
                    this.onHitEntity(closestEntity.getEntity(), closestEntity.getHitPos(),
                            startVec, endVec, closestEntity.isHeadshot());
                    hitEntities.remove(closestEntity);
                    hitSomething = true;

                    // If we shattered or ran out of penetrations, stop here
                    if (hasShattered || remainingPenetrations <= 0) {
                        this.remove(RemovalReason.KILLED);
                        return;
                    }
                } else {
                    break;
                }
            }

            if (hitBlock) {
                BlockState state = this.level().getBlockState(blockResult.getBlockPos());
                this.onHitBlock(state, blockResult.getBlockPos(), blockResult.getDirection(),
                        blockResult.getLocation().x, blockResult.getLocation().y, blockResult.getLocation().z);
                // ALWAYS remove after hitting a block
                this.remove(RemovalReason.KILLED);
                return;
            }

            if (hitSomething && remainingPenetrations <= 0) {
                this.remove(RemovalReason.KILLED);
                return;
            }
        }

        this.setPos(this.getX() + this.getDeltaMovement().x,
                this.getY() + this.getDeltaMovement().y,
                this.getZ() + this.getDeltaMovement().z);

        if (this.projectile.isGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, this.modifiedGravity, 0));
        }

        this.updateHeading();

        if (this.tickCount >= this.life) {
            this.onExpired();
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        super.onHitBlock(state, pos, face, x, y, z);

        if (!this.level().isClientSide) {
            float dropChance = getBlockDropChance();

            if (this.random.nextFloat() < dropChance) {
                dropBlazeRod(x, y, z);
            } else {
                spawnFlameParticles(x, y, z);
                playExtinguishSound(x, y, z);
            }
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        if (entity.getId() == this.shooterId) {
            return;
        }

        // Only make the pierce vs explode decision on the FIRST mob hit
        if (!pierceDecisionMade) {
            pierceDecisionMade = true;
            willPierce = this.random.nextFloat() < PIERCE_CHANCE;
        }

        float damage = this.getDamage();
        float newDamage = this.getCriticalDamage(this.getWeapon(), this.random, damage);
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
        entity.hurt(source, damage);

        if (entity instanceof LivingEntity livingEntity) {
            applyImpactEffect(livingEntity);

            if (this.random.nextFloat() < 0.75F) {
                entity.setSecondsOnFire(FIRE_DURATION);
            }
        }

        // Follow the decision made on first hit
        if (willPierce && remainingPenetrations > 0) {
            remainingPenetrations--;
            mobsPierced++;
            entity.invulnerableTime = 0;

            Vec3 motion = this.getDeltaMovement();
            this.setPos(
                    this.getX() + motion.x * 0.2,
                    this.getY() + motion.y * 0.2,
                    this.getZ() + motion.z * 0.2
            );
            this.setDeltaMovement(motion.multiply(0.85D, 0.85D, 0.85D));

            // Don't drop blaze rod here during pierce - only on final block hit
        } else {
            // Shatter path - trigger explosion
            hasShattered = true;

            if (!this.level().isClientSide) {
                triggerMiniExplosion(hitVec, damage * 0.5f);

                if (this.random.nextFloat() < POWDER_DROP_ON_SHATTER) {
                    dropBlazePowder(hitVec.x, hitVec.y, hitVec.z);
                }
            }

            remainingPenetrations = 0;
        }

        // Don't call super.onHitEntity - we've handled everything ourselves
    }

    private void triggerMiniExplosion(Vec3 center, float baseDamage) {
        if (this.level().isClientSide()) {
            return;
        }

        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center.x - EXPLOSION_RADIUS,
                        center.y - EXPLOSION_RADIUS,
                        center.z - EXPLOSION_RADIUS,
                        center.x + EXPLOSION_RADIUS,
                        center.y + EXPLOSION_RADIUS,
                        center.z + EXPLOSION_RADIUS)
        );

        DamageSource explosionSource = ModDamageTypes.Sources.projectile(
                this.level().registryAccess(), this, this.shooter);

        for (LivingEntity target : nearbyEntities) {
            double distance = target.position().distanceTo(center);
            if (distance > EXPLOSION_RADIUS) {
                continue;
            }

            float distanceRatio = (float) (distance / EXPLOSION_RADIUS);
            float damageMultiplier = 1.0f - (distanceRatio * (1.0f - EXPLOSION_DAMAGE_FALLOFF));
            float explosionDamage = baseDamage * damageMultiplier;

            if (target == this.getShooter()) {
                explosionDamage *= 0.3f;
            }

            explosionDamage = applyProjectileProtection(target, explosionDamage);

            if (explosionDamage > 0.5f) {
                target.hurt(explosionSource, explosionDamage);

                if (this.random.nextFloat() < (0.5f * damageMultiplier)) {
                    target.setSecondsOnFire(FIRE_DURATION);
                }
            }
        }

        spawnExplosionParticles(center);
    }

    private void spawnExplosionParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0) * 2 * Math.PI;
                double radius = 0.4 + this.random.nextDouble() * 0.6;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.4;

                double speedX = offsetX * 0.15;
                double speedY = (this.random.nextDouble() - 0.3) * 0.2;
                double speedZ = offsetZ * 0.15;

                serverLevel.sendParticles(ParticleTypes.FLAME,
                        position.x + offsetX, position.y + offsetY, position.z + offsetZ,
                        1, speedX, speedY, speedZ, 0.08);
            }

            serverLevel.sendParticles(ParticleTypes.LAVA,
                    position.x, position.y, position.z,
                    8, 0.3, 0.3, 0.3, 0.2);

            for (int i = 0; i < 10; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 1.2;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.4;
                double offsetZ = (this.random.nextDouble() - 0.5) * 1.2;

                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        position.x + offsetX, position.y + offsetY, position.z + offsetZ,
                        1, 0, 0.1, 0, 0.05);
            }
        }

        this.level().playSound(null, position.x, position.y, position.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL,
                0.8F, 1.2F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
    }

    private void applyImpactEffect(LivingEntity target) {
        if (this.getProjectile().getImpactEffect() != null) {
            float effectChance = this.getProjectile().getImpactEffectChance();
            if (this.random.nextFloat() < effectChance) {
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(this.getProjectile().getImpactEffect());
                if (effect != null) {
                    target.addEffect(new MobEffectInstance(
                            effect,
                            this.getProjectile().getImpactEffectDuration(),
                            this.getProjectile().getImpactEffectAmplifier()
                    ));
                }
            }
        }
    }

    private float getBlockDropChance() {
        if (mobsPierced == 0) {
            return ROD_DROP_NO_PIERCE;
        } else if (mobsPierced == 1) {
            return ROD_DROP_ONE_PIERCE;
        } else {
            return ROD_DROP_TWO_PIERCE;
        }
    }

    private void dropBlazeRod(double x, double y, double z) {
        ItemEntity itemEntity = new ItemEntity(
                this.level(),
                x, y, z,
                new ItemStack(Items.BLAZE_ROD)
        );

        double bounceStrength = 0.05;
        itemEntity.setDeltaMovement(
                (this.random.nextDouble() - 0.5) * bounceStrength,
                0.2,
                (this.random.nextDouble() - 0.5) * bounceStrength
        );

        this.level().addFreshEntity(itemEntity);
        this.level().playSound(null, x, y, z,
                SoundEvents.BLAZE_SHOOT,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
        );
    }

    private void dropBlazePowder(double x, double y, double z) {
        ItemEntity powderEntity = new ItemEntity(
                this.level(),
                x, y, z,
                new ItemStack(Items.BLAZE_POWDER)
        );

        double bounceStrength = 0.15;
        powderEntity.setDeltaMovement(
                (this.random.nextDouble() - 0.5) * bounceStrength,
                0.15,
                (this.random.nextDouble() - 0.5) * bounceStrength
        );

        this.level().addFreshEntity(powderEntity);
    }

    private void spawnFlameParticles(double x, double y, double z) {
        ((ServerLevel)this.level()).sendParticles(ParticleTypes.FLAME,
                x, y, z,
                8,
                0.2D, 0.2D, 0.2D,
                0.05D
        );
    }

    private void playExtinguishSound(double x, double y, double z) {
        this.level().playSound(null, x, y, z,
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
        );
    }

    @Override
    protected void onProjectileTick() {
        super.onProjectileTick();
        if (this.level().isClientSide) {
            this.level().addParticle(
                    ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(),
                    0, 0, 0
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("RemainingPenetrations", this.remainingPenetrations);
        compound.putInt("MobsPierced", this.mobsPierced);
        compound.putBoolean("HasShattered", this.hasShattered);
        compound.putBoolean("PierceDecisionMade", this.pierceDecisionMade);
        compound.putBoolean("WillPierce", this.willPierce);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.remainingPenetrations = compound.getInt("RemainingPenetrations");
        this.mobsPierced = compound.getInt("MobsPierced");
        this.hasShattered = compound.getBoolean("HasShattered");
        this.pierceDecisionMade = compound.getBoolean("PierceDecisionMade");
        this.willPierce = compound.getBoolean("WillPierce");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(this.remainingPenetrations);
        buffer.writeInt(this.mobsPierced);
        buffer.writeBoolean(this.hasShattered);
        buffer.writeBoolean(this.pierceDecisionMade);
        buffer.writeBoolean(this.willPierce);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        super.readSpawnData(buffer);
        this.remainingPenetrations = buffer.readInt();
        this.mobsPierced = buffer.readInt();
        this.hasShattered = buffer.readBoolean();
        this.pierceDecisionMade = buffer.readBoolean();
        this.willPierce = buffer.readBoolean();
    }
}