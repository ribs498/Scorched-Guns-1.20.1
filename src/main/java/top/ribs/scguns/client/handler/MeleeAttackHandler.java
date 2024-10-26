package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.GunEventBus;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.item.BayonetItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageMeleeAttack;
import top.ribs.scguns.util.GunModifierHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
public class MeleeAttackHandler {
    private static final double REACH_DISTANCE = 3.0f;
    private static final float ENCHANTMENT_DAMAGE_SCALING_FACTOR = 0.70f;
    private static final float BASE_SPEED_DAMAGE_SCALING_FACTOR = 2.0f;
    private static final float[] BANZAI_SCALING_FACTORS = {5.0f, 7.5f, 10.0f};

    private static final String MELEE_COOLDOWN_TAG = "MeleeCooldown";
    private static boolean isBanzai = false;

    public static boolean isBanzaiActive() {
        return isBanzai;
    }
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
        PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageMeleeAttack(heldItem));
        LivingEntity target = findTargetWithinReach(player);
        if (target != null && target != player) {
            performMeleeAttackOnTarget(player, target, false);
            damageGunAndAttachments(heldItem, player);
        }
        setMeleeCooldown(player, heldItem, gunItem);
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
        float additionalDamage = GunModifierHelper.getAdditionalDamage(heldItem, true); // Pass true for melee attacks

        float enchantmentDamage = getEnchantmentDamageFromBayonet(heldItem, target, gunItem);

        // Use melee damage from the gun
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
            List<LivingEntity> targets = findTargetsInArea(player, 3.0);
            for (LivingEntity aoeTarget : targets) {
                if (aoeTarget.hurt(damageSource, attackDamage)) {
                    applyKnockback(player, aoeTarget, heldItem);
                    applySpecialEnchantmentsFromBayonet(heldItem, aoeTarget, player, gunItem);
                    triggerBanzaiImpactIfNecessary(heldItem);
                    if (player.level().isClientSide) {
                        ClientMeleeAttackHandler.spawnHitParticles((ClientLevel) player.level(), aoeTarget);
                    }
                }
            }
        } else {
            LivingEntity raycastTarget = raycastForMeleeAttack(player, 2.5); // Example reach distance
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
        if (!isBanzai) {
            return;
        }
        ItemStack currentHeldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!ItemStack.matches(currentHeldItem, banzaiActiveItem)) {
            stopBanzai();
            return;
        }
        if (!player.isSprinting()) {
            stopBanzai();
            return;
        }

        List<LivingEntity> targets = findTargetsWithinReach(player);
        for (LivingEntity target : targets) {
            if (target != player) {
                performMeleeAttackOnTarget(player, target, true);
            }
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


