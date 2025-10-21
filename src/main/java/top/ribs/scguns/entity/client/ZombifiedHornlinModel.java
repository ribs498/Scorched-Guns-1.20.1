package top.ribs.scguns.entity.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import top.ribs.scguns.entity.monster.ZombifiedHornlinEntity;
import top.ribs.scguns.item.GunItem;

public class ZombifiedHornlinModel extends HumanoidModel<ZombifiedHornlinEntity> {
    private final ModelPart leftEar;
    private final ModelPart rightEar;

    private final PartPose headDefault;
    private final PartPose bodyDefault;
    private final PartPose leftArmDefault;
    private final PartPose rightArmDefault;

    public ZombifiedHornlinModel(ModelPart root) {
        super(root);
        this.leftEar = this.head.getChild("left_ear");
        this.rightEar = this.head.getChild("right_ear");

        this.headDefault = this.head.storePose();
        this.bodyDefault = this.body.storePose();
        this.leftArmDefault = this.leftArm.storePose();
        this.rightArmDefault = this.rightArm.storePose();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(36, 50).addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(46, 12).addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(50, 12).addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("head_r1",
                CubeListBuilder.create().texOffs(46, 50).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.8F, -7.75F, -4.75F, -0.3927F, 0.0F, 0.0F));

        head.addOrReplaceChild("head_r2",
                CubeListBuilder.create().texOffs(38, 12).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.15F, -5.0F, -4.375F, -0.4215F, 0.3614F, -0.1572F));

        head.addOrReplaceChild("left_ear",
                CubeListBuilder.create()
                        .texOffs(16, 49).addBox(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 44).addBox(-1.0F, 3.75F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.5F, -6.0F, 0.0F, 0.0F, 0.0F, -0.6109F));

        head.addOrReplaceChild("right_ear",
                CubeListBuilder.create().texOffs(26, 49).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.5F, -6.0F, 0.0F, 0.0F, 0.0F, 0.6109F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.5F, 6.0F, -4.0F, 11.0F, 8.0F, 8.0F, new CubeDeformation(0.2F))
                        .texOffs(0, 32).addBox(-5.5F, 14.0F, -4.0F, 11.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(36, 16).addBox(-5.0F, 0.0F, -3.5F, 10.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(38, 39).addBox(-5.0F, -1.0F, -2.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 44).addBox(-4.0F, 5.0F, -1.25F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 44).addBox(-4.0F, -2.0F, -1.25F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 1.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(38, 39).mirror().addBox(0.0F, -1.0F, -2.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(0, 44).mirror().addBox(0.0F, 5.0F, -1.25F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(16, 44).mirror().addBox(0.0F, -2.0F, -1.25F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(5.0F, 1.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(53, 33).addBox(-3.0F, 7.0F, -2.5F, 5.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(54, 5).addBox(-3.0F, 1.0F, -3.5F, 5.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(53, 33).mirror().addBox(-2.0F, 7.0F, -2.5F, 5.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(54, 5).mirror().addBox(-2.0F, 1.0F, -3.5F, 5.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(ZombifiedHornlinEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.loadPose(this.headDefault);
        this.body.loadPose(this.bodyDefault);
        this.leftArm.loadPose(this.leftArmDefault);
        this.rightArm.loadPose(this.rightArmDefault);

        boolean isEating = entity.isEatingGold();
        boolean isPreparing = entity.isPreparingToEat();
        boolean holdingGun = entity.getMainHandItem().getItem() instanceof GunItem;

        if (!isEating && !isPreparing) {
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        } else {
            this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
            this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
            this.rightLeg.yRot = 0.0F;
            this.leftLeg.yRot = 0.0F;
            this.rightLeg.zRot = 0.0F;
            this.leftLeg.zRot = 0.0F;
        }

        float earWiggle = ((float)Math.PI / 6F);
        float earTime = ageInTicks * 0.08F + limbSwing * 0.4F;
        float earAmount = 0.06F + limbSwingAmount * 0.3F;
        this.leftEar.zRot = -earWiggle - Mth.cos(earTime * 1.1F) * earAmount;
        this.rightEar.zRot = earWiggle + Mth.cos(earTime * 0.9F) * earAmount;

        if (isEating || isPreparing) {
            animateEating(entity, ageInTicks, isEating, isPreparing);
        } else if (holdingGun) {
            this.rightArm.xRot = -1.5708F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;

            this.leftArm.xRot = -1.1F;
            this.leftArm.yRot = 0.8F;
            this.leftArm.zRot = 0.5F;
            this.leftArm.x -= 1.0F;
        }
    }

    private void animateEating(ZombifiedHornlinEntity entity, float ageInTicks, boolean isEating, boolean isPreparing) {
        if (isPreparing) {
            float progress = entity.getHeldFoodItem().isEmpty() ? 0.0F : 1.0F;

            this.leftArm.xRot = Mth.lerp(progress, 0.0F, -1.8F);
            this.leftArm.yRot = Mth.lerp(progress, 0.0F, 0.4F);
            this.leftArm.zRot = Mth.lerp(progress, 0.0F, 0.3F);

            this.head.xRot += Mth.lerp(progress, 0.0F, 0.15F);

        } else if (isEating) {
            this.leftArm.xRot = -1.8F;
            this.leftArm.yRot = 0.4F;
            this.leftArm.zRot = 0.3F;

            float eatBob = Mth.sin(ageInTicks * 0.35F) * 0.06F;
            this.leftArm.xRot += eatBob;

            this.head.xRot += 0.15F + Mth.sin(ageInTicks * 0.3F) * 0.04F;

            float zombieEarWiggle = Mth.sin(ageInTicks * 0.4F) * 0.12F;
            this.leftEar.zRot += zombieEarWiggle;
            this.rightEar.zRot -= zombieEarWiggle;
        }
    }
}