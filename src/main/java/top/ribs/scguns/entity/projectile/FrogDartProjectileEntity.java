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
    private static final double UNDERWATER_GRAVITY_REDUCTION = 0.1;
    private static final double LAND_GRAVITY_MULTIPLIER = 3.0;
    private static final float LAND_DAMAGE_PENALTY = 0.65f;

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
        return super.getModifiedGravity() * LAND_GRAVITY_MULTIPLIER;
    }

    @Override
    public float getDamage() {
        float baseDamage = super.getDamage();
        if (!this.isInWater()) {
            baseDamage *= LAND_DAMAGE_PENALTY;
        }

        return baseDamage;
    }

}