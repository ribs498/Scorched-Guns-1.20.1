package top.ribs.scguns.entity.projectile;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.block.AutoTurretBlock;
import top.ribs.scguns.block.BasicTurretBlock;
import top.ribs.scguns.block.EnemyTurretBlock;
import top.ribs.scguns.block.ShotgunTurretBlock;
import top.ribs.scguns.blockentity.AutoTurretBlockEntity;
import top.ribs.scguns.blockentity.BasicTurretBlockEntity;
import top.ribs.scguns.blockentity.EnemyTurretBlockEntity;
import top.ribs.scguns.blockentity.ShotgunTurretBlockEntity;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBlood;
import top.ribs.scguns.network.message.S2CMessageProjectileHitEntity;
import top.ribs.scguns.util.GunEnchantmentHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class LightningProjectileEntity extends ProjectileEntity {
    private static final int MAX_BOUNCES = 3;
    private static final double BOUNCE_RANGE = 10.0;
    private static final float HEADSHOT_EFFECT_DURATION_MULTIPLIER = 1.5f;
    private static final float BOUNCE_EFFECT_REDUCTION = 0.55f;
    private int bouncesLeft;
    private float currentDamage;
    private final Set<Integer> hitEntities = new HashSet<>();

    public LightningProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.bouncesLeft = MAX_BOUNCES;
        this.currentDamage = this.getDamage();
    }

    public LightningProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.bouncesLeft = MAX_BOUNCES;
        this.currentDamage = this.getDamage();
        this.hitEntities.add(shooter.getId());
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        hitEntities.add(entity.getId());
        currentDamage = applyProjectileProtection(livingEntity, currentDamage);
        if (entity instanceof Creeper creeper) {
            if (this.random.nextFloat() < 0.15f) { // 15% chance
                try {
                    if (!creeper.isPowered()) {
                        net.minecraft.nbt.CompoundTag nbt = new net.minecraft.nbt.CompoundTag();
                        creeper.addAdditionalSaveData(nbt);
                        nbt.putBoolean("powered", true);
                        creeper.readAdditionalSaveData(nbt);
                        spawnLightningParticles(new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ()));
                    }
                } catch (Exception e) {
                    spawnLightningParticles(new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ()));
                }
            }
        }

        livingEntity.hurt(ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner()), currentDamage);
        Vec3 entityPosition = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight() * 0.5, entity.getZ());
        spawnLightningArc(this.position(), entityPosition);

        if (entity instanceof LivingEntity) {
            ResourceLocation effectLocation = this.getProjectile().getImpactEffect();
            if (effectLocation != null) {
                float effectChance = this.getProjectile().getImpactEffectChance();
                if (headshot) {
                    effectChance = Math.min(1.0f, effectChance * 1.25f);
                }

                float bounceChanceMultiplier = (float)Math.pow(BOUNCE_EFFECT_REDUCTION, MAX_BOUNCES - bouncesLeft);
                effectChance *= bounceChanceMultiplier;

                if (this.random.nextFloat() < effectChance) {
                    MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                    if (effect != null) {
                        int duration = this.getProjectile().getImpactEffectDuration();
                        if (headshot) {
                            duration = (int)(duration * HEADSHOT_EFFECT_DURATION_MULTIPLIER);
                        }
                        float bounceMultiplier = (float)Math.pow(BOUNCE_EFFECT_REDUCTION, MAX_BOUNCES - bouncesLeft);
                        duration = (int)(duration * bounceMultiplier);
                        int amplifier = Math.max(0, this.getProjectile().getImpactEffectAmplifier() - (MAX_BOUNCES - bouncesLeft));

                        livingEntity.addEffect(new MobEffectInstance(
                                effect,
                                duration,
                                amplifier
                        ));
                    }
                }
            }

            GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), livingEntity);
        }

        if (bouncesLeft > 0) {
            bouncesLeft--;
            currentDamage *= 0.75F;
            LivingEntity nextTarget = findNextTarget(entity);
            if (nextTarget != null) {
                scheduleBounce(nextTarget, entityPosition);
            } else {
                this.discard();
            }
        } else {
            this.discard();
        }

        PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z, entity.getType()));
    }

    private void scheduleBounce(LivingEntity nextTarget, Vec3 previousPosition) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> bounceToNextTarget(nextTarget, previousPosition));
        }
    }
    public float applyProjectileProtection(LivingEntity target, float damage) {
        int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROJECTILE_PROTECTION, target);

        if (protectionLevel > 0) {
            // Projectile Protection reduces projectile damage by 10% per level
            float reduction = protectionLevel * 0.10f;
            reduction = Math.min(reduction, 0.8f);
            damage *= (1.0f - reduction);
        }

        return damage;
    }
    private void bounceToNextTarget(LivingEntity nextTarget, Vec3 previousPosition) {
        if (nextTarget.getId() == this.getShooterId() ||
                nextTarget == this.getShooter() ||
                nextTarget == this.getOwner()) {
            this.discard();
            return;
        }

        Vec3 direction = nextTarget.position().subtract(this.position()).normalize();
        this.setDeltaMovement(direction.scale(1.5));
        this.setPos(nextTarget.getX(), nextTarget.getY() + nextTarget.getEyeHeight() * 0.5, nextTarget.getZ()); // Adjust to chest height
        Vec3 nextTargetPosition = new Vec3(nextTarget.getX(), nextTarget.getY() + nextTarget.getEyeHeight() * 0.5, nextTarget.getZ());
        spawnLightningArc(previousPosition, nextTargetPosition);
        this.onHitEntity(nextTarget, nextTargetPosition, this.position(), nextTargetPosition, false);
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        if (state.getBlock() instanceof AutoTurretBlock) {
            BlockEntity blockEntity = level().getBlockEntity(pos);
            if (blockEntity instanceof AutoTurretBlockEntity turret) {
                turret.onHitByLightningProjectile();
            }
        }
        if (state.getBlock() instanceof BasicTurretBlock) {
            BlockEntity blockEntity = level().getBlockEntity(pos);
            if (blockEntity instanceof BasicTurretBlockEntity turret) {
                turret.onHitByLightningProjectile();
            }
        }
        if (state.getBlock() instanceof ShotgunTurretBlock) {
            BlockEntity blockEntity = level().getBlockEntity(pos);
            if (blockEntity instanceof ShotgunTurretBlockEntity turret) {
                turret.onHitByLightningProjectile();
            }
        }
        if (state.getBlock() instanceof EnemyTurretBlock) {
            BlockEntity blockEntity = level().getBlockEntity(pos);
            if (blockEntity instanceof EnemyTurretBlockEntity turret) {
                turret.onHitByLightningProjectile();
            }
        }
        spawnLightningParticles(new Vec3(x, y + 0.1, z));
        this.discard();
    }



    @Override
    public void onExpired() {
        spawnLightningParticles(new Vec3(this.getX(), this.getY() + 0.1, this.getZ()));
    }

    private void spawnLightningArc(Vec3 start, Vec3 end) {
        if (!this.level().isClientSide) {
            if (this.getShooter() != null) {
                Vec3 shooterPos = new Vec3(this.getShooter().getX(), this.getShooter().getY() + this.getShooter().getEyeHeight() * 0.5, this.getShooter().getZ());
                double distanceToShooterStart = start.distanceTo(shooterPos);
                double distanceToShooterEnd = end.distanceTo(shooterPos);

                if (distanceToShooterStart < 1.0 || distanceToShooterEnd < 1.0) {
                    return;
                }
            }

            ServerLevel serverLevel = (ServerLevel) this.level();
            Vec3 direction = end.subtract(start);
            double distance = direction.length();
            direction = direction.normalize();
            double stepSize = 0.1;
            for (double d = 0; d < distance; d += stepSize) {
                Vec3 particlePos = start.add(direction.scale(d));
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
        }
    }

    private void spawnLightningParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            int particleCount = 20;
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, position.x, position.y, position.z, particleCount, 0, 0, 0, 0.1);
        }
    }

    @Override
    @javax.annotation.Nullable
    protected List<EntityResult> findEntitiesOnPath(Vec3 startVec, Vec3 endVec) {
        List<EntityResult> hitEntities = new ArrayList<>();
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);

        for (Entity entity : entities) {
            if (entity.getId() == this.getShooterId() ||
                    entity == this.getShooter() ||
                    entity == this.getOwner() ||
                    entity.equals(this.shooter)) {
                continue;
            }

            if (this.hitEntities.contains(entity.getId())) {
                continue;
            }

            EntityResult result = this.getHitResult(entity, startVec, endVec);
            if (result == null)
                continue;
            hitEntities.add(result);
        }
        return hitEntities;
    }

    @Override
    @javax.annotation.Nullable
    protected EntityResult findEntityOnPath(Vec3 startVec, Vec3 endVec) {
        Vec3 hitVec = null;
        Entity hitEntity = null;
        boolean headshot = false;
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            if (entity.getId() == this.getShooterId() ||
                    entity == this.getShooter() ||
                    entity == this.getOwner() ||
                    entity.equals(this.shooter)) {
                continue;
            }

            if (hitEntities.contains(entity.getId())) {
                continue;
            }

            EntityResult result = this.getHitResult(entity, startVec, endVec);
            if (result == null)
                continue;
            Vec3 hitPos = result.getHitPos();
            double distanceToHit = startVec.distanceTo(hitPos);
            if (distanceToHit < closestDistance) {
                hitVec = hitPos;
                hitEntity = entity;
                closestDistance = distanceToHit;
                headshot = result.isHeadshot();
            }
        }
        return hitEntity != null ? new EntityResult(hitEntity, hitVec, headshot) : null;
    }

    private LivingEntity findNextTarget(Entity currentTarget) {
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, currentTarget.getBoundingBox().inflate(BOUNCE_RANGE));

        nearbyEntities.removeIf(entity ->
                hitEntities.contains(entity.getId()) ||
                        entity == this.getOwner() ||
                        entity == currentTarget ||
                        entity == this.getShooter() ||
                        entity.getId() == this.getShooterId()
        );

        if (!nearbyEntities.isEmpty()) {
            return nearbyEntities.get(0);
        }
        return null;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide || this.isRemoved()) {
            return;
        }
        this.updateHeading();
        this.onProjectileTick();

        if (!this.level().isClientSide()) {
            Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());

            net.minecraft.world.level.ClipContext fluidContext = new net.minecraft.world.level.ClipContext(startVec, endVec, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.ANY, this);
            net.minecraft.world.phys.BlockHitResult fluidResult = this.level().clip(fluidContext);

            if (fluidResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                BlockPos blockPos = fluidResult.getBlockPos();
                net.minecraft.world.level.block.state.BlockState blockState = this.level().getBlockState(blockPos);
                net.minecraft.world.level.material.FluidState fluidState = blockState.getFluidState();

                if (fluidState.is(net.minecraft.tags.FluidTags.WATER)) {
                    if (top.ribs.scguns.Config.CLIENT.particle.enableWaterImpactParticles.get()) {
                        this.onWaterImpact(fluidResult.getLocation());
                    } else {
                        this.level().playSound(null, fluidResult.getLocation().x, fluidResult.getLocation().y, fluidResult.getLocation().z,
                                net.minecraft.sounds.SoundEvents.PLAYER_SPLASH, net.minecraft.sounds.SoundSource.NEUTRAL,
                                1.2F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                    }
                } else if (fluidState.is(net.minecraft.tags.FluidTags.LAVA)) {
                    this.onLavaImpact(fluidResult.getLocation());
                }
            }

            net.minecraft.world.phys.HitResult blockResult = rayTraceBlocks(this.level(), new net.minecraft.world.level.ClipContext(startVec, endVec, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, this), IGNORE_LEAVES);
            if (blockResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                endVec = blockResult.getLocation();
            }

            List<EntityResult> hitEntitiesList = findCustomEntitiesOnPath(startVec, endVec);

            if (!hitEntitiesList.isEmpty()) {
                for (EntityResult entityResult : hitEntitiesList) {
                    EntityHitResult result = new top.ribs.scguns.util.math.ExtendedEntityRayTraceResult(entityResult);
                    if (result.getEntity() instanceof Player player) {
                        if (this.shooter instanceof Player && !((Player) this.shooter).canHarmPlayer(player)) {
                            continue; // Skip this entity
                        }
                    }
                    this.onHit(result, startVec, endVec);
                }
            } else if (blockResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                this.onHit(blockResult, startVec, endVec);
            }
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

    private List<EntityResult> findCustomEntitiesOnPath(Vec3 startVec, Vec3 endVec) {
        List<EntityResult> hitEntitiesList = new java.util.ArrayList<>();
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);

        for (Entity entity : entities) {
            if (entity.getId() == this.getShooterId() ||
                    entity == this.getShooter() ||
                    entity == this.getOwner() ||
                    entity.equals(this.shooter)) {
                continue;
            }

            if (this.hitEntities.contains(entity.getId())) {
                continue;
            }

            EntityResult result = this.getHitResult(entity, startVec, endVec);
            if (result == null)
                continue;
            hitEntitiesList.add(result);
        }
        return hitEntitiesList;
    }
}