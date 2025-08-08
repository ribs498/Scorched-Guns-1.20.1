package top.ribs.scguns.entity.projectile.turret;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Config;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModSounds;

public class TurretProjectileEntity extends AbstractArrow {

    public TurretProjectileEntity(EntityType<? extends AbstractArrow> type, Level world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public TurretProjectileEntity(Level world, BulletType bulletType) {
        super(ModEntities.TURRET_PROJECTILE.get(), world);

        double baseDamage = Config.COMMON.turret.bulletDamage.get(bulletType).get();

        if (Config.COMMON.turret.enableDamageScaling.get()) {
            long daysInWorld = this.level().getDayTime() / 24000L;
            double scalingRate = Config.COMMON.turret.damageScalingRate.get();
            double maxDamage = Config.COMMON.turret.maxScaledDamage.get();

            double scaledDamage = Math.min(baseDamage + (scalingRate * daysInWorld), maxDamage);
            this.setBaseDamage(scaledDamage);
        } else {
            this.setBaseDamage(baseDamage);
        }

        this.setNoGravity(true);
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
    public void setEnchantmentEffectsFromEntity(LivingEntity pShooter, float pVelocity) {
        // Do nothing to avoid applying enchantments
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        Entity entity = pResult.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            float damageAmount = (float) this.getBaseDamage();
            int damage = Mth.ceil(damageAmount);
            if (livingEntity.hurt(this.damageSources().arrow(this, this.getOwner()), damage)) {
                if (livingEntity.isAlive()) {
                    this.doPostHurtEffects(livingEntity);
                }
            }
            livingEntity.setArrowCount(livingEntity.getArrowCount() - 1);
        }
        this.discard();
    }

    @Override
    public boolean isCritArrow() {
        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            spawnTrailParticles();
        }

        if (this.inGround || this.tickCount > 100) {
            this.discard();
        }
    }

    private void spawnTrailParticles() {
        double posX = this.getX();
        double posY = this.getY();
        double posZ = this.getZ();

        for (int i = 0; i < 3; i++) {
            double offsetX = this.random.nextGaussian() * 0.02;
            double offsetY = this.random.nextGaussian() * 0.02;
            double offsetZ = this.random.nextGaussian() * 0.02;
            this.level().addParticle(ParticleTypes.SMALL_FLAME, posX, posY, posZ, offsetX, offsetY, offsetZ);
        }
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TurretDamage")) {
            this.setBaseDamage(compound.getDouble("TurretDamage"));
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

    public enum BulletType {
        STANDARD_COPPER_ROUND,
        ADVANCED_ROUND,
        GIBBS_ROUND,
        COMPACT_COPPER_ROUND,
        COMPACT_ADVANCED_ROUND,
        HOG_ROUND,
        SHOTGUN_SHELL,
        BEARPACK_SHELL
    }
}