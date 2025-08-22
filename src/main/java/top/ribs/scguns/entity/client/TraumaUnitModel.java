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
import top.ribs.scguns.entity.animations.ModAnimationDefinitions;
import top.ribs.scguns.entity.monster.TraumaUnitEntity;

public class TraumaUnitModel<T extends Entity> extends HierarchicalModel<T> implements ArmedModel {
    private final ModelPart main;
    private final ModelPart bone;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart cog; // Add reference to the cog
    private int lastAttackTimeout = 0;

    public TraumaUnitModel(ModelPart root) {
        this.main = root;
        ModelPart full = this.main.getChild("Full");
        ModelPart torso = full.getChild("Torso");
        this.head = torso.getChild("Head");
        this.bone = torso.getChild("LeftArm");
        this.leftArm = torso.getChild("LeftArm");
        this.rightArm = torso.getChild("RightArm");
        this.cog = torso.getChild("Cog"); // Get reference to the cog
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Full = partdefinition.addOrReplaceChild("Full", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Torso = Full.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(68, 69).addBox(-4.5F, -8.75F, -1.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, -10.0F, -11.0F, 10.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(32, 16).addBox(-7.5F, 0.0F, -10.5F, 9.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(36, 37).addBox(-7.0F, 3.0F, -9.75F, 8.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(36, 27).addBox(-7.0F, -3.0F, -10.0F, 8.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(54, 44).addBox(-6.0F, -10.925F, -3.0F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -17.0F, 7.0F));

        PartDefinition cube_r1 = Torso.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(29, 0).addBox(8.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5F, 3.05F, -6.6F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r2 = Torso.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -7.9544F, -3.5971F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3364F, -8.1293F, -11.3956F, -1.5708F, 0.0F, 0.3927F));

        PartDefinition cube_r3 = Torso.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(4, 16).addBox(-1.0F, -3.3058F, -1.4187F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3364F, -8.1293F, -11.3956F, -0.3927F, 0.0F, 0.3927F));

        PartDefinition cube_r4 = Torso.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(24, 18).addBox(-1.75F, -0.9029F, -1.9544F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(70, 42).addBox(-2.25F, 0.0971F, -2.3544F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(24, 16).addBox(-3.25F, 3.0971F, -0.3544F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3364F, -8.1293F, -11.3956F, 0.0F, 0.0F, 0.3927F));

        PartDefinition cube_r5 = Torso.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(4, 19).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.713F, -11.4526F, -3.4411F, -2.3562F, 0.0F, 0.3927F));

        PartDefinition cube_r6 = Torso.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(4, 16).addBox(-1.0F, -3.0F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1718F, -10.1461F, -2.0269F, 3.1416F, 0.0F, 0.3927F));

        PartDefinition Cog = Torso.addOrReplaceChild("Cog", CubeListBuilder.create().texOffs(64, 37).addBox(-1.0F, -1.0F, -1.49F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(24, 63).addBox(-1.0F, -4.0F, -0.99F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -7.25F, 0.74F));

        PartDefinition cube_r7 = Cog.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(66, 16).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.01F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r8 = Cog.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(60, 63).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.015F, 0.0F, 0.0F, 2.3562F));

        PartDefinition cube_r9 = Cog.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(52, 63).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.015F, 0.0F, 0.0F, 0.7854F));

        PartDefinition Head = Torso.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -4.6667F, -4.8333F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-4.5F, 0.3333F, -5.3333F, 9.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(70, 8).addBox(-4.5F, -2.6667F, -0.3333F, 9.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -12.3333F, -6.1667F));

        PartDefinition LeftArm = Torso.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(66, 26).addBox(-2.5F, 4.0F, 0.25F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(54, 54).addBox(-4.0F, -0.75F, -1.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(54, 9).addBox(-4.0F, 4.25F, -1.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 71).mirror().addBox(-4.5F, 1.3F, -0.1F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-7.5F, -9.0F, -7.5F));

        PartDefinition cube_r10 = LeftArm.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 1.3F, 0.9F, -0.7854F, 0.0F, 0.0F));

        PartDefinition RightArm = Torso.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(38, 0).addBox(-0.45F, -1.4F, -2.55F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(68, 63).addBox(0.25F, 3.85F, -0.8F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(36, 62).addBox(-0.25F, 2.1F, -2.05F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(58, 0).addBox(-0.25F, 7.1F, -2.05F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(38, 9).addBox(-0.25F, 4.1F, -2.05F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 71).addBox(3.5F, 1.4F, -1.15F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, -8.85F, -6.45F));

        PartDefinition cube_r11 = RightArm.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 4).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0F, 9.65F, -0.15F, -0.7854F, 0.0F, 0.0F));

        PartDefinition LeftLeg = Torso.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(0, 54).addBox(-2.0F, 9.3F, -2.7F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(12, 63).addBox(-2.0F, 7.3F, 2.3F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 54).addBox(-2.0F, 0.3F, -2.7F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(40, 68).addBox(-1.0F, 4.3F, -1.2F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 44).addBox(-2.0F, 4.3F, -2.7F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 71).mirror().addBox(-3.0F, -0.65F, -1.0F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.0F, 3.7F, -6.6F));

        PartDefinition RightLeg = Torso.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(36, 44).addBox(-2.0F, 9.2F, -2.7F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 63).addBox(-2.0F, 7.2F, 2.3F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(36, 53).addBox(-2.0F, 0.2F, -2.7F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(32, 68).addBox(-1.0F, 4.2F, -1.2F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 44).addBox(-2.0F, 4.2F, -2.7F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 71).addBox(2.0F, -0.75F, -1.0F, 1.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 3.8F, -6.6F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        animateCog(ageInTicks);

        if (entity instanceof TraumaUnitEntity) {
            TraumaUnitEntity traumaUnit = (TraumaUnitEntity) entity;

            this.animateWalk(ModAnimationDefinitions.TRAUMA_UNIT_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
            if (traumaUnit.isAttacking() && traumaUnit.getAttackTimeout() > 0) {
                animateAttackSmooth(traumaUnit.getAttackTimeout(), ageInTicks);
            }

            float clampedYaw = Mth.clamp(netHeadYaw, -75.0F, 75.0F);
            float clampedPitch = Mth.clamp(headPitch, -30.0F, 30.0F);
            this.head.yRot = clampedYaw * ((float)Math.PI / 180F);
            this.head.xRot = clampedPitch * ((float)Math.PI / 180F);
        }
    }

    private void animateCog(float ageInTicks) {
        float rotationSpeed = 0.08f;
        this.cog.zRot = ageInTicks * rotationSpeed;
    }

    private void animateAttackSmooth(int attackTimeout, float ageInTicks) {
        if (attackTimeout <= 0) {
            this.leftArm.xRot = 0;
            this.leftArm.yRot = 0;
            this.leftArm.zRot = 0;
            this.rightArm.xRot = 0;
            this.rightArm.yRot = 0;
            this.rightArm.zRot = 0;
            lastAttackTimeout = 0;
            return;
        }

        float swingCurve = getSwingCurve(attackTimeout, ageInTicks);

        float swingIntensity = swingCurve * 1.2f;
        this.leftArm.xRot = -swingIntensity;
        this.rightArm.xRot = -swingIntensity;

        float sideMotion = swingCurve * 0.2f;
        this.leftArm.yRot = sideMotion;
        this.rightArm.yRot = -sideMotion;

        float thrust = swingCurve * 0.2f;
        this.leftArm.zRot = thrust;
        this.rightArm.zRot = -thrust;

        lastAttackTimeout = attackTimeout;
    }

    private static float getSwingCurve(int attackTimeout, float ageInTicks) {
        float attackProgress = (15.0f - attackTimeout) / 15.0f;
        attackProgress = Mth.clamp(attackProgress, 0.0f, 1.0f);

        float smoothProgress = Mth.sin(attackProgress * (float)Math.PI);
        return smoothProgress;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return main;
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {

    }
}