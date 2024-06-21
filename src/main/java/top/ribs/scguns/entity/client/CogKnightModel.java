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
import top.ribs.scguns.entity.monster.CogKnightEntity;

public class CogKnightModel<T extends Entity> extends HierarchicalModel<T> implements ArmedModel {
	private final ModelPart main;
	private final ModelPart bone;
	private final ModelPart head;
	public CogKnightModel(ModelPart root) {
		this.main = root.getChild("CogKnight");
		this.head = this.main.getChild("Full").getChild("Head");
		this.bone = this.main.getChild("Full").getChild("LeftArm");
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

		PartDefinition LeftHand = LeftArm.addOrReplaceChild("LeftHand", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mace = LeftHand.addOrReplaceChild("Mace", CubeListBuilder.create().texOffs(74, 34).addBox(-0.5F, -1.0F, 12.5F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(33, 23).addBox(-1.0F, -2.0F, 11.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(68, 10).addBox(-0.5F, -0.5F, 1.5F, 1.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(62, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(54, 52).addBox(0.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 16.0F, -15.5F));

		PartDefinition leftarm_r1 = Mace.addOrReplaceChild("leftarm_r1", CubeListBuilder.create().texOffs(54, 52).addBox(0.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition leftarm_r2 = Mace.addOrReplaceChild("leftarm_r2", CubeListBuilder.create().texOffs(0, 20).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 18.5F, 0.7418F, 0.0F, 0.0F));

		PartDefinition RightHand = Full.addOrReplaceChild("RightHand", CubeListBuilder.create(), PartPose.offset(6.0F, -7.8889F, 0.1111F));

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
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity instanceof CogKnightEntity) {
			CogKnightEntity cogKnight = (CogKnightEntity) entity;
			this.animateWalk(ModAnimationDefinitions.COG_KNIGHT_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
			this.animate(cogKnight.idleAnimationState, ModAnimationDefinitions.COG_KNIGHT_IDLE, ageInTicks, 1f);
			this.animate(cogKnight.attackAnimationState, ModAnimationDefinitions.COG_KNIGHT_ATTACK, ageInTicks, 1f);
			float clampedYaw = Mth.clamp(netHeadYaw, -75.0F, 75.0F);
			float clampedPitch = Mth.clamp(headPitch, -30.0F, 30.0F);
			this.head.yRot = clampedYaw * ((float)Math.PI / 180F);
			this.head.xRot = clampedPitch * ((float)Math.PI / 180F);
		}
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
		ModelPart armPart = arm == HumanoidArm.LEFT ? this.bone : this.main.getChild("Full").getChild("LeftArm").getChild("LeftHand"); // Assuming the right hand path
		armPart.translateAndRotate(poseStack);
		float x = -0.15F;
		float y = 1.0F;
		float z = -0.1F;
		poseStack.translate(x, y, z);
	}
}
/*
@Override
public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
	this.bone.translateAndRotate(poseStack);
	float x = -0.15F;
	float y = 1.0F;
	float z = -0.1F;
	poseStack.translate(x, y, z);
}*/
