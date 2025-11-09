package top.ribs.scguns.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.screen.widget.MiniButton;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.common.container.slot.AttachmentSlot;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.util.GunCompositeStatHelper;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class AttachmentScreen extends AbstractContainerScreen<AttachmentContainer>
{
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("scguns:textures/gui/attachments.png");

    private final Container weaponInventory;

    private int windowZoom = 10;
    private int windowX, windowY;
    private float windowRotationX, windowRotationY;
    private boolean mouseGrabbed;
    private int mouseGrabbedButton;
    private int mouseClickedX, mouseClickedY;

    private float momentumX = 0F;
    private float momentumY = 0F;
    private float lastDragX = 0F;
    private float lastDragY = 0F;
    private float targetRotationX = 0F;
    private float targetRotationY = 0F;
    private float prevRotationX = 0F;
    private float prevRotationY = 0F;
    private final List<Float> recentDragSpeedsX = new ArrayList<>();
    private final List<Float> recentDragSpeedsY = new ArrayList<>();
    private static final int VELOCITY_SAMPLES = 5;
    private float totalDragDistance = 0F;

    private static final float MOMENTUM_DAMPING = 0.92F;
    private static final float MIN_MOMENTUM = 0.15F;
    private static final float DRAG_THRESHOLD = 25F;
    private static final float MOMENTUM_SCALE = 1.4F;
    private static final float INTERPOLATION_SPEED = 0.5F;

    public AttachmentScreen(AttachmentContainer screenContainer, Inventory playerInventory, Component titleIn)
    {
        super(screenContainer, playerInventory, titleIn);
        this.weaponInventory = screenContainer.getWeaponInventory();
        this.imageHeight = 192;
        this.imageWidth = 188;
    }

    @Override
    protected void init()
    {
        super.init();

        List<MiniButton> buttons = this.gatherButtons();
        for(int i = 0; i < buttons.size(); i++)
        {
            MiniButton button = buttons.get(i);
            switch(Config.CLIENT.buttonAlignment.get())
            {
                case LEFT -> {
                    assert this.minecraft != null;
                    int titleWidth = this.minecraft.font.width(this.title);
                    button.setX(this.leftPos + titleWidth + 8 + 3 + i * 13 - 6);
                }
                case RIGHT -> {
                    button.setX(this.leftPos + this.imageWidth - 7 - 10 - (buttons.size() - 1 - i) * 13 - 6);
                }
            }
            button.setY(this.topPos + 5 + 19);
            this.addRenderableWidget(button);
        }
    }

    private List<MiniButton> gatherButtons()
    {
        List<MiniButton> buttons = new ArrayList<>();
        return buttons;
    }

    @Override
    public void containerTick()
    {
        super.containerTick();
        if(this.minecraft != null && this.minecraft.player != null)
        {
            if(!(this.minecraft.player.getMainHandItem().getItem() instanceof GunItem))
            {
                Minecraft.getInstance().setScreen(null);
            }
        }

        this.prevRotationX = this.windowRotationX;
        this.prevRotationY = this.windowRotationY;

        if(this.mouseGrabbed && this.mouseGrabbedButton == 1)
        {
            this.windowRotationX += (this.targetRotationX - this.windowRotationX) * INTERPOLATION_SPEED;
            this.windowRotationY += (this.targetRotationY - this.windowRotationY) * INTERPOLATION_SPEED;
        }
        else if(!this.mouseGrabbed)
        {
            if(Math.abs(this.momentumX) > MIN_MOMENTUM || Math.abs(this.momentumY) > MIN_MOMENTUM)
            {
                this.windowRotationX += this.momentumX;
                this.windowRotationY += this.momentumY;
                this.targetRotationX = this.windowRotationX;
                this.targetRotationY = this.windowRotationY;

                this.momentumX *= MOMENTUM_DAMPING;
                this.momentumY *= MOMENTUM_DAMPING;

                if(Math.abs(this.momentumX) < MIN_MOMENTUM) this.momentumX = 0F;
                if(Math.abs(this.momentumY) < MIN_MOMENTUM) this.momentumY = 0F;
            }
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);

        int startX = (this.width - this.imageWidth) / 2 - 6;
        int startY = (this.height - this.imageHeight) / 2 + 19;

        int numSlots = Math.min(5, IAttachment.Type.values().length);
        int centerX = 88 - (numSlots * 18) / 2 + 18 - 5;

        for(int i = 0; i < numSlots; i++)
        {
            int x = centerX + i * 18;
            int y = 89;
            if(RenderUtil.isMouseWithin(mouseX, mouseY, startX + x, startY + y, 18, 18))
            {
                IAttachment.Type type = IAttachment.Type.values()[i];
                if(!this.menu.getSlot(i).isActive())
                {
                    pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.scguns.attachment." + type.getTranslationKey()), Component.translatable("slot.scguns.attachment.not_applicable")), mouseX, mouseY);
                }
                else if(this.menu.getSlot(i) instanceof AttachmentSlot slot && slot.getItem().isEmpty() && !this.isCompatible(this.menu.getCarried(), slot))
                {
                    pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.scguns.attachment.incompatible").withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();

        int left = (this.width - this.imageWidth) / 2 - 6;
        int top = (this.height - this.imageHeight) / 2 + 19;

        float partialTicks = minecraft.getFrameTime();

        float renderRotationX = this.prevRotationX + (this.windowRotationX - this.prevRotationX) * partialTicks;
        float renderRotationY = this.prevRotationY + (this.windowRotationY - this.prevRotationY) * partialTicks;

        pGuiGraphics.enableScissor(left + 8 - 14, top + 17 - 82, left + 8 + 176 + 14, top + 17 + 64);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(96, 48, 150);
        pGuiGraphics.pose().translate(this.windowX + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseX - this.mouseClickedX : 0), 0, 0);
        pGuiGraphics.pose().translate(0, this.windowY + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseY - this.mouseClickedY : 0), 0);
        pGuiGraphics.pose().mulPose(Axis.XP.rotationDegrees(-30F));
        pGuiGraphics.pose().mulPose(Axis.XP.rotationDegrees(renderRotationY));
        pGuiGraphics.pose().mulPose(Axis.YP.rotationDegrees(renderRotationX));
        pGuiGraphics.pose().mulPose(Axis.YP.rotationDegrees(150F));
        pGuiGraphics.pose().scale(this.windowZoom / 10F, this.windowZoom / 10F, this.windowZoom / 10F);
        pGuiGraphics.pose().mulPose(Axis.YP.rotationDegrees(90F));
        pGuiGraphics.pose().mulPoseMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
        pGuiGraphics.pose().scale(90.0F, 90.0F, 90.0F);
        PoseStack modelStack = RenderSystem.getModelViewStack();
        modelStack.pushPose();
        modelStack.mulPoseMatrix(pGuiGraphics.pose().last().pose());
        RenderSystem.applyModelViewMatrix();
        assert this.minecraft != null;
        MultiBufferSource.BufferSource buffer = this.minecraft.renderBuffers().bufferSource();
        assert this.minecraft.player != null;
        GunRenderingHandler.get().renderWeapon(this.minecraft.player, this.minecraft.player.getMainHandItem(), ItemDisplayContext.GROUND, new PoseStack(), buffer, 15728880, 0F);
        buffer.endBatch();
        pGuiGraphics.pose().popPose();
        modelStack.popPose();
        RenderSystem.applyModelViewMatrix();
        pGuiGraphics.disableScissor();

        pGuiGraphics.flush();

        this.renderGunStats(pGuiGraphics);
    }

    private void renderGunStats(GuiGraphics pGuiGraphics) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        ItemStack gunStack = this.minecraft.player.getMainHandItem();
        if (!(gunStack.getItem() instanceof GunItem gunItem)) return;

        Gun modifiedGun = gunItem.getModifiedGun(gunStack);
        Gun.Projectile projectile = modifiedGun.getProjectile(gunStack);
        Gun.General general = modifiedGun.getGeneral();
        Gun.Reloads reloads = modifiedGun.getReloads();

        int startX = -60;
        int startY = 0;
        int lineHeight = 7;
        float scale = 0.6f;

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(scale, scale, scale);

        float currentY = startY;

        String gunName = Component.translatable(gunStack.getDescriptionId()).getString();
        int gunNameWidth = this.font.width(gunName);
        pGuiGraphics.drawString(this.font, gunName, (int)(startX / scale), 0, 0xFFFFFF, false);
        pGuiGraphics.fill((int)(startX / scale), (int)((currentY + 6) / scale), (int)((startX + gunNameWidth * scale) / scale), (int)((currentY + 7) / scale), 0xFFFFFFFF);
        currentY += lineHeight + 3;

        float baseDamage = projectile.getDamage();
        baseDamage = GunModifierHelper.getModifiedProjectileDamage(gunStack, baseDamage);
        baseDamage = GunEnchantmentHelper.getAcceleratorDamage(gunStack, baseDamage);
        baseDamage = GunEnchantmentHelper.getHeavyShotDamage(gunStack, baseDamage);
        baseDamage *= Config.COMMON.gameplay.globalDamageMultiplier.get().floatValue();

        float additionalDamage = GunModifierHelper.getAdditionalDamage(gunStack, false);
        CompoundTag tagCompound = gunStack.getTag();
        if (tagCompound != null && tagCompound.contains("AdditionalDamage", Tag.TAG_ANY_NUMERIC)) {
            additionalDamage += tagCompound.getFloat("AdditionalDamage");
        }

        float totalDamage = baseDamage + additionalDamage;
        pGuiGraphics.drawString(this.font,
                Component.translatable("info.scguns.damage").getString() + ": " + String.format("%.1f", totalDamage),
                (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
        currentY += lineHeight;

        float critChance = GunModifierHelper.getCriticalChance(gunStack);
        if (critChance > 0) {
            pGuiGraphics.drawString(this.font,
                    Component.translatable("info.scguns.critical_chance").getString() + ": " + String.format("%.1f%%", critChance * 100),
                    (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
            currentY += lineHeight;

            pGuiGraphics.drawString(this.font,
                    Component.translatable("info.scguns.critical_multiplier").getString() + ": " + String.format("%.1fx", projectile.getCritDamageMultiplier()),
                    (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
            currentY += lineHeight;
        }

        if (projectile.getEnergyUse() > 0) {
            pGuiGraphics.drawString(this.font,
                    Component.translatable("info.scguns.energy_use").getString() + ": " + projectile.getEnergyUse(),
                    (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
            currentY += lineHeight;
        }

        float baseArmorPen = projectile.getArmorPen();
        float puncturingPen = GunEnchantmentHelper.getPuncturingArmorBypass(gunStack);
        float totalArmorPen = baseArmorPen + puncturingPen;
        if (totalArmorPen > 0) {
            pGuiGraphics.drawString(this.font,
                    Component.translatable("info.scguns.armor_penetration").getString() + ": " + String.format("%.1f", totalArmorPen),
                    (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
            currentY += lineHeight;
        }

        FireMode fireMode = general.getFireMode();
        String fireModeKey = "fire_mode." + fireMode.id().toString();
        pGuiGraphics.drawString(this.font,
                Component.translatable("info.scguns.fire_mode").getString() + ": " + Component.translatable(fireModeKey).getString(),
                (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
        currentY += lineHeight;

        int modifiedRate = GunCompositeStatHelper.getCompositeRate(gunStack, modifiedGun);
        float rpm = (20.0f / modifiedRate) * 60.0f;
        pGuiGraphics.drawString(this.font,
                Component.translatable("info.scguns.fire_rate").getString() + ": " + String.format("%.0f", rpm) + " RPM",
                (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
        currentY += lineHeight;

        int modifiedAmmo = GunModifierHelper.getModifiedAmmoCapacity(gunStack, modifiedGun);
        pGuiGraphics.drawString(this.font,
                Component.translatable("info.scguns.max_ammo").getString() + ": " + modifiedAmmo,
                (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
        currentY += lineHeight;

        ReloadType reloadType = reloads.getReloadType();
        String reloadTypeKey = "reload_type." + reloadType.id().toString().replace(":", ".");
        pGuiGraphics.drawString(this.font,
                Component.translatable("info.scguns.reload_type").getString() + ": " + Component.translatable(reloadTypeKey).getString(),
                (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
        currentY += lineHeight;

        if (reloadType == ReloadType.SINGLE_ITEM && reloads.getReloadByproduct() != null) {
            pGuiGraphics.drawString(this.font,
                    Component.translatable("info.scguns.reload_byproduct").getString() + ": " + Component.translatable(reloads.getReloadByproduct().getDescriptionId()).getString(),
                    (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
            currentY += lineHeight;

            if (reloads.getByproductChance() > 0) {
                pGuiGraphics.drawString(this.font,
                        Component.translatable("info.scguns.byproduct_chance").getString() + ": " + String.format("%.0f%%", reloads.getByproductChance() * 100),
                        (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
                currentY += lineHeight;
            }
        }

        GripType gripType = modifiedGun.determineGripType(gunStack);
        String gripKey = gripType == GripType.ONE_HANDED ? "info.scguns.grip_one_handed" : "info.scguns.grip_two_handed";
        pGuiGraphics.drawString(this.font,
                Component.translatable("info.scguns.grip_type").getString() + ": " + Component.translatable(gripKey).getString(),
                (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
        currentY += lineHeight;

        if (projectile.isAlwaysSpread()) {
            pGuiGraphics.drawString(this.font,
                    Component.translatable("info.scguns.always_spread").getString(),
                    (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
            currentY += lineHeight;
        }

        float modifiedSpread = GunModifierHelper.getModifiedSpread(gunStack, projectile.getSpread());
        pGuiGraphics.drawString(this.font,
                Component.translatable("info.scguns.spread").getString() + ": " + String.format("%.2f", modifiedSpread),
                (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
        currentY += lineHeight;

        pGuiGraphics.drawString(this.font,
                Component.translatable("info.scguns.recoil_angle").getString() + ": " + String.format("%.1f", projectile.getRecoilAngle()),
                (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
        currentY += lineHeight;

        float falloffStart = GunModifierHelper.getModifiedDamageFalloffStart(gunStack, projectile.getDamageFalloffStart());
        float falloffEnd = GunModifierHelper.getModifiedDamageFalloffEnd(gunStack, projectile.getDamageFalloffEnd());

        if (falloffStart > 0 || falloffEnd > 0) {
            pGuiGraphics.drawString(this.font,
                    Component.translatable("info.scguns.falloff_range").getString() + ": " +
                            String.format("%.0f", falloffStart) + " - " + String.format("%.0f", falloffEnd),
                    (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
        } else {
            pGuiGraphics.drawString(this.font,
                    Component.translatable("info.scguns.falloff_range").getString() + ": " +
                            Component.translatable("info.scguns.falloff_none").getString(),
                    (int)(startX / scale), (int)(currentY / scale), 0xFFFFFF, false);
        }

        pGuiGraphics.pose().popPose();
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int left = (this.width - this.imageWidth) / 2 - 6;
        int top = (this.height - this.imageHeight) / 2 + 19;
        pGuiGraphics.blit(GUI_TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight);

        int numSlots = Math.min(5, IAttachment.Type.values().length);
        int centerX = 88 - (numSlots * 18) / 2 + 18 - 5;

        for (int i = 0; i < numSlots; i++) {
            int x = centerX + i * 18;
            int y = 89;
            if (!this.canPlaceAttachmentInSlot(this.menu.getCarried(), this.menu.getSlot(i))) {
                pGuiGraphics.blit(GUI_TEXTURES, left + x, top + y, 192, 0, 16, 16);
            } else if (this.weaponInventory.getItem(i).isEmpty()) {
                pGuiGraphics.blit(GUI_TEXTURES, left + x, top + y, 192, 16 + i * 16, 16, 16);
            }
        }
    }

    private boolean canPlaceAttachmentInSlot(ItemStack stack, Slot slot)
    {
        if(!slot.isActive())
            return false;

        if(!slot.equals(this.getSlotUnderMouse()))
            return true;

        if(!slot.getItem().isEmpty())
            return true;

        if(!(slot instanceof AttachmentSlot s))
            return true;


        if(!(stack.getItem() instanceof IAttachment<?> a))
            return true;

        if(!s.getType().equals(a.getType()))
            return true;

        return s.mayPlace(stack);
    }

    private boolean isCompatible(ItemStack stack, AttachmentSlot slot)
    {
        if(stack.isEmpty())
            return true;


        if(!(stack.getItem() instanceof IAttachment<?> attachment))
            return false;

        if(!attachment.getType().equals(slot.getType()))
            return true;

        if(!attachment.canAttachTo(stack))
            return false;

        return slot.mayPlace(stack);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        int startX = (this.width - this.imageWidth) / 2 - 6;
        int startY = (this.height - this.imageHeight) / 2 + 19;
        if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, startX + 8 - 14, startY + 17 - 82, 176 + 28, 146))
        {
            if(scroll < 0 && this.windowZoom > 0)
            {
                this.windowZoom--;
            }
            else if(scroll > 0)
            {
                this.windowZoom++;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int startX = (this.width - this.imageWidth) / 2 - 6;
        int startY = (this.height - this.imageHeight) / 2 + 19;

        if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, startX + 8 - 14, startY + 17 - 82, 176 + 28, 146))
        {
            if(!this.mouseGrabbed && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
            {
                this.mouseGrabbed = true;
                this.mouseGrabbedButton = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? 1 : 0;
                this.mouseClickedX = (int) mouseX;
                this.mouseClickedY = (int) mouseY;

                this.momentumX = 0F;
                this.momentumY = 0F;
                this.lastDragX = (float) mouseX;
                this.lastDragY = (float) mouseY;
                this.targetRotationX = this.windowRotationX;
                this.targetRotationY = this.windowRotationY;
                this.totalDragDistance = 0F;
                this.recentDragSpeedsX.clear();
                this.recentDragSpeedsY.clear();

                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if(this.mouseGrabbed && this.mouseGrabbedButton == 1)
        {
            float deltaX = (float) (mouseX - this.lastDragX);
            float deltaY = (float) (mouseY - this.lastDragY);

            this.targetRotationX += deltaX;
            this.targetRotationY -= deltaY;

            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            this.totalDragDistance += distance;

            this.recentDragSpeedsX.add(deltaX);
            this.recentDragSpeedsY.add(-deltaY);

            if(this.recentDragSpeedsX.size() > VELOCITY_SAMPLES)
            {
                this.recentDragSpeedsX.remove(0);
                this.recentDragSpeedsY.remove(0);
            }

            this.lastDragX = (float) mouseX;
            this.lastDragY = (float) mouseY;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(this.mouseGrabbed)
        {
            if(this.mouseGrabbedButton == 0 && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                this.mouseGrabbed = false;
                this.windowX += (int) (mouseX - this.mouseClickedX - 1);
                this.windowY += (int) (mouseY - this.mouseClickedY);
            }
            else if(mouseGrabbedButton == 1 && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                this.mouseGrabbed = false;

                if(this.totalDragDistance >= DRAG_THRESHOLD && !this.recentDragSpeedsX.isEmpty())
                {
                    float avgSpeedX = 0F;
                    float avgSpeedY = 0F;

                    for(int i = 0; i < this.recentDragSpeedsX.size(); i++)
                    {
                        avgSpeedX += this.recentDragSpeedsX.get(i);
                        avgSpeedY += this.recentDragSpeedsY.get(i);
                    }

                    avgSpeedX /= this.recentDragSpeedsX.size();
                    avgSpeedY /= this.recentDragSpeedsY.size();

                    this.momentumX = avgSpeedX * MOMENTUM_SCALE;
                    this.momentumY = avgSpeedY * MOMENTUM_SCALE;

                    float maxMomentum = 20F;
                    this.momentumX = Math.max(-maxMomentum, Math.min(maxMomentum, this.momentumX));
                    this.momentumY = Math.max(-maxMomentum, Math.min(maxMomentum, this.momentumY));
                }
                else
                {
                    this.momentumX = 0F;
                    this.momentumY = 0F;
                }

                this.recentDragSpeedsX.clear();
                this.recentDragSpeedsY.clear();
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}