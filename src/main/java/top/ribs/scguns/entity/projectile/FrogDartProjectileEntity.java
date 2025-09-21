package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;


public class FrogDartProjectileEntity extends ProjectileEntity {

    private static final double UNDERWATER_SPEED_MULTIPLIER = 1.5;
    private static final double LAND_SPEED_MULTIPLIER = 0.6;
    private static final double UNDERWATER_GRAVITY_REDUCTION = 0.3;
    private static final float LAND_DAMAGE_PENALTY = 0.7f;

    private boolean wasUnderwater = false;

    public FrogDartProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public FrogDartProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        if (this.isInWater()) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.scale(UNDERWATER_SPEED_MULTIPLIER));
            this.wasUnderwater = true;
        }
    }

    @Override
    public void tick() {
        boolean currentlyUnderwater = this.isInWater();
        if (currentlyUnderwater && !wasUnderwater) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.scale(UNDERWATER_SPEED_MULTIPLIER / LAND_SPEED_MULTIPLIER));
        } else if (!currentlyUnderwater && wasUnderwater) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.scale(LAND_SPEED_MULTIPLIER / UNDERWATER_SPEED_MULTIPLIER));
        }

        wasUnderwater = currentlyUnderwater;

        super.tick();
        if (currentlyUnderwater && !this.level().isClientSide) {
            if (this.tickCount % 3 == 0) {
                ServerLevel serverLevel = (ServerLevel) this.level();
                serverLevel.sendParticles(ParticleTypes.BUBBLE,
                        this.getX(), this.getY(), this.getZ(),
                        1, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }

    @Override
    protected void onProjectileTick() {
        if (this.isInWater()) {
            Vec3 motion = this.getDeltaMovement();
            if (motion.lengthSqr() > 0.001) {
                this.setDeltaMovement(motion.scale(1.02));
            }
        }
    }

    @Override
    public double getModifiedGravity() {
        if (this.isInWater()) {
            return super.getModifiedGravity() * UNDERWATER_GRAVITY_REDUCTION;
        }
        return super.getModifiedGravity();
    }

    @Override
    public float getDamage() {
        float baseDamage = super.getDamage();
        if (!this.isInWater()) {
            baseDamage *= LAND_DAMAGE_PENALTY;
        }

        return baseDamage;
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        super.onHitBlock(state, pos, face, x, y, z);
        if (!this.level().isClientSide && !this.isInWater()) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            Vec3 hitPos = new Vec3(x, y, z);
            for (int i = 0; i < 3; i++) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        hitPos.x, hitPos.y, hitPos.z,
                        1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        this.random.nextDouble() * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        0.02);
            }
        }
    }
}