package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageProjectileHitEntity;
import top.ribs.scguns.util.GunEnchantmentHelper;

import static top.ribs.scguns.compat.CompatManager.SCULK_HORDE_LOADED;

public class FireRoundEntity extends ProjectileEntity {

    private static final float SHIELD_IGNITE_CHANCE = 0.4f;
    private static final int SCULK_CLEARING_RADIUS = 2;

    public FireRoundEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public FireRoundEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide && (this.tickCount > 1 && this.tickCount < this.life)) {
            if (this.tickCount % 2 == 0) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                this.level().addParticle(ParticleTypes.FLAME, true, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
                this.level().addParticle(ParticleTypes.LAVA, true, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
            }
            if (this.tickCount % 6 == 0) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                this.level().addParticle(ParticleTypes.SMOKE, true, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        float damage = this.getDamage();
        float newDamage = this.getCriticalDamage(this.getWeapon(), this.random, damage);
        boolean critical = damage != newDamage;
        damage = newDamage;
        ResourceLocation advantage = this.getProjectile().getAdvantage();
        damage *= advantageMultiplier(entity);

        if (headshot) {
            damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
        }

        if (entity instanceof LivingEntity livingTarget) {
            damage = applyProjectileProtection(livingTarget, damage);
        }

        DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner());

        boolean blocked = ProjectileHelper.handleShieldHit(entity, this, damage);

        if (blocked) {
            if (entity instanceof Player player && this.random.nextFloat() < SHIELD_IGNITE_CHANCE) {
                ItemStack shield = player.getUseItem();
                if (shield.getItem() instanceof ShieldItem) {
                    player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.AIR));
                    player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), shield));
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        } else {
            if (!(entity.getType().is(ModTags.Entities.GHOST) && !advantage.equals(ModTags.Entities.UNDEAD.location()))) {
                entity.hurt(source, damage);
            }
            entity.setSecondsOnFire(5);
        }

        if (entity instanceof LivingEntity) {
            GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), (LivingEntity) entity);
        }

        if (this.shooter instanceof Player) {
            int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter, new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }

        spawnExplosionParticles(hitVec);

        clearSculkInArea(BlockPos.containing(hitVec));
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        spawnExplosionParticles(new Vec3(x, y, z));
        if (Config.COMMON.gameplay.enableFirePlacement.get()) {
            setBlockOnFire(pos, face);
        }

        clearSculkInArea(pos);
    }

    @Override
    public void onExpired() {
        Vec3 position = new Vec3(this.getX(), this.getY(), this.getZ());
        spawnExplosionParticles(position);
        clearSculkInArea(BlockPos.containing(position));
    }

    private void clearSculkInArea(BlockPos center) {
        if (this.level().isClientSide) {
            return;
        }

        if (!SCULK_HORDE_LOADED) {
            int clearedBlocks = 0;
            int maxClearBlocks = 8;

            for (int x = -SCULK_CLEARING_RADIUS; x <= SCULK_CLEARING_RADIUS; x++) {
                for (int y = -SCULK_CLEARING_RADIUS; y <= SCULK_CLEARING_RADIUS; y++) {
                    for (int z = -SCULK_CLEARING_RADIUS; z <= SCULK_CLEARING_RADIUS; z++) {
                        if (clearedBlocks >= maxClearBlocks) {
                            return;
                        }

                        BlockPos checkPos = center.offset(x, y, z);
                        BlockState blockState = this.level().getBlockState(checkPos);

                        if (blockState.is(ModTags.Blocks.SCULK_BLOCKS)) {
                            this.level().destroyBlock(checkPos, true);

                            spawnCleansingParticles(checkPos);
                            this.level().playSound(null, checkPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.2F + this.random.nextFloat() * 0.4F);

                            clearedBlocks++;
                        }
                    }
                }
            }
            if (clearedBlocks > 0) {
                this.level().playSound(null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.5F, 1.5F);
            }
        }
    }

    private void spawnCleansingParticles(BlockPos pos) {
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 5; i++) {
                double offsetX = pos.getX() + 0.5 + (this.random.nextDouble() - 0.5) * 0.8;
                double offsetY = pos.getY() + 0.5 + (this.random.nextDouble() - 0.5) * 0.8;
                double offsetZ = pos.getZ() + 0.5 + (this.random.nextDouble() - 0.5) * 0.8;

                serverLevel.sendParticles(ParticleTypes.WHITE_ASH, offsetX, offsetY, offsetZ, 2, 0.2, 0.2, 0.2, 0.1);
                serverLevel.sendParticles(ParticleTypes.SMOKE, offsetX, offsetY, offsetZ, 1, 0.1, 0.3, 0.1, 0.05);
            }
            serverLevel.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.02);
        }
    }

    private void spawnExplosionParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            for (int i = 0; i < 15; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.2;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.2;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.2;
                double speedX = (this.random.nextDouble() - 0.5) * 0.5;
                double speedY = (this.random.nextDouble() - 0.5) * 0.5;
                double speedZ = (this.random.nextDouble() - 0.5) * 0.5;
                serverLevel.sendParticles(ParticleTypes.LAVA, position.x + offsetX, position.y + offsetY, position.z + offsetZ, 1, speedX, speedY, speedZ, 0.1);
                serverLevel.sendParticles(ParticleTypes.DRIPPING_LAVA, position.x + offsetX, position.y + offsetY, position.z + offsetZ, 1, speedX, speedY, speedZ, 0.1);
                serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, position.x + offsetX, position.y + offsetY, position.z + offsetZ, 1, speedX, speedY, speedZ, 0.1);
            }
        }
    }

    private void setBlockOnFire(BlockPos pos, Direction face) {
        tryPlaceWallFire(pos, face);
        if (this.random.nextFloat() < 0.6f) {
            Direction[] adjacentFaces = getAdjacentFaces(face);
            for (Direction adjacentFace : adjacentFaces) {
                if (this.random.nextFloat() < 0.4f) {
                    tryPlaceWallFire(pos, adjacentFace);
                }
            }
        }

        if (face != Direction.UP && this.random.nextFloat() < 0.7f) {
            tryPlaceWallFire(pos, Direction.UP);
        }
    }

    private boolean canSustainFireOnFace(BlockState blockState, BlockPos pos, Direction face) {
        if (blockState.isFlammable(this.level(), pos, face)) {
            return true;
        }
        return blockState.isSolidRender(this.level(), pos);
    }

    private BlockState getWallFireState(Direction attachedFace) {
        BlockState fireState = Blocks.FIRE.defaultBlockState();
        return switch (attachedFace) {
            case UP -> fireState.setValue(FireBlock.UP, true);
            case NORTH -> fireState.setValue(FireBlock.NORTH, true);
            case SOUTH -> fireState.setValue(FireBlock.SOUTH, true);
            case EAST -> fireState.setValue(FireBlock.EAST, true);
            case WEST -> fireState.setValue(FireBlock.WEST, true);
            default -> fireState;
        };
    }

    private void tryPlaceWallFire(BlockPos pos, Direction face) {
        BlockPos offsetPos = pos.relative(face);

        if (this.level().isEmptyBlock(offsetPos)) {
            BlockState hitBlockState = this.level().getBlockState(pos);

            if (canSustainFireOnFace(hitBlockState, pos, face)) {
                BlockState fireState = getWallFireState(face.getOpposite());
                this.level().setBlock(offsetPos, fireState, 11);
            }
        }
    }

    private Direction[] getAdjacentFaces(Direction face) {
        return switch (face) {
            case UP, DOWN -> new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
            case NORTH, SOUTH -> new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST};
            case EAST, WEST -> new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH};
        };
    }
}