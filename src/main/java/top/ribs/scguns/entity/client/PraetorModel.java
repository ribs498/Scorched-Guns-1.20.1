package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.entity.monster.PraetorEntity;

public class PraetorModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart mask;
    private final ModelPart torso;
    private final ModelPart left_leg;
    private final ModelPart right_leg;
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
    private static final float ATTACK_DURATION = 8.0f;

    private float roarStartTime = -1;
    private int lastRoarTick = 0;
    private static final float ROAR_DURATION = 40.0f;

    public PraetorModel(ModelPart root) {
        this.head = root.getChild("head");
        this.mask = this.head.getChild("mask");
        this.torso = root.getChild("torso");
        this.left_leg = root.getChild("left_leg");
        this.right_leg = root.getChild("right_leg");
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

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -9.25F, -0.75F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(56, 99).addBox(-5.0F, 4.0F, 0.0F, 9.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(104, 109).addBox(-6.0F, -4.0F, -3.0F, 11.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -4.85F, -4.85F, 0.3054F, 0.0F, 0.0F));

        PartDefinition mask = head.addOrReplaceChild("mask", CubeListBuilder.create(), PartPose.offset(0.0F, -9.7431F, -4.2957F));

        PartDefinition cube_r2 = mask.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(66, 15).addBox(-4.5F, -0.5F, -8.0F, 9.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(26, 79).addBox(-5.5F, 0.5F, -2.0F, 11.0F, 8.0F, 1.0F, new CubeDeformation(0.25F))
                .texOffs(0, 0).addBox(-1.0F, -1.5F, -5.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(51, 26).addBox(-4.5F, -1.5F, 0.0F, 9.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 3.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 60).addBox(-8.0F, -10.0F, 2.0F, 16.0F, 8.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(62, 43).addBox(-11.0F, -6.0F, 4.0F, 21.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-9.0F, -2.0F, 1.0F, 18.0F, 10.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(92, 81).addBox(-9.0F, -2.0F, -1.0F, 18.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-9.0F, 8.0F, 1.0F, 18.0F, 4.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-9.0F, 12.0F, -1.0F, 18.0F, 3.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(66, 33).addBox(-9.0F, 7.0F, -4.0F, 18.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, -3.0F));

        PartDefinition cube_r3 = torso.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(80, 90).addBox(-10.0F, -6.0F, 0.5F, 18.0F, 6.0F, 3.0F, new CubeDeformation(-0.025F)), PartPose.offsetAndRotation(1.0F, 12.2039F, 10.4703F, -0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r4 = torso.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(92, 54).addBox(-8.0F, -2.0F, 0.5F, 16.0F, 8.0F, 3.0F, new CubeDeformation(-0.025F)), PartPose.offsetAndRotation(0.0F, -6.2039F, 10.4703F, 0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r5 = torso.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(36, 90).addBox(-9.5F, -4.5F, -3.0F, 18.0F, 4.0F, 4.0F, new CubeDeformation(-0.025F)), PartPose.offsetAndRotation(0.5F, 13.0607F, 1.3033F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r6 = torso.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(66, 0).addBox(-8.5F, -3.0F, -1.5F, 18.0F, 9.0F, 6.0F, new CubeDeformation(-0.025F)), PartPose.offsetAndRotation(-0.5F, 2.0307F, -0.3181F, -0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r7 = torso.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(28, 98).addBox(-4.5F, -3.0F, -0.5F, 11.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8284F, -3.0F, 0.5F, 0.0F, 0.0F, -0.7854F));

        PartDefinition cube_r8 = torso.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(106, 15).addBox(-3.5F, -2.0F, -3.0F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, 0.0F, 7.5F, 0.0F, 0.0F, -0.7854F));

        PartDefinition cube_r9 = torso.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(86, 99).addBox(-4.5F, -2.0F, -3.0F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5F, 0.0F, 7.5F, 0.0F, 0.0F, 0.7854F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(54, 60).addBox(-4.0F, -1.0F, -7.0F, 7.0F, 18.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 7.0F, 4.0F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(54, 60).mirror().addBox(-3.0F, -1.0F, -7.0F, 7.0F, 18.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.0F, 7.0F, 4.0F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(-10.5F, -10.5F, 5.0F));

        PartDefinition cube_r10 = left_arm.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(92, 65).addBox(-6.5005F, -3.2594F, -5.0F, 7.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(88, 109).addBox(-5.5005F, 21.7406F, 0.0F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(72, 109).addBox(-5.5005F, 19.7406F, -4.0F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(56, 109).addBox(-5.5005F, 19.7406F, 4.0F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 79).addBox(-6.5005F, 12.7406F, -5.0F, 8.0F, 9.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 98).addBox(-5.5005F, 3.7406F, -4.0F, 6.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition cube_r11 = left_arm.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(51, 29).addBox(17.5894F, 3.7635F, 2.0F, 8.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, -0.5F, 0.0F, -3.1416F, 0.0F, 1.8413F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 25).addBox(5.25F, -0.25F, -0.75F, 0.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(10.5F, -10.5F, 5.0F));

        PartDefinition cube_r12 = right_arm.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(6, 5).addBox(-1.25F, -2.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -1.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r13 = right_arm.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(92, 65).mirror().addBox(-0.4995F, -3.2594F, -5.0F, 7.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(88, 109).mirror().addBox(-2.4995F, 21.7406F, 0.0F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(72, 109).mirror().addBox(-2.4995F, 19.7406F, -4.0F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(56, 109).mirror().addBox(-2.4995F, 19.7406F, 4.0F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 79).mirror().addBox(-1.4995F, 12.7406F, -5.0F, 8.0F, 9.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 98).mirror().addBox(-0.4995F, 3.7406F, -4.0F, 6.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.75F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition cube_r14 = right_arm.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(51, 29).mirror().addBox(-25.5894F, 3.7635F, 2.0F, 8.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.75F, -0.5F, 0.0F, -3.1416F, 0.0F, -1.8413F));

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

        float breathe = Mth.sin(ageInTicks * 0.04f) * 0.05f;
        this.torso.y += breathe;

        float clampedYaw = Mth.clamp(netHeadYaw, -45.0F, 45.0F);
        float clampedPitch = Mth.clamp(headPitch, -20.0F, 20.0F);
        this.head.yRot = clampedYaw * ((float)Math.PI / 180F);
        this.head.xRot = clampedPitch * ((float)Math.PI / 180F);

        if (entity instanceof PraetorEntity praetor) {
            this.mask.visible = !praetor.isInSecondPhase();

            if (praetor.isRoaring()) {
                setupRoarAnim(praetor.getRoarTick(), ageInTicks);
            } else if (praetor.isAttacking() && praetor.getAttackTimeout() > 0) {
                setupAttackAnim(praetor.getAttackTimeout(), ageInTicks, praetor.getAttackVariation());
            } else {
                this.left_arm.xRot = -0.35f;
                this.left_arm.zRot = 0.35f;
                this.right_arm.xRot = -0.35f;
                this.right_arm.zRot = -0.35f;

                float armSway = Mth.sin(ageInTicks * 0.05f) * 0.05f;
                this.left_arm.xRot += armSway;
                this.right_arm.xRot -= armSway;
            }
        } else {
            this.left_arm.xRot = -0.35f;
            this.left_arm.zRot = 0.35f;
            this.right_arm.xRot = -0.35f;
            this.right_arm.zRot = -0.35f;
        }

        // Walking animation - legs always animate when moving
        if (limbSwingAmount > 0.01f) {
            float legSwing = Mth.cos(limbSwing * 0.5f) * 0.5f * limbSwingAmount;
            this.left_leg.xRot = legSwing;
            this.right_leg.xRot = -legSwing;

            // Only apply torso sway if not roaring
            if (!(entity instanceof PraetorEntity praetor && praetor.isRoaring())) {
                float torsoSway = Mth.sin(limbSwing * 0.5f) * limbSwingAmount * 0.1f;
                this.torso.yRot = torsoSway;
                this.head.yRot += torsoSway * 0.5f;

                if (!(entity instanceof PraetorEntity praetor2 && praetor2.isAttacking())) {
                    this.left_arm.yRot = torsoSway * 0.3f;
                    this.right_arm.yRot = torsoSway * 0.3f;
                }
            }
        }
    }

    public void setupRoarAnim(int roarTick, float ageInTicks) {
        if (roarTick <= 0) {
            roarStartTime = -1;
            lastRoarTick = 0;
            return;
        }

        if (roarTick < lastRoarTick || lastRoarTick == 0 || roarStartTime < 0) {
            roarStartTime = ageInTicks;
        }
        lastRoarTick = roarTick;

        float animProgress = getAnimProgress(ageInTicks);

        this.head.xRot = 1.2f * animProgress;

        float armSpread = 0.9f * animProgress;
        this.left_arm.xRot = -0.35f - armSpread * 0.5f;
        this.left_arm.zRot = 0.35f + armSpread;
        this.left_arm.yRot = -armSpread * 0.3f;

        this.right_arm.xRot = -0.35f - armSpread * 0.5f;
        this.right_arm.zRot = -0.35f - armSpread;
        this.right_arm.yRot = armSpread * 0.3f;

        if (animProgress > 0.5f) {
            this.torso.xRot = Mth.sin(ageInTicks * 1.5f) * 0.05f * animProgress;
        }
    }

    private float getAnimProgress(float ageInTicks) {
        float elapsedTime = (ageInTicks - roarStartTime);
        float roarProgress = elapsedTime / ROAR_DURATION;
        roarProgress = Mth.clamp(roarProgress, 0.0f, 1.0f);

        float peakProgress = 0.35f;
        float holdEnd = 0.75f;

        float animProgress;
        if (roarProgress < peakProgress) {
            animProgress = roarProgress / peakProgress;
        } else if (roarProgress < holdEnd) {
            animProgress = 1.0f;
        } else {
            animProgress = 1.0f - ((roarProgress - holdEnd) / (1.0f - holdEnd));
        }
        return animProgress;
    }

    public void setupAttackAnim(int attackTicks, float ageInTicks, int variation) {
        if (attackTicks <= 0) {
            attackStartTime = -1;
            lastAttackTimeout = 0;
            return;
        }

        if (attackTicks > lastAttackTimeout || attackStartTime < 0) {
            attackStartTime = ageInTicks;
        }
        lastAttackTimeout = attackTicks;

        float elapsedTime = (ageInTicks - attackStartTime);
        float attackProgress = elapsedTime / ATTACK_DURATION;
        attackProgress = Mth.clamp(attackProgress, 0.0f, 1.0f);

        float swingCurve = Mth.sin(attackProgress * (float)Math.PI);

        switch (variation) {
            case 0:
                this.left_arm.xRot = -0.35f - swingCurve * 1.8f;
                this.left_arm.zRot = 0.35f + swingCurve * 0.5f;
                this.left_arm.yRot = -swingCurve * 0.2f;

                this.right_arm.xRot = -0.35f;
                this.right_arm.zRot = -0.35f;
                break;
            case 1:
                this.left_arm.xRot = -0.35f;
                this.left_arm.zRot = 0.35f;

                this.right_arm.xRot = -0.35f - swingCurve * 1.8f;
                this.right_arm.zRot = -0.35f - swingCurve * 0.5f;
                this.right_arm.yRot = swingCurve * 0.2f;
                break;
            case 2:
                this.left_arm.xRot = -0.35f - swingCurve * 1.8f;
                this.left_arm.zRot = 0.35f + swingCurve * 0.5f;
                this.left_arm.yRot = -swingCurve * 0.05f;

                this.right_arm.xRot = -0.35f - swingCurve * 1.8f;
                this.right_arm.zRot = -0.35f - swingCurve * 0.5f;
                this.right_arm.yRot = swingCurve * 0.05f;
                break;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}