package top.ribs.scguns.entity.projectile;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.effect.CustomExplosion;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.util.GunEnchantmentHelper;

/**
 * Author: MrCrayfish
 */
public class MicroJetEntity extends ProjectileEntity {
    public static final float EXPLOSION_DAMAGE_MULTIPLIER = 2.0F;
    private static final float SHIELD_DISABLE_CHANCE = 0.75f; // 75% chance to disable shield
    private static final float SHIELD_DAMAGE_PENETRATION = 0.2f; // 20% of damage passes through shield

    public MicroJetEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public MicroJetEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }
    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide) {
            for (int i = 2; i > 0; i--) {
                // Adjust the particle properties to simulate a smaller size
                this.level().addParticle(ModParticleTypes.ROCKET_TRAIL.get(), true,
                        this.getX() - (this.getDeltaMovement().x() / i),
                        this.getY() - (this.getDeltaMovement().y() / i),
                        this.getZ() - (this.getDeltaMovement().z() / i),
                        0, 0, 0);
            }
            if (this.level().random.nextInt(4) == 0) {
                this.level().addParticle(ParticleTypes.SMALL_FLAME, true,
                        this.getX(), this.getY(), this.getZ(),
                        0, 0, 0);
                this.level().addParticle(ParticleTypes.SMOKE, true,
                        this.getX(), this.getY(), this.getZ(),
                        0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        float damage = this.getDamage();
        if (headshot) {
            damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
        }
        DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner());
        boolean blocked = ProjectileHelper.handleShieldHit(entity, this, damage, SHIELD_DISABLE_CHANCE);

        if (blocked) {
            float penetratingDamage = damage * SHIELD_DAMAGE_PENETRATION;
            entity.hurt(source, penetratingDamage);
        } else {
            entity.hurt(source, damage);
        }

        if(entity instanceof LivingEntity) {
            GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), (LivingEntity) entity);
        }
        createMiniExplosion(this, 1.0f);
    }


    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        createMiniExplosion(this, 1.0f);
    }

    @Override
    public void onExpired() {
        createMiniExplosion(this, 1.0f);
    }

    public static void createMiniExplosion(Entity entity, float radius) {
        Level world = entity.level();
        if (world.isClientSide)
            return;
        CustomExplosion explosion = new CustomExplosion(world, entity, entity.getX(), entity.getY(), entity.getZ(), radius, false, CustomExplosion.CustomBlockInteraction.NONE) {
        };
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion))
            return;
        explosion.explode();
        explosion.finalizeExplosion(true);

        for (ServerPlayer player : ((ServerLevel) world).players()) {
            if (player.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) < 4096) {
                player.connection.send(new ClientboundExplodePacket(entity.getX(), entity.getY(), entity.getZ(), radius, explosion.getToBlow(), explosion.getHitPlayers().get(player)));
            }
        }
    }
}
