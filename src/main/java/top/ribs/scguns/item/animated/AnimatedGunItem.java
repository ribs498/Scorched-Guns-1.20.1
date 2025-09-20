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
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.loading.FMLEnvironment;
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
import top.ribs.scguns.animations.GunAnimations;
import top.ribs.scguns.attributes.SCAttributes;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.client.handler.DualWieldShotTracker;
import top.ribs.scguns.client.handler.MeleeAttackHandler;
import top.ribs.scguns.client.render.gun.animated.AnimatedGunRenderer;
import top.ribs.scguns.client.util.GunRotationHandler;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.common.network.ServerPlayHandler;
import top.ribs.scguns.event.GunEventBus;
import top.ribs.scguns.init.ModSounds;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageEjectCasing;
import top.ribs.scguns.network.message.C2SMessageGunLoaded;
import top.ribs.scguns.network.message.C2SMessageReload;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;
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
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected) {
        if (entity instanceof ItemEntity) {
            CompoundTag tag = stack.getOrCreateTag();
            cleanupReloadState(tag);
            tag.remove("IsShooting");
            tag.remove("IsInspecting");
            tag.remove("IsAiming");
            tag.remove("IsRunning");
            tag.remove("IsDrawn");
            tag.remove("IsDrawing");
            tag.remove("DrawnTick");
            tag.remove("loaded");
            tag.remove("shouldStopOnLoopEnd");
            tag.remove("shouldTransitionToStop");
            tag.remove("MagazinePosition");
            tag.remove("MagazineOverride");
            tag.remove("scguns:MagazineTracking");
            // Clean up session marker
            tag.remove("_InitializedThisSession");

            tag.putBoolean("IsDroppedItem", true);

            if (world.isClientSide()) {
                tag.getBoolean("WasReloadingLastTick");
                boolean isReloading = tag.getBoolean("scguns:IsReloading");

                tag.putBoolean("WasReloadingLastTick", isReloading);

                clientInventoryTick(stack, entity, slot, selected);
            }
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.remove("IsDroppedItem");
        if (entity instanceof Player player) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand != stack && GeoItem.getId(mainHand) != GeoItem.getId(stack)) {
                tag.remove("_InitializedThisSession");
            }
        }

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
        handleReloadStateSynchronization(stack, entity, nbtCompound);
        handleAnimationControllerUpdates(stack, entity, nbtCompound);
        if (entity instanceof Player player) {
            handlePlayerSpecificLogic(stack, player, nbtCompound);
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handleReloadStateSynchronization(ItemStack stack, Entity entity, CompoundTag nbtCompound) {
        boolean currentReloading = nbtCompound.getBoolean("scguns:IsReloading");
        boolean serverReloading = entity instanceof Player ?
                ModSyncedDataKeys.RELOADING.getValue((Player)entity) : false;
        String currentReloadState = nbtCompound.getString(RELOAD_STATE);
        boolean inCriticalPhase = nbtCompound.getBoolean("InCriticalReloadPhase");

        Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        boolean isManualReload = modifiedGun.getReloads().getReloadType() == ReloadType.MANUAL;

        long currentTime = System.currentTimeMillis();
        if (currentReloading && inCriticalPhase && !isManualReload) {
            long reloadStartTime = nbtCompound.getLong("ReloadStartTime");
            if (reloadStartTime == 0) {
                nbtCompound.putLong("ReloadStartTime", currentTime);
                reloadStartTime = currentTime;
            }
            if (currentTime - reloadStartTime > 10000) {
                cleanupReloadState(nbtCompound);
                nbtCompound.remove("InCriticalReloadPhase");
                nbtCompound.remove("ReloadStartTime");
                nbtCompound.remove("ReloadAnimationStarted");
                nbtCompound.remove("ReloadAnimationRestarted");
                nbtCompound.remove("ReloadCompleted");
                if (entity instanceof Player) {
                    ModSyncedDataKeys.RELOADING.setValue((Player)entity, false);
                }
                return;
            }
        } else {
            nbtCompound.remove("ReloadStartTime");
        }

        if (!serverReloading && inCriticalPhase && !isManualReload) {
            cleanupReloadState(nbtCompound);
            nbtCompound.remove("InCriticalReloadPhase");
            nbtCompound.remove("ReloadStartTime");
            nbtCompound.remove("ReloadAnimationStarted");
            nbtCompound.remove("ReloadAnimationRestarted");
            nbtCompound.remove("ReloadCompleted");
            return;
        }

        // For non-manual reloads, prevent state corruption
        if (!isManualReload) {
            // Clear any manual reload states that shouldn't exist
            if (currentReloadState.equals("STOPPING") || currentReloadState.equals("LOADING") || currentReloadState.equals("STARTING")) {
                nbtCompound.remove(RELOAD_STATE);
            }

            if (serverReloading && !currentReloading) {
                nbtCompound.putBoolean("scguns:IsReloading", true);
                nbtCompound.remove(RELOAD_STATE);
                nbtCompound.putLong("ReloadStartTime", currentTime);
            } else if (!serverReloading && currentReloading) {
                nbtCompound.remove("scguns:IsReloading");
                cleanupReloadState(nbtCompound);
                nbtCompound.remove("InCriticalReloadPhase");
                nbtCompound.remove("ReloadStartTime");
                nbtCompound.remove("ReloadAnimationStarted");
                nbtCompound.remove("ReloadAnimationRestarted");
                nbtCompound.remove("ReloadCompleted");
            }
            return;
        }
        boolean isInTransitionState = currentReloadState.equals("STOPPING") ||
                nbtCompound.getBoolean("scguns:IsPlayingReloadStop");

        boolean playerTryingToAim = false;
        if (entity instanceof Player) {
            playerTryingToAim = KeyBinds.getAimMapping().isDown();
        }

        if (serverReloading && !currentReloading && !isInTransitionState) {
            nbtCompound.putBoolean("scguns:IsReloading", true);
            if (!nbtCompound.contains(RELOAD_STATE)) {
                nbtCompound.putString(RELOAD_STATE, ReloadState.NONE.name());
            }
        } else if (!serverReloading && currentReloading && !isInTransitionState) {
            nbtCompound.putString(RELOAD_STATE, ReloadState.STOPPING.name());
            nbtCompound.putBoolean("scguns:IsPlayingReloadStop", true);
        } else if (!serverReloading && isInTransitionState && !playerTryingToAim) {
            long lastCleanup = nbtCompound.getLong("LastCleanupTime");

            if (currentTime - lastCleanup > 50) {
                nbtCompound.putLong("LastCleanupTime", currentTime);
                finishReloadTransition(nbtCompound);
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handleAnimationControllerUpdates(ItemStack stack, Entity entity, CompoundTag nbtCompound) {
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

        boolean inCriticalPhase = nbtCompound.getBoolean("InCriticalReloadPhase");
        boolean isReloading = nbtCompound.getBoolean("scguns:IsReloading");

        if (animationController != null && !isReloading && !inCriticalPhase
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
    }

    @OnlyIn(Dist.CLIENT)
    private void handlePlayerSpecificLogic(ItemStack stack, Player player, CompoundTag nbtCompound) {
        long id = GeoItem.getId(stack);
        AnimationController<GeoAnimatable> animationController = this.getAnimatableInstanceCache()
                .getManagerForId(id)
                .getAnimationControllers()
                .get("controller");

        if (GeoItem.getId(player.getMainHandItem()) != id) {
            handleItemNotHeld(nbtCompound, animationController, stack, player);
            return;
        }

        handleDrawingState(nbtCompound);

        handleActionStates(nbtCompound, animationController, stack, player);

        handlePlayerStateUpdates(nbtCompound, player, stack);

        handleInitializationAndAnimations(nbtCompound, animationController, stack, player);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleItemNotHeld(CompoundTag nbtCompound, AnimationController<GeoAnimatable> animationController,
                                   ItemStack stack, Player player) {
        if (nbtCompound.getBoolean("IsDrawn")) {
            nbtCompound.remove("IsDrawn");
            nbtCompound.remove("DrawnTick");
            this.drawTick = 0;

            cleanupAllReloadTags(nbtCompound);
            ModSyncedDataKeys.RELOADING.setValue(player, false);

            nbtCompound.remove("IsShooting");
            nbtCompound.remove("IsInspecting");
            nbtCompound.remove("IsAiming");
            nbtCompound.remove("IsRunning");
            nbtCompound.remove("loaded");
            nbtCompound.remove("DrawnTick");
        }

        animationController.forceAnimationReset();
        if (isInCarbineMode(stack)) {
            animationController.tryTriggerAnimation("carbine_idle");
        } else {
            animationController.tryTriggerAnimation("idle");
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleDrawingState(CompoundTag nbtCompound) {
        this.updateBooleanTag(nbtCompound, "IsDrawing", nbtCompound.getBoolean("IsDrawn"));
        if (nbtCompound.getBoolean("IsDrawing") && nbtCompound.getInt("DrawnTick") < 15) {
            this.drawTick++;
            nbtCompound.putInt("DrawnTick", this.drawTick);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleActionStates(CompoundTag nbtCompound, AnimationController<GeoAnimatable> animationController,
                                    ItemStack stack, Player player) {
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
    }
    @OnlyIn(Dist.CLIENT)
    private void handlePlayerStateUpdates(CompoundTag nbtCompound, Player player, ItemStack stack) {
        if (!nbtCompound.getBoolean("IsDrawn")) {
            nbtCompound.putBoolean("IsDrawn", true);
        }

        boolean isSprinting = player.isSprinting();
        boolean isAiming = ModSyncedDataKeys.AIMING.getValue(player);
        boolean serverReloading = ModSyncedDataKeys.RELOADING.getValue(player);
        boolean clientReloading = nbtCompound.getBoolean("scguns:IsReloading");

        if (serverReloading || clientReloading) {
            if (isAiming) {
                ModSyncedDataKeys.AIMING.setValue(player, false);
            }
            this.updateBooleanTag(nbtCompound, "IsAiming", false);
            this.updateBooleanTag(nbtCompound, "IsRunning", isSprinting);
            return;
        }

        if (isAiming && nbtCompound.getBoolean("IsDrawing") && nbtCompound.getInt("DrawnTick") < 15) {
            ModSyncedDataKeys.AIMING.setValue(player, false);
            isAiming = false;
        }

        this.updateBooleanTag(nbtCompound, "IsAiming", isAiming);
        this.updateBooleanTag(nbtCompound, "IsRunning", isSprinting);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleInitializationAndAnimations(CompoundTag nbtCompound, AnimationController<GeoAnimatable> animationController,
                                                   ItemStack stack, Player player) {

        boolean wasDrawn = nbtCompound.getBoolean("IsDrawn");
        boolean isFirstTick = !wasDrawn && !nbtCompound.getBoolean("_InitializedThisSession");
        if (isFirstTick) {
            handleFirstTickInitialization(nbtCompound, animationController, stack, player);
        }
        handleAnimationStateFixes(nbtCompound, animationController, stack, player);
        handleMainAnimationLogic(nbtCompound, animationController, stack, player);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleFirstTickInitialization(CompoundTag nbtCompound, AnimationController<GeoAnimatable> animationController,
                                               ItemStack stack, Player player) {
        boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);

        nbtCompound.putBoolean("_InitializedThisSession", true);
        nbtCompound.putBoolean("IsDrawn", true);
        nbtCompound.putInt("DrawnTick", 0);
        this.drawTick = 0;

        if (animationController != null) {
            boolean hasNoAnimation = animationController.getCurrentAnimation() == null;
            boolean isStopped = animationController.getAnimationState() == AnimationController.State.STOPPED;
            boolean isCurrentlyReloading = nbtCompound.getBoolean("scguns:IsReloading") ||
                    ModSyncedDataKeys.RELOADING.getValue(player);

            if ((hasNoAnimation || isStopped) &&
                    !isCurrentlyReloading &&
                    nbtCompound.getInt("DrawnTick") >= 15) {

                updateCarbineState(stack, animationController);
                if (isInCarbineMode(stack)) {
                    animationController.tryTriggerAnimation("carbine_idle");
                } else {
                    animationController.tryTriggerAnimation("idle");
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleAnimationStateFixes(CompoundTag nbtCompound, AnimationController<GeoAnimatable> animationController,
                                           ItemStack stack, Player player) {
        if (animationController != null) {
            boolean hasNoAnimation = animationController.getCurrentAnimation() == null;
            boolean isStopped = animationController.getAnimationState() == AnimationController.State.STOPPED;
            boolean serverReloading = ModSyncedDataKeys.RELOADING.getValue(player);

            if ((hasNoAnimation || isStopped) &&
                    !nbtCompound.getBoolean("scguns:IsReloading") &&
                    nbtCompound.getInt("DrawnTick") >= 15) {
                updateCarbineState(stack, animationController);
                if (isInCarbineMode(stack)) {
                    animationController.tryTriggerAnimation("carbine_idle");
                } else {
                    animationController.tryTriggerAnimation("idle");
                }
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handleMainAnimationLogic(CompoundTag nbtCompound, AnimationController<GeoAnimatable> animationController,
                                          ItemStack stack, Player player) {
        if (nbtCompound.getBoolean("IsDrawing") && nbtCompound.getInt("DrawnTick") < 15
                && Config.COMMON.gameplay.drawAnimation.get()) {
            this.handleDrawingState(nbtCompound, animationController, stack);
            return;
        }

        if (nbtCompound.getInt("DrawnTick") >= 15) {
            assert animationController != null;

            String currentAnim = animationController.getCurrentAnimation() != null ?
                    animationController.getCurrentAnimation().animation().name() : "none";

            boolean isReloading = nbtCompound.getBoolean("scguns:IsReloading") ||
                    ModSyncedDataKeys.RELOADING.getValue(player);
            boolean inCriticalPhase = nbtCompound.getBoolean("InCriticalReloadPhase");

            if (isReloading) {
                this.handleReloadingState(nbtCompound, animationController, stack);
                return;
            }

            if (inCriticalPhase) {
                return;
            }

            if (!isPlayingCriticalAnimations(animationController)) {
                if (nbtCompound.contains(RELOAD_STATE) &&
                        nbtCompound.getString(RELOAD_STATE).equals(ReloadState.STOPPING.name())) {
                    this.handleReloadingState(nbtCompound, animationController, stack);
                } else if (nbtCompound.getBoolean("IsAiming")) {
                    this.handleAimingState(nbtCompound, animationController);
                } else if (nbtCompound.getBoolean("IsRunning") &&
                        isPlayingInspectOrReloadAnimations(animationController)) {
                    this.handleRunningState(animationController);
                } else if (isPlayingInspectOrReloadAnimations(animationController)) {
                    if (isInCarbineMode(stack)) {
                        animationController.tryTriggerAnimation("carbine_idle");
                    } else {
                        animationController.tryTriggerAnimation("idle");
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isPlayingCriticalAnimations(AnimationController<GeoAnimatable> animationController) {
        return isAnimationPlaying(animationController, "draw") ||
                isAnimationPlaying(animationController, "carbine_draw") ||
                isAnimationPlaying(animationController, "jam") ||
                isAnimationPlaying(animationController, "melee") ||
                isAnimationPlaying(animationController, "bayonet") ||
                isAnimationPlaying(animationController, "shoot") ||
                isAnimationPlaying(animationController, "shoot1") ||
                isAnimationPlaying(animationController, "carbine_shoot") ||
                isAnimationPlaying(animationController, "aim_shoot") ||
                isAnimationPlaying(animationController, "aim_shoot1") ||
                isAnimationPlaying(animationController, "carbine_aim_shoot") ||
                isAnimationPlaying(animationController, "inspect") ||
                isAnimationPlaying(animationController, "carbine_inspect") ||
                isAnimationPlaying(animationController, "reload_stop") ||
                isAnimationPlaying(animationController, "carbine_reload_stop");
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isPlayingInspectOrReloadAnimations(AnimationController<GeoAnimatable> animationController) {
        return !isAnimationPlaying(animationController, "inspect") &&
                !isAnimationPlaying(animationController, "carbine_inspect") &&
                !isAnimationPlaying(animationController, "reload") &&
                !isAnimationPlaying(animationController, "carbine_reload") &&
                !isAnimationPlaying(animationController, "reload_alt") &&
                !isAnimationPlaying(animationController, "carbine_reload_loop") &&
                !isAnimationPlaying(animationController, "reload_loop");
    }

    @OnlyIn(Dist.CLIENT)
    private void finishReloadTransition(CompoundTag nbt) {
        nbt.remove("scguns:IsReloading");
        nbt.remove("scguns:IsPlayingReloadStop");
        nbt.remove("scguns:ReloadComplete");

        String currentState = nbt.getString(RELOAD_STATE);
        if (currentState.equals("STOPPING")) {
            cleanupReloadState(nbt);
        }
    }
    private void cleanupAllReloadTags(CompoundTag nbt) {
        cleanupReloadState(nbt);
        nbt.remove("IsMagReload");
        nbt.remove("IsManualReload");
        nbt.remove("shouldStopOnLoopEnd");
        nbt.remove("shouldTransitionToStop");
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
        Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean serverReloading = ModSyncedDataKeys.RELOADING.getValue(player);
        String currentAnim = animationController.getCurrentAnimation() != null ?
                animationController.getCurrentAnimation().animation().name() : "none";
        AnimationController.State animState = animationController.getAnimationState();

        if (!serverReloading) {
            cleanupReloadState(nbt);
            nbt.remove("InCriticalReloadPhase");
            nbt.remove("ReloadAnimationStarted");
            nbt.remove("ReloadAnimationRestarted");
            nbt.remove("ReloadCompleted");
            return;
        }

        double reloadSpeedMultiplier = 1.0;
        AttributeInstance reloadSpeedAttribute = player.getAttribute(SCAttributes.RELOAD_SPEED.get());
        if (reloadSpeedAttribute != null) {
            reloadSpeedMultiplier = reloadSpeedAttribute.getValue();
        }

        int actualReloadTime = (int) Math.ceil(GunEnchantmentHelper.getRealReloadSpeed(stack) / reloadSpeedMultiplier);
        float speedMultiplier = (float) modifiedGun.getReloads().getReloadTimer() / actualReloadTime;

        animationController.setAnimationSpeed(speedMultiplier);

        boolean isCarbine = isInCarbineMode(stack);
        String reloadAnim = isCarbine ? "carbine_reload" : "reload";

        if (nbt.getBoolean("ReloadCompleted")) {
            return;
        }

        boolean hasStartedReload = nbt.getBoolean("ReloadAnimationStarted");
        if (!isAnimationPlaying(animationController, reloadAnim) && !hasStartedReload) {
            if (!isAnimationPlaying(animationController, "draw") &&
                    !isAnimationPlaying(animationController, "carbine_draw")) {
                animationController.forceAnimationReset();
                animationController.tryTriggerAnimation(reloadAnim);
                nbt.putBoolean("ReloadAnimationStarted", true);
                return;
            }
        }

        if (!isAnimationPlaying(animationController, reloadAnim) && hasStartedReload &&
                animState == AnimationController.State.STOPPED &&
                !nbt.getBoolean("ReloadAnimationRestarted")) {

            animationController.forceAnimationReset();
            animationController.tryTriggerAnimation(reloadAnim);
            nbt.putBoolean("ReloadAnimationRestarted", true);
            return;
        }

        if (animationController.getAnimationState() == AnimationController.State.STOPPED &&
                hasStartedReload &&
                !isAnimationPlaying(animationController, reloadAnim) &&
                !isAnimationPlaying(animationController, "draw") &&
                !isAnimationPlaying(animationController, "carbine_draw")) {


            if (modifiedGun.getReloads().getReloadType() == ReloadType.MAG_FED) {
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageGunLoaded());
            } else if (modifiedGun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) {
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handleReloadingState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        String currentState = nbt.getString(RELOAD_STATE);

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean serverReloading = ModSyncedDataKeys.RELOADING.getValue(player);
        boolean clientReloading = nbt.getBoolean("scguns:IsReloading");

        // For non-manual reloads, use simple logic
        if (modifiedGun.getReloads().getReloadType() != ReloadType.MANUAL) {
            handleNormalReload(nbt, animationController, stack);
            return;
        }

        // Manual reload handling with state machine
        if (!serverReloading && clientReloading && !currentState.equals("STOPPING")) {
            nbt.putString(RELOAD_STATE, ReloadState.STOPPING.name());
            nbt.putBoolean("scguns:IsPlayingReloadStop", true);
            nbt.remove("scguns:IsReloading");
            animationController.stop();
            animationController.setAnimationSpeed(1.0);
            animationController.tryTriggerAnimation(isInCarbineMode(stack) ? "carbine_reload_stop" : "reload_stop");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long lastStateChange = nbt.getLong("LastReloadStateChange");
        if (currentTime - lastStateChange < 0) {
            return;
        }

        double reloadSpeedMultiplier = 1.0;
        if (Minecraft.getInstance().player != null) {
            AttributeInstance reloadSpeedAttribute = Minecraft.getInstance().player.getAttribute(SCAttributes.RELOAD_SPEED.get());
            if (reloadSpeedAttribute != null) {
                reloadSpeedMultiplier = reloadSpeedAttribute.getValue();
            }
        }
        int actualReloadTime = (int) Math.ceil(GunEnchantmentHelper.getRealReloadSpeed(stack) / reloadSpeedMultiplier);
        float speedMultiplier = (float) modifiedGun.getReloads().getReloadTimer() / actualReloadTime;

        CompoundTag tag = stack.getOrCreateTag();
        int currentAmmo = tag.getInt("AmmoCount");
        int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(stack, modifiedGun);
        boolean ammoFull = currentAmmo >= maxAmmo;
        boolean hasNoAmmo = Gun.findAmmo(player, modifiedGun.getProjectile().getItem()).stack().isEmpty();

        if (currentState.isEmpty() || currentState.equals("NONE")) {
            currentState = ReloadState.NONE.name();
            nbt.putString(RELOAD_STATE, currentState);
        }

        ReloadState state;
        try {
            state = ReloadState.valueOf(currentState);
        } catch (IllegalArgumentException e) {
            state = ReloadState.NONE;
            nbt.putString(RELOAD_STATE, ReloadState.NONE.name());
        }
        switch (state) {
            case NONE:
                nbt.putString(RELOAD_STATE, ReloadState.STARTING.name());
                nbt.putLong("LastReloadStateChange", currentTime);
                nbt.putBoolean("ManualReloadInitialized", true);
                animationController.setAnimationSpeed(speedMultiplier);
                animationController.tryTriggerAnimation(isInCarbineMode(stack) ? "carbine_reload_start" : "reload_start");
                break;

            case STARTING:
                boolean isStartAnimPlaying = isAnimationPlaying(animationController, "reload_start") ||
                        isAnimationPlaying(animationController, "carbine_reload_start");

                if (!isStartAnimPlaying && animationController.getAnimationState() == AnimationController.State.STOPPED) {
                    if (ammoFull || hasNoAmmo || !ModSyncedDataKeys.RELOADING.getValue(player)) {
                        nbt.putString(RELOAD_STATE, ReloadState.STOPPING.name());
                        nbt.putBoolean("scguns:IsPlayingReloadStop", true);
                        animationController.setAnimationSpeed(1.0);
                        animationController.tryTriggerAnimation(isInCarbineMode(stack) ? "carbine_reload_stop" : "reload_stop");
                    } else {
                        nbt.putString(RELOAD_STATE, ReloadState.LOADING.name());
                        nbt.putLong("LastReloadStateChange", currentTime);
                        nbt.putBoolean("InReloadLoop", true);
                        animationController.setAnimationSpeed(speedMultiplier);
                        animationController.tryTriggerAnimation(isInCarbineMode(stack) ? "carbine_reload_loop" : "reload_loop");
                    }
                }
                break;

            case LOADING:
                if (ammoFull || hasNoAmmo || !ModSyncedDataKeys.RELOADING.getValue(player)) {
                    nbt.putString(RELOAD_STATE, ReloadState.STOPPING.name());
                    nbt.putBoolean("scguns:IsPlayingReloadStop", true);
                    nbt.remove("InReloadLoop");
                    animationController.stop();
                    animationController.setAnimationSpeed(1.0);
                    animationController.tryTriggerAnimation(isInCarbineMode(stack) ? "carbine_reload_stop" : "reload_stop");
                } else {
                    boolean isLoopPlaying = isAnimationPlaying(animationController, "reload_loop") ||
                            isAnimationPlaying(animationController, "carbine_reload_loop");

                    if (!isLoopPlaying && animationController.getAnimationState() == AnimationController.State.STOPPED) {
                        animationController.setAnimationSpeed(speedMultiplier);
                        animationController.tryTriggerAnimation(isInCarbineMode(stack) ? "carbine_reload_loop" : "reload_loop");
                    }
                }
                break;

            case STOPPING:
                if (!isAnimationPlaying(animationController, "reload_stop") &&
                        !isAnimationPlaying(animationController, "carbine_reload_stop")) {

                    if (animationController.getAnimationState() == AnimationController.State.STOPPED) {
                        cleanupReloadState(nbt);
                        nbt.remove("LastReloadStateChange");
                        nbt.remove("ManualReloadInitialized");
                        nbt.remove("InReloadLoop");

                        if (isInCarbineMode(stack)) {
                            animationController.tryTriggerAnimation("carbine_idle");
                        } else {
                            animationController.tryTriggerAnimation("idle");
                        }
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
        nbt.remove("LastReloadStateChange");
        nbt.remove("InCriticalReloadPhase");
        nbt.remove("ReloadAnimStartTime");
        nbt.remove("IsMagReload");
        nbt.remove("IsManualReload");
        // CRITICAL FIX: Remove animation tracking flags
        nbt.remove("ReloadAnimationStarted");
        nbt.remove("ReloadAnimationRestarted");
        nbt.remove("ReloadCompleted");
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
        isInCarbineMode(stack);
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
        if (player == null) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean isMainHand = mainHand.getItem() == this && !mainHand.getOrCreateTag().getBoolean("IsDroppedItem");
        boolean isOffHand = offHand.getItem() == this && !offHand.getOrCreateTag().getBoolean("IsDroppedItem");

        if (!isMainHand && !isOffHand) {
            return;
        }

        ItemStack heldStack = isMainHand ? mainHand : offHand;
        CompoundTag heldTag = heldStack.getOrCreateTag();

        if (!heldTag.getBoolean("IsDrawn")) {
            return;
        }

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
                break;
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
                break;
            case "bolt":
                player.playSound(ModSounds.BOLT.get(), 1.0F, 1.0F);
                break;
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void particleListener(ParticleKeyframeEvent<AnimatedGunItem> gunItemParticleKeyframeEvent) {
        Player player = ClientUtils.getClientPlayer();
        if (player == null) return;
        ItemStack currentItem = player.getMainHandItem();
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean isMainHand = mainHand.getItem() == this && !mainHand.getOrCreateTag().getBoolean("IsDroppedItem");
        boolean isOffHand = offHand.getItem() == this && !offHand.getOrCreateTag().getBoolean("IsDroppedItem");

        if (!isMainHand && !isOffHand) {
            return;
        }

        ItemStack heldStack = isMainHand ? mainHand : offHand;
        CompoundTag heldTag = heldStack.getOrCreateTag();

        if (!heldTag.getBoolean("IsDrawn")) {
            return;
        }

        GunItem gunItem = (GunItem) heldStack.getItem();
        Gun gun = gunItem.getModifiedGun(heldStack);
        CompoundTag tag = heldStack.getOrCreateTag();

        String effect = gunItemParticleKeyframeEvent.getKeyframeData().getEffect();

        switch(effect) {
            case "reset_mag_position":
                tag.putFloat("MagazinePosition", 0.0f);
                tag.putBoolean("MagazineOverride", true);
                break;

            case "loaded":
                heldStack.getOrCreateTag();
                ModSyncedDataKeys.RELOADING.getValue(player);
                tag.putBoolean("loaded", true);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageGunLoaded());
                break;

            case "eject_casing":
                if (gun.getProjectile().ejectsCasing() && gun.getProjectile().ejectDuringReload()) {
                    if (Config.COMMON.gameplay.spawnCasings.get()) {
                        Level level = player.level();
                        GunEventBus.ejectCasing(level, player, false);
                        if (gun.getProjectile().casingType != null && !player.getAbilities().instabuild) {
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageEjectCasing());
                        }
                    }
                }
                break;
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
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return PlayState.STOP;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean isMainHand = mainHand.getItem() == this && !mainHand.getOrCreateTag().getBoolean("IsDroppedItem");
        boolean isOffHand = offHand.getItem() == this && !offHand.getOrCreateTag().getBoolean("IsDroppedItem");

        if (!isMainHand && !isOffHand) {
            return PlayState.STOP;
        }

        ItemStack heldStack = isMainHand ? mainHand : offHand;
        CompoundTag heldTag = heldStack.getOrCreateTag();

        if (!heldTag.getBoolean("IsDrawn")) {
            return PlayState.STOP;
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