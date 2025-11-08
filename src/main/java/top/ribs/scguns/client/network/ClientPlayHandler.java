package top.ribs.scguns.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import top.ribs.scguns.Config;
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
import top.ribs.scguns.common.exosuit.ExoSuitData;
import top.ribs.scguns.common.exosuit.ExoSuitUpgradeManager;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.item.animated.ExoSuitItem;
import top.ribs.scguns.network.message.*;
import top.ribs.scguns.particles.BulletHoleData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class ClientPlayHandler {

    public static void handleEntityCasingEject(S2CMessageEntityCasingEject message) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        Entity entity = level.getEntity(message.getEntityId());
        if (!(entity instanceof LivingEntity livingEntity)) return;

        Vec3 lookVec = livingEntity.getLookAngle();
        Vec3 rightVec = new Vec3(-lookVec.z, 0, lookVec.x).normalize();
        Vec3 forwardVec = new Vec3(lookVec.x, 0, lookVec.z).normalize();

        double offsetX = rightVec.x * 0.5 + forwardVec.x * 0.5;
        double offsetY = livingEntity.getEyeHeight() - 0.4;
        double offsetZ = rightVec.z * 0.5 + forwardVec.z * 0.5;

        Vec3 particlePos = livingEntity.getPosition(1).add(offsetX, offsetY, offsetZ);

        ParticleType<?> particleType = ForgeRegistries.PARTICLE_TYPES.getValue(message.getParticleLocation());
        if (particleType instanceof SimpleParticleType simpleParticleType) {
            level.addParticle(simpleParticleType,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0);
        }
    }


    public static void handleRaidFlareBurst(S2CMessageRaidFlareBurst message) {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;
        if (world == null) return;

        double centerX = message.getX();
        double centerY = message.getY();
        double centerZ = message.getZ();
        String patternType = message.getPatternType();
        double scale = message.getScale();
        int repetitions = message.getRepetitions();

        switch (patternType) {
            case "star" -> spawnStarPattern(world, centerX, centerY, centerZ, scale, repetitions, message.getParticles());
            case "circle" -> spawnCirclePattern(world, centerX, centerY, centerZ, scale, repetitions, message.getParticles());
            case "union_jack" -> spawnUnionJackPattern(world, centerX, centerY, centerZ, scale, message.getParticles());
            case "spiral" -> spawnSpiralPattern(world, centerX, centerY, centerZ, scale, repetitions, message.getParticles());
            case "wave" -> spawnWavePattern(world, centerX, centerY, centerZ, scale, repetitions, message.getParticles());
            case "cross" -> spawnCrossPattern(world, centerX, centerY, centerZ, scale, message.getParticles());
            case "double_helix" -> spawnDoubleHelixPattern(world, centerX, centerY, centerZ, scale, repetitions, message.getParticles());
            case "burst" -> spawnBurstPattern(world, centerX, centerY, centerZ, scale, repetitions, message.getParticles());
            case "ring" -> spawnRingPattern(world, centerX, centerY, centerZ, scale, repetitions, message.getParticles());
            case "pig_snout" -> spawnPigSnoutPattern(world, centerX, centerY, centerZ, scale, message.getParticles());
            case "skulk_pulse" -> spawnSkulkPulsePattern(world, centerX, centerY, centerZ, scale, repetitions, message.getParticles());
            default -> spawnDefaultBurst(world, centerX, centerY, centerZ, message.getParticles());
        }
    }

    private static void spawnPigSnoutPattern(Level world, double centerX, double centerY, double centerZ,
                                             double scale, List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        int segments = 25;

        double nostrilRadius = scale * 0.25;
        double nostrilOffset = scale * 0.35;

        for (int i = 0; i < segments; i++) {
            double angle = (Math.PI * 2 * i) / segments;
            double x = centerX - nostrilOffset + Math.cos(angle) * nostrilRadius;
            double z = centerZ + Math.sin(angle) * nostrilRadius;
            spawnBurstParticlesAt(world, x, centerY, z, particleTypes);
        }

        for (int i = 0; i < segments; i++) {
            double angle = (Math.PI * 2 * i) / segments;
            double x = centerX + nostrilOffset + Math.cos(angle) * nostrilRadius;
            double z = centerZ + Math.sin(angle) * nostrilRadius;
            spawnBurstParticlesAt(world, x, centerY, z, particleTypes);
        }

        int outerSegments = 40;
        for (int i = 0; i < outerSegments; i++) {
            double angle = (Math.PI * 2 * i) / outerSegments;
            double x = centerX + Math.cos(angle) * scale;
            double z = centerZ + Math.sin(angle) * scale * 0.7;
            spawnBurstParticlesAt(world, x, centerY, z, particleTypes);
        }
    }

    private static void spawnSkulkPulsePattern(Level world, double centerX, double centerY, double centerZ,
                                               double scale, int repetitions, List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        int segments = 30;

        for (int rep = 0; rep < repetitions; rep++) {
            double pulseRadius = scale * ((rep + 1.0) / repetitions);

            for (int i = 0; i < segments; i++) {
                double angle = (Math.PI * 2 * i) / segments;
                double x = centerX + Math.cos(angle) * pulseRadius;
                double z = centerZ + Math.sin(angle) * pulseRadius;
                spawnBurstParticlesAt(world, x, centerY, z, particleTypes);
            }

            if (rep % 2 == 0) {
                int tendrils = 8;
                for (int t = 0; t < tendrils; t++) {
                    double tendrilAngle = (Math.PI * 2 * t) / tendrils + (rep * 0.3);
                    int points = 5;
                    for (int p = 0; p < points; p++) {
                        double distance = pulseRadius + (p * scale * 0.2);
                        double x = centerX + Math.cos(tendrilAngle) * distance;
                        double z = centerZ + Math.sin(tendrilAngle) * distance;
                        spawnBurstParticlesAt(world, x, centerY, z, particleTypes);
                    }
                }
            }
        }
    }

    private static void spawnSpiralPattern(Level world, double centerX, double centerY, double centerZ,
                                           double radius, int repetitions, List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        int segments = 40;
        for (int rep = 0; rep < repetitions; rep++) {
            for (int i = 0; i < segments; i++) {
                double t = (double) i / segments;
                double angle = t * Math.PI * 4;
                double r = radius * t;
                double x = centerX + Math.cos(angle) * r;
                double z = centerZ + Math.sin(angle) * r;
                spawnBurstParticlesAt(world, x, centerY, z, particleTypes);
            }
        }
    }

    private static void spawnWavePattern(Level world, double centerX, double centerY, double centerZ,
                                         double scale, int repetitions, List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        int segments = 30;
        for (int rep = 0; rep < repetitions; rep++) {
            for (int i = 0; i < segments; i++) {
                double t = (double) i / segments;
                double angle = t * Math.PI * 2;
                double waveHeight = Math.sin(angle * 3) * scale * 0.3;

                double x = centerX + (t - 0.5) * scale * 2;
                double y = centerY + waveHeight;
                double z = centerZ;
                spawnBurstParticlesAt(world, x, y, z, particleTypes);

                x = centerX;
                z = centerZ + (t - 0.5) * scale * 2;
                spawnBurstParticlesAt(world, x, y, z, particleTypes);
            }
        }
    }

    private static void spawnCrossPattern(Level world, double centerX, double centerY, double centerZ,
                                          double scale, List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        int segments = 30;

        for (int i = 0; i < segments; i++) {
            double t = (double) i / segments;
            double offset = (t - 0.5) * scale * 2;

            spawnBurstParticlesAt(world, centerX + offset, centerY, centerZ, particleTypes);
            spawnBurstParticlesAt(world, centerX, centerY, centerZ + offset, particleTypes);
        }
    }

    private static void spawnDoubleHelixPattern(Level world, double centerX, double centerY, double centerZ,
                                                double radius, int repetitions, List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        int segments = 50;
        for (int rep = 0; rep < repetitions; rep++) {
            for (int i = 0; i < segments; i++) {
                double t = (double) i / segments;
                double angle = t * Math.PI * 4;
                double height = (t - 0.5) * radius * 2;

                double x1 = centerX + Math.cos(angle) * radius * 0.5;
                double z1 = centerZ + Math.sin(angle) * radius * 0.5;
                spawnBurstParticlesAt(world, x1, centerY + height, z1, particleTypes);

                double x2 = centerX + Math.cos(angle + Math.PI) * radius * 0.5;
                double z2 = centerZ + Math.sin(angle + Math.PI) * radius * 0.5;
                spawnBurstParticlesAt(world, x2, centerY + height, z2, particleTypes);
            }
        }
    }

    private static void spawnBurstPattern(Level world, double centerX, double centerY, double centerZ,
                                          double scale, int repetitions, List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        int rays = 12;
        int pointsPerRay = 8;

        for (int rep = 0; rep < repetitions; rep++) {
            for (int ray = 0; ray < rays; ray++) {
                double angle = (Math.PI * 2 * ray) / rays;
                for (int point = 0; point < pointsPerRay; point++) {
                    double distance = (scale / pointsPerRay) * (point + 1);
                    double x = centerX + Math.cos(angle) * distance;
                    double z = centerZ + Math.sin(angle) * distance;
                    spawnBurstParticlesAt(world, x, centerY, z, particleTypes);
                }
            }
        }
    }

    private static void spawnRingPattern(Level world, double centerX, double centerY, double centerZ,
                                         double radius, int repetitions, List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        int particleCount = 40;
        for (int rep = 0; rep < repetitions; rep++) {
            double ringHeight = centerY + (rep - repetitions / 2.0) * 0.5;
            for (int i = 0; i < particleCount; i++) {
                double angle = (Math.PI * 2 * i) / particleCount;
                double x = centerX + Math.cos(angle) * radius;
                double z = centerZ + Math.sin(angle) * radius;
                spawnBurstParticlesAt(world, x, ringHeight, z, particleTypes);
            }
        }
    }
    private static void spawnStarPattern(Level world, double centerX, double centerY, double centerZ,
                                         double radius, int repetitions, List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        int points = 5;
        for (int rep = 0; rep < repetitions; rep++) {
            for (int i = 0; i < points * 2; i++) {
                double angle = (Math.PI * 2 * i) / (points * 2);
                double r = (i % 2 == 0) ? radius : radius * 0.4;
                double x = centerX + Math.cos(angle) * r;
                double z = centerZ + Math.sin(angle) * r;
                spawnBurstParticlesAt(world, x, centerY, z, particleTypes);
            }
        }
    }

    private static void spawnUnionJackPattern(Level world, double centerX, double centerY, double centerZ,
                                              double scale, List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        int segments = 30;

        for (int i = 0; i < segments; i++) {
            double t = (double) i / segments;
            double x = centerX + (t - 0.5) * scale * 2;
            double z = centerZ + (t - 0.5) * scale * 2;
            spawnBurstParticlesAt(world, x, centerY, z, particleTypes);
        }

        for (int i = 0; i < segments; i++) {
            double t = (double) i / segments;
            double x = centerX + (t - 0.5) * scale * 2;
            double z = centerZ - (t - 0.5) * scale * 2;
            spawnBurstParticlesAt(world, x, centerY, z, particleTypes);
        }

        for (int i = 0; i < segments; i++) {
            double t = (double) i / segments;
            double x = centerX + (t - 0.5) * scale * 2;
            spawnBurstParticlesAt(world, x, centerY, centerZ, particleTypes);
        }

        for (int i = 0; i < segments; i++) {
            double t = (double) i / segments;
            double z = centerZ + (t - 0.5) * scale * 2;
            spawnBurstParticlesAt(world, centerX, centerY, z, particleTypes);
        }

        for (int i = 0; i <= segments / 4; i++) {
            double t = (double) i / (segments / 4);
            double offset = t * scale * 0.3;

            spawnBurstParticlesAt(world, centerX - scale + offset, centerY, centerZ + scale - offset, particleTypes);
            spawnBurstParticlesAt(world, centerX + scale - offset, centerY, centerZ + scale - offset, particleTypes);
            spawnBurstParticlesAt(world, centerX - scale + offset, centerY, centerZ - scale + offset, particleTypes);
            spawnBurstParticlesAt(world, centerX + scale - offset, centerY, centerZ - scale + offset, particleTypes);
        }
    }

    private static void spawnCirclePattern(Level world, double centerX, double centerY, double centerZ,
                                           double radius, int repetitions, List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        int particleCount = 20;
        for (int rep = 0; rep < repetitions; rep++) {
            double repRadius = radius * (rep + 1) / repetitions;
            for (int i = 0; i < particleCount; i++) {
                double angle = (Math.PI * 2 * i) / particleCount;
                double x = centerX + Math.cos(angle) * repRadius;
                double z = centerZ + Math.sin(angle) * repRadius;
                spawnBurstParticlesAt(world, x, centerY, z, particleTypes);
            }
        }
    }

    private static void spawnDefaultBurst(Level world, double x, double y, double z,
                                          List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        spawnBurstParticlesAt(world, x, y, z, particleTypes);
    }

    private static void spawnBurstParticlesAt(Level world, double x, double y, double z,
                                              List<S2CMessageRaidFlareBurst.ParticleData> particleTypes) {
        for (S2CMessageRaidFlareBurst.ParticleData particleData : particleTypes) {
            ParticleOptions particle = getParticleWithColor(particleData.particleId, particleData.color);
            if (particle == null) continue;

            for (int i = 0; i < particleData.count; i++) {
                double offsetX = (world.random.nextDouble() - 0.5) * particleData.spread;
                double offsetY = (world.random.nextDouble() - 0.5) * particleData.spread;
                double offsetZ = (world.random.nextDouble() - 0.5) * particleData.spread;

                double velX = (world.random.nextDouble() - 0.5) * particleData.speed * 2;
                double velY = (world.random.nextDouble() - 0.5) * particleData.speed * 2;
                double velZ = (world.random.nextDouble() - 0.5) * particleData.speed * 2;

                world.addParticle(particle,
                        x + offsetX, y + offsetY, z + offsetZ,
                        velX, velY, velZ);
            }
        }
    }

    private static ParticleOptions getParticleTypeFromString(String particleId) {
        try {
            ResourceLocation location = new ResourceLocation(particleId);
            return (ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.get(location);
        } catch (Exception e) {
            return null;
        }
    }

    private static ParticleOptions getParticleWithColor(String particleId, int color) {
        if (particleId.equals("minecraft:dust")) {
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            return new DustParticleOptions(new Vector3f(r, g, b), 1.0f);
        }
        return getParticleTypeFromString(particleId);
    }
    public static void handleSyncUpgradeRegistry(S2CMessageSyncUpgradeRegistry message) {
        Minecraft.getInstance().execute(() -> {
            ExoSuitUpgradeManager.deserializeUpgrades(message.getUpgradeData());
        });
    }
    public static void handleSyncExoSuitUpgrades(S2CMessageSyncExoSuitUpgrades message) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        Level level = Minecraft.getInstance().level;
        if (localPlayer == null || level == null) return;

        Player targetPlayer = null;
        for (Player player : level.players()) {
            if (player.getUUID().equals(message.getPlayerId())) {
                targetPlayer = player;
                break;
            }
        }

        if (targetPlayer != null) {
            ItemStack armorPiece = targetPlayer.getItemBySlot(message.getArmorSlot());

            if (armorPiece.isEmpty() && !message.getUpgradeData().isEmpty()) {
                return;
            }

            if (armorPiece.getItem() instanceof ExoSuitItem) {
                ExoSuitData.setUpgradeData(armorPiece, message.getUpgradeData());

                targetPlayer.setItemSlot(message.getArmorSlot(), armorPiece.copy());

                if (targetPlayer == localPlayer) {
                    localPlayer.inventoryMenu.broadcastChanges();
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
        tag.putString("scguns:ReloadState", "STOPPING");
        tag.putBoolean("scguns:IsPlayingReloadStop", true);
        tag.remove("InReloadLoop");
        tag.remove("scguns:IsReloading");
        ModSyncedDataKeys.RELOADING.setValue(player, false);

        long id = GeoItem.getId(heldItem);
        AnimationController<GeoAnimatable> animationController = gunItem.getAnimatableInstanceCache()
                .getManagerForId(id)
                .getAnimationControllers()
                .get("controller");

        if (animationController != null) {
            animationController.stop();
            animationController.setAnimationSpeed(1.0);
            animationController.tryTriggerAnimation(
                    gunItem.isInCarbineMode(heldItem) ? "carbine_reload_stop" : "reload_stop"
            );
        }
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
            boolean isVisible = message.isVisible();
            double trailThickness = message.getTrailThickness();

            for(int i = 0; i < message.getCount(); i++)
            {
                BulletTrailRenderingHandler.get().add(new BulletTrail(entityIds[i], positions[i], motions[i],
                        item, trailColor, trailLengthMultiplier, life, gravity, shooterId, enchanted, data,
                        isVisible, trailThickness));
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

