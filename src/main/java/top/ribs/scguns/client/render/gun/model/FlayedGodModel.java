package top.ribs.scguns.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.gun.IOverrideModel;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.init.ModSyncedDataKeys;

import java.util.Random;
import java.util.WeakHashMap;


public class FlayedGodModel implements IOverrideModel {
    private final WeakHashMap<LivingEntity, AnimationState> animationMap = new WeakHashMap<>();

    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        RenderUtil.renderModel(SpecialModels.FLAYED_GOD_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);

        assert entity != null;
        if (entity.equals(Minecraft.getInstance().player)) {
            matrixStack.pushPose();

            AnimationState state = this.animationMap.computeIfAbsent(entity, k -> new AnimationState());
            boolean isShooting = ModSyncedDataKeys.SHOOTING.getValue((Player) entity);

            state.updateTimers(partialTicks, isShooting);
            float transitionFactor = state.getTransitionFactor();

            matrixStack.translate(0, 0.05, 0);

            if (transitionFactor > 0) {
                matrixStack.translate(0, 0.0, 0);
                float hoverHeight = (float) Math.sin(state.baseTimer * 0.25) * 0.03f * transitionFactor;
                matrixStack.translate(0, hoverHeight, 0);
                float circleX = (float) Math.sin(state.time * 0.15) * 0.02f * transitionFactor;
                float circleZ = (float) Math.cos(state.time * 0.15) * 0.02f * transitionFactor;
                matrixStack.translate(circleX, 0, circleZ);
                float[] rotations = state.getSmoothedRotations();
                matrixStack.mulPose(Axis.YP.rotationDegrees(rotations[0] * transitionFactor));
                matrixStack.mulPose(Axis.XP.rotationDegrees(rotations[1] * transitionFactor));
                matrixStack.mulPose(Axis.ZP.rotationDegrees(rotations[2] * transitionFactor));
                matrixStack.translate(0, -0.0, 0);
            }

            renderBarrelAndAttachments(stack, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
        }
    }

    private void renderBarrelAndAttachments(ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        RenderUtil.renderModel(SpecialModels.FLAYED_GOD_HEAD.getModel(), stack, matrixStack, buffer, light, overlay);
    }

    private static class AnimationState {
        private double baseTimer;
        private double time = 0;
        private float continuousAngle = 0;  // Track the continuous rotation separately
        private static final Random random = new Random();
        private static final double TWO_PI = Math.PI * 2;
        private static final float SPIN_SPEED = 8f;  // Degrees per tick

        private static final float TRANSITION_TO_FIRING_SPEED = 0.15f;
        private static final float TRANSITION_TO_IDLE_SPEED = 0.006f;
        private float transitionTimer = 1.0f;

        private final double[][] frequencies = {
                {0.1, 0.04, 0.07},    // Yaw frequencies
                {0.05, 0.08, 0.03},   // Pitch frequencies
                {0.06, 0.03, 0.09}    // Roll frequencies
        };

        private final double[][] amplitudes = {
                {15.0, 10.0, 5.0},
                {5.0, 3.0, 2.0},
                {8.0, 4.0, 2.0}
        };

        private final double[][] phases = {
                {0, TWO_PI / 3, TWO_PI * 2 / 3},
                {TWO_PI / 6, TWO_PI / 2, TWO_PI * 5 / 6},
                {TWO_PI / 4, TWO_PI * 3 / 4, TWO_PI * 7 / 8}
        };

        public AnimationState() {
            for (int axis = 0; axis < 3; axis++) {
                for (int i = 0; i < phases[axis].length; i++) {
                    phases[axis][i] += random.nextDouble() * TWO_PI;
                }
            }
        }

        public void updateTimers(float partialTicks, boolean isShooting) {
            float timeStep = 0.016f * partialTicks;
            baseTimer += timeStep;
            time += timeStep;

            // Update continuous rotation angle
            continuousAngle = (continuousAngle + SPIN_SPEED * timeStep) % 360;

            float transitionSpeed = isShooting ? TRANSITION_TO_FIRING_SPEED : TRANSITION_TO_IDLE_SPEED;
            if (isShooting) {
                transitionTimer = Math.max(0, transitionTimer - transitionSpeed);
            } else {
                transitionTimer = Math.min(1, transitionTimer + transitionSpeed);
            }

            if (time > 1000000) {
                time = 0;
            }
        }

        public float getTransitionFactor() {
            return (float) enhancedSmoothStep(transitionTimer);
        }

        private double enhancedSmoothStep(double x) {
            x = Math.max(0, Math.min(1, x));
            return x * x * x * (x * (x * 6 - 15) + 10);
        }

        public float[] getSmoothedRotations() {
            float[] rotations = new float[3];
            for (int axis = 0; axis < 3; axis++) {
                double rotation = 0;
                for (int wave = 0; wave < frequencies[axis].length; wave++) {
                    rotation += Math.sin(time * frequencies[axis][wave] + phases[axis][wave])
                            * amplitudes[axis][wave];
                }
                rotations[axis] = (float) rotation;
            }

            rotations[0] += continuousAngle;

            return rotations;
        }
    }
}