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
import top.ribs.scguns.common.ChokeBombCloud;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;

public class ThrowableChokeBombEntity extends ThrowableGrenadeEntity
{
    private final float explosionRadius;
    private int cloudTicks;
    private final int cloudDuration;
    private final int activationDelay;
    private boolean cloudActive = false;

    public ThrowableChokeBombEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
        this.explosionRadius = 4.0f;
        this.cloudDuration = 20 * 20;
        this.activationDelay = 40;
        this.cloudTicks = 0;
    }

    public ThrowableChokeBombEntity(Level world, LivingEntity entity, int timeLeft, float radius)
    {
        super(ModEntities.THROWABLE_CHOKE_BOMB.get(), world, entity);
        this.setShouldBounce(true);
        this.setItem(new ItemStack(ModItems.CHOKE_BOMB.get()));
        this.setMaxLife(timeLeft);
        this.explosionRadius = radius;
        this.cloudDuration = 20 * 20;
        this.activationDelay = 40;
        this.cloudTicks = 0;
    }

    @Override
    protected void defineSynchedData()
    {
    }

    @Override
    public void tick()
    {
        super.tick();

        if (!cloudActive && this.tickCount >= this.activationDelay) {
            cloudActive = true;
            this.setMaxLife(this.tickCount + this.cloudDuration);
            onCloudActivation();
        }

        if (cloudActive) {
            cloudTicks++;
            emitChokeCloudParticles();
            applyChokeEffects();

            if (cloudTicks >= cloudDuration) {
                this.remove(RemovalReason.KILLED);
            }
        }
    }

    @Override
    public void particleTick()
    {
        if (this.level().isClientSide && !cloudActive)
        {
            this.level().addParticle(ParticleTypes.WHITE_ASH, true, this.getX(), this.getY() + 0.35, this.getZ(), 0, 0, 0);
            this.level().addParticle(ParticleTypes.SNOWFLAKE, true, this.getX(), this.getY() + 0.35, this.getZ(), 0, 0, 0);
        }
    }

    private void onCloudActivation() {
        double y = this.getY() + this.getType().getDimensions().height * 0.5;
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 2, 1);
    }

    private void emitChokeCloudParticles() {
        Vec3 center = this.position();
        float intensity = 0.8f;

        if (!this.level().isClientSide) {
            ChokeBombCloud.spawnChokeCloudParticles(this.level(), center, this.explosionRadius, intensity, this.random);
        }
    }

    private void applyChokeEffects() {
        if (!this.level().isClientSide) {
            Vec3 center = this.position();
            ChokeBombCloud.applyChokeEffects(this.level(), center, this.explosionRadius);
            ChokeBombCloud.extinguishFireInArea(this.level(), center, this.explosionRadius);
        }
    }

    @Override
    public void onDeath()
    {
        if (!cloudActive) {
            cloudActive = true;
            onCloudActivation();
        }
    }
}