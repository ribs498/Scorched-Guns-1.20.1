package top.ribs.scguns.item.animated;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.AnimatableIdCache;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.keyframe.event.ParticleKeyframeEvent;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import top.ribs.scguns.Config;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.animations.GunAnimations;
import top.ribs.scguns.attributes.SCAttributes;
import top.ribs.scguns.client.handler.MeleeAttackHandler;
import top.ribs.scguns.client.render.gun.animated.AnimatedGunRenderer;
import top.ribs.scguns.client.util.GunRotationHandler;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.event.GunEventBus;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModSounds;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageEjectCasing;
import top.ribs.scguns.network.message.C2SMessageGunLoaded;
import top.ribs.scguns.network.message.C2SMessageManualReloadEnd;
import top.ribs.scguns.network.message.C2SMessageReloadByproduct;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;


import java.util.Objects;
import java.util.function.Consumer;

public class AnimatedGunItem extends GunItem implements GeoAnimatable, GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private final String gunID;
    private final SoundEvent reloadSoundMagOut;
    private final SoundEvent reloadSoundMagIn;
    private final SoundEvent reloadSoundEnd;
    private final SoundEvent boltPullSound;
    private final SoundEvent boltReleaseSound;
    private int drawTick = 0;
    private static final String RELOAD_START_TIME = "reloadStartTime";
    public static final String RELOAD_STATE = "scguns:ReloadState";
    private final GunRotationHandler rotationHandler = new GunRotationHandler();

    public GunRotationHandler getRotationHandler() {
        return rotationHandler;
    }

    private enum ReloadState {
        NONE,
        STARTING,
        LOADING,
        STOPPING
    }
    public AnimatedGunItem(Item.Properties properties, String path, SoundEvent reloadSoundMagOut, SoundEvent reloadSoundMagIn, SoundEvent reloadSoundEnd, SoundEvent boltPullSound, SoundEvent boltReleaseSound) {
        super(properties);
        this.gunID = path;
        this.reloadSoundMagOut = reloadSoundMagOut;
        this.reloadSoundMagIn = reloadSoundMagIn;
        this.reloadSoundEnd = reloadSoundEnd;
        this.boltPullSound = boltPullSound;
        this.boltReleaseSound = boltReleaseSound;
    }
    public boolean isInCarbineMode(ItemStack stack) {

        if (stack.getItem() instanceof GunItem gunItem) {
            return gunItem.isOneHandedCarbineCandidate(stack) &&
                    (Gun.hasExtendedBarrel(stack) || Gun.hasStock(stack));
        }
        return false;
    }
    @OnlyIn(Dist.CLIENT)
    private void updateCarbineState(ItemStack stack, AnimationController<GeoAnimatable> controller) {
        boolean isCarbine = isInCarbineMode(stack);
        String currentAnim = controller.getCurrentAnimation() != null ?
                controller.getCurrentAnimation().animation().name() : "";
        boolean isCurrentCarbine = currentAnim.startsWith("carbine_");
        if (isCarbine != isCurrentCarbine) {
            controller.forceAnimationReset();
            controller.tryTriggerAnimation(isCarbine ? "carbine_idle" : "idle");
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected) {
        if (world.isClientSide()) {
            clientInventoryTick(stack, entity, slot, selected);
        }
        CompoundTag nbtCompound = stack.getOrCreateTag();
        if (!nbtCompound.contains("GeckoLibID", 99) && world instanceof ServerLevel) {
            nbtCompound.putLong("GeckoLibID", AnimatableIdCache.getFreeId((ServerLevel) world));
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void clientInventoryTick(ItemStack stack, Entity entity, int slot, boolean selected) {
        CompoundTag nbtCompound = stack.getOrCreateTag();

        long id = GeoItem.getId(stack);
        AnimationController<GeoAnimatable> animationController = this.getAnimatableInstanceCache()
                .getManagerForId(id)
                .getAnimationControllers()
                .get("controller");
        rotationHandler.updateRotations(Minecraft.getInstance().getPartialTick());
        if (nbtCompound.getBoolean("AttachmentChanged")) {
            if (animationController != null) {
                updateCarbineState(stack, animationController);
            }
            nbtCompound.remove("AttachmentChanged");
        }
        if (animationController != null && !nbtCompound.getBoolean("scguns:IsReloading")
                && !isAnimationPlaying(animationController, "draw")
                && !isAnimationPlaying(animationController, "carbine_draw")
                && !isAnimationPlaying(animationController, "shoot")
                && !isAnimationPlaying(animationController, "shoot1")
                && !isAnimationPlaying(animationController, "carbine_shoot")
                && !isAnimationPlaying(animationController, "inspect")
                && !isAnimationPlaying(animationController, "carbine_inspect")
                && !isAnimationPlaying(animationController, "reload")
                && !isAnimationPlaying(animationController, "carbine_reload")
                && !isAnimationPlaying(animationController, "reload_start")
                && !isAnimationPlaying(animationController, "carbine_reload_start")
                && !isAnimationPlaying(animationController, "reload_loop")
                && !isAnimationPlaying(animationController, "carbine_reload_loop")
                && !isAnimationPlaying(animationController, "reload_stop")
                && !isAnimationPlaying(animationController, "carbine_reload_stop")) {
            updateCarbineState(stack, animationController);
        }

        Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        boolean isManualReload = modifiedGun.getReloads().getReloadType() == ReloadType.MANUAL;
        if (entity instanceof Player player) {
            if (GeoItem.getId(player.getMainHandItem()) != id) {
                if (nbtCompound.getBoolean("IsDrawn")) {
                    nbtCompound.remove("IsDrawn");
                    nbtCompound.remove("DrawnTick");
                    this.drawTick = 0;
                    if (!isManualReload || nbtCompound.getBoolean("scguns:ReloadComplete")) {
                        nbtCompound.remove("scguns:IsReloading");
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                    }

                    nbtCompound.remove("IsShooting");
                    nbtCompound.remove("IsInspecting");
                    nbtCompound.remove("IsAiming");
                    nbtCompound.remove("IsRunning");
                    nbtCompound.remove("loaded");
                    nbtCompound.remove("DrawnTick");

                    nbtCompound.remove("scguns:IsMagReload");
                    nbtCompound.remove("scguns:IsManualReload");
                    nbtCompound.remove("scguns:IsPlayingReloadStop");
                    nbtCompound.remove("scguns:ReloadComplete");
                }
                animationController.forceAnimationReset();
                if (isInCarbineMode(stack)) {
                    animationController.tryTriggerAnimation("carbine_idle");
                } else {
                    animationController.tryTriggerAnimation("idle");
                }
                return;
            }

            this.updateBooleanTag(nbtCompound, "IsDrawing", nbtCompound.getBoolean("IsDrawn"));
            if (nbtCompound.getBoolean("IsDrawing") && nbtCompound.getInt("DrawnTick") < 15) {
                this.drawTick++;
                nbtCompound.putInt("DrawnTick", this.drawTick);
            }

            if (nbtCompound.getBoolean("IsShooting")) {
                handleShootState(nbtCompound, animationController, stack);
            }

            if (nbtCompound.getBoolean("IsInspecting")) {
                handleInspectState(animationController, stack);
            }

            if (MeleeAttackHandler.isBanzaiActive() &&
                    (isAnimationPlaying(animationController, "inspect") ||
                            isAnimationPlaying(animationController, "carbine_inspect"))) {
                if (isInCarbineMode(stack)) {
                    animationController.tryTriggerAnimation("carbine_idle");
                } else {
                    animationController.tryTriggerAnimation("idle");
                }
            }
            if (player.isSprinting() &&
                    (isAnimationPlaying(animationController, "inspect") ||
                            isAnimationPlaying(animationController, "carbine_inspect"))) {
                if (isInCarbineMode(stack)) {
                    animationController.tryTriggerAnimation("carbine_idle");
                } else {
                    animationController.tryTriggerAnimation("idle");
                }
            }

            if (!nbtCompound.getBoolean("IsDrawn")) {
                nbtCompound.putBoolean("IsDrawn", true);
            }
            boolean isSprinting = player.isSprinting();
            boolean isAiming = ModSyncedDataKeys.AIMING.getValue(player);
            boolean sReloading = ModSyncedDataKeys.RELOADING.getValue(player);
            if (isAiming && nbtCompound.getBoolean("IsDrawing") && nbtCompound.getInt("DrawnTick") < 15) {
                ModSyncedDataKeys.AIMING.setValue(player, false);
                isAiming = false;
                this.updateBooleanTag(nbtCompound, "IsAiming", false);
            }
            if (sReloading && !nbtCompound.getBoolean("scguns:IsReloading")) {
                nbtCompound.putBoolean("scguns:IsReloading", true);
                if (!nbtCompound.contains(RELOAD_STATE)) {
                    nbtCompound.putString(RELOAD_STATE, ReloadState.NONE.name());
                }
            } else if (!sReloading && nbtCompound.getBoolean("scguns:IsReloading")) {
                if (modifiedGun.getReloads().getReloadType() == ReloadType.MANUAL) {
                    if (!nbtCompound.contains(RELOAD_STATE) ||
                            !nbtCompound.getString(RELOAD_STATE).equals(ReloadState.STOPPING.name())) {
                        nbtCompound.putString(RELOAD_STATE, ReloadState.STOPPING.name());
                        nbtCompound.putBoolean("scguns:IsPlayingReloadStop", true);
                    }
                } else {
                    nbtCompound.remove("scguns:IsReloading");
                }
            }

            this.updateBooleanTag(nbtCompound, "IsAiming", isAiming);
            this.updateBooleanTag(nbtCompound, "IsRunning", isSprinting);

            if (nbtCompound.getBoolean("IsDrawing") && nbtCompound.getInt("DrawnTick") < 15
                    && Config.COMMON.gameplay.drawAnimation.get()) {
                this.handleDrawingState(nbtCompound, animationController, stack);
                return;
            }

            if (nbtCompound.getInt("DrawnTick") >= 15) {
                assert animationController != null;
                if (!isAnimationPlaying(animationController, "draw") &&
                        !isAnimationPlaying(animationController, "carbine_draw") &&
                        !isAnimationPlaying(animationController, "jam") &&
                        !isAnimationPlaying(animationController, "melee") &&
                        !isAnimationPlaying(animationController, "bayonet") &&
                        !isAnimationPlaying(animationController, "shoot") &&
                        !isAnimationPlaying(animationController, "shoot1") &&
                        !isAnimationPlaying(animationController, "carbine_shoot") &&
                        !isAnimationPlaying(animationController, "aim_shoot") &&
                        !isAnimationPlaying(animationController, "aim_shoot1") &&
                        !isAnimationPlaying(animationController, "carbine_aim_shoot") &&
                        !isAnimationPlaying(animationController, "inspect") &&
                        !isAnimationPlaying(animationController, "carbine_inspect") &&
                        !isAnimationPlaying(animationController, "reload_stop") &&
                        !isAnimationPlaying(animationController, "carbine_reload_stop")) {
                    if (nbtCompound.getBoolean("scguns:IsReloading") ||
                            (nbtCompound.contains(RELOAD_STATE) &&
                                    nbtCompound.getString(RELOAD_STATE).equals(ReloadState.STOPPING.name()))) {
                        this.handleReloadingState(nbtCompound, animationController, stack);
                    } else if (nbtCompound.getBoolean("IsAiming")) {
                        this.handleAimingState(nbtCompound, animationController);
                    } else if (nbtCompound.getBoolean("IsRunning") &&
                            !isAnimationPlaying(animationController, "inspect") &&
                            !isAnimationPlaying(animationController, "carbine_inspect") &&
                            !isAnimationPlaying(animationController, "reload") &&
                            !isAnimationPlaying(animationController, "carbine_reload") &&
                            !isAnimationPlaying(animationController, "reload_alt") &&
                            !isAnimationPlaying(animationController, "carbine_reload_loop") &&
                            !isAnimationPlaying(animationController, "reload_loop")) {
                        this.handleRunningState(animationController);
                    } else if (!isAnimationPlaying(animationController, "inspect") &&
                            !isAnimationPlaying(animationController, "carbine_inspect") &&
                            !isAnimationPlaying(animationController, "carbine_reload") &&
                            !isAnimationPlaying(animationController, "reload")) {
                        if (isInCarbineMode(stack)) {
                            animationController.tryTriggerAnimation("carbine_idle");
                        } else {
                            animationController.tryTriggerAnimation("idle");
                        }
                    }
                }
            }
        }
    }
    public boolean isAnimationPlaying(AnimationController<GeoAnimatable> animationController, String animationName) {
        return animationController.getCurrentAnimation() != null &&
                animationController.getCurrentAnimation().animation().name().equals(animationName);
    }

    private void updateBooleanTag(CompoundTag nbt, String key, boolean value) {
        if (value) {
            nbt.putBoolean(key, true);
        } else {
            nbt.remove(key);
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handleDrawingState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        double drawSpeedMultiplier = 1.0;

        int quickHandsLevel = GunEnchantmentHelper.getQuickHands(stack);
        if (quickHandsLevel > 0) {
            drawSpeedMultiplier += 0.12 * quickHandsLevel;
        }
        int lightweightLevel = GunEnchantmentHelper.getLightweight(stack);
        if (lightweightLevel > 0) {
            drawSpeedMultiplier += 0.05 * lightweightLevel;
        }
        drawSpeedMultiplier = GunModifierHelper.getModifiedDrawSpeed(stack, drawSpeedMultiplier);

        animationController.setAnimationSpeed(drawSpeedMultiplier);

        if (nbt.getInt("DrawnTick") < 15 && !nbt.getBoolean("scguns:IsReloading")) {
            if (isInCarbineMode(stack)) {
                animationController.tryTriggerAnimation("carbine_draw");
            } else {
                animationController.tryTriggerAnimation("draw");
            }
        }
        nbt.remove("IsShooting");
        nbt.remove("IsInspecting");
    }
    @OnlyIn(Dist.CLIENT)
    private void handleNormalReload(CompoundTag nbt, AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        boolean isCarbine = isInCarbineMode(stack);
        String reloadAnim = isCarbine ? "carbine_reload" : "reload";
        if (nbt.getBoolean("scguns:IsReloading") && !isAnimationPlaying(animationController, reloadAnim)) {
            if (nbt.getInt("DrawnTick") >= 15) {
                if (!isAnimationPlaying(animationController, "draw") &&
                        !isAnimationPlaying(animationController, "shoot") &&
                        !isAnimationPlaying(animationController, "shoot1") &&
                        !isAnimationPlaying(animationController, "aim_shoot")) {
                    animationController.tryTriggerAnimation(reloadAnim);
                }
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handleReloadingState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        double reloadSpeedMultiplier = 1.0;
        if (Minecraft.getInstance().player != null) {
            AttributeInstance reloadSpeedAttribute = Minecraft.getInstance().player.getAttribute(SCAttributes.RELOAD_SPEED.get());
            if (reloadSpeedAttribute != null) {
                reloadSpeedMultiplier = reloadSpeedAttribute.getValue();
            }
        }
        int actualReloadTime = (int) Math.ceil(GunEnchantmentHelper.getRealReloadSpeed(stack) / reloadSpeedMultiplier);
        float speedMultiplier = (float) modifiedGun.getReloads().getReloadTimer() / actualReloadTime;

        if (modifiedGun.getReloads().getReloadType() != ReloadType.MANUAL) {
            handleNormalReload(nbt, animationController, stack);
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int currentAmmo = tag.getInt("AmmoCount");
        int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(stack, modifiedGun);
        boolean ammoFull = currentAmmo >= maxAmmo;
        String currentAnimation = animationController.getCurrentAnimation() != null ?
                animationController.getCurrentAnimation().animation().name() : "none";

        if (ammoFull && (isAnimationPlaying(animationController, "carbine_reload_loop") ||
                isAnimationPlaying(animationController, "reload_loop"))) {
            animationController.stop();
            animationController.setAnimationSpeed(speedMultiplier);
            nbt.putString(RELOAD_STATE, ReloadState.STOPPING.name());
            animationController.tryTriggerAnimation(isInCarbineMode(stack) ? "carbine_reload_stop" : "reload_stop");
            nbt.putBoolean("scguns:IsPlayingReloadStop", true);
            return;
        }

        String currentState = nbt.getString(RELOAD_STATE);
        switch (ReloadState.valueOf(currentState.isEmpty() ? ReloadState.NONE.name() : currentState)) {
            case NONE:
                nbt.putString(RELOAD_STATE, ReloadState.LOADING.name());
                animationController.setAnimationSpeed(speedMultiplier);
                animationController.tryTriggerAnimation(isInCarbineMode(stack) ? "carbine_reload_start" : "reload_start");
                break;

            case LOADING:
                if (animationController.getAnimationState() == AnimationController.State.STOPPED &&
                        !isAnimationPlaying(animationController, "reload_stop") &&
                        !isAnimationPlaying(animationController, "carbine_reload_stop")) {
                    animationController.setAnimationSpeed(speedMultiplier);
                    animationController.tryTriggerAnimation(isInCarbineMode(stack) ? "carbine_reload_loop" : "reload_loop");
                }
                break;

            case STOPPING:
                if (animationController.getAnimationState() == AnimationController.State.STOPPED &&
                        !isAnimationPlaying(animationController, "reload_stop") &&
                        !isAnimationPlaying(animationController, "carbine_reload_stop")) {
                    if (currentAnimation.equals("idle") || currentAnimation.equals("carbine_idle")) {
                        cleanupReloadState(nbt);
                    } else {
                        animationController.setAnimationSpeed(speedMultiplier);
                        animationController.tryTriggerAnimation(isInCarbineMode(stack) ? "carbine_reload_stop" : "reload_stop");
                    }
                }
                break;
        }
    }

    public void cleanupReloadState(CompoundTag nbt) {
        nbt.remove(RELOAD_STATE);
        nbt.remove(RELOAD_START_TIME);
        nbt.remove("scguns:ReloadComplete");
        nbt.remove("scguns:IsPlayingReloadStop");
        nbt.remove("scguns:IsMagReload");
        nbt.remove("scguns:IsManualReload");
        nbt.remove("loaded");
        nbt.remove("IsReloading");
        nbt.remove("scguns:IsReloading");
        nbt.remove("scguns:PausedDuringReload");
    }

    @OnlyIn(Dist.CLIENT)
    private void handleAimingState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController) {

        if (nbt.getBoolean("IsDrawing") && nbt.getInt("DrawnTick") < 15) {
            return;
        }

        animationController.setAnimationSpeed(1.0);
        nbt.remove("IsInspecting");
        assert Minecraft.getInstance().player != null;
        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        if (stack.getItem() instanceof AnimatedGunItem && isInCarbineMode(stack)) {
            animationController.tryTriggerAnimation("carbine_idle");
        } else {
            animationController.tryTriggerAnimation("idle");
        }
    }
    private void handleRunningState(AnimationController<GeoAnimatable> animationController) {
        animationController.setAnimationSpeed(1.0);
        assert Minecraft.getInstance().player != null;
        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        boolean isCarbine = stack.getItem() instanceof AnimatedGunItem && isInCarbineMode(stack);

        if (isAnimationPlaying(animationController, isCarbine ? "carbine_inspect" : "inspect")) {
            animationController.tryTriggerAnimation(isCarbine ? "carbine_idle" : "idle");
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handleInspectState(AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        boolean isCarbine = isInCarbineMode(stack);
        String animToPlay = isCarbine ? "carbine_inspect" : "inspect";

        if (!isAnimationPlaying(animationController, animToPlay)) {
            animationController.setAnimationSpeed(1.0);
            animationController.forceAnimationReset();
            animationController.tryTriggerAnimation(animToPlay);
        }
    }
    private void handleShootState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        boolean isCarbine = isInCarbineMode(stack);

        if (stack.getItem() instanceof AnimatedDualWieldGunItem) {
            if (nbt.getBoolean("IsAiming")) {
                boolean useAlternate = GunEventBus.RatKingAndQueenModel.GunFireEventRatHandler.shouldUseAlternateAnimation();
                animationController.tryTriggerAnimation(useAlternate ? "aim_shoot1" : "aim_shoot");
            } else {
                boolean useAlternate = GunEventBus.RatKingAndQueenModel.GunFireEventRatHandler.shouldUseAlternateAnimation();
                animationController.tryTriggerAnimation(useAlternate ? "shoot1" : "shoot");
            }
            GunEventBus.RatKingAndQueenModel.GunFireEventRatHandler.incrementShotCount();
        } else {
            if (nbt.getBoolean("IsAiming")) {
                animationController.tryTriggerAnimation(isCarbine ? "carbine_aim_shoot" : "aim_shoot");
            } else {
                animationController.tryTriggerAnimation(isCarbine ? "carbine_shoot" : "shoot");
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void soundListener(SoundKeyframeEvent<AnimatedGunItem> gunItemSoundKeyframeEvent) {
        Player player = ClientUtils.getClientPlayer();
        if (player != null) {
            switch (gunItemSoundKeyframeEvent.getKeyframeData().getSound()) {
                case "gun_rustle":
                    player.playSound(ModSounds.GUN_RUSTLE.get(), 1.0F, 1.0F);
                    break;
                case "metal":
                    player.playSound(ModSounds.METAL.get(), 1.0F, 1.0F);
                    break;
                case "jam":
                    player.playSound(ModSounds.COPPER_GUN_JAM.get(), 1.0F, 1.0F);
                    break;
                case "lever":
                    player.playSound(ModSounds.LEVER.get(), 1.0F, 1.0F);
                    break;
                case "slap":
                    player.playSound(ModSounds.SLAP.get(), 1.0F, 1.0F);
                    break;
                case "rack":
                    player.playSound(ModSounds.RACK.get(), 1.0F, 1.0F);
                case "reload_mag_out":
                    player.playSound(this.reloadSoundMagOut, 1.0F, 1.0F);
                    break;
                case "reload_mag_in":
                    player.playSound(this.reloadSoundMagIn, 1.0F, 1.0F);
                    break;
                case "reload_end":
                    player.playSound(this.reloadSoundEnd, 1.0F, 1.0F);
                    break;
                case "bolt_pull":
                    player.playSound(this.boltPullSound, 1.0F, 1.0F);
                    break;
                case "bolt_release":
                    player.playSound(this.boltReleaseSound, 1.0F, 1.0F);
                case "bolt":
                    player.playSound(ModSounds.BOLT.get(), 1.0F, 1.0F);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void particleListener(ParticleKeyframeEvent<AnimatedGunItem> gunItemParticleKeyframeEvent) {
        Player player = ClientUtils.getClientPlayer();
        if (player != null && player.getMainHandItem().getItem() instanceof AnimatedGunItem) {
            ItemStack stack = player.getMainHandItem();
            GunItem gunItem = (GunItem) stack.getItem();
            Gun gun = gunItem.getModifiedGun(stack);
            CompoundTag tag = stack.getOrCreateTag();

            String effect = gunItemParticleKeyframeEvent.getKeyframeData().getEffect();
            switch(effect) {
                case "stop_mag_tracking":
                    tag.remove("scguns:MagazineTracking");
                    break;
                case "loaded":
                    tag.putBoolean("loaded", true);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageGunLoaded());
                    break;

                case "loop_end":
                    if (tag.getBoolean("shouldStopOnLoopEnd")) {
                        tag.putBoolean("shouldTransitionToStop", true);
                        tag.remove("shouldStopOnLoopEnd");
                    }
                    break;

                case "end_reload":
                    if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                        tag.putBoolean("scguns:ReloadComplete", true);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageManualReloadEnd());
                    }
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageReloadByproduct());
                    break;

                case "eject_casing":
                    if (gun.getProjectile().ejectsCasing() && gun.getProjectile().ejectDuringReload()) {
                        Level level = player.level();
                        GunEventBus.ejectCasing(level, player, false);
                        if (gun.getProjectile().casingType != null && !player.getAbilities().instabuild) {
                            new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(gun.getProjectile().casingType)));

                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageEjectCasing());
                        }
                    }
                    break;
            }
        }
    }
    @Override
    public boolean isPerspectiveAware() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private AnimatedGunRenderer renderer;

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedGunRenderer(new ResourceLocation("scguns", gunID));
                }
                return this.renderer;
            }
        });
    }
    @OnlyIn(Dist.CLIENT)
    private PlayState predicate(AnimationState<AnimatedGunItem> event) {
        if (event.getController().getCurrentAnimation() == null ||
                event.getController().getAnimationState() == AnimationController.State.STOPPED) {
            assert Minecraft.getInstance().player != null;
            ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
            if (stack.getItem() instanceof AnimatedGunItem gunItem && gunItem.isInCarbineMode(stack)) {
                event.getController().tryTriggerAnimation("carbine_idle");
            } else {
                event.getController().tryTriggerAnimation("idle");
            }
        }
        return PlayState.CONTINUE;
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registerClientControllers(controllers);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void registerClientControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<AnimatedGunItem> controller = new AnimationController<>(this, "controller", 0, this::predicate)
                .setSoundKeyframeHandler(this::soundListener)
                .setParticleKeyframeHandler(this::particleListener)
                .triggerableAnim("idle", GunAnimations.IDLE)
                .triggerableAnim("carbine_idle", GunAnimations.CARBINE_IDLE)
                .triggerableAnim("shoot", GunAnimations.SHOOT)
                .triggerableAnim("shoot1", GunAnimations.SHOOT1)
                .triggerableAnim("carbine_shoot", GunAnimations.CARBINE_SHOOT)
                .triggerableAnim("aim_shoot", GunAnimations.AIM_SHOOT)
                .triggerableAnim("aim_shoot1", GunAnimations.AIM_SHOOT1)
                .triggerableAnim("carbine_aim_shoot", GunAnimations.CARBINE_AIM_SHOOT)
                .triggerableAnim("reload", GunAnimations.RELOAD)
                .triggerableAnim("carbine_reload", GunAnimations.CARBINE_RELOAD)
                .triggerableAnim("reload_alt", GunAnimations.RELOAD_ALT)
                .triggerableAnim("reload_start", GunAnimations.RELOAD_START)
                .triggerableAnim("carbine_reload_start", GunAnimations.CARBINE_RELOAD_START)
                .triggerableAnim("reload_loop", GunAnimations.RELOAD_LOOP)
                .triggerableAnim("carbine_reload_loop", GunAnimations.CARBINE_RELOAD_LOOP)
                .triggerableAnim("reload_stop", GunAnimations.RELOAD_STOP)
                .triggerableAnim("carbine_reload_stop", GunAnimations.CARBINE_RELOAD_STOP)
                .triggerableAnim("draw", GunAnimations.DRAW)
                .triggerableAnim("carbine_draw", GunAnimations.CARBINE_DRAW)
                .triggerableAnim("inspect", GunAnimations.INSPECT)
                .triggerableAnim("carbine_inspect", GunAnimations.CARBINE_INSPECT)
                .triggerableAnim("jam", GunAnimations.JAM);
        controllers.add(controller);
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}