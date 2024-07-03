package top.ribs.scguns.client.handler;

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
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.GunEventBus;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.item.BayonetItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.util.GunModifierHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
public class MeleeAttackHandler {
    private static final double REACH_DISTANCE = 2.5f;
    private static final int COOLDOWN_TICKS = 15;
    private static final float ENCHANTMENT_DAMAGE_SCALING_FACTOR = 0.70f;
    private static final float BASE_SPEED_DAMAGE_SCALING_FACTOR = 2.0f;
    private static final float[] BANZAI_SCALING_FACTORS = {10.0f, 15.0f, 25.0f};
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

        startMeleeAnimation(gunItem, heldItem);

        LivingEntity target = findTargetWithinReach(player);
        if (target != null && target != player) {
            performMeleeAttackOnTarget(player, target, false);
            setMeleeCooldown(player, heldItem);
            damageGunAndAttachments(heldItem, player);
        }
    }

    private static boolean isMeleeOnCooldown(ServerPlayer player, ItemStack heldItem) {
        CompoundTag tag = heldItem.getOrCreateTag();
        long currentTime = player.level().getGameTime();
        return tag.contains(MELEE_COOLDOWN_TAG) && currentTime < tag.getLong(MELEE_COOLDOWN_TAG);
    }

    private static void startMeleeAnimation(GunItem gunItem, ItemStack heldItem) {
//        if (gunItem.hasBayonet(heldItem)) {
//            GunRenderingHandler.get().startBayonetStabAnimation();
//        } else {
//            GunRenderingHandler.get().startMeleeAnimation();
//        }
//        GunRenderingHandler.get().startThirdPersonMeleeAnimation();
    }
    private static void performMeleeAttackOnTarget(ServerPlayer player, LivingEntity target, boolean isBanzaiAttack) {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }

        player.swing(InteractionHand.MAIN_HAND);

        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float additionalDamage = GunModifierHelper.getAdditionalDamage(heldItem);
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
            logPlayerSpeed(player, speedDamageMultiplier, baseDamage + additionalDamage + enchantmentDamage + meleeDamage, attackDamage, heldItem);
        }
        DamageSource damageSource = player.serverLevel().damageSources().playerAttack(player);
        if (target.hurt(damageSource, attackDamage)) {
            applyKnockback(player, target, heldItem);
            applySpecialEnchantmentsFromBayonet(heldItem, target, player, gunItem);
            triggerBanzaiImpactIfNecessary(heldItem);
            if (player.level().isClientSide) {
                ClientLevel clientLevel = (ClientLevel) player.level();
                spawnHitParticles(clientLevel, target);
            }
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
    private static void spawnHitParticles(ClientLevel clientLevel, LivingEntity target) {
        clientLevel.addParticle(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY(), target.getZ(), 0.1D, 0.1D, 0.1D);
    }

    private static void logPlayerSpeed(ServerPlayer player, float speedDamageMultiplier, float baseDamage, float totalDamage, ItemStack heldItem) {
        double speed = player.getDeltaMovement().length();
        int banzaiLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BANZAI.get(), heldItem);
        //System.out.printf("Player speed: %.3f, Base damage: %.3f, Speed multiplier: %.3f, Total damage: %.3f, Banzai level: %d%n", speed, baseDamage, speedDamageMultiplier, totalDamage, banzaiLevel);
    }

    private static void triggerBanzaiImpactIfNecessary(ItemStack heldItem) {
        if (((GunItem) heldItem.getItem()).hasBayonet(heldItem)) {
            GunRenderingHandler.get().triggerBanzaiImpact();
        }
    }

    private static void setMeleeCooldown(ServerPlayer player, ItemStack heldItem) {
        CompoundTag tag = heldItem.getOrCreateTag();
        long currentTime = player.level().getGameTime();
        tag.putLong(MELEE_COOLDOWN_TAG, currentTime + COOLDOWN_TICKS);
        heldItem.setTag(tag);
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
       // System.out.printf("Banzai Level: %d, Scaling Factor: %.2f%n", banzaiLevel, scalingFactor);
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
            spawnParticleEffect(player, target, ParticleTypes.FLAME);
        } else if (enchantment == Enchantments.KNOCKBACK) {
            Vec3 direction = target.position().subtract(player.position()).normalize();
            target.knockback(level * 0.5F, -direction.x(), -direction.z());
        } else if (enchantment == Enchantments.SMITE) {
            spawnParticleEffect(player, target, ParticleTypes.ENCHANTED_HIT);
        } else if (enchantment == Enchantments.BANE_OF_ARTHROPODS) {
            spawnParticleEffect(player, target, ParticleTypes.ENCHANTED_HIT);
        } else if (enchantment == Enchantments.SHARPNESS) {
            spawnParticleEffect(player, target, ParticleTypes.ENCHANTED_HIT);
        }
    }
    private static void spawnParticleEffect(Player player, LivingEntity target, ParticleType<?> particleType) {
        if (player.level().isClientSide) {
            ClientLevel clientLevel = (ClientLevel) player.level();
            clientLevel.addParticle((ParticleOptions) particleType, target.getX(), target.getY(), target.getZ(), 0.1D, 0.1D, 0.1D);
        } else {
            ((ServerLevel) player.level()).sendParticles((SimpleParticleType) particleType, target.getX(), target.getY(), target.getZ(), 10, 0.5D, 0.5D, 0.5D, 0.0D);
        }
    }
    private static void applyKnockback(Player player, LivingEntity target, ItemStack stack) {
        int knockbackLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, stack);
        Vec3 direction = target.position().subtract(player.position()).normalize();
        target.knockback(0.4F + (knockbackLevel * 0.5F), -direction.x(), -direction.z());
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
