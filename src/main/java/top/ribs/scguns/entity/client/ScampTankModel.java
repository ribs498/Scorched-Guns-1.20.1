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
import top.ribs.scguns.entity.monster.ScampTankEntity;

public class ScampTankModel <T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart main;
    private final ModelPart machine_gun;
    private final ModelPart machine_gun_flash;
    private final ModelPart body;
    private final ModelPart turret;
    private final ModelPart turret_flash;
    private final ModelPart head;
    private final ModelPart treadL;
    private final ModelPart treadR;
    private final ModelPart cannon;

    private float mainTurretYaw = 0.0f;
    private float mainTurretPitch = 0.0f;
    private float machineGunYaw = 0.0f;
    private float machineGunPitch = 0.0f;

    public ScampTankModel(ModelPart root) {
        this.main = root.getChild("main");
        this.machine_gun = this.main.getChild("machine_gun");
        this.machine_gun_flash = this.machine_gun.getChild("machine_gun_flash");
        this.body = this.main.getChild("body");
        this.turret = this.main.getChild("turret");
        this.turret_flash = this.turret.getChild("turret_flash");
        this.head = this.turret.getChild("head");
        this.treadL = this.main.getChild("treadL");
        this.treadR = this.main.getChild("treadR");
        this.cannon = this.turret.getChild("cannon");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition machine_gun = main.addOrReplaceChild("machine_gun", CubeListBuilder.create().texOffs(118, 321).addBox(-4.4179F, -5.25F, -3.5F, 9.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(312, 49).addBox(-0.9179F, -6.25F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(308, 334).addBox(-3.4179F, -5.25F, 3.5F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0821F, -32.75F, -21.5F));

        PartDefinition cube_r1 = machine_gun.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(180, 321).addBox(-0.5F, -1.5F, -3.5F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.75F, -2.75F, -9.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r2 = machine_gun.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(128, 239).addBox(-0.5F, -1.5F, -3.5F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -2.75F, -9.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r3 = machine_gun.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(282, 335).addBox(-1.5F, -1.5F, 1.5F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2071F, -2.75F, -7.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r4 = machine_gun.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(36, 315).addBox(-1.5F, -1.5F, 1.5F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0429F, -2.75F, -7.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r5 = machine_gun.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 42).addBox(-5.5F, -1.5F, -1.5F, 11.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0821F, -2.75F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition machine_gun_flash = machine_gun.addOrReplaceChild("machine_gun_flash", CubeListBuilder.create(), PartPose.offset(0.0821F, -2.75F, -12.5F));

        PartDefinition cube_r6 = machine_gun_flash.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(96, 326).addBox(-4.0F, -1.0F, 0.0F, 8.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, -0.7854F));

        PartDefinition cube_r7 = machine_gun_flash.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(96, 326).addBox(-4.0F, -1.0F, 0.0F, 8.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition cube_r8 = machine_gun_flash.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(330, 49).addBox(-3.5F, -4.5F, -3.7F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7071F, 0.0F, 3.5F, 0.0F, 0.0F, 0.7854F));

        PartDefinition body = main.addOrReplaceChild("body", CubeListBuilder.create().texOffs(154, 203).addBox(-7.0F, -17.0F, -5.0F, 14.0F, 5.0F, 52.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-15.0F, -34.0F, -9.0F, 30.0F, 17.0F, 60.0F, new CubeDeformation(0.0F))
                .texOffs(0, 302).addBox(-6.0F, -36.0F, -6.0F, 12.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(160, 334).addBox(-4.0F, -38.0F, -4.0F, 8.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(128, 231).addBox(-2.5F, -41.0F, -3.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(148, 77).addBox(15.0F, -33.0F, -9.0F, 4.0F, 3.0F, 60.0F, new CubeDeformation(0.0F))
                .texOffs(154, 140).addBox(-19.0F, -33.0F, -9.0F, 4.0F, 3.0F, 60.0F, new CubeDeformation(0.0F))
                .texOffs(0, 207).addBox(-14.0F, -40.0F, 15.0F, 28.0F, 6.0F, 36.0F, new CubeDeformation(0.0F))
                .texOffs(106, 249).addBox(-10.0F, -42.0F, 48.0F, 20.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(282, 131).addBox(14.775F, -39.0F, 18.0F, 2.0F, 2.0F, 29.0F, new CubeDeformation(0.0F))
                .texOffs(148, 140).addBox(13.775F, -39.0F, 44.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(106, 260).addBox(-13.0F, -44.0F, 22.0F, 26.0F, 4.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(292, 251).addBox(-7.0F, -45.0F, 28.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(118, 306).addBox(-5.0F, -50.0F, 30.0F, 10.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(24, 337).addBox(-4.5F, -39.4366F, 53.63F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(66, 326).addBox(3.475F, -34.7071F, -9.7071F, 3.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(324, 319).addBox(-6.525F, -34.7071F, -9.7071F, 3.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(224, 69).addBox(-8.5F, -41.0282F, 16.3873F, 17.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(286, 218).addBox(-15.0F, -17.0F, 17.0F, 30.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(286, 229).addBox(-15.0F, -17.0F, 38.0F, 30.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(286, 240).addBox(-15.0F, -17.0F, -4.0F, 30.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, -21.0F));

        PartDefinition cube_r9 = body.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(128, 223).addBox(-13.0F, -10.5F, 0.5F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(190, 334).addBox(-12.0F, -17.5F, 1.5F, 3.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(262, 57).addBox(10.0F, -17.5F, 1.5F, 3.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(128, 215).addBox(9.0F, -10.5F, 0.5F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(180, 69).addBox(-8.0F, -9.5F, -2.5F, 17.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(276, 107).addBox(-14.0F, -8.5F, -4.5F, 29.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -24.8076F, 48.1716F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r10 = body.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(292, 278).addBox(-14.5F, 0.0F, -6.0F, 15.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.25F, -19.5147F, 58.9897F, 0.0F, -1.5708F, 1.5708F));

        PartDefinition cube_r11 = body.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(292, 266).addBox(-0.5F, 0.0F, -6.0F, 15.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.25F, -19.5147F, 58.9897F, 0.0F, 1.5708F, -1.5708F));

        PartDefinition cube_r12 = body.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(180, 57).addBox(-15.0F, 68.5F, -7.5F, 29.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -21.0147F, -8.9853F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r13 = body.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(34, 335).addBox(-0.5F, 7.5F, -2.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.6F, -32.0926F, -21.7208F, 1.5708F, 0.0F, 0.7854F));

        PartDefinition cube_r14 = body.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(198, 314).addBox(-0.5F, 9.5F, -2.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.6F, -32.2284F, -26.2208F, 1.5708F, 0.0F, 0.7854F));

        PartDefinition cube_r15 = body.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(276, 301).addBox(-1.5F, 8.5F, -2.5F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.6F, -31.4784F, -24.7208F, 1.5708F, 0.0F, 0.7854F));

        PartDefinition cube_r16 = body.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(180, 330).addBox(-3.0F, -9.5F, -5.5F, 7.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(36, 322).addBox(4.0F, -9.5F, -7.5F, 3.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(150, 321).addBox(-6.0F, -9.5F, -7.5F, 3.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(276, 83).addBox(-14.0F, -8.5F, -7.5F, 29.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -24.8076F, -6.1716F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r17 = body.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(204, 334).addBox(-8.5F, -15.0F, -18.25F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(16, 337).addBox(-9.5F, -15.5F, -14.75F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(250, 335).addBox(-7.5F, -16.0F, -15.25F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(312, 24).addBox(-6.5F, -17.0F, -16.25F, 13.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(294, 334).addBox(7.5F, -15.0F, -18.25F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(260, 335).addBox(6.5F, -16.0F, -15.25F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -33.1161F, 71.6829F, 0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r18 = body.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(146, 239).addBox(-1.025F, -1.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -36.083F, 53.8622F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r19 = body.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(44, 9).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5F, -34.3247F, -3.5115F, 0.4194F, 0.7401F, 0.2921F));

        PartDefinition cube_r20 = body.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(44, 12).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.5F, -34.3247F, -3.5115F, 0.4194F, -0.7401F, -0.2921F));

        PartDefinition cube_r21 = body.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(44, 27).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.8817F, -44.3247F, 45.8702F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r22 = body.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(44, 21).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.8817F, -44.3247F, 45.8702F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r23 = body.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(44, 18).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.8817F, -44.3247F, 24.6202F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r24 = body.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(44, 24).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.8817F, -44.3247F, 24.6202F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r25 = body.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(44, 15).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.5F, -39.5747F, 11.9885F, 0.4194F, -0.7401F, -0.2921F));

        PartDefinition cube_r26 = body.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(44, 6).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5F, -39.5747F, 11.9885F, 0.4194F, 0.7401F, 0.2921F));

        PartDefinition cube_r27 = body.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(276, 57).addBox(-14.0F, -3.0F, -2.0F, 27.0F, 6.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -31.7261F, -1.2648F, 0.3054F, 0.0F, 0.0F));

        PartDefinition cube_r28 = body.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(46, 335).addBox(-1.0F, -1.025F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.6537F, -38.0F, 17.2929F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone = body.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(-0.5F, -24.8076F, -6.1716F));

        PartDefinition cube_r29 = bone.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(218, 334).addBox(8.0F, -3.5F, -12.5F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(312, 12).addBox(-6.0F, -5.5F, -14.5F, 13.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(230, 335).addBox(-7.0F, -4.5F, -13.5F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(198, 307).addBox(-8.0F, -3.5F, -12.5F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(240, 335).addBox(7.0F, -4.5F, -13.5F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r30 = bone.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(198, 75).addBox(-5.5F, -3.0F, 1.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(258, 301).addBox(-7.5F, -3.0F, -4.0F, 4.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 9.364F, -11.1421F, 1.5708F, 0.0F, 0.0F));

        PartDefinition turret = main.addOrReplaceChild("turret", CubeListBuilder.create().texOffs(286, 190).addBox(-8.0F, -12.0F, -8.25F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(46, 302).addBox(8.0F, -10.0F, -8.25F, 2.0F, 4.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(82, 306).addBox(-10.0F, -10.0F, -8.25F, 2.0F, 4.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 328).addBox(-7.0F, -10.0F, 7.75F, 14.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(106, 290).addBox(-7.0F, -14.0F, -7.25F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(258, 307).addBox(-5.0F, -16.0F, -5.25F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(158, 307).addBox(-8.0F, -12.0F, -12.25F, 16.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(134, 253).addBox(-3.0F, -2.0F, -12.25F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -39.0F, 14.25F));

        PartDefinition cube_r31 = turret.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(150, 215).addBox(-1.5F, -11.0F, -1.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3358F, -16.0F, -9.5321F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r32 = turret.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(146, 246).addBox(-1.5F, 0.0F, -1.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0429F, -15.0F, -9.5321F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r33 = turret.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(270, 335).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.75F, -13.0F, -9.5F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r34 = turret.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(44, 30).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -10.0F, -12.75F, 0.0F, 0.0F, -0.7854F));

        PartDefinition cube_r35 = turret.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(44, 33).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -10.0F, -12.75F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r36 = turret.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(198, 318).addBox(-4.0F, -4.0F, 5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, -20.25F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r37 = turret.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(128, 334).addBox(-6.0F, -3.0F, -1.5F, 13.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -2.9393F, 7.568F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r38 = turret.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(0, 337).addBox(0.75F, -2.0F, -3.25F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 337).addBox(0.75F, -2.0F, 3.75F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.1161F, -4.0555F, -2.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition cube_r39 = turret.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(58, 335).addBox(-2.75F, -2.0F, 3.75F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(268, 68).addBox(-2.75F, -2.0F, -3.25F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1161F, -4.0555F, -2.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition turret_flash = turret.addOrReplaceChild("turret_flash", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, -61.25F));

        PartDefinition cube_r40 = turret_flash.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(0, 22).addBox(-11.0F, -10.0F, 0.0F, 22.0F, 20.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, -1.5708F, 0.0F, -0.7854F));

        PartDefinition cube_r41 = turret_flash.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(0, 22).addBox(-11.0F, -9.0F, 0.0F, 22.0F, 20.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition cube_r42 = turret_flash.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, -11.0F, -1.3F, 22.0F, 22.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition head = turret.addOrReplaceChild("head", CubeListBuilder.create().texOffs(230, 319).addBox(-4.0F, -11.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(298, 307).addBox(-4.5F, -9.75F, -5.75F, 9.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(106, 253).addBox(-4.5F, -8.75F, 0.25F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(312, 0).addBox(-4.5F, -11.75F, -4.75F, 9.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, -0.25F));

        PartDefinition cannon = turret.addOrReplaceChild("cannon", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, -60.25F));

        PartDefinition cube_r43 = cannon.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(294, 319).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 6.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(210, 260).addBox(-2.0F, -2.0F, 8.0F, 4.0F, 4.0F, 37.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r44 = cannon.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(263, 319).addBox(-2.0F, -2.0F, -14.0F, 5.0F, 5.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.75F, 40.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition treadR = main.addOrReplaceChild("treadR", CubeListBuilder.create().texOffs(96, 333).addBox(-1.0F, -8.0F, 30.0F, 14.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(324, 332).addBox(-1.0F, -8.0F, -32.0F, 14.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 77).addBox(-1.0F, -9.0F, -30.0F, 14.0F, 9.0F, 60.0F, new CubeDeformation(0.0F))
                .texOffs(162, 290).addBox(-2.0F, -10.0F, -28.0F, 16.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(292, 290).addBox(-2.0F, -10.0F, 20.0F, 16.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(210, 301).addBox(-2.0F, -10.0F, -4.0F, 16.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 146).addBox(-2.0F, -12.0F, -30.0F, 18.0F, 2.0F, 59.0F, new CubeDeformation(0.0F))
                .texOffs(180, 0).addBox(-2.0F, -18.0F, -26.0F, 15.0F, 6.0F, 51.0F, new CubeDeformation(0.0F))
                .texOffs(0, 249).addBox(13.0F, -14.0F, -26.0F, 2.0F, 2.0F, 51.0F, new CubeDeformation(0.0F))
                .texOffs(312, 36).addBox(-2.0F, -24.0F, 13.0F, 11.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(282, 162).addBox(-2.0F, -20.0F, -13.0F, 6.0F, 2.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(0, 315).addBox(-2.0F, -24.0F, -20.0F, 11.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(128, 207).addBox(1.0F, -22.0F, -24.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(180, 75).addBox(1.0F, -22.0F, -25.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(17.0F, 0.0F, 4.0F));

        PartDefinition cube_r45 = treadR.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(82, 302).addBox(-3.5F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -18.0F, -23.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r46 = treadR.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(44, 3).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -24.5F, 16.5F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r47 = treadR.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(44, 0).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -24.5F, -16.5F, 0.0F, -0.7854F, 0.0F));

        PartDefinition treadL = main.addOrReplaceChild("treadL", CubeListBuilder.create().texOffs(96, 333).mirror().addBox(-13.0F, -8.0F, 30.0F, 14.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(324, 332).mirror().addBox(-13.0F, -8.0F, -32.0F, 14.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 77).mirror().addBox(-13.0F, -9.0F, -30.0F, 14.0F, 9.0F, 60.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(162, 290).mirror().addBox(-14.0F, -10.0F, -28.0F, 16.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(292, 290).mirror().addBox(-14.0F, -10.0F, 20.0F, 16.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 301).mirror().addBox(-14.0F, -10.0F, -4.0F, 16.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 146).mirror().addBox(-16.0F, -12.0F, -30.0F, 18.0F, 2.0F, 59.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(180, 0).mirror().addBox(-13.0F, -18.0F, -26.0F, 15.0F, 6.0F, 51.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 249).mirror().addBox(-15.0F, -14.0F, -26.0F, 2.0F, 2.0F, 51.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(312, 36).mirror().addBox(-9.0F, -24.0F, 13.0F, 11.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(282, 162).mirror().addBox(-4.0F, -20.0F, -13.0F, 6.0F, 2.0F, 26.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 315).mirror().addBox(-9.0F, -24.0F, -20.0F, 11.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(128, 207).mirror().addBox(-9.0F, -22.0F, -24.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(180, 75).mirror().addBox(-9.0F, -22.0F, -25.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-17.0F, 0.0F, 4.0F));

        PartDefinition cube_r48 = treadL.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(36, 42).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -24.5F, -16.5F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r49 = treadL.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(28, 42).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -24.5F, 16.5F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r50 = treadL.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(82, 302).mirror().addBox(-3.5F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.0F, -18.0F, -23.75F, -0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public @NotNull ModelPart root() {
        return main;
    }
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (entity instanceof ScampTankEntity scampTank) {
            this.cannon.visible = !scampTank.isInSecondPhase();
            this.machine_gun.visible = !scampTank.isInSecondPhase();

            float headDropOffset = 0.0F;
            if (scampTank.isMainTurretFlashVisible() || scampTank.isCharging()) {
                headDropOffset = 3.0F;
            }
            this.head.y = -12.0F + headDropOffset;

            Player player = Minecraft.getInstance().player;
            if (player != null && entity.distanceToSqr(player) <= 45.0 * 45.0) {
                double deltaX = player.getX() - entity.getX();
                double deltaZ = player.getZ() - entity.getZ();
                double deltaY = player.getEyeY() - (entity.getY() + entity.getEyeHeight());
                double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                float entityYaw = entity.getYRot();
                float absoluteTargetYaw = (float) Math.toDegrees(Math.atan2(-deltaX, deltaZ));
                float relativeTargetYaw = absoluteTargetYaw - entityYaw;

                while (relativeTargetYaw > 180.0f) relativeTargetYaw -= 360.0f;
                while (relativeTargetYaw < -180.0f) relativeTargetYaw += 360.0f;

                float targetPitch = (float) -Math.toDegrees(Math.atan2(deltaY, horizontalDistance));

                targetPitch = Mth.clamp(targetPitch, -30.0f, 60.0f);

                this.mainTurretYaw = lerpAngle(this.mainTurretYaw, relativeTargetYaw, 0.08f);
                this.mainTurretPitch = Mth.lerp(0.12f, this.mainTurretPitch, targetPitch);

                this.machineGunYaw = lerpAngle(this.machineGunYaw, relativeTargetYaw, 0.15f);
                this.machineGunPitch = Mth.lerp(0.18f, this.machineGunPitch, targetPitch);
            } else {
                this.mainTurretYaw = lerpAngle(this.mainTurretYaw, 0.0f, 0.05f);
                this.mainTurretPitch = Mth.lerp(0.08f, this.mainTurretPitch, 0.0f);

                this.machineGunYaw = lerpAngle(this.machineGunYaw, 0.0f, 0.1f);
                this.machineGunPitch = Mth.lerp(0.12f, this.machineGunPitch, 0.0f);
            }

            this.turret.yRot = this.mainTurretYaw * ((float)Math.PI / 180F);
            this.turret.xRot = this.mainTurretPitch * ((float)Math.PI / 180F);
            this.machine_gun.yRot = this.machineGunYaw * ((float)Math.PI / 180F);
            this.machine_gun.xRot = this.machineGunPitch * ((float)Math.PI / 180F);
            this.turret_flash.visible = scampTank.isMainTurretFlashVisible();
            this.machine_gun_flash.visible = scampTank.isMachineGunFlashVisible();
        }
    }

    private float lerpAngle(float current, float target, float factor) {
        float difference = target - current;
        while (difference > 180.0f) difference -= 360.0f;
        while (difference < -180.0f) difference += 360.0f;

        return current + difference * factor;
    }
}


