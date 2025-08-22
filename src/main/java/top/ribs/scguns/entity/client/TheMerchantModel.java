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
import top.ribs.scguns.entity.monster.TheMerchantEntity;

public class TheMerchantModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart main;
    private final ModelPart torso;
    private final ModelPart head;

    public TheMerchantModel(ModelPart root) {
        this.main = root.getChild("Full");
        this.torso = this.main.getChild("Torso");
        this.head = this.torso.getChild("Head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Full = partdefinition.addOrReplaceChild("Full", CubeListBuilder.create(), PartPose.offset(0.0F, 21.0F, 0.0F));

        PartDefinition Torso = Full.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(70, 55).addBox(-8.0F, 25.4658F, -9.7844F, 16.0F, 12.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -41.9658F, 3.7844F));

        PartDefinition cube_r1 = Torso.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, 1.0F, -7.0F, 22.0F, 12.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition cube_r2 = Torso.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 55).addBox(-6.5F, -4.1F, -11.7F, 13.0F, 5.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 33.9706F, 3.848F, -0.8727F, 0.0F, 0.0F));

        PartDefinition cube_r3 = Torso.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 28).addBox(-7.0F, -8.0F, -8.5F, 15.0F, 5.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 43.4658F, -1.2844F, -0.2618F, 0.0F, 0.0F));

        PartDefinition cube_r4 = Torso.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(76, 0).addBox(-7.0F, 11.5F, -7.0F, 14.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.9658F, -0.7844F, 0.0611F, 0.0F, 0.0F));

        PartDefinition cube_r5 = Torso.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(74, 28).addBox(-10.0F, 4.0F, -7.0F, 20.0F, 9.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.9658F, -0.7844F, -0.0873F, 0.0F, 0.0F));

        PartDefinition cube_r6 = Torso.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 111).addBox(-3.0F, -4.75F, 0.25F, 6.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 14.7158F, -9.7844F, 0.6545F, 0.0F, 0.0F));

        PartDefinition Backpack = Torso.addOrReplaceChild("Backpack", CubeListBuilder.create(), PartPose.offset(0.0F, 15.5498F, 9.9325F));

        PartDefinition cube_r7 = Backpack.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(65, 55).addBox(4.0F, 11.0F, 7.0F, 2.0F, 10.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 57).addBox(-10.0F, 11.0F, 7.0F, 2.0F, 10.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(106, 115).addBox(-8.0F, 5.0F, 13.0F, 12.0F, 17.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 82).addBox(-8.0F, 2.0F, 6.0F, 12.0F, 22.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -15.5498F, -9.9325F, -0.0873F, 0.0F, 0.0F));

        PartDefinition Post = Backpack.addOrReplaceChild("Post", CubeListBuilder.create(), PartPose.offset(6.6667F, -10.5452F, 0.1771F));

        PartDefinition cube_r8 = Post.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(58, 45).addBox(3.5F, 9.0F, 8.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6667F, -8.2546F, -10.8596F, 0.192F, 0.0F, 0.0F));

        PartDefinition cube_r9 = Post.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(22, 111).addBox(3.5F, -9.0F, 8.1F, 2.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6667F, -10.2546F, -10.8596F, 0.192F, 0.0F, 0.0F));

        PartDefinition cube_r10 = Post.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(66, 28).addBox(1.0F, -2.0F, -12.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.1667F, -8.5062F, -5.5722F, -1.4835F, 0.0F, 0.0F));

        PartDefinition Lantern = Post.addOrReplaceChild("Lantern", CubeListBuilder.create(), PartPose.offset(-0.1542F, -17.991F, -12.9617F));

        PartDefinition cube_r11 = Lantern.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 0).addBox(4.675F, -1.0F, -2.25F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 57).addBox(4.675F, -1.0F, 5.75F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(131, 0).addBox(3.675F, -2.0F, -1.25F, 6.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.6875F, 3.875F, 0.975F, -1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r12 = Lantern.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 5).addBox(-0.5F, 0.4F, -2.25F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0375F, 1.875F, 1.475F, -1.5708F, 0.0F, 0.0F));

        PartDefinition Gun2 = Torso.addOrReplaceChild("Gun2", CubeListBuilder.create(), PartPose.offsetAndRotation(-9.25F, 33.2149F, -2.4563F, 0.0436F, 0.0F, 0.0F));

        PartDefinition cube_r13 = Gun2.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(49, 55).addBox(-1.0F, -3.5F, -4.0F, 3.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -3.2149F, 0.9563F, 0.1745F, 0.0F, 0.0F));

        PartDefinition Gun = Gun2.addOrReplaceChild("Gun", CubeListBuilder.create().texOffs(14, 38).addBox(0.0518F, 1.5296F, 0.2056F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(-1.0F, -1.6232F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-0.5F, -1.8732F, -3.5F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(52, 35).addBox(-0.4482F, 1.1268F, -2.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 55).addBox(-1.1982F, 1.1268F, -3.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 55).addBox(-1.1982F, -1.5732F, -3.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 43).addBox(-1.5F, -0.6232F, -3.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 55).addBox(-0.9482F, 0.6268F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 46).addBox(-0.9482F, -1.3732F, 2.0F, 2.0F, 2.0F, 1.5F, new CubeDeformation(0.0F))
                .texOffs(12, 55).addBox(-1.0F, -0.1482F, 3.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -4.5317F, -0.9269F, 1.7453F, 0.0F, 0.0F));

        PartDefinition gun_body_r1 = Gun.addOrReplaceChild("gun_body_r1", CubeListBuilder.create().texOffs(52, 28).addBox(-1.0F, -1.0F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition gun_body_r2 = Gun.addOrReplaceChild("gun_body_r2", CubeListBuilder.create().texOffs(0, 28).addBox(-1.13F, -1.13F, -2.8325F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.0662F, -8.6675F, 0.0F, 0.0F, 0.7854F));

        PartDefinition gun_body_r3 = Gun.addOrReplaceChild("gun_body_r3", CubeListBuilder.create().texOffs(0, 45).addBox(-1.25F, -0.75F, -2.2075F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1982F, 1.9818F, -5.3F, 0.0F, 0.0F, 0.7854F));

        PartDefinition gun_body_r4 = Gun.addOrReplaceChild("gun_body_r4", CubeListBuilder.create().texOffs(52, 45).addBox(-0.3763F, -1.4327F, -0.8714F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0987F, 2.0138F, 3.7255F, -0.7854F, 0.0F, 0.0F));

        PartDefinition gun_body_r5 = Gun.addOrReplaceChild("gun_body_r5", CubeListBuilder.create().texOffs(18, 46).addBox(-0.6363F, -0.9327F, -0.3714F, 1.26F, 1.5F, 1.25F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0581F, 1.2781F, 2.6687F, -0.3927F, 0.0F, 0.0F));

        PartDefinition gun_body_r6 = Gun.addOrReplaceChild("gun_body_r6", CubeListBuilder.create().texOffs(52, 41).addBox(-0.3852F, 0.3372F, -0.6137F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1148F, 2.0177F, 4.1269F, 0.3927F, 0.0F, 0.0F));

        PartDefinition gun_body_r7 = Gun.addOrReplaceChild("gun_body_r7", CubeListBuilder.create().texOffs(18, 48).addBox(-0.345F, -0.625F, -0.375F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.13F, 3.7719F, 4.0514F, -0.3927F, 0.0F, 0.0F));

        PartDefinition hammer_r1 = Gun.addOrReplaceChild("hammer_r1", CubeListBuilder.create().texOffs(12, 0).addBox(0.8325F, -0.975F, 0.0213F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7807F, -0.5978F, 3.1684F, 0.3927F, 0.0F, 0.0F));

        PartDefinition Bullets = Torso.addOrReplaceChild("Bullets", CubeListBuilder.create(), PartPose.offset(-7.0F, 9.6288F, -2.7059F));

        PartDefinition cube_r14 = Bullets.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(12, 28).addBox(-2.0F, -2.0F, 0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.6807F, -7.6232F, -0.3054F, 0.0F, 0.0F));

        PartDefinition cube_r15 = Bullets.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(66, 117).addBox(-3.0F, 0.0F, -4.0F, 4.0F, 17.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -9.663F, -1.0785F, -0.0873F, 0.0F, 0.0F));

        PartDefinition cube_r16 = Bullets.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(58, 41).addBox(-2.0F, -0.5F, -1.5F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.9168F, 6.9945F, -0.48F, 0.0F, 0.0F));

        PartDefinition Head = Torso.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, 8.9658F, -9.7844F));

        PartDefinition Hood_r1 = Head.addOrReplaceChild("Hood_r1", CubeListBuilder.create().texOffs(38, 84).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(78, 84).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition RightLeg = Torso.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(78, 100).mirror().addBox(-3.0F, 0.5F, -4.0F, 6.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.0F, 34.9658F, -2.7844F));

        PartDefinition LeftLeg = Torso.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(78, 100).addBox(-3.0F, 0.5F, -4.0F, 6.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 34.9658F, -2.7844F));

        PartDefinition RightArm = Torso.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(38, 104).mirror().addBox(-4.1667F, -3.1667F, -3.8333F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(110, 84).mirror().addBox(-3.1667F, 3.8333F, -2.8333F, 5.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(106, 100).mirror().addBox(-4.1667F, 13.8333F, -3.8333F, 6.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-11.8333F, 11.1324F, 0.0489F));

        PartDefinition LeftArm = Torso.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(38, 104).addBox(-1.8333F, -3.1667F, -3.8333F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(110, 84).addBox(-1.8333F, 3.8333F, -2.8333F, 5.0F, 10.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(106, 100).addBox(-1.8333F, 13.8333F, -3.8333F, 6.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(11.8333F, 11.1324F, 0.0489F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }
	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity instanceof TheMerchantEntity) {
            TheMerchantEntity theMerchant = (TheMerchantEntity) entity;
			this.animateWalk(ModAnimationDefinitions.THE_MERCHANT_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
			this.animate(theMerchant.idleAnimationState, ModAnimationDefinitions.THE_MERCHANT_IDLE, ageInTicks, 1f);
			//this.animate(theMerchant.attackAnimationState, ModAnimationDefinitions.DISSIDENT_ATTACK, ageInTicks, 1f);

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