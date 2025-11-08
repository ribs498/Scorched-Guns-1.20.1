package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.entity.monster.BlundererEntity;
import top.ribs.scguns.item.GunItem;

public class BlundererModel<T extends BlundererEntity> extends HierarchicalModel<T> implements ArmedModel, HeadedModel {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart torso;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    private final PartPose headDefault;
    private final PartPose torsoDefault;
    private final PartPose leftArmDefault;
    private final PartPose rightArmDefault;
    private final PartPose leftLegDefault;
    private final PartPose rightLegDefault;


    public BlundererModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.torso = root.getChild("torso");
        this.left_arm = root.getChild("left_arm");
        this.right_arm = root.getChild("right_arm");
        this.left_leg = root.getChild("left_leg");
        this.right_leg = root.getChild("right_leg");

        this.headDefault = this.head.storePose();
        this.torsoDefault = this.torso.storePose();
        this.leftArmDefault = this.left_arm.storePose();
        this.rightArmDefault = this.right_arm.storePose();
        this.leftLegDefault = this.left_leg.storePose();
        this.rightLegDefault = this.right_leg.storePose();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.2071F, -8.25F, -2.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 80).addBox(-4.0F, -8.5F, -3.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.275F))
                .texOffs(36, 82).addBox(-4.0F, -8.5F, -3.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2071F, -2.25F, -0.25F, 0.0F, 0.0F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(68, 40).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.2071F, -4.75F, 2.75F, -1.5708F, 0.7854F, -1.5708F));

        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(44, 63).addBox(-1.0F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.2071F, -5.45F, 2.75F, -1.5708F, 0.7854F, -1.5708F));

        PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(40, 63).addBox(0.0F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7929F, -5.45F, 2.75F, -1.5708F, -0.7854F, 1.5708F));

        PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(62, 40).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7929F, -4.75F, 2.75F, -1.5708F, -0.7854F, 1.5708F));

        PartDefinition cube_r6 = head.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(104, 8).addBox(-1.0F, -0.5F, -4.0F, 6.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7929F, -5.25F, 0.75F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r7 = head.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(80, 65).addBox(-5.0F, -1.5F, -4.0F, 10.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7929F, -2.25F, 0.75F, 0.0F, -1.5708F, 0.0F));

        PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(58, 43).addBox(-8.0F, -19.0F, -5.5F, 15.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(104, 0).addBox(-7.5F, -21.2855F, 0.0F, 14.0F, 3.0F, 5.0F, new CubeDeformation(0.025F))
                .texOffs(0, 24).addBox(-8.0F, -6.0F, -10.5F, 15.0F, 3.0F, 16.0F, new CubeDeformation(0.25F))
                .texOffs(0, 43).addBox(-7.5F, -8.0F, -10.0F, 14.0F, 5.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, -8.0F, -10.5F, 15.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(43, 43).addBox(-4.0F, -8.0F, 5.5F, 7.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 5).addBox(-4.0F, -7.0F, 7.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(62, 21).addBox(-3.0F, 0.0F, -10.5F, 5.0F, 4.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(76, 100).addBox(-8.0F, 0.0F, 1.5F, 15.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(62, 0).mirror().addBox(-0.1F, 0.0F, -9.5F, 7.0F, 7.0F, 14.0F, new CubeDeformation(0.025F)).mirror(false)
                .texOffs(62, 0).addBox(-7.9F, 0.0F, -9.5F, 7.0F, 7.0F, 14.0F, new CubeDeformation(0.025F)), PartPose.offset(0.5F, 10.0F, -1.5F));

        PartDefinition cube_r8 = torso.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(38, 100).addBox(-7.0F, -2.5F, -2.5F, 14.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -17.0F, 5.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r9 = torso.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 100).addBox(-7.0F, -2.5F, -2.5F, 14.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -17.75F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r10 = torso.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(40, 65).addBox(-8.5F, -7.0F, -6.0F, 15.0F, 12.0F, 5.0F, new CubeDeformation(-0.025F)), PartPose.offsetAndRotation(0.5F, -10.1637F, 6.8133F, -0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r11 = torso.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 24).mirror().addBox(1.625F, 3.5F, -6.125F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 7).mirror().addBox(1.625F, 2.5F, -6.875F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.375F, -9.5711F, -10.1091F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r12 = torso.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 10).addBox(-6.875F, 2.5F, -6.875F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 28).addBox(-6.875F, 3.5F, -6.125F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.375F, -9.5711F, -10.1091F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r13 = torso.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 32).addBox(1.3155F, -1.9116F, 1.6112F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 32).addBox(-3.3155F, -2.0884F, -3.6112F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9461F, -15.377F, -7.5209F, -0.421F, 0.6473F, -0.7081F));

        PartDefinition cube_r14 = torso.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(22, 110).addBox(-0.8F, -1.0F, -7.0F, 1.0F, 2.0F, 14.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(7.2863F, -16.2995F, 0.9311F, -0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r15 = torso.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(102, 31).addBox(-7.5F, -5.0F, -6.0F, 16.0F, 2.0F, 14.0F, new CubeDeformation(0.225F)), PartPose.offsetAndRotation(0.5F, -10.2694F, -2.649F, -0.3655F, -0.147F, -0.3655F));

        PartDefinition cube_r16 = torso.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 63).addBox(-8.5F, -7.0F, -6.0F, 15.0F, 12.0F, 5.0F, new CubeDeformation(-0.025F)), PartPose.offsetAndRotation(0.5F, -10.2694F, -2.649F, -0.3927F, 0.0F, 0.0F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(76, 109).addBox(-5.2807F, 7.7955F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(98, 109).addBox(-5.2807F, 0.1955F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(108, 78).addBox(-7.5307F, 6.1955F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.25F, -9.0F, -1.5F));

        PartDefinition cube_r17 = left_arm.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 110).addBox(-2.5307F, -7.3045F, -4.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(-0.025F)), PartPose.offsetAndRotation(-2.6864F, 14.3849F, 0.9815F, -0.0013F, 0.0036F, -0.3055F));

        PartDefinition cube_r18 = left_arm.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(102, 21).addBox(-5.0F, 0.75F, -4.5F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(108, 88).addBox(-4.0F, -3.25F, -3.5F, 8.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.5F, 0.0F, 0.0F, 0.0F, -0.3927F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(76, 109).mirror().addBox(0.2807F, 7.7955F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(98, 109).mirror().addBox(0.2807F, 0.1955F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(108, 78).mirror().addBox(-0.4693F, 6.1955F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(7.25F, -9.0F, -1.5F));

        PartDefinition cube_r19 = right_arm.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 110).mirror().addBox(-2.4693F, -7.3045F, -4.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(-0.025F)).mirror(false), PartPose.offsetAndRotation(2.6864F, 14.3849F, 0.9815F, -0.0013F, -0.0036F, 0.3055F));

        PartDefinition cube_r20 = right_arm.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(102, 21).mirror().addBox(-4.0F, 0.75F, -4.5F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(108, 88).mirror().addBox(-4.0F, -3.25F, -3.5F, 8.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.3927F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(80, 78).addBox(-3.0333F, 0.0F, -2.3333F, 6.0F, 14.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.5333F, 5.5F, -3.3333F, 5.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.9667F, 10.0F, -5.1667F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(80, 78).mirror().addBox(-2.9667F, 0.0F, -2.3333F, 6.0F, 14.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 0).mirror().addBox(-2.4667F, 5.5F, -3.3333F, 5.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.9667F, 10.0F, -5.1667F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.loadPose(this.headDefault);
        this.torso.loadPose(this.torsoDefault);
        this.left_arm.loadPose(this.leftArmDefault);
        this.right_arm.loadPose(this.rightArmDefault);
        this.left_leg.loadPose(this.leftLegDefault);
        this.right_leg.loadPose(this.rightLegDefault);

        boolean holdingItem = !entity.getMainHandItem().isEmpty();
        boolean holdingGun = entity.getMainHandItem().getItem() instanceof GunItem;

        float legSwing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        float armSwing = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.0F * limbSwingAmount;

        this.right_leg.xRot += legSwing;
        this.left_leg.xRot -= legSwing;

        float clampedYaw = Mth.clamp(netHeadYaw, -60.0F, 60.0F);
        float clampedPitch = Mth.clamp(headPitch, -45.0F, 45.0F);

        if (entity.isTossingGrenade()) {
            int tossTimeout = entity.getGrenadeTossTimeout();

            if (tossTimeout > 6) {
                float windUp = (12 - tossTimeout) / 6.0f;
                this.right_arm.xRot = -2.8f + (windUp * 0.3f);
                this.right_arm.zRot = 0.5f;
            } else {
                float throwMotion = (6 - tossTimeout) / 6.0f;
                float curve = Mth.sin(throwMotion * (float)Math.PI);
                this.right_arm.xRot = -2.8f + (curve * 3.5f);
                this.right_arm.zRot = 0.5f - (throwMotion * 0.3f);
            }

            this.torso.yRot = -0.2f;
            this.left_arm.xRot = -0.5f;
            this.left_arm.zRot = -0.2f;

        } else if (entity.isCharging()) {
            float chargeIntensity = Mth.sin(ageInTicks * 0.8F) * 0.15F;

            this.torso.xRot = -0.4F + chargeIntensity;

            this.left_arm.xRot = -1.2F + chargeIntensity;
            this.left_arm.zRot = -0.3F;
            this.right_arm.xRot = -1.2F + chargeIntensity;
            this.right_arm.zRot = 0.3F;

            float fastLegSwing = Mth.cos(limbSwing * 1.2F) * 2.0F * limbSwingAmount;
            this.right_leg.xRot = fastLegSwing;
            this.left_leg.xRot = -fastLegSwing;

            this.head.xRot = -0.2F;
        } else if (!holdingItem) {
            this.right_arm.xRot += armSwing;
            this.left_arm.xRot -= armSwing;
        } else if (!holdingGun) {
            if (entity.isAttacking()) {
                animateAttackSmooth(ageInTicks);
            } else {
                this.left_arm.xRot -= armSwing;
            }
            this.right_arm.xRot += armSwing;
        } else {
            float aimPitchRadians = clampedPitch * ((float)Math.PI / 180F);

            this.left_arm.xRot = -1.5708F + aimPitchRadians;
            this.left_arm.yRot = 0.0F;
            this.left_arm.zRot = 0.0F;

            this.right_arm.xRot = armSwing;

            this.torso.xRot += aimPitchRadians * 0.4F;
        }

        this.head.yRot += clampedYaw * ((float)Math.PI / 180F);
        this.head.xRot += clampedPitch * ((float)Math.PI / 180F) * 0.5F;

        float breathe = Mth.sin(ageInTicks * 0.08F) * 0.03F;
        this.torso.y += breathe;
    }

    private void animateAttackSmooth(float ageInTicks) {
        float attackProgress = (ageInTicks % 20) / 20.0f;
        float swingCurve = Mth.sin(attackProgress * (float)Math.PI);

        this.left_arm.xRot = -swingCurve * 2.0f;
        this.left_arm.yRot = 0.0f;
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void translateToHand(HumanoidArm pSide, PoseStack pPoseStack) {
        if (pSide == HumanoidArm.LEFT) {
            this.left_arm.translateAndRotate(pPoseStack);

            pPoseStack.translate(-0.2, 0.25, -0.05);
            pPoseStack.scale(1.1F, 1.1F, 1.1F);
        }
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }
}