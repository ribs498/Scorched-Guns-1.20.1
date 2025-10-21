package top.ribs.scguns.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.item.GunItem;

@Mixin(HumanoidModel.class)
public abstract class MixinHumanoidModel {

    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart head;
    @Shadow public boolean crouching;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void overrideGunPose(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                 float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Mob mob)) {
            return;
        }

        ItemStack mainHand = mob.getMainHandItem();
        if (!(mainHand.getItem() instanceof GunItem)) {
            return;
        }
        if (entity instanceof Zombie || entity instanceof AbstractIllager) {
            return;
        }

        applyGunHoldingPose(mob);
    }

    private void applyGunHoldingPose(Mob mob) {
        HumanoidArm mainArm = mob.getMainArm();
        boolean rightHanded = mainArm == HumanoidArm.RIGHT;

        this.rightArm.xRot = 0;
        this.rightArm.yRot = 0;
        this.rightArm.zRot = 0;
        this.leftArm.xRot = 0;
        this.leftArm.yRot = 0;
        this.leftArm.zRot = 0;

        this.rightArm.visible = true;
        this.leftArm.visible = true;

        if (mob instanceof AbstractSkeleton) {
            applySkeletonGunPose(rightHanded);
        } else if (mob instanceof AbstractPiglin) {
            applyPiglinGunPose(rightHanded);
        } else if (mob instanceof Witch) {
            applyWitchGunPose(rightHanded);
        } else {
            applyDefaultGunPose(rightHanded);
        }

        if (this.crouching) {
            this.rightArm.xRot += 0.4F;
            this.leftArm.xRot += 0.4F;
        }
    }

    private void applyDefaultGunPose(boolean rightHanded) {
        if (rightHanded) {
            this.rightArm.xRot = (float) Math.toRadians(-90F);
            this.rightArm.yRot = (float) Math.toRadians(-5F);
            this.rightArm.zRot = 0;

            this.leftArm.xRot = (float) Math.toRadians(-85F);
            this.leftArm.yRot = (float) Math.toRadians(30F);
            this.leftArm.zRot = 0;
        } else {
            this.leftArm.xRot = (float) Math.toRadians(-90F);
            this.leftArm.yRot = (float) Math.toRadians(5F);
            this.leftArm.zRot = 0;

            this.rightArm.xRot = (float) Math.toRadians(-85F);
            this.rightArm.yRot = (float) Math.toRadians(-30F);
            this.rightArm.zRot = 0;
        }
    }

    private void applySkeletonGunPose(boolean rightHanded) {
        if (rightHanded) {
            this.rightArm.xRot = (float) Math.toRadians(-90F);
            this.rightArm.yRot = (float) Math.toRadians(-8F);
            this.rightArm.zRot = 0;

            this.leftArm.xRot = (float) Math.toRadians(-85F);
            this.leftArm.yRot = (float) Math.toRadians(35F);
            this.leftArm.zRot = (float) Math.toRadians(-5F);
        } else {
            this.leftArm.xRot = (float) Math.toRadians(-90F);
            this.leftArm.yRot = (float) Math.toRadians(8F);
            this.leftArm.zRot = 0;

            this.rightArm.xRot = (float) Math.toRadians(-85F);
            this.rightArm.yRot = (float) Math.toRadians(-35F);
            this.rightArm.zRot = (float) Math.toRadians(5F);
        }
    }

    private void applyPiglinGunPose(boolean rightHanded) {
        if (rightHanded) {
            this.rightArm.xRot = (float) Math.toRadians(-90F);
            this.rightArm.yRot = (float) Math.toRadians(-5F);
            this.rightArm.zRot = 0;

            this.leftArm.xRot = (float) Math.toRadians(-87F);
            this.leftArm.yRot = (float) Math.toRadians(28F);
            this.leftArm.zRot = 0;
        } else {
            this.leftArm.xRot = (float) Math.toRadians(-90F);
            this.leftArm.yRot = (float) Math.toRadians(5F);
            this.leftArm.zRot = 0;

            this.rightArm.xRot = (float) Math.toRadians(-87F);
            this.rightArm.yRot = (float) Math.toRadians(-28F);
            this.rightArm.zRot = 0;
        }
    }

    private void applyWitchGunPose(boolean rightHanded) {
        if (rightHanded) {
            this.rightArm.xRot = (float) Math.toRadians(-90F);
            this.rightArm.yRot = (float) Math.toRadians(-7F);
            this.rightArm.zRot = 0;

            this.leftArm.xRot = (float) Math.toRadians(-85F);
            this.leftArm.yRot = (float) Math.toRadians(30F);
            this.leftArm.zRot = 0;
        } else {
            this.leftArm.xRot = (float) Math.toRadians(-90F);
            this.leftArm.yRot = (float) Math.toRadians(7F);
            this.leftArm.zRot = 0;

            this.rightArm.xRot = (float) Math.toRadians(-85F);
            this.rightArm.yRot = (float) Math.toRadians(-30F);
            this.rightArm.zRot = 0;
        }
    }
}