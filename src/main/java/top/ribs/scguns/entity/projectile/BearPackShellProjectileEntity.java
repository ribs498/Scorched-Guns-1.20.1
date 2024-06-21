package top.ribs.scguns.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

public class BearPackShellProjectileEntity extends ProjectileEntity {

    public BearPackShellProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public BearPackShellProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onProjectileTick()
    {
        if(this.level().isClientSide && (this.tickCount > 2 && this.tickCount < this.life)) {

            for(int i = 0; i < 5; i++) {
                this.level().addParticle(ParticleTypes.FLAME, true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }

        }
    }

}
