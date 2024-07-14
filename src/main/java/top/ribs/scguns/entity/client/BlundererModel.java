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
import top.ribs.scguns.entity.monster.BlundererEntity;

public class BlundererModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart Blunderer;
	private final ModelPart head;
	final ModelPart Flash;

	public BlundererModel(ModelPart root) {
		this.Blunderer = root.getChild("Blunderer");
		this.head = this.Blunderer.getChild("head");
		this.Flash = this.Blunderer.getChild("right_arm").getChild("Blunderbuss").getChild("Flash");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Blunderer = partdefinition.addOrReplaceChild("Blunderer", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = Blunderer.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 27).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(68, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.25F))
				.texOffs(0, 27).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -24.0F, 0.0F));

		PartDefinition body = Blunderer.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 27).addBox(-4.0F, -24.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(60, 31).addBox(-3.0F, -14.0F, 3.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(60, 40).addBox(-3.0F, -15.0F, 3.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-4.0F, -24.0F, -3.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_arm = Blunderer.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, -24.0F, 0.0F, 0.0F, -0.1745F, 0.0F));

		PartDefinition right_arm_r1 = right_arm.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(0, 57).addBox(-5.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 1.0F, 1.0F, -0.6981F, 0.0F, 0.0F));

		PartDefinition Blunderbuss = right_arm.addOrReplaceChild("Blunderbuss", CubeListBuilder.create().texOffs(28, 0).addBox(-1.5F, -1.5F, 1.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 45).addBox(-1.5F, 1.0651F, 10.6397F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(24, 27).addBox(-1.5F, 4.0651F, 15.6397F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-1.0F, 1.0651F, 19.6397F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 20).addBox(0.0F, 1.5F, 1.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(24, 45).addBox(-1.0F, 0.0F, -9.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2462F, 5.5F, -9.4566F, 0.0F, 0.1745F, 0.0F));

		PartDefinition cube_r1 = Blunderbuss.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(22, 2).addBox(0.0F, -0.5F, 0.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.8F, 1.05F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r2 = Blunderbuss.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(8, 21).addBox(0.0F, -0.5F, -1.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -1.0F, 3.9F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r3 = Blunderbuss.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(34, 8).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.5F, 2.75F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r4 = Blunderbuss.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(34, 0).addBox(-1.51F, -1.0F, -2.5F, 3.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.5F, 7.25F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r5 = Blunderbuss.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(15, 45).addBox(-1.3473F, -1.3597F, -14.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0123F, 2.5F, 2.0F, 0.0F, 0.0F, 0.7679F));

		PartDefinition cube_r6 = Blunderbuss.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(14, 10).addBox(-2.6527F, -1.6403F, -13.0F, 3.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(22, 0).addBox(-1.6527F, -0.6403F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.75F, -0.25F, 0.0F, 0.0F, 0.0F, 0.7679F));

		PartDefinition Flash = Blunderbuss.addOrReplaceChild("Flash", CubeListBuilder.create().texOffs(34, 14).addBox(-5.0F, -5.4F, -13.25F, 10.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_arm = Blunderer.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 57).addBox(0.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -23.0F, -0.75F, -0.5031F, 0.274F, 0.7459F));

		PartDefinition left_leg = Blunderer.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(54, 14).addBox(0.0F, 0.0F, -2.5F, 4.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition right_leg = Blunderer.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 45).addBox(-4.0F, 0.0F, -2.5F, 4.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity instanceof BlundererEntity blunderer) {
            this.animateWalk(ModAnimationDefinitions.REDCOAT_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
			this.animate(blunderer.idleAnimationState, ModAnimationDefinitions.REDCOAT_IDLE, ageInTicks, 1f);
			this.animate(blunderer.attackAnimationState, ModAnimationDefinitions.REDCOAT_IDLE, ageInTicks, 1f);

			float clampedYaw = Mth.clamp(netHeadYaw, -45.0F, 45.0F);
			float clampedPitch = Mth.clamp(headPitch, -20.0F, 20.0F);

			this.head.yRot = clampedYaw * ((float)Math.PI / 180F);
			this.head.xRot = clampedPitch * ((float)Math.PI / 180F);

            this.Flash.visible = blunderer.isMuzzleFlashVisible();
		}
	}



	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Blunderer.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return Blunderer;
	}
}



