package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.entity.projectile.RaidFlareEntity;

public class RaidFlareRenderer extends EntityRenderer<RaidFlareEntity> {
    private final ItemRenderer itemRenderer;

    public RaidFlareRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RaidFlareEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        if (entity.tickCount < 2) return;

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot() - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getXRot()));

        ItemStack flareStack = getFlareItemStack(entity.getRaidId());

        if (!flareStack.isEmpty()) {
            BakedModel model = this.itemRenderer.getModel(flareStack, entity.level(), null, 0);
            this.itemRenderer.render(flareStack, ItemDisplayContext.GROUND, false, poseStack, buffer,
                    packedLight, OverlayTexture.NO_OVERLAY, model);
        }

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private ItemStack getFlareItemStack(String raidId) {
        if (raidId == null || raidId.isEmpty()) {
            return ItemStack.EMPTY;
        }

        String itemId = raidId + "_flare";

        if ("piglin".equals(raidId)) {
            itemId = "gold_flare";
        }

        Item flareItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("scguns", itemId));

        return flareItem != null ? new ItemStack(flareItem) : ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getTextureLocation(RaidFlareEntity entity) {
        return new ResourceLocation("scguns", "textures/entity/raid_flare.png");
    }
}