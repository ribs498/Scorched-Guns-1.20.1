package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBlood;
import top.ribs.scguns.network.message.S2CMessageProjectileHitBlock;
import top.ribs.scguns.network.message.S2CMessageProjectileHitEntity;
import top.ribs.scguns.util.GunEnchantmentHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class KrahgRoundProjectileEntity extends ProjectileEntity {

    private static final int ARMOR_BYPASS_AMOUNT = 4;
    private static final float KRAHG_SHIELD_DISABLE_CHANCE = 0.60f;
    private static final float SHIELD_DAMAGE_PENETRATION = 0.4f;
    private static final float MAX_BREAKABLE_HARDNESS = 4.0f;
    private boolean hasPassedThroughBlock = false;
    private static final List<Block> UNBREAKABLE_BLOCKS = Arrays.asList(
            Blocks.BEDROCK,
            Blocks.OBSIDIAN,
            Blocks.CRYING_OBSIDIAN,
            Blocks.END_PORTAL_FRAME,
            Blocks.ANCIENT_DEBRIS,
            Blocks.REINFORCED_DEEPSLATE

    );

    public KrahgRoundProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public KrahgRoundProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    private boolean canBreakBlock(BlockState state, BlockPos pos) {
        if (!Config.COMMON.gameplay.griefing.enableBlockBreaking.get()) {
            return false;
        }

        if (UNBREAKABLE_BLOCKS.contains(state.getBlock())) {
            return false;
        }
        float hardness = state.getDestroySpeed(this.level(), pos);
        if (hardness < 0 || hardness > MAX_BREAKABLE_HARDNESS) {
            return false;
        }
        if (state.hasBlockEntity()) {
            return false;
        }
        return state.getFluidState().isEmpty();
    }
    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            BlockHitResult blockResult = rayTraceBlocks(this.level(),
                    new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this),
                    IGNORE_LEAVES);

            List<EntityResult> hitEntities = this.findEntitiesOnPath(startVec, endVec);
            while (!hasPassedThroughBlock || !Objects.requireNonNull(hitEntities).isEmpty()) {
                double blockDist = blockResult.getType() != HitResult.Type.MISS ?
                        startVec.distanceToSqr(blockResult.getLocation()) : Double.MAX_VALUE;

                EntityResult closestEntity = null;
                double closestEntityDist = Double.MAX_VALUE;

                assert hitEntities != null;
                if (!hitEntities.isEmpty()) {
                    for (EntityResult entity : hitEntities) {
                        double dist = startVec.distanceToSqr(entity.getHitPos());
                        if (dist < closestEntityDist) {
                            closestEntityDist = dist;
                            closestEntity = entity;
                        }
                    }
                }
                if (blockDist < closestEntityDist && blockResult.getType() != HitResult.Type.MISS) {
                    BlockState state = this.level().getBlockState(blockResult.getBlockPos());
                    if (!canBreakBlock(state, blockResult.getBlockPos()) || hasPassedThroughBlock) {
                        Vec3 hitLoc = blockResult.getLocation();
                        this.onHitBlock(state, blockResult.getBlockPos(), blockResult.getDirection(),
                                hitLoc.x, hitLoc.y, hitLoc.z);
                        this.remove(RemovalReason.KILLED);
                        return;
                    }
                    Vec3 hitLoc = blockResult.getLocation();
                    this.onHitBlock(state, blockResult.getBlockPos(), blockResult.getDirection(),
                            hitLoc.x, hitLoc.y, hitLoc.z);
                    hasPassedThroughBlock = true;
                    startVec = hitLoc.add(this.getDeltaMovement().scale(0.01));
                    endVec = startVec.add(this.getDeltaMovement());
                    blockResult = rayTraceBlocks(this.level(),
                            new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this),
                            IGNORE_LEAVES);
                    hitEntities = this.findEntitiesOnPath(startVec, endVec);
                } else if (closestEntity != null) {
                    this.onHitEntity(closestEntity.getEntity(), closestEntity.getHitPos(),
                            startVec, endVec, closestEntity.isHeadshot());
                    this.remove(RemovalReason.KILLED);
                    return;
                } else {
                    break;
                }
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.8D, 0.8D, 0.8D));
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
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        boolean canBreak = canBreakBlock(state, pos);
        if (!this.level().isClientSide) {
            if (canBreak) {
                this.level().destroyBlock(pos, true);
                ((ServerLevel) this.level()).sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        x, y, z, 20, 0.0D, 0.0D, 0.0D, 0.15D
                );
                this.level().playSound(null, pos,
                        state.getSoundType().getBreakSound(),
                        SoundSource.BLOCKS, 1.0F, 1.0F
                );
            } else {
                ((ServerLevel) this.level()).sendParticles(
                        ParticleTypes.CRIT,
                        x, y, z,
                        10,
                        0.0D, 0.0D, 0.0D,
                        0.1D
                );
                PacketHandler.getPlayChannel().sendToTrackingChunk(
                        () -> this.level().getChunkAt(pos),
                        new S2CMessageProjectileHitBlock(x, y, z, pos, face)
                );
            }
        }
        if (!canBreak) {
            this.remove(RemovalReason.KILLED);
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

        if (entity instanceof LivingEntity livingEntity) {
            damage = applyArmorBypass(livingEntity, damage);
        }

        DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner());
        boolean blocked = ProjectileHelper.handleShieldHit(entity, this, damage, KRAHG_SHIELD_DISABLE_CHANCE);

        if (blocked) {
            float penetratingDamage = damage * SHIELD_DAMAGE_PENETRATION;
            entity.hurt(source, penetratingDamage);
        } else {
            if (!(entity.getType().is(ModTags.Entities.GHOST) && !advantage.equals(ModTags.Entities.UNDEAD.location()))) {
                entity.hurt(source, damage);
            }
        }

        if(entity instanceof LivingEntity) {
            GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), (LivingEntity) entity);
        }

        if (this.shooter instanceof Player) {
            int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter, new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }

        PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z, entity.getType()));
    }

    private float applyArmorBypass(LivingEntity entity, float damage) {
        int armorValue = entity.getArmorValue();
        int bypassedArmorValue = Math.max(0, armorValue - ARMOR_BYPASS_AMOUNT);
        float armorReduction = bypassedArmorValue * 0.04f;
        float damageMultiplier = 1.0f + Math.min(armorReduction, 0.75f);
        float finalDamage = damage * damageMultiplier;
        return Math.min(finalDamage, damage);
    }

    @Override
    public void onExpired() {
    }
}