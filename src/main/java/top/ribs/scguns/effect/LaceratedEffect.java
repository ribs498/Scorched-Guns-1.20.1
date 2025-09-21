package top.ribs.scguns.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.RandomSource;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBlood;

public class LaceratedEffect extends MobEffect {
    private static final int MIN_DAMAGE_INTERVAL = 35;
    private static final int MAX_DAMAGE_INTERVAL = 80;
    private static final float MIN_DAMAGE = 1.0f;
    private static final float MAX_DAMAGE = 4.0f;

    public LaceratedEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getType().is(ModTags.Entities.CANNOT_BE_LACERATED)) {
            return;
        }

        RandomSource random = entity.getRandom();
        int damageInterval = MIN_DAMAGE_INTERVAL + random.nextInt(MAX_DAMAGE_INTERVAL - MIN_DAMAGE_INTERVAL + 1);

        if (random.nextFloat() < (1.0f / damageInterval)) {
            float baseDamage = MIN_DAMAGE + random.nextFloat() * (MAX_DAMAGE - MIN_DAMAGE);
            float damage = baseDamage * (1.0f + amplifier * 0.3f);
            entity.hurt(entity.damageSources().magic(), damage);

            if (!entity.level().isClientSide) {
                double x = entity.getX();
                double y = entity.getY() + entity.getBbHeight() * 0.5;
                double z = entity.getZ();

                S2CMessageBlood message = new S2CMessageBlood(x, y, z, entity.getType());
                PacketHandler.getPlayChannel().sendToTracking(() -> entity, message);
            }
        }

        super.applyEffectTick(entity, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}