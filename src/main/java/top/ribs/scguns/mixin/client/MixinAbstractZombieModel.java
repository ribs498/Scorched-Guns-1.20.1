package top.ribs.scguns.mixin.client;

import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.item.GunItem;

@Mixin(AbstractZombieModel.class)
public abstract class MixinAbstractZombieModel extends HumanoidModel<Monster> {

    public MixinAbstractZombieModel(ModelPart pRoot) {
        super(pRoot);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/Monster;FFFFF)V", at = @At("TAIL"))
    private void overrideZombieGunPose(Monster entity, float limbSwing, float limbSwingAmount,
                                       float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ItemStack mainHand = entity.getMainHandItem();
        if (!(mainHand.getItem() instanceof GunItem)) {
            return;
        }
        applyZombieGunPose(entity);
    }

    private void applyZombieGunPose(Monster mob) {
        HumanoidArm mainArm = mob.getMainArm();
        boolean rightHanded = mainArm == HumanoidArm.RIGHT;
        this.rightArm.xRot = 0;
        this.rightArm.yRot = 0;
        this.rightArm.zRot = 0;
        this.leftArm.xRot = 0;
        this.leftArm.yRot = 0;
        this.leftArm.zRot = 0;

        if (rightHanded) {
            this.rightArm.xRot = (float) Math.toRadians(-90F);
            this.rightArm.yRot = (float) Math.toRadians(-5F);
            this.rightArm.zRot = 0;
            this.leftArm.xRot = (float) Math.toRadians(-88F);
            this.leftArm.yRot = (float) Math.toRadians(30F);
            this.leftArm.zRot = 0;
        } else {
            this.leftArm.xRot = (float) Math.toRadians(-90F);
            this.leftArm.yRot = (float) Math.toRadians(5F);
            this.leftArm.zRot = 0;
            this.rightArm.xRot = (float) Math.toRadians(-88F);
            this.rightArm.yRot = (float) Math.toRadians(-25F);
            this.rightArm.zRot = 0;
        }
        if (this.crouching) {
            this.rightArm.xRot += 0.4F;
            this.leftArm.xRot += 0.4F;
        }
    }
}