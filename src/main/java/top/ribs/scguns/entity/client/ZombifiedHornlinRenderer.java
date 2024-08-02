package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.ZombifiedHornlinEntity;

public class ZombifiedHornlinRenderer extends MobRenderer<ZombifiedHornlinEntity, ZombifiedHornlinModel<ZombifiedHornlinEntity>> {
    public ZombifiedHornlinRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new ZombifiedHornlinModel<>(pContext.bakeLayer(ModModelLayers.ZOMBIFIED_HORNLIN_LAYER)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(ZombifiedHornlinEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/zombified_hornlin.png");
    }

    @Override
    public void render(ZombifiedHornlinEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}






