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
    private WeakHashMap<LivingEntity, AnimationState> animationMap = new WeakHashMap<>();
    private Random random = new Random();

    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        RenderUtil.renderModel(SpecialModels.BLASPHEMY_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);

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
                // Apply idle animations
                matrixStack.translate(Math.sin(state.wiggle) * 0.01, Math.cos(state.wiggle) * 0.01, 0);
                state.wiggle += (random.nextFloat() - 0.5) * 0.05; // Slower animation
                state.lookAround += (random.nextFloat() - 0.5) * 0.05; // Slower animation
                matrixStack.mulPose(Axis.YP.rotationDegrees((float) state.lookAround));
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
        private double wiggle;
        private double lookAround;
    }

}