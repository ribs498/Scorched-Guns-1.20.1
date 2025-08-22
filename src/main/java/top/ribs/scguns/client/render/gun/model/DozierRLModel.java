package top.ribs.scguns.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.gun.IOverrideModel;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.attachment.IAttachment;

public class DozierRLModel implements IOverrideModel {
    private static final float ROTATION_INCREMENT = 90.0f;
    private int shotCount = 0;

    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        RenderUtil.renderModel(SpecialModels.DOZIER_RL_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);
        // Renders the iron sights if no scope is attached.
        if (Gun.getScope(stack) == null)
            RenderUtil.renderModel(SpecialModels.DOZIER_RL_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        else
            RenderUtil.renderModel(SpecialModels.DOZIER_RL_NO_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.UNDER_BARREL)) {
            if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.VERTICAL_GRIP.get())
                RenderUtil.renderModel(SpecialModels.DOZIER_RL_GRIP_VERTICAL.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.LIGHT_GRIP.get())
                RenderUtil.renderModel(SpecialModels.DOZIER_RL_GRIP_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.IRON_BAYONET.get())
                RenderUtil.renderModel(SpecialModels.DOZIER_RL_IRON_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.ANTHRALITE_BAYONET.get())
                RenderUtil.renderModel(SpecialModels.DOZIER_RL_ANTHRALITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.DIAMOND_BAYONET.get())
                RenderUtil.renderModel(SpecialModels.DOZIER_RL_DIAMOND_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
            else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.NETHERITE_BAYONET.get())
                RenderUtil.renderModel(SpecialModels.DOZIER_RL_NETHERITE_BAYONET.getModel(), stack, matrixStack, buffer, light, overlay);
        }
        if (entity.equals(Minecraft.getInstance().player)) {
            renderBoltAndMagazine(matrixStack, buffer, stack, light, overlay);
            renderFlame(matrixStack, buffer, stack, light, overlay);
        }
    }

    private void renderBoltAndMagazine(PoseStack matrixStack, MultiBufferSource buffer, ItemStack stack, int light, int overlay) {
        assert Minecraft.getInstance().player != null;
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());

        matrixStack.pushPose();
        matrixStack.translate(0, -5.8 * 0.0625, 0);

        if (cooldown > 0) {
            shotCount = (shotCount + 1) % 4; // Increment shot count and reset after 4 shots
        }

        matrixStack.translate(0, 5.8 * 0.0625, 0);
        matrixStack.popPose();

        renderMagazineRotation(matrixStack, buffer, stack, light, overlay);
    }

    private void renderMagazineRotation(PoseStack matrixStack, MultiBufferSource buffer, ItemStack stack, int light, int overlay) {
        matrixStack.pushPose();
        matrixStack.translate(0, -0.0, 0);
        float currentRotation = shotCount * ROTATION_INCREMENT; // Calculate current rotation
        matrixStack.mulPose(Axis.ZP.rotationDegrees(currentRotation));
        matrixStack.translate(-0, 0.0, -0);
        RenderUtil.renderModel(SpecialModels.DOZIER_RL_DRUM.getModel(), stack, matrixStack, buffer, light, overlay);
        matrixStack.popPose();
    }
    private void renderFlame(PoseStack matrixStack, MultiBufferSource buffer, ItemStack stack, int light, int overlay) {
        matrixStack.pushPose();
        matrixStack.translate(0, -5.8 * 0.0625, 0);
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
        cooldown = (float) ease(cooldown);

        float scale = cooldown > 0 ? 1.0f : 0.0f;
        matrixStack.scale(scale, scale, scale);

        matrixStack.translate(0, 0, cooldown / 8);
        matrixStack.translate(0, 5.8 * 0.0625, 0);
        RenderUtil.renderModel(SpecialModels.DOZIER_RL_FIRE.getModel(), stack, matrixStack, buffer, light, overlay);


        // Always pop
        matrixStack.popPose();
    }
    private double ease(double x) {
        return 1 - Math.pow(1 - (2 * x), 4);
    }
}



