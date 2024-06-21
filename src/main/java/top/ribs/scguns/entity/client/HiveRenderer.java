package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.HiveEntity;

public class HiveRenderer extends MobRenderer<HiveEntity, HiveModel<HiveEntity>> {
    public HiveRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new HiveModel<>(pContext.bakeLayer(ModModelLayers.HIVE_LAYER)), 0.6f);
    }

    @Override
    public ResourceLocation getTextureLocation(HiveEntity hiveEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/hive.png");
    }

    @Override
    public void render(HiveEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
            pMatrixStack.scale(0.9f, 0.9f, 0.9f);


        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

}
