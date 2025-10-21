package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.entity.monster.AdjudicatorEntity;
import top.ribs.scguns.item.GunItem;

public class AdjudicatorModel<T extends AdjudicatorEntity> extends HierarchicalModel<T> implements ArmedModel {
    private final ModelPart root;
    private final ModelPart right_leg;
    private final ModelPart left_leg;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart nose;
    private final ModelPart left_ear;
    private final ModelPart right_ear;
    private final ModelPart left_arm;
    private final ModelPart right_arm;

    private final PartPose headDefault;
    private final PartPose noseDefault;
    private final PartPose leftArmDefault;
    private final PartPose rightArmDefault;
    private final PartPose leftLegDefault;
    private final PartPose rightLegDefault;

    private float attackStartTime = -1;
    private int lastAttackTimeout = 0;
    private static final float ATTACK_DURATION = 12.0f;

    public AdjudicatorModel(ModelPart root) {
        this.root = root;
        this.right_leg = root.getChild("right_leg");
        this.left_leg = root.getChild("left_leg");
        this.torso = root.getChild("torso");
        this.head = root.getChild("head");
        this.nose = this.head.getChild("nose");
        this.left_ear = this.head.getChild("left_ear");
        this.right_ear = this.head.getChild("right_ear");
        this.left_arm = root.getChild("left_arm");
        this.right_arm = root.getChild("right_arm");

        this.headDefault = this.head.storePose();
        this.noseDefault = this.nose.storePose();
        this.leftArmDefault = this.left_arm.storePose();
        this.rightArmDefault = this.right_arm.storePose();
        this.leftLegDefault = this.left_leg.storePose();
        this.rightLegDefault = this.right_leg.storePose();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 47).mirror().addBox(-2.9F, 4.2285F, -5.576F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.85F, 11.0F, 9.35F));

        PartDefinition right_leg_r1 = right_leg.addOrReplaceChild("right_leg_r1", CubeListBuilder.create().texOffs(52, 44).mirror().addBox(-1.25F, -7.3896F, -2.9787F, 3.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.75F, 5.6396F, -3.1213F, -0.7854F, 0.0F, 0.0F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(20, 47).addBox(-1.1F, 4.2285F, -5.576F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.85F, 11.0F, 9.35F));

        PartDefinition left_leg_r1 = left_leg.addOrReplaceChild("left_leg_r1", CubeListBuilder.create().texOffs(52, 44).addBox(-1.75F, -7.3896F, -2.9787F, 3.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.75F, 5.6396F, -3.1213F, -0.7854F, 0.0F, 0.0F));

        PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(-3.6332F, 7.2892F, 4.2402F));

        PartDefinition body_r1 = torso.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(30, 60).addBox(-4.0F, -0.0192F, 0.3659F, 8.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 3.25F, 1.7802F, -1.2828F, -1.5708F));

        PartDefinition body_r2 = torso.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(14, 60).addBox(-4.0F, -0.0192F, 0.3659F, 8.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2663F, 2.0F, 3.25F, 1.8588F, 1.2828F, 1.5708F));

        PartDefinition body_r3 = torso.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(54, 25).addBox(-4.0F, -0.0192F, 0.3659F, 8.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.6332F, 0.9681F, 6.7335F, 0.288F, 0.0F, 0.0F));

        PartDefinition body_r4 = torso.addOrReplaceChild("body_r4", CubeListBuilder.create().texOffs(32, 0).addBox(-3.5F, -4.0F, -2.0F, 8.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1332F, 3.0711F, 2.5543F, 1.0734F, 0.0F, 0.0F));

        PartDefinition body_r5 = torso.addOrReplaceChild("body_r5", CubeListBuilder.create().texOffs(33, 14).addBox(-3.5F, -10.75F, -1.5F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1332F, -1.9289F, 5.5543F, 1.3788F, 0.0F, 0.0F));

        PartDefinition body_r6 = torso.addOrReplaceChild("body_r6", CubeListBuilder.create().texOffs(0, 16).addBox(-4.5F, -11.25F, -1.5F, 9.0F, 9.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.6332F, 3.0711F, 2.5543F, 1.3788F, 0.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.0456F, -4.0525F, -0.3403F, 0.0F, 0.0F));

        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -1.5F, -3.5F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(74, 63).addBox(-4.0F, -1.5F, -3.5F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.0F, -5.59F, -3.5625F, 0.5236F, 0.0F, 0.0F));

        PartDefinition nose = head.addOrReplaceChild("nose", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.025F, -0.301F, -4.1306F, 0.2094F, 0.0F, 0.0F));

        PartDefinition head_r2 = nose.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(0, 67).addBox(-1.025F, -1.8907F, -7.2403F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(26, 67).addBox(-1.025F, -1.8907F, -2.2403F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.25F, -0.75F, 1.309F, 0.0F, 0.0F));

        PartDefinition head_r3 = nose.addOrReplaceChild("head_r3", CubeListBuilder.create().texOffs(72, 7).addBox(-0.975F, -2.8029F, 1.8261F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.25F, -0.75F, 0.9163F, 0.0F, 0.0F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(5.4413F, -5.071F, 1.2706F));

        PartDefinition ear_r1 = left_ear.addOrReplaceChild("ear_r1", CubeListBuilder.create().texOffs(0, 57).addBox(0.0F, -1.5F, -4.0F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5934F, 0.3378F, 0.2042F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-5.4413F, -5.071F, 1.2706F));

        PartDefinition ear_r2 = right_ear.addOrReplaceChild("ear_r2", CubeListBuilder.create().texOffs(58, 13).addBox(0.0F, -1.5F, -4.0F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5934F, -0.3378F, -0.2042F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(4.5F, 7.0F, -0.75F, 0.1833F, 0.0F, 0.0F));

        PartDefinition cube_r1 = left_arm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 32).addBox(0.4F, 0.55F, -1.6F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.75F, 2.3097F, -0.9567F, -0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r2 = left_arm.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -0.25F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.75F, 2.3097F, -0.9567F, -1.1781F, 0.0F, 0.0F));

        PartDefinition left_arm_r1 = left_arm.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(66, 49).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -0.3496F, -0.438F, -0.3655F, 0.147F, 0.3655F));

        PartDefinition left_arm_r2 = left_arm.addOrReplaceChild("left_arm_r2", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, 4.75F, -0.95F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, 3.75F, 1.05F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(66, 63).addBox(-0.5F, 1.75F, -0.75F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(14, 67).addBox(-1.0F, -4.25F, -1.25F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 3.2503F, -2.1997F, -0.3927F, 0.0F, 0.0F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.25F, 7.0F, -1.6F, 0.1309F, 0.0F, 0.0F));

        PartDefinition right_arm_r1 = right_arm.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.0F, 3.75F, 1.05F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(24, 0).mirror().addBox(-2.0F, 4.75F, -0.95F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(66, 63).mirror().addBox(-1.5F, 1.75F, -0.75F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.25F, 3.0306F, -1.4008F, -0.3927F, 0.0F, 0.0F));

        PartDefinition right_arm_r2 = right_arm.addOrReplaceChild("right_arm_r2", CubeListBuilder.create().texOffs(66, 56).addBox(-2.0F, -1.5F, -2.2F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.25F, -0.5692F, 0.3609F, -0.3655F, -0.147F, -0.3655F));

        PartDefinition right_arm_r3 = right_arm.addOrReplaceChild("right_arm_r3", CubeListBuilder.create().texOffs(60, 0).addBox(-2.0F, -3.25F, -0.95F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.25F, 1.7806F, -1.4008F, -0.3927F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.loadPose(this.headDefault);
        this.nose.loadPose(this.noseDefault);
        this.left_arm.loadPose(this.leftArmDefault);
        this.right_arm.loadPose(this.rightArmDefault);
        this.left_leg.loadPose(this.leftLegDefault);
        this.right_leg.loadPose(this.rightLegDefault);

        boolean holdingItem = !entity.getMainHandItem().isEmpty();
        boolean holdingGun = entity.getMainHandItem().getItem() instanceof GunItem;

        float legSwing = Mth.cos(limbSwing * 0.4F) * 1.0F * limbSwingAmount;
        float armSwing = Mth.cos(limbSwing * 0.4F + (float)Math.PI) * 1.0F * limbSwingAmount;

        this.right_leg.xRot += legSwing;
        this.left_leg.xRot -= legSwing;

        if (!holdingItem) {
            this.right_arm.xRot += armSwing;
            this.left_arm.xRot -= armSwing;
        } else if (!holdingGun) {
            if (entity.isAttacking() && entity.getAttackTimeout() > 0) {
                animateAttackSmooth(entity.getAttackTimeout(), ageInTicks);
            }
            this.left_arm.xRot -= armSwing;
        }

        float earTime = ageInTicks * 0.08F + limbSwing * 0.4F;
        float earAmount = 0.06F + limbSwingAmount * 0.3F;
        this.left_ear.zRot = Mth.cos(earTime * 1.1F) * earAmount;
        this.right_ear.zRot = -Mth.cos(earTime) * earAmount;

        float breathe = Mth.sin(ageInTicks * 0.12F) * 0.04F;
        this.nose.xRot += breathe;

        float clampedYaw = Mth.clamp(netHeadYaw, -60.0F, 60.0F);
        float clampedPitch = Mth.clamp(headPitch, -25.0F, 25.0F);
        this.head.yRot += clampedYaw * ((float)Math.PI / 180F);
        this.head.xRot += clampedPitch * ((float)Math.PI / 180F) * 0.5F;

        if (holdingGun) {
            this.right_arm.xRot = -1.5708F + 0.3927F;
            this.right_arm.yRot = 0.0F;
            this.right_arm.zRot = 0.0F;
        }
    }

    private void animateAttackSmooth(int attackTimeout, float ageInTicks) {
        if (attackTimeout <= 0) {
            this.right_arm.xRot = 0.0f;
            this.right_arm.yRot = 0.0f;
            attackStartTime = -1;
            lastAttackTimeout = 0;
            return;
        }

        if (attackTimeout > lastAttackTimeout || attackStartTime < 0) {
            attackStartTime = ageInTicks;
        }
        lastAttackTimeout = attackTimeout;

        float elapsedTime = (ageInTicks - attackStartTime);
        float attackProgress = elapsedTime / ATTACK_DURATION;
        attackProgress = Mth.clamp(attackProgress, 0.0f, 1.0f);

        float swingCurve = Mth.sin(attackProgress * (float)Math.PI);

        this.right_arm.xRot = -swingCurve * 1.7f;
        this.right_arm.yRot = 0.0f;
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void translateToHand(HumanoidArm pSide, PoseStack pPoseStack) {
        if (pSide == HumanoidArm.RIGHT) {
            this.right_arm.translateAndRotate(pPoseStack);

            pPoseStack.mulPose(com.mojang.math.Axis.XP.rotation(-0.3927F));

            pPoseStack.translate(-0.05, 0.55, 0.05);
            pPoseStack.scale(0.8F, 0.8F, 0.8F);
        }
    }

    @Override
    public ModelPart root() {
        return root;
    }
}