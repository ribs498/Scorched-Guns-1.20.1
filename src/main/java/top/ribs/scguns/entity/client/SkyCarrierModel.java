package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.entity.monster.SkyCarrierEntity;

public class SkyCarrierModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart SkyCarrier;
    private final ModelPart Full;
    private final ModelPart head;
    private final ModelPart Flash;
    private final ModelPart propLeft;
    private final ModelPart propRight;
    private final ModelPart propBack;
    private final ModelPart body;
    private final ModelPart actualHead;
    private final ModelPart armRight;
    private final ModelPart armLeft;

    public SkyCarrierModel(ModelPart root) {
        this.SkyCarrier = root.getChild("SkyCarrier");
        this.Full = this.SkyCarrier.getChild("Full");

        this.head = this.Full.getChild("Gun");
        this.Flash = this.head.getChild("Flash");
        this.propLeft = this.Full.getChild("WingL").getChild("Prop2");
        this.propRight = this.Full.getChild("WingR").getChild("Prop3");
        this.propBack = this.Full.getChild("BackCog");
        this.body = this.Full.getChild("body");
        this.actualHead = this.Full.getChild("Wheel");
        this.armRight = this.Full.getChild("ArmRight");
        this.armLeft = this.Full.getChild("ArmLeft");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition SkyCarrier = partdefinition.addOrReplaceChild("SkyCarrier", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Full = SkyCarrier.addOrReplaceChild("Full", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition BackCog = Full.addOrReplaceChild("BackCog", CubeListBuilder.create(), PartPose.offsetAndRotation(-22.0F, -13.79F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r1 = BackCog.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(82, 0).addBox(0.0F, -5.0F, -1.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.44F, 0.0F, -0.7854F, 0.0F, -1.5708F));

        PartDefinition cube_r2 = BackCog.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(81, 80).addBox(0.0F, -5.0F, -1.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.44F, 0.0F, 0.7854F, 0.0F, -1.5708F));

        PartDefinition cube_r3 = BackCog.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(82, 0).addBox(0.0F, -5.0F, -1.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.54F, 0.0F, -1.5708F, 0.0F, -1.5708F));

        PartDefinition cube_r4 = BackCog.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(81, 80).addBox(0.0F, -5.0F, -1.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.54F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition cube_r5 = BackCog.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 63).addBox(0.5F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.54F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition WingL = Full.addOrReplaceChild("WingL", CubeListBuilder.create(), PartPose.offset(3.5F, -12.1F, -11.75F));

        PartDefinition body_r1 = WingL.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(40, 16).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(80, 13).addBox(-1.5F, -2.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.1F, 1.75F, 0.0F, 1.5708F, 0.0F));

        PartDefinition body_r2 = WingL.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(36, 40).addBox(-2.5F, -1.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.6498F, 2.2771F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Prop2 = WingL.addOrReplaceChild("Prop2", CubeListBuilder.create().texOffs(10, 0).addBox(-0.2586F, -5.1102F, -1.0118F, 0.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 63).addBox(-0.7586F, -1.0891F, -0.9907F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0875F, 6.8158F, 1.7438F, 0.0F, 0.0F, -1.5708F));

        PartDefinition cube_r6 = Prop2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(45, 57).addBox(-0.15F, -4.9851F, -0.9851F, 0.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0086F, -0.1102F, 0.0093F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r7 = Prop2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(45, 57).addBox(-0.25F, -4.9789F, -1.0F, 0.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0086F, -0.1102F, 0.0093F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r8 = Prop2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(10, 0).addBox(-0.1499F, -5.0185F, -0.9798F, 0.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0086F, -0.1102F, 0.0093F, -1.5708F, 0.0F, 0.0F));

        PartDefinition WingR = Full.addOrReplaceChild("WingR", CubeListBuilder.create(), PartPose.offset(3.5F, -13.1F, 11.75F));

        PartDefinition body_r3 = WingR.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(40, 16).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(80, 13).addBox(-1.5F, -2.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.1F, -1.75F, 0.0F, -1.5708F, 0.0F));

        PartDefinition body_r4 = WingR.addOrReplaceChild("body_r4", CubeListBuilder.create().texOffs(36, 40).addBox(-2.5F, -1.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.6498F, -2.2771F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Prop3 = WingR.addOrReplaceChild("Prop3", CubeListBuilder.create().texOffs(10, 0).addBox(-0.2586F, -5.1102F, -0.9882F, 0.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 63).addBox(-0.7586F, -1.0891F, -1.0093F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0875F, 7.8158F, -1.7438F, 0.0F, 0.0F, -1.5708F));

        PartDefinition cube_r9 = Prop3.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(45, 57).addBox(-0.15F, -4.9851F, -1.0149F, 0.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0086F, -0.1102F, -0.0093F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r10 = Prop3.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(45, 57).addBox(-0.25F, -4.9789F, -1.0F, 0.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0086F, -0.1102F, -0.0093F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r11 = Prop3.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(10, 0).addBox(-0.1499F, -5.0185F, -1.0202F, 0.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0086F, -0.1102F, -0.0093F, 1.5708F, 0.0F, 0.0F));

        PartDefinition ArmRight = Full.addOrReplaceChild("ArmRight", CubeListBuilder.create(), PartPose.offsetAndRotation(9.5F, -7.5F, 4.5F, 0.0F, 0.0F, 1.2217F));

        PartDefinition body_r5 = ArmRight.addOrReplaceChild("body_r5", CubeListBuilder.create().texOffs(29, 58).addBox(-5.5F, -5.25F, 2.5F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 4.25F, -4.5F, 0.0F, 1.5708F, 0.0F));

        PartDefinition JointRight = ArmRight.addOrReplaceChild("JointRight", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, 0.5F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition body_r6 = JointRight.addOrReplaceChild("body_r6", CubeListBuilder.create().texOffs(51, 40).addBox(-0.5F, -2.0F, 2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(10, 0).addBox(-0.5F, 0.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(46, 0).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0209F, -0.4559F, 0.0F, 0.0F, 1.5708F, -0.7418F));

        PartDefinition ArmLeft = Full.addOrReplaceChild("ArmLeft", CubeListBuilder.create(), PartPose.offsetAndRotation(9.5F, -7.5F, -4.5F, 0.0F, 0.0F, 1.2217F));

        PartDefinition body_r7 = ArmLeft.addOrReplaceChild("body_r7", CubeListBuilder.create().texOffs(29, 58).addBox(-5.5F, -5.25F, -8.5F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 4.25F, 4.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition JointLeft = ArmLeft.addOrReplaceChild("JointLeft", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, 0.5F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition body_r8 = JointLeft.addOrReplaceChild("body_r8", CubeListBuilder.create().texOffs(51, 40).addBox(-0.5F, -2.0F, -4.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(10, 0).addBox(-0.5F, 0.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(46, 0).addBox(-0.5F, -1.0F, -3.0F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0209F, -0.4559F, 0.0F, 0.0F, -1.5708F, -0.7418F));

        PartDefinition head = Full.addOrReplaceChild("head", CubeListBuilder.create().texOffs(68, 64).addBox(-4.0F, -4.25F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -13.5F, 0.0F));

        PartDefinition Wheel = Full.addOrReplaceChild("Wheel", CubeListBuilder.create(), PartPose.offset(-19.3794F, -2.547F, 0.0F));

        PartDefinition body_r9 = Wheel.addOrReplaceChild("body_r9", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 2.3998F));

        PartDefinition Gun = Full.addOrReplaceChild("Gun", CubeListBuilder.create(), PartPose.offset(2.55F, -23.2F, 0.0F));

        PartDefinition body_r10 = Gun.addOrReplaceChild("body_r10", CubeListBuilder.create().texOffs(46, 7).addBox(-0.5F, -25.25F, -7.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(-1.0F, -25.75F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(29, 58).addBox(-1.0F, -25.75F, -3.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(52, 7).addBox(-0.5F, -26.25F, -2.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 58).addBox(-1.0F, -25.75F, -2.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.55F, 22.95F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Flash = Gun.addOrReplaceChild("Flash", CubeListBuilder.create().texOffs(0, 77).addBox(6.0F, -6.0F, -4.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = Full.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 3.75F, 0.0F));

        PartDefinition body_r11 = body.addOrReplaceChild("body_r11", CubeListBuilder.create().texOffs(48, 40).addBox(-5.0F, -18.5F, -9.0F, 10.0F, 10.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(53, 72).addBox(6.0F, -15.75F, 5.0F, 1.0F, 3.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(25, 72).addBox(-7.0F, -15.75F, 5.0F, 1.0F, 3.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(25, 72).addBox(-8.0F, -15.75F, -7.0F, 1.0F, 3.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(76, 35).addBox(-2.0F, -8.5F, -9.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(28, 73).addBox(-2.0F, -21.75F, -8.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 72).addBox(5.0F, -15.75F, -8.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(57, 72).addBox(-8.0F, -15.75F, -8.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(53, 72).addBox(7.0F, -15.75F, -7.0F, 1.0F, 3.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(19, 73).addBox(2.0F, -21.75F, -4.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(19, 73).addBox(-4.0F, -21.75F, -4.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(19, 79).addBox(-2.0F, -22.75F, -3.0F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 73).addBox(-2.0F, -21.75F, -7.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, -20.75F, 17.0F, 4.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-2.0F, -16.75F, 18.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(76, 25).addBox(-4.0F, -17.75F, 20.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(80, 19).addBox(-6.0F, -15.75F, 17.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(80, 19).addBox(2.0F, -15.75F, 17.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(65, 64).addBox(-2.0F, -24.75F, 14.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(65, 59).addBox(-5.0F, -19.75F, 14.0F, 10.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-6.0F, -17.75F, 5.0F, 12.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(46, 0).addBox(-6.0F, -20.75F, -7.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16).addBox(-7.0F, -19.75F, -7.0F, 14.0F, 10.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(50, 13).addBox(-5.0F, -9.75F, 6.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, -9.75F, -8.0F, 16.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(38, 59).addBox(-4.0F, -7.75F, 5.0F, 8.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(39, 25).addBox(-6.0F, -7.75F, -8.0F, 12.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -4.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition body_r12 = body.addOrReplaceChild("body_r12", CubeListBuilder.create().texOffs(77, 40).addBox(-2.0F, -2.75F, 2.5F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 22).addBox(-1.0F, -1.75F, -1.5F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.0F, -10.75F, 0.0F, 0.0F, 1.5708F, 2.3998F));

        PartDefinition body_r13 = body.addOrReplaceChild("body_r13", CubeListBuilder.create().texOffs(0, 24).addBox(-2.0F, 17.5F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.0F, 5.75F, 0.0F, 0.0F, 1.5708F, -3.1416F));

        PartDefinition body_r14 = body.addOrReplaceChild("body_r14", CubeListBuilder.create().texOffs(0, 46).addBox(-2.0F, -20.75F, 14.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 72).addBox(-2.0F, -24.75F, 4.0F, 4.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 58).addBox(-5.0F, -23.75F, 4.0F, 10.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition body_r15 = body.addOrReplaceChild("body_r15", CubeListBuilder.create().texOffs(0, 73).addBox(-2.0F, -24.75F, -1.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -4.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition body_r16 = body.addOrReplaceChild("body_r16", CubeListBuilder.create().texOffs(0, 78).addBox(-6.0F, -5.75F, -4.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, -7.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition body_r17 = body.addOrReplaceChild("body_r17", CubeListBuilder.create().texOffs(0, 78).addBox(-6.0F, -5.75F, 2.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, -7.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        SkyCarrierEntity skyCarrier = (SkyCarrierEntity) entity;
        this.root().getAllParts().forEach(ModelPart::resetPose);

        Vec3 velocity = skyCarrier.getDeltaMovement();
        float speed = Mth.sqrt((float) velocity.horizontalDistanceSqr());
        float verticalSpeed = (float) velocity.y;

        float propSpinSpeed = 1.5f + (speed * 4.0f) + Math.abs(verticalSpeed) * 2.0f;

        if (skyCarrier.isChargeAttacking()) {
            propSpinSpeed *= 2.0f;
        }

        this.propLeft.xRot = ageInTicks * propSpinSpeed;
        this.propRight.xRot = ageInTicks * propSpinSpeed;
        this.propBack.yRot = ageInTicks * propSpinSpeed;

        float forwardMovement = (float) velocity.z;
        float strafeMovement = (float) velocity.x;

        float targetXRot = -forwardMovement * 0.8f;
        float targetZRot = strafeMovement * 0.9f;

        if (skyCarrier.isChargeAttacking()) {
            targetXRot = -0.3f;
            this.body.xRot = Mth.lerp(0.3f, this.body.xRot, targetXRot);
            this.body.zRot = Mth.lerp(0.3f, this.body.zRot, 0.0f);
        } else {
            targetXRot = Mth.clamp(targetXRot, -0.4f, 0.4f);
            targetZRot = Mth.clamp(targetZRot, -0.5f, 0.5f);

            this.body.xRot = Mth.lerp(0.15f, this.body.xRot, targetXRot);
            this.body.zRot = Mth.lerp(0.15f, this.body.zRot, targetZRot);

            if (verticalSpeed > 0.05f) {
                this.body.xRot = Mth.lerp(0.1f, this.body.xRot, -0.15f);
            } else if (verticalSpeed < -0.05f) {
                this.body.xRot = Mth.lerp(0.1f, this.body.xRot, 0.15f);
            }
        }

        if (!skyCarrier.isChargeAttacking()) {
            float hoverBob = Mth.sin(ageInTicks * 0.08f) * 0.15f;
            float hoverRoll = Mth.cos(ageInTicks * 0.06f) * 0.03f;
            float hoverPitch = Mth.sin(ageInTicks * 0.05f) * 0.02f;

            this.Full.y += hoverBob;
            this.Full.zRot += hoverRoll;
            this.Full.xRot += hoverPitch;
        }

        float headBob = Mth.cos(ageInTicks * 0.1f) * 0.03f;
        this.actualHead.y = -2.547f + headBob;

        float armSwing = Mth.cos(ageInTicks * 0.3f) * 0.05f;
        float armBob = Mth.sin(ageInTicks * 0.25f) * 0.03f;

        this.armRight.zRot = 1.2217f + armSwing;
        this.armRight.y = -7.5f + armBob;

        this.armLeft.zRot = 1.2217f - armSwing;
        this.armLeft.y = -7.5f - armBob;

        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        this.Flash.visible = skyCarrier.isMuzzleFlashVisible();
    }


    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        SkyCarrier.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return SkyCarrier;
    }
}