package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.GunItem;

public class ClientMeleeAttackHandler {
    public static void startMeleeAnimation(GunItem gunItem, ItemStack heldItem) {
        if (Minecraft.getInstance().player != null) {
            boolean isOnCooldown = MeleeAttackHandler.isMeleeOnCooldown(Minecraft.getInstance().player, heldItem);
            if (isOnCooldown) {
                return;
            }
            GunRenderingHandler.get().startMeleeAnimation(heldItem);

            if (gunItem.hasBayonet(heldItem)) {
                GunRenderingHandler.get().startBayonetStabAnimation();
            }

            GunRenderingHandler.get().startThirdPersonMeleeAnimation();
            updateMeleeCooldownHUD(gunItem, heldItem);
        }
    }


    public static void updateMeleeCooldownHUD(GunItem gunItem, ItemStack heldItem) {
        HUDRenderHandler.isMeleeCooldownActive = true;
        HUDRenderHandler.maxMeleeCooldown = gunItem.getModifiedGun(heldItem).getGeneral().getMeleeCooldownTicks();
        HUDRenderHandler.meleeCooldown = HUDRenderHandler.maxMeleeCooldown;
    }

    public static void spawnHitParticles(ClientLevel clientLevel, LivingEntity target) {
        clientLevel.addParticle(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY(), target.getZ(), 0.1D, 0.1D, 0.1D);
    }

    public static void spawnParticleEffect(Player player, LivingEntity target, ParticleType<?> particleType) {
        if (player.level().isClientSide) {
            ClientLevel clientLevel = (ClientLevel) player.level();
            clientLevel.addParticle((ParticleOptions) particleType, target.getX(), target.getY(), target.getZ(), 0.1D, 0.1D, 0.1D);
        } else {
            ((ServerLevel) player.level()).sendParticles((SimpleParticleType) particleType, target.getX(), target.getY(), target.getZ(), 10, 0.5D, 0.5D, 0.5D, 0.0D);
        }
    }
}
