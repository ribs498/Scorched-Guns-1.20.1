package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantments;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.BayonetItem;
import top.ribs.scguns.item.GunItem;

public class C2SMessageOffhandMelee extends PlayMessage<C2SMessageOffhandMelee> {
    private int targetId;
    private float x, y, z;

    private static final float OFFHAND_DAMAGE_MULTIPLIER = 0.65f;
    private static final float OFFHAND_COOLDOWN_MULTIPLIER = 1.25f;

    public C2SMessageOffhandMelee() {}

    public C2SMessageOffhandMelee(int targetId, float x, float y, float z) {
        this.targetId = targetId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void encode(C2SMessageOffhandMelee message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.targetId);
        buffer.writeFloat(message.x);
        buffer.writeFloat(message.y);
        buffer.writeFloat(message.z);
    }

    @Override
    public C2SMessageOffhandMelee decode(FriendlyByteBuf buffer) {
        return new C2SMessageOffhandMelee(
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat()
        );
    }

    public void handle(C2SMessageOffhandMelee message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if (player == null || player.isSpectator()) return;

            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            if (!(mainHand.getItem() instanceof GunItem gunItem)) return;

            Gun gun = gunItem.getModifiedGun(mainHand);
            GripType gripType = gun.getGeneral().getGripType(mainHand);

            if (gripType != GripType.ONE_HANDED) return;

            if (!(offHand.getItem() instanceof SwordItem) && !(offHand.getItem() instanceof BayonetItem)) {
                return;
            }

            float attackStrength = player.getAttackStrengthScale(0.5F);
            if (attackStrength < 0.95f) {
                return;
            }

            player.swing(InteractionHand.OFF_HAND, true);

            float attackSpeed = getWeaponAttackSpeed(offHand);

            player.resetAttackStrengthTicker();

            float cooldownTicks = (20.0f / attackSpeed) * OFFHAND_COOLDOWN_MULTIPLIER;
            player.getCooldowns().addCooldown(offHand.getItem(), (int)cooldownTicks);

            if (message.targetId == -1) {
                return;
            }

            Entity target = player.level().getEntity(message.targetId);
            if (target == null) return;

            double distance = player.distanceTo(target);
            if (distance > 6.0) return;

            performOffhandMeleeAttack(player, target, offHand, attackStrength);
        });
        context.setHandled(true);
    }

    private void performOffhandMeleeAttack(ServerPlayer player, Entity target, ItemStack weapon, float attackStrength) {
        float baseDamage = getWeaponDamage(weapon) * OFFHAND_DAMAGE_MULTIPLIER;
        if (target instanceof net.minecraft.world.entity.LivingEntity livingTarget) {
            baseDamage += net.minecraft.world.item.enchantment.EnchantmentHelper.getDamageBonus(weapon, livingTarget.getMobType());

            int corrodedLevel = weapon.getEnchantmentLevel(top.ribs.scguns.init.ModEnchantments.CORRODED.get());
            if (corrodedLevel > 0 && isBotEntity(livingTarget)) {
                baseDamage += top.ribs.scguns.enchantment.CorrodedEnchantment.getBotDamageBonus(corrodedLevel);
            }
        }

        float damage = baseDamage * (0.2F + attackStrength * attackStrength * 0.8F);

        boolean isCritical = player.fallDistance > 0.0F && !player.onGround() &&
                !player.isInWater() && !player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS) &&
                !player.isPassenger();

        if (isCritical) {
            damage *= 1.3F;
        }

        var damageSource = player.damageSources().playerAttack(player);
        boolean damaged = target.hurt(damageSource, damage);

        if (damaged) {
            if (target instanceof net.minecraft.world.entity.LivingEntity livingTarget) {
                float knockback = 0.25F;
                int knockbackLevel = net.minecraft.world.item.enchantment.EnchantmentHelper.getKnockbackBonus(player);
                if (knockbackLevel > 0) {
                    knockback += knockbackLevel * 0.4F;
                }

                livingTarget.knockback(knockback,
                        Math.sin(player.getYRot() * Math.PI / 180.0),
                        -Math.cos(player.getYRot() * Math.PI / 180.0));

                int fireAspectLevel = net.minecraft.world.item.enchantment.EnchantmentHelper.getFireAspect(player);
                if (fireAspectLevel > 0) {
                    livingTarget.setSecondsOnFire(fireAspectLevel * 4);
                }

                float sweepingLevel = net.minecraft.world.item.enchantment.EnchantmentHelper.getSweepingDamageRatio(player);
                if (sweepingLevel > 0 && weapon.getItem() instanceof net.minecraft.world.item.SwordItem) {
                    applySweepingDamage(player, target, weapon, sweepingLevel, damage * 0.25f);
                }

                int corrodedLevel = weapon.getEnchantmentLevel(top.ribs.scguns.init.ModEnchantments.CORRODED.get());
                if (corrodedLevel > 0) {
                    applyCorrodedEffects(player, livingTarget, weapon, corrodedLevel);
                }

                spawnEnchantmentParticles(player, weapon, livingTarget);
            }

            if (weapon.isDamageableItem()) {
                weapon.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(InteractionHand.OFF_HAND));
            }

            net.minecraft.sounds.SoundEvent soundEvent = isCritical ?
                    net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT :
                    net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_STRONG;

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    soundEvent, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.1F);

            if (target instanceof net.minecraft.world.entity.LivingEntity) {
                ((net.minecraft.server.level.ServerLevel)player.level()).sendParticles(
                        net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        1, 0.0, 0.0, 0.0, 0.0);
            }

            if (isCritical && target instanceof net.minecraft.world.entity.LivingEntity) {
                ((net.minecraft.server.level.ServerLevel)player.level()).sendParticles(
                        net.minecraft.core.particles.ParticleTypes.CRIT,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        6, 0.15, 0.15, 0.15, 0.0);
            }
        }
    }
    private void applyCorrodedEffects(ServerPlayer player, net.minecraft.world.entity.LivingEntity target, ItemStack weapon, int corrodedLevel) {
        if (isBotEntity(target)) {
            spawnCorrodedParticles(target, corrodedLevel);
        } else {
            if (player.level().getRandom().nextFloat() < 0.30F) {
                int poisonDuration = 60 + (corrodedLevel * 20);
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.POISON, poisonDuration, 0));
            }
        }
    }

    private void spawnCorrodedParticles(net.minecraft.world.entity.LivingEntity entity, int level) {
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            java.util.Random random = new java.util.Random();
            for (int i = 0; i < level * 5; i++) {
                double offsetX = (random.nextDouble() - 0.5) * entity.getBbWidth();
                double offsetY = random.nextDouble() * entity.getBbHeight();
                double offsetZ = (random.nextDouble() - 0.5) * entity.getBbWidth();
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        entity.getX() + offsetX,
                        entity.getY() + offsetY,
                        entity.getZ() + offsetZ,
                        1,
                        0, 0, 0,
                        0.1);
            }
        }
    }

    private boolean isBotEntity(net.minecraft.world.entity.LivingEntity entity) {
        return entity.getType().is(top.ribs.scguns.init.ModTags.Entities.BOT);
    }

    private void spawnEnchantmentParticles(ServerPlayer player, ItemStack weapon, net.minecraft.world.entity.LivingEntity target) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        int sharpnessLevel = weapon.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS);
        int smiteLevel = weapon.getEnchantmentLevel(Enchantments.SMITE);
        int baneLevel = weapon.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.BANE_OF_ARTHROPODS);

        boolean hasRelevantEnchantment = false;

        if (smiteLevel > 0 && target.getMobType() == net.minecraft.world.entity.MobType.UNDEAD) {
            hasRelevantEnchantment = true;
        }
        else if (baneLevel > 0 && target.getMobType() == net.minecraft.world.entity.MobType.ARTHROPOD) {
            hasRelevantEnchantment = true;
        }
        else if (sharpnessLevel > 0) {
            hasRelevantEnchantment = true;
        }
        if (hasRelevantEnchantment) {
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5,
                    target.getZ(),
                    8,
                    target.getBbWidth() * 0.5,
                    target.getBbHeight() * 0.25,
                    target.getBbWidth() * 0.5,
                    0.02 // speed
            );
        }
        int fireAspectLevel = weapon.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT);
        if (fireAspectLevel > 0) {
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5,
                    target.getZ(),
                    fireAspectLevel * 2,
                    target.getBbWidth() * 0.3,
                    target.getBbHeight() * 0.2,
                    target.getBbWidth() * 0.3,
                    0.05
            );
        }
    }
    private void applySweepingDamage(ServerPlayer player, Entity target, ItemStack weapon, float sweepingLevel, float sweepDamage) {
        double range = 1.0D + sweepingLevel;

        for (net.minecraft.world.entity.LivingEntity entity : player.level().getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class,
                target.getBoundingBox().inflate(range, 0.25D, range))) {

            if (entity != player && entity != target && !player.isAlliedTo(entity) &&
                    (!(entity instanceof net.minecraft.world.entity.decoration.ArmorStand armorStand) || !armorStand.isMarker()) &&
                    player.distanceToSqr(entity) < range * range) {

                entity.knockback(0.4F,
                        Math.sin(player.getYRot() * Math.PI / 180.0),
                        -Math.cos(player.getYRot() * Math.PI / 180.0));
                entity.hurt(player.damageSources().playerAttack(player), sweepDamage);
            }
        }
    }

    private float getWeaponAttackSpeed(ItemStack weapon) {
        var speedModifiers = weapon.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND)
                .get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);

        float attackSpeed = 4.0F;
        for (var modifier : speedModifiers) {
            attackSpeed += (float) modifier.getAmount();
        }

        return Math.max(0.1F, attackSpeed);
    }

    private float getWeaponDamage(ItemStack weapon) {
        var damageModifiers = weapon.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND)
                .get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);

        float damage = 1.0F;
        for (var modifier : damageModifiers) {
            damage += (float) modifier.getAmount();
        }

        return damage;
    }
}