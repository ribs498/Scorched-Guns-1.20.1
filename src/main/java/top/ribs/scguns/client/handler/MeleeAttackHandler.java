package top.ribs.scguns.client.handler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.event.GunEventBus;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.BayonetItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageReload;
import top.ribs.scguns.network.message.S2CMessageMeleeAttack;
import top.ribs.scguns.util.GunModifierHelper;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MeleeAttackHandler {
    private static final double REACH_DISTANCE = 3.0f;
    private static final float ENCHANTMENT_DAMAGE_SCALING_FACTOR = 0.70f;
    private static final float BASE_SPEED_DAMAGE_SCALING_FACTOR = 0.0f;
    private static final float[] BANZAI_SCALING_FACTORS = {3.0f, 5.5f, 7.0f};
    private static final String WALL_COLLISION_COOLDOWN_TAG = "WallCollisionCooldown";
    private static final String MELEE_COOLDOWN_TAG = "MeleeCooldown";
    private static final int KNOCKBACK_GRACE_PERIOD_TICKS = 5;
    private static final String KNOCKBACK_GRACE_TAG = "KnockbackGracePeriod";
    private static boolean isBanzai = false;
    private static final int WALL_COLLISION_COOLDOWN_TICKS = 20;
    private static final double WALL_CHECK_DISTANCE = 1.0;
    private static final double[] WALL_CHECK_ANGLES = {0, 10, -10, 20, -20, 30, -30};
    public static boolean isBanzaiActive() {
        return isBanzai;
    }
    private static final String BANZAI_DAMAGE_COOLDOWN_TAG = "BanzaiDamageCooldown";
    private static final int BANZAI_DAMAGE_COOLDOWN_TICKS = 25;
    private static final double BANZAI_AOE_RADIUS = 1.5;
    private static ItemStack banzaiActiveItem = ItemStack.EMPTY;

    public static void startBanzai(ServerPlayer player) {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }
        if (!gunItem.hasBayonet(heldItem)) {
            performMeleeAttack(player);
            return;
        }
        isBanzai = true;
        banzaiActiveItem = heldItem.copy();
    }

    public static void stopBanzai() {
        isBanzai = false;
        banzaiActiveItem = ItemStack.EMPTY;
    }

    public static void performMeleeAttack(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }
        if (isMeleeOnCooldown(player, heldItem)) {
            return;
        }

        // Handle reload cancellation for animated guns
        if (heldItem.getItem() instanceof AnimatedGunItem animatedGunItem) {
            CompoundTag tag = heldItem.getTag();
            long id = GeoItem.getId(heldItem);
            AnimationController<GeoAnimatable> animationController = animatedGunItem.getAnimatableInstanceCache()
                    .getManagerForId(id)
                    .getAnimationControllers()
                    .get("controller");

            if (tag != null && tag.getBoolean("scguns:IsReloading")) {
                Gun gun = gunItem.getModifiedGun(heldItem);
                if (gun.getReloads().getReloadType() == ReloadType.MAG_FED) {
                    // For mag-fed weapons, completely reset the reload
                    tag.remove("scguns:IsReloading");
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                    if (animationController != null) {
                        animationController.forceAnimationReset();
                    }
                } else if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                    // For manual reload weapons, trigger reload stop
                    if (animationController != null &&
                            (animatedGunItem.isAnimationPlaying(animationController, "reload_loop") ||
                                    animatedGunItem.isAnimationPlaying(animationController, "reload_start"))) {
                        tag.putBoolean("scguns:ReloadComplete", true);
                        animationController.tryTriggerAnimation("reload_stop");
                        tag.remove("scguns:IsReloading");
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                    }
                }
            }
        }
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
        if (heldItem.getItem() instanceof AnimatedGunItem) {
            AnimationController<GeoAnimatable> controller = ((AnimatedGunItem)heldItem.getItem())
                    .getAnimatableInstanceCache()
                    .getManagerForId(GeoItem.getId(heldItem))
                    .getAnimationControllers()
                    .get("controller");

            if (controller != null && ((AnimatedGunItem)heldItem.getItem()).isAnimationPlaying(controller, "inspect")) {
                controller.tryTriggerAnimation("idle");
            }
        }

        PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageMeleeAttack(heldItem));
        LivingEntity target = findTargetWithinReach(player);
        if (target != null && target != player) {
            performMeleeAttackOnTarget(player, target, false);
            damageGunAndAttachments(heldItem, player);
        } else {
            HitResult hitResult = rayTraceBlocks(player);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos pos = blockHitResult.getBlockPos();
                BlockState blockState = player.level().getBlockState(pos);
                ClientboundLevelParticlesPacket particlePacket = getClientboundLevelParticlesPacket(blockHitResult, blockState);
                player.connection.send(particlePacket);
            }
        }
        setMeleeCooldown(player, heldItem, gunItem);
    }

    @NotNull
    private static ClientboundLevelParticlesPacket getClientboundLevelParticlesPacket(BlockHitResult blockHitResult, BlockState blockState) {
        Vec3 hitVec = blockHitResult.getLocation();
        BlockParticleOption particleData = new BlockParticleOption(ParticleTypes.BLOCK, blockState);
        return new ClientboundLevelParticlesPacket(
                particleData,
                true,
                hitVec.x,
                hitVec.y,
                hitVec.z,
                0.0F,
                0.0F,
                0.0F,
                0.1F,
                10
        );
    }
    private static HitResult rayTraceBlocks(Player player) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getLookAngle();
        Vec3 reachVector = eyePosition.add(lookVector.scale(MeleeAttackHandler.REACH_DISTANCE));
        return player.level().clip(new ClipContext(
                eyePosition,
                reachVector,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));
    }

    public static boolean isMeleeOnCooldown(Player player, ItemStack heldItem) {
        CompoundTag tag = heldItem.getOrCreateTag();
        long currentTime = player.level().getGameTime();
        return tag.contains(MELEE_COOLDOWN_TAG) && currentTime < tag.getLong(MELEE_COOLDOWN_TAG);
    }

    public static void setMeleeCooldown(Player player, ItemStack heldItem, GunItem gunItem) {
        CompoundTag tag = heldItem.getOrCreateTag();
        long currentTime = player.level().getGameTime();
        int cooldownTicks = gunItem.getModifiedGun(heldItem).getGeneral().getMeleeCooldownTicks();
        tag.putLong(MELEE_COOLDOWN_TAG, currentTime + cooldownTicks);
        heldItem.setTag(tag);
    }
    private static void performMeleeAttackOnTarget(ServerPlayer player, LivingEntity target, boolean isBanzaiAttack) {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }

        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float additionalDamage = GunModifierHelper.getAdditionalDamage(heldItem, true);

        float enchantmentDamage = getEnchantmentDamageFromBayonet(heldItem, target, gunItem);
        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        float meleeDamage = modifiedGun.getGeneral().getMeleeDamage();
        meleeDamage += gunItem.getBayonetAdditionalDamage(heldItem);

        float attackDamage = baseDamage + additionalDamage + enchantmentDamage + meleeDamage;
        if (isBanzaiAttack) {
            float speedDamageMultiplier = getBanzaiDamageMultiplier(player, heldItem);
            attackDamage *= speedDamageMultiplier;
            attackDamage = (float) (Math.round(attackDamage * 100.0) / 100.0);
        }
        DamageSource damageSource = player.serverLevel().damageSources().playerAttack(player);

        if (isBanzaiAttack) {
            List<LivingEntity> targets = findTargetsInArea(player, 2.5);
            for (LivingEntity aoeTarget : targets) {
                if (aoeTarget.hurt(damageSource, attackDamage)) {
                    player.level().playSound(
                            null,
                            aoeTarget.getX(),
                            aoeTarget.getY(),
                            aoeTarget.getZ(),
                            SoundEvents.PLAYER_ATTACK_WEAK,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );

                    applyKnockback(player, aoeTarget, heldItem);
                    applySpecialEnchantmentsFromBayonet(heldItem, aoeTarget, player, gunItem);
                    triggerBanzaiImpactIfNecessary(heldItem);
                    if (player.level().isClientSide) {
                        ClientMeleeAttackHandler.spawnHitParticles((ClientLevel) player.level(), aoeTarget);
                    }
                }
            }
        } else {
            LivingEntity raycastTarget = raycastForMeleeAttack(player, 2.5);
            if (raycastTarget != null && raycastTarget.hurt(damageSource, attackDamage)) {
                applyKnockback(player, raycastTarget, heldItem);
                applySpecialEnchantmentsFromBayonet(heldItem, raycastTarget, player, gunItem);
                triggerBanzaiImpactIfNecessary(heldItem);
                if (player.level().isClientSide) {
                    ClientMeleeAttackHandler.spawnHitParticles((ClientLevel) player.level(), raycastTarget);
                }
            }
        }
    }
    private static void triggerBanzaiImpactIfNecessary(ItemStack heldItem) {
        if (((GunItem) heldItem.getItem()).hasBayonet(heldItem)) {
            GunRenderingHandler.get().triggerBanzaiImpact();
        }
    }

    private static void applySpecialEnchantmentsFromBayonet(ItemStack gunStack, LivingEntity target, Player player, GunItem gunItem) {
        for (IAttachment.Type type : IAttachment.Type.values()) {
            ItemStack attachmentStack = gunItem.getAttachment(gunStack, type);
            if (attachmentStack.getItem() instanceof BayonetItem) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(attachmentStack);
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    int level = entry.getValue();
                    applyEnchantmentEffects(enchantment, level, target, player);
                }
            }
        }
    }

    public static void performNormalMeleeAttack(ServerPlayer player) {
        performMeleeAttack(player);
    }

    public static void handleBanzaiMode(ServerPlayer player) {
        if (!isBanzai) return;

        ItemStack currentHeldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!ItemStack.matches(currentHeldItem, banzaiActiveItem)) {
            stopBanzai();
            return;
        }

        CompoundTag playerData = player.getPersistentData();
        long currentTime = player.level().getGameTime();

        boolean inGracePeriod = playerData.contains(KNOCKBACK_GRACE_TAG) &&
                currentTime < playerData.getLong(KNOCKBACK_GRACE_TAG);

        if (!player.isSprinting() && !inGracePeriod) {
            stopBanzai();
            return;
        }

        if (checkForWallCollision(player)) {
            knockPlayerBack(player);
            sendWallImpactParticles(player);
            triggerBanzaiImpactIfNecessary(currentHeldItem);
            playerData.putLong(KNOCKBACK_GRACE_TAG, currentTime + KNOCKBACK_GRACE_PERIOD_TICKS);
            return;
        }

        if (playerData.contains(BANZAI_DAMAGE_COOLDOWN_TAG) &&
                currentTime < playerData.getLong(BANZAI_DAMAGE_COOLDOWN_TAG)) {
            return;
        }

        List<LivingEntity> targets = findTargetsInArea(player, BANZAI_AOE_RADIUS);
        if (!targets.isEmpty()) {
            playerData.putLong(BANZAI_DAMAGE_COOLDOWN_TAG, currentTime + BANZAI_DAMAGE_COOLDOWN_TICKS);
            for (LivingEntity target : targets) {
                if (target != player) {
                    performMeleeAttackOnTarget(player, target, true);
                }
            }
        }
    }
    private static boolean checkForWallCollision(ServerPlayer player) {
        CompoundTag playerData = player.getPersistentData();
        long currentTime = player.level().getGameTime();
        if (playerData.contains(WALL_COLLISION_COOLDOWN_TAG) &&
                currentTime < playerData.getLong(WALL_COLLISION_COOLDOWN_TAG)) {
            return false;
        }
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getLookAngle();
        Vec3 playerMotion = player.getDeltaMovement();
        if (lookVector.dot(playerMotion) <= 0) {
            return false;
        }
        double[] heightOffsets = {0, 0.5, -0.5};
        for (double heightOffset : heightOffsets) {
            Vec3 checkPosition = eyePosition.add(0, heightOffset, 0);
            for (double angle : WALL_CHECK_ANGLES) {
                Vec3 rotatedVector = rotateVector(lookVector, angle);
                Vec3 reachVector = checkPosition.add(rotatedVector.scale(WALL_CHECK_DISTANCE));

                BlockHitResult hitResult = player.level().clip(new ClipContext(
                        checkPosition,
                        reachVector,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                ));

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    playerData.putLong(WALL_COLLISION_COOLDOWN_TAG,
                            currentTime + WALL_COLLISION_COOLDOWN_TICKS);
                    return true;
                }
            }
        }
        return false;
    }

    private static Vec3 rotateVector(Vec3 lookVector, double angle) {
        double angleRadians = Math.toRadians(angle);
        double x = lookVector.x * Math.cos(angleRadians) - lookVector.z * Math.sin(angleRadians);
        double z = lookVector.x * Math.sin(angleRadians) + lookVector.z * Math.cos(angleRadians);
        return new Vec3(x, lookVector.y, z);
    }

    private static void knockPlayerBack(ServerPlayer player) {
        Vec3 knockbackDirection = player.getLookAngle().scale(-0.5);
        player.push(knockbackDirection.x, 0.3, knockbackDirection.z);
        player.hurtMarked = true;
    }
    private static void sendWallImpactParticles(ServerPlayer player) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getLookAngle();
        Vec3 reachVector = eyePosition.add(lookVector.scale(WALL_CHECK_DISTANCE));
        ClipContext context = new ClipContext(
                eyePosition,
                reachVector,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );
        BlockHitResult hitResult = player.level().clip(context);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState blockState = player.level().getBlockState(pos);
            ClientboundLevelParticlesPacket particlePacket = getClientboundLevelParticlesPacket(hitResult, blockState);
            player.connection.send(particlePacket);
            player.level().playSound(
                    null,
                    hitResult.getLocation().x,
                    hitResult.getLocation().y,
                    hitResult.getLocation().z,
                    SoundEvents.SHIELD_BLOCK,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
    }

    private static float getBanzaiDamageMultiplier(ServerPlayer player, ItemStack heldItem) {
        double speed = player.getDeltaMovement().length();
        int banzaiLevel = ((GunItem) heldItem.getItem()).getBayonetBanzaiLevel(heldItem);
        float scalingFactor = BASE_SPEED_DAMAGE_SCALING_FACTOR;
        if (banzaiLevel > 0 && banzaiLevel <= 3) {
            scalingFactor = BANZAI_SCALING_FACTORS[banzaiLevel - 1];
        }
        return 1.0f + (float) speed * scalingFactor;
    }

    private static float getEnchantmentDamageFromBayonet(ItemStack gunStack, LivingEntity target, GunItem gunItem) {
        float enchantmentDamage = 0.0f;
        for (IAttachment.Type type : IAttachment.Type.values()) {
            ItemStack attachmentStack = gunItem.getAttachment(gunStack, type);
            if (attachmentStack.getItem() instanceof BayonetItem) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(attachmentStack);
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    int level = entry.getValue();
                    if (enchantment instanceof DamageEnchantment damageEnchantment) {
                        float damageBonus = damageEnchantment.getDamageBonus(level, target.getMobType());
                        enchantmentDamage += damageBonus * ENCHANTMENT_DAMAGE_SCALING_FACTOR;
                    }
                }
            }
        }
        return enchantmentDamage;
    }

    private static void applyEnchantmentEffects(Enchantment enchantment, int level, LivingEntity target, Player player) {
        if (enchantment == Enchantments.FIRE_ASPECT) {
            target.setSecondsOnFire(level * 4);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMeleeAttackHandler.spawnParticleEffect(player, target, ParticleTypes.FLAME));
        } else if (enchantment == Enchantments.KNOCKBACK) {
            Vec3 direction = target.position().subtract(player.position()).normalize();
            target.knockback(level * 0.5F, -direction.x(), -direction.z());
        } else if (enchantment == Enchantments.SMITE) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMeleeAttackHandler.spawnParticleEffect(player, target, ParticleTypes.ENCHANTED_HIT));
        } else if (enchantment == Enchantments.BANE_OF_ARTHROPODS) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMeleeAttackHandler.spawnParticleEffect(player, target, ParticleTypes.ENCHANTED_HIT));
        } else if (enchantment == Enchantments.SHARPNESS) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMeleeAttackHandler.spawnParticleEffect(player, target, ParticleTypes.ENCHANTED_HIT));
        }
    }

    private static void applyKnockback(Player player, LivingEntity target, ItemStack stack) {
        int knockbackLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, stack);
        Vec3 direction = target.position().subtract(player.position()).normalize();
        target.knockback(0.4F + (knockbackLevel * 0.5F), -direction.x(), -direction.z());
    }
    private static LivingEntity raycastForMeleeAttack(Player player, double reachDistance) {
        Vec3 startVec = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endVec = startVec.add(lookVec.scale(reachDistance));
        AABB boundingBox = new AABB(startVec, endVec);

        return player.level().getEntitiesOfClass(LivingEntity.class, boundingBox, entity -> entity != player && entity.isAlive())
                .stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }
    private static List<LivingEntity> findTargetsInArea(Player player, double radius) {
        Vec3 position = player.position();
        AABB boundingBox = new AABB(position.subtract(radius, radius, radius), position.add(radius, radius, radius));

        return player.level().getEntitiesOfClass(LivingEntity.class, boundingBox, entity -> entity != player && entity.isAlive());
    }

    private static LivingEntity findTargetWithinReach(Player player) {
        AABB boundingBox = player.getBoundingBox().inflate(REACH_DISTANCE, REACH_DISTANCE, REACH_DISTANCE);
        return player.level().getEntitiesOfClass(LivingEntity.class, boundingBox, entity -> entity != player && entity.isAlive())
                .stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }

    private static List<LivingEntity> findTargetsWithinReach(Player player) {
        AABB boundingBox = player.getBoundingBox().inflate(REACH_DISTANCE, REACH_DISTANCE, REACH_DISTANCE);
        return player.level().getEntitiesOfClass(LivingEntity.class, boundingBox, entity -> entity != player && entity.isAlive());
    }

    private static void damageGunAndAttachments(ItemStack stack, Player player) {
        Level level = player.level();
        GunEventBus.damageGun(stack, level, player);
        GunEventBus.damageAttachments(stack, level, player);
    }
}


