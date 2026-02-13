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
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.cache.HotBarrelCache;
import top.ribs.scguns.common.ChargeHandler;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.ammo_boxes.CreativeAmmoBoxItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.item.exosuit.RabbitModuleItem;
import top.ribs.scguns.util.GunModifierHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.ribs.scguns.common.exosuit.ExoSuitData;
import top.ribs.scguns.common.exosuit.ExoSuitPowerManager;
import top.ribs.scguns.common.exosuit.ExoSuitUpgrade;
import top.ribs.scguns.common.exosuit.ExoSuitUpgradeManager;
import top.ribs.scguns.item.animated.ExoSuitItem;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
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

    private static ItemStack cachedGunStack = ItemStack.EMPTY;
    private static Gun cachedGun = null;
    private static CompoundTag cachedTag = null;
    private static boolean cachedHasCreativeBox = false;
    private static int cacheValidityTimer = 0;
    private static final int CACHE_DURATION = 10;

    private static int cachedScreenWidth = -1;
    private static int cachedScreenHeight = -1;
    private static int screenCacheTimer = 0;
    private static final int SCREEN_CACHE_DURATION = 60;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) {
            return;
        }

        if (event.getOverlay() != VanillaGuiOverlay.VIGNETTE.type()) {
          return
        };

        updateScreenCache(mc);

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();

        renderExoSuitStatusHUD(poseStack, guiGraphics, player);

        if (!Config.CLIENT.display.displayGunInfo.get()) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof GunItem)) {
            return;
        }

        updateCache(heldItem, player);

        renderGunInfoHUD(heldItem, poseStack, guiGraphics, player);
        renderChargeBarHUD(heldItem, event.getPartialTick(), poseStack, guiGraphics, player);
        renderMeleeCooldownHUD(event.getPartialTick(), poseStack, guiGraphics);
        renderHitMarker(event.getPartialTick(), poseStack, guiGraphics);
        renderHotBarrelOverlay(heldItem, poseStack, guiGraphics);
    }

    private static void updateCache(ItemStack heldItem, Player player) {
        if (cacheValidityTimer <= 0 || !ItemStack.matches(cachedGunStack, heldItem)) {
            cachedGunStack = heldItem.copy();
            cachedGun = ((GunItem) heldItem.getItem()).getGun();
            cachedTag = heldItem.getTag();

            updateCreativeBoxCache(player);
            cacheValidityTimer = CACHE_DURATION;
        } else {
            cacheValidityTimer--;
        }
    }

    private static void updateCreativeBoxCache(Player player) {
        cachedHasCreativeBox = player.getInventory().items.stream()
                .anyMatch(itemStack -> itemStack.getItem() instanceof CreativeAmmoBoxItem);

        if (!cachedHasCreativeBox) {
            AtomicBoolean hasBox = new AtomicBoolean(false);
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                IItemHandlerModifiable curios = handler.getEquippedCurios();
                for (int i = 0; i < curios.getSlots() && !hasBox.get(); i++) {
                    ItemStack stack = curios.getStackInSlot(i);
                    if (stack.getItem() instanceof CreativeAmmoBoxItem) {
                        hasBox.set(true);
                    }
                }
            });
            cachedHasCreativeBox = hasBox.get();
        }

        if (!cachedHasCreativeBox) {
            cachedHasCreativeBox = hasCreativeAmmoBoxInExoSuit(player);
        }
    }

    private static boolean hasCreativeAmmoBoxInExoSuit(Player player) {
        ItemStack chestplate = getEquippedChestplate(player);
        if (chestplate.isEmpty()) {
            return false;
        }

        ItemStack pouchUpgrade = findPouchUpgrade(chestplate);
        if (pouchUpgrade.isEmpty()) {
            return false;
        }

        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pouchUpgrade);
        if (upgrade == null) {
            return false;
        }

        String pouchId = getPouchId(pouchUpgrade);
        ItemStackHandler pouchInventory = getPouchInventory(chestplate, pouchId, upgrade.getDisplay().getStorageSize());

        for (int i = 0; i < pouchInventory.getSlots(); i++) {
            ItemStack stack = pouchInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof CreativeAmmoBoxItem) {
                return true;
            }
        }

        return false;
    }

    private static ItemStack getEquippedChestplate(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack findPouchUpgrade(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("pouches")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static String getPouchId(ItemStack pouchUpgrade) {
        return pouchUpgrade.getItem().toString();
    }

    private static ItemStackHandler getPouchInventory(ItemStack chestplate, String pouchId, int size) {
        CompoundTag pouchData = chestplate.getOrCreateTag().getCompound("PouchData");

        ItemStackHandler handler = new ItemStackHandler(size);
        if (pouchData.contains(pouchId)) {
            handler.deserializeNBT(pouchData.getCompound(pouchId));
        }

        return handler;
    }

    private static void updateScreenCache(Minecraft mc) {
        if (screenCacheTimer <= 0) {
            cachedScreenWidth = mc.getWindow().getGuiScaledWidth();
            cachedScreenHeight = mc.getWindow().getGuiScaledHeight();
            screenCacheTimer = SCREEN_CACHE_DURATION;
        } else {
            screenCacheTimer--;
        }
    }

    public static void renderHotBarrelOverlay(ItemStack heldItem, PoseStack poseStack, GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOT_BARREL.get(), heldItem) <= 0) {
            return;
        }

        float chargeRatio = HotBarrelCache.getSmoothHotBarrelPercentage(player, heldItem);
        if (chargeRatio <= 0.0f) {
            return;
        }

        int barWidth = 128;
        int barHeight = 8;
        float scale = 1.25f;
        int barX = (int) (((float) cachedScreenWidth / 2) - ((barWidth * scale) / 2));
        int barY = (int) (cachedScreenHeight - 45 * scale);

        poseStack.pushPose();
        poseStack.scale(scale, scale, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, HOT_BARREL_TEXTURE);

        int scaledBarX = (int) (barX / scale);
        int scaledBarY = (int) (barY / scale);

        guiGraphics.blit(HOT_BARREL_TEXTURE, scaledBarX, scaledBarY, 0, 0, barWidth, 5, barWidth, barHeight);

        int fillWidth = (int) (barWidth * chargeRatio);
        if (fillWidth > 0) {
            int fillY = scaledBarY + 1;
            guiGraphics.blit(HOT_BARREL_TEXTURE, scaledBarX, fillY, 0, 5, fillWidth, 3, barWidth, barHeight);
        }

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static void renderMeleeCooldownHUD(float partialTick, PoseStack poseStack, GuiGraphics guiGraphics) {
        if (!isMeleeCooldownActive) {
            return;
        }

        int barWidth = 16;
        int barHeight = 16;
        int barX = (int) (cachedScreenWidth * 0.87);
        int barY = (int) (cachedScreenHeight * 0.73);

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

    private static void renderGunInfoHUD(ItemStack heldItem, PoseStack poseStack, GuiGraphics guiGraphics, Player player) {
        if (!Config.CLIENT.display.displayGunInfo.get()) {
            return;
        }

        if (cachedTag == null) {
            return;
        }
        if (Config.CLIENT.display.immersiveGunInfo.get()) {
            if (heldItem.getItem() instanceof AnimatedGunItem animatedGun) {
                long id = GeoItem.getId(heldItem);
                AnimationController<GeoAnimatable> controller = animatedGun.getAnimatableInstanceCache()
                        .getManagerForId(id)
                        .getAnimationControllers()
                        .get("controller");

                if (controller == null ||
                        (!animatedGun.isAnimationPlaying(controller, "inspect") &&
                                !animatedGun.isAnimationPlaying(controller, "carbine_inspect"))) {
                    return;
                }
            }
        }

        int currentAmmo = cachedTag.getInt("AmmoCount");
        int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(heldItem, cachedGun);

        boolean isCreative = player.isCreative();
        MutableComponent ammoCountValue;
        MutableComponent reserveAmmoValue;

        if (isCreative) {
            ammoCountValue = Component.literal("∞ / ∞").withStyle(ChatFormatting.BOLD);
            reserveAmmoValue = Component.literal("∞").withStyle(ChatFormatting.BOLD);
        } else {
            ammoCountValue = Component.literal(currentAmmo + " / " + maxAmmo).withStyle(ChatFormatting.BOLD);
            if (cachedHasCreativeBox) {
                reserveAmmoValue = Component.literal("∞").withStyle(ChatFormatting.BOLD);
            } else {
                reserveAmmoValue = Component.literal(String.valueOf(reserveAmmo)).withStyle(ChatFormatting.BOLD);
            }
        }

        int ammoPosX = (int) (cachedScreenWidth * 0.88);
        int ammoPosY = (int) (cachedScreenHeight * 0.8);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft mc = Minecraft.getInstance();

        if (ModSyncedDataKeys.RELOADING.getValue(player)) {
            if (player.isAlive()) {
                guiGraphics.drawString(mc.font, "Reloading...", ammoPosX, ammoPosY - 10, 0xFFFF55);
            }
        }

        guiGraphics.drawString(mc.font, ammoCountValue, ammoPosX, ammoPosY, (currentAmmo > 0 || isCreative ? 0xFFFFFF : 0xFF5555));
        int reserveAmmoPosY = ammoPosY + 10;
        guiGraphics.drawString(mc.font, reserveAmmoValue, ammoPosX, reserveAmmoPosY, (reserveAmmo <= 0 && !Gun.hasUnlimitedReloads(heldItem) ? 0x555555 : 0xAAAAAA));

        ItemStack ammoItemStack = new ItemStack(Objects.requireNonNull(cachedGun.getProjectile().getItem()));
        renderAmmoTypeTexture(ammoItemStack, ammoPosX - 20, ammoPosY, guiGraphics, mc);

        RenderSystem.disableBlend();
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
        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);

        if (gun.getGeneral().getFireMode() != FireMode.PULSE) {
            return;
        }

        int maxChargeTime = gun.getGeneral().getFireTimer();
        int chargeTime = ChargeHandler.getChargeTime(player.getUUID());
        if (chargeTime <= 0) {
            return;
        }

        float chargeRatio = (float) chargeTime / maxChargeTime;

        int barWidth = 16;
        int barHeight = 16;
        int barX = cachedScreenWidth / 2 - 15;
        int barY = cachedScreenHeight / 2 - barHeight / 2;

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
    private static void renderExoSuitStatusHUD(PoseStack poseStack, GuiGraphics guiGraphics, Player player) {
        if (!hasAnyExoSuitPiece(player)) {
            return;
        }

        int hudX = 3;
        int hudY = cachedScreenHeight - 60;
        int lineHeight = 10;
        int currentY = hudY;

        Minecraft mc = Minecraft.getInstance();

        // Power Core Display
        ItemStack chestplate = getEquippedExoSuitChestplate(player);
        if (!chestplate.isEmpty()) {
            ItemStack powerCore = findPowerCoreInChestplate(chestplate);
            if (!powerCore.isEmpty()) {
                int energyStored = powerCore.getCapability(ForgeCapabilities.ENERGY)
                        .map(IEnergyStorage::getEnergyStored).orElse(0);
                int maxEnergy = powerCore.getCapability(ForgeCapabilities.ENERGY)
                        .map(IEnergyStorage::getMaxEnergyStored).orElse(0);

                if (maxEnergy > 0) {
                    int energyPercent = (energyStored * 100) / maxEnergy;
                    ChatFormatting energyColor = getEnergyColorForHUD(energyPercent);
                    Component powerText = Component.translatable("tooltip.scguns.exosuit.energy_level", energyPercent)
                            .withStyle(energyColor);
                    guiGraphics.drawString(mc.font, powerText, hudX, currentY,
                            energyColor.getColor() != null ? energyColor.getColor() : 0xFFFFFF);
                    currentY += lineHeight;
                }
            } else {
                Component noPowerText = Component.translatable("hud.scguns.exosuit.no_power_core")
                        .withStyle(ChatFormatting.RED);
                guiGraphics.drawString(mc.font, noPowerText, hudX, currentY, 0xFF5555);
                currentY += lineHeight;
            }
        }

        ItemStack helmet = getEquippedExoSuitHelmet(player);
        if (!helmet.isEmpty() && hasHudModule(helmet)) {
            boolean hudEnabled = ExoSuitPowerManager.isPowerEnabled(player, "hud");
            boolean canFunction = ExoSuitPowerManager.canUpgradeFunction(player, "hud");

            String hudModuleKey = getHudModuleTranslationKey(helmet);
            String statusText = getModuleStatusText(hudEnabled, canFunction);
            ChatFormatting statusColor = getModuleStatusColor(hudEnabled, canFunction);

            Component helmetText = Component.translatable(hudModuleKey)
                    .append(": " + statusText)
                    .withStyle(statusColor);
            guiGraphics.drawString(mc.font, helmetText, hudX, currentY,
                    statusColor.getColor() != null ? statusColor.getColor() : 0xFFFFFF);
            currentY += lineHeight;
        }

        if (!chestplate.isEmpty() && hasUtilityModule(chestplate)) {
            boolean utilityEnabled = ExoSuitPowerManager.isPowerEnabled(player, "utility");
            boolean canFunction = ExoSuitPowerManager.canUpgradeFunction(player, "utility");

            String utilityModuleKey = getUtilityModuleTranslationKey(chestplate);
            String statusText = getModuleStatusText(utilityEnabled, canFunction);
            ChatFormatting statusColor = getModuleStatusColor(utilityEnabled, canFunction);

            Component chestText = Component.translatable(utilityModuleKey)
                    .append(": " + statusText)
                    .withStyle(statusColor);
            guiGraphics.drawString(mc.font, chestText, hudX, currentY,
                    statusColor.getColor() != null ? statusColor.getColor() : 0xFFFFFF);
            currentY += lineHeight;
        }

        // Boots Mobility Module Display
        ItemStack boots = getEquippedExoSuitBoots(player);
        if (!boots.isEmpty() && hasMobilityModule(boots)) {
            boolean mobilityEnabled = ExoSuitPowerManager.isPowerEnabled(player, "mobility");
            boolean canFunction = ExoSuitPowerManager.canUpgradeFunction(player, "mobility");

            String mobilityModuleKey = getMobilityModuleTranslationKey(boots);
            String statusText = getModuleStatusText(mobilityEnabled, canFunction);
            ChatFormatting statusColor = getModuleStatusColor(mobilityEnabled, canFunction);

            Component bootsText = Component.translatable(mobilityModuleKey)
                    .append(": " + statusText)
                    .withStyle(statusColor);
            guiGraphics.drawString(mc.font, bootsText, hudX, currentY,
                    statusColor.getColor() != null ? statusColor.getColor() : 0xFFFFFF);
        }
    }
    private static boolean hasUtilityModule(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("utility")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getUtilityModuleTranslationKey(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("utility")) {
                    if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.JetpackModuleItem) {
                        return "exosuit.upgrade.jetpack";
                    }
                    else if (upgrade.getEffects().hasFlight()) {
                        return "exosuit.upgrade.jetpack";
                    }
                    else {
                        return "exosuit.upgrade.utility";
                    }
                }
            }
        }
        return "exosuit.upgrade.utility";
    }
    private static boolean hasAnyExoSuitPiece(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack getEquippedExoSuitHelmet(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.HELMET) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack getEquippedExoSuitChestplate(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack getEquippedExoSuitBoots(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.BOOTS) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack findPowerCoreInChestplate(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("power_core")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean hasHudModule(ItemStack helmet) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(helmet, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("hud")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasMobilityModule(ItemStack boots) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(boots, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("mobility")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getHudModuleTranslationKey(ItemStack helmet) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(helmet, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("hud")) {
                    if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.NightVisionModuleItem) {
                        return "exosuit.upgrade.night_vision";
                    } else if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.TargetTrackerModuleItem) {
                        return "exosuit.upgrade.target_tracker";
                    } else {
                        return "exosuit.upgrade.hud";
                    }
                }
            }
        }
        return "exosuit.upgrade.hud";
    }

    private static String getMobilityModuleTranslationKey(ItemStack boots) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(boots, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("mobility")) {
                    return "exosuit.upgrade.mobility";
                }
            }
        }
        return "exosuit.upgrade.mobility";
    }

    // Simple status text method
    private static String getModuleStatusText(boolean enabled, boolean canFunction) {
        if (!enabled) {
            return "Disabled";
        } else if (!canFunction) {
            return "No Power";
        } else {
            return "Active";
        }
    }

    private static ChatFormatting getEnergyColorForHUD(int energyPercent) {
        if (energyPercent > 75) {
            return ChatFormatting.GREEN;
        } else if (energyPercent > 50) {
            return ChatFormatting.YELLOW;
        } else if (energyPercent > 25) {
            return ChatFormatting.GOLD;
        } else if (energyPercent > 0) {
            return ChatFormatting.RED;
        } else {
            return ChatFormatting.DARK_RED;
        }
    }

    private static ChatFormatting getModuleStatusColor(boolean enabled, boolean canFunction) {
        if (!enabled) {
            return ChatFormatting.GRAY;
        } else if (!canFunction) {
            return ChatFormatting.RED;
        } else {
            return ChatFormatting.GREEN;
        }
    }
}
