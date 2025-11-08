package top.ribs.scguns.entity.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.entity.projectile.SulfurGasCloudEntity;


public class SulfurGasCloudRenderer extends EntityRenderer<SulfurGasCloudEntity> {

    public SulfurGasCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SulfurGasCloudEntity entity) {
        return null;
    }

    @Override
    public void render(SulfurGasCloudEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }
}