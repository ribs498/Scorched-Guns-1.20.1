package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.config.EnemyProjectileConfig;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModSounds;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageTurretBulletTrail;

public class EnemyProjectileEntity extends AbstractArrow {
    private boolean trailSpawned = false;

    public EnemyProjectileEntity(EntityType<? extends AbstractArrow> type, Level world) {
        super(type, world);
    }

    public EnemyProjectileEntity(Level world, LivingEntity shooter) {
        this(ModEntities.ENEMY_PROJECTILE.get(), world, shooter);
    }

    public EnemyProjectileEntity(EntityType<EnemyProjectileEntity> type, Level world, LivingEntity shooter) {
        super(type, shooter, world);
        this.setBaseDamage(EnemyProjectileConfig.getDamageForEntity(shooter.getType()));
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            int damage = Mth.ceil(this.getBaseDamage());
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
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.discard();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && (this.inGround || this.tickCount > 300)) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide && !this.trailSpawned && this.tickCount == 1) {
            this.spawnBulletTrail();
            this.trailSpawned = true;
        }

        super.tick();
        if (!this.inGround && !this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, 0.01, 0));
        }
    }

    private void spawnBulletTrail() {
        Vec3 position = this.position();
        Vec3 motion = this.getDeltaMovement();

        int trailColor = 0xFFAA00;
        double trailLength = 1.0;
        int maxAge = 300;
        double trailThickness = 0.7;

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
    public void playSound(SoundEvent soundEvent, float volume, float pitch) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putDouble("damage", this.getBaseDamage());
        compound.putBoolean("TrailSpawned", this.trailSpawned);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("damage")) {
            this.setBaseDamage(compound.getDouble("damage"));
        }
        this.trailSpawned = compound.getBoolean("TrailSpawned");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void handleInsidePortal(BlockPos pos) {
        this.discard();
    }

    @Override
    public boolean isCritArrow() {
        return false;
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
    }

    @Override
    public void setEnchantmentEffectsFromEntity(LivingEntity pShooter, float pVelocity) {
    }
}