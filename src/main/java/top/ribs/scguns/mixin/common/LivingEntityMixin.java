package top.ribs.scguns.mixin.common;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.entity.projectile.ProjectileEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Unique
    private DamageSource scorched_Guns_1_20_1$currentDamageSource;

    @Unique
    private static final Map<UUID, Long> scorched_Guns_1_20_1$knockbackCooldowns = new HashMap<>();

    @Unique
    private static final int KNOCKBACK_COOLDOWN_TICKS = 10; // 0.5 seconds

    @Inject(method = "hurt", at = @At("HEAD"))
    private void onHurtStart(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.scorched_Guns_1_20_1$currentDamageSource = source;
    }

    @ModifyArg(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"), index = 0)
    private double modifyApplyKnockbackArgs(double original) {
        if (this.scorched_Guns_1_20_1$currentDamageSource != null &&
                this.scorched_Guns_1_20_1$currentDamageSource.getDirectEntity() instanceof ProjectileEntity projectile) {

            LivingEntity entity = (LivingEntity)(Object)this;
            UUID entityId = entity.getUUID();
            long currentTick = entity.level().getGameTime();

            Long lastKnockbackTick = scorched_Guns_1_20_1$knockbackCooldowns.get(entityId);
            if (lastKnockbackTick != null && (currentTick - lastKnockbackTick) < KNOCKBACK_COOLDOWN_TICKS) {
                return 0.0D;
            }
            scorched_Guns_1_20_1$knockbackCooldowns.put(entityId, currentTick);
            scorched_Guns_1_20_1$knockbackCooldowns.entrySet().removeIf(
                    entry -> (currentTick - entry.getValue()) > 100
            );

            return projectile.getModifiedKnockback();
        }
        return original;
    }
}
