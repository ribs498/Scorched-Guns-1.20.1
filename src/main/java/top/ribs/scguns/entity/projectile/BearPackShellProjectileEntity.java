package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.interfaces.IDamageable;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBlood;
import top.ribs.scguns.network.message.S2CMessageProjectileHitBlock;
import top.ribs.scguns.network.message.S2CMessageProjectileHitEntity;
import top.ribs.scguns.util.GunEnchantmentHelper;

import java.util.List;

public class BearPackShellProjectileEntity extends ProjectileEntity {

    private static final float SHIELD_DISABLE_CHANCE = 0.40f;
    private static final float SHIELD_DAMAGE_PENETRATION = 0.4f;

    private int remainingPenetrations;

    public BearPackShellProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.remainingPenetrations = 2;
    }

    public BearPackShellProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        int collateralLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.COLLATERAL.get(), weapon);
        this.remainingPenetrations = 2 + collateralLevel;
    }

    @Override
    public void tick() {
        super.onProjectileTick();

        if (!this.level().isClientSide()) {
            Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            BlockHitResult blockResult = rayTraceBlocks(this.level(),
                    new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this),
                    IGNORE_LEAVES);
            if (blockResult.getType() != HitResult.Type.MISS) {
                endVec = blockResult.getLocation();
            }
            List<EntityResult> hitEntities = this.findEntitiesOnPath(startVec, endVec);
            boolean hitSomething = false;
            while (remainingPenetrations > 0 && hitEntities != null && !hitEntities.isEmpty()) {
                EntityResult closestEntity = null;
                double closestEntityDist = Double.MAX_VALUE;
                for (EntityResult entity : hitEntities) {
                    double dist = startVec.distanceToSqr(entity.getHitPos());
                    if (dist < closestEntityDist) {
                        closestEntityDist = dist;
                        closestEntity = entity;
                    }
                }

                if (closestEntity != null) {
                    this.onHitEntity(closestEntity.getEntity(), closestEntity.getHitPos(),
                            startVec, endVec, closestEntity.isHeadshot());
                    hitEntities.remove(closestEntity);
                    hitSomething = true;
                } else {
                    break;
                }
            }
            if (blockResult.getType() != HitResult.Type.MISS) {
                BlockState state = this.level().getBlockState(blockResult.getBlockPos());
                this.onHitBlock(state, blockResult.getBlockPos(), blockResult.getDirection(),
                        blockResult.getLocation().x, blockResult.getLocation().y, blockResult.getLocation().z);
                this.remove(RemovalReason.KILLED);
                return;
            }
            if (hitSomething && remainingPenetrations <= 0) {
                this.remove(RemovalReason.KILLED);
                return;
            }
        }
        this.setPos(this.getX() + this.getDeltaMovement().x,
                this.getY() + this.getDeltaMovement().y,
                this.getZ() + this.getDeltaMovement().z);
        if (this.projectile.isGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, this.modifiedGravity, 0));
        }

        this.updateHeading();

        if (this.tickCount >= this.life) {
            this.onExpired();
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        if (entity.getId() == this.shooterId) {
            return;
        }

        float damage = this.getDamage();
        float newDamage = this.getCriticalDamage(this.getWeapon(), this.random, damage);
        boolean critical = damage != newDamage;
        damage = newDamage;
        ResourceLocation advantage = this.getProjectile().getAdvantage();
        damage *= advantageMultiplier(entity);

        if (headshot) {
            damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
        }

        DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, this.shooter);
        boolean blocked = ProjectileHelper.handleShieldHit(entity, this, damage, SHIELD_DISABLE_CHANCE);

        if (blocked) {
            float penetratingDamage = damage * SHIELD_DAMAGE_PENETRATION;
            entity.hurt(source, penetratingDamage);
        } else {
            if (!(entity.getType().is(ModTags.Entities.GHOST) &&
                    !advantage.equals(ModTags.Entities.UNDEAD.location()))) {
                entity.hurt(source, damage);
            }
        }

        if (entity instanceof LivingEntity) {
            GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), (LivingEntity) entity);
        }

        if (this.shooter instanceof Player) {
            int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter, new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }

        PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z, entity.getType()));

        if (this.remainingPenetrations > 0) {
            this.remainingPenetrations--;
            entity.invulnerableTime = 0;
            Vec3 motion = this.getDeltaMovement();
            this.setPos(
                    this.getX() + motion.x * 0.2,
                    this.getY() + motion.y * 0.2,
                    this.getZ() + motion.z * 0.2
            );
            this.setDeltaMovement(motion.multiply(0.8D, 0.8D, 0.8D));
        }
    }
    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        PacketHandler.getPlayChannel().sendToTrackingChunk(() -> this.level().getChunkAt(pos),
                new S2CMessageProjectileHitBlock(x, y, z, pos, face));

        if(state.getBlock() instanceof DoorBlock) {
            boolean isOpen = state.getValue(DoorBlock.OPEN);
            if (!isOpen) {
                this.level().setBlock(pos, state.setValue(DoorBlock.OPEN, true), 10);
                this.level().playSound(null, pos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }

        if (state.getBlock() instanceof IDamageable) {
            ((IDamageable) state.getBlock()).onBlockDamaged(this.level(), state, pos, this,
                    this.getDamage(), (int) Math.ceil(this.getDamage() / 2.0) + 1);
        }

        if (!state.canBeReplaced()) {
            this.remainingPenetrations--;
            if (this.remainingPenetrations <= 0) {
                this.remove(RemovalReason.KILLED);
            } else {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.multiply(0.8D, 0.8D, 0.8D));
                this.setPos(
                        this.getX() + motion.x * 0.2,
                        this.getY() + motion.y * 0.2,
                        this.getZ() + motion.z * 0.2
                );
            }
        }
    }
    @Override
    public void remove(@NotNull RemovalReason reason) {
        if (reason == RemovalReason.KILLED) {
            if (remainingPenetrations > 0 && this.tickCount < this.life) {
                return;
            }
        }
        super.remove(reason);
    }

    @Override
    protected void onProjectileTick() {
        super.onProjectileTick();

        if (this.level().isClientSide && this.tickCount > 2 && this.tickCount < this.life) {
            for (int i = 0; i < 5; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.2;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.2;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.2;
                double velocityX = (this.random.nextDouble() - 0.5) * 0.1;
                double velocityY = (this.random.nextDouble() - 0.5) * 0.1;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.1;
                this.level().addParticle(ParticleTypes.SMALL_FLAME, true,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        velocityX, velocityY, velocityZ);
            }
        }
    }
}