package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.entity.animations.ModAnimationDefinitions;
import top.ribs.scguns.entity.monster.SupplyScampEntity;

public class SupplyScampModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart main;
	private final ModelPart[] masks = new ModelPart[16];

	public SupplyScampModel(ModelPart root) {
		this.main = root.getChild("SupplyScamp");
		this.masks[0] = this.main.getChild("head").getChild("MaskBlack");
		this.masks[1] = this.main.getChild("head").getChild("MaskRed");
		this.masks[2] = this.main.getChild("head").getChild("MaskGreen");
		this.masks[3] = this.main.getChild("head").getChild("MaskBrown");
		this.masks[4] = this.main.getChild("head").getChild("MaskBlue");
		this.masks[5] = this.main.getChild("head").getChild("MaskPurple");
		this.masks[6] = this.main.getChild("head").getChild("MaskCyan");
		this.masks[7] = this.main.getChild("head").getChild("MaskLightGray");
		this.masks[8] = this.main.getChild("head").getChild("MaskGray");
		this.masks[9] = this.main.getChild("head").getChild("MaskPink");
		this.masks[10] = this.main.getChild("head").getChild("MaskLime");
		this.masks[11] = this.main.getChild("head").getChild("MaskYellow");
		this.masks[12] = this.main.getChild("head").getChild("MaskLightBlue");
		this.masks[13] = this.main.getChild("head").getChild("MaskMagenta");
		this.masks[14] = this.main.getChild("head").getChild("MaskOrange");
		this.masks[15] = this.main.getChild("head").getChild("MaskWhite");
	}

	@Override
	public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity instanceof SupplyScampEntity supplyScamp) {
			this.animateWalk(ModAnimationDefinitions.SUPPLY_SCAMP_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
			this.animate(supplyScamp.idleAnimationState, ModAnimationDefinitions.SUPPLY_SCAMP_IDLE, ageInTicks, 1f);
			this.animate(supplyScamp.panicAnimationState, ModAnimationDefinitions.SUPPLY_SCAMP_PANIC, ageInTicks, 1f);
			this.animate(supplyScamp.sitAnimationState, ModAnimationDefinitions.SUPPLY_SCAMP_SIT, ageInTicks, 1f);

			for (ModelPart mask : masks) {
				mask.visible = false;
			}

			int maskColor = supplyScamp.getMaskColor();
			if (maskColor >= 0 && maskColor < masks.length) {
				masks[maskColor].visible = supplyScamp.isTame();
			}
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

		PartDefinition MaskBlack = head.addOrReplaceChild("MaskBlack", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r1 = MaskBlack.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(13, 67).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskPink = head.addOrReplaceChild("MaskPink", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r2 = MaskPink.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(13, 81).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskPurple = head.addOrReplaceChild("MaskPurple", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r3 = MaskPurple.addOrReplaceChild("head_r3", CubeListBuilder.create().texOffs(13, 88).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskBlue = head.addOrReplaceChild("MaskBlue", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r4 = MaskBlue.addOrReplaceChild("head_r4", CubeListBuilder.create().texOffs(29, 81).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskRed = head.addOrReplaceChild("MaskRed", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r5 = MaskRed.addOrReplaceChild("head_r5", CubeListBuilder.create().texOffs(13, 95).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskMagenta = head.addOrReplaceChild("MaskMagenta", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r6 = MaskMagenta.addOrReplaceChild("head_r6", CubeListBuilder.create().texOffs(13, 109).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskGreen = head.addOrReplaceChild("MaskGreen", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r7 = MaskGreen.addOrReplaceChild("head_r7", CubeListBuilder.create().texOffs(13, 102).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskLightGreen = head.addOrReplaceChild("MaskLime", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r8 = MaskLightGreen.addOrReplaceChild("head_r8", CubeListBuilder.create().texOffs(29, 88).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskBrown = head.addOrReplaceChild("MaskBrown", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r9 = MaskBrown.addOrReplaceChild("head_r9", CubeListBuilder.create().texOffs(29, 95).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskGray = head.addOrReplaceChild("MaskGray", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r10 = MaskGray.addOrReplaceChild("head_r10", CubeListBuilder.create().texOffs(29, 102).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskLightGray = head.addOrReplaceChild("MaskLightGray", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r11 = MaskLightGray.addOrReplaceChild("head_r11", CubeListBuilder.create().texOffs(29, 109).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskYellow = head.addOrReplaceChild("MaskYellow", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r12 = MaskYellow.addOrReplaceChild("head_r12", CubeListBuilder.create().texOffs(45, 109).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskCyan = head.addOrReplaceChild("MaskCyan", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r13 = MaskCyan.addOrReplaceChild("head_r13", CubeListBuilder.create().texOffs(45, 102).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskLightBlue = head.addOrReplaceChild("MaskLightBlue", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r14 = MaskLightBlue.addOrReplaceChild("head_r14", CubeListBuilder.create().texOffs(45, 95).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskWhite = head.addOrReplaceChild("MaskWhite", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r15 = MaskWhite.addOrReplaceChild("head_r15", CubeListBuilder.create().texOffs(45, 88).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition MaskOrange = head.addOrReplaceChild("MaskOrange", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_r16 = MaskOrange.addOrReplaceChild("head_r16", CubeListBuilder.create().texOffs(45, 81).addBox(-0.2F, -3.75F, -4.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, -0.25F, 0.0F, 0.1309F, 0.0F, 0.0F));

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