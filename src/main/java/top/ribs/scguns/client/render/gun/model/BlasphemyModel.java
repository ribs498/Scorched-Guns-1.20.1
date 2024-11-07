package top.ribs.scguns.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;
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


public class BlasphemyModel implements IOverrideModel {
    private final WeakHashMap<LivingEntity, AnimationState> animationMap = new WeakHashMap<>();

    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        RenderUtil.renderModel(SpecialModels.BLASPHEMY_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);

        assert entity != null;
        if (entity.equals(Minecraft.getInstance().player)) {
            matrixStack.pushPose();
            matrixStack.translate(0, -5.8 * 0.0625, 0);
            ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
            cooldown = (float) ease(cooldown);
            matrixStack.translate(0, 0, cooldown / 18);
            matrixStack.translate(0, 5.8 * 0.0625, 0);

            AnimationState state = this.animationMap.computeIfAbsent(entity, k -> new AnimationState());
            if (!ModSyncedDataKeys.SHOOTING.getValue((Player) entity)) {
                state.updateTimers(partialTicks);
                float hoverHeight = (float) Math.sin(state.baseTimer * 0.3) * 0.012f;
                matrixStack.translate(0, hoverHeight, 0);
                float swayAmount = (float) Math.sin(state.baseTimer * 0.2) * 0.008f;
                matrixStack.translate(swayAmount, 0, 0);
                float rotation = state.getSmoothedRotation();
                matrixStack.mulPose(Axis.YP.rotationDegrees(rotation));
            }

            renderBarrelAndAttachments(stack, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
        }
    }

    private void renderBarrelAndAttachments(ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        RenderUtil.renderModel(SpecialModels.BLASPHEMY_HEAD.getModel(), stack, matrixStack, buffer, light, overlay);
    }

    private double ease(double x) {
        return 1 - Math.pow(1 - (2 * x), 4);
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        this.animationMap.clear();
    }

    private static class AnimationState {
        private double baseTimer;
        private static final Random random = new Random();

        private double time = 0;
        private static final double TWO_PI = Math.PI * 2;
        private final double[] frequencies = {0.12, 0.05, 0.08};
        private final double[] amplitudes = {2.0, 3.0, 1.5};
        private final double[] phases = {0, TWO_PI / 3, TWO_PI * 2 / 3};

        public AnimationState() {
            for (int i = 0; i < phases.length; i++) {
                phases[i] += random.nextDouble() * TWO_PI;
            }
        }

        public void updateTimers(float partialTicks) {
            baseTimer += 0.016 * partialTicks;
            time += 0.016 * partialTicks;
            if (time > 1000000) {
                time = 0;
            }
        }

        public float getSmoothedRotation() {
            double rotation = 0;
            for (int i = 0; i < frequencies.length; i++) {
                rotation += Math.sin(time * frequencies[i] + phases[i]) * amplitudes[i];
            }

            return (float) rotation;
        }
    }
}