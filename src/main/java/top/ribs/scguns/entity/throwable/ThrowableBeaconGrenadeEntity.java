package top.ribs.scguns.entity.throwable;

import net.minecraft.core.BlockPos;
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
import top.ribs.scguns.entity.monster.SignalBeaconEntity;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;

public class ThrowableBeaconGrenadeEntity extends ThrowableGrenadeEntity {
    public float rotation;
    public float prevRotation;

    public ThrowableBeaconGrenadeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ThrowableBeaconGrenadeEntity(Level world, LivingEntity entity, int timeLeft) {
        super(ModEntities.THROWABLE_BEACON_GRENADE.get(), world, entity);
        this.setShouldBounce(true);
        this.setGravityVelocity(0.025F);
        this.setItem(new ItemStack(ModItems.BEACON_GRENADE.get()));
        this.setMaxLife(timeLeft);
    }

    @Override
    public void tick() {
        super.tick();
        this.prevRotation = this.rotation;
    }

    @Override
    public void particleTick() {
    }

    @Override
    protected void onHit(HitResult result) {
        switch (result.getType()) {
            case BLOCK:
                BlockHitResult blockResult = (BlockHitResult) result;
                if (this.shouldBounce) {
                    BlockState state = this.level().getBlockState(blockResult.getBlockPos());
                    double speed = this.getDeltaMovement().length();
                    if (speed > 0.1) {
                        this.level().playSound(null, blockResult.getLocation().x, blockResult.getLocation().y, blockResult.getLocation().z,
                                state.getBlock().getSoundType(state, this.level(), blockResult.getBlockPos(), this).getStepSound(),
                                SoundSource.AMBIENT, 1.0F, 1.0F);
                    }
                    this.bounce(blockResult.getDirection());
                }
                this.remove(RemovalReason.KILLED);
                this.onDeath();
                break;
            case ENTITY:
                EntityHitResult entityResult = (EntityHitResult) result;
                Entity entity = entityResult.getEntity();
                if (this.shouldBounce) {
                    double speed = this.getDeltaMovement().length();
                    if (speed > 0.1) {
                        entity.hurt(entity.damageSources().thrown(this, this.getOwner()), 1.0F);
                    }
                    this.bounce(net.minecraft.core.Direction.getNearest(this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z()).getOpposite());
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.25, 1.0, 0.25));
                }
                this.remove(RemovalReason.KILLED);
                this.onDeath();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDeath() {
        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 2.0F, 1.0F);
            Vec3 pos = new Vec3(this.getX(), this.getY(), this.getZ());
            BlockPos blockPos = new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
            while (!this.level().getBlockState(blockPos).isAir() && pos.y < this.getY() + 1.0) {
                pos = pos.add(0, 0.1, 0);
                blockPos = new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
            }
            SignalBeaconEntity beacon = new SignalBeaconEntity(ModEntities.SIGNAL_BEACON.get(), this.level());
            beacon.moveTo(pos.x, pos.y, pos.z, this.random.nextFloat() * 360F, 0.0F);
            boolean added = this.level().addFreshEntity(beacon);
        }
    }
}