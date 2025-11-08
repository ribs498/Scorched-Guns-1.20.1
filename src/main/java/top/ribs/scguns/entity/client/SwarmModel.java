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
import top.ribs.scguns.entity.monster.SwarmEntity;

public class SwarmModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart Swarm;
    private final ModelPart[] flies;

    public SwarmModel(ModelPart root) {
        this.Swarm = root.getChild("Swarm");

        // Store all 12 fly parts for individual animation
        this.flies = new ModelPart[12];
        this.flies[0] = this.Swarm.getChild("Fly");
        this.flies[1] = this.Swarm.getChild("Fly2");
        this.flies[2] = this.Swarm.getChild("Fly3");
        this.flies[3] = this.Swarm.getChild("Fly4");
        this.flies[4] = this.Swarm.getChild("Fly5");
        this.flies[5] = this.Swarm.getChild("Fly6");
        this.flies[6] = this.Swarm.getChild("Fly7");
        this.flies[7] = this.Swarm.getChild("Fly8");
        this.flies[8] = this.Swarm.getChild("Fly9");
        this.flies[9] = this.Swarm.getChild("Fly10");
        this.flies[10] = this.Swarm.getChild("Fly11");
        this.flies[11] = this.Swarm.getChild("Fly12");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Swarm = partdefinition.addOrReplaceChild("Swarm", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition Fly = Swarm.addOrReplaceChild("Fly", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.5F, 0.0F));

        PartDefinition cube_r1 = Fly.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.5F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r2 = Fly.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Fly2 = Swarm.addOrReplaceChild("Fly2", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.5F, 0.0F));

        PartDefinition cube_r3 = Fly2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.5F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r4 = Fly2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Fly3 = Swarm.addOrReplaceChild("Fly3", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 2.5F, -6.0F));

        PartDefinition cube_r5 = Fly3.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.5F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r6 = Fly3.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Fly4 = Swarm.addOrReplaceChild("Fly4", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -6.5F, -6.0F));

        PartDefinition cube_r7 = Fly4.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.5F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r8 = Fly4.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Fly5 = Swarm.addOrReplaceChild("Fly5", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -3.5F, -2.0F));

        PartDefinition cube_r9 = Fly5.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.5F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r10 = Fly5.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Fly6 = Swarm.addOrReplaceChild("Fly6", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 0.5F, 4.0F));

        PartDefinition cube_r11 = Fly6.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.5F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r12 = Fly6.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Fly7 = Swarm.addOrReplaceChild("Fly7", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -7.5F, 4.0F));

        PartDefinition cube_r13 = Fly7.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.5F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r14 = Fly7.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Fly8 = Swarm.addOrReplaceChild("Fly8", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -1.5F, -9.0F));

        PartDefinition cube_r15 = Fly8.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.5F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r16 = Fly8.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Fly9 = Swarm.addOrReplaceChild("Fly9", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -4.5F, 5.0F));

        PartDefinition cube_r17 = Fly9.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.5F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r18 = Fly9.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Fly10 = Swarm.addOrReplaceChild("Fly10", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -6.5F, 5.0F));

        PartDefinition cube_r19 = Fly10.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.5F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r20 = Fly10.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Fly11 = Swarm.addOrReplaceChild("Fly11", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -1.5F, -7.0F));

        PartDefinition cube_r21 = Fly11.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.5F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r22 = Fly11.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Fly12 = Swarm.addOrReplaceChild("Fly12", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.5F, 5.5F, 6.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -8.5F, -2.0F));

        PartDefinition cube_r23 = Fly12.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.5F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r24 = Fly12.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Each fly gets its own unique random floating pattern
        for (int i = 0; i < flies.length; i++) {
            ModelPart fly = flies[i];

            // Use fly index to offset timing for uniqueness
            float timeOffset = i * 13.7F;
            float time = ageInTicks + timeOffset;

            // Random float patterns with different frequencies
            float bobSpeed = 0.3F + (i % 3) * 0.1F;
            float bobAmount = 0.8F + (i % 4) * 0.3F;

            // Vertical bobbing
            fly.y += Mth.sin(time * bobSpeed) * bobAmount;

            // Horizontal sway in X
            float swayXSpeed = 0.25F + (i % 5) * 0.08F;
            float swayXAmount = 0.5F + (i % 3) * 0.2F;
            fly.x += Mth.cos(time * swayXSpeed) * swayXAmount;

            // Horizontal sway in Z
            float swayZSpeed = 0.2F + ((i + 2) % 5) * 0.07F;
            float swayZAmount = 0.4F + ((i + 1) % 3) * 0.25F;
            fly.z += Mth.sin(time * swayZSpeed) * swayZAmount;

            // Random rotation wobbles
            fly.xRot = Mth.sin(time * 0.4F + i) * 0.2F;
            fly.yRot = Mth.cos(time * 0.35F + i * 2) * 0.3F;
            fly.zRot = Mth.sin(time * 0.3F + i * 3) * 0.15F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        Swarm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return Swarm;
    }
}