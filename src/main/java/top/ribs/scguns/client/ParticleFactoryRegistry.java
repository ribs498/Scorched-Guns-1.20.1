package top.ribs.scguns.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.particle.*;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.client.particle.CasingParticle;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleFactoryRegistry
{
    @SubscribeEvent
    public static void onRegisterParticleFactory(RegisterParticleProvidersEvent event)
    {

        event.registerSpecial(ModParticleTypes.BULLET_HOLE.get(), (typeIn, worldIn, x, y, z, xSpeed, ySpeed, zSpeed) -> new BulletHoleParticle(worldIn, x, y, z, typeIn.getDirection(), typeIn.getPos()));
        event.registerSpriteSet(ModParticleTypes.BLOOD.get(), BloodParticle.Factory::new);
        event.registerSpriteSet(ModParticleTypes.TRAIL.get(), TrailParticle.Factory::new);
        event.registerSpriteSet(ModParticleTypes.ROCKET_TRAIL.get(), RocketTrailParticle.Factory::new);
        event.registerSpriteSet(ModParticleTypes.SONIC_BLAST.get(), SonicBlastParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.COPPER_CASING_PARTICLE.get(), CasingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.BRASS_CASING_PARTICLE.get(), CasingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.IRON_CASING_PARTICLE.get(), CasingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.DIAMOND_STEEL_CASING_PARTICLE.get(), CasingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SHELL_PARTICLE.get(), CasingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.BEARPACK_PARTICLE.get(), CasingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.GREEN_FLAME.get(), GreenFlameParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.PLASMA_RING.get(), PlasmaRingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SULFUR_SMOKE.get(), SulfurSmokeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SULFUR_DUST.get(), SulfurDustParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.RAMROD_IMPACT.get(), RamrodImpactParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.BEOWULF_IMPACT.get(), BeowulfImpactParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.TURRET_MUZZLE_FLASH.get(), TurretMuzzleFlashParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.LASER.get(), LaserParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SMALL_LASER.get(), SmallLaserParticle.Provider::new);


        event.registerSpriteSet(ModParticleTypes.PLASMA_EXPLOSION.get(), PlasmaExplosionParticle.Provider::new);
    }
}
