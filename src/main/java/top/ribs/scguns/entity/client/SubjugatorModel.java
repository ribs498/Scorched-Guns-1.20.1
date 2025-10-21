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
import top.ribs.scguns.entity.monster.SubjugatorEntity;
import top.ribs.scguns.item.GunItem;

public class SubjugatorModel<T extends SubjugatorEntity> extends HierarchicalModel<T> implements ArmedModel {
    private final ModelPart root;
    private final ModelPart left_leg;
    private final ModelPart right_leg;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart left_ear;
    private final ModelPart right_ear;
    private final ModelPart left_arm;
    private final ModelPart right_arm;

    private final PartPose headDefault;
    private final PartPose torsoDefault;
    private final PartPose leftArmDefault;
    private final PartPose rightArmDefault;
    private final PartPose leftLegDefault;
    private final PartPose rightLegDefault;

    private float attackStartTime = -1;
    private int lastAttackTimeout = 0;
    private static final float ATTACK_DURATION = 12.0f;

    public SubjugatorModel(ModelPart root) {
        this.root = root;
        this.left_leg = root.getChild("left_leg");
        this.right_leg = root.getChild("right_leg");
        this.torso = root.getChild("torso");
        this.head = root.getChild("head");
        this.left_ear = this.head.getChild("left_ear");
        this.right_ear = this.head.getChild("right_ear");
        this.left_arm = root.getChild("left_arm");
        this.right_arm = root.getChild("right_arm");

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

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(20, 49).addBox(-1.6F, -0.5215F, -1.576F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(32, 28).addBox(-1.6F, 6.4785F, -1.576F, 5.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.55F, 8.75F, -0.4F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 49).mirror().addBox(-2.4F, -0.5215F, -1.576F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(32, 28).mirror().addBox(-3.4F, 6.4785F, -1.576F, 5.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.65F, 8.75F, -0.4F));

        PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 28).addBox(-0.8668F, -10.924F, -5.5044F, 9.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(32, 41).addBox(-0.3668F, -10.924F, 1.4956F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-0.3668F, -3.7551F, -5.0693F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.6332F, 8.2892F, 2.4902F));

        PartDefinition cube_r1 = torso.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(0.4F, 0.55F, -1.6F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.8831F, -9.9795F, -5.6969F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r2 = torso.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 20).addBox(-0.5F, -0.25F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.8831F, -9.9795F, -5.6969F, 0.0F, 1.5708F, 0.7854F));

        PartDefinition body_r1 = torso.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(37, 59).addBox(-1.5056F, 2.287F, 3.7218F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7501F, -1.2181F, -3.5637F, 0.0F, -1.5708F, 0.2094F));

        PartDefinition body_r2 = torso.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(60, 0).addBox(-3.4944F, 2.4699F, 3.2833F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7826F, -1.1905F, -2.5637F, 0.0F, 1.5708F, -0.288F));

        PartDefinition body_r3 = torso.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(52, 54).addBox(-4.1289F, 2.5388F, 4.4934F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7621F, -1.6856F, -3.8566F, 0.1309F, 0.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.59F, -3.5625F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16).addBox(-4.5F, -2.59F, -3.9475F, 9.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(24, 0).addBox(-1.5F, -5.015F, -4.0475F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(32, 10).addBox(-4.5F, -4.59F, 2.0525F, 9.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.9544F, -0.0525F));

        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(20, 43).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -4.015F, -3.6475F, 0.0F, 0.0F, 0.7854F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(5.4413F, -5.571F, 2.8206F));

        PartDefinition ear_r1 = left_ear.addOrReplaceChild("ear_r1", CubeListBuilder.create().texOffs(0, 51).addBox(0.0F, -1.5F, -4.0F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.25F, 0.45F, 0.0F, 0.3923F, 0.0181F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-5.4413F, -5.571F, 2.8206F));

        PartDefinition ear_r2 = right_ear.addOrReplaceChild("ear_r2", CubeListBuilder.create().texOffs(52, 44).addBox(0.0F, -1.5F, -4.0F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.25F, 0.45F, 0.0F, -0.3923F, -0.0181F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(4.5F, -1.25F, 0.25F));

        PartDefinition left_arm_r1 = left_arm.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(56, 18).mirror().addBox(-0.75F, 2.875F, -1.7F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(1.25F, 4.3282F, 0.2387F, 0.0F, 0.0F, 0.0F));

        PartDefinition cube_r3 = left_arm.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(14, 51).addBox(0.2F, -1.4902F, -1.0348F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2F, 3.4783F, 0.4904F, 0.0F, 0.0F, 0.0F));

        PartDefinition cube_r4 = left_arm.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(30, 60).addBox(-0.7F, -2.0922F, -2.043F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2F, 3.4783F, 0.4904F, -0.7854F, 0.0F, 0.0F));

        PartDefinition left_arm_r2 = left_arm.addOrReplaceChild("left_arm_r2", CubeListBuilder.create().texOffs(36, 15).addBox(-4.2673F, -7.0235F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, 5.3282F, 0.0637F, 0.0F, 0.0F, 0.3927F));

        PartDefinition left_arm_r3 = left_arm.addOrReplaceChild("left_arm_r3", CubeListBuilder.create().texOffs(60, 6).addBox(1.0F, -1.5F, 1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 7.7032F, 1.8637F, 0.0F, 1.5708F, 0.0F));

        PartDefinition left_arm_r4 = left_arm.addOrReplaceChild("left_arm_r4", CubeListBuilder.create().texOffs(14, 60).addBox(-1.25F, -0.125F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(36, 49).addBox(-1.75F, -6.125F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, 5.3282F, 0.0637F, 0.0F, 0.0F, 0.0F));

        PartDefinition left_arm_r5 = left_arm.addOrReplaceChild("left_arm_r5", CubeListBuilder.create().texOffs(52, 36).addBox(-1.25F, 3.875F, -2.5F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.25F, 5.3282F, 0.5637F, 0.0F, 0.0F, 0.0F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-4.5F, -1.25F, 0.25F));

        PartDefinition cube_r5 = right_arm.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(26, 60).addBox(-0.2F, -1.4902F, -1.0348F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.2F, 3.4783F, 0.4904F, 0.0F, 0.0F, 0.0F));

        PartDefinition cube_r6 = right_arm.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(6, 61).addBox(-0.3F, -2.0922F, -2.043F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.2F, 3.4783F, 0.4904F, -0.7854F, 0.0F, 0.0F));

        PartDefinition right_arm_r1 = right_arm.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(0, 43).addBox(-0.7327F, -7.0235F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.75F, 5.3282F, 0.0637F, 0.0F, 0.0F, -0.3927F));

        PartDefinition right_arm_r2 = right_arm.addOrReplaceChild("right_arm_r2", CubeListBuilder.create().texOffs(56, 18).addBox(-2.25F, 2.875F, -1.7F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-1.25F, 4.3282F, 0.2387F, 0.0F, 0.0F, 0.0F));

        PartDefinition right_arm_r3 = right_arm.addOrReplaceChild("right_arm_r3", CubeListBuilder.create().texOffs(0, 61).addBox(-3.0F, -1.5F, 1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 7.7032F, 1.8637F, 0.0F, -1.5708F, 0.0F));

        PartDefinition right_arm_r4 = right_arm.addOrReplaceChild("right_arm_r4", CubeListBuilder.create().texOffs(50, 23).addBox(-1.75F, -2.125F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(56, 10).addBox(-2.25F, -6.125F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.75F, 5.3282F, 0.0637F, 0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.loadPose(this.headDefault);
        this.torso.loadPose(this.torsoDefault);
        this.left_arm.loadPose(this.leftArmDefault);
        this.right_arm.loadPose(this.rightArmDefault);
        this.left_leg.loadPose(this.leftLegDefault);
        this.right_leg.loadPose(this.rightLegDefault);

        boolean holdingGun = entity.getMainHandItem().getItem() instanceof GunItem;

        float legSwing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        float armSwing = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.0F * limbSwingAmount;

        this.right_leg.xRot += legSwing;
        this.left_leg.xRot -= legSwing;

        if (!holdingGun) {
            if (entity.isAttacking() && entity.getAttackTimeout() > 0) {
                animateAttackSmooth(entity.getAttackTimeout(), ageInTicks);
            }
        }
        this.left_arm.xRot -= armSwing;

        float earTime = ageInTicks * 0.08F + limbSwing * 0.4F;
        float earAmount = 0.05F + limbSwingAmount * 0.2F;
        this.left_ear.zRot = Mth.cos(earTime * 1.1F) * earAmount;
        this.right_ear.zRot = -Mth.cos(earTime) * earAmount;

        float clampedYaw = Mth.clamp(netHeadYaw, -60.0F, 60.0F);
        float clampedPitch = Mth.clamp(headPitch, -25.0F, 25.0F);
        this.head.yRot += clampedYaw * ((float)Math.PI / 180F);
        this.head.xRot += clampedPitch * ((float)Math.PI / 180F) * 0.5F;

        if (holdingGun) {
            this.right_arm.xRot = -1.5708F;
            this.right_arm.yRot = 0.0F;
            this.right_arm.zRot = 0.0F;

            this.left_arm.xRot = -1.3F;
            this.left_arm.yRot = 0.6F;
            this.left_arm.zRot = 0.3F;
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

        this.right_arm.xRot = -swingCurve * 2.0f;
        this.right_arm.yRot = 0.0f;
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void translateToHand(HumanoidArm pSide, PoseStack pPoseStack) {
        if (pSide == HumanoidArm.RIGHT) {
            this.right_arm.translateAndRotate(pPoseStack);
            pPoseStack.translate(-0.05, 0.25, -0.0);
            pPoseStack.scale(1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public ModelPart root() {
        return root;
    }
}