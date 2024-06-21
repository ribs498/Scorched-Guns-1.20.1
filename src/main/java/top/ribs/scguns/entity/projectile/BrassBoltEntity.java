package top.ribs.scguns.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraftforge.network.NetworkHooks;
import top.ribs.scguns.init.ModEntities;

public class BrassBoltEntity extends AbstractArrow {
    public BrassBoltEntity(EntityType<? extends AbstractArrow> type, Level world) {
        super(type, world);
        this.setBaseDamage(5.0);
    }

    public BrassBoltEntity(Level world, LivingEntity shooter) {
        this(ModEntities.BRASS_BOLT.get(), world, shooter);
    }

    public BrassBoltEntity(EntityType<BrassBoltEntity> type, Level world, LivingEntity shooter) {
        super(type, shooter, world);
        this.setBaseDamage(5.0);
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
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
        super.tick();

        // Spawn particle trail
        if (this.level().isClientSide) {
            spawnTrailParticles();
        }

        if (this.inGround) {
            this.discard();
        }
    }

    private void spawnTrailParticles() {
        double posX = this.getX();
        double posY = this.getY();
        double posZ = this.getZ();

        for (int i = 0; i < 2; i++) {
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
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return null; // Return null to prevent default sound
    }

    @Override
    public void playSound(SoundEvent soundEvent, float volume, float pitch) {
        // Override to prevent sound from playing
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
