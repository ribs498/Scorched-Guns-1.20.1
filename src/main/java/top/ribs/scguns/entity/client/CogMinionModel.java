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
import top.ribs.scguns.entity.monster.CogMinionEntity;

public class CogMinionModel<T extends Entity> extends HierarchicalModel<T> implements ArmedModel {
	private final ModelPart main;
	private final ModelPart bone;
	private final ModelPart head;

	public CogMinionModel(ModelPart root) {
		this.main = root.getChild("CogMinion");
		this.head = this.main.getChild("Full").getChild("head");
		this.bone = this.main.getChild("Full").getChild("body").getChild("hand").getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition CogMinion = partdefinition.addOrReplaceChild("CogMinion", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Full = CogMinion.addOrReplaceChild("Full", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = Full.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -27.0F, 0.0F));

		PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(36, 23).addBox(-4.0F, -35.0F, -4.0F, 9.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 27.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition body = Full.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 31).addBox(-4.0F, -12.0F, -5.0F, 9.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(58, 0).addBox(-3.0F, -15.0F, -4.0F, 7.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(60, 39).addBox(-3.0F, -22.0F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(44, 57).addBox(-3.0F, -27.0F, -4.0F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition Backpack = body.addOrReplaceChild("Backpack", CubeListBuilder.create().texOffs(0, 44).addBox(-8.0F, -27.0F, -5.0F, 5.0F, 9.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(32, 54).addBox(-6.0F, -29.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(62, 22).addBox(-8.0F, -18.0F, -4.0F, 5.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Cog = Backpack.addOrReplaceChild("Cog", CubeListBuilder.create().texOffs(32, 17).addBox(-0.91F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 8).addBox(-1.41F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.09F, -22.25F, 0.0F));

		PartDefinition cube_r1 = Cog.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 31).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.115F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r2 = Cog.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.115F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r3 = Cog.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(28, 31).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.09F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition hand = body.addOrReplaceChild("hand", CubeListBuilder.create().texOffs(64, 57).addBox(1.7F, -2.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(67, 65).addBox(-4.25F, -2.5F, -2.5F, 7.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 63).addBox(5.7F, -3.0F, -3.0F, 5.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(10.7F, -3.0F, -3.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(40, 0).addBox(10.7F, -3.0F, 0.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -20.0F, 0.0F));

		PartDefinition bone = hand.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(9.0F, -1.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition tread = body.addOrReplaceChild("tread", CubeListBuilder.create().texOffs(18, 54).addBox(-7.5F, 7.0F, -6.0F, 1.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(62, 22).addBox(-3.5F, 3.0F, -7.0F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(58, 0).addBox(1.5F, 3.0F, 6.0F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(44, 39).addBox(-3.5F, 3.0F, 6.0F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-6.5F, 6.0F, -6.0F, 14.0F, 5.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(46, 42).addBox(7.5F, 7.0F, -6.0F, 1.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 17).addBox(-4.5F, 3.0F, -6.0F, 10.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 44).addBox(1.5F, 3.0F, -7.0F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(24, 39).addBox(1.5F, 2.0F, -7.0F, 3.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(38, 3).addBox(-3.5F, 2.0F, -7.0F, 3.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(62, 11).addBox(-0.5F, 2.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}
	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity instanceof CogMinionEntity) {
			CogMinionEntity cogMinion = (CogMinionEntity) entity;
			this.animateWalk(ModAnimationDefinitions.COG_MINION_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
			this.animate(cogMinion.idleAnimationState, ModAnimationDefinitions.COG_MINION_IDLE, ageInTicks, 1f);
			this.animate(cogMinion.attackAnimationState, ModAnimationDefinitions.COG_MINION_ATTACK, ageInTicks, 1f);

			// Add head rotation logic
			float clampedYaw = Mth.clamp(netHeadYaw, -45.0F, 45.0F); // Limit yaw to +/- 45 degrees
			float clampedPitch = Mth.clamp(headPitch, -20.0F, 20.0F); // Limit pitch to +/- 20 degrees

			this.head.yRot = clampedYaw * ((float)Math.PI / 180F); // Convert degrees to radians and apply
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
		this.bone.translateAndRotate(poseStack);
		float x = -0.5F;
		float y = 0.3F;
		float z = 0.3F;
		poseStack.translate(x, y, z);

	}
}