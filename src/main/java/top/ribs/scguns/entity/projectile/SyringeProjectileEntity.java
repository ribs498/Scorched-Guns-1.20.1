package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

public class SyringeProjectileEntity extends ProjectileEntity {
    private static final float REUSE_CHANCE = 0.25F;
    private boolean canBeReused = false;

    public SyringeProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public SyringeProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.canBeReused = this.random.nextFloat() < REUSE_CHANCE;
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        super.onHitEntity(entity, hitVec, startVec, endVec, headshot);
        if (!this.level().isClientSide) {
            ResourceLocation effectLocation = this.getProjectile().getImpactEffect();
            if (effectLocation != null) {
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                if (effect != null) {
                    new MobEffectInstance(
                            effect,
                            this.getProjectile().getImpactEffectDuration(),
                            this.getProjectile().getImpactEffectAmplifier()
                    );

                    int potionColor = effect.getColor();
                    ((ServerLevel) this.level()).sendParticles(
                            ParticleTypes.ENTITY_EFFECT,
                            hitVec.x,
                            hitVec.y + 0.5,
                            hitVec.z,
                            1,
                            0.3,
                            0.3,
                            0.3,
                            0.0
                    );

                    this.level().levelEvent(2002, this.blockPosition(), potionColor);
                }
            }
        }
    }
    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        super.onHitBlock(state, pos, face, x, y, z);

        if (!this.level().isClientSide) {
            ResourceLocation effectLocation = this.getProjectile().getImpactEffect();
            if (effectLocation != null) {
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                if (effect != null) {
                    int potionColor = effect.getColor();
                    this.level().levelEvent(2002, this.blockPosition(), potionColor);

                    ((ServerLevel)this.level()).sendParticles(
                            ParticleTypes.ENTITY_EFFECT,
                            x, y, z,
                            1,
                            0.3,
                            0.3,
                            0.3,
                            0.0
                    );
                }
            }
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