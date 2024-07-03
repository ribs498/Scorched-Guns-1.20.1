package top.ribs.scguns.entity.throwable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModParticleTypes;

/**
 * Author: MrCrayfish
 */
public class ThrowableChokeBombEntity extends ThrowableGrenadeEntity
{
    private final float explosionRadius;

    public ThrowableChokeBombEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
        this.explosionRadius = 4.0f;
    }

    public ThrowableChokeBombEntity(Level world, LivingEntity entity, int timeLeft, float radius)
    {
        super(ModEntities.THROWABLE_CHOKE_BOMB.get(), world, entity);
        this.setShouldBounce(true);
        this.setItem(new ItemStack(ModItems.CHOKE_BOMB.get()));
        this.setMaxLife(20 * 3);
        this.explosionRadius = radius;
    }

    @Override
    protected void defineSynchedData()
    {
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    public void particleTick()
    {
        if (this.level().isClientSide)
        {
            this.level().addParticle(ParticleTypes.WHITE_ASH, true, this.getX(), this.getY() + 0.35, this.getZ(), 0, 0, 0);
            this.level().addParticle(ParticleTypes.SNOWFLAKE, true, this.getX(), this.getY() + 0.35, this.getZ(), 0, 0, 0);
        }
    }

    @Override
    public void onDeath()
    {
        double y = this.getY() + this.getType().getDimensions().height * 0.5;
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.BLOCKS, 2, 1);
        GrenadeEntity.createChokeExplosion(this, this.explosionRadius);
        this.spawnExplosionParticles(new Vec3(this.getX(), y, this.getZ()));
    }
    private void spawnExplosionParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            for (int i = 0; i < 10; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.3;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.3;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.3;
                double speedX = (this.random.nextDouble() - 0.5) * 0.5;
                double speedY = (this.random.nextDouble() - 0.5) * 0.5;
                double speedZ = (this.random.nextDouble() - 0.5) * 0.5;
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, position.x + offsetX, position.y + offsetY, position.z + offsetZ, 1, speedX, speedY, speedZ, 0.1);
                serverLevel.sendParticles(ParticleTypes.WHITE_ASH, position.x + offsetX, position.y + offsetY, position.z + offsetZ, 1, speedX, speedY, speedZ, 0.1);
            }
        }
    }
}

