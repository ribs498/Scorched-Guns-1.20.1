package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.entity.monster.CogKnightEntity;
import top.ribs.scguns.item.GunItem;

public class CogKnightModel<T extends Entity> extends HierarchicalModel<T> implements ArmedModel {
    private final ModelPart main;
    private final ModelPart full;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart torso;
    private final ModelPart cog;
    private final ModelPart cog2;

    private final PartPose headDefault;
    private final PartPose leftArmDefault;
    private final PartPose rightArmDefault;
    private final PartPose torsoDefault;
    private final PartPose mainDefault;

    private float attackStartTime = -1;
    private int lastAttackTimeout = 0;
    private static final float ATTACK_DURATION = 12.0f;

    public CogKnightModel(ModelPart root) {
        this.main = root.getChild("CogKnight");
        this.full = this.main.getChild("Full");
        this.head = full.getChild("Head");
        this.leftArm = full.getChild("LeftArm");
        this.rightArm = full.getChild("RightArm");
        this.torso = full.getChild("Torso");
        this.cog = this.torso.getChild("Cog");
        this.cog2 = this.torso.getChild("Cog2");

        this.headDefault = this.head.storePose();
        this.leftArmDefault = this.leftArm.storePose();
        this.rightArmDefault = this.rightArm.storePose();
        this.torsoDefault = this.torso.storePose();
        this.mainDefault = this.main.storePose();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition CogKnight = partdefinition.addOrReplaceChild("CogKnight", CubeListBuilder.create(), PartPose.offset(-1.0F, 7.0F, -0.6589F));
        PartDefinition Full = CogKnight.addOrReplaceChild("Full", CubeListBuilder.create(), PartPose.offset(1.0F, 0.8889F, -0.1111F));
        PartDefinition Torso = Full.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(0, 65).addBox(5.0F, -9.8889F, -4.8889F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(30, 44).addBox(-4.0F, -0.8889F, -2.8889F, 8.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(35, 0).addBox(-5.0F, 3.1111F, -3.8889F, 10.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, 6.1111F, -5.8889F, 12.0F, 9.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(33, 27).addBox(-5.0F, 15.1111F, -4.8889F, 10.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 20).addBox(-6.0F, -7.8889F, -5.8889F, 12.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(62, 23).addBox(-4.0F, -7.8889F, 3.1111F, 8.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(30, 37).addBox(-7.0F, -9.8889F, 1.1111F, 14.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(64, 37).addBox(-7.0F, -9.8889F, -4.8889F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Cog = Torso.addOrReplaceChild("Cog", CubeListBuilder.create(), PartPose.offset(0.0F, -4.6289F, 6.67F));
        PartDefinition cube_r1 = Cog.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(45, 23).addBox(-1.5F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 69).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.01F, 0.1F, 0.0F, 1.5708F, 1.5708F));
        PartDefinition cube_r2 = Cog.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(48, 69).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.015F, 0.05F, 0.0F, 1.5708F, 2.3562F));
        PartDefinition cube_r3 = Cog.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(48, 69).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.015F, 0.05F, 0.0F, 1.5708F, 0.7854F));
        PartDefinition cube_r4 = Cog.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(40, 69).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.01F, 0.1F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Cog2 = Torso.addOrReplaceChild("Cog2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 17.3511F, -0.33F, -1.5708F, 0.0F, 0.0F));
        PartDefinition cube_r5 = Cog2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(45, 23).addBox(-0.75F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 69).addBox(-0.25F, -5.0F, -1.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.01F, 0.12F, 0.0F, 1.5708F, 1.5708F));
        PartDefinition cube_r6 = Cog2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(48, 69).addBox(-0.25F, -5.0F, -1.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.015F, 0.07F, 0.0F, 1.5708F, 2.3562F));
        PartDefinition cube_r7 = Cog2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(48, 69).addBox(-0.25F, -5.0F, -1.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.015F, 0.07F, 0.0F, 1.5708F, 0.7854F));
        PartDefinition cube_r8 = Cog2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(40, 69).addBox(-0.25F, -5.0F, -1.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.01F, 0.12F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Head = Full.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 36).addBox(-3.0F, -9.0F, -5.0F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(36, 11).addBox(-2.0F, -10.0F, -4.0F, 6.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(64, 47).addBox(-3.0F, -8.0F, -6.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(69, 0).addBox(-4.0F, -8.0F, -6.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(26, 67).addBox(5.0F, -8.0F, -6.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(20, 53).addBox(-2.5F, -9.0F, 2.0F, 7.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -6.8889F, 0.1111F));
        PartDefinition head_r1 = Head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 51).addBox(-2.0F, -24.0F, -40.75F, 6.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 38.0F, 0.0F, -1.1781F, 0.0F, 0.0F));

        PartDefinition LeftArm = Full.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(16, 62).addBox(-5.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(49, 46).addBox(-6.0F, -3.0F, -4.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(59, 57).addBox(-2.0F, -4.0F, -4.0F, 2.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(39, 57).addBox(-6.0F, 7.0F, -3.0F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(58, 11).addBox(-5.0F, 14.0F, -3.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -7.8889F, 0.1111F));
        PartDefinition RightArm = Full.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(16, 62).mirror().addBox(1.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(49, 46).mirror().addBox(2.0F, -3.0F, -4.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(59, 57).mirror().addBox(0.0F, -4.0F, -4.0F, 2.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(39, 57).mirror().addBox(1.0F, 7.0F, -3.0F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(58, 11).mirror().addBox(0.0F, 14.0F, -3.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(6.0F, -7.8889F, 0.1111F));
        PartDefinition SHIELD = RightArm.addOrReplaceChild("SHIELD", CubeListBuilder.create().texOffs(74, 24).addBox(-6.0F, 9.5F, -4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(67, 59).addBox(-1.0F, 1.5F, -5.0F, 2.0F, 16.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 24).addBox(-4.0F, 9.5F, -4.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 24).addBox(-4.0F, 9.5F, 3.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 5.5F, 0.0F));
        PartDefinition cube_r9 = SHIELD.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(8, 84).addBox(-1.575F, -1.25F, -4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3491F));
        PartDefinition cube_r10 = SHIELD.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(8, 73).addBox(-1.575F, -1.75F, -4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 19.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

        this.main.loadPose(this.mainDefault);
        this.head.loadPose(this.headDefault);
        this.leftArm.loadPose(this.leftArmDefault);
        this.rightArm.loadPose(this.rightArmDefault);
        this.torso.loadPose(this.torsoDefault);

        if (entity instanceof CogKnightEntity cogKnight) {
            boolean isHoldingGun = cogKnight.getMainHandItem().getItem() instanceof GunItem;

            float bobAmount = Mth.sin(ageInTicks * 0.05f);
            this.main.y += bobAmount;

            if (limbSwingAmount > 0.01f) {
                float walkSway = Mth.sin(limbSwing * 0.6f) * limbSwingAmount * 0.3f;
                this.main.zRot = walkSway * 0.1f;
                this.torso.yRot = walkSway * 0.05f;

                if (!isHoldingGun && !cogKnight.isCharging() && !cogKnight.isAttacking()) {
                    float hoverAngle = limbSwingAmount * 0.4f;
                    float tinySwing = Mth.sin(ageInTicks * 0.15f) * 0.05f;

                    this.leftArm.xRot = hoverAngle + tinySwing;
                    this.rightArm.xRot = hoverAngle - tinySwing;
                }
            }

            float backCogSpeed = 0.1f;
            float propellerSpeed = 0.4f;

            if (cogKnight.isCharging()) {
                backCogSpeed = 0.3f;

                this.main.xRot = -0.3f;
                this.leftArm.xRot = -1.5f;
                this.leftArm.zRot = -0.2f;

                float chargeTime = ageInTicks * 0.8f;
                this.leftArm.xRot += Mth.cos(chargeTime) * 0.1f;
                this.torso.zRot = Mth.sin(ageInTicks * 0.6f) * 0.1f;

            } else if (cogKnight.isAttacking() && cogKnight.getAttackTimeout() > 0) {
                backCogSpeed = 0.25f;
                animateAttackSmooth(cogKnight.getAttackTimeout(), ageInTicks);

            } else if (isHoldingGun) {
                this.leftArm.xRot = -1.5708f;
                this.leftArm.yRot = 0.0f;
                this.leftArm.zRot = 0.0f;

                this.rightArm.xRot = -1.3f;
                this.rightArm.yRot = 0.6f;
                this.rightArm.zRot = 0.2f;
            }

            this.cog2.yRot = (ageInTicks * propellerSpeed) % ((float)Math.PI * 2);

            this.cog.zRot = (ageInTicks * backCogSpeed) % ((float)Math.PI * 2);

            float clampedYaw = Mth.clamp(netHeadYaw, -75.0F, 75.0F);
            float clampedPitch = Mth.clamp(headPitch, -30.0F, 30.0F);
            this.head.yRot = clampedYaw * ((float)Math.PI / 180F);
            this.head.xRot = clampedPitch * ((float)Math.PI / 180F);
        }
    }

    private void animateAttackSmooth(int attackTimeout, float ageInTicks) {
        if (attackTimeout <= 0) {
            this.leftArm.xRot = 0.0f;
            this.leftArm.yRot = 0.0f;
            this.leftArm.zRot = 0.0f;
            this.torso.yRot = 0.0f;
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

        this.leftArm.xRot = -swingCurve * 2.0f;
        this.leftArm.yRot = 0.0f;
        this.leftArm.zRot = 0.0f;
        this.torso.yRot = 0.0f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void translateToHand(HumanoidArm pSide, PoseStack pPoseStack) {
        if (pSide == HumanoidArm.LEFT) {
            this.main.translateAndRotate(pPoseStack);
            this.full.translateAndRotate(pPoseStack);
            this.leftArm.translateAndRotate(pPoseStack);

            pPoseStack.translate(-0.2, 0.5, -0.1);
            pPoseStack.scale(0.85F, 0.85F, 0.85F);
        }
    }

    @Override
    public ModelPart root() {
        return main;
    }
}