package top.ribs.scguns.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.screen.widget.MiniButton;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.container.AttachmentContainer;
import top.ribs.scguns.common.container.slot.AttachmentSlot;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class AttachmentScreen extends AbstractContainerScreen<AttachmentContainer>
{
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("scguns:textures/gui/attachments.png");
    private static final Component CONFIG_TOOLTIP = Component.translatable("scguns.button.config.tooltip");

    private final Inventory playerInventory;
    private final Container weaponInventory;

    private boolean showHelp = true;
    private int windowZoom = 10;
    private int windowX, windowY;
    private float windowRotationX, windowRotationY;
    private boolean mouseGrabbed;
    private int mouseGrabbedButton;
    private int mouseClickedX, mouseClickedY;

    public AttachmentScreen(AttachmentContainer screenContainer, Inventory playerInventory, Component titleIn)
    {
        super(screenContainer, playerInventory, titleIn);
        this.playerInventory = playerInventory;
        this.weaponInventory = screenContainer.getWeaponInventory();
        this.imageHeight = 184;
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
                    int titleWidth = this.minecraft.font.width(this.title);
                    button.setX(this.leftPos + titleWidth + 8 + 3 + i * 13);
                }
                case RIGHT -> {
                    button.setX(this.leftPos + this.imageWidth - 7 - 10 - (buttons.size() - 1 - i) * 13);
                }
            }
            button.setY(this.topPos + 5);
            this.addRenderableWidget(button);
        }
    }

    private List<MiniButton> gatherButtons()
    {
        List<MiniButton> buttons = new ArrayList<>();
        if(!Config.CLIENT.hideConfigButton.get())
        {
            MiniButton configButton = new MiniButton(0, 0, 192, 0, GUI_TEXTURES, onPress -> this.openConfigScreen());
            configButton.setTooltip(Tooltip.create(CONFIG_TOOLTIP));
            buttons.add(configButton);
        }
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
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY); //Render tool tips

        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            int x = i < 4 ? 8 : 152; // Adjust x coordinate for right side slots
            int y = 17 + (i % 4) * 18; // Adjust y coordinate for slots
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
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        pGuiGraphics.drawString(this.font, this.playerInventory.getDisplayName(), this.inventoryLabelX, this.inventoryLabelY + 19, 4210752, false);

        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        pGuiGraphics.enableScissor(left + 26, top + 17, left + 26 + 124, top + 17 + 70);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(96, 50, 150);
        pGuiGraphics.pose().translate(this.windowX + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseX - this.mouseClickedX : 0), 0, 0);
        pGuiGraphics.pose().translate(0, this.windowY + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseY - this.mouseClickedY : 0), 0);
        pGuiGraphics.pose().mulPose(Axis.XP.rotationDegrees(-30F));
        pGuiGraphics.pose().mulPose(Axis.XP.rotationDegrees(this.windowRotationY - (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseY - this.mouseClickedY : 0)));
        pGuiGraphics.pose().mulPose(Axis.YP.rotationDegrees(this.windowRotationX + (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseX - this.mouseClickedX : 0)));
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

        if (this.showHelp) {
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().scale(0.5F, 0.5F, 0.5F);
            pGuiGraphics.drawString(minecraft.font, I18n.get("container.scguns.attachments.window_help"), 56, 38, 0xFFFFFF);
            pGuiGraphics.pose().popPose();
        }
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(GUI_TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight);

        /* Draws the icons for each attachment slot. If not applicable
         * for the weapon, it will draw a cross instead. */
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            int x = i < 4 ? 8 : 152; // Adjust x coordinate for right side slots
            int y = 17 + (i % 4) * 18; // Adjust y coordinate for slots
            if (!this.canPlaceAttachmentInSlot(this.menu.getCarried(), this.menu.getSlot(i))) {
                pGuiGraphics.blit(GUI_TEXTURES, left + x, top + y, 176, 0, 16, 16);
            } else if (this.weaponInventory.getItem(i).isEmpty()) {
                pGuiGraphics.blit(GUI_TEXTURES, left + x, top + y, 176, 16 + (i % 5) * 16, 16, 16);
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
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;
        if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 124, 70))
        {
            if(scroll < 0 && this.windowZoom > 0)
            {
                this.showHelp = false;
                this.windowZoom--;
            }
            else if(scroll > 0)
            {
                this.showHelp = false;
                this.windowZoom++;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 124, 70))
        {
            if(!this.mouseGrabbed && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
            {
                this.mouseGrabbed = true;
                this.mouseGrabbedButton = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? 1 : 0;
                this.mouseClickedX = (int) mouseX;
                this.mouseClickedY = (int) mouseY;
                this.showHelp = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
                this.windowRotationX += (float) (mouseX - this.mouseClickedX);
                this.windowRotationY -= (float) (mouseY - this.mouseClickedY);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void openConfigScreen()
    {
        ModList.get().getModContainerById(Reference.MOD_ID).ifPresent(container ->
        {
            Screen screen = container.getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class).map(function -> function.screenFunction().apply(this.minecraft, null)).orElse(null);
            if(screen != null)
            {
                this.minecraft.setScreen(screen);
            }
            else if(this.minecraft != null && this.minecraft.player != null)
            {
                MutableComponent modName = Component.literal("Configured");
                modName.setStyle(modName.getStyle()
                        .withColor(ChatFormatting.YELLOW)
                        .withUnderlined(true)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("scguns.chat.open_curseforge_page")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/configured")));
                Component message = Component.translatable("scguns.chat.install_configured", modName);
                this.minecraft.player.displayClientMessage(message, false);
            }
        });
    }
}