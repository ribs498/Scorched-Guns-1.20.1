package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.SupplyScampEntity;
import top.ribs.scguns.entity.monster.ViventrumEntity;

public class SupplyScampRenderer extends MobRenderer<SupplyScampEntity, SupplyScampModel<SupplyScampEntity>> {
    public SupplyScampRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SupplyScampModel<>(pContext.bakeLayer(ModModelLayers.SUPPLY_SCAMP_LAYER)), 0.7f);
        this.addLayer(new SupplyScampPumpkinLayer(this));
    }
    @Override
    public ResourceLocation getTextureLocation(SupplyScampEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/supply_scamp.png");
    }
    @Override
    public void render(SupplyScampEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0D, 0.05D, 0.0D);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        poseStack.popPose();
    }
}


