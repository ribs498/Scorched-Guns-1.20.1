package top.ribs.scguns.entity.projectile.turret;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModSounds;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageTurretBulletTrail;

public class TurretProjectileEntity extends AbstractArrow {
    private boolean trailSpawned = false;

    public TurretProjectileEntity(EntityType<? extends AbstractArrow> type, Level world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public TurretProjectileEntity(Level world) {
        super(ModEntities.TURRET_PROJECTILE.get(), world);
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
    protected void onHitEntity(EntityHitResult pResult) {
        Entity entity = pResult.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            float damageAmount = (float) this.getBaseDamage();
            if (livingEntity.hurt(this.damageSources().arrow(this, this.getOwner()), damageAmount)) {
                if (livingEntity.isAlive()) {
                    this.doPostHurtEffects(livingEntity);
                }
            }
            livingEntity.setArrowCount(livingEntity.getArrowCount() - 1);
            entity.invulnerableTime = 0;
        }
        this.discard();
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TurretDamage")) {
            this.setBaseDamage(compound.getDouble("TurretDamage"));
        }
        this.trailSpawned = compound.getBoolean("TrailSpawned");
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