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
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.client.render.IHeldAnimation;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.item.animated.AnimatedGunItem;

/**
 * Author: MrCrayfish
 */
public class TwoHandedPose extends WeaponPose
{
    @Override
    protected AimPose getUpPose()
    {
        AimPose upPose = new AimPose();
        upPose.getIdle().setRenderYawOffset(45F).setItemRotation(new Vector3f(60F, 0F, 10F)).setRightArm(new LimbPose().setRotationAngleX(-120F).setRotationAngleY(-55F).setRotationPointX(-5).setRotationPointY(3).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-160F).setRotationAngleY(-20F).setRotationAngleZ(-30F).setRotationPointY(2).setRotationPointZ(-1));
        upPose.getAiming().setRenderYawOffset(45F).setItemRotation(new Vector3f(40F, 0F, 30F)).setItemTranslate(new Vector3f(-1, 0, 0)).setRightArm(new LimbPose().setRotationAngleX(-140F).setRotationAngleY(-55F).setRotationPointX(-5).setRotationPointY(3).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-170F).setRotationAngleY(-20F).setRotationAngleZ(-35F).setRotationPointY(1).setRotationPointZ(0));
        return upPose;
    }

    @Override
    protected AimPose getForwardPose()
    {
        AimPose forwardPose = new AimPose();
        forwardPose.getIdle().setRenderYawOffset(45F).setItemRotation(new Vector3f(30F, -11F, 0F)).setRightArm(new LimbPose().setRotationAngleX(-60F).setRotationAngleY(-55F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2).setRotationPointZ(1)).setLeftArm(new LimbPose().setRotationAngleX(-65F).setRotationAngleY(-10F).setRotationAngleZ(5F).setRotationPointY(2).setRotationPointZ(-1));
        forwardPose.getAiming().setRenderYawOffset(45F).setItemRotation(new Vector3f(5F, -21F, 0F)).setRightArm(new LimbPose().setRotationAngleX(-85F).setRotationAngleY(-65F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(-15F).setRotationAngleZ(0F).setRotationPointY(2).setRotationPointZ(0));
        return forwardPose;
    }

    @Override
    protected AimPose getDownPose()
    {
        AimPose downPose = new AimPose();
        downPose.getIdle().setRenderYawOffset(45F).setItemRotation(new Vector3f(-15F, -5F, 0F)).setItemTranslate(new Vector3f(0, -0.5F, 0.5F)).setRightArm(new LimbPose().setRotationAngleX(-30F).setRotationAngleY(-65F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-5F).setRotationAngleY(-20F).setRotationAngleZ(20F).setRotationPointY(5).setRotationPointZ(0));
        downPose.getAiming().setRenderYawOffset(45F).setItemRotation(new Vector3f(-20F, -5F, -10F)).setItemTranslate(new Vector3f(0, -0.5F, 1F)).setRightArm(new LimbPose().setRotationAngleX(-30F).setRotationAngleY(-65F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(1)).setLeftArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(-20F).setRotationAngleZ(30F).setRotationPointY(5).setRotationPointZ(0));
        return downPose;
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

    private void applyBanzaiPose(ModelPart rightArm, ModelPart leftArm, float banzaiProgress) {
        float smoothProgress = easeInOut(banzaiProgress);
        rightArm.xRot = Mth.lerp(smoothProgress, rightArm.xRot, (float) Math.toRadians(-70F));
        leftArm.xRot = Mth.lerp(smoothProgress, leftArm.xRot, (float) Math.toRadians(-70F));
    }
    private float easeInOut(float t) {
        return t * t * (3 - 2 * t);
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyPlayerModelRotation(Player player, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress) {
        super.applyPlayerModelRotation(player, rightArm, leftArm, head, hand, aimProgress);
        float angle = this.getPlayerPitch(player);
        head.xRot = (float) Math.toRadians(angle > 0.0 ? angle * 70F : angle * 90F);

        boolean isTwoHandedPose = true;

        if (!isTwoHandedPose) {
            player.getOffhandItem().isEmpty();
        }
        super.applyPlayerModelRotation(player, rightArm, leftArm, head, hand, aimProgress);

        if (GunRenderingHandler.get().isThirdPersonMeleeAttacking()) {
            float banzaiProgress = GunRenderingHandler.get().getThirdPersonMeleeProgress();
            applyBanzaiPose(rightArm, leftArm, banzaiProgress);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyPlayerPreRender(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
        if (Config.CLIENT.display.oldAnimations.get())
        {
            boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
            player.yBodyRotO = player.yRotO + (right ? 25F : -25F) + aimProgress * (right ? 20F : -20F);
            player.yBodyRot = player.getYRot() + (right ? 25F : -25F) + aimProgress * (right ? 20F : -20F);
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
        if (Config.CLIENT.display.oldAnimations.get())
        {
            if (hand == InteractionHand.MAIN_HAND)
            {
                boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT;
                poseStack.translate(0, 0, 0.05);
                float invertRealProgress = 1.0F - aimProgress;
                poseStack.mulPose(Axis.ZP.rotationDegrees((25F * invertRealProgress) * (right ? 1F : -1F)));
                poseStack.mulPose(Axis.YP.rotationDegrees((30F * invertRealProgress + aimProgress * -20F) * (right ? 1F : -1F)));
                poseStack.mulPose(Axis.XP.rotationDegrees(25F * invertRealProgress + aimProgress * 5F));
            }
        }
        else
        {
            super.applyHeldItemTransforms(player, hand, aimProgress, poseStack, buffer);
        }
    }

    @Override
    public void renderFirstPersonArms(Player player, HumanoidArm hand, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, float partialTicks)
    {
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
            poseStack.translate(4.0 * 0.0625 * side, 0, 0);
            poseStack.translate((armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.translate(-0.3125 * side, -0.12, -0.35);

            poseStack.mulPose(Axis.XP.rotationDegrees(80F));
            poseStack.mulPose(Axis.YP.rotationDegrees(15F * -side));
            poseStack.mulPose(Axis.ZP.rotationDegrees(15F * -side));
            poseStack.mulPose(Axis.XP.rotationDegrees(-35F));

            RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand.getOpposite(), poseStack, buffer, light);
        }
        poseStack.popPose();

        // Back arm holding the handle
        poseStack.pushPose();
        {
            poseStack.translate(0, 0.1, -0.7);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(-4.0 * 0.0625 * side, 0, 0);
            poseStack.translate(-(armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(80F));
            poseStack.mulPose(Axis.YP.rotationDegrees(5F * side));

            RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand, poseStack, buffer, light);
        }
        poseStack.popPose();
    }
    @Override
    public boolean applyOffhandTransforms(Player player, PlayerModel model, ItemStack stack, PoseStack poseStack, float partialTicks)
    {
        return GripType.applyBackTransforms(player, poseStack);
    }
    @Override
    protected AimPose getBanzaiPose() {
        AimPose banzaiPose = new AimPose();
        banzaiPose.getIdle().setRenderYawOffset(0F).setItemRotation(new Vector3f(0F, 0F, 0F))
                .setRightArm(new LimbPose().setRotationAngleX(-70F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0))
                .setLeftArm(new LimbPose().setRotationAngleX(-70F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0));
        banzaiPose.getAiming().setRenderYawOffset(0F).setItemRotation(new Vector3f(0F, 0F, 0F))
                .setRightArm(new LimbPose().setRotationAngleX(-70F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0))
                .setLeftArm(new LimbPose().setRotationAngleX(-70F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0));
        return banzaiPose;
    }
}

