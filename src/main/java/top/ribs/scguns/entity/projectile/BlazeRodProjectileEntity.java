package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

public class BlazeRodProjectileEntity extends ProjectileEntity {
    private static final float REUSE_CHANCE = 0.75F;
    private static final float POWDER_DROP_CHANCE = 0.75F;
    private static final float FIRE_CHANCE = 0.75F;
    private static final int FIRE_DURATION = 5;
    private boolean canBeReused = false;
    public BlazeRodProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public BlazeRodProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
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
                    new ItemStack(Items.BLAZE_ROD)
            );

            double bounceStrength = 0.05;
            itemEntity.setDeltaMovement(
                    (this.random.nextDouble() - 0.5) * bounceStrength,
                    0.2,
                    (this.random.nextDouble() - 0.5) * bounceStrength
            );

            this.level().addFreshEntity(itemEntity);
            this.level().playSound(null,
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLAZE_SHOOT,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
            );
        } else {
            if (!this.level().isClientSide && this.random.nextFloat() < POWDER_DROP_CHANCE) {
                ItemEntity powderEntity = new ItemEntity(
                        this.level(),
                        this.getX(), this.getY(), this.getZ(),
                        new ItemStack(Items.BLAZE_POWDER)
                );

                double bounceStrength = 0.15;
                powderEntity.setDeltaMovement(
                        (this.random.nextDouble() - 0.5) * bounceStrength,
                        0.15,
                        (this.random.nextDouble() - 0.5) * bounceStrength
                );

                this.level().addFreshEntity(powderEntity);
            }
            if (!this.level().isClientSide) {
                ((ServerLevel)this.level()).sendParticles(ParticleTypes.FLAME,
                        this.getX(), this.getY(), this.getZ(),
                        8,
                        0.2D, 0.2D, 0.2D,
                        0.05D // speed
                );
            }

            this.level().playSound(null,
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
            );
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {

        if (!this.level().isClientSide && this.random.nextFloat() < FIRE_CHANCE) {
            entity.setSecondsOnFire(FIRE_DURATION);
            ((ServerLevel)this.level()).sendParticles(
                    ParticleTypes.FLAME,
                    entity.getX(),
                    entity.getY() + (entity.getBbHeight() * 0.5),
                    entity.getZ(),
                    10,
                    0.2D, 0.2D, 0.2D,
                    0.05D
            );
        }
        super.onHitEntity(entity, hitVec, startVec, endVec, headshot);
        if (this.canBeReused && !this.level().isClientSide) {
            ItemEntity itemEntity = new ItemEntity(
                    this.level(),
                    this.getX(), this.getY(), this.getZ(),
                    new ItemStack(Items.BLAZE_ROD)
            );
            double bounceStrength = 0.05;
            itemEntity.setDeltaMovement(
                    (this.random.nextDouble() - 0.5) * bounceStrength,
                    0.2,
                    (this.random.nextDouble() - 0.5) * bounceStrength
            );

            this.level().addFreshEntity(itemEntity);
            this.level().playSound(null,
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLAZE_SHOOT,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
            );
        } else {
            if (!this.level().isClientSide && this.random.nextFloat() < POWDER_DROP_CHANCE) {
                ItemEntity powderEntity = new ItemEntity(
                        this.level(),
                        this.getX(), this.getY(), this.getZ(),
                        new ItemStack(Items.BLAZE_POWDER)
                );
                double bounceStrength = 0.15;
                powderEntity.setDeltaMovement(
                        (this.random.nextDouble() - 0.5) * bounceStrength,
                        0.15,
                        (this.random.nextDouble() - 0.5) * bounceStrength
                );

                this.level().addFreshEntity(powderEntity);
            }
            if (!this.level().isClientSide) {
                ((ServerLevel)this.level()).sendParticles(ParticleTypes.FLAME,
                        this.getX(), this.getY(), this.getZ(),
                        8,
                        0.2D, 0.2D, 0.2D,
                        0.05D
                );
            }
        }
    }
    @Override
    protected void onProjectileTick() {
        super.onProjectileTick();
        if (this.level().isClientSide) {
            this.level().addParticle(
                    ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(),
                    0, 0, 0
            );
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