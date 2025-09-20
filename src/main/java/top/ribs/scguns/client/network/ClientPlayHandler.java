package top.ribs.scguns.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import top.ribs.scguns.Config;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.client.BulletTrail;
import top.ribs.scguns.client.CustomGunManager;
import top.ribs.scguns.client.audio.GunShotSound;
import top.ribs.scguns.client.handler.BeamHandler;
import top.ribs.scguns.client.handler.BulletTrailRenderingHandler;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.handler.HUDRenderHandler;
import top.ribs.scguns.client.particle.BloodParticle;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.NetworkGunManager;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.common.exosuit.ExoSuitData;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.item.animated.ExoSuitItem;
import top.ribs.scguns.network.message.*;
import top.ribs.scguns.particles.BulletHoleData;
import top.ribs.scguns.util.GunModifierHelper;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class ClientPlayHandler {
    public static void handleSyncExoSuitUpgrades(S2CMessageSyncExoSuitUpgrades message) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            // Find the player by UUID
            Player targetPlayer = null;
            if (localPlayer.getUUID().equals(message.getPlayerId())) {
                targetPlayer = localPlayer;
            } else {
                for (Player player : localPlayer.level().players()) {
                    if (player.getUUID().equals(message.getPlayerId())) {
                        targetPlayer = player;
                        break;
                    }
                }
            }

            if (targetPlayer != null) {
                ItemStack armorPiece = targetPlayer.getItemBySlot(message.getArmorSlot());
                if (!armorPiece.isEmpty() && armorPiece.getItem() instanceof ExoSuitItem exoSuitItem) {
                    ExoSuitData.setUpgradeData(armorPiece, message.getUpgradeData());

                    if (targetPlayer == localPlayer) {
                        localPlayer.inventoryMenu.broadcastChanges();

                        ItemStack refreshedStack = armorPiece.copy();
                        targetPlayer.setItemSlot(message.getArmorSlot(), refreshedStack);
                    }
                }
            }
        }
    }
    public static void handleMessageDualWieldShotCount(S2CMessageDualWieldShotCount message) {
        GunRenderingHandler.get().updateDualWieldShotCount(message.getEntityId(), message.getShotCount());
    }
    public static void handleReloadState(boolean reloading) {
        if (Minecraft.getInstance().player != null) {
            ItemStack heldItem = Minecraft.getInstance().player.getMainHandItem();
            if (heldItem.getItem() instanceof GunItem) {
                CompoundTag tag = heldItem.getOrCreateTag();

                if (!reloading) {
                    tag.remove("IsReloading");
                    tag.remove("scguns:IsReloading");
                    tag.putBoolean("scguns:ReloadComplete", true);
                    ModSyncedDataKeys.RELOADING.setValue(Minecraft.getInstance().player, false);
                } else {
                    ModSyncedDataKeys.RELOADING.setValue(Minecraft.getInstance().player, true);
                }
            }
        }
    }
    public static void handleStopReload(S2CMessageStopReload message) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof AnimatedGunItem gunItem)) return;

        CompoundTag tag = heldItem.getOrCreateTag();
        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        boolean isManualReload = modifiedGun.getReloads().getReloadType() == ReloadType.MANUAL;
        boolean isReloading = tag.getBoolean("scguns:IsReloading") ||
                (tag.contains("scguns:ReloadState") && !tag.getString("scguns:ReloadState").equals("NONE"));

        if (!isManualReload || !isReloading) {
            return;
        }

        long id = GeoItem.getId(heldItem);
        AnimationController<GeoAnimatable> animationController = gunItem.getAnimatableInstanceCache()
                .getManagerForId(id)
                .getAnimationControllers()
                .get("controller");

        int currentAmmo = tag.getInt("AmmoCount");
        int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(heldItem, modifiedGun);
        boolean hasNoAmmo = Gun.findAmmo(player, modifiedGun.getProjectile().getItem()).stack().isEmpty();

        if (animationController != null && (currentAmmo >= maxAmmo || hasNoAmmo)) {
            tag.putString("scguns:ReloadState", "STOPPING");
            tag.putBoolean("scguns:IsPlayingReloadStop", true);
            animationController.setAnimationSpeed(1.0);
            animationController.forceAnimationReset();
            animationController.tryTriggerAnimation(
                    gunItem.isInCarbineMode(heldItem) ? "carbine_reload_stop" : "reload_stop"
            );
        }
        tag.remove("scguns:IsReloading");
        tag.remove("loaded");
        tag.remove("scguns:ReloadComplete");
    }
    public static void handleMessageGunSound(S2CMessageGunSound message) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.level == null)
            return;

        if(message.showMuzzleFlash()) {
            GunRenderingHandler.get().showMuzzleFlashForPlayer(message.getShooterId());
        }

        if(message.getShooterId() == mc.player.getId()) {
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                    message.getId(),
                    SoundSource.PLAYERS,
                    message.getVolume(),
                    message.getPitch(),
                    mc.level.getRandom(),
                    false,
                    0,
                    SoundInstance.Attenuation.NONE,
                    0, 0, 0,
                    true));
        } else {
            Minecraft.getInstance().getSoundManager().play(new GunShotSound(
                    message.getId(),
                    SoundSource.PLAYERS,
                    message.getX(),
                    message.getY(),
                    message.getZ(),
                    message.getVolume(),
                    message.getPitch(),
                    message.isReload(),
                    mc.level.getRandom()));
        }
    }
    public static void handleBeamPenetration(S2CMessageBeamPenetration message) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) return;

        BeamHandler.BeamInfo beamInfo = BeamHandler.activeBeams.get(message.getPlayerId());
        if (beamInfo == null) return;
        beamInfo.glassPenetrationPoints = message.getPenetrations().stream()
                .map(S2CMessageBeamPenetration.GlassPenetrationData::getPosition)
                .collect(Collectors.toList());
        Player player = level.getPlayerByUUID(message.getPlayerId());
        if (player == null) return;

        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof GunItem gunItem)) return;

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if (modifiedGun == null) return;

        float[] interpolatedColor = BeamHandler.getBeamColorForWeapon(heldItem, modifiedGun);
        for (S2CMessageBeamPenetration.GlassPenetrationData penetration : message.getPenetrations()) {
            Vec3 pos = penetration.getPosition();
            for (int i = 0; i < 1; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 0.1;
                double offsetY = (level.random.nextDouble() - 0.5) * 0.1;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.1;
                level.addParticle(ModParticleTypes.BLOOD.get(), false,
                        pos.x + offsetX,
                        pos.y + offsetY,
                        pos.z + offsetZ,
                        interpolatedColor[0],
                        interpolatedColor[1],
                        interpolatedColor[2]);
            }
        }
    }
    public static void handleMessageBlood(S2CMessageBlood message)
    {
        if (!Config.CLIENT.particle.enableBlood.get())
        {
            return;
        }
        Level world = Minecraft.getInstance().level;
        if (world != null)
        {
            EntityType<?> entityType = message.getEntityType();
            for (int i = 0; i < 10; i++)
            {
                Particle particle = Minecraft.getInstance().particleEngine.createParticle(ModParticleTypes.BLOOD.get(), message.getX(), message.getY(), message.getZ(), 0.5, 0, 0.5);
                if (particle instanceof BloodParticle)
                {
                    ((BloodParticle) particle).setColorBasedOnEntity(entityType);
                }
            }
        }
    }
    public static void handleBeamUpdate(S2CMessageBeamUpdate message) {
        UUID playerId = message.getPlayerId();
        Vec3 startPos = message.getStartPos();
        Vec3 endPos = message.getEndPos();

        BeamHandler.updateBeam(playerId, startPos, endPos);
    }

    public static void handleMessageBulletTrail(S2CMessageBulletTrail message)
    {
        Level world = Minecraft.getInstance().level;
        if(world != null)
        {
            int[] entityIds = message.getEntityIds();
            Vec3[] positions = message.getPositions();
            Vec3[] motions = message.getMotions();
            ItemStack item = message.getItem();
            int trailColor = message.getTrailColor();
            double trailLengthMultiplier = message.getTrailLengthMultiplier();
            int life = message.getLife();
            double gravity = message.getGravity();
            int shooterId = message.getShooterId();
            boolean enchanted = message.isEnchanted();
            ParticleOptions data = message.getParticleData();
            for(int i = 0; i < message.getCount(); i++)
            {
                BulletTrailRenderingHandler.get().add(new BulletTrail(entityIds[i], positions[i], motions[i], item, trailColor, trailLengthMultiplier, life, gravity, shooterId, enchanted, data));
            }
        }
    }

    public static void handleExplosionStunGrenade(S2CMessageStunGrenade message)
    {
        Minecraft mc = Minecraft.getInstance();
        ParticleEngine particleManager = mc.particleEngine;
        Level world = Objects.requireNonNull(mc.level);
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();

        /* Spawn lingering smoke particles */
        for(int i = 0; i < 30; i++)
        {
            spawnParticle(particleManager, ParticleTypes.CLOUD, x, y, z, world.random, 0.2);
        }

        /* Spawn fast moving smoke/spark particles */
        for(int i = 0; i < 30; i++)
        {
            Particle smoke = spawnParticle(particleManager, ParticleTypes.SMOKE, x, y, z, world.random, 4.0);
            smoke.setLifetime((int) ((8 / (Math.random() * 0.1 + 0.4)) * 0.5));
            spawnParticle(particleManager, ParticleTypes.CRIT, x, y, z, world.random, 4.0);
        }
    }

    private static Particle spawnParticle(ParticleEngine manager, ParticleOptions data, double x, double y, double z, RandomSource rand, double velocityMultiplier)
    {
        return manager.createParticle(data, x, y, z, (rand.nextDouble() - 0.5) * velocityMultiplier, (rand.nextDouble() - 0.5) * velocityMultiplier, (rand.nextDouble() - 0.5) * velocityMultiplier);
    }

    public static void handleProjectileHitBlock(S2CMessageProjectileHitBlock message)
    {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;
        if(world != null)
        {
            BlockState state = world.getBlockState(message.getPos());
            double holeX = message.getX() + 0.005 * message.getFace().getStepX();
            double holeY = message.getY() + 0.005 * message.getFace().getStepY();
            double holeZ = message.getZ() + 0.005 * message.getFace().getStepZ();
            double distance = Math.sqrt(mc.player.distanceToSqr(message.getX(), message.getY(), message.getZ()));
            world.addParticle(new BulletHoleData(message.getFace(), message.getPos()), false, holeX, holeY, holeZ, 0, 0, 0);
            if(distance < Config.CLIENT.particle.impactParticleDistance.get())
            {
                for(int i = 0; i < 4; i++)
                {
                    Vec3i normal = message.getFace().getNormal();
                    Vec3 motion = new Vec3(normal.getX(), normal.getY(), normal.getZ());
                    motion.add(getRandomDir(world.random), getRandomDir(world.random), getRandomDir(world.random));
                    world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), false, message.getX(), message.getY(), message.getZ(), motion.x, motion.y, motion.z);
                }
            }
            if(distance <= Config.CLIENT.sounds.impactSoundDistance.get())
            {
                world.playLocalSound(message.getX(), message.getY(), message.getZ(), state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 2.0F, 2.0F, false);
            }
        }
    }

    private static double getRandomDir(RandomSource random)
    {
        return -0.25 + random.nextDouble() * 0.5;
    }

    public static void handleProjectileHitEntity(S2CMessageProjectileHitEntity message)
    {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;
        if(world == null)
            return;

        HUDRenderHandler.playHitMarker(message.isCritical() || message.isHeadshot());
        SoundEvent event = getHitSound(message.isCritical(), message.isHeadshot(), message.isPlayer());
        if(event == null)
            return;

        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0F, 1.0F + world.random.nextFloat() * 0.2F));
    }


    @Nullable
    private static SoundEvent getHitSound(boolean critical, boolean headshot, boolean player)
    {
        if(critical)
        {
            if(Config.CLIENT.sounds.playSoundWhenCritical.get())
            {
                SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(Config.CLIENT.sounds.criticalSound.get()));
                return event != null ? event : SoundEvents.PLAYER_ATTACK_CRIT;
            }
        }
        else if(headshot)
        {
            if(Config.CLIENT.sounds.playSoundWhenHeadshot.get())
            {
                SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(Config.CLIENT.sounds.headshotSound.get()));
                return event != null ? event : SoundEvents.PLAYER_ATTACK_KNOCKBACK;
            }
        }
        else if(player)
        {
            return SoundEvents.PLAYER_HURT;
        }
        return null;
    }


    public static void handleRemoveProjectile(S2CMessageRemoveProjectile message)
    {
        BulletTrailRenderingHandler.get().remove(message.getEntityId());
    }

    public static void handleUpdateGuns(S2CMessageUpdateGuns message)
    {
        NetworkGunManager.updateRegisteredGuns(message);
        CustomGunManager.updateCustomGuns(message);
    }

    public static void handleStopBeam(S2CMessageStopBeam message) {
        BeamHandler.stopBeam(message.getPlayerId());
    }
}


