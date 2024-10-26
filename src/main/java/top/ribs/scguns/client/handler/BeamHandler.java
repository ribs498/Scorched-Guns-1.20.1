package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.client.render.entity.BeamRenderer;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageStopBeam;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BeamHandler {
    private static final Map<UUID, BeamInfo> activeBeams = new HashMap<>();
    private static final BeamRenderer beamRenderer = new BeamRenderer();

    public static void updateBeam(UUID playerId, Vec3 startPos, Vec3 endPos) {
        BeamInfo beamInfo = activeBeams.get(playerId);
        if (beamInfo != null) {
            beamInfo.updatePositions(startPos, endPos);
        } else {
            activeBeams.put(playerId, new BeamInfo(startPos, endPos, System.currentTimeMillis()));
        }
    }


    public static void removeBeam(UUID playerId) {
        activeBeams.remove(playerId);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

            float partialTicks = event.getPartialTick();

            activeBeams.forEach((uuid, beamInfo) -> {
                float[] color = calculateBeamColor(beamInfo.startTime);

                ScorchedGuns.LOGGER.debug("Rendering beam: Start: " + beamInfo.startPos + ", End: " + beamInfo.endPos);

                // Pass both last and current positions to the render method
                BeamRenderer.renderBeam(
                        poseStack,
                        bufferSource,
                        partialTicks,
                        beamInfo.startPos,
                        beamInfo.endPos,
                        beamInfo.lastStartPos,  // Previous start position
                        beamInfo.lastEndPos,    // Previous end position
                        color
                );
            });

            bufferSource.endBatch();
            poseStack.popPose();
        }
    }

    public static void stopBeam(UUID playerId) {
        activeBeams.remove(playerId);
    }

    private static float[] calculateBeamColor(long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        float hue = (elapsed % 3000) / 3000f;
        return Color.getHSBColor(hue, 1f, 1f).getRGBColorComponents(null);
    }

    public static class BeamInfo {
        public Vec3 startPos;
        public Vec3 endPos;
        public Vec3 lastStartPos;
        public Vec3 lastEndPos;
        public long startTime;

        public BeamInfo(Vec3 startPos, Vec3 endPos, long startTime) {
            this.startPos = startPos;
            this.endPos = endPos;
            this.lastStartPos = startPos;
            this.lastEndPos = endPos;
            this.startTime = startTime;
        }

        public void updatePositions(Vec3 newStartPos, Vec3 newEndPos) {
            this.lastStartPos = this.startPos;
            this.lastEndPos = this.endPos;
            this.startPos = newStartPos;
            this.endPos = newEndPos;
        }
    }
}