package top.ribs.scguns.attributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.entity.EntityType;
import top.ribs.scguns.ScorchedGuns;

@Mod.EventBusSubscriber(modid = ScorchedGuns.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SCEntityAttributes {
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, SCAttributes.PROJECTILE_SPEED.get());
        event.add(EntityType.PLAYER, SCAttributes.RELOAD_SPEED.get());
        event.add(EntityType.PLAYER, SCAttributes.ADDITIONAL_BULLET_DAMAGE.get());
        event.add(EntityType.PLAYER, SCAttributes.BULLET_DAMAGE_MULTIPLIER.get());
        event.add(EntityType.PLAYER, SCAttributes.SPREAD_MULTIPLIER.get());
        //Disabled
        //event.add(EntityType.PLAYER, SCAttributes.BULLET_RESISTANCE.get());
        //event.add(EntityType.PLAYER, SCAttributes.FIRE_RATE_MULTIPLIER.get());
    }
}