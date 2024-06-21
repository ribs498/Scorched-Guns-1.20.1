package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.RedcoatEntity;

public class RedcoatRenderer extends MobRenderer<RedcoatEntity, RedcoatModel<RedcoatEntity>> {
    public RedcoatRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new RedcoatModel<>(pContext.bakeLayer(ModModelLayers.REDCOAT_LAYER)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(RedcoatEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/redcoat.png");
    }

    @Override
    public void render(RedcoatEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}






