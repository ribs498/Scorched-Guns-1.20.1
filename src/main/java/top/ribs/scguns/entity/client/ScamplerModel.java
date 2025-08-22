package top.ribs.scguns.entity.client;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ScamplerModel<T extends Entity> extends EntityModel<T> {

    private final ModelPart main;

    public ScamplerModel(ModelPart root) {
        this.main = root.getChild("main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(26, 25).addBox(-5.0F, -2.5F, -4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(28, 15).addBox(2.0F, -2.5F, -4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 34).addBox(2.5F, -3.5F, -4.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-5.5F, -3.5F, -4.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(20, 35).addBox(-4.5F, -5.5F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(34, 35).addBox(2.5F, -5.5F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.5F, -6.5F, -5.0F, 6.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(14, 43).addBox(-3.0F, -2.75F, -6.25F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-3.0F, -7.5F, -5.0F, 5.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-2.5F, -6.5F, -6.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-3.0F, -2.0F, -4.0F, 5.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 24.5F, 0.0F));

        PartDefinition cube_r1 = main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(28, 43).addBox(-2.0F, -0.5F, -2.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -9.0F, 0.7071F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r2 = main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(32, 9).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -8.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r3 = main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(40, 43).addBox(-2.0F, -1.0F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -4.75F, 5.5F, 0.0F, 0.0F, -0.7854F));

        PartDefinition cube_r4 = main.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(46, 27).addBox(3.0F, -0.5F, -4.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(46, 29).addBox(-5.0F, -0.5F, -4.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.318F, 6.4749F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r5 = main.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(14, 46).addBox(-5.0F, -0.5F, 2.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(46, 25).addBox(3.0F, -0.5F, 2.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.318F, -6.4749F, 0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}