package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.BayonetItem;
import top.ribs.scguns.item.GunItem;

public class C2SMessageOffhandMelee extends PlayMessage<C2SMessageOffhandMelee> {
    private int targetId;
    private float x, y, z;

    private static final float OFFHAND_DAMAGE_MULTIPLIER = 0.75f;
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

        float damage = baseDamage * (0.2F + attackStrength * attackStrength * 0.8F);

        boolean isCritical = player.fallDistance > 0.0F && !player.onGround()  &&
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
                livingTarget.knockback(knockback,
                        Math.sin(player.getYRot() * Math.PI / 180.0),
                        -Math.cos(player.getYRot() * Math.PI / 180.0));
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

    private float getWeaponAttackSpeed(ItemStack weapon) {
        // Get attack speed from attributes
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