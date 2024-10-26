package top.ribs.scguns.client.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.ChargeHandler;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.HotBarrelHandler;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.ammo_boxes.CreativeAmmoBoxItem;
import top.ribs.scguns.util.GunModifierHelper;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HUDRenderHandler {
    private static final ResourceLocation CHARGE_BAR = new ResourceLocation(Reference.MOD_ID, "textures/gui/charging_bar.png");
    private static final ResourceLocation FILL_BAR = new ResourceLocation(Reference.MOD_ID, "textures/gui/fill_bar.png");
    private static final ResourceLocation MELEE_ATTACK_INDICATOR_PROGRESS = new ResourceLocation(Reference.MOD_ID, "textures/gui/melee_attack_indicator_progress.png");
    private static final ResourceLocation MELEE_ATTACK_INDICATOR_BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/melee_attack_indicator_background.png");
    public static final ResourceLocation HOT_BARREL_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/hot_barrel.png");
    static int meleeCooldown = 0;
    static int maxMeleeCooldown = 0;
    static boolean isMeleeCooldownActive = false;
    private static boolean playingHitMarker = false;
    private static int hitMarkerTime;
    private static int prevHitMarkerTime;
    private static final int hitMarkerMaxTime = 2;
    private static boolean hitMarkerCrit = false;

    private static int reserveAmmo = 0;
    private static int ammoAutoUpdateTimer = 0;
    private static final int ammoAutoUpdateRate = 20;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!Config.CLIENT.display.displayGunInfo.get()) {
            return; // Early exit if HUD is disabled
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof GunItem)) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        renderGunInfoHUD(heldItem, event.getPartialTick(), poseStack, guiGraphics);
        renderChargeBarHUD(heldItem, event.getPartialTick(), poseStack, guiGraphics, player);
        renderMeleeCooldownHUD(event.getPartialTick(), poseStack, guiGraphics);
        renderHitMarker(event.getPartialTick(), poseStack, guiGraphics);
        renderHotBarrelOverlay(heldItem, poseStack, guiGraphics);
    }
    public static void renderHotBarrelOverlay(ItemStack heldItem, PoseStack poseStack, GuiGraphics guiGraphics) {
        if (!(heldItem.getItem() instanceof GunItem) || EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOT_BARREL.get(), heldItem) <= 0) {
            return;
        }

        int hotBarrelLevel = HotBarrelHandler.getHotBarrelLevel(heldItem);
        float chargeRatio = (float) hotBarrelLevel / HotBarrelHandler.MAX_HOT_BARREL;
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int barWidth = 128;
        int barHeight = 8;
        float scale = 1.25f;
        int barX = (int) ((screenWidth / 2) - ((barWidth * scale) / 2));
        int barY = (int) (screenHeight - 45 * scale);
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, HOT_BARREL_TEXTURE);
        guiGraphics.blit(HOT_BARREL_TEXTURE, (int) (barX / scale), (int) (barY / scale), 0, 0, barWidth, 5, barWidth, barHeight);
        int fillWidth = (int) (barWidth * chargeRatio);
        if (fillWidth > 0) {
            int fillY = (int) ((barY / scale) + 1);
            guiGraphics.blit(HOT_BARREL_TEXTURE, (int) (barX / scale), fillY, 0, 5, fillWidth, 3, barWidth, barHeight);
        }

        RenderSystem.disableBlend();
        poseStack.popPose();
    }


    private static void renderMeleeCooldownHUD(float partialTick, PoseStack poseStack, GuiGraphics guiGraphics) {
        if (!isMeleeCooldownActive) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int barWidth = 16;
        int barHeight = 16;
        int barX = (int) (screenWidth * 0.87);  // Adjust position
        int barY = (int) (screenHeight * 0.73); // Adjust position

        float cooldownRatio = (meleeCooldown > 0) ? (float) meleeCooldown / maxMeleeCooldown : 0;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, MELEE_ATTACK_INDICATOR_BACKGROUND);
        guiGraphics.blit(MELEE_ATTACK_INDICATOR_BACKGROUND, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        if (cooldownRatio > 0) {
            int fillHeight = (int) (barHeight * cooldownRatio);
            int fillY = barY + (barHeight - fillHeight);
            RenderSystem.setShaderTexture(0, MELEE_ATTACK_INDICATOR_PROGRESS);
            guiGraphics.blit(MELEE_ATTACK_INDICATOR_PROGRESS, barX, fillY, 0, barHeight - fillHeight, barWidth, fillHeight, barWidth, barHeight);
        }

        RenderSystem.disableBlend();
    }

    private static void renderGunInfoHUD(ItemStack heldItem, float partialTick, PoseStack poseStack, GuiGraphics guiGraphics) {
        if (!Config.CLIENT.display.displayGunInfo.get())
            return;

        Minecraft mc = Minecraft.getInstance();
        Gun gun = ((GunItem) heldItem.getItem()).getGun();
        CompoundTag tagCompound = heldItem.getTag();
        Player player = mc.player;

        if (tagCompound != null && player != null) {
            int currentAmmo = tagCompound.getInt("AmmoCount");
            int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(heldItem, gun);
            MutableComponent ammoCountValue;
            MutableComponent reserveAmmoValue;

            // Check if the player is in creative mode
            boolean isCreative = player.isCreative();

            // Check if the player has a CreativeAmmoBoxItem in their inventory
            AtomicBoolean hasCreativeAmmoBox = new AtomicBoolean(player.getInventory().items.stream()
                    .anyMatch(itemStack -> itemStack.getItem() instanceof CreativeAmmoBoxItem));

            // If not found in inventory, check Curios slots
            if (!hasCreativeAmmoBox.get()) {
                CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                    IItemHandlerModifiable curios = handler.getEquippedCurios();
                    for (int i = 0; i < curios.getSlots(); i++) {
                        ItemStack stack = curios.getStackInSlot(i);
                        if (stack.getItem() instanceof CreativeAmmoBoxItem) {
                            hasCreativeAmmoBox.set(true);
                            break;
                        }
                    }
                });
            }

            // Determine ammo count display
            if (isCreative) {
                ammoCountValue = Component.literal("∞ / ∞").withStyle(ChatFormatting.BOLD);
                reserveAmmoValue = Component.literal("∞").withStyle(ChatFormatting.BOLD);
            } else {
                ammoCountValue = Component.literal(currentAmmo + " / " + maxAmmo).withStyle(ChatFormatting.BOLD);

                // Display infinity for reserve ammo if the player has a CreativeAmmoBoxItem
                if (hasCreativeAmmoBox.get()) {
                    reserveAmmoValue = Component.literal("∞").withStyle(ChatFormatting.BOLD);
                } else {
                    reserveAmmoValue = Component.literal(String.valueOf(reserveAmmo)).withStyle(ChatFormatting.BOLD);
                }
            }

            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            int ammoPosX = (int) (screenWidth * 0.88);
            int ammoPosY = (int) (screenHeight * 0.8);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                if (player.isAlive()) {
                    guiGraphics.drawString(mc.font, "Reloading...", ammoPosX, ammoPosY - 10, 0xFFFF55);
                }
            }

            // Draw the current ammo value
            guiGraphics.drawString(mc.font, ammoCountValue, ammoPosX, ammoPosY, (currentAmmo > 0 || isCreative ? 0xFFFFFF : 0xFF5555));

            // Draw the reserve ammo value
            int reserveAmmoPosY = ammoPosY + 10;
            guiGraphics.drawString(mc.font, reserveAmmoValue, ammoPosX, reserveAmmoPosY, (reserveAmmo <= 0 && !Gun.hasUnlimitedReloads(heldItem) ? 0x555555 : 0xAAAAAA));

            // Render the ammo type texture
            ItemStack ammoItemStack = new ItemStack(Objects.requireNonNull(gun.getProjectile().getItem()));
            renderAmmoTypeTexture(ammoItemStack, ammoPosX - 20, ammoPosY, guiGraphics, mc);

            RenderSystem.disableBlend();
        }
    }


    private static void renderAmmoTypeTexture(ItemStack ammoItemStack, int x, int y, GuiGraphics guiGraphics, Minecraft mc) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        ResourceLocation ammoTexture = ForgeRegistries.ITEMS.getKey(ammoItemStack.getItem());
        TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(ammoTexture.getNamespace(), "item/" + ammoTexture.getPath()));
        int iconSize = 16;
        int zLevel = 0;
        guiGraphics.blit(x, y, zLevel, iconSize, iconSize, sprite);
    }

    private static void renderChargeBarHUD(ItemStack heldItem, float partialTick, PoseStack poseStack, GuiGraphics guiGraphics, LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);

        if (gun.getGeneral().getFireMode() != FireMode.PULSE) {
            return;
        }

        int maxChargeTime = gun.getGeneral().getFireTimer();
        int chargeTime = ChargeHandler.getChargeTime();
        if (chargeTime <= 0) {
            return;  // Do not display the charge bar until firing starts
        }

        float chargeRatio = (float) chargeTime / maxChargeTime;

        int barWidth = 16;
        int barHeight = 16;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int barX = screenWidth / 2 - 15;
        int barY = screenHeight / 2 - barHeight / 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CHARGE_BAR);

        guiGraphics.blit(CHARGE_BAR, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        if (chargeRatio > 0) {
            int fillBarWidth = 16;
            int fillBarHeight = 16;
            int fillHeight = (int) (fillBarHeight * chargeRatio);
            int fillY = barY + (fillBarHeight - fillHeight);

            RenderSystem.setShaderTexture(0, FILL_BAR);
            guiGraphics.blit(FILL_BAR, barX, fillY, 0, fillBarHeight - fillHeight, fillBarWidth, fillHeight, fillBarWidth, fillBarHeight);
        }

        RenderSystem.disableBlend();
    }

    private static void renderHitMarker(float partialTick, PoseStack poseStack, GuiGraphics guiGraphics) {
        if (!playingHitMarker) {
            return;
        }

        float progress = (prevHitMarkerTime + (hitMarkerTime - prevHitMarkerTime) * partialTick) / (float) hitMarkerMaxTime;
        if (progress >= 1.0F) {
            playingHitMarker = false;
            hitMarkerTime = 0;
        }
    }

    public static void playHitMarker(boolean crit) {
        playingHitMarker = true;
        hitMarkerCrit = crit;
        hitMarkerTime = 1;
        prevHitMarkerTime = 0;
    }

    private static void fetchReserveAmmo(Player player, Gun gun) {
        reserveAmmo = Gun.getReserveAmmoCount(player, gun.getProjectile().getItem());
        ammoAutoUpdateTimer = 0;
    }

    public static void stageReserveAmmoUpdate() {
        ammoAutoUpdateTimer = ammoAutoUpdateRate;
    }

    public static void updateReserveAmmo(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            Gun modifiedGun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
            fetchReserveAmmo(player, modifiedGun);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        updateHitMarker();

        if (++ammoAutoUpdateTimer >= ammoAutoUpdateRate) {
            ammoAutoUpdateTimer = 0;
        }

        if (isMeleeCooldownActive) {
            meleeCooldown--;
            if (meleeCooldown <= 0) {
                isMeleeCooldownActive = false;
                meleeCooldown = 0;
            }
        }
    }

    private static void updateHitMarker() {
        prevHitMarkerTime = hitMarkerTime;

        if (playingHitMarker) {
            hitMarkerTime++;
            if (hitMarkerTime > hitMarkerMaxTime) {
                playingHitMarker = false;
                hitMarkerTime = 0;
            }
        } else {
            hitMarkerTime = 0;
        }
    }

    public static boolean isRenderingHitMarker() {
        return playingHitMarker;
    }

    public static float getHitMarkerProgress(float partialTicks) {
        return (prevHitMarkerTime + (hitMarkerTime - prevHitMarkerTime) * partialTicks) / (float) hitMarkerMaxTime;
    }

    public static boolean getHitMarkerCrit() {
        return hitMarkerCrit;
    }
}



