package top.ribs.scguns.entity.projectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.effect.CustomExplosion;
import top.ribs.scguns.effect.PlasmaExplosion;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;

public class PlasmaProjectileEntity extends ProjectileEntity {

    public PlasmaProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public PlasmaProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide && (this.tickCount > 1 && this.tickCount < this.life)) {
            if (this.tickCount % 2 == 0) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                 this.level().addParticle(ModParticleTypes.GREEN_FLAME.get(), true, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
//                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, true, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
            }
            if (this.tickCount % 5 == 0) {
                this.level().addParticle(ModParticleTypes.PLASMA_RING.get(), true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
        }
    }


    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        float damage = this.getDamage();
        entity.hurt(ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner()), damage);
        createPlasmaExplosion(this, 1.0f);
        spawnExplosionParticles(hitVec);
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        createPlasmaExplosion(this, 1.0f);
        spawnExplosionParticles(new Vec3(x, y, z));
    }

    @Override
    public void onExpired() {
        createPlasmaExplosion(this, 1.0f);
        spawnExplosionParticles(new Vec3(this.getX(), this.getY(), this.getZ()));
    }

    public static void createPlasmaExplosion(Entity entity, float radius) {
        if (!entity.level().isClientSide) {
            PlasmaExplosion explosion = new PlasmaExplosion((ServerLevel) entity.level(), entity, entity.getX(), entity.getY(), entity.getZ(), radius, false, CustomExplosion.CustomBlockInteraction.NONE);
            explosion.explode();
        }
    }

    private void spawnExplosionParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            serverLevel.sendParticles(ModParticleTypes.PLASMA_EXPLOSION.get(), position.x, position.y, position.z, 1, 0, 0, 0, 0.1);
            for (int i = 0; i < 10; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.2;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.2;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.2;
                double speedX = (this.random.nextDouble() - 0.5) * 0.5;
                double speedY = (this.random.nextDouble() - 0.5) * 0.5;
                double speedZ = (this.random.nextDouble() - 0.5) * 0.5;
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, position.x + offsetX, position.y + offsetY, position.z + offsetZ, 1, speedX, speedY, speedZ, 0.1);
                serverLevel.sendParticles(ModParticleTypes.GREEN_FLAME.get(), position.x + offsetX, position.y + offsetY, position.z + offsetZ, 1, speedX, speedY, speedZ, 0.1);
            }
        }
    }
}