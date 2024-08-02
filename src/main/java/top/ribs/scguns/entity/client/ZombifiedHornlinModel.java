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
import top.ribs.scguns.entity.monster.ZombifiedHornlinEntity;

public class ZombifiedHornlinModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart Hornlin;
	private final ModelPart head;
	final ModelPart Flash;

	public ZombifiedHornlinModel(ModelPart root) {
		this.Hornlin = root.getChild("Hornlin");
		this.head = this.Hornlin.getChild("head");
		this.Flash = this.Hornlin.getChild("left_arm").getChild("Freyr").getChild("Flash");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Hornlin = partdefinition.addOrReplaceChild("Hornlin", CubeListBuilder.create(), PartPose.offset(0.0F, 25.25F, 0.0F));

		PartDefinition head = Hornlin.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 19).addBox(-4.913F, -4.5681F, -4.207F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(17, 35).addBox(-1.913F, -0.5681F, -5.207F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(28, 22).addBox(2.087F, 1.4319F, -5.207F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(28, 19).addBox(-2.913F, 1.4319F, -5.207F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.087F, -27.4658F, 0.1809F));

		PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 19).addBox(-1.0F, -1.25F, -1.25F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8862F, -1.5367F, -3.9901F, 1.1249F, 0.1704F, -0.0665F));

		PartDefinition head_r2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(0, 23).addBox(1.5F, -1.9F, -4.75F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.087F, -5.5681F, -0.207F, 0.5672F, 0.0F, 0.0F));

		PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(36, 25).addBox(-3.5933F, 7.5607F, -0.6526F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(0, 57).addBox(-2.8433F, 4.0607F, -2.6526F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.587F, -7.5253F, 0.4456F, 0.0F, 0.0F, -0.6109F));

		PartDefinition left_arm = Hornlin.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(17, 52).addBox(-5.0F, 5.25F, -2.0F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(40, 0).addBox(-5.0F, -0.75F, -3.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -21.1888F, 2.1585F, -1.5265F, -0.1744F, -0.0077F));

		PartDefinition Freyr = left_arm.addOrReplaceChild("Freyr", CubeListBuilder.create().texOffs(53, 56).addBox(-1.5018F, -7.9167F, -2.503F, 3.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(58, 26).addBox(1.4982F, -5.9167F, -2.253F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(54, 0).addBox(2.4982F, -5.4167F, -1.753F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(57, 17).addBox(-1.5018F, -4.9167F, 1.497F, 3.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9982F, 16.9167F, -3.497F, 0.0F, 0.0F, 0.1309F));

		PartDefinition cube_r1 = Freyr.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(31, 6).addBox(-0.5F, -5.0F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0518F, -3.9167F, -0.003F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r2 = Freyr.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 35).addBox(0.0F, -5.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 6).addBox(-0.5F, -3.0F, -0.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-0.5F, -9.0F, -0.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0018F, 9.0833F, -1.253F, 0.0F, -0.7854F, 0.0F));

		PartDefinition Flash = Freyr.addOrReplaceChild("Flash", CubeListBuilder.create().texOffs(25, 0).addBox(-3.0F, 7.25F, -3.75F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(25, 0).addBox(-3.0F, 7.25F, -3.75F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_arm = Hornlin.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(39, 48).addBox(1.0F, 5.25F, -2.0F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(24, 39).addBox(1.0F, -0.75F, -3.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4127F, -22.307F, -0.6788F, -1.1201F, 0.7417F, 0.4083F));

		PartDefinition body = Hornlin.addOrReplaceChild("body", CubeListBuilder.create().texOffs(29, 28).addBox(-5.5F, -5.0F, -3.5F, 11.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-5.5F, -1.0F, -5.0F, 11.0F, 10.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(29, 58).addBox(5.5F, 5.0F, -3.05F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(57, 48).addBox(5.5F, 4.0F, -3.05F, 3.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(57, 48).mirror().addBox(-8.5F, 4.0F, -3.05F, 3.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(29, 58).mirror().addBox(-7.5F, 5.0F, -3.05F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -19.0F, 0.0F));

		PartDefinition left_leg = Hornlin.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 48).addBox(-5.75F, 11.0F, -3.0F, 5.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 35).addBox(-5.75F, 5.0F, -4.0F, 5.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, -15.0F, 0.0F));

		PartDefinition right_leg = Hornlin.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(44, 39).addBox(0.75F, 11.0F, -3.0F, 5.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(33, 12).addBox(0.75F, 5.0F, -4.0F, 5.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, -15.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity instanceof ZombifiedHornlinEntity) {
			ZombifiedHornlinEntity redcoat = (ZombifiedHornlinEntity) entity;
			this.animateWalk(ModAnimationDefinitions.HORNLIN_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
			this.animate(redcoat.idleAnimationState, ModAnimationDefinitions.HORNLIN_IDLE, ageInTicks, 1f);
			this.animate(redcoat.attackAnimationState, ModAnimationDefinitions.HORNLIN_IDLE, ageInTicks, 1f);

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
		Hornlin.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return Hornlin;
	}
}



