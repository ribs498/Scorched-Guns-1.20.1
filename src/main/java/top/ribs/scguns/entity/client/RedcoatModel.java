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
import top.ribs.scguns.entity.monster.RedcoatEntity;

public class RedcoatModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart Redcoat;
	private final ModelPart head;
	final ModelPart Flash;

	public RedcoatModel(ModelPart root) {
		this.Redcoat = root.getChild("Redcoat");
		this.head = this.Redcoat.getChild("head");
		this.Flash = this.Redcoat.getChild("right_arm").getChild("Musket").getChild("Flash");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Redcoat = partdefinition.addOrReplaceChild("Redcoat", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = Redcoat.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18, 18).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-4.5F, -8.75F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -24.0F, 0.0F));

		PartDefinition body = Redcoat.addOrReplaceChild("body", CubeListBuilder.create().texOffs(36, 0).addBox(-4.0F, -24.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_arm = Redcoat.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, -24.0F, 0.0F, 0.0F, -0.1745F, 0.0F));

		PartDefinition right_arm_r1 = right_arm.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-5.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 1.0F, 1.0F, -0.6981F, 0.0F, 0.0F));

		PartDefinition Musket = right_arm.addOrReplaceChild("Musket", CubeListBuilder.create().texOffs(15, 35).addBox(-1.5F, -1.5F, 1.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 39).addBox(-1.5F, 1.0651F, 10.6397F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(40, 36).addBox(-1.5F, 4.0651F, 15.6397F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(18, 18).addBox(-1.0F, 1.0651F, 19.6397F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(0.0F, 1.5F, 1.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(40, 24).addBox(-1.0F, 0.0F, -9.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(40, 29).addBox(0.0F, 1.0F, -22.0F, 0.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2462F, 5.5F, -9.4566F, 0.0F, 0.1745F, 0.0F));

		PartDefinition cube_r1 = Musket.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, -0.5F, 0.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.8F, 1.05F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r2 = Musket.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 4).addBox(0.0F, -0.5F, -1.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -1.0F, 3.9F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r3 = Musket.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.5F, 2.75F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r4 = Musket.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(22, 34).addBox(-1.51F, -1.0F, -2.5F, 3.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.5F, 7.25F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r5 = Musket.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(42, 16).addBox(-1.3473F, -1.3597F, -14.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0123F, 2.0F, 0.0F, 0.0F, 0.0F, 0.7679F));

		PartDefinition cube_r6 = Musket.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 18).addBox(-0.6527F, 0.3597F, -15.0F, 1.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.75F, -1.0F, 0.0F, 0.0F, 0.0F, 0.7679F));

		PartDefinition Flash = Musket.addOrReplaceChild("Flash", CubeListBuilder.create().texOffs(52, 44).addBox(-4.0F, -4.4F, -15.25F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_arm = Redcoat.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 51).addBox(0.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -23.0F, -0.75F, -0.5031F, 0.274F, 0.7459F));

		PartDefinition left_leg = Redcoat.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 48).addBox(0.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition right_leg = Redcoat.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 48).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity instanceof RedcoatEntity) {
			RedcoatEntity redcoat = (RedcoatEntity) entity;
			this.animateWalk(ModAnimationDefinitions.REDCOAT_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
			this.animate(redcoat.idleAnimationState, ModAnimationDefinitions.REDCOAT_IDLE, ageInTicks, 1f);
			this.animate(redcoat.attackAnimationState, ModAnimationDefinitions.REDCOAT_IDLE, ageInTicks, 1f);

			float clampedYaw = Mth.clamp(netHeadYaw, -45.0F, 45.0F);
			float clampedPitch = Mth.clamp(headPitch, -20.0F, 20.0F);

			this.head.yRot = clampedYaw * ((float)Math.PI / 180F);
			this.head.xRot = clampedPitch * ((float)Math.PI / 180F);

			boolean muzzleFlashVisible = redcoat.isMuzzleFlashVisible();
			this.Flash.visible = muzzleFlashVisible;
		}
	}



	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Redcoat.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return Redcoat;
	}
}



