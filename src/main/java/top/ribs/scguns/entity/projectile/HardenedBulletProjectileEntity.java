package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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

public class HardenedBulletProjectileEntity extends ProjectileEntity {
    private static final float REUSE_CHANCE = 0.5F;
    private boolean canBeReused = false;

    public HardenedBulletProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public HardenedBulletProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.canBeReused = this.random.nextFloat() < REUSE_CHANCE;
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        super.onHitBlock(state, pos, face, x, y, z);

        if (this.canBeReused && !this.level().isClientSide) {
            ItemEntity itemEntity = new ItemEntity(
                    this.level(),
                    this.getX(), this.getY(), this.getZ(),
                    this.getItem()
            );

            double bounceStrength = 0.1;
            itemEntity.setDeltaMovement(
                    (this.random.nextDouble() - 0.5) * bounceStrength,
                    0.15,
                    (this.random.nextDouble() - 0.5) * bounceStrength
            );

            this.level().addFreshEntity(itemEntity);
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        super.onHitEntity(entity, hitVec, startVec, endVec, headshot);

        if (this.canBeReused && !this.level().isClientSide) {
            ItemEntity itemEntity = new ItemEntity(
                    this.level(),
                    this.getX(), this.getY(), this.getZ(),
                    this.getItem()
            );
            double bounceStrength = 0.1;
            itemEntity.setDeltaMovement(
                    (this.random.nextDouble() - 0.5) * bounceStrength,
                    0.15,
                    (this.random.nextDouble() - 0.5) * bounceStrength
            );
            this.level().addFreshEntity(itemEntity);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("CanBeReused", this.canBeReused);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.canBeReused = compound.getBoolean("CanBeReused");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(this.canBeReused);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        super.readSpawnData(buffer);
        this.canBeReused = buffer.readBoolean();
    }
}