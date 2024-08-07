package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;

public class SculkCellEntity extends ProjectileEntity {

    private static final int ARMOR_BYPASS_AMOUNT = 6;

    public SculkCellEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public SculkCellEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide && (this.tickCount > 1 && this.tickCount < this.life)) {
            if (this.tickCount % 4 == 0) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                this.level().addParticle(ParticleTypes.SCULK_SOUL, true, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
            }
        }
        if (this.level().isClientSide && (this.tickCount > 1 && this.tickCount < this.life)) {
            if (this.tickCount % 2 == 0) {
                this.level().addParticle(ModParticleTypes.SONIC_BLAST.get(), true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        float damage = this.getDamage();
        if (entity instanceof LivingEntity livingEntity) {
            damage = applyArmorBypass(livingEntity, damage);
        }
        entity.hurt(ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner()), damage);
    }

    private float applyArmorBypass(LivingEntity entity, float damage) {
        int armorValue = entity.getArmorValue();
        int effectiveArmorValue = Math.max(0, armorValue - ARMOR_BYPASS_AMOUNT);
        float damageMultiplier = 1.0f - (effectiveArmorValue * 0.04f);

//        if (!this.level().isClientSide) {
//           // LOGGER.debug("Entity: {}, Armor Value: {}, Effective Armor Value: {}, Damage Multiplier: {}, Final Damage: {}",
//                    entity.getName().getString(), armorValue, effectiveArmorValue, damageMultiplier, damage * damageMultiplier);
//        }

        return damage * damageMultiplier;
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
    }

    @Override
    public void onExpired() {
    }
}
