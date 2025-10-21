package top.ribs.scguns.mixin.client;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.item.GunItem;

@Mixin(IllagerModel.class)
public abstract class MixinIllagerModel {

    @Shadow private ModelPart rightArm;
    @Shadow private ModelPart leftArm;
    @Shadow private ModelPart arms;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/AbstractIllager;FFFFF)V", at = @At("TAIL"))
    private void overrideIllagerGunPose(AbstractIllager entity, float limbSwing, float limbSwingAmount,
                                        float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ItemStack mainHand = entity.getMainHandItem();
        if (!(mainHand.getItem() instanceof GunItem)) {
            return;
        }

        this.arms.visible = false;
        this.leftArm.visible = true;
        this.rightArm.visible = true;

        applyIllagerGunPose(entity);
    }

    private void applyIllagerGunPose(AbstractIllager mob) {
        HumanoidArm mainArm = mob.getMainArm();
        boolean rightHanded = mainArm == HumanoidArm.RIGHT;

        this.rightArm.x = -5.0F;
        this.rightArm.y = 2.0F;
        this.rightArm.z = 0.0F;
        this.leftArm.x = 5.0F;
        this.leftArm.y = 2.0F;
        this.leftArm.z = 0.0F;

        this.rightArm.xRot = 0;
        this.rightArm.yRot = 0;
        this.rightArm.zRot = 0;
        this.leftArm.xRot = 0;
        this.leftArm.yRot = 0;
        this.leftArm.zRot = 0;

        if (rightHanded) {
            this.rightArm.xRot = (float) Math.toRadians(-90F);
            this.rightArm.yRot = (float) Math.toRadians(-10F);
            this.rightArm.zRot = 0;

            this.leftArm.xRot = (float) Math.toRadians(-85F);
            this.leftArm.yRot = (float) Math.toRadians(30F);
            this.leftArm.zRot = 0;
        } else {
            this.leftArm.xRot = (float) Math.toRadians(-90F);
            this.leftArm.yRot = (float) Math.toRadians(10F);
            this.leftArm.zRot = 0;

            this.rightArm.xRot = (float) Math.toRadians(-85F);
            this.rightArm.yRot = (float) Math.toRadians(-30F);
            this.rightArm.zRot = 0;
        }

    }
}