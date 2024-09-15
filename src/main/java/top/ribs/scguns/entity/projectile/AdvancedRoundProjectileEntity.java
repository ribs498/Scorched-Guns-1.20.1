package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBlood;
import top.ribs.scguns.network.message.S2CMessageProjectileHitEntity;
import top.ribs.scguns.util.GunEnchantmentHelper;

public class AdvancedRoundProjectileEntity extends ProjectileEntity {

    private static final int ARMOR_BYPASS_AMOUNT = 2;
    private static final float ADVANCED_SHIELD_DISABLE_CHANCE = 0.45f; // 45% chance, increased from the default 30%

    public AdvancedRoundProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public AdvancedRoundProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
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

        boolean blocked = ProjectileHelper.handleShieldHit(entity, this, damage, ADVANCED_SHIELD_DISABLE_CHANCE);

        if (!blocked) {
            DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner());

            if (!(entity.getType().is(ModTags.Entities.GHOST) && !advantage.equals(ModTags.Entities.UNDEAD.location()))) {
                entity.hurt(source, damage);
            }

            if(entity instanceof LivingEntity) {
                GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), (LivingEntity) entity);
            }
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
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        super.onHitBlock(state, pos, face, x, y, z);
    }

    @Override
    public void onExpired() {
    }
}
