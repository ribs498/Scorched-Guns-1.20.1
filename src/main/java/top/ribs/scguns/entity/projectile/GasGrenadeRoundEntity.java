package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;


public class GasGrenadeRoundEntity extends ProjectileEntity {
    private static final float EXPLOSION_RADIUS = 3.0f;
    private static final float GAS_CLOUD_RADIUS = 6.0f;

    public GasGrenadeRoundEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public GasGrenadeRoundEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn,
                                 LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        if (entity instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();

            boolean isBlockingMainHand = player.isBlocking() && mainHandItem.getItem() instanceof ShieldItem;
            boolean isBlockingOffHand = player.isBlocking() && offHandItem.getItem() instanceof ShieldItem;

            if (isBlockingMainHand || isBlockingOffHand) {
                ItemStack shield = isBlockingMainHand ? mainHandItem : offHandItem;
                InteractionHand hand = isBlockingMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

                player.getCooldowns().addCooldown(shield.getItem(), 100);
                player.stopUsingItem();
                player.level().broadcastEntityEvent(player, (byte)30);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1.0F,
                        0.8F + player.level().getRandom().nextFloat() * 0.4F);

                shield.hurtAndBreak(15, player, (p) -> p.broadcastBreakEvent(hand));
            }
        }

        createGasGrenadeExplosion(this, EXPLOSION_RADIUS, GAS_CLOUD_RADIUS);
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        createGasGrenadeExplosion(this, EXPLOSION_RADIUS, GAS_CLOUD_RADIUS);
    }

    @Override
    public void onExpired() {
        createGasGrenadeExplosion(this, EXPLOSION_RADIUS, GAS_CLOUD_RADIUS);
    }

    private void createGasGrenadeExplosion(Entity entity, float explosionRadius, float gasCloudRadius) {
        Level world = entity.level();
        if (world.isClientSide()) {
            return;
        }

        Vec3 explosionPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());

        playGasReleaseSound(world, explosionPos);
        spawnGasReleaseParticles(world, explosionPos, explosionRadius);
        createSulfurGasCloud(world, explosionPos, gasCloudRadius);
    }

    private void playGasReleaseSound(Level world, Vec3 pos) {
        world.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.CAT_HISS, SoundSource.BLOCKS,
                2.0F, 1.0F);
    }

    private void spawnGasReleaseParticles(Level world, Vec3 explosionPos, float radius) {
        ServerLevel serverLevel = (ServerLevel) world;

        BlockPos blockPos = BlockPos.containing(explosionPos.x, explosionPos.y, explosionPos.z);
        BlockState blockAtExplosion = world.getBlockState(blockPos);

        double adjustedY = blockAtExplosion.isAir() ? explosionPos.y + 0.2 : blockPos.getY() + 1.0;

        double renderDistance = 128.0;
        serverLevel.getEntitiesOfClass(ServerPlayer.class,
                new AABB(explosionPos.x - renderDistance, explosionPos.y - renderDistance, explosionPos.z - renderDistance,
                        explosionPos.x + renderDistance, explosionPos.y + renderDistance, explosionPos.z + renderDistance));


        for (int i = 0; i < 30; i++) {
            double angle = world.random.nextDouble() * 2 * Math.PI;
            double distance = world.random.nextDouble() * radius * 1.5;

            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;
            double offsetY = (world.random.nextDouble() - 0.5) * radius * 0.5;

            serverLevel.sendParticles(ModParticleTypes.SULFUR_DUST.get(),
                    explosionPos.x + offsetX, adjustedY + offsetY, explosionPos.z + offsetZ,
                    3,
                    (world.random.nextDouble() - 0.5) * 0.4,
                    world.random.nextDouble() * 0.3,
                    (world.random.nextDouble() - 0.5) * 0.4,
                    0.08);
        }
    }

    private void createSulfurGasCloud(Level world, Vec3 center, float radius) {
        if (world.isClientSide()) return;

        SulfurGasCloudEntity gasCloud = new SulfurGasCloudEntity(
                top.ribs.scguns.init.ModEntities.SULFUR_GAS_CLOUD.get(),
                world,
                center,
                radius,
                200,
                100,
                2
        );

        world.addFreshEntity(gasCloud);
    }
}