package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.SwarmEntity;

public class SwarmRenderer extends MobRenderer<SwarmEntity, SwarmModel<SwarmEntity>> {
    public SwarmRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SwarmModel<>(pContext.bakeLayer(ModModelLayers.SWARM_LAYER)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(SwarmEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/swarm.png");
    }

    @Override
    public void render(SwarmEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
            pMatrixStack.scale(1.5f, 1.0f, 1.5f);


        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}
