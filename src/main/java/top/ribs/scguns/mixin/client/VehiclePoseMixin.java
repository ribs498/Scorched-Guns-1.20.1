package top.ribs.scguns.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.render.pose.WeaponPose;
import top.ribs.scguns.client.render.pose.AimPose;
import top.ribs.scguns.client.render.pose.LimbPose;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.VehicleDetectionHandler;
import top.ribs.scguns.item.GunItem;
import java.lang.reflect.Method;

@Mixin(value = PlayerModel.class, priority = 1100)
public class VehiclePoseMixin<T extends LivingEntity> {

    @Inject(method = "setupAnim*", at = @At(value = "RETURN"), cancellable = false)
    private void applyVehicleGunPoses(T entity, float animationPos, float animationSpeed, float animationBob, float deltaHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player)) {
            return;
        }

        if (!VehicleDetectionHandler.isPlayerInVehicle(player)) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }

        PlayerModel<T> model = (PlayerModel<T>) (Object) this;

        float aimProgress = 0.0f;
        if (player.isLocalPlayer()) {
            aimProgress = AimingHandler.get().getAimProgress(player, Minecraft.getInstance().getFrameTime());
        }

        Gun gun = gunItem.getModifiedGun(heldItem);
        applyAdaptedPose(gun, heldItem, model, aimProgress);

        copyModelAngles(model.rightArm, model.rightSleeve);
        copyModelAngles(model.leftArm, model.leftSleeve);
        copyModelAngles(model.head, model.hat);
    }

    private void applyAdaptedPose(Gun gun, ItemStack stack, PlayerModel<T> model, float aimProgress) {
        try {
            var heldAnimation = gun.getGeneral().getGripType(stack).heldAnimation();

            if (heldAnimation instanceof WeaponPose weaponPose) {
                AimPose forwardPose = getForwardPose(weaponPose);
                if (forwardPose != null) {
                    applyModifiedPose(forwardPose, model.rightArm, model.leftArm, aimProgress);
                    return;
                }
            }
        } catch (Exception e) {
            // Fallback if reflection fails
        }

        applyFallbackPose(model.rightArm, model.leftArm, aimProgress);
    }

    private AimPose getForwardPose(WeaponPose weaponPose) {
        try {
            Method getForwardPoseMethod = WeaponPose.class.getDeclaredMethod("getForwardPose");
            getForwardPoseMethod.setAccessible(true);
            return (AimPose) getForwardPoseMethod.invoke(weaponPose);
        } catch (Exception e) {
            return null;
        }
    }

    private void applyModifiedPose(AimPose forwardPose, ModelPart rightArm, ModelPart leftArm, float aimProgress) {
        var idlePose = forwardPose.getIdle();
        var aimingPose = forwardPose.getAiming();

        LimbPose rightArmIdle = idlePose.getRightArm();
        LimbPose rightArmAiming = aimingPose != null ? aimingPose.getRightArm() : rightArmIdle;
        applyLimbPose(rightArmIdle, rightArmAiming, rightArm, aimProgress, true);

        LimbPose leftArmIdle = idlePose.getLeftArm();
        LimbPose leftArmAiming = aimingPose != null ? aimingPose.getLeftArm() : leftArmIdle;
        applyLimbPose(leftArmIdle, leftArmAiming, leftArm, aimProgress, false);
    }

    private void applyLimbPose(LimbPose idlePose, LimbPose aimingPose, ModelPart modelPart, float aimProgress, boolean isRightArm) {
        float rotX = interpolate(idlePose.getRotationAngleX(), aimingPose.getRotationAngleX(), aimProgress);
        float rotY = interpolate(idlePose.getRotationAngleY(), aimingPose.getRotationAngleY(), aimProgress);
        float rotZ = interpolate(idlePose.getRotationAngleZ(), aimingPose.getRotationAngleZ(), aimProgress);

        if (isRightArm) {
            rotX = Math.max(rotX, -120F);
            modelPart.xRot = (float) Math.toRadians(rotX);
            modelPart.yRot = (float) Math.toRadians(Math.min(Math.abs(rotY), 15F) * Math.signum(rotY));
            modelPart.zRot = (float) Math.toRadians(rotZ * 0.5F);
        } else {
            modelPart.xRot = (float) Math.toRadians(-20F);
            modelPart.yRot = (float) Math.toRadians(15F);
            modelPart.zRot = 0F;
        }

        if (idlePose.getRotationPointX() != null) {
            modelPart.x = interpolate(idlePose.getRotationPointX(),
                    aimingPose.getRotationPointX() != null ? aimingPose.getRotationPointX() : idlePose.getRotationPointX(),
                    aimProgress);
        }
        if (idlePose.getRotationPointY() != null) {
            modelPart.y = interpolate(idlePose.getRotationPointY(),
                    aimingPose.getRotationPointY() != null ? aimingPose.getRotationPointY() : idlePose.getRotationPointY(),
                    aimProgress);
        }
        if (idlePose.getRotationPointZ() != null) {
            modelPart.z = interpolate(idlePose.getRotationPointZ(),
                    aimingPose.getRotationPointZ() != null ? aimingPose.getRotationPointZ() : idlePose.getRotationPointZ(),
                    aimProgress);
        }
    }

    private float interpolate(Float start, Float end, float progress) {
        if (start == null) start = 0F;
        if (end == null) end = start;
        return start + (end - start) * progress;
    }

    private void applyFallbackPose(ModelPart rightArm, ModelPart leftArm, float aimProgress) {
        rightArm.xRot = (float) Math.toRadians(-80F);
        rightArm.yRot = (float) Math.toRadians(-10F);
        rightArm.zRot = 0F;

        leftArm.xRot = (float) Math.toRadians(-20F);
        leftArm.yRot = (float) Math.toRadians(15F);
        leftArm.zRot = 0F;
    }

    private static void copyModelAngles(ModelPart source, ModelPart target) {
        target.xRot = source.xRot;
        target.yRot = source.yRot;
        target.zRot = source.zRot;
    }
}