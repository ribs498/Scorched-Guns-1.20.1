package top.ribs.scguns.effect;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.init.ModDamageTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static top.ribs.scguns.entity.projectile.MicroJetEntity.EXPLOSION_DAMAGE_MULTIPLIER;

public class CustomExplosion extends Explosion
{
    final CustomBlockInteraction customBlockInteraction;
    final Level level;
    private final Entity source;
    final double x;
    final double y;
    final double z;
    private final float radius;

    public CustomExplosion(Level pLevel, @Nullable Entity pSource, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, boolean pFire, CustomBlockInteraction customBlockInteraction)
    {
        super(pLevel, pSource, pToBlowX, pToBlowY, pToBlowZ, pRadius, pFire, BlockInteraction.KEEP);
        this.customBlockInteraction = customBlockInteraction;
        this.level = pLevel;
        this.source = pSource;
        this.x = pToBlowX;
        this.y = pToBlowY;
        this.z = pToBlowZ;
        this.radius = pRadius;
    }

    @Override
    public void explode()
    {
        if (this.customBlockInteraction == CustomBlockInteraction.NONE)
        {
            this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
            Set<BlockPos> set = Sets.newHashSet();
            float f2 = this.radius * 2.0F;
            int k = Mth.floor(this.x - (double)f2 - 1.0);
            int l = Mth.floor(this.x + (double)f2 + 1.0);
            int i2 = Mth.floor(this.y - (double)f2 - 1.0);
            int i1 = Mth.floor(this.y + (double)f2 + 1.0);
            int j2 = Mth.floor(this.z - (double)f2 - 1.0);
            int j1 = Mth.floor(this.z + (double)f2 + 1.0);
            List<Entity> list = this.level.getEntities(this.source, new AABB((double)k, (double)i2, (double)j2, (double)l, (double)i1, (double)j1));
            ForgeEventFactory.onExplosionDetonate(this.level, this, list, (double)f2);
            Vec3 vec3 = new Vec3(this.x, this.y, this.z);

            for (Entity entity : list)
            {
                if (!entity.ignoreExplosion())
                {
                    double d12 = Math.sqrt(entity.distanceToSqr(vec3)) / (double)f2;
                    if (d12 <= 1.0)
                    {
                        double d5 = entity.getX() - this.x;
                        double d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
                        double d9 = entity.getZ() - this.z;
                        double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                        if (d13 != 0.0)
                        {
                            d5 /= d13;
                            d7 /= d13;
                            d9 /= d13;
                            double d14 = getSeenPercent(vec3, entity);
                            double d10 = (1.0 - d12) * d14;
                            float explosionDamage = (float) ((d10 * d10 + d10) / 2.0 * 4.0 * (double)f2 + 1.0) * EXPLOSION_DAMAGE_MULTIPLIER;
                            entity.hurt(ModDamageTypes.Sources.projectile(this.level.registryAccess(), (ProjectileEntity)this.source, (LivingEntity)((ProjectileEntity)this.source).getOwner()), explosionDamage);
                            double d11;
                            if (entity instanceof LivingEntity)
                            {
                                LivingEntity livingentity = (LivingEntity)entity;
                                d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingentity, d10);
                            }
                            else
                            {
                                d11 = d10;
                            }

                            d5 *= d11;
                            d7 *= d11;
                            d9 *= d11;
                            Vec3 vec31 = new Vec3(d5, d7, d9);
                            entity.setDeltaMovement(entity.getDeltaMovement().add(vec31));
                            if (entity instanceof Player)
                            {
                                Player player = (Player)entity;
                                if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying))
                                {
                                    this.getHitPlayers().put(player, vec31);
                                }
                            }
                        }
                    }
                }
            }
        }
        else
        {
            super.explode();
        }
    }


    @Override
    public void finalizeExplosion(boolean pSpawnParticles)
    {
        if (this.customBlockInteraction != CustomBlockInteraction.NONE)
        {
            super.finalizeExplosion(pSpawnParticles);
        }
        else
        {
            this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 0.5, 0.0, 0.0);
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.0F, 1.0F, false);
        }
    }

    public enum CustomBlockInteraction
    {
        NONE,
        KEEP,
        DESTROY
    }
}

