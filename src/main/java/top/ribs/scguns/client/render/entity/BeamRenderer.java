package top.ribs.scguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class BeamRenderer {
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");
    private static final int MAX_RENDER_DISTANCE = 64;

    public BeamRenderer() {
    }

    public static void renderBeam(PoseStack pPoseStack, MultiBufferSource pBuffer, float partialTicks, Vec3 start, Vec3 end, Vec3 lastStart, Vec3 lastEnd, float[] color) {
        // Interpolate between last and current positions for smoother beam movement
        Vec3 interpolatedStart = lerpVec3(partialTicks, lastStart, start);
        Vec3 interpolatedEnd = lerpVec3(partialTicks, lastEnd, end);

        Vec3 beamVec = interpolatedEnd.subtract(interpolatedStart);
        float length = (float) beamVec.length();
        Vec3 beamNormal = beamVec.normalize();

        pPoseStack.pushPose();
        pPoseStack.translate(interpolatedStart.x, interpolatedStart.y, interpolatedStart.z);

        // Calculate rotation to align with beam direction
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 axis = up.cross(beamNormal);
        float angle = (float) Math.acos(up.dot(beamNormal));

        // Apply rotation
        if (!axis.equals(Vec3.ZERO)) {
            pPoseStack.mulPose(Axis.of(new Vector3f((float)axis.x, (float)axis.y, (float)axis.z)).rotationDegrees((float) Math.toDegrees(angle)));
        }

        // Render the beam
        assert Minecraft.getInstance().level != null;
        renderBeamSegment(pPoseStack, pBuffer, partialTicks, Minecraft.getInstance().level.getGameTime(), 0, length, color);

        pPoseStack.popPose();
    }

    // Linear interpolation (lerp) between two vectors
    private static Vec3 lerpVec3(float partialTicks, Vec3 lastPos, Vec3 currentPos) {
        double x = Mth.lerp(partialTicks, lastPos.x, currentPos.x);
        double y = Mth.lerp(partialTicks, lastPos.y, currentPos.y);
        double z = Mth.lerp(partialTicks, lastPos.z, currentPos.z);
        return new Vec3(x, y, z);
    }


    private static void renderBeamSegment(PoseStack pPoseStack, MultiBufferSource pBufferSource, float pPartialTick, long pGameTime, float pYOffset, float pHeight, float[] pColors) {
        // Example of thinner beam
        renderBeamPart(pPoseStack, pBufferSource, BEAM_LOCATION, pPartialTick, 1.0F, pGameTime, 0, Math.round(pHeight), pColors, 0.05F, 0.07F);

    }

        public static void renderBeamPart(PoseStack pPoseStack, MultiBufferSource pBufferSource, ResourceLocation pBeamLocation, float pPartialTick, float pTextureScale, long pGameTime, int pYOffset, int pHeight, float[] pColors, float pBeamRadius, float pGlowRadius) {
            int maxY = pYOffset + pHeight;
            pPoseStack.pushPose();
            float f = (float)Math.floorMod(pGameTime, 40) + pPartialTick;
            float f1 = pHeight < 0 ? f : -f;
            float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
            float r = pColors[0];
            float g = pColors[1];
            float b = pColors[2];
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
            float f6 = 0.0F;
            float f8 = 0.0F;
            float f9 = -pBeamRadius;
            float f13 = 0.0F;
            float f14 = 0.0F;
            float f15 = -pBeamRadius;
            float f16 = 0.0F;
            float f17 = 1.0F;
            float f18 = -1.0F + f2;
            float f19 = (float)pHeight * pTextureScale * (0.5F / pBeamRadius) + f18;
            renderPart(pPoseStack, pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, false)), r, g, b, 1.0F, pYOffset, maxY, 0.0F, pBeamRadius, pBeamRadius, 0.0F, f9, 0.0F, 0.0F, f15, 0.0F, 1.0F, f19, f18);
            pPoseStack.popPose();
            f6 = -pGlowRadius;
            float f7 = -pGlowRadius;
            f8 = -pGlowRadius;
            f9 = -pGlowRadius;
            f16 = 0.0F;
            f17 = 1.0F;
            f18 = -1.0F + f2;
            f19 = (float)pHeight * pTextureScale + f18;
            renderPart(pPoseStack, pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, true)), r, g, b, 0.125F, pYOffset, maxY, f6, f7, pGlowRadius, f8, f9, pGlowRadius, pGlowRadius, pGlowRadius, 0.0F, 1.0F, f19, f18);
            pPoseStack.popPose();
        }

        private static void renderPart(PoseStack pPoseStack, VertexConsumer pConsumer, float pRed, float pGreen, float pBlue, float pAlpha, int pMinY, int pMaxY, float pX0, float pZ0, float pX1, float pZ1, float pX2, float pZ2, float pX3, float pZ3, float pMinU, float pMaxU, float pMinV, float pMaxV) {
            PoseStack.Pose pose = pPoseStack.last();
            Matrix4f matrix4f = pose.pose();
            Matrix3f matrix3f = pose.normal();
            renderQuad(matrix4f, matrix3f, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxY, pX0, pZ0, pX1, pZ1, pMinU, pMaxU, pMinV, pMaxV);
            renderQuad(matrix4f, matrix3f, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxY, pX3, pZ3, pX2, pZ2, pMinU, pMaxU, pMinV, pMaxV);
            renderQuad(matrix4f, matrix3f, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxY, pX1, pZ1, pX3, pZ3, pMinU, pMaxU, pMinV, pMaxV);
            renderQuad(matrix4f, matrix3f, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxY, pX2, pZ2, pX0, pZ0, pMinU, pMaxU, pMinV, pMaxV);
        }

        private static void renderQuad(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer, float pRed, float pGreen, float pBlue, float pAlpha, int pMinY, int pMaxY, float pMinX, float pMinZ, float pMaxX, float pMaxZ, float pMinU, float pMaxU, float pMinV, float pMaxV) {
            addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMinX, pMinZ, pMaxU, pMinV);
            addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMinX, pMinZ, pMaxU, pMaxV);
            addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxX, pMaxZ, pMinU, pMaxV);
            addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMaxX, pMaxZ, pMinU, pMinV);
        }

        private static void addVertex(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer, float pRed, float pGreen, float pBlue, float pAlpha, int pY, float pX, float pZ, float pU, float pV) {
            pConsumer.vertex(pPose, pX, (float)pY, pZ).color(pRed, pGreen, pBlue, pAlpha).uv(pU, pV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(pNormal, 0.0F, 1.0F, 0.0F).endVertex();
        }
    }