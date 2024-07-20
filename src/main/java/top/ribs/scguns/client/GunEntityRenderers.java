package top.ribs.scguns.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.*;
import top.ribs.scguns.entity.throwable.ThrowableSwarmBombEntity;
import top.ribs.scguns.init.ModEntities;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GunEntityRenderers
{
    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(ModEntities.PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.BEARPACK_SHELL_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.PLASMA_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.RAMROD_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.HOG_ROUND_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.BEOWULF_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.FIRE_ROUND_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.OSBORNE_SLUG_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.GRENADE.get(), GrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.ROCKET.get(), RocketRenderer::new);
        event.registerEntityRenderer(ModEntities.MICROJET.get(), MicroJetRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_CELL.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_GRENADE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_STUN_GRENADE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_MOLOTOV_COCKTAIL.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_CHOKE_BOMB.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_SWARM_BOMB.get(), ThrowableGrenadeRenderer::new);
    }
}
