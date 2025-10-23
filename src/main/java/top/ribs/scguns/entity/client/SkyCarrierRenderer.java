package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.SkyCarrierEntity;

public class SkyCarrierRenderer extends MobRenderer<SkyCarrierEntity, SkyCarrierModel<SkyCarrierEntity>> {
    public SkyCarrierRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SkyCarrierModel<>(pContext.bakeLayer(ModModelLayers.SKY_CARRIER_LAYER)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(SkyCarrierEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/sky_carrier.png");
    }

    @Override
    public void render(SkyCarrierEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.scale(1.1f, 1.1f, 1.1f);


        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    @Override
    protected void setupRotations(SkyCarrierEntity pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);

        if (pEntityLiving.hurtTime > 0) {
            float hurtTime = (float)pEntityLiving.hurtTime - pPartialTicks;
            float shakeAmount = Mth.sin(hurtTime * 1.5F) * (float)pEntityLiving.hurtTime * 0.01F;

            pMatrixStack.mulPose(Axis.ZP.rotation(shakeAmount));
        }
    }
}