package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import software.bernie.geckolib.animatable.GeoItem;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.CogMinionEntity;

public class CogMinionRenderer extends MobRenderer<CogMinionEntity, CogMinionModel<CogMinionEntity>> {
    public CogMinionRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new CogMinionModel<>(pContext.bakeLayer(ModModelLayers.COG_MINION_LAYER)), 0.5f);
        this.addLayer(new ItemInHandLayer<>(this, pContext.getItemInHandRenderer()));
        this.addLayer(new CogMinionHelmetLayer(this, pContext.getModelSet(), pContext.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(CogMinionEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/cog_minion.png");
    }

    @Override
    public void render(CogMinionEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        pMatrixStack.scale(1.0f, 1.0f, 1.0f);

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);

        pMatrixStack.popPose();
    }

    private static class CogMinionHelmetLayer extends RenderLayer<CogMinionEntity, CogMinionModel<CogMinionEntity>> {
        private final ItemInHandRenderer itemInHandRenderer;
        private final HumanoidModel<CogMinionEntity> helmetModel;
        private final HumanoidModel<CogMinionEntity> geoArmorProxy;

        public CogMinionHelmetLayer(MobRenderer<CogMinionEntity, CogMinionModel<CogMinionEntity>> renderer,
                                    EntityModelSet modelSet,
                                    ItemInHandRenderer itemInHandRenderer) {
            super(renderer);
            this.itemInHandRenderer = itemInHandRenderer;
            this.helmetModel = new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
            this.geoArmorProxy = new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           CogMinionEntity entity, float limbSwing, float limbSwingAmount,
                           float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

            ItemStack helmetStack = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (helmetStack.isEmpty()) {
                return;
            }

            Item item = helmetStack.getItem();

            if (item instanceof ArmorItem armorItem) {
                if (item instanceof GeoItem) {
                    renderGeoHelmet(poseStack, buffer, packedLight, entity, helmetStack, armorItem, partialTicks);
                } else {
                    poseStack.pushPose();
                    this.getParentModel().head.translateAndRotate(poseStack);
                    renderVanillaHelmet(poseStack, buffer, packedLight, entity, helmetStack, armorItem);
                    poseStack.popPose();
                }
            } else if (item instanceof BlockItem) {
                poseStack.pushPose();
                this.getParentModel().head.translateAndRotate(poseStack);
                poseStack.scale(0.625F, -0.625F, -0.625F);
                poseStack.translate(0.0D, -0.5D, 0.0D);

                this.itemInHandRenderer.renderItem(entity, helmetStack, ItemDisplayContext.HEAD, false,
                        poseStack, buffer, packedLight);
                poseStack.popPose();
            }
        }

        private void renderGeoHelmet(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                     CogMinionEntity entity, ItemStack helmetStack, ArmorItem armorItem, float partialTicks) {

            copyHeadTransform(this.getParentModel().head, this.geoArmorProxy.head);

            this.geoArmorProxy.head.y -= 17.0f;

            this.geoArmorProxy.head.visible = true;
            this.geoArmorProxy.body.visible = false;
            this.geoArmorProxy.rightArm.visible = false;
            this.geoArmorProxy.leftArm.visible = false;
            this.geoArmorProxy.rightLeg.visible = false;
            this.geoArmorProxy.leftLeg.visible = false;
            this.geoArmorProxy.hat.visible = false;

            HumanoidModel<?> armorModel = getArmorModel(entity, helmetStack, EquipmentSlot.HEAD, this.geoArmorProxy);
            if (armorModel != null) {
                poseStack.pushPose();

                float scale = 1.4f;
                poseStack.scale(scale, scale, scale);

                armorModel.renderToBuffer(poseStack, buffer.getBuffer(RenderType.armorCutoutNoCull(getArmorTexture(helmetStack))),
                        packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

                poseStack.popPose();
            }
        }

        private void copyHeadTransform(net.minecraft.client.model.geom.ModelPart source, net.minecraft.client.model.geom.ModelPart target) {
            target.x = source.x;
            target.y = source.y;
            target.z = source.z;
            target.xRot = source.xRot;
            target.yRot = source.yRot;
            target.zRot = source.zRot;
            target.xScale = source.xScale;
            target.yScale = source.yScale;
            target.zScale = source.zScale;
        }

        private HumanoidModel<?> getArmorModel(CogMinionEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> defaultModel) {
            return (HumanoidModel<?>) net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, stack, slot, defaultModel);
        }

        private void renderVanillaHelmet(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                         CogMinionEntity entity, ItemStack helmetStack, ArmorItem armorItem) {
            poseStack.scale(1.1F, 1.0F, 1.1F);
            poseStack.translate(0.0D, -0.09D, 0.0D);

            this.helmetModel.head.xRot = 0;
            this.helmetModel.head.yRot = 0;
            this.helmetModel.head.zRot = 0;

            this.helmetModel.body.visible = false;
            this.helmetModel.rightArm.visible = false;
            this.helmetModel.leftArm.visible = false;
            this.helmetModel.rightLeg.visible = false;
            this.helmetModel.leftLeg.visible = false;
            this.helmetModel.hat.visible = false;
            this.helmetModel.head.visible = true;

            ResourceLocation texture = getArmorTexture(helmetStack);
            var renderType = RenderType.armorCutoutNoCull(texture);
            var vertexConsumer = buffer.getBuffer(renderType);

            this.helmetModel.head.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

            this.helmetModel.body.visible = true;
            this.helmetModel.rightArm.visible = true;
            this.helmetModel.leftArm.visible = true;
            this.helmetModel.rightLeg.visible = true;
            this.helmetModel.leftLeg.visible = true;
        }

        private ResourceLocation getArmorTexture(ItemStack stack) {
            ArmorItem item = (ArmorItem) stack.getItem();

            String texture = item.getMaterial().getName();
            String domain = "minecraft";
            int idx = texture.indexOf(':');
            if (idx != -1) {
                domain = texture.substring(0, idx);
                texture = texture.substring(idx + 1);
            }

            String s1 = String.format(java.util.Locale.ROOT, "%s:textures/models/armor/%s_layer_%d.png",
                    domain, texture, 1);

            return new ResourceLocation(s1);
        }
    }
}