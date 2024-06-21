package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import top.ribs.scguns.entity.animations.ModAnimationDefinitions;
import top.ribs.scguns.entity.monster.HiveEntity;

public class HiveModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart Hive;

	public HiveModel(ModelPart root) {
		this.Hive = root.getChild("Hive");
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

		this.animateWalk(ModAnimationDefinitions.HIVE_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
		this.animate(((HiveEntity) entity).idleAnimationState, ModAnimationDefinitions.HIVE_IDLE, ageInTicks, 1f);
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