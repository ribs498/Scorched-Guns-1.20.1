package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import top.ribs.scguns.entity.animations.ModAnimationDefinitions;
import top.ribs.scguns.entity.monster.HiveEntity;

public class HiveModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart Hive;
    private final ModelPart Torso;
    private final ModelPart Head;
    private final ModelPart LeftArm;
    private final ModelPart RightArm;
    private final ModelPart LeftLeg;
    private final ModelPart RightLeg;

    public HiveModel(ModelPart root) {
        this.Hive = root.getChild("Hive");
        this.Torso = this.Hive.getChild("Torso");
        this.Head = this.Torso.getChild("Head");
        this.LeftArm = this.Torso.getChild("LeftArm");
        this.RightArm = this.Torso.getChild("RightArm");
        this.LeftLeg = this.Hive.getChild("LeftLeg");
        this.RightLeg = this.Hive.getChild("RightLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Hive = partdefinition.addOrReplaceChild("Hive", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Torso = Hive.addOrReplaceChild("Torso", CubeListBuilder.create(), PartPose.offset(0.0F, -25.0F, 4.0F));

        PartDefinition TorsoInt = Torso.addOrReplaceChild("TorsoInt", CubeListBuilder.create().texOffs(27, 10).addBox(-3.0F, 4.5F, -3.5F, 6.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(27, 0).addBox(-2.0F, 2.5F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(15, 33).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.5F, -2.5F));

        PartDefinition cube_r1 = TorsoInt.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 17).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r2 = TorsoInt.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(25, 26).addBox(-4.0F, -0.1F, 0.0F, 8.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.5F, -3.5F, -0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r3 = TorsoInt.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -10.0F, 0.0F, 10.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.5F, -3.5F, -1.0472F, 0.0F, 0.0F));

        PartDefinition Head = Torso.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 49).addBox(-4.0F, -4.0F, 8.0F, 8.0F, 18.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 4.0F));

        PartDefinition cube_r4 = Head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(20, 43).addBox(-7.0F, -5.0F, -4.0F, 8.0F, 18.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(36, 43).addBox(-7.0F, -5.0F, 4.0F, 8.0F, 18.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition LeftArm = Torso.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(52, 40).addBox(-3.0F, -2.0F, -2.0F, 3.0F, 15.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -1.0F, 2.0F));

        PartDefinition RightArm = Torso.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(52, 40).mirror().addBox(0.0F, -2.0F, -2.0F, 3.0F, 15.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.0F, -1.0F, 2.0F));

        PartDefinition LeftLeg = Hive.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(45, 0).addBox(-1.0F, 11.0F, -2.0F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 33).addBox(-2.0F, 0.0F, -3.0F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -16.0F, 0.0F));

        PartDefinition RightLeg = Hive.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(45, 0).mirror().addBox(-2.0F, 11.0F, -2.0F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 33).mirror().addBox(-2.0F, 0.0F, -3.0F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.0F, -16.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        HiveEntity hiveEntity = entity instanceof HiveEntity ? (HiveEntity) entity : null;
        boolean isSpawning = hiveEntity != null && hiveEntity.isSpawning();
        float spawnProgress = hiveEntity != null ? hiveEntity.getSpawnAnimationProgress(0) : 0.0F;

        if (isSpawning) {
            float spawnCurve = Mth.sin(spawnProgress * (float)Math.PI);

            this.LeftArm.xRot = -0.5F - spawnCurve * 1.2F;
            this.LeftArm.zRot = -spawnCurve * 0.8F;
            this.RightArm.xRot = -0.5F - spawnCurve * 1.2F;
            this.RightArm.zRot = spawnCurve * 0.8F;

            this.Torso.xRot = -spawnCurve * 0.3F;

            this.Head.xRot = -spawnCurve * 0.4F;

            return;
        }
        if (limbSwingAmount > 0.01F) {
            float legSwing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
            this.LeftLeg.xRot = legSwing;
            this.RightLeg.xRot = -legSwing;

            float armSwing = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 0.8F * limbSwingAmount;
            float twitchTime = ageInTicks * 0.3F;
            float twitch = Mth.sin(twitchTime) * 0.05F;

            this.LeftArm.xRot = -armSwing + twitch;
            this.RightArm.xRot = armSwing - twitch;

            this.LeftArm.zRot = Mth.cos(limbSwing * 0.4F) * 0.15F * limbSwingAmount - 0.1F;
            this.RightArm.zRot = -Mth.cos(limbSwing * 0.4F) * 0.15F * limbSwingAmount + 0.1F;

            float lurch = Mth.sin(limbSwing * 0.6662F) * 0.08F * limbSwingAmount;
            this.Torso.xRot = lurch + 0.1F;
            this.Torso.yRot = Mth.cos(limbSwing * 0.3331F) * 0.12F * limbSwingAmount;
        }

        float breatheTime = ageInTicks * 0.08F;
        float breathe = Mth.sin(breatheTime) * 0.04F;
        float breathePulse = Mth.sin(breatheTime * 2.0F) * 0.02F;

        this.Torso.y += breathe + breathePulse;
        this.Torso.xRot += breathePulse * 2.0F;

        float headSway = Mth.sin(ageInTicks * 0.05F) * 0.04F;
        float headTwitch = Mth.sin(ageInTicks * 0.15F) * 0.015F;

        this.Head.yRot = headSway + headTwitch;
        this.Head.xRot = Mth.cos(ageInTicks * 0.04F) * 0.03F + headTwitch;

        if (ageInTicks % 180 < 2) {
            this.Head.yRot += 0.15F;
        }

        this.Head.zRot = Mth.sin(ageInTicks * 0.4F) * 0.02F;
        if (ageInTicks % 160 < 3) {
            float jerkAmount = ((ageInTicks % 160) / 3.0F);
            this.LeftArm.xRot += Mth.sin(jerkAmount * (float)Math.PI) * 0.4F;
            this.RightArm.xRot -= Mth.sin(jerkAmount * (float)Math.PI) * 0.4F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        Hive.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return Hive;
    }
}