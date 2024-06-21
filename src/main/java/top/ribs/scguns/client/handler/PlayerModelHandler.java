package top.ribs.scguns.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

/**
 * Author: MrCrayfish
 */
public class PlayerModelHandler {

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();
        GunRenderingHandler renderingHandler = GunRenderingHandler.get();

       if (renderingHandler.isThirdPersonMeleeAttacking()) {
           applyCustomArmTransforms(event.getPoseStack(), event.getPartialTick(), event.getRenderer().getModel(), player);
       } else if (!heldItem.isEmpty() && heldItem.getItem() instanceof GunItem) {
         Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
           gun.getGeneral().getGripType().getHeldAnimation().applyPlayerPreRender(player, InteractionHand.MAIN_HAND, AimingHandler.get().getAimProgress(event.getEntity(), event.getPartialTick()), event.getPoseStack(), event.getMultiBufferSource());
       }
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Post event) {
        // Reset the model part positions back to original definitions
        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        boolean slim = ((AbstractClientPlayer) event.getEntity()).getModelName().equals("slim");
        model.rightArm.x = -5.0F;
        model.rightArm.y = slim ? 2.5F : 2.0F;
        model.rightArm.z = 0.0F;
        model.leftArm.x = 5.0F;
        model.leftArm.y = slim ? 2.5F : 2.0F;
        model.leftArm.z = 0.0F;
    }

    private void applyCustomArmTransforms(PoseStack poseStack, float partialTicks, PlayerModel<AbstractClientPlayer> model, Player player) {
        GunRenderingHandler renderingHandler = GunRenderingHandler.get();
        float progress = Mth.lerp(partialTicks, renderingHandler.prevThirdPersonMeleeProgress, renderingHandler.thirdPersonMeleeProgress);
        //System.out.println("Applying custom arm transforms with progress: " + progress);

        // Apply transformations to the right arm
        poseStack.pushPose();
        model.rightArm.translateAndRotate(poseStack);

        if (progress < 0.33f) {
            float raiseProgress = progress / 0.33f;
            poseStack.translate(0, 0.35 * raiseProgress, 0);
            poseStack.translate(0, 0, 0.1 * raiseProgress);
            poseStack.mulPose(Axis.XP.rotationDegrees(35F * raiseProgress));
        } else if (progress < 0.66f) {
            float swingProgress = (progress - 0.33f) / 0.33f;
            poseStack.translate(0, 0.35 - 0.7 * swingProgress, 0);
            poseStack.translate(0, 0.1 - 0.2 * swingProgress, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(35F - 70F * swingProgress));
        } else {
            float returnProgress = (progress - 0.66f) / 0.34f;
            poseStack.translate(0, -0.35 * (1 - returnProgress), 0);
            poseStack.translate(0, 0, -0.1 * (1 - returnProgress));
            poseStack.mulPose(Axis.XP.rotationDegrees(-35F * (1 - returnProgress)));
        }
        model.rightArm.translateAndRotate(poseStack); // Ensure the arm transformation is applied correctly.
        poseStack.popPose();
    }
}

