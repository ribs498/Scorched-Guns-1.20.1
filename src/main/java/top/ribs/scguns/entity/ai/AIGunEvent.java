package top.ribs.scguns.entity.ai;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ProjectileManager;
import top.ribs.scguns.common.SpreadTracker;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.interfaces.IProjectileFactory;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBulletTrail;
import top.ribs.scguns.network.message.S2CMessageEntityMuzzleFlash;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;


public class AIGunEvent {

    public static void performGunAttack(Mob shooter, LivingEntity target, ItemStack itemStack, Gun modifiedGun, float spreadModifier) {
        final Level level = shooter.level();
        if (level.isClientSide()) return;

        int count = modifiedGun.getGeneral().getProjectileAmount();
        Gun.Projectile projectileProps = modifiedGun.getProjectile();
        ProjectileEntity[] spawnedProjectiles = new ProjectileEntity[count];

        if (shooter.hasEffect(ModEffects.DEAFENED.get()) || shooter.hasEffect(ModEffects.BLINDED.get())) {
            spreadModifier *= 2;
        }
        if (target.hasEffect(ModEffects.DEAFENED.get())) {
            spreadModifier *= 1.5F;
        }

        for (int i = 0; i < count; ++i) {
            IProjectileFactory factory = ProjectileManager.getInstance().getFactory(BuiltInRegistries.ITEM.getKey(projectileProps.getItem()));
            ProjectileEntity projectileEntity = factory.create(level, shooter, itemStack, (GunItem) itemStack.getItem(), modifiedGun);
            projectileEntity.setWeapon(itemStack);
            projectileEntity.setAdditionalDamage(Gun.getAdditionalDamage(itemStack));

            Vec3 dir = getDirection(shooter, itemStack, (GunItem) itemStack.getItem(), modifiedGun, spreadModifier);
            double speedModifier = GunEnchantmentHelper.getProjectileSpeedModifier(itemStack);
            double speed = GunModifierHelper.getModifiedProjectileSpeed(itemStack, projectileEntity.getProjectile().getSpeed() * speedModifier);

            projectileEntity.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
            projectileEntity.updateHeading();

            double posX = shooter.xOld + (shooter.getX() - shooter.xOld) / 2.0;
            double posY = shooter.yOld + (shooter.getY() - shooter.yOld) / 2.0 + shooter.getEyeHeight();
            double posZ = shooter.zOld + (shooter.getZ() - shooter.zOld) / 2.0;
            projectileEntity.setPos(posX, posY, posZ);

            level.addFreshEntity(projectileEntity);
            spawnedProjectiles[i] = projectileEntity;
            projectileEntity.tick();
        }

        int radius = (int) shooter.getX();
        int y1 = (int) (shooter.getY() + 1.0);
        int z1 = (int) shooter.getZ();
        double r = Config.COMMON.network.projectileTrackingRange.get();

        // Send bullet trail
        ParticleOptions data = GunEnchantmentHelper.getParticle(itemStack);
        boolean isVisible = !modifiedGun.getProjectile().hideTrail();
        S2CMessageBulletTrail messageBulletTrail = new S2CMessageBulletTrail(spawnedProjectiles, projectileProps, shooter.getId(), data, isVisible);
        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                () -> LevelLocation.create(level, radius, y1, z1, r),
                messageBulletTrail
        );

        // Send muzzle flash
        if (modifiedGun.getDisplay().getFlash() != null) {
            float randomValue = level.random.nextFloat();
            S2CMessageEntityMuzzleFlash flashMessage = new S2CMessageEntityMuzzleFlash(shooter.getId(), randomValue);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(
                    () -> LevelLocation.create(level, radius, y1, z1, r),
                    flashMessage
            );
        }

        // Fire lights
        if (Config.CLIENT.display.fireLights.get()) {
            BlockState targetState = shooter.level().getBlockState(BlockPos.containing(shooter.getEyePosition()));
            if (targetState.getBlock() == ModBlocks.TEMPORARY_LIGHT.get()) {
                if (getValue(shooter.level(), BlockPos.containing(shooter.getEyePosition()), "Delay") < 1.0) {
                    updateDelayAndNotify(shooter.level(), BlockPos.containing(shooter.getEyePosition()), targetState);
                }
            } else if (targetState.getBlock() == Blocks.AIR || targetState.getBlock() == Blocks.CAVE_AIR) {
                BlockState dynamicLightState = ModBlocks.TEMPORARY_LIGHT.get().defaultBlockState();
                shooter.level().setBlock(BlockPos.containing(shooter.getEyePosition()), dynamicLightState, 3);
            }
        }
    }

    public static Vec3 getDirection(LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun, float spreadModifier)
    {
        float gunSpread = GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getGeneral().getSpread());

        if(gunSpread == 0F)
        {
            return getVectorFromRotation(shooter.getViewXRot(1F), shooter.getViewYRot(1F));
        }

        if(shooter instanceof Player)
        {
            if(!modifiedGun.getGeneral().isAlwaysSpread())
            {
                gunSpread *= SpreadTracker.get((Player) shooter).getSpread(item);
            }

            if(ModSyncedDataKeys.AIMING.getValue((Player) shooter))
            {
                gunSpread *= 0.5F;
            }
        }
        else {
            //gunSpread *= shooter.level().getDifficulty() != Difficulty.HARD ? 10F : 5F;
            gunSpread *= shooter.level().getDifficulty() != Difficulty.HARD ? spreadModifier*2 : spreadModifier;
            if (gunSpread > 60) {
                gunSpread = 60;
            }
        }

        gunSpread = Math.min(gunSpread, 170F) * 0.5F * Mth.DEG_TO_RAD;

        Vec3 vecforwards = getVectorFromRotation(shooter.getXRot(), shooter.getYRot());
        Vec3 vecupwards = getVectorFromRotation(shooter.getXRot() + 90F, shooter.getYRot());
        Vec3 vecsideways = vecforwards.cross(vecupwards);

        float theta = shooter.level().random.nextFloat() * 2F * (float) Math.PI;
        float r = Mth.sqrt(shooter.level().random.nextFloat()) * (float) Math.tan((double) gunSpread);

        float a1 = Mth.cos(theta) * r;
        float a2 = Mth.sin(theta) * r;


        return vecforwards.add(vecsideways.scale(a1)).add(vecupwards.scale(a2)).normalize();
    }

    private static Vec3 getVectorFromRotation(float pitch, float yaw)
    {
        float f = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = Mth.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -Mth.cos(-pitch * 0.017453292F);
        float f3 = Mth.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    private static void updateDelayAndNotify(LevelAccessor world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.getPersistentData().putDouble("Delay", 1.0);
        }
        if (world instanceof Level) {
            ((Level) world).sendBlockUpdated(pos, state, state, 3);
        }
    }

    public static double getValue(LevelAccessor world, BlockPos pos, String tag) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null ? blockEntity.getPersistentData().getDouble(tag) : -1.0;
    }
}