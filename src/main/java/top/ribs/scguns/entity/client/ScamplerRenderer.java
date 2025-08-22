package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.ScamplerEntity;

public class ScamplerRenderer extends MobRenderer<ScamplerEntity, ScamplerModel<ScamplerEntity>> {
    public ScamplerRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new ScamplerModel<>(pContext.bakeLayer(ModModelLayers.SCAMPLER_LAYER)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(ScamplerEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/scampler.png");
    }

    @Override
    public void render(ScamplerEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
            pMatrixStack.scale(1.1f, 1.1f, 1.1f);


        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}
