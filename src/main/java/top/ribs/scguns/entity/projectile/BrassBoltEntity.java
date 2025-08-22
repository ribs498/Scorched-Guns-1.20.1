package top.ribs.scguns.entity.projectile;

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
import top.ribs.scguns.config.BrassBoltConfig;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModSounds;

public class BrassBoltEntity extends AbstractArrow {
    public BrassBoltEntity(EntityType<? extends AbstractArrow> type, Level world) {
        super(type, world);
    }

    public BrassBoltEntity(Level world, LivingEntity shooter) {
        this(ModEntities.BRASS_BOLT.get(), world, shooter);
    }

    public BrassBoltEntity(EntityType<BrassBoltEntity> type, Level world, LivingEntity shooter) {
        super(type, shooter, world);
        this.setBaseDamage(BrassBoltConfig.getDamageForEntity(shooter.getType()));
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
        super.tick();
        if (!this.inGround && !this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, 0.01, 0));
        }
        if (this.level().isClientSide) {
            spawnTrailParticles();
        }
    }

    private void spawnTrailParticles() {
        double posX = this.getX();
        double posY = this.getY();
        double posZ = this.getZ();
        for (int i = 0; i < 1; i++) {
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
    public void playSound(SoundEvent soundEvent, float volume, float pitch) {
        // Empty to disable sounds
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putDouble("damage", this.getBaseDamage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("damage")) {
            this.setBaseDamage(compound.getDouble("damage"));
        }
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
        // Do nothing to avoid applying enchantments
    }
}