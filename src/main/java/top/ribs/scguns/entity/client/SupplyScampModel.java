package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import top.ribs.scguns.entity.animations.ModAnimationDefinitions;
import top.ribs.scguns.entity.monster.SupplyScampEntity;

public class SupplyScampModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart main;

    public SupplyScampModel(ModelPart main) {
        this.main = main;
    }
	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity instanceof SupplyScampEntity) {
			SupplyScampEntity supplyScamp = (SupplyScampEntity) entity;
			this.animateWalk(ModAnimationDefinitions.SUPPLY_SCAMP_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
			this.animate(supplyScamp.idleAnimationState, ModAnimationDefinitions.SUPPLY_SCAMP_IDLE, ageInTicks, 1f);
			this.animate(supplyScamp.panicAnimationState, ModAnimationDefinitions.SUPPLY_SCAMP_PANIC, ageInTicks, 1f);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition SupplyScamp = partdefinition.addOrReplaceChild("SupplyScamp", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition tread = SupplyScamp.addOrReplaceChild("tread", CubeListBuilder.create().texOffs(48, 69).addBox(-8.5F, 7.0F, -7.0F, 1.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 21).addBox(-6.5F, 5.0F, -8.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 13).addBox(4.5F, 5.0F, 7.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(10, 0).addBox(-6.5F, 5.0F, 7.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 40).addBox(-7.5F, 6.0F, -7.0F, 16.0F, 5.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(32, 59).addBox(8.5F, 7.0F, -7.0F, 1.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 10).addBox(4.5F, 5.0F, -8.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, 0.0F));

		PartDefinition body = SupplyScamp.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 3.75F, 0.0F));

		PartDefinition body_r1 = body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(51, 0).addBox(-5.0F, -18.5F, -9.0F, 10.0F, 10.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(24, 59).addBox(-2.0F, -9.75F, 4.0F, 4.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(64, 69).addBox(-8.0F, -15.75F, -7.0F, 1.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(9, 9).addBox(-2.0F, -20.75F, -8.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 21).addBox(5.0F, -15.75F, -8.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 28).addBox(-8.0F, -15.75F, -8.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 75).addBox(7.0F, -15.75F, -7.0F, 1.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(54, 35).addBox(-2.0F, -20.75F, -7.0F, 4.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(74, 35).addBox(-2.0F, -17.75F, 4.0F, 4.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(7, 26).addBox(-2.0F, -19.75F, 4.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(49, 21).addBox(-6.0F, -16.75F, 4.0F, 12.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(49, 48).addBox(-7.0F, -19.75F, -7.0F, 14.0F, 10.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(0, 21).addBox(-8.0F, -9.75F, -8.0F, 16.0F, 2.0F, 17.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-8.0F, -7.75F, -9.0F, 16.0F, 2.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -4.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition body_r2 = body.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(8, 29).addBox(6.0F, -15.75F, 6.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -4.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition body_r3 = body.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(0, 32).addBox(6.0F, -15.75F, -7.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -4.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition body_r4 = body.addOrReplaceChild("body_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -17.75F, 12.0F, 4.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -4.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition head = SupplyScamp.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 59).addBox(-4.0F, -4.25F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -13.5F, 0.0F));

		PartDefinition Cog = SupplyScamp.addOrReplaceChild("Cog", CubeListBuilder.create(), PartPose.offset(2.0F, -22.21F, 0.0F));

		PartDefinition cube_r1 = Cog.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(46, 40).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.06F, 0.0F, 0.7854F, 0.0F, 1.5708F));

		PartDefinition cube_r2 = Cog.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.06F, 0.0F, -0.7854F, 0.0F, 1.5708F));

		PartDefinition cube_r3 = Cog.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(46, 40).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.04F, 0.0F, 1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r4 = Cog.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(6, 33).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 40).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.04F, 0.0F, 0.0F, 0.0F, 1.5708F));

		return LayerDefinition.create(meshdefinition, 128, 128);
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