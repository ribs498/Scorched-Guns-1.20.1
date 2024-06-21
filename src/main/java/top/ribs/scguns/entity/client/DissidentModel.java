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
import top.ribs.scguns.entity.monster.DissidentEntity;

public class DissidentModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart main;
	private final ModelPart head;

	public DissidentModel(ModelPart root) {
		this.main = root.getChild("dissident");
		this.head = this.main.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition dissident = partdefinition.addOrReplaceChild("dissident", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition back_left_leg = dissident.addOrReplaceChild("back_left_leg", CubeListBuilder.create(), PartPose.offset(6.5F, 4.0F, 14.25F));

		PartDefinition bacK_left_leg2 = back_left_leg.addOrReplaceChild("bacK_left_leg2", CubeListBuilder.create(), PartPose.offset(0.0F, 7.0F, -1.0F));

		PartDefinition front_left_ankle_r1 = bacK_left_leg2.addOrReplaceChild("front_left_ankle_r1", CubeListBuilder.create().texOffs(0, 43).addBox(2.0F, -15.0F, -5.5F, 5.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.75F, 4.0F, 2.5F, 0.1745F, 0.0F, 0.0F));

		PartDefinition front_left_ankle_r2 = bacK_left_leg2.addOrReplaceChild("front_left_ankle_r2", CubeListBuilder.create().texOffs(18, 55).addBox(3.925F, -12.0F, -6.5F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.75F, 10.0F, 2.5F, -0.0873F, 0.0F, 0.0F));

		PartDefinition back_right_leg = dissident.addOrReplaceChild("back_right_leg", CubeListBuilder.create(), PartPose.offset(-6.5F, 4.0F, 14.25F));

		PartDefinition bacK_right_leg2 = back_right_leg.addOrReplaceChild("bacK_right_leg2", CubeListBuilder.create(), PartPose.offset(0.0F, 7.0F, -1.0F));

		PartDefinition front_right_ankle_r1 = bacK_right_leg2.addOrReplaceChild("front_right_ankle_r1", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(-7.0F, -15.0F, -5.5F, 5.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.75F, 4.0F, 2.5F, 0.1745F, 0.0F, 0.0F));

		PartDefinition front_right_ankle_r2 = bacK_right_leg2.addOrReplaceChild("front_right_ankle_r2", CubeListBuilder.create().texOffs(18, 55).mirror().addBox(-7.925F, -12.0F, -6.5F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(6.75F, 10.0F, 2.5F, -0.0873F, 0.0F, 0.0F));

		PartDefinition front_left_leg = dissident.addOrReplaceChild("front_left_leg", CubeListBuilder.create(), PartPose.offset(3.9F, 7.0F, -2.75F));

		PartDefinition front_left_ankle = front_left_leg.addOrReplaceChild("front_left_ankle", CubeListBuilder.create(), PartPose.offset(-0.5F, 0.0F, 0.5F));

		PartDefinition front_left_ankle_r3 = front_left_ankle.addOrReplaceChild("front_left_ankle_r3", CubeListBuilder.create().texOffs(58, 58).addBox(-2.5F, -3.0F, -1.5F, 5.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.85F, -0.7479F, -2.3618F, 0.2618F, 0.0F, 0.0F));

		PartDefinition front_left_ankle_r4 = front_left_ankle.addOrReplaceChild("front_left_ankle_r4", CubeListBuilder.create().texOffs(46, 45).addBox(3.975F, -12.5F, -8.1F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.65F, 14.0F, 3.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition front_right_leg = dissident.addOrReplaceChild("front_right_leg", CubeListBuilder.create(), PartPose.offset(-3.9F, 7.0F, -2.75F));

		PartDefinition front_right_ankle = front_right_leg.addOrReplaceChild("front_right_ankle", CubeListBuilder.create(), PartPose.offset(0.5F, 0.0F, 0.5F));

		PartDefinition front_right_ankle_r3 = front_right_ankle.addOrReplaceChild("front_right_ankle_r3", CubeListBuilder.create().texOffs(58, 58).mirror().addBox(-2.5F, -3.0F, -1.5F, 5.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.85F, -0.7479F, -2.3618F, 0.2618F, 0.0F, 0.0F));

		PartDefinition front_right_ankle_r4 = front_right_ankle.addOrReplaceChild("front_right_ankle_r4", CubeListBuilder.create().texOffs(46, 45).mirror().addBox(-7.975F, -12.5F, -8.1F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.65F, 14.0F, 3.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition body = dissident.addOrReplaceChild("body", CubeListBuilder.create().texOffs(31, 32).addBox(-6.0F, -12.25F, -7.0F, 12.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-7.0F, -10.25F, -10.0F, 14.0F, 13.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(-6.0F, -10.25F, 0.0F, 12.0F, 11.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 4.5F));

		PartDefinition bone3 = body.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 0).addBox(-0.125F, 1.75F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(38, 0).addBox(-0.375F, -1.25F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.375F, -1.75F, -4.5F));

		PartDefinition bone2 = body.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.125F, 1.75F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(38, 0).mirror().addBox(-0.625F, -1.25F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-7.375F, -7.75F, -1.5F));

		PartDefinition head = dissident.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -8.5F));

		PartDefinition mask = head.addOrReplaceChild("mask", CubeListBuilder.create().texOffs(22, 45).addBox(-3.9167F, -5.2083F, -1.25F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(33, 25).addBox(-3.4167F, 0.7917F, -1.0F, 7.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(-0.9167F, -0.7083F, -1.75F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0833F, -1.7917F, 0.25F));

		PartDefinition head_rotation = head.addOrReplaceChild("head_rotation", CubeListBuilder.create().texOffs(39, 14).addBox(-4.0F, -0.9179F, -8.9276F, 8.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(4, 0).addBox(-3.75F, -2.9179F, -7.7204F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(2, 0).addBox(-3.75F, -2.9179F, -5.4704F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-3.75F, -2.9179F, -3.6704F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(3.75F, -2.9179F, -3.6704F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(2, 0).addBox(3.75F, -2.9179F, -5.4704F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(4, 0).addBox(3.75F, -2.9179F, -7.7204F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.775F, 3.625F, 0.8727F, 0.0F, 0.0F));

		PartDefinition tongue = head_rotation.addOrReplaceChild("tongue", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.3225F, -1.7298F, 0.8727F, 0.0F, 0.0F));

		PartDefinition tongue_r1 = tongue.addOrReplaceChild("tongue_r1", CubeListBuilder.create().texOffs(38, 0).addBox(-2.0F, -0.6148F, -1.839F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -0.0865F, -0.0237F, 2.0508F, 0.0F, 0.0F));

		PartDefinition tongue2 = tongue.addOrReplaceChild("tongue2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.1F, -5.1841F, -2.442F, 0.8727F, 0.0F, 0.0F));

		PartDefinition tongue2_r1 = tongue2.addOrReplaceChild("tongue2_r1", CubeListBuilder.create().texOffs(54, 0).addBox(-1.575F, -2.0293F, -0.6851F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, -0.7005F, 1.1671F, 1.789F, 0.0F, 0.0F));

		PartDefinition tongue3 = tongue2.addOrReplaceChild("tongue3", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.3F, -4.7492F, -1.1454F, -2.4435F, 0.0F, 0.0F));

		PartDefinition tongue3_r1 = tongue3.addOrReplaceChild("tongue3_r1", CubeListBuilder.create().texOffs(55, 25).addBox(-1.425F, -1.4445F, -4.4744F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -0.2292F, 0.129F, 1.5708F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}
	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity instanceof DissidentEntity) {
			DissidentEntity dissisdent = (DissidentEntity) entity;
			this.animateWalk(ModAnimationDefinitions.DISSIDENT_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
			this.animate(dissisdent.idleAnimationState, ModAnimationDefinitions.DISSIDENT_IDLE, ageInTicks, 1f);
			this.animate(dissisdent.attackAnimationState, ModAnimationDefinitions.DISSIDENT_ATTACK, ageInTicks, 1f);

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
}