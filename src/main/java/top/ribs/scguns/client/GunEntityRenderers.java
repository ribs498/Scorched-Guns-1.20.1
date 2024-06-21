package top.ribs.scguns.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.*;
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
       // event.registerEntityRenderer(ModEntities.SPECTRE_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.BEARPACK_SHELL_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.GRENADE.get(), GrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.ROCKET.get(), RocketRenderer::new);
        event.registerEntityRenderer(ModEntities.MICROJET.get(), MicroJetRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_GRENADE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_STUN_GRENADE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_MOLOTOV_COCKTAIL.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_CHOKE_BOMB.get(), ThrowableGrenadeRenderer::new);
    }
}
