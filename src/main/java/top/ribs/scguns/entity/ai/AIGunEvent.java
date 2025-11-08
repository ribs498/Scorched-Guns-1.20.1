package top.ribs.scguns.entity.ai;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ProjectileManager;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.interfaces.IProjectileFactory;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBulletTrail;
import top.ribs.scguns.network.message.S2CMessageEntityCasingEject;
import top.ribs.scguns.network.message.S2CMessageEntityMuzzleFlash;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import java.util.Objects;


public class AIGunEvent {
    public static Vec3 getLeadingDirection(LivingEntity shooter, LivingEntity target, double projectileSpeed) {
        Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.8, 0);
        Vec3 targetVelocity = target.getDeltaMovement();
        Vec3 shooterPos = shooter.position().add(0, shooter.getEyeHeight(), 0);

        Vec3 toTarget = targetPos.subtract(shooterPos);
        double distance = toTarget.length();
        double timeToHit = distance / projectileSpeed;

        Vec3 predictedPos = targetPos.add(targetVelocity.scale(timeToHit));

        return predictedPos.subtract(shooterPos).normalize();
    }

    public static void performGunAttack(Mob shooter, LivingEntity target, ItemStack itemStack, Gun modifiedGun, float accuracyModifier){
        final Level level = shooter.level();
        if (level.isClientSide()) return;

        int count = modifiedGun.getProjectile().getProjectileAmount();
        Gun.Projectile projectileProps = modifiedGun.getProjectile();
        ProjectileEntity[] spawnedProjectiles = new ProjectileEntity[count];

        if (shooter.hasEffect(ModEffects.DEAFENED.get()) || shooter.hasEffect(ModEffects.BLINDED.get())) {
            accuracyModifier *= 0.5F;
        }
        if (target.hasEffect(ModEffects.DEAFENED.get())) {
            accuracyModifier *= 0.75F;
        }

        float difficultyDamageMultiplier = getDifficultyDamageMultiplier(level.getDifficulty());
        float configDamageMultiplier = Config.COMMON.gameplay.mobGunDamageMultiplier.get().floatValue();
        float finalDamageMultiplier = difficultyDamageMultiplier * configDamageMultiplier;

        for (int i = 0; i < count; ++i) {
            IProjectileFactory factory = ProjectileManager.getInstance().getFactory(BuiltInRegistries.ITEM.getKey(Objects.requireNonNull(projectileProps.getItem())));
            ProjectileEntity projectileEntity = factory.create(level, shooter, itemStack, (GunItem) itemStack.getItem(), modifiedGun);
            projectileEntity.setWeapon(itemStack);

            float originalDamage = Gun.getAdditionalDamage(itemStack);
            float scaledDamage = originalDamage * finalDamageMultiplier;
            projectileEntity.setAdditionalDamage(scaledDamage);

            projectileEntity.getPersistentData().putFloat("AIDamageScale", finalDamageMultiplier);

            Vec3 dir = getDirection(shooter, target, itemStack, (GunItem) itemStack.getItem(), modifiedGun, accuracyModifier);

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

        ParticleOptions data = GunEnchantmentHelper.getParticle(itemStack);
        boolean isVisible = !modifiedGun.getProjectile().shouldHideTrail();
        S2CMessageBulletTrail messageBulletTrail = new S2CMessageBulletTrail(spawnedProjectiles, projectileProps, shooter.getId(), data, isVisible);
        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                () -> LevelLocation.create(level, radius, y1, z1, r),
                messageBulletTrail
        );

        if (modifiedGun.getDisplay().getFlash() != null) {
            float randomValue = level.random.nextFloat();

            Vec3 weaponOrigin = top.ribs.scguns.client.util.PropertyHelper.getModelOrigin(
                    itemStack,
                    top.ribs.scguns.client.util.PropertyHelper.GUN_DEFAULT_ORIGIN
            );
            Vec3 flashPosition = top.ribs.scguns.client.util.PropertyHelper.getMuzzleFlashPosition(
                    itemStack,
                    modifiedGun
            ).subtract(weaponOrigin);

            S2CMessageEntityMuzzleFlash flashMessage = new S2CMessageEntityMuzzleFlash(
                    shooter.getId(),
                    randomValue,
                    flashPosition,
                    false
            );

            PacketHandler.getPlayChannel().sendToNearbyPlayers(
                    () -> LevelLocation.create(level, radius, y1, z1, r),
                    flashMessage
            );
        }
        if (Config.COMMON.gameplay.spawnCasings.get()) {
            if (modifiedGun.getProjectile().ejectsCasing() && !modifiedGun.getProjectile().ejectDuringReload()) {
                ResourceLocation particleLocation = modifiedGun.getProjectile().getCasingParticle();
                if (particleLocation != null) {
                    S2CMessageEntityCasingEject casingMessage = new S2CMessageEntityCasingEject(shooter.getId(), particleLocation);
                    PacketHandler.getPlayChannel().sendToNearbyPlayers(
                            () -> LevelLocation.create(level, radius, y1, z1, r),
                            casingMessage
                    );
                }
            }
        }

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

    private static float getDifficultyDamageMultiplier(Difficulty difficulty) {
        return switch(difficulty) {
            case PEACEFUL -> 0.05F;
            case EASY -> 0.4F;
            case NORMAL -> 0.55F;
            case HARD -> 0.7F;
        };
    }

    public static Vec3 getDirection(LivingEntity shooter, LivingEntity target, ItemStack weapon, GunItem item, Gun modifiedGun, float accuracyModifier) {
        float gunSpread = GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getProjectile().getSpread());

        float baseAimError = 5.0F;

        float difficultyMod = switch(shooter.level().getDifficulty()) {
            case PEACEFUL -> 3.0F;
            case EASY -> 2.0F;
            case NORMAL -> 1.5F;
            case HARD -> 1.0F;
        };

        float aimError = (baseAimError * difficultyMod) / accuracyModifier;

        aimError = Math.min(aimError, 25F);

        Vec3 baseDirection = getVectorFromRotation(shooter.getViewXRot(1F), shooter.getViewYRot(1F));

        if (shooter.level().getDifficulty() == Difficulty.HARD && target.getDeltaMovement().lengthSqr() > 0.01) {
            double speed = modifiedGun.getProjectile().getSpeed();
            Vec3 leadDir = getLeadingDirection(shooter, target, speed);
            baseDirection = baseDirection.add(leadDir.scale(0.3)).normalize();
        }

        float aimErrorRad = aimError * Mth.DEG_TO_RAD;
        float theta1 = shooter.level().random.nextFloat() * 2F * (float) Math.PI;
        float r1 = Mth.sqrt(shooter.level().random.nextFloat()) * (float) Math.tan(aimErrorRad);

        Vec3 vecUpwards = getVectorFromRotation(shooter.getViewXRot(1F) + 90F, shooter.getViewYRot(1F));
        Vec3 vecSideways = baseDirection.cross(vecUpwards);

        float a1 = Mth.cos(theta1) * r1;
        float a2 = Mth.sin(theta1) * r1;

        Vec3 aimedDirection = baseDirection.add(vecSideways.scale(a1)).add(vecUpwards.scale(a2)).normalize();

        if (gunSpread == 0F) {
            return aimedDirection;
        }

        gunSpread = Math.min(gunSpread, 170F) * 0.5F * Mth.DEG_TO_RAD;

        Vec3 spreadUpwards = getVectorFromRotation(shooter.getViewXRot(1F) + 90F, shooter.getViewYRot(1F));
        Vec3 spreadSideways = aimedDirection.cross(spreadUpwards);

        float theta2 = shooter.level().random.nextFloat() * 2F * (float) Math.PI;
        float r2 = Mth.sqrt(shooter.level().random.nextFloat()) * (float) Math.tan(gunSpread);

        float b1 = Mth.cos(theta2) * r2;
        float b2 = Mth.sin(theta2) * r2;

        return aimedDirection.add(spreadSideways.scale(b1)).add(spreadUpwards.scale(b2)).normalize();
    }

    private static Vec3 getVectorFromRotation(float pitch, float yaw) {
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