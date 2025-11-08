package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.entity.monster.ViventrumEntity;
import top.ribs.scguns.item.GunItem;

public class ViventrumModel<T extends ViventrumEntity> extends EntityModel<T> implements ArmedModel, HeadedModel {
    private final ModelPart body;
    private final ModelPart tail_top;
    private final ModelPart tail_middle;
    private final ModelPart tail_end;
    private final ModelPart head;
    private final ModelPart left_arm;

    private final PartPose bodyDefault;
    private final PartPose tailTopDefault;
    private final PartPose tailMiddleDefault;
    private final PartPose tailEndDefault;
    private final PartPose headDefault;
    private final PartPose leftArmDefault;

    private float attackStartTime = -1;
    private int lastAttackTimeout = 0;
    private static final float ATTACK_DURATION = 10.0f;

    public ViventrumModel(ModelPart root) {
        this.body = root.getChild("body");
        this.tail_top = this.body.getChild("tail_top");
        this.tail_middle = this.tail_top.getChild("tail_middle");
        this.tail_end = this.tail_middle.getChild("tail_end");
        this.head = root.getChild("head");
        this.left_arm = root.getChild("left_arm");

        this.bodyDefault = this.body.storePose();
        this.tailTopDefault = this.tail_top.storePose();
        this.tailMiddleDefault = this.tail_middle.storePose();
        this.tailEndDefault = this.tail_end.storePose();
        this.headDefault = this.head.storePose();
        this.leftArmDefault = this.left_arm.storePose();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(38, 41).addBox(-2.5F, -6.0F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(37, 15).addBox(-1.5F, -7.0F, -1.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-3.5F, -4.0F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(20, 24).addBox(-4.0F, -5.0F, 1.0F, 7.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 13.0F, 0.0F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(24, 4).addBox(-0.5F, 0.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.5F, -3.25F, -1.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(38, 37).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -2.5F, -1.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(20, 28).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -1.5F, 0.5F, 0.0F, 0.0F, 0.7854F));

        PartDefinition tail_top = body.addOrReplaceChild("tail_top", CubeListBuilder.create(), PartPose.offset(0.0F, -0.75F, 1.0F));

        PartDefinition cube_r4 = tail_top.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(42, 14).addBox(-1.5F, -1.5F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.25F, -0.5F, -1.5708F, 0.0F, 0.0F));

        PartDefinition tail_middle = tail_top.addOrReplaceChild("tail_middle", CubeListBuilder.create(), PartPose.offset(0.0F, 3.25F, 0.0F));

        PartDefinition cube_r5 = tail_middle.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(24, 0).addBox(-1.5F, -1.5F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -0.5F, -1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r6 = tail_middle.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(8, 42).addBox(-2.0F, -2.0F, -3.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -0.5F, -1.5708F, 0.0F, 0.0F));

        PartDefinition tail_end = tail_middle.addOrReplaceChild("tail_end", CubeListBuilder.create(), PartPose.offset(0.0F, 2.75F, 0.0F));

        PartDefinition cube_r7 = tail_end.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(41, 27).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 3.25F, 0.0F, 1.5708F, -0.7854F, -3.1416F));

        PartDefinition cube_r8 = tail_end.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(40, 5).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 3.25F, 0.0F, -1.5708F, -0.7854F, 0.0F));

        PartDefinition cube_r9 = tail_end.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 15).addBox(-1.5F, -1.0F, 3.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -3.75F, -0.5F, -1.5708F, 0.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -4.4F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(4.25F, -2.0F, -2.1929F, 0.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-6.0144F, -3.6411F, -4.265F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(38, 33).addBox(-4.75F, -5.5F, -5.25F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(24, 21).addBox(-2.65F, -3.5F, -5.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(32, 11).addBox(-4.25F, -2.5F, -5.25F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 34).addBox(-4.75F, -5.5F, -4.25F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(36, 28).addBox(-5.25F, -5.0F, -4.25F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.4F));

        PartDefinition cube_r10 = head.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(32, 5).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.75F, -4.0F, 0.5F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r11 = head.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(40, 11).addBox(0.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.3F, -4.3F, -5.15F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r12 = head.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(30, 36).addBox(-1.0F, -2.0F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.3F, -3.55F, -4.9F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r13 = head.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(4, 0).addBox(0.0F, -0.5F, -0.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0934F, -3.9225F, 0.2305F, -0.2317F, 0.5856F, 0.1474F));

        PartDefinition cube_r14 = head.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 3).addBox(0.0F, -0.5F, -0.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.56F, -4.4294F, -1.616F, -0.1966F, 0.2016F, 0.2372F));

        PartDefinition cube_r15 = head.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(4, 5).addBox(-0.6638F, -0.7727F, -0.4641F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.3146F, -4.3321F, -2.9058F, -0.2132F, 0.4409F, 0.185F));

        PartDefinition cube_r16 = head.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 0).addBox(-0.7976F, -0.7727F, -1.8588F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.3146F, -4.3321F, -2.9058F, -0.2036F, -0.3291F, 0.3437F));

        PartDefinition cube_r17 = head.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(18, 18).addBox(0.0F, -0.5F, -0.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.8958F, -3.1411F, 0.9356F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r18 = head.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(18, 15).addBox(0.0F, -0.5F, -0.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.8232F, -3.1411F, -0.8034F, 0.0F, 0.3927F, 0.0F));

        PartDefinition cube_r19 = head.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6612F, -3.1411F, -4.6178F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r20 = head.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(2, 0).addBox(0.0F, -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.8083F, -3.1411F, -4.9711F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r21 = head.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(4, 3).addBox(0.0F, -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.8083F, -4.8911F, -4.9711F, -0.7494F, -1.2344F, 1.0165F));

        PartDefinition cube_r22 = head.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(34, 40).addBox(-0.9148F, -1.1672F, 1.0609F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5777F, -0.4363F, -2.5259F, -0.3927F, 0.5061F, -1.5708F));

        PartDefinition cube_r23 = head.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(30, 40).addBox(-0.961F, -0.6723F, -0.554F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5777F, -0.4363F, -2.5259F, 0.0F, 0.5061F, -1.5708F));

        PartDefinition cube_r24 = head.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(40, 24).addBox(-1.0713F, -0.6723F, -1.7122F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5777F, -0.4363F, -2.5259F, 0.0F, -0.2793F, -1.5708F));

        PartDefinition cube_r25 = head.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(36, 33).addBox(-1.6449F, -0.6723F, -2.1715F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5777F, -0.4363F, -2.5259F, 0.0F, -0.672F, -1.5708F));

        PartDefinition cube_r26 = head.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(32, 0).addBox(1.0F, 0.5F, -2.5F, 3.0F, 0.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 24).addBox(-2.0F, -1.0F, -3.5F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 5.6F, -1.5708F, -0.7854F, 0.0F));

        PartDefinition cube_r27 = head.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(36, 21).addBox(-1.5F, -0.5F, 3.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(30, 21).addBox(-1.5F, -0.5F, -4.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 5.6F, -1.5708F, -0.7854F, 0.0F));

        PartDefinition cube_r28 = head.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(4, 41).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -4.0F, 0.6F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r29 = head.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(22, 15).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -2.0F, -1.9F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r30 = head.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(0, 41).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -4.0F, -2.4F, -0.7854F, 0.0F, 0.0F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(10, 34).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 36).addBox(0.0F, 5.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 10.5F, -1.0F));

        PartDefinition cube_r31 = left_arm.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(24, 36).addBox(0.0F, -2.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.loadPose(this.bodyDefault);
        this.tail_top.loadPose(this.tailTopDefault);
        this.tail_middle.loadPose(this.tailMiddleDefault);
        this.tail_end.loadPose(this.tailEndDefault);
        this.head.loadPose(this.headDefault);
        this.left_arm.loadPose(this.leftArmDefault);

        boolean holdingGun = entity.getMainHandItem().getItem() instanceof GunItem;

        if (entity.isTame() && entity.isDefensive()) {
            animateDefensive(ageInTicks);
            return;
        }

        if (entity.isTame() && entity.isPartying()) {
            animateDancing(ageInTicks);
            return;
        }

        float bobAmount = Mth.sin(ageInTicks * 0.1F) * 0.1F;
        this.body.y += bobAmount;
        this.head.y += bobAmount;

        this.body.zRot = Mth.cos(ageInTicks * 0.08F) * 0.04F;

        float tailTime = ageInTicks * 0.12F;
        float tailSwayAmount = 0.3F;

        this.tail_top.xRot = Mth.sin(tailTime) * tailSwayAmount;
        this.tail_top.zRot = Mth.cos(tailTime * 0.8F) * tailSwayAmount * 0.5F;

        this.tail_middle.xRot = Mth.sin(tailTime + 0.8F) * tailSwayAmount * 0.8F;
        this.tail_middle.zRot = Mth.cos(tailTime * 0.8F + 0.6F) * tailSwayAmount * 0.4F;

        this.tail_end.xRot = Mth.sin(tailTime + 1.6F) * tailSwayAmount * 0.6F;
        this.tail_end.yRot = Mth.cos(tailTime + 1.2F) * 0.2F;

        if (!holdingGun && !entity.isAttacking()) {
            float armTime = ageInTicks * 0.08F;
            this.left_arm.xRot = Mth.sin(armTime) * 0.15F - 0.15F;
            this.left_arm.zRot = Mth.cos(armTime * 0.6F) * 0.08F;
            this.left_arm.yRot = Mth.sin(armTime * 0.5F) * 0.05F;
        }
        if (entity.isAttacking() && entity.getAttackTimeout() > 0 && !holdingGun) {
            animateAttackSmooth(entity.getAttackTimeout(), ageInTicks);
        }

        if (holdingGun) {
            this.left_arm.xRot = -1.5708F;
            this.left_arm.yRot = 0.0F;
            this.left_arm.zRot = 0.0F;
        }

        float clampedYaw = Mth.clamp(netHeadYaw, -60.0F, 60.0F);
        float clampedPitch = Mth.clamp(headPitch, -30.0F, 30.0F);
        this.head.yRot = clampedYaw * ((float)Math.PI / 180F);
        this.head.xRot = clampedPitch * ((float)Math.PI / 180F) * 0.5F;
    }

    private void animateAttackSmooth(int attackTimeout, float ageInTicks) {
        if (attackTimeout <= 0) {
            this.left_arm.xRot = 0.0f;
            this.left_arm.yRot = 0.0f;
            this.left_arm.zRot = 0.0f;
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

        this.left_arm.xRot = -swingCurve * 1.8f;
        this.left_arm.yRot = swingCurve * 0.3f;
        this.left_arm.zRot = 0.0f;
    }

    private void animateDefensive(float ageInTicks) {
        float bob = Mth.sin(ageInTicks * 0.08F) * 0.15F;

        this.body.y += 1.5f + bob;
        this.body.xRot = -0.8f;
        this.body.zRot = 0.0f;

        this.head.xRot = 1.2f;
        this.head.yRot = 0.0f;
        this.head.zRot = 0.0f;
        this.head.y += 0.5f + bob * 0.8f;

        this.left_arm.xRot = -1.8f;
        this.left_arm.yRot = 0.4f;
        this.left_arm.zRot = 0.6f;
        this.left_arm.y += 1.0f + bob * 0.7f;

        this.tail_top.xRot = 2.0f;
        this.tail_top.yRot = 0.0f;
        this.tail_top.zRot = 0.0f;

        this.tail_middle.xRot = 1.8f;
        this.tail_middle.yRot = 0.0f;
        this.tail_middle.zRot = 0.0f;

        this.tail_end.xRot = 1.4f;
        this.tail_end.yRot = 0.0f;
        this.tail_end.zRot = 0.0f;
    }

    private void animateDancing(float ageInTicks) {
        float danceSpeed = 0.4f;

        float bounce = Mth.abs(Mth.sin(ageInTicks * danceSpeed)) * 1.5f;
        this.body.y += bounce;
        this.head.y += bounce * 1.2f;

        this.body.zRot = Mth.sin(ageInTicks * danceSpeed * 0.8f) * 0.25f;
        this.body.xRot = Mth.cos(ageInTicks * danceSpeed * 0.5f) * 0.12f;

        this.head.xRot = Mth.sin(ageInTicks * danceSpeed * 2.0f) * 0.2f;
        this.head.yRot = Mth.sin(ageInTicks * danceSpeed * 1.2f) * 0.35f;
        this.head.zRot = Mth.cos(ageInTicks * danceSpeed * 1.5f) * 0.15f;

        float armPhase = ageInTicks * danceSpeed;
        this.left_arm.xRot = -2.2f + Mth.sin(armPhase * 1.3f) * 0.3f;
        this.left_arm.zRot = Mth.sin(armPhase * 0.9f) * 0.4f;
        this.left_arm.yRot = Mth.cos(armPhase * 0.7f) * 0.2f;

        float tailTime = ageInTicks * danceSpeed * 1.5f;
        this.tail_top.xRot = Mth.sin(tailTime) * 0.6f;
        this.tail_top.zRot = Mth.cos(tailTime * 0.9f) * 0.5f;
        this.tail_top.yRot = Mth.sin(tailTime * 0.7f) * 0.3f;

        this.tail_middle.xRot = Mth.sin(tailTime + 1.0f) * 0.7f;
        this.tail_middle.zRot = Mth.cos(tailTime * 0.8f + 0.8f) * 0.6f;
        this.tail_middle.yRot = Mth.cos(tailTime + 0.5f) * 0.4f;

        this.tail_end.xRot = Mth.sin(tailTime + 2.0f) * 0.5f;
        this.tail_end.yRot = Mth.cos(tailTime + 1.5f) * 0.5f;
        this.tail_end.zRot = Mth.sin(tailTime * 1.2f) * 0.3f;
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void translateToHand(HumanoidArm pSide, PoseStack pPoseStack) {
        if (pSide == HumanoidArm.LEFT) {
            this.left_arm.translateAndRotate(pPoseStack);
            pPoseStack.translate(-0.05, 0.0, 0.0);
            pPoseStack.scale(0.9F, 0.9F, 0.9F);
        }
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }
}