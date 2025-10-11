package top.ribs.scguns.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.CogMaceItem;
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class CogMaceEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (attacker.getMainHandItem().getItem() instanceof CogMaceItem) {
                LivingEntity target = event.getEntity();

                double movementSpeed = attacker.getAttributeValue(Attributes.MOVEMENT_SPEED);
                double speedBonus = Math.max(0, movementSpeed - 0.1) * 30.0;

                if (attacker.isSprinting()) {
                    speedBonus *= 2.0;
                }
                double fallBonus = attacker.fallDistance * 1.0;

                float totalBonus = (float)(speedBonus + fallBonus);

                if (totalBonus > 0.1F) {
                    if (target.isBlocking()) {
                        totalBonus *= 0.5F;
                    }
                    event.setAmount(event.getAmount() + totalBonus);
                }
            }
        }
    }
}