package top.ribs.scguns.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.init.ModDamageTypes;

import java.util.List;

import static top.ribs.scguns.entity.projectile.MicroJetEntity.EXPLOSION_DAMAGE_MULTIPLIER;

public class PlasmaExplosion extends CustomExplosion {

    private final RandomSource random = RandomSource.create();

    public PlasmaExplosion(Level pLevel, @Nullable Entity pSource, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, boolean pFire, CustomBlockInteraction customBlockInteraction) {
        super(pLevel, pSource, pToBlowX, pToBlowY, pToBlowZ, pRadius, pFire, customBlockInteraction);
    }

    @Override
    public void explode() {
        super.explode();

        if (this.level.isClientSide) {
            for (int i = 0; i < 100; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;
                this.level.addParticle(ParticleTypes.ELECTRIC_SPARK, this.x + offsetX, this.y + offsetY, this.z + offsetZ, 0.0, 0.0, 0.0);
            }
        }
    }
}

