package top.ribs.scguns.entity.throwable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.entity.monster.SwarmEntity;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;

/**
 * Author: MrCrayfish
 */
public class ThrowableSwarmBombEntity extends ThrowableGrenadeEntity {
    public ThrowableSwarmBombEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ThrowableSwarmBombEntity(Level world, LivingEntity entity, int timeLeft) {
        super(ModEntities.THROWABLE_SWARM_BOMB.get(), world, entity);
        this.setShouldBounce(false);
        this.setItem(new ItemStack(ModItems.SWARM_BOMB.get()));
        this.setMaxLife(20 * 3);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void particleTick() {
    }

    @Override
    public void onDeath() {
        if (!this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                SwarmEntity swarm = new SwarmEntity(ModEntities.SWARM.get(), this.level());
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;
                swarm.moveTo(this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, this.getYRot(), this.getXRot());
                this.level().addFreshEntity(swarm);
            }
            AreaEffectCloud effectCloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
            effectCloud.setRadius(3.0F);
            effectCloud.setDuration(200);
            effectCloud.setRadiusOnUse(-0.5F);
            effectCloud.setWaitTime(10);
            effectCloud.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            this.level().addFreshEntity(effectCloud);
        }
    }
}
