package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.HornlinEntity;

public class HornlinRenderer extends HumanoidMobRenderer<HornlinEntity, HornlinModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/entity/hornlin.png");

    public HornlinRenderer(EntityRendererProvider.Context context) {
        super(context, new HornlinModel(context.bakeLayer(ModModelLayers.HORNLIN_LAYER)), 0.4f);
        this.addLayer(new HornlinFoodItemLayer(this, context.getItemInHandRenderer()));
        this.addLayer(new ScaledArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    private static class ScaledArmorLayer<T extends HornlinEntity, M extends HumanoidModel<T>>
            extends HumanoidArmorLayer<T, M, HumanoidModel<T>> {

        private final HumanoidModel<T> innerModel;
        private final HumanoidModel<T> outerModel;

        public ScaledArmorLayer(HumanoidMobRenderer<T, M> renderer,
                                HumanoidModel<T> innerModel,
                                HumanoidModel<T> outerModel,
                                ModelManager modelManager) {
            super(renderer, innerModel, outerModel, modelManager);
            this.innerModel = innerModel;
            this.outerModel = outerModel;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           T entity, float limbSwing, float limbSwingAmount,
                           float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

            if (!entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty()) {
                poseStack.pushPose();
                poseStack.scale(1.1F, 1.0F, 1.1F);
                this.renderHelmetOnly(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount,
                        partialTicks, ageInTicks, netHeadYaw, headPitch);
                poseStack.popPose();
            }

            poseStack.pushPose();
            poseStack.scale(1.2F, 1.05F, 1.35F);
            this.renderBodyArmorOnly(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount,
                    partialTicks, ageInTicks, netHeadYaw, headPitch);
            poseStack.popPose();
        }

        private void renderHelmetOnly(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                      T entity, float limbSwing, float limbSwingAmount,
                                      float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

            this.innerModel.body.visible = false;
            this.innerModel.rightArm.visible = false;
            this.innerModel.leftArm.visible = false;
            this.innerModel.rightLeg.visible = false;
            this.innerModel.leftLeg.visible = false;

            this.outerModel.body.visible = false;
            this.outerModel.rightArm.visible = false;
            this.outerModel.leftArm.visible = false;
            this.outerModel.rightLeg.visible = false;
            this.outerModel.leftLeg.visible = false;

            super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount,
                    partialTicks, ageInTicks, netHeadYaw, headPitch);

            this.innerModel.body.visible = true;
            this.innerModel.rightArm.visible = true;
            this.innerModel.leftArm.visible = true;
            this.innerModel.rightLeg.visible = true;
            this.innerModel.leftLeg.visible = true;

            this.outerModel.body.visible = true;
            this.outerModel.rightArm.visible = true;
            this.outerModel.leftArm.visible = true;
            this.outerModel.rightLeg.visible = true;
            this.outerModel.leftLeg.visible = true;
        }

        private void renderBodyArmorOnly(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                         T entity, float limbSwing, float limbSwingAmount,
                                         float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

            this.innerModel.head.visible = false;
            this.innerModel.hat.visible = false;
            this.outerModel.head.visible = false;
            this.outerModel.hat.visible = false;

            super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount,
                    partialTicks, ageInTicks, netHeadYaw, headPitch);

            this.innerModel.head.visible = true;
            this.innerModel.hat.visible = true;
            this.outerModel.head.visible = true;
            this.outerModel.hat.visible = true;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(HornlinEntity entity) {
        return TEXTURE;
    }

    @Override
    protected boolean isShaking(HornlinEntity entity) {
        return super.isShaking(entity) || entity.isConverting();
    }

    private static class HornlinFoodItemLayer extends RenderLayer<HornlinEntity, HornlinModel> {
        private final ItemInHandRenderer itemInHandRenderer;

        public HornlinFoodItemLayer(HumanoidMobRenderer<HornlinEntity, HornlinModel> renderer,
                                    ItemInHandRenderer itemInHandRenderer) {
            super(renderer);
            this.itemInHandRenderer = itemInHandRenderer;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           HornlinEntity entity, float limbSwing, float limbSwingAmount,
                           float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

            if (!entity.isEatingGold() && !entity.isPreparingToEat()) {
                return;
            }

            ItemStack heldFood = entity.getHeldFoodItem();
            if (heldFood.isEmpty()) {
                return;
            }

            poseStack.pushPose();

            HornlinModel model = this.getParentModel();
            model.leftArm.translateAndRotate(poseStack);

            poseStack.translate(0.125D, 0.625D, 0.0D);

            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            poseStack.scale(0.75F, 0.75F, 0.75F);

            this.itemInHandRenderer.renderItem(
                    entity,
                    heldFood,
                    ItemDisplayContext.GROUND,
                    false,
                    poseStack,
                    buffer,
                    packedLight
            );

            poseStack.popPose();
        }
    }
}