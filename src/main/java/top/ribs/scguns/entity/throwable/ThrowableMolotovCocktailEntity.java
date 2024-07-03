package top.ribs.scguns.entity.throwable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;

/**
 * Author: MrCrayfish
 */
public class ThrowableMolotovCocktailEntity extends ThrowableGrenadeEntity
{
    public float rotation;
    public float prevRotation;

    public ThrowableMolotovCocktailEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    public ThrowableMolotovCocktailEntity(Level world, LivingEntity entity, int timeLeft)
    {
        super(ModEntities.THROWABLE_MOLOTOV_COCKTAIL.get(), world, entity);
        this.setShouldBounce(false);
        this.setItem(new ItemStack(ModItems.MOLOTOV_COCKTAIL.get()));
        this.setMaxLife(20 * 3);
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
            this.level().addParticle(ParticleTypes.FLAME, true, this.getX(), this.getY() + 0.25, this.getZ(), 0, 0, 0);
            this.level().addParticle(ParticleTypes.LAVA, true, this.getX(), this.getY() + 0.25, this.getZ(), 0, 0, 0);
        }
    }

    @Override
    public void onDeath()
    {
        double y = this.getY() + this.getType().getDimensions().height * 0.5;
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 2, 1);
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 2, 1);
        GrenadeEntity.createFireExplosion(this, 2.0F, true);
    }
}
