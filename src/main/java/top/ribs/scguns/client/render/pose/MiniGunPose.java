package top.ribs.scguns.client.render.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.item.animated.AnimatedGunItem;

/**
 * Author: MrCrayfish
 */
public class MiniGunPose extends WeaponPose
{
    @Override
    protected AimPose getUpPose()
    {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(45F).setItemRotation(new Vector3f(10F, 0F, 0F)).setRightArm(new LimbPose().setRotationAngleX(-100F).setRotationAngleY(-45F).setRotationAngleZ(0F).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-150F).setRotationAngleY(40F).setRotationAngleZ(-10F).setRotationPointY(1));
        return pose;
    }

    @Override
    protected AimPose getForwardPose()
    {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(45F).setRightArm(new LimbPose().setRotationAngleX(-15F).setRotationAngleY(-45F).setRotationAngleZ(0F).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-45F).setRotationAngleY(30F).setRotationAngleZ(0F).setRotationPointY(2));
        return pose;
    }

    @Override
    protected AimPose getDownPose()
    {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(45F).setItemRotation(new Vector3f(-50F, 0F, 0F)).setItemTranslate(new Vector3f(0F, 0F, 1F)).setRightArm(new LimbPose().setRotationAngleX(0F).setRotationAngleY(-45F).setRotationAngleZ(0F).setRotationPointY(1)).setLeftArm(new LimbPose().setRotationAngleX(-25F).setRotationAngleY(30F).setRotationAngleZ(15F).setRotationPointY(4));
        return pose;
    }

    @Override
    protected AimPose getMeleePose() {
        AimPose meleePose = new AimPose();
        meleePose.getIdle().setRenderYawOffset(0F).setItemRotation(new Vector3f(0F, 0F, 0F))
                .setRightArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0))
                .setLeftArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0));
        meleePose.getAiming().setRenderYawOffset(0F).setItemRotation(new Vector3f(0F, 0F, 0F))
                .setRightArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0))
                .setLeftArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0));
        return meleePose;
    }

    @Override
    protected AimPose getBanzaiPose() {
        AimPose banzaiPose = new AimPose();
        banzaiPose.getIdle().setRenderYawOffset(0F).setItemRotation(new Vector3f(0F, 0F, 0F))
                .setRightArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0))
                .setLeftArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0));
        return banzaiPose;
    }

    @Override
    protected boolean hasAimPose()
    {
        return false;
    }

    @Override
    public void applyPlayerModelRotation(Player player, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress)
    {
        if(Config.CLIENT.display.oldAnimations.get())
        {
            boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
            ModelPart mainArm = right ? rightArm : leftArm;
            ModelPart secondaryArm = right ? leftArm : rightArm;
            mainArm.xRot = (float) Math.toRadians(-15F);
            mainArm.yRot = (float) Math.toRadians(-45F) * (right ? 1F : -1F);
            mainArm.zRot = (float) Math.toRadians(0F);
            secondaryArm.xRot = (float) Math.toRadians(-45F);
            secondaryArm.yRot = (float) Math.toRadians(30F) * (right ? 1F : -1F);
            secondaryArm.zRot = (float) Math.toRadians(0F);
        }
        else
        {
            super.applyPlayerModelRotation(player, rightArm, leftArm, head, hand, aimProgress);
        }

        if (GunRenderingHandler.get().isThirdPersonMeleeAttacking()) {
            float banzaiProgress = GunRenderingHandler.get().getThirdPersonMeleeProgress();
            applyBanzaiPose(rightArm, leftArm, banzaiProgress);
        }
    }

    private void applyBanzaiPose(ModelPart rightArm, ModelPart leftArm, float banzaiProgress) {
        rightArm.xRot = Mth.lerp(banzaiProgress, rightArm.xRot, (float) Math.toRadians(-90F));
        leftArm.xRot = Mth.lerp(banzaiProgress, leftArm.xRot, (float) Math.toRadians(-90F));
    }

    @Override
    public void applyPlayerPreRender(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
        if(Config.CLIENT.display.oldAnimations.get())
        {
            boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
            player.yBodyRotO = player.yRotO + 45F * (right ? 1F : -1F);
            player.yBodyRot = player.getYRot() + 45F * (right ? 1F : -1F);
        }
        else
        {
            super.applyPlayerPreRender(player, hand, aimProgress, poseStack, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
        if(Config.CLIENT.display.oldAnimations.get())
        {
            if(hand == InteractionHand.OFF_HAND)
            {
                poseStack.translate(0, -10 * 0.0625F, 0);
                poseStack.translate(0, 0, -2 * 0.0625F);
            }
        }
        else
        {
            super.applyHeldItemTransforms(player, hand, aimProgress, poseStack, buffer);
        }
    }

    @Override
    public boolean applyOffhandTransforms(Player player, PlayerModel model, ItemStack stack, PoseStack poseStack, float partialTicks)
    {
        return GripType.applyBackTransforms(player, poseStack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderFirstPersonArms(Player player, HumanoidArm hand, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, float partialTicks) {
        if (stack.getItem() instanceof AnimatedGunItem) {
            return;
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(180F));

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, player.level(), player, 0);
        float translateX = model.getTransforms().firstPersonRightHand.translation.x();
        int side = hand.getOpposite() == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(translateX * side, 0, 0);

        boolean slim = Minecraft.getInstance().player.getModelName().equals("slim");
        float armWidth = slim ? 3.0F : 4.0F;

        // Front arm holding the barrel
        poseStack.pushPose();
        {
            float reloadProgress = ReloadHandler.get().getReloadProgress(partialTicks);
            poseStack.translate(reloadProgress * 0.5, -reloadProgress, -reloadProgress * 0.5);

            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(4.0 * 0.0625 * side, 0.65, 0);
            poseStack.translate((armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.translate(-0.55 * side, -0.1, -1.25);

            poseStack.mulPose(Axis.XP.rotationDegrees(110F));
            poseStack.mulPose(Axis.YP.rotationDegrees(25f * -side));
            poseStack.mulPose(Axis.ZP.rotationDegrees(25f * -side));
            poseStack.mulPose(Axis.XP.rotationDegrees(-35F));
            RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand.getOpposite(), poseStack, buffer, light);
        }
        poseStack.popPose();
        // Back arm holding the handle
        poseStack.pushPose();
        {
            poseStack.translate(0, 0.1, -0.675);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(-4.0 * 0.0625 * side, 0, 0);
            poseStack.translate(-(armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(80F));

            RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand, poseStack, buffer, light);
        }
        poseStack.popPose();
    }

    @Override
    public boolean canApplySprintingAnimation()
    {
        return false;
    }
}
