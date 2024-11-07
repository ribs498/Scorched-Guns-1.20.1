package top.ribs.scguns.client.handler;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.client.particle.BloodParticle;
import top.ribs.scguns.client.render.entity.BeamRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import top.ribs.scguns.client.util.PropertyHelper;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.item.attachment.impl.Scope;

@OnlyIn(Dist.CLIENT)
public class BeamHandler {
    public static final Map<UUID, BeamInfo> activeBeams = new HashMap<>();
    private static final float SMOOTHING_FACTOR = 0.3f;
    private static final float INITIAL_SMOOTHING_FACTOR = 0.6f;
    private static final long MINIMUM_BEAM_DURATION_MS = 100;
    private static final long CLEANUP_DELAY_MS = 150;
    private static float[] getBeamColorForWeapon(ItemStack heldItem, Gun modifiedGun) {
        boolean isEnchanted = heldItem.isEnchanted();
        String primaryColorHex = isEnchanted && modifiedGun.getGeneral().getEnchantedBeamColor() != null ?
                modifiedGun.getGeneral().getEnchantedBeamColor() :
                modifiedGun.getGeneral().getBeamColor();

        String secondaryColorHex = isEnchanted && modifiedGun.getGeneral().getEnchantedSecondaryBeamColor() != null ?
                modifiedGun.getGeneral().getEnchantedSecondaryBeamColor() :
                modifiedGun.getGeneral().getSecondaryBeamColor();

        float[] primaryColor = parseBeamColor(primaryColorHex);
        float[] secondaryColor = parseBeamColor(secondaryColorHex);

        assert Minecraft.getInstance().level != null;
        float time = (Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime()) % 100;
        float progress = (Mth.sin((float) (time / 100.0 * Math.PI * 2)) + 1.0f) / 2.0f;

        return interpolateColors(primaryColor, secondaryColor, progress);
    }
    public static void stopBeam(UUID playerId) {
        ScorchedGuns.LOGGER.debug("Client stopping beam for player: " + playerId);
        BeamInfo beamInfo = activeBeams.get(playerId);
        if (beamInfo != null) {
            beamInfo.isBeamActive = false;
            beamInfo.expiryTime = System.currentTimeMillis() + MINIMUM_BEAM_DURATION_MS;

            // Force remove the beam after a short delay
            Minecraft.getInstance().executeBlocking(() -> {
                activeBeams.remove(playerId);
            });
        }
    }
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        long currentTime = System.currentTimeMillis();

        Iterator<Map.Entry<UUID, BeamInfo>> iterator = activeBeams.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, BeamInfo> entry = iterator.next();
            BeamInfo beamInfo = entry.getValue();

            // Only remove the beam if it's inactive AND has expired
            if (!beamInfo.isBeamActive && currentTime > beamInfo.expiryTime + CLEANUP_DELAY_MS) {
                ScorchedGuns.LOGGER.debug("Removing expired beam for player: " + entry.getKey());
                iterator.remove();
                continue;
            }

            // Check if we haven't received an update in a while (timeout)
            if (currentTime - beamInfo.lastUpdateTime > 500) { // 500ms timeout
                ScorchedGuns.LOGGER.debug("Removing stale beam for player: " + entry.getKey());
                iterator.remove();
                continue;
            }
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        float partialTicks = event.getPartialTick();
        double x = Mth.lerp(partialTicks, player.xo, player.getX());
        double y = Mth.lerp(partialTicks, player.yo, player.getY()) + player.getEyeHeight();
        double z = Mth.lerp(partialTicks, player.zo, player.getZ());
        Vec3 renderPos = new Vec3(x, y, z);

        poseStack.pushPose();
        poseStack.translate(-renderPos.x, -renderPos.y, -renderPos.z);
//
//        Iterator<Map.Entry<UUID, BeamInfo>> iterator = activeBeams.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, BeamInfo> entry = iterator.next();
            BeamInfo beamInfo = entry.getValue();
            if (!beamInfo.isBeamActive && System.currentTimeMillis() > beamInfo.expiryTime) {
                iterator.remove();
            }
        }
        activeBeams.forEach((uuid, beamInfo) -> {
            if (beamInfo.isBeamActive || System.currentTimeMillis() <= beamInfo.expiryTime) {
                Player beamPlayer = mc.level.getPlayerByUUID(uuid);
                if (beamPlayer != null && !beamPlayer.isRemoved()) {
                    ItemStack heldItem = beamPlayer.getMainHandItem();
                    if (heldItem.getItem() instanceof GunItem gunItem) {
                        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
                        if (modifiedGun != null) {
                            float[] interpolatedColor = getBeamColorForWeapon(heldItem, modifiedGun);
                            float smoothingFactor = System.currentTimeMillis() - beamInfo.startTime < 100 ?
                                    INITIAL_SMOOTHING_FACTOR : SMOOTHING_FACTOR;
                            double beamX = Mth.lerp(partialTicks, beamPlayer.xo, beamPlayer.getX());
                            double beamY = Mth.lerp(partialTicks, beamPlayer.yo, beamPlayer.getY()) + beamPlayer.getEyeHeight();
                            double beamZ = Mth.lerp(partialTicks, beamPlayer.zo, beamPlayer.getZ());
                            float deltaDistanceWalked = beamPlayer.walkDist - beamPlayer.walkDistO;
                            float distanceWalked = -(beamPlayer.walkDist + deltaDistanceWalked * partialTicks);
                            float bobbing = Mth.lerp(partialTicks, beamPlayer.oBob, beamPlayer.bob);
                            float yRot = Mth.lerp(partialTicks, beamPlayer.yRotO, beamPlayer.getYRot());
                            float rotationRadians = (float) Math.toRadians(yRot);
                            float horizontalBob = (float) (Math.sin(distanceWalked * Math.PI) * bobbing * 0.65F);
                            beamX += horizontalBob * Math.cos(rotationRadians);
                            beamY += Math.abs(Math.cos(distanceWalked * Math.PI)) * bobbing * 0.85F;
                            beamZ += horizontalBob * Math.sin(rotationRadians);
                            Vec3 basePos = new Vec3(beamX, beamY, beamZ);
                            float xRot = Mth.lerp(partialTicks, beamPlayer.xRotO, beamPlayer.getXRot());
                            Vec3 lookVec = Vec3.directionFromRotation(xRot, yRot);
                            Vec3 upVec = Vec3.directionFromRotation(xRot - 90.0F, yRot);
                            Vec3 rightVec = lookVec.cross(upVec).normalize();

                            float aimProgress = AimingHandler.get().getAimProgress(beamPlayer, partialTicks);
                            Gun.Display.BeamOrigin beamOriginConfig = modifiedGun.getDisplay().getBeamOrigin();
                            double horizontalOffset, verticalOffset, forwardOffset;
                            if (beamOriginConfig != null) {
                                horizontalOffset = Mth.lerp(aimProgress,
                                        beamOriginConfig.getHorizontalOffset(),
                                        beamOriginConfig.getAimHorizontalOffset());
                                verticalOffset = beamOriginConfig.getVerticalOffset();
                                forwardOffset = beamOriginConfig.getForwardOffset();
                            } else {
                                horizontalOffset = Mth.lerp(aimProgress, 0.1, 0.0);
                                verticalOffset = -0.1;
                                forwardOffset = 0.3;
                            }
                            Vec3 beamOrigin = basePos
                                    .add(rightVec.scale(horizontalOffset))
                                    .add(upVec.scale(verticalOffset))
                                    .add(lookVec.scale(forwardOffset));
                            beamInfo.smoothedStartPos = beamOrigin;
                            Vec3 targetEndPos = beamOrigin.add(lookVec.scale(beamInfo.endPos.subtract(beamInfo.startPos).length()));
                            beamInfo.smoothedEndPos = smoothPosition(beamInfo.smoothedEndPos, targetEndPos, smoothingFactor);

                            beamInfo.lastStartPos = beamInfo.smoothedStartPos;
                            beamInfo.lastEndPos = beamInfo.smoothedEndPos;

                            BeamRenderer.renderBeam(
                                    poseStack,
                                    bufferSource,
                                    partialTicks,
                                    beamInfo.smoothedStartPos,
                                    beamInfo.smoothedEndPos,
                                    beamInfo.lastStartPos,
                                    beamInfo.lastEndPos,
                                    interpolatedColor
                            );
                        }
                    }
                }
            }
        });

        bufferSource.endBatch();
        poseStack.popPose();
    }

    private static Vec3 smoothPosition(Vec3 previous, Vec3 current, float smoothingFactor) {
        if (previous == null) return current;
        return new Vec3(
                Mth.lerp(smoothingFactor, previous.x, current.x),
                Mth.lerp(smoothingFactor, previous.y, current.y),
                Mth.lerp(smoothingFactor, previous.z, current.z)
        );
    }

    private static float[] interpolateColors(float[] colorA, float[] colorB, float progress) {
        float r = Mth.lerp(progress, colorA[0], colorB[0]);
        float g = Mth.lerp(progress, colorA[1], colorB[1]);
        float b = Mth.lerp(progress, colorA[2], colorB[2]);
        float a = Mth.lerp(progress, colorA[3], colorB[3]);
        return new float[]{r, g, b, a};
    }

    private static float[] parseBeamColor(String beamColorHex) {
        if (beamColorHex == null || beamColorHex.isEmpty()) {
            return new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        }
        try {
            if (beamColorHex.startsWith("#")) {
                beamColorHex = beamColorHex.substring(1);
            }
            long colorLong = Long.parseLong(beamColorHex, 16);
            float a, r, g, b;
            if (beamColorHex.length() == 8) {
                a = ((colorLong >> 24) & 0xFF) / 255.0f;
                r = ((colorLong >> 16) & 0xFF) / 255.0f;
                g = ((colorLong >> 8) & 0xFF) / 255.0f;
                b = (colorLong & 0xFF) / 255.0f;
            } else {
                a = 1.0f;
                r = ((colorLong >> 16) & 0xFF) / 255.0f;
                g = ((colorLong >> 8) & 0xFF) / 255.0f;
                b = (colorLong & 0xFF) / 255.0f;
            }
            return new float[]{r, g, b, a};
        } catch (NumberFormatException e) {
            return new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        }
    }

    public static void spawnBeamImpactParticles(ClientLevel world, Vec3 hitPosition, Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem gunItem) {
            Gun modifiedGun = gunItem.getModifiedGun(heldItem);
            float[] interpolatedColor = getBeamColorForWeapon(heldItem, modifiedGun);

            for (int i = 0; i < 10; i++) {
                double offsetX = (world.random.nextDouble() - 0.5) * 0.2;
                double offsetY = (world.random.nextDouble() - 0.5) * 0.2;
                double offsetZ = (world.random.nextDouble() - 0.5) * 0.2;
                world.addParticle(ModParticleTypes.BLOOD.get(), false,
                        hitPosition.x + offsetX, hitPosition.y + offsetY, hitPosition.z + offsetZ,
                        interpolatedColor[0], interpolatedColor[1], interpolatedColor[2]);
            }
        }
    }

    public static void updateBeam(UUID playerId, Vec3 startPos, Vec3 endPos) {
        BeamInfo beamInfo = activeBeams.get(playerId);
        if (beamInfo != null) {
            beamInfo.updatePositions(startPos, endPos);
            beamInfo.isBeamActive = true;
        } else {
            BeamInfo newBeamInfo = new BeamInfo(startPos, endPos, System.currentTimeMillis());
            newBeamInfo.isBeamActive = true;
            activeBeams.put(playerId, newBeamInfo);
        }
    }


    public static class BeamInfo {
        public Vec3 startPos;
        public Vec3 endPos;
        public Vec3 lastStartPos;
        public Vec3 lastEndPos;
        public Vec3 smoothedStartPos;
        public Vec3 smoothedEndPos;
        public float lastYaw;
        public float lastPitch;
        public Vec3 lastPlayerPos;
        public long startTime;
        public long lastDamageTime;
        public long lastUpdateTime; // Add this field
        public int ticksActive;
        public boolean isBeamActive;
        public long expiryTime;

        public BeamInfo(Vec3 startPos, Vec3 endPos, long startTime) {
            this.startPos = startPos;
            this.endPos = endPos;
            this.lastStartPos = startPos;
            this.lastEndPos = endPos;
            this.smoothedStartPos = startPos;
            this.smoothedEndPos = endPos;
            this.lastYaw = 0.0f;
            this.lastPitch = 0.0f;
            this.lastPlayerPos = new Vec3(0, 0, 0);
            this.startTime = startTime;
            this.lastDamageTime = startTime;
            this.lastUpdateTime = startTime;
            this.ticksActive = 0;
            this.expiryTime = 0;
        }

        public void updatePositions(Vec3 newStartPos, Vec3 newEndPos) {
            this.lastStartPos = this.startPos;
            this.lastEndPos = this.endPos;
            this.startPos = newStartPos;
            this.endPos = newEndPos;
            this.lastUpdateTime = System.currentTimeMillis();
        }
    }
}