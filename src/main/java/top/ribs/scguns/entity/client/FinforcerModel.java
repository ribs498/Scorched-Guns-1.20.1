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
import top.ribs.scguns.entity.monster.FinforcerEntity;
import top.ribs.scguns.item.GunItem;

public class FinforcerModel<T extends FinforcerEntity> extends HierarchicalModel<T> implements ArmedModel {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart tail;
    private final ModelPart tail_fin;
    private final ModelPart back_fin;

    private final PartPose bodyDefault;
    private final PartPose headDefault;
    private final PartPose tailDefault;
    private final PartPose rightArmDefault;
    private final PartPose leftArmDefault;
    private final PartPose backFinDefault;

    private float attackStartTime = -1;
    private int lastAttackTimeout = 0;
    private static final float ATTACK_DURATION = 12.0f;

    public FinforcerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.right_arm = this.body.getChild("right_arm");
        this.left_arm = this.body.getChild("left_arm");
        this.tail = root.getChild("tail");
        this.tail_fin = this.tail.getChild("tail_fin");
        this.back_fin = this.body.getChild("back_fin");

        this.bodyDefault = this.body.storePose();
        this.headDefault = this.head.storePose();
        this.tailDefault = this.tail.storePose();
        this.rightArmDefault = this.right_arm.storePose();
        this.leftArmDefault = this.left_arm.storePose();
        this.backFinDefault = this.back_fin.storePose();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, -3.0F));

        PartDefinition body_r1 = body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, 7.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -19.5F, 2.25F, -1.5708F, 0.0F, 0.0F));

        PartDefinition body_r2 = body.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(42, 10).addBox(-5.0F, -8.0F, 8.0F, 9.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -20.0F, 2.5F, -1.5708F, 0.0F, 0.0F));

        PartDefinition body_r3 = body.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(42, 0).addBox(-3.5F, -3.5F, -2.5F, 7.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -23.75F, 5.0F, -2.3562F, 0.0F, 0.0F));

        PartDefinition body_r4 = body.addOrReplaceChild("body_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -3.0F, 8.0F, 7.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -20.0F, 3.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 20).addBox(-4.5F, -7.6F, -6.25F, 9.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(38, 27).addBox(-4.5F, -3.6F, -1.25F, 9.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(32, 34).addBox(-4.0F, -7.5F, -5.5F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(-1.5F, -5.5F, 2.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 51).addBox(-1.0F, -2.75F, -9.5F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 51).addBox(-1.0F, 0.0F, -8.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -23.5F, 4.5F));

        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(62, 52).addBox(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(42, 62).addBox(7.0F, 0.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -1.75F, -6.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition head_r2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(36, 62).addBox(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.9142F, -1.75F, -5.3358F, -0.7854F, 0.7854F, 0.0F));

        PartDefinition head_r3 = head.addOrReplaceChild("head_r3", CubeListBuilder.create().texOffs(54, 61).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.1488F, -1.75F, -3.488F, 2.3562F, 1.1781F, 3.1416F));

        PartDefinition head_r4 = head.addOrReplaceChild("head_r4", CubeListBuilder.create().texOffs(62, 10).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3835F, -1.75F, 0.3597F, 0.0F, 1.5708F, 0.7854F));

        PartDefinition head_r5 = head.addOrReplaceChild("head_r5", CubeListBuilder.create().texOffs(62, 54).addBox(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3835F, -2.4077F, 3.8663F, 0.7854F, 1.1781F, 1.5708F));

        PartDefinition head_r6 = head.addOrReplaceChild("head_r6", CubeListBuilder.create().texOffs(62, 56).addBox(0.9068F, -0.3533F, -4.2073F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.4743F, 4.3152F, 1.8744F, 0.5797F, 2.1488F));

        PartDefinition head_r7 = head.addOrReplaceChild("head_r7", CubeListBuilder.create().texOffs(62, 50).addBox(-2.9068F, -0.3533F, -4.2073F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.4743F, 4.3152F, 1.8744F, -0.5797F, -2.1488F));

        PartDefinition head_r8 = head.addOrReplaceChild("head_r8", CubeListBuilder.create().texOffs(48, 62).addBox(-2.0F, 0.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3835F, -2.4077F, 3.8663F, 0.7854F, -1.1781F, -1.5708F));

        PartDefinition head_r9 = head.addOrReplaceChild("head_r9", CubeListBuilder.create().texOffs(30, 60).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3835F, -1.75F, 0.3597F, 0.0F, -1.5708F, -0.7854F));

        PartDefinition head_r10 = head.addOrReplaceChild("head_r10", CubeListBuilder.create().texOffs(40, 56).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.1488F, -1.75F, -3.488F, 2.3562F, -1.1781F, -3.1416F));

        PartDefinition head_r11 = head.addOrReplaceChild("head_r11", CubeListBuilder.create().texOffs(30, 62).addBox(-2.0F, 0.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9142F, -1.75F, -5.3358F, -0.7854F, -0.7854F, 0.0F));

        PartDefinition head_r12 = head.addOrReplaceChild("head_r12", CubeListBuilder.create().texOffs(8, 60).addBox(-3.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -3.0F, 4.35F, -0.7854F, 0.0F, 0.0F));

        PartDefinition head_r13 = head.addOrReplaceChild("head_r13", CubeListBuilder.create().texOffs(26, 51).addBox(2.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -2.7F, 4.45F, -0.7854F, 0.0F, 0.0F));

        PartDefinition head_r14 = head.addOrReplaceChild("head_r14", CubeListBuilder.create().texOffs(32, 50).addBox(-3.0F, -2.0F, -2.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.95F, 4.45F, -0.7854F, 0.0F, 0.0F));

        PartDefinition head_r15 = head.addOrReplaceChild("head_r15", CubeListBuilder.create().texOffs(26, 56).addBox(-2.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -1.0F, -6.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition helmet_r1 = head.addOrReplaceChild("helmet_r1", CubeListBuilder.create().texOffs(54, 63).addBox(0.0F, 0.0F, -0.5F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -2.6F, 0.0F, 0.0F, 0.0F, 0.3927F));

        PartDefinition helmet_r2 = head.addOrReplaceChild("helmet_r2", CubeListBuilder.create().texOffs(56, 63).addBox(0.0F, 0.0F, -0.5F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -2.6F, 0.0F, 0.0F, 0.0F, -0.3927F));

        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(4.25F, -22.0F, 5.5F, 0.0F, 0.0F, -1.5708F));

        PartDefinition right_arm_r1 = right_arm.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(54, 59).addBox(-5.5F, -0.505F, -0.505F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(62, 16).addBox(-0.5F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 12).addBox(-7.5F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 1.25F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition right_arm_r2 = right_arm.addOrReplaceChild("right_arm_r2", CubeListBuilder.create().texOffs(40, 59).addBox(-2.0F, 0.0F, -1.5F, 4.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.5F, 1.25F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition right_arm_r3 = right_arm.addOrReplaceChild("right_arm_r3", CubeListBuilder.create().texOffs(0, 57).addBox(-3.0F, 0.0F, -1.5F, 4.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.5F, 1.25F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition right_arm_r4 = right_arm.addOrReplaceChild("right_arm_r4", CubeListBuilder.create().texOffs(22, 60).addBox(3.0F, -3.0F, -1.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(14, 57).addBox(4.0F, -4.0F, -1.5F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 1.25F, -2.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, -22.0F, 5.5F, 0.0F, 0.0F, 1.5708F));

        PartDefinition left_arm_r1 = left_arm.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(54, 59).mirror().addBox(0.5F, -0.505F, -0.505F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(62, 16).mirror().addBox(-1.5F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(62, 12).mirror().addBox(5.5F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, 1.5F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition left_arm_r2 = left_arm.addOrReplaceChild("left_arm_r2", CubeListBuilder.create().texOffs(40, 59).mirror().addBox(-2.0F, 0.0F, -1.5F, 4.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(11.5F, 1.5F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition left_arm_r3 = left_arm.addOrReplaceChild("left_arm_r3", CubeListBuilder.create().texOffs(0, 57).mirror().addBox(-1.0F, 0.0F, -1.5F, 4.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(10.5F, 1.5F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition left_arm_r4 = left_arm.addOrReplaceChild("left_arm_r4", CubeListBuilder.create().texOffs(22, 60).mirror().addBox(-4.0F, -3.0F, -1.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(14, 57).mirror().addBox(-5.0F, -4.0F, -1.5F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.5F, 1.5F, -2.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition tail = partdefinition.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 13.5F, 3.0F));

        PartDefinition tail_r1 = tail.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(0, 34).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -0.5F, -1.5708F, 0.0F, 0.0F));

        PartDefinition tail_fin = tail.addOrReplaceChild("tail_fin", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 11.25F, 1.5F, 1.5708F, 0.0F, 0.0F));

        PartDefinition tail_fin_r1 = tail_fin.addOrReplaceChild("tail_fin_r1", CubeListBuilder.create().texOffs(38, 20).addBox(-5.0F, -3.0F, 19.5F, 10.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -20.25F, -2.5F, -1.5708F, 0.0F, 0.0F));

        PartDefinition back_fin = body.addOrReplaceChild("back_fin", CubeListBuilder.create(), PartPose.offset(0.0F, -19.25F, 10.25F));
        PartDefinition back_fin_r1 = back_fin.addOrReplaceChild("back_fin_r1", CubeListBuilder.create().texOffs(50, 50).addBox(-0.5F, -11.0F, 3.0F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.75F, -7.25F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.loadPose(this.bodyDefault);
        this.head.loadPose(this.headDefault);
        this.tail.loadPose(this.tailDefault);
        this.right_arm.loadPose(this.rightArmDefault);
        this.left_arm.loadPose(this.leftArmDefault);
        this.back_fin.loadPose(this.backFinDefault);

        boolean holdingGun = entity.getMainHandItem().getItem() instanceof GunItem;

        float squirmTime = ageInTicks * 0.4F;
        float squirmAmount = 0.15F;

        if (limbSwingAmount > 0.01f) {
            float walkSquirm = Mth.sin(limbSwing * 0.5F) * limbSwingAmount;

            this.body.zRot = walkSquirm * 0.12F;
            this.tail.zRot = walkSquirm * -0.2F;
            this.tail.yRot = walkSquirm * 0.2F;
        } else {
            float idleWobble = Mth.sin(squirmTime) * squirmAmount * 0.08F;
            this.body.zRot = idleWobble;
            this.tail.zRot = Mth.sin(squirmTime + 1.0F) * squirmAmount * -0.12F;
            this.tail.yRot = Mth.cos(squirmTime) * squirmAmount * 0.1F;
        }

        this.tail_fin.yRot = Mth.sin(ageInTicks * 0.3F) * 0.2F;

        if (holdingGun) {
            this.left_arm.xRot = 0.0F;
            this.left_arm.yRot = 1.6F;
            this.left_arm.zRot = 1.5708F;

            this.right_arm.xRot = 0.0F;
            this.right_arm.yRot = -2.2F;
            this.right_arm.zRot = -3.5F;
        }else {
            if (entity.isAttacking() && entity.getAttackTimeout() > 0) {
                animateAttackSmooth(entity.getAttackTimeout(), ageInTicks);
            } else {
                float idleArmSwing = Mth.sin(ageInTicks * 0.1F) * 0.05F;
                this.right_arm.xRot = idleArmSwing;
                this.left_arm.xRot = -idleArmSwing;
            }
        }

        float clampedYaw = Mth.clamp(netHeadYaw, -75.0F, 75.0F);
        float clampedPitch = Mth.clamp(headPitch, -30.0F, 30.0F);
        this.head.yRot = clampedYaw * ((float)Math.PI / 180F);
        this.head.xRot = clampedPitch * ((float)Math.PI / 180F);
    }

    private void animateAttackSmooth(int attackTimeout, float ageInTicks) {
        if (attackTimeout <= 0) {
            this.left_arm.xRot = 0.0f;
            this.left_arm.yRot = 0.0f;
            this.left_arm.zRot = 1.5708f;
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

        this.left_arm.xRot = 0.0f;
        this.left_arm.yRot = swingCurve * 1.8f;
        this.left_arm.zRot = 1.5708f;
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    @Override
    public void translateToHand(HumanoidArm pSide, PoseStack pPoseStack) {
        if (pSide == HumanoidArm.LEFT) {
            this.body.translateAndRotate(pPoseStack);
            this.left_arm.translateAndRotate(pPoseStack);

            pPoseStack.mulPose(com.mojang.math.Axis.ZP.rotation(-1.5708F));


            pPoseStack.translate(-0.15, 0.2, 0.05);
            pPoseStack.scale(1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public ModelPart root() {
        return root;
    }
}