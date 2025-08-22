package top.ribs.scguns.event;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.effect.SulfurPoisoningEffect;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class SulfurPoisoningEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (SulfurPoisoningEffect.hasFireVulnerability(entity)) {
            if (event.getSource().is(DamageTypes.IN_FIRE) ||
                    event.getSource().is(DamageTypes.ON_FIRE) ||
                    event.getSource().is(DamageTypes.LAVA) ||
                    event.getSource().is(DamageTypes.HOT_FLOOR)) {

                float multiplier = SulfurPoisoningEffect.getFireDamageMultiplier(entity);
                event.setAmount(event.getAmount() * multiplier);
            }
        }
    }
}