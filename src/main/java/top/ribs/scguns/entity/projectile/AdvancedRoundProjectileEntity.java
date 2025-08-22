package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
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
    private static final float ADVANCED_SHIELD_DISABLE_CHANCE = 0.45f;
    private static final float HEADSHOT_EFFECT_DURATION_MULTIPLIER = 1.25f;

    public AdvancedRoundProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public AdvancedRoundProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        float armorBypass = 3.0F;
        float puncturingBypass = GunEnchantmentHelper.getPuncturingArmorBypass(weapon);
        armorBypass += puncturingBypass;

        this.setArmorBypassAmount(armorBypass);
    }
    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        float damage = this.getDamage();
        float newDamage = this.getCriticalDamage(this.getWeapon(), this.random, damage);
        boolean critical = damage != newDamage;
        damage = newDamage;
        ResourceLocation advantage = this.getProjectile().getAdvantage();
        damage *= advantageMultiplier(entity);

        boolean wasAlive = entity instanceof LivingEntity && entity.isAlive();
        if (headshot) {
            damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
        }

        if (entity instanceof LivingEntity livingTarget) {
            damage = GunEnchantmentHelper.getPuncturingDamageReduction(this.getWeapon(), livingTarget, damage);
            damage = applyProjectileProtection(livingTarget, damage);
            damage = calculateArmorBypassDamage(livingTarget, damage);
        }
        boolean blocked = ProjectileHelper.handleShieldHit(entity, this, damage, ADVANCED_SHIELD_DISABLE_CHANCE);
        if (!blocked) {
            DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner());

            if (!(entity.getType().is(ModTags.Entities.GHOST) && !advantage.equals(ModTags.Entities.UNDEAD.location()))) {
                entity.hurt(source, damage);

                if (entity instanceof LivingEntity livingEntity) {
                    ResourceLocation effectLocation = this.getProjectile().getImpactEffect();
                    if (effectLocation != null) {
                        float effectChance = this.getProjectile().getImpactEffectChance();
                        if (this.random.nextFloat() < effectChance) {
                            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                            if (effect != null) {
                                int duration = this.getProjectile().getImpactEffectDuration();
                                if (headshot) {
                                    duration = (int)(duration * HEADSHOT_EFFECT_DURATION_MULTIPLIER);
                                }
                                if (entity instanceof LivingEntity livingTarget) {
                                    damage = applyProjectileProtection(livingTarget, damage);
                                    damage = calculateArmorBypassDamage(livingTarget, damage);
                                }
                                livingEntity.addEffect(new MobEffectInstance(
                                        effect,
                                        duration,
                                        this.getProjectile().getImpactEffectAmplifier()
                                ));
                            }
                        }
                    }
                }
            }

            if(entity instanceof LivingEntity) {
                GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), (LivingEntity) entity);
            }
        }

        if (this.shooter instanceof Player) {
            int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter, new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }
        if (wasAlive && entity instanceof LivingEntity livingEntity && !livingEntity.isAlive()) {
            checkForDiamondSteelBonus(livingEntity, hitVec);
        }
        PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z, entity.getType()));
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        super.onHitBlock(state, pos, face, x, y, z);
    }

    @Override
    public void onExpired() {
    }
}