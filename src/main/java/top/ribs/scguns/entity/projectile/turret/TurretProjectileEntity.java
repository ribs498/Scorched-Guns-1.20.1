package top.ribs.scguns.entity.projectile.turret;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModSounds;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageTurretBulletTrail;

import java.util.List;

public class TurretProjectileEntity extends AbstractArrow {
    private static final float GIBBS_ROUND_XP_MULTIPLIER = 0.25f;
    private static final int GIBBS_ROUND_LOOTING_LEVEL = 4;
    private static boolean eventRegistered = false;

    private static final int SHRAPNEL_COUNT = 20;
    private static final float SHRAPNEL_RANGE = 5.0f;
    private static final float SHRAPNEL_DAMAGE_MULTIPLIER = 0.3f;

    private boolean trailSpawned = false;
    private float armorPenetration = 0.0F;
    private int mobPenetration = 0;
    private int entitiesHit = 0;
    private boolean isGibbsRound = false;
    private boolean isShatterRound = false;

    public TurretProjectileEntity(EntityType<? extends AbstractArrow> type, Level world) {
        super(type, world);
        this.setNoGravity(true);
        registerLootingEventHandler();
    }

    public TurretProjectileEntity(Level world) {
        super(ModEntities.TURRET_PROJECTILE.get(), world);
        this.setNoGravity(true);
        registerLootingEventHandler();
    }

    private static synchronized void registerLootingEventHandler() {
        if (!eventRegistered) {
            MinecraftForge.EVENT_BUS.register(TurretProjectileEntity.class);
            eventRegistered = true;
        }
    }

    @SubscribeEvent
    public static void onLootingLevel(LootingLevelEvent event) {
        if (event.getDamageSource() != null &&
                event.getDamageSource().getDirectEntity() instanceof TurretProjectileEntity projectile) {
            if (projectile.isGibbsRound) {
                event.setLootingLevel(event.getLootingLevel() + GIBBS_ROUND_LOOTING_LEVEL);
            }
        }
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        this.setDeltaMovement(this.getDeltaMovement().normalize().scale(velocity));
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity entity = pResult.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            float damageAmount = (float) this.getBaseDamage();

            if (this.armorPenetration > 0) {
                float originalArmor = livingEntity.getArmorValue();
                float reducedArmor = Math.max(0, originalArmor - this.armorPenetration);
                float armorReduction = (originalArmor - reducedArmor) / 25.0F;
                damageAmount += damageAmount * armorReduction;
            }

            boolean wasAlive = livingEntity.isAlive();

            if (livingEntity.hurt(this.damageSources().arrow(this, this.getOwner()), damageAmount)) {
                if (livingEntity.isAlive()) {
                    this.doPostHurtEffects(livingEntity);
                }
            }

            if (wasAlive && !livingEntity.isAlive() && this.isGibbsRound) {
                spawnGibbsXPBonus(livingEntity, entity.position());
            }

            if (this.isShatterRound) {
                explodeShrapnel(pResult.getLocation());
            }

            livingEntity.setArrowCount(livingEntity.getArrowCount() - 1);
            entity.invulnerableTime = 0;

            this.entitiesHit++;

            if (this.mobPenetration > 0 && this.entitiesHit <= this.mobPenetration) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
                return;
            }
        }
        this.discard();
    }

    private void spawnGibbsXPBonus(LivingEntity killedEntity, Vec3 position) {
        if (!this.level().isClientSide) {
            int baseXP = killedEntity.getExperienceReward();
            int gibbsXP = Math.round(baseXP * GIBBS_ROUND_XP_MULTIPLIER);

            if (gibbsXP > 0) {
                ExperienceOrb xpOrb = new ExperienceOrb(this.level(), position.x, position.y, position.z, gibbsXP);
                this.level().addFreshEntity(xpOrb);
            }
        }
    }

    @Override
    public void setEnchantmentEffectsFromEntity(LivingEntity pShooter, float pVelocity) {
    }

    @Override
    public boolean isCritArrow() {
        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (this.isShatterRound) {
            explodeShrapnel(result.getLocation());
        }

        this.discard();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && !this.trailSpawned && this.tickCount == 1) {
            this.spawnBulletTrail();
            this.trailSpawned = true;
        }

        if (this.inGround || this.tickCount > 100) {
            this.discard();
        }
    }

    private void spawnBulletTrail() {
        Vec3 position = this.position();
        Vec3 motion = this.getDeltaMovement();

        int trailColor = 0xFF6600;
        double trailLength = 1.0;
        int maxAge = 100;
        double trailThickness = 0.8;

        S2CMessageTurretBulletTrail message = new S2CMessageTurretBulletTrail(
                this.getId(),
                position,
                motion,
                trailColor,
                trailLength,
                maxAge,
                trailThickness
        );

        PacketHandler.getPlayChannel().sendToTrackingEntity(() -> this, message);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        this.discard();
    }

    @Override
    protected @NotNull SoundEvent getDefaultHitGroundSoundEvent() {
        return ModSounds.BULLET_FLYBY.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putDouble("TurretDamage", this.getBaseDamage());
        compound.putBoolean("TrailSpawned", this.trailSpawned);
        compound.putFloat("ArmorPenetration", this.armorPenetration);
        compound.putInt("MobPenetration", this.mobPenetration);
        compound.putInt("EntitiesHit", this.entitiesHit);
        compound.putBoolean("IsGibbsRound", this.isGibbsRound);
        compound.putBoolean("IsShatterRound", this.isShatterRound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TurretDamage")) {
            this.setBaseDamage(compound.getDouble("TurretDamage"));
        }
        this.trailSpawned = compound.getBoolean("TrailSpawned");
        if (compound.contains("ArmorPenetration")) {
            this.armorPenetration = compound.getFloat("ArmorPenetration");
        }
        if (compound.contains("MobPenetration")) {
            this.mobPenetration = compound.getInt("MobPenetration");
        }
        if (compound.contains("EntitiesHit")) {
            this.entitiesHit = compound.getInt("EntitiesHit");
        }
        if (compound.contains("IsGibbsRound")) {
            this.isGibbsRound = compound.getBoolean("IsGibbsRound");
        }
        if (compound.contains("IsShatterRound")) {
            this.isShatterRound = compound.getBoolean("IsShatterRound");
        }
    }

    @Override
    public void playSound(SoundEvent soundEvent, float volume, float pitch) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void handleInsidePortal(BlockPos pos) {
        this.discard();
    }

    private void explodeShrapnel(Vec3 explosionPos) {
        if (this.level().isClientSide) return;

        createShrapnelExplosionEffects(explosionPos);
        fireShrapnel(explosionPos);
    }

    private void createShrapnelExplosionEffects(Vec3 pos) {
        ServerLevel serverLevel = (ServerLevel) this.level();

        this.level().playSound(null, pos.x, pos.y, pos.z,
                net.minecraft.sounds.SoundEvents.GLASS_BREAK, net.minecraft.sounds.SoundSource.NEUTRAL,
                2.0f, 0.8f + this.random.nextFloat() * 0.4f);
        this.level().playSound(null, pos.x, pos.y, pos.z,
                net.minecraft.sounds.SoundEvents.AMETHYST_CLUSTER_BREAK, net.minecraft.sounds.SoundSource.NEUTRAL,
                1.5f, 1.2f + this.random.nextFloat() * 0.3f);

        for (int i = 0; i < 40; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double pitch = (this.random.nextDouble() - 0.5) * Math.PI * 0.5;
            double speed = 0.3 + this.random.nextDouble() * 0.4;

            double offsetX = Math.cos(angle) * Math.cos(pitch) * speed;
            double offsetY = Math.sin(pitch) * speed;
            double offsetZ = Math.sin(angle) * Math.cos(pitch) * speed;

            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                    pos.x, pos.y, pos.z, 1, offsetX, offsetY, offsetZ, 0.02);
        }

        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK,
                pos.x, pos.y, pos.z, 1, 0.1, 0.1, 0.1, 0.0);
    }

    private void fireShrapnel(Vec3 origin) {
        float shrapnelDamage = (float) this.getBaseDamage() * SHRAPNEL_DAMAGE_MULTIPLIER;

        for (int i = 0; i < SHRAPNEL_COUNT; i++) {
            Vec3 direction = generateRandomDirection();
            Vec3 endPos = origin.add(direction.scale(SHRAPNEL_RANGE));
            traceShrapnelRay(origin, endPos, shrapnelDamage);
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

        float length = net.minecraft.util.Mth.sqrt(lengthSquared);
        return new Vec3(x / length, y / length, z / length);
    }

    private void traceShrapnelRay(Vec3 start, Vec3 end, float damage) {
        net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(start, end).inflate(1.0);
        Entity owner = this.getOwner();
        List<Entity> hitEntities = this.level().getEntities(this, searchBox, entity ->
                entity != null &&
                        entity.isPickable() &&
                        !entity.isSpectator() &&
                        (owner == null || entity != owner) &&
                        (owner == null || entity.getId() != owner.getId())
        );

        Vec3 traceEnd = end;
        Entity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;
        Vec3 closestHitPos = null;

        for (Entity entity : hitEntities) {
            net.minecraft.world.phys.AABB boundingBox = entity.getBoundingBox();
            java.util.Optional<Vec3> hitPos = boundingBox.clip(start, end);

            if (hitPos.isPresent()) {
                double distance = start.distanceToSqr(hitPos.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                    closestHitPos = hitPos.get();
                }
            }
        }

        if (closestEntity != null) {
            closestEntity.hurt(this.damageSources().arrow(this, owner), damage);
            closestEntity.invulnerableTime = Math.min(closestEntity.invulnerableTime, 3);
            traceEnd = closestHitPos;
        } else {
            net.minecraft.world.level.ClipContext clipContext = new net.minecraft.world.level.ClipContext(
                    start, end,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    this);
            BlockHitResult blockHit = this.level().clip(clipContext);
            if (blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                traceEnd = blockHit.getLocation();
            }
        }

        createShrapnelTracer(start, traceEnd);
    }

    private void createShrapnelTracer(Vec3 start, Vec3 end) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        Vec3 direction = end.subtract(start);
        double distance = direction.length();

        if (distance < 0.1) return;

        direction = direction.normalize();
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

                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                        particlePos.x + offsetX,
                        particlePos.y + offsetY,
                        particlePos.z + offsetZ,
                        1, 0, 0, 0, 0);
            }
        }
    }

    public float getArmorPenetration() {
        return this.armorPenetration;
    }

    public void setArmorPenetration(float armorPenetration) {
        this.armorPenetration = armorPenetration;
    }

    public int getMobPenetration() {
        return this.mobPenetration;
    }

    public void setMobPenetration(int mobPenetration) {
        this.mobPenetration = mobPenetration;
    }

    public boolean isGibbsRound() {
        return this.isGibbsRound;
    }

    public void setGibbsRound(boolean isGibbsRound) {
        this.isGibbsRound = isGibbsRound;
    }

    public boolean isShatterRound() {
        return this.isShatterRound;
    }

    public void setShatterRound(boolean isShatterRound) {
        this.isShatterRound = isShatterRound;
    }

}