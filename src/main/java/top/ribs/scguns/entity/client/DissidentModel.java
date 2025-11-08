package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import top.ribs.scguns.entity.monster.DissidentEntity;

public class DissidentModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart seal_2;
    private final ModelPart seal_1;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart tongue_1;
    private final ModelPart tongue_2;
    private final ModelPart tongue_3;
    private final ModelPart back_right_leg;
    private final ModelPart back_left_leg;
    private final ModelPart front_right_leg;
    private final ModelPart front_left_leg;

    private final PartPose bodyDefault;
    private final PartPose headDefault;
    private final PartPose jawDefault;
    private final PartPose tongue1Default;
    private final PartPose tongue2Default;
    private final PartPose tongue3Default;
    private final PartPose frontRightLegDefault;
    private final PartPose frontLeftLegDefault;
    private final PartPose backRightLegDefault;
    private final PartPose backLeftLegDefault;

    private float attackStartTime = -1;
    private int lastAttackTimeout = 0;
    private static final float ATTACK_DURATION = 12.0f;

    public DissidentModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.seal_2 = this.body.getChild("seal_2");
        this.seal_1 = this.body.getChild("seal_1");
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.tongue_1 = this.jaw.getChild("tongue_1");
        this.tongue_2 = this.tongue_1.getChild("tongue_2");
        this.tongue_3 = this.tongue_2.getChild("tongue_3");
        this.back_right_leg = root.getChild("back_right_leg");
        this.back_left_leg = root.getChild("back_left_leg");
        this.front_right_leg = root.getChild("front_right_leg");
        this.front_left_leg = root.getChild("front_left_leg");

        this.bodyDefault = this.body.storePose();
        this.headDefault = this.head.storePose();
        this.jawDefault = this.jaw.storePose();
        this.tongue1Default = this.tongue_1.storePose();
        this.tongue2Default = this.tongue_2.storePose();
        this.tongue3Default = this.tongue_3.storePose();
        this.frontRightLegDefault = this.front_right_leg.storePose();
        this.frontLeftLegDefault = this.front_left_leg.storePose();
        this.backRightLegDefault = this.back_right_leg.storePose();
        this.backLeftLegDefault = this.back_left_leg.storePose();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(31, 32).addBox(-6.0F, -5.875F, -3.75F, 12.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-7.0F, -3.875F, -6.75F, 14.0F, 13.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(34, 55).addBox(-2.0F, -1.875F, -8.75F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 23).addBox(-6.0F, -3.875F, 3.25F, 12.0F, 11.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.625F, 1.25F));

        PartDefinition seal_2 = body.addOrReplaceChild("seal_2", CubeListBuilder.create().texOffs(0, 0).addBox(-0.125F, 0.45F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.375F, 4.925F, -1.25F));

        PartDefinition bone3_r1 = seal_2.addOrReplaceChild("bone3_r1", CubeListBuilder.create().texOffs(38, 0).addBox(-0.5F, -1.5F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.125F, -0.05F, 0.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition seal_1 = body.addOrReplaceChild("seal_1", CubeListBuilder.create(), PartPose.offset(-7.375F, -1.125F, 1.75F));

        PartDefinition bone2_r1 = seal_1.addOrReplaceChild("bone2_r1", CubeListBuilder.create().texOffs(38, 0).addBox(-0.5F, -1.5F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.125F, 0.0F, 0.0F, 2.3562F, 0.0F, 3.1416F));

        PartDefinition bone2_r2 = seal_1.addOrReplaceChild("bone2_r2", CubeListBuilder.create().texOffs(0, 0).addBox(0.125F, -1.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.75F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(22, 45).addBox(-4.0F, -5.9667F, -3.9667F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(33, 25).addBox(-3.5F, -0.0667F, -4.0667F, 7.0F, 3.0F, 4.0F, new CubeDeformation(-0.1F))
                .texOffs(0, 23).addBox(-1.0F, -1.4667F, -4.4667F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.4667F, -5.5333F));

        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(39, 14).addBox(-4.0F, -0.0357F, -8.9062F, 8.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(4, 0).addBox(-3.75F, -2.0357F, -7.699F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 0).addBox(-3.75F, -2.0357F, -5.449F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.75F, -2.0357F, -3.649F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(3.75F, -2.0357F, -3.649F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 0).addBox(3.75F, -2.0357F, -5.449F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 0).addBox(3.75F, -2.0357F, -7.699F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.1761F, -0.1131F, 0.3927F, 0.0F, 0.0F));

        PartDefinition tongue_1 = jaw.addOrReplaceChild("tongue_1", CubeListBuilder.create().texOffs(58, 28).addBox(-1.55F, -1.0F, 0.5F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.05F, -0.8916F, -1.8211F));

        PartDefinition tongue_r1_r1 = tongue_1.addOrReplaceChild("tongue_r1_r1", CubeListBuilder.create().texOffs(38, 0).addBox(-2.0F, -1.0F, -4.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.25F, 0.0F, 0.0F, -3.1416F));

        PartDefinition tongue_2 = tongue_1.addOrReplaceChild("tongue_2", CubeListBuilder.create().texOffs(55, 25).addBox(-1.5F, -1.0F, -4.6414F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 0.0F, -7.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition tongue_3 = tongue_2.addOrReplaceChild("tongue_3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.025F, -4.475F, 0.3927F, 0.0F, 0.0F));

        PartDefinition tongue2_r1_r1 = tongue_3.addOrReplaceChild("tongue2_r1_r1", CubeListBuilder.create().texOffs(54, 0).addBox(-1.5F, -0.9459F, -2.6307F, 3.0F, 2.0F, 5.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition back_right_leg = partdefinition.addOrReplaceChild("back_right_leg", CubeListBuilder.create().texOffs(18, 55).addBox(-3.175F, 5.25F, -0.75F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, 6.75F, 10.0F));

        PartDefinition back_right_leg_r1 = back_right_leg.addOrReplaceChild("back_right_leg_r1", CubeListBuilder.create().texOffs(0, 43).addBox(-2.5F, -4.5F, -3.0F, 5.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.75F, 1.75F, 0.25F, 0.3927F, 0.0F, 0.0F));

        PartDefinition back_left_leg = partdefinition.addOrReplaceChild("back_left_leg", CubeListBuilder.create().texOffs(18, 55).mirror().addBox(-0.825F, 5.25F, -0.75F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.5F, 6.75F, 10.0F));

        PartDefinition back_left_leg_r1 = back_left_leg.addOrReplaceChild("back_left_leg_r1", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(-2.5F, -4.5F, -3.0F, 5.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.75F, 1.75F, 0.25F, 0.3927F, 0.0F, 0.0F));

        PartDefinition front_right_leg = partdefinition.addOrReplaceChild("front_right_leg", CubeListBuilder.create(), PartPose.offset(0.0F, 7.75F, -2.5F));

        PartDefinition back_right_leg_r2 = front_right_leg.addOrReplaceChild("back_right_leg_r2", CubeListBuilder.create().texOffs(58, 58).addBox(-2.5F, -3.0F, -2.0F, 5.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.25F, 1.5021F, -1.6118F, 0.3927F, 0.0F, 0.0F));

        PartDefinition back_right_leg_r3 = front_right_leg.addOrReplaceChild("back_right_leg_r3", CubeListBuilder.create().texOffs(46, 45).addBox(-7.725F, -6.5F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.25F, -2.35F, -0.2182F, 0.0F, 0.0F));

        PartDefinition front_left_leg = partdefinition.addOrReplaceChild("front_left_leg", CubeListBuilder.create(), PartPose.offset(0.0F, 7.75F, -2.5F));

        PartDefinition back_left_leg_r2 = front_left_leg.addOrReplaceChild("back_left_leg_r2", CubeListBuilder.create().texOffs(58, 58).mirror().addBox(-2.5F, -3.0F, -2.0F, 5.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(6.25F, 1.5021F, -1.6118F, 0.3927F, 0.0F, 0.0F));

        PartDefinition back_left_leg_r3 = front_left_leg.addOrReplaceChild("back_left_leg_r3", CubeListBuilder.create().texOffs(46, 45).mirror().addBox(3.725F, -6.5F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 10.25F, -2.35F, -0.2182F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof DissidentEntity dissident)) return;

        this.body.loadPose(this.bodyDefault);
        this.head.loadPose(this.headDefault);
        this.jaw.loadPose(this.jawDefault);
        this.tongue_1.loadPose(this.tongue1Default);
        this.tongue_2.loadPose(this.tongue2Default);
        this.tongue_3.loadPose(this.tongue3Default);
        this.front_right_leg.loadPose(this.frontRightLegDefault);
        this.front_left_leg.loadPose(this.frontLeftLegDefault);
        this.back_right_leg.loadPose(this.backRightLegDefault);
        this.back_left_leg.loadPose(this.backLeftLegDefault);

        this.jaw.xRot += 0.3927F;
        this.tongue_2.xRot += 0.3927F;
        this.tongue_3.xRot += 0.3927F;

        boolean isLeaping = dissident.isLeaping();
        boolean isLanding = dissident.isLanding();

        if (isLeaping) {
            this.front_right_leg.xRot += -0.8F;
            this.front_left_leg.xRot += -0.8F;

            this.back_right_leg.xRot += 0.6F;
            this.back_left_leg.xRot += 0.6F;

            this.body.xRot = -0.15F;

            this.head.xRot += -0.3F;

            this.jaw.xRot = 0.6F;

            this.tongue_2.xRot = 0.5F;
            this.tongue_3.xRot = 0.5F;
        } else if (isLanding && dissident.landingAnimationTimeout > 0) {
            float landProgress = (float)dissident.landingAnimationTimeout / 8.0F;

            this.front_right_leg.xRot += -0.4F * landProgress;
            this.front_left_leg.xRot += -0.4F * landProgress;

            this.back_right_leg.xRot += 0.3F * landProgress;
            this.back_left_leg.xRot += 0.3F * landProgress;

            this.body.xRot = -0.1F * landProgress;
            this.body.y -= 0.5F * landProgress;

            this.head.xRot += -0.2F * landProgress;

            this.jaw.xRot += 0.15F * landProgress;

            this.tongue_2.xRot += 0.1F * landProgress;
            this.tongue_3.xRot += 0.1F * landProgress;
        } else {
            float walkSpeed = 0.5F;
            float walkAmount = 0.6F;

            float frontLegSwing = Mth.cos(limbSwing * walkSpeed) * walkAmount * limbSwingAmount;
            this.front_right_leg.xRot += frontLegSwing;
            this.front_left_leg.xRot += -frontLegSwing;

            float backLegSwing = Mth.cos(limbSwing * walkSpeed + (float)Math.PI) * walkAmount * limbSwingAmount;
            this.back_right_leg.xRot += backLegSwing;
            this.back_left_leg.xRot += -backLegSwing;

            this.body.zRot += Mth.cos(limbSwing * walkSpeed * 0.5F) * 0.02F * limbSwingAmount;
        }

        if (!isLeaping && !isLanding) {
            float clampedYaw = Mth.clamp(netHeadYaw, -75.0F, 75.0F);
            float clampedPitch = Mth.clamp(headPitch, -45.0F, 45.0F);
            this.head.yRot += clampedYaw * ((float)Math.PI / 180F);
            this.head.xRot += clampedPitch * ((float)Math.PI / 180F);
        }

        if (dissident.isAttacking() && dissident.attackAnimationTimeout > 0) {
            animateAttackSmooth(dissident.attackAnimationTimeout, ageInTicks);
        } else {
            attackStartTime = -1;
            lastAttackTimeout = 0;
            if (!isLeaping && !isLanding) {
                this.jaw.xRot += Mth.sin(ageInTicks * 0.04F) * 0.02F;
            }
        }
        if (!isLeaping && !isLanding) {
            float breathe = Mth.sin(ageInTicks * 0.06F) * 0.01F;
            this.body.y += breathe;
        }
    }

    private void animateAttackSmooth(int attackTimeout, float ageInTicks) {
        if (attackTimeout <= 0) {
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

        float attackCurve = Mth.sin(attackProgress * (float)Math.PI);

        this.body.xRot = -attackCurve * 0.25F;

        this.head.xRot += -attackCurve * 0.6F;

        this.jaw.xRot = 0.3927F - (attackCurve * 0.3F);

        float tongueExtend = attackCurve * 0.3927F;
        this.tongue_2.xRot = 0.3927F - tongueExtend;
        this.tongue_3.xRot = 0.3927F - tongueExtend;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        back_right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        back_left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        front_right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        front_left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}