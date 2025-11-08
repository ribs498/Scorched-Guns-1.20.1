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
import top.ribs.scguns.entity.monster.CogMinionEntity;


public class CogMinionModel<T extends Entity> extends HierarchicalModel<T> implements ArmedModel {
    private final ModelPart main;
    final ModelPart head;
    private final ModelPart body;
    private final ModelPart tread;
    private final ModelPart cog;
    private final ModelPart hand;

    private final PartPose headDefault;
    private final PartPose bodyDefault;
    private final PartPose treadDefault;
    private final PartPose cogDefault;
    private final PartPose handDefault;

    private float attackStartTime = -1;
    private int lastAttackTimeout = 0;
    private static final float ATTACK_DURATION = 12.0f;

    public CogMinionModel(ModelPart root) {
        this.main = root;
        this.body = root.getChild("body");
        this.tread = root.getChild("tread");
        this.head = root.getChild("head");
        this.cog = root.getChild("cog");
        this.hand = root.getChild("hand");

        this.headDefault = this.head.storePose();
        this.bodyDefault = this.body.storePose();
        this.treadDefault = this.tread.storePose();
        this.cogDefault = this.cog.storePose();
        this.handDefault = this.hand.storePose();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 4.5625F, 3.25F));

        PartDefinition hand_r1 = body.addOrReplaceChild("hand_r1", CubeListBuilder.create().texOffs(67, 65).addBox(-0.75F, -3.25F, -2.5F, 7.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 44).addBox(-5.5F, -7.75F, -5.0F, 5.0F, 9.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(32, 54).addBox(-3.5F, -9.75F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(62, 22).addBox(-5.5F, 1.25F, -4.0F, 5.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(8, 35).addBox(0.5F, 10.25F, -3.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 31).addBox(-1.5F, 7.25F, -5.0F, 9.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(58, 0).addBox(-0.5F, 4.25F, -4.0F, 7.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(60, 39).addBox(-0.5F, -2.75F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(70, 65).addBox(0.5F, -8.75F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(44, 57).addBox(-0.5F, -7.75F, -4.0F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.1875F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition tread = partdefinition.addOrReplaceChild("tread", CubeListBuilder.create(), PartPose.offset(-0.0455F, 15.2273F, 0.6364F));

        PartDefinition tread_r1 = tread.addOrReplaceChild("tread_r1", CubeListBuilder.create().texOffs(62, 11).addBox(-0.5F, -3.5F, -5.5F, 2.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(39, 4).addBox(-3.5F, -3.5F, -6.5F, 3.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(25, 40).addBox(1.5F, -3.5F, -6.5F, 3.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(0, 44).addBox(1.5F, -2.5F, -6.5F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(1, 18).addBox(-4.5F, -2.5F, -5.5F, 10.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(47, 43).addBox(7.0F, 1.5F, -5.5F, 1.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(-6.0F, 0.5F, -5.5F, 13.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(44, 39).addBox(-3.5F, -2.5F, 5.5F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(58, 0).addBox(1.5F, -2.5F, 5.5F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(62, 22).addBox(-3.5F, -2.5F, -6.5F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0455F, 3.2727F, 0.3636F, 0.0F, 1.5708F, 0.0F));

        PartDefinition tread_r2 = tread.addOrReplaceChild("tread_r2", CubeListBuilder.create().texOffs(19, 55).addBox(-8.0F, 1.5F, -5.5F, 1.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0455F, 3.2727F, -0.6364F, 0.0F, 1.5708F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, 1.0F));

        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(36, 23).addBox(-4.5F, -4.0F, -4.0F, 9.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cog = partdefinition.addOrReplaceChild("cog", CubeListBuilder.create(), PartPose.offset(0.0F, 1.75F, 9.7525F));

        PartDefinition cog_r1 = cog.addOrReplaceChild("cog_r1", CubeListBuilder.create().texOffs(0, 8).addBox(-1.2625F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(-0.7375F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.1475F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cog_r2 = cog.addOrReplaceChild("cog_r2", CubeListBuilder.create().texOffs(32, 17).addBox(-0.7625F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.1475F, 0.0F, 1.5708F, 0.7854F));

        PartDefinition cog_r3 = cog.addOrReplaceChild("cog_r3", CubeListBuilder.create().texOffs(28, 31).addBox(-0.7625F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.1475F, 0.0F, 1.5708F, -0.7854F));

        PartDefinition cog_r4 = cog.addOrReplaceChild("cog_r4", CubeListBuilder.create().texOffs(0, 31).addBox(-0.7375F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.1475F, 0.0F, 1.5708F, 1.5708F));

        PartDefinition hand = partdefinition.addOrReplaceChild("hand", CubeListBuilder.create(), PartPose.offset(0.0F, 3.25F, -2.5F));

        PartDefinition hand_r2 = hand.addOrReplaceChild("hand_r2", CubeListBuilder.create().texOffs(62, 57).addBox(-7.75F, -1.0F, -2.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 63).addBox(-1.75F, -2.0F, -3.0F, 5.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(3.25F, -2.0F, -3.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(40, 0).addBox(3.25F, -2.0F, 0.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.25F, -1.75F, 0.0F, 1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.loadPose(this.headDefault);
        this.body.loadPose(this.bodyDefault);
        this.tread.loadPose(this.treadDefault);
        this.cog.loadPose(this.cogDefault);
        this.hand.loadPose(this.handDefault);

        if (entity instanceof CogMinionEntity cogMinion) {
            this.cog.zRot = (ageInTicks * 0.15f) % ((float)Math.PI * 2);

            if (limbSwingAmount > 0.01f) {
                float treadBob = Mth.abs(Mth.sin(limbSwing * 0.6662f)) * limbSwingAmount * 0.5f;
                this.tread.y += treadBob;
            }

            if (cogMinion.isAttacking() && cogMinion.getAttackTimeout() > 0) {
                animateAttackSmooth(cogMinion.getAttackTimeout(), ageInTicks);
            } else {
                attackStartTime = -1;
                lastAttackTimeout = 0;
            }

            float clampedYaw = Mth.clamp(netHeadYaw, -45.0F, 45.0F);
            float clampedPitch = Mth.clamp(headPitch, -20.0F, 20.0F);

            this.head.yRot = clampedYaw * ((float)Math.PI / 180F);
            this.head.xRot = clampedPitch * ((float)Math.PI / 180F);
        }
    }

    private void animateAttackSmooth(int attackTimeout, float ageInTicks) {
        if (attackTimeout <= 0) {
            this.hand.z = 0.0f;
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

        this.hand.z = -swingCurve * 7.0f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        tread.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        cog.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        hand.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return main;
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        this.hand.translateAndRotate(poseStack);

        poseStack.translate(0.0F, -0.05F, 0.15F);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0F));
        poseStack.scale(1.0F, 1.0F, 1.0F);
    }
}