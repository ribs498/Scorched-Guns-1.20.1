package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.TraumaUnitEntity;
import top.ribs.scguns.entity.monster.ZombifiedHornlinEntity;

public class TraumaUnitRenderer extends MobRenderer<TraumaUnitEntity, TraumaUnitModel<TraumaUnitEntity>> {
    public TraumaUnitRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new TraumaUnitModel<>(pContext.bakeLayer(ModModelLayers.TRAUMA_UNIT_LAYER)), 0.7f);


    }
    @Override
    public ResourceLocation getTextureLocation(TraumaUnitEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/trauma_unit.png");
    }

    @Override
    public void render(TraumaUnitEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}


