package top.ribs.scguns.entity.client;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.entity.monster.MotherGhastEntity;


public class MotherGhastModel <T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart fin;
    private final ModelPart vent_1;
    private final ModelPart vent_2;
    private final ModelPart gear_1;
    private final ModelPart turret1;
    private final ModelPart muzzle1;
    private final ModelPart[] tentacles = new ModelPart[9];

    private float turretYaw = 0.0f;
    private float turretPitch = 0.0f;

    public MotherGhastModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.fin = root.getChild("fin");
        this.vent_1 = this.body.getChild("vent_1");
        this.vent_2 = this.body.getChild("vent_2");
        this.gear_1 = root.getChild("gear_1");
        this.turret1 = root.getChild("turret1");
        this.muzzle1 = this.turret1.getChild("muzzle1");

        for(int i = 0; i < this.tentacles.length; i++) {
            this.tentacles[i] = root.getChild("tentacle" + (i + 1));
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-13.3529F, -21.8365F, -36.0288F, 27.0F, 27.0F, 56.0F, new CubeDeformation(0.0F))
                .texOffs(132, 149).addBox(-9.7279F, 4.6635F, -6.9663F, 20.0F, 14.0F, 27.0F, new CubeDeformation(0.0F))
                .texOffs(182, 124).addBox(9.9596F, 4.226F, -1.9038F, 2.0F, 6.0F, 17.0F, new CubeDeformation(0.0F))
                .texOffs(132, 190).addBox(-11.9779F, 4.226F, -1.9038F, 2.0F, 6.0F, 17.0F, new CubeDeformation(0.0F))
                .texOffs(166, 0).addBox(-7.1029F, 18.0385F, -1.9038F, 14.0F, 4.0F, 23.0F, new CubeDeformation(0.0F))
                .texOffs(0, 83).addBox(-15.6654F, -7.5865F, -37.7163F, 31.0F, 6.0F, 60.0F, new CubeDeformation(0.0F))
                .texOffs(0, 149).addBox(-3.3216F, -23.649F, -37.7163F, 6.0F, 17.0F, 60.0F, new CubeDeformation(0.0F))
                .texOffs(21, 0).addBox(-2.3216F, -18.649F, -39.2163F, 4.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.3288F, 18.3029F, 8.476F));

        PartDefinition body_r1 = body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(216, 206).addBox(-4.5F, -4.5F, -2.0F, 9.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1341F, -4.399F, -37.4038F, 0.0F, 0.0F, 0.7854F));

        PartDefinition body_r2 = body.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(16, 226).addBox(-3.875F, -3.0F, -3.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.4596F, -4.5865F, -24.9663F, -0.7854F, 0.0F, 0.0F));

        PartDefinition body_r3 = body.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(28, 22).addBox(-0.5F, -6.6569F, 4.6569F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(28, 26).addBox(-0.5F, -11.6066F, 9.6066F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(30, 18).addBox(-0.5F, -17.2635F, 15.2635F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(28, 30).addBox(-0.5F, -1.7071F, -0.2929F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(21, 14).addBox(-0.5F, 3.2426F, -5.2427F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3288F, -23.8365F, -9.7163F, -0.7854F, 0.0F, -1.5708F));

        PartDefinition body_r4 = body.addOrReplaceChild("body_r4", CubeListBuilder.create().texOffs(24, 18).addBox(-0.5F, 3.2426F, -5.2426F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3288F, -23.8365F, -17.7163F, -0.7854F, 0.0F, -1.5708F));

        PartDefinition body_r5 = body.addOrReplaceChild("body_r5", CubeListBuilder.create().texOffs(27, 14).addBox(-0.5F, 15.2635F, -17.2635F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3288F, -23.8365F, -8.7163F, -0.7854F, 0.0F, -1.5708F));

        PartDefinition body_r6 = body.addOrReplaceChild("body_r6", CubeListBuilder.create().texOffs(220, 145).mirror().addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(220, 145).addBox(30.8269F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.9923F, -4.5865F, -15.7163F, -0.7854F, 0.0F, 0.0F));

        PartDefinition body_r7 = body.addOrReplaceChild("body_r7", CubeListBuilder.create().texOffs(226, 185).mirror().addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(226, 185).addBox(30.8269F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.9923F, -4.5865F, -8.7163F, -0.7854F, 0.0F, 0.0F));

        PartDefinition body_r8 = body.addOrReplaceChild("body_r8", CubeListBuilder.create().texOffs(194, 234).mirror().addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(194, 234).addBox(30.8269F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.9923F, -4.5865F, -1.7163F, -0.7854F, 0.0F, 0.0F));

        PartDefinition body_r9 = body.addOrReplaceChild("body_r9", CubeListBuilder.create().texOffs(238, 79).mirror().addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-15.9923F, -4.5865F, -24.7163F, -0.7854F, 0.0F, 0.0F));

        PartDefinition body_r10 = body.addOrReplaceChild("body_r10", CubeListBuilder.create().texOffs(238, 79).mirror().addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(238, 79).addBox(30.8269F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.9923F, -4.5865F, -32.7163F, -0.7854F, 0.0F, 0.0F));

        PartDefinition body_r11 = body.addOrReplaceChild("body_r11", CubeListBuilder.create().texOffs(220, 145).addBox(-0.5F, 1.4749F, -3.4749F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.6654F, -4.5865F, -37.7163F, 0.0F, 1.5708F, 0.7854F));

        PartDefinition body_r12 = body.addOrReplaceChild("body_r12", CubeListBuilder.create().texOffs(226, 185).addBox(-0.5F, -3.4749F, 1.4749F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.6654F, -4.5865F, -37.7163F, 0.0F, 1.5708F, 0.7854F));

        PartDefinition body_r13 = body.addOrReplaceChild("body_r13", CubeListBuilder.create().texOffs(220, 145).addBox(-0.5F, 1.4749F, -3.4749F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.3346F, -4.5865F, -37.7163F, 0.0F, 1.5708F, 0.7854F));

        PartDefinition body_r14 = body.addOrReplaceChild("body_r14", CubeListBuilder.create().texOffs(226, 185).addBox(-0.5F, -3.4749F, 1.4749F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3346F, -4.5865F, -37.7163F, 0.0F, 1.5708F, 0.7854F));

        PartDefinition body_r15 = body.addOrReplaceChild("body_r15", CubeListBuilder.create().texOffs(238, 71).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.3346F, -4.5865F, 5.2837F, -0.7854F, 0.0F, 0.0F));

        PartDefinition body_r16 = body.addOrReplaceChild("body_r16", CubeListBuilder.create().texOffs(238, 75).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.3346F, -4.5865F, 13.2837F, -0.7854F, 0.0F, 0.0F));

        PartDefinition body_r17 = body.addOrReplaceChild("body_r17", CubeListBuilder.create().texOffs(212, 27).addBox(-5.25F, -5.25F, -1.6875F, 10.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(200, 190).addBox(-6.25F, -6.25F, -5.6875F, 12.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1471F, 11.9135F, 25.7212F, 0.0F, 0.0F, -0.7854F));

        PartDefinition body_r18 = body.addOrReplaceChild("body_r18", CubeListBuilder.create().texOffs(0, 20).addBox(-5.25F, -5.25F, -1.6875F, 10.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1471F, -8.0865F, 21.7212F, 0.0F, 0.0F, -0.7854F));

        PartDefinition body_r19 = body.addOrReplaceChild("body_r19", CubeListBuilder.create().texOffs(116, 236).addBox(-1.0F, -2.5F, -2.5F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(74, 237).addBox(1.0F, -1.5F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.2721F, 12.1635F, 6.5337F, -0.7854F, 0.0F, 0.0F));

        PartDefinition vent_1 = body.addOrReplaceChild("vent_1", CubeListBuilder.create().texOffs(226, 157).addBox(-2.4465F, -2.8715F, -3.025F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-15.2189F, -4.715F, 8.3087F));

        PartDefinition body_r20 = vent_1.addOrReplaceChild("body_r20", CubeListBuilder.create().texOffs(228, 49).addBox(-3.0F, -3.0F, -4.95F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.25F, -2.75F, 3.0F, 0.0F, 0.0F, 1.1781F));

        PartDefinition body_r21 = vent_1.addOrReplaceChild("body_r21", CubeListBuilder.create().texOffs(226, 177).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8715F, 0.6285F, 3.025F, 0.0F, 0.0F, 0.3927F));

        PartDefinition vent_2 = body.addOrReplaceChild("vent_2", CubeListBuilder.create().texOffs(226, 145).addBox(-1.6094F, -2.7716F, -3.0417F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-16.056F, -4.8149F, 16.3253F));

        PartDefinition body_r22 = vent_2.addOrReplaceChild("body_r22", CubeListBuilder.create().texOffs(228, 41).addBox(-3.0F, -3.0F, 3.05F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.4128F, -2.6501F, -5.0167F, 0.0F, 0.0F, 1.1781F));

        PartDefinition body_r23 = vent_2.addOrReplaceChild("body_r23", CubeListBuilder.create().texOffs(226, 169).addBox(-3.0F, -3.0F, 3.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0344F, 0.7284F, -4.9917F, 0.0F, 0.0F, 0.3927F));

        PartDefinition fin = partdefinition.addOrReplaceChild("fin", CubeListBuilder.create().texOffs(166, 27).addBox(0.476F, -13.5F, -0.625F, 0.0F, 27.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.9664F, 31.0096F));

        PartDefinition body_r24 = fin.addOrReplaceChild("body_r24", CubeListBuilder.create().texOffs(182, 77).addBox(0.0F, -13.5F, -10.125F, 0.0F, 27.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 9.5F, 0.0F, 0.0F, -1.5708F));

        PartDefinition gear_1 = partdefinition.addOrReplaceChild("gear_1", CubeListBuilder.create(), PartPose.offset(16.4343F, 30.4663F, 15.0096F));

        PartDefinition body_r25 = gear_1.addOrReplaceChild("body_r25", CubeListBuilder.create().texOffs(216, 219).addBox(-1.0F, -4.5F, -4.5F, 1.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(180, 216).addBox(-4.0F, -4.5F, -4.5F, 1.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(170, 190).addBox(-3.0F, -6.5F, -6.5F, 2.0F, 13.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1667F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition turret1 = partdefinition.addOrReplaceChild("turret1", CubeListBuilder.create().texOffs(82, 226).addBox(2.5846F, -3.2011F, -2.2419F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(232, 194).addBox(6.5846F, -2.7011F, -2.2419F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(232, 185).addBox(5.5846F, 1.2989F, -1.7419F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(164, 213).addBox(3.5846F, 2.7989F, 1.7581F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(232, 119).addBox(3.5846F, 2.7989F, -0.2419F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(200, 233).addBox(1.5846F, -2.7011F, -2.2419F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(166, 77).addBox(2.5846F, -2.2011F, 3.7581F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(236, 119).addBox(2.5846F, -2.7011F, -4.2419F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(236, 133).addBox(3.5846F, -0.7011F, -12.2419F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(40, 238).addBox(3.0846F, -0.7011F, -7.2419F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(17.1654F, 13.6154F, -17.4615F));

        PartDefinition cube_r1 = turret1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(236, 219).addBox(-1.5F, -1.5F, -5.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5846F, 0.2989F, -12.2419F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r2 = turret1.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(236, 126).addBox(-1.5F, -1.5F, -4.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(222, 119).addBox(-1.5F, -1.5F, 1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5846F, -0.2011F, -7.2419F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r3 = turret1.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(52, 238).addBox(-1.5F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(212, 65).addBox(0.5F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0846F, 0.2989F, 0.7581F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r4 = turret1.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(238, 65).addBox(-0.5F, -1.0F, -1.9996F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0336F, -0.0361F, 0.7581F, 0.0F, 0.0F, -1.1781F));

        PartDefinition cube_r5 = turret1.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(226, 237).addBox(1.0F, 0.0F, -2.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.7346F, 1.2989F, 0.7581F, 0.0F, 0.0F, -0.7854F));

        PartDefinition muzzle1 = turret1.addOrReplaceChild("muzzle1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -1.0F, -17.25F));

        PartDefinition cube_r6 = muzzle1.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 10).addBox(-5.0F, -6.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 1.0F, -2.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition tentacle1 = partdefinition.addOrReplaceChild("tentacle1", CubeListBuilder.create().texOffs(0, 226).addBox(-2.3125F, -0.5F, -1.6875F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.7678F, 21.7788F, -22.4904F));

        PartDefinition tentacle2 = partdefinition.addOrReplaceChild("tentacle2", CubeListBuilder.create().texOffs(222, 65).addBox(6.125F, -0.8125F, -1.6875F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.6697F, 21.7788F, -22.4904F));

        PartDefinition tentacle3 = partdefinition.addOrReplaceChild("tentacle3", CubeListBuilder.create().texOffs(200, 206).addBox(-10.75F, -1.0625F, -1.6875F, 4.0F, 23.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(11.1072F, 21.7788F, -22.4904F));

        PartDefinition tentacle4 = partdefinition.addOrReplaceChild("tentacle4", CubeListBuilder.create().texOffs(212, 41).addBox(-2.3125F, -1.4375F, -1.6875F, 4.0F, 20.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.1553F, 21.7788F, -14.0529F));

        PartDefinition tentacle5 = partdefinition.addOrReplaceChild("tentacle5", CubeListBuilder.create().texOffs(132, 213).addBox(-2.3125F, -1.4375F, -1.6875F, 4.0F, 20.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.7178F, 21.7788F, -14.0529F));

        PartDefinition tentacle6 = partdefinition.addOrReplaceChild("tentacle6", CubeListBuilder.create().texOffs(220, 124).addBox(-2.3125F, -0.125F, -1.6875F, 4.0F, 17.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(6.7197F, 21.7788F, -14.0529F));

        PartDefinition tentacle7 = partdefinition.addOrReplaceChild("tentacle7", CubeListBuilder.create().texOffs(148, 213).addBox(-2.3125F, 0.25F, -1.6875F, 4.0F, 20.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.7678F, 21.7788F, -5.6154F));

        PartDefinition tentacle8 = partdefinition.addOrReplaceChild("tentacle8", CubeListBuilder.create().texOffs(164, 216).addBox(6.125F, 0.25F, -1.6875F, 4.0F, 20.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.6697F, 21.7788F, -5.6154F));

        PartDefinition tentacle9 = partdefinition.addOrReplaceChild("tentacle9", CubeListBuilder.create().texOffs(222, 85).addBox(-10.75F, -0.8125F, -1.6875F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(11.1072F, 21.7788F, -5.6154F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        for(int i = 0; i < this.tentacles.length; i++) {
            this.tentacles[i].xRot = 0.2F * Mth.sin(ageInTicks * 0.3F + (float)i) + 0.4F;
        }
        this.fin.yRot = Mth.sin(ageInTicks * 0.1F) * 0.15F;
        this.gear_1.xRot = ageInTicks * 0.15F;

        this.vent_1.xRot = Mth.sin(ageInTicks * 0.4F) * 0.08F;
        this.vent_2.xRot = Mth.sin(ageInTicks * 0.4F + 1.5F) * 0.08F;

        if (entity instanceof MotherGhastEntity motherGhast) {
            this.muzzle1.visible = motherGhast.isTurretFlashVisible();

            LivingEntity target = motherGhast.getTarget();

            // Fallback to client player if target isn't synced yet
            if (target == null) {
                Player player = Minecraft.getInstance().player;
                if (player != null && !player.isSpectator() &&
                        entity.distanceToSqr(player) < 100 * 100) {
                    target = player;
                }
            }

            if (target != null) {
                double deltaX = target.getX() - entity.getX();
                double deltaZ = target.getZ() - entity.getZ();
                double deltaY = target.getEyeY() - (entity.getY() + entity.getEyeHeight());
                double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                float entityYaw = entity.getYRot();
                float absoluteTargetYaw = (float) Math.toDegrees(Math.atan2(-deltaX, deltaZ));
                float relativeTargetYaw = absoluteTargetYaw - entityYaw;

                while (relativeTargetYaw > 180.0f) relativeTargetYaw -= 360.0f;
                while (relativeTargetYaw < -180.0f) relativeTargetYaw += 360.0f;

                float targetPitch = (float) -Math.toDegrees(Math.atan2(deltaY, horizontalDistance));
                targetPitch = Mth.clamp(targetPitch, -30.0f, 60.0f);

                this.turretYaw = lerpAngle(this.turretYaw, relativeTargetYaw, 0.1f);
                this.turretPitch = Mth.lerp(0.12f, this.turretPitch, targetPitch);
            } else {
                this.turretYaw = lerpAngle(this.turretYaw, 0.0f, 0.05f);
                this.turretPitch = Mth.lerp(0.08f, this.turretPitch, 0.0f);
            }

            this.turret1.yRot = this.turretYaw * ((float)Math.PI / 180F);
            this.turret1.xRot = this.turretPitch * ((float)Math.PI / 180F);
        }
    }

    private float lerpAngle(float current, float target, float factor) {
        float difference = target - current;
        while (difference > 180.0f) difference -= 360.0f;
        while (difference < -180.0f) difference += 360.0f;
        return current + difference * factor;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        fin.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        gear_1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        turret1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        for(ModelPart tentacle : this.tentacles) {
            tentacle.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}