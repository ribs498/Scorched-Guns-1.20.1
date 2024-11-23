package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.ribs.scguns.client.render.entity.BeamRenderer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.GunItem;

@OnlyIn(Dist.CLIENT)
public class BeamHandler {
    public static final Map<UUID, BeamInfo> activeBeams = new HashMap<>();
    private static final float SMOOTHING_FACTOR = 0.3f;
    private static final float INITIAL_SMOOTHING_FACTOR = 0.6f;
    private static final long CONTINUOUS_BEAM_DURATION_MS = 200;
    private static final long SEMI_BEAM_DURATION_MS = 50;
    private static final long CLEANUP_DELAY_MS = 50;
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
        BeamInfo beamInfo = activeBeams.get(playerId);
        if (beamInfo != null) {
            beamInfo.isBeamActive = false;
            long duration = beamInfo.isBeamFireMode ? CONTINUOUS_BEAM_DURATION_MS : SEMI_BEAM_DURATION_MS;
            beamInfo.expiryTime = System.currentTimeMillis() + duration;
        }
    }


    private static void cleanupInactiveBeams() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, BeamInfo>> iterator = activeBeams.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, BeamInfo> entry = iterator.next();
            BeamInfo beamInfo = entry.getValue();

            if ((!beamInfo.isBeamActive && currentTime > beamInfo.expiryTime + CLEANUP_DELAY_MS) ||
                    (currentTime - beamInfo.lastUpdateTime > 500)) {
                iterator.remove();
            }
        }
    }
    public static void updateBeam(UUID playerId, Vec3 startPos, Vec3 endPos) {
        BeamInfo beamInfo = activeBeams.get(playerId);
        if (beamInfo != null) {
            beamInfo.updatePositions(startPos, endPos);
            beamInfo.isBeamActive = true;
        } else {
            assert Minecraft.getInstance().level != null;
            Player player = Minecraft.getInstance().level.getPlayerByUUID(playerId);
            boolean isBeamFireMode = false;

            if (player != null) {
                ItemStack heldItem = player.getMainHandItem();
                if (heldItem.getItem() instanceof GunItem gunItem) {
                    Gun modifiedGun = gunItem.getModifiedGun(heldItem);
                    if (modifiedGun != null) {
                        isBeamFireMode = modifiedGun.getGeneral().getFireMode().equals(FireMode.BEAM);
                    }
                }
            }

            BeamInfo newBeamInfo = new BeamInfo(startPos, endPos, System.currentTimeMillis(), isBeamFireMode);
            newBeamInfo.isBeamActive = true;
            activeBeams.put(playerId, newBeamInfo);
        }
    }
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        cleanupInactiveBeams();

        LocalPlayer player = mc.player;
        if (player == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        setupRenderContext(poseStack, event.getPartialTick(), player);

        renderActiveBeams(mc, event.getPartialTick(), poseStack, bufferSource);

        bufferSource.endBatch();
        poseStack.popPose();
    }

    private static void setupRenderContext(PoseStack poseStack, float partialTicks, LocalPlayer player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem gunItem) {
            Gun modifiedGun = gunItem.getModifiedGun(heldItem);
            if (modifiedGun != null && modifiedGun.getGeneral().getFireMode().equals(FireMode.BEAM)) {
                player.bob = 0;
                player.oBob = 0;
                player.walkDist = 0;
                player.walkDistO = 0;
            }
        }

        Vec3 renderPos = calculateBasePosition(player, partialTicks);
        poseStack.pushPose();
        poseStack.translate(-renderPos.x, -renderPos.y, -renderPos.z);
    }
    private static void renderActiveBeams(Minecraft mc, float partialTicks, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        activeBeams.forEach((uuid, beamInfo) -> {
            if (!shouldRenderBeam(beamInfo)) return;

            assert mc.level != null;
            Player beamPlayer = mc.level.getPlayerByUUID(uuid);
            if (beamPlayer == null || beamPlayer.isRemoved()) return;

            ItemStack heldItem = beamPlayer.getMainHandItem();
            if (!(heldItem.getItem() instanceof GunItem gunItem)) return;

            Gun modifiedGun = gunItem.getModifiedGun(heldItem);
            if (modifiedGun == null) return;

            renderBeam(beamPlayer, beamInfo, modifiedGun, heldItem, partialTicks, poseStack, bufferSource);
        });
    }

    private static boolean shouldRenderBeam(BeamInfo beamInfo) {
        return beamInfo.isBeamActive || System.currentTimeMillis() <= beamInfo.expiryTime;
    }

    private static void renderBeam(Player beamPlayer, BeamInfo beamInfo, Gun modifiedGun, ItemStack heldItem,
                                   float partialTicks, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        float originalBob = beamPlayer.bob;
        float originalOBob = beamPlayer.oBob;
        float originalWalkDist = beamPlayer.walkDist;
        float originalWalkDistO = beamPlayer.walkDistO;

        if (heldItem.getItem() instanceof GunItem &&
                ((GunItem) heldItem.getItem()).getModifiedGun(heldItem).getGeneral().getFireMode().equals(FireMode.BEAM)) {
            beamPlayer.bob = 0;
            beamPlayer.oBob = 0;
            beamPlayer.walkDist = 0;
            beamPlayer.walkDistO = 0;
        }
        float[] interpolatedColor = getBeamColorForWeapon(heldItem, modifiedGun);
        float smoothingFactor = System.currentTimeMillis() - beamInfo.startTime < 100 ?
                INITIAL_SMOOTHING_FACTOR : SMOOTHING_FACTOR;

        Vec3 beamOrigin = calculateBeamOrigin(beamPlayer, modifiedGun, partialTicks);
        beamInfo.smoothedStartPos = beamOrigin;

        Vec3 lookVec = Vec3.directionFromRotation(
                Mth.lerp(partialTicks, beamPlayer.xRotO, beamPlayer.getXRot()),
                Mth.lerp(partialTicks, beamPlayer.yRotO, beamPlayer.getYRot())
        );

        Vec3 targetEndPos = beamOrigin.add(lookVec.scale(beamInfo.endPos.subtract(beamInfo.startPos).length()));
        beamInfo.smoothedEndPos = smoothPosition(beamInfo.smoothedEndPos, targetEndPos, smoothingFactor);

        beamInfo.lastStartPos = beamInfo.smoothedStartPos;
        beamInfo.lastEndPos = beamInfo.smoothedEndPos;

        beamInfo.updateFade(partialTicks);

        BeamRenderer.renderBeam(
                poseStack,
                bufferSource,
                partialTicks,
                beamInfo.smoothedStartPos,
                beamInfo.smoothedEndPos,
                beamInfo.lastStartPos,
                beamInfo.lastEndPos,
                interpolatedColor,
                beamInfo.fadeProgress
        );
        beamPlayer.bob = originalBob;
        beamPlayer.oBob = originalOBob;
        beamPlayer.walkDist = originalWalkDist;
        beamPlayer.walkDistO = originalWalkDistO;
    }
    private static Vec3 calculateBeamOrigin(Player player, Gun modifiedGun, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        boolean isThirdPerson = mc.options.getCameraType().ordinal() > 0;

        if (!isThirdPerson) {
            return calculateFirstPersonBeamOrigin(player, modifiedGun, partialTicks);
        } else {
            return calculateThirdPersonBeamOrigin(player, modifiedGun, partialTicks);
        }
    }

    private static Vec3 calculateFirstPersonBeamOrigin(Player player, Gun modifiedGun, float partialTicks) {
        Vec3 basePos = calculateBasePosition(player, partialTicks);
        Vec3 lookVec = Vec3.directionFromRotation(
                Mth.lerp(partialTicks, player.xRotO, player.getXRot()),
                Mth.lerp(partialTicks, player.yRotO, player.getYRot())
        );
        Vec3 upVec = Vec3.directionFromRotation(
                Mth.lerp(partialTicks, player.xRotO, player.getXRot()) - 90.0F,
                Mth.lerp(partialTicks, player.yRotO, player.getYRot())
        );
        Vec3 rightVec = lookVec.cross(upVec).normalize();

        float aimProgress = AimingHandler.get().getAimProgress(player, partialTicks);
        double[] offsets = calculateBeamOffsets(modifiedGun, aimProgress);

        return basePos
                .add(rightVec.scale(offsets[0]))
                .add(upVec.scale(offsets[1]))
                .add(lookVec.scale(offsets[2]));
    }

    private static Vec3 calculateThirdPersonBeamOrigin(Player player, Gun modifiedGun, float partialTicks) {
        Vec3 basePos = new Vec3(
                Mth.lerp(partialTicks, player.xo, player.getX()),
                Mth.lerp(partialTicks, player.yo, player.getY()) + player.getEyeHeight() - 0.2,
                Mth.lerp(partialTicks, player.zo, player.getZ())
        );

        Vec3 lookVec = Vec3.directionFromRotation(
                Mth.lerp(partialTicks, player.xRotO, player.getXRot()),
                Mth.lerp(partialTicks, player.yRotO, player.getYRot())
        );
        Vec3 upVec = Vec3.directionFromRotation(
                Mth.lerp(partialTicks, player.xRotO, player.getXRot()) - 90.0F,
                Mth.lerp(partialTicks, player.yRotO, player.getYRot())
        );
        Vec3 rightVec = lookVec.cross(upVec).normalize();

        float aimProgress = AimingHandler.get().getAimProgress(player, partialTicks);
        calculateBeamOffsets(modifiedGun, aimProgress);

        return basePos
                .add(rightVec.scale(0.1))
                .add(upVec.scale(-0.1))
                .add(lookVec.scale(2.15));
    }
    private static Vec3 calculateBasePosition(Player player, float partialTicks) {
        double x = Mth.lerp(partialTicks, player.xo, player.getX());
        double y = Mth.lerp(partialTicks, player.yo, player.getY()) + player.getEyeHeight();
        double z = Mth.lerp(partialTicks, player.zo, player.getZ());

        return new Vec3(x, y, z);
    }
    private static double[] calculateBeamOffsets(Gun modifiedGun, float aimProgress) {
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

        return new double[]{horizontalOffset, verticalOffset, forwardOffset};
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
        public long lastUpdateTime;
        public int ticksActive;
        public boolean isBeamActive;
        public long expiryTime;
        public boolean isBeamFireMode;
        public float fadeProgress = 0.0f;
        private static final float FADE_SPEED = 2.0f;
        public BeamInfo(Vec3 startPos, Vec3 endPos, long startTime, boolean isBeamFireMode) {
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
            this.isBeamFireMode = isBeamFireMode;
        }

        public void updatePositions(Vec3 newStartPos, Vec3 newEndPos) {
            this.lastStartPos = this.startPos;
            this.lastEndPos = this.endPos;
            this.startPos = newStartPos;
            this.endPos = newEndPos;
            this.lastUpdateTime = System.currentTimeMillis();
        }
        public void updateFade(float partialTicks) {
            if (!isBeamActive && fadeProgress < 1.0f) {
                fadeProgress = Math.min(1.0f, fadeProgress + FADE_SPEED * partialTicks);
            } else if (isBeamActive) {
                fadeProgress = 0.0f;
            }
        }
    }
}