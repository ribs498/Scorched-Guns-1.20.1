package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import top.ribs.scguns.Config;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.common.*;
import top.ribs.scguns.common.network.ServerPlayHandler;
import top.ribs.scguns.compat.PlayerReviveHelper;
import top.ribs.scguns.event.GunEventBus;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.BayonetItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedDualWieldGunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.*;
import top.ribs.scguns.util.GunCompositeStatHelper;

/**
 * Author: MrCrayfish
 */
public class ShootingHandler
{
    private static ShootingHandler instance;
    private int fireTimer;
    private int burstCooldownTimer;
    private boolean wasRightClickPressed = false;
    private boolean wasHoldingFireWhenEmpty = false;
    private boolean hasReleasedFireSinceEmpty = false;

    public static ShootingHandler get()
    {
        if(instance == null)
        {
            instance = new ShootingHandler();
        }
        return instance;
    }
    private boolean shooting;
    private boolean doEmptyClick;
    private int slot = -1;
    private int burstCounter = 0;

    private ShootingHandler() {
        fireTimer = 0;
    }

    private boolean isInGame()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.getOverlay() != null)
            return true;
        if(mc.screen != null)
            return true;
        if(!mc.mouseHandler.isMouseGrabbed())
            return true;
        return !mc.isWindowActive();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMouseClick(InputEvent.InteractionKeyMappingTriggered event) {
        if(event.isCanceled())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player == null)
            return;

        if(PlayerReviveHelper.isBleeding(player))
            return;

        if(event.isAttack()) {
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
        else if(event.isUseItem()) {
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem gunItem) {
                if(event.getHand() == InteractionHand.MAIN_HAND) {
                    Gun modifiedGun = gunItem.getModifiedGun(heldItem);
                    GripType gripType = modifiedGun.getGeneral().getGripType(heldItem);

                    if(gripType == GripType.ONE_HANDED && !player.getOffhandItem().isEmpty()) {
                        ItemStack offhandItem = player.getOffhandItem();
                        if(offhandItem.getItem() instanceof SwordItem || offhandItem.getItem() instanceof BayonetItem) {
                            boolean currentRightClick = mc.options.keyUse.isDown();

                            if(currentRightClick && !wasRightClickPressed) {
                                event.setCanceled(true);
                                event.setSwingHand(false);

                                if(mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
                                    EntityHitResult entityHit = (EntityHitResult) mc.hitResult;
                                    Entity target = entityHit.getEntity();
                                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageOffhandMelee(
                                            target.getId(),
                                            (float) entityHit.getLocation().x,
                                            (float) entityHit.getLocation().y,
                                            (float) entityHit.getLocation().z
                                    ));
                                } else {
                                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageOffhandMelee(-1, 0, 0, 0));
                                }
                            }
                            wasRightClickPressed = currentRightClick;
                            return;
                        }
                    }
                }

                if(event.getHand() == InteractionHand.OFF_HAND) {
                    Gun modifiedGun = gunItem.getModifiedGun(heldItem);
                    GripType gripType = modifiedGun.getGeneral().getGripType(heldItem);
                    if(gripType == GripType.ONE_HANDED) {
                        return;
                    }
                    if(player.getOffhandItem().getItem() == Items.SHIELD && player.isUsingItem() && player.getUsedItemHand() == InteractionHand.OFF_HAND) {
                        return;
                    }

                    event.setCanceled(true);
                    event.setSwingHand(false);
                    return;
                }

                if(AimingHandler.get().isZooming() && AimingHandler.get().isLookingAtInteractableBlock()) {
                    event.setCanceled(true);
                    event.setSwingHand(false);
                }
            }
        }
    }

    @SubscribeEvent
    public void onHandleShooting(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        if (this.isInGame())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof GunItem gunItem && !PlayerReviveHelper.isBleeding(player)) {
                Gun modifiedGun = gunItem.getModifiedGun(heldItem);

                boolean shouldShoot = KeyBinds.getShootMapping().isDown() || (burstCounter > 0 && Gun.hasBurstFire(heldItem));
                if (ScorchedGuns.controllableLoaded) {
                    shouldShoot |= ControllerHandler.isShooting();
                }

                if (modifiedGun.getGeneral().getFireMode() == FireMode.BEAM || modifiedGun.getGeneral().getFireMode() == FireMode.SEMI_BEAM) {
                    if (shouldShoot && burstCooldownTimer <= 0) {
                        if (!this.shooting) {
                            this.shooting = true;
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(true));
                        }
                    } else {
                        if (this.shooting) {
                            this.shooting = false;
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageStopBeam());
                        }
                    }
                } else {
                    if (shouldShoot && burstCooldownTimer <= 0) {
                        if (!this.shooting) {
                            this.shooting = true;
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(true));
                        }
                    } else if (this.shooting) {
                        this.shooting = false;
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
                    }
                }
            } else if (this.shooting) {
                this.shooting = false;
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
                ItemStack lastHeldItem = player.getMainHandItem();
                if (lastHeldItem.getItem() instanceof GunItem) {
                    Gun modifiedGun = ((GunItem) lastHeldItem.getItem()).getModifiedGun(lastHeldItem);
                    if (modifiedGun != null && (modifiedGun.getGeneral().getFireMode() == FireMode.BEAM ||
                            modifiedGun.getGeneral().getFireMode() == FireMode.SEMI_BEAM)) {
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageStopBeam());
                    }
                }
            }
        } else {
            this.shooting = false;
        }
    }

    private boolean isEmpty(Player player, ItemStack heldItem)
    {
        if(!(heldItem.getItem() instanceof GunItem))
            return false;

        if(player.isSpectator())
            return false;

        return (!Gun.hasAmmo(heldItem) || !Gun.canShoot(heldItem)) && !player.isCreative();
    }
    public void fire(Player player, ItemStack heldItem) {
        if(!(heldItem.getItem() instanceof GunItem gunItem))
            return;
        if (heldItem.isDamageableItem() &&
                heldItem.getDamageValue() >= (heldItem.getMaxDamage() - 1)) {
            return;
        }

        Gun gun = gunItem.getModifiedGun(heldItem);
        boolean isPulse = gun.getGeneral().getFireMode() == FireMode.PULSE;

        if(isEmpty(player, heldItem)) {
            ItemCooldowns tracker = player.getCooldowns();
            if(!tracker.isOnCooldown(heldItem.getItem())) {

                if (doEmptyClick && heldItem.getItem() instanceof GunItem && canUseTrigger(player, heldItem)) {
                    doEmptyClick = false;

                    boolean isCurrentlyHoldingFire = KeyBinds.getShootMapping().isDown();

                    if (isPulse) {
                        ChargeHandler.updateChargeTime(player, heldItem, false);
                        fireTimer = 0;
                    }

                    if (Config.COMMON.gameplay.enableAutoReload.get()) {

                        boolean isAutomaticWeapon = gun.getGeneral().getFireMode() == FireMode.AUTOMATIC;

                        if (isAutomaticWeapon) {
                            if (isCurrentlyHoldingFire && !wasHoldingFireWhenEmpty) {
                                wasHoldingFireWhenEmpty = true;
                                return;
                            }
                            if (wasHoldingFireWhenEmpty && !hasReleasedFireSinceEmpty) {
                               return;
                            }
                        }

                        boolean hasAmmoAvailable;
                        if (gun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) {
                            hasAmmoAvailable = !Gun.findAmmo(player, gun.getReloads().getReloadItem()).stack().isEmpty();
                        } else {
                            hasAmmoAvailable = !Gun.findAmmo(player, gun.getProjectile().getItem()).stack().isEmpty();
                        }
                        boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);
                        int currentAmmo = Gun.getAmmoCount(heldItem);
                        int maxAmmo = gun.getReloads().getMaxAmmo();

                        if (hasAmmoAvailable && !isReloading && currentAmmo < maxAmmo) {

                            if (heldItem.getItem() instanceof AnimatedGunItem animatedGun) {
                                CompoundTag tag = heldItem.getOrCreateTag();
                                String reloadState = tag.getString("scguns:ReloadState");
                                boolean isPlayingReloadStop = tag.getBoolean("scguns:IsPlayingReloadStop");

                                if (reloadState.equals("STOPPING") || isPlayingReloadStop) {
                                    tag.remove("scguns:ReloadState");
                                    tag.remove("scguns:IsPlayingReloadStop");
                                    tag.remove("scguns:IsReloading");
                                    tag.remove("IsReloading");

                                    long id = GeoItem.getId(heldItem);
                                    AnimationController<GeoAnimatable> controller = animatedGun.getAnimatableInstanceCache()
                                            .getManagerForId(id)
                                            .getAnimationControllers()
                                            .get("controller");

                                    if (controller != null) {
                                        controller.forceAnimationReset();
                                        if (animatedGun.isInCarbineMode(heldItem)) {
                                            controller.tryTriggerAnimation("carbine_idle");
                                        } else {
                                            controller.tryTriggerAnimation("idle");
                                        }
                                    }
                                }
                            }

                            boolean canAutoReload = isCanAutoReload(heldItem);

                            if (canAutoReload) {
                                ReloadHandler.get().setReloading(true);
                                wasHoldingFireWhenEmpty = false;
                                hasReleasedFireSinceEmpty = false;
                            }
                        }
                    }
                }
            }
            burstCounter = 0;
            return;
        }
        wasHoldingFireWhenEmpty = false;
        hasReleasedFireSinceEmpty = false;

        if(player.isSprinting())
            player.setSprinting(false);

        if(!canFire(player, heldItem))
            return;

        ItemCooldowns tracker = player.getCooldowns();
        if(!tracker.isOnCooldown(heldItem.getItem())) {
            if(MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(player, heldItem)))
                return;
            if (gunItem instanceof AnimatedGunItem animatedGunItem) {
                long id = GeoItem.getId(heldItem);
                AnimationController<GeoAnimatable> controller = animatedGunItem.getAnimatableInstanceCache()
                        .getManagerForId(id)
                        .getAnimationControllers()
                        .get("controller");

                controller.forceAnimationReset();

                if (gunItem instanceof AnimatedDualWieldGunItem) {
                    boolean useAlternate = ServerPlayHandler.RatKingAndQueenModel.GunFireEventRatHandler.shouldUseAlternateAnimation();
                    if (ModSyncedDataKeys.AIMING.getValue(player)) {
                        controller.tryTriggerAnimation(useAlternate ? "aim_shoot1" : "aim_shoot");
                    } else {
                        controller.tryTriggerAnimation(useAlternate ? "shoot1" : "shoot");
                    }
                    ServerPlayHandler.RatKingAndQueenModel.GunFireEventRatHandler.incrementShotCount();
                } else {
                    boolean isCarbine = animatedGunItem.isInCarbineMode(heldItem);
                    if (ModSyncedDataKeys.AIMING.getValue(player)) {
                        controller.tryTriggerAnimation(isCarbine ? "carbine_aim_shoot" : "aim_shoot");
                    } else {
                        controller.tryTriggerAnimation(isCarbine ? "carbine_shoot" : "shoot");
                    }
                }
            }

            int rate = GunCompositeStatHelper.getCompositeRate(heldItem, gun, player);
            tracker.addCooldown(heldItem.getItem(), rate);

            if (Gun.hasBurstFire(heldItem)) {
                if (burstCounter == 0) {
                    burstCounter = Gun.getBurstCount(heldItem);
                }
                burstCounter--;

                if (burstCounter == 0) {
                    burstCooldownTimer = Gun.getBurstCooldown(heldItem);
                }
            }

            PacketHandler.getPlayChannel().sendToServer(new C2SMessageShoot(player));
            MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(player, heldItem));
        }
    }
    @SubscribeEvent
    public void onPostClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        if (isInGame())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null) {
            boolean currentRightClick = mc.options.keyUse.isDown();
            if (!currentRightClick) {
                wasRightClickPressed = false; // Reset when key is released
            }
            if (PlayerReviveHelper.isBleeding(player))
                return;

            if (!isSameWeapon(player)) {
                ModSyncedDataKeys.BURSTCOUNT.setValue(player, 0);
                if (player.getMainHandItem().getItem() instanceof GunItem) {
                    burstCounter = 0;
                    burstCooldownTimer = 0;
                    fireTimer = 0;
                    // Reset flags when switching weapons
                    wasHoldingFireWhenEmpty = false;
                    hasReleasedFireSinceEmpty = false;
                    // FIXED: Clear charge state when switching weapons
                    ItemStack heldItem = player.getMainHandItem();
                    ChargeHandler.clearLastChargeProgress(player.getUUID());
                }
            }
            if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                burstCounter = 0;
                burstCooldownTimer = 0;
            }
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof GunItem) {
                Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
                int maxChargeTime = gun.getGeneral().getFireTimer();
                if (burstCooldownTimer > 0) {
                    burstCooldownTimer--;
                }
                boolean isHoldingFire = KeyBinds.getShootMapping().isDown();

                if (!isHoldingFire && wasHoldingFireWhenEmpty) {
                    hasReleasedFireSinceEmpty = true;
                }

                if (gun.getGeneral().getFireMode() == FireMode.PULSE) {
                    if (isHoldingFire) {
                        // FIXED: Always try to fire when holding fire, even if empty (for auto-reload)
                        if (Gun.hasAmmo(heldItem) || player.isCreative()) {
                            int preSoundThreshold = maxChargeTime / 3;
                            if (fireTimer == preSoundThreshold) {
                                PacketHandler.getPlayChannel().sendToServer(new C2SMessagePreFireSound(player));
                            }
                            fireTimer = Math.min(fireTimer + 1, maxChargeTime);
                            ChargeHandler.updateChargeTime(player, heldItem, true);
                        } else {
                            doEmptyClick = true;
                            this.fire(player, heldItem);
                            fireTimer = 0;
                            ChargeHandler.updateChargeTime(player, heldItem, false);
                        }
                    } else if (fireTimer > 0) {
                        this.fire(player, heldItem);
                        fireTimer = 0;
                        ChargeHandler.updateChargeTime(player, heldItem, false);
                    }
                } else {
                    if (!isHoldingFire && maxChargeTime != 0) {
                        fireTimer = maxChargeTime;
                        if (wasHoldingFireWhenEmpty) {
                            wasHoldingFireWhenEmpty = false;
                        }
                        ChargeHandler.updateChargeTime(player, heldItem, false);
                    }
                    if ((isHoldingFire || burstCounter > 0) && burstCooldownTimer <= 0) {
                        if (maxChargeTime != 0) {
                            ItemCooldowns tracker = player.getCooldowns();
                            if (!tracker.isOnCooldown(heldItem.getItem())) {
                                if (fireTimer == maxChargeTime - 2) {
                                    PacketHandler.getPlayChannel().sendToServer(new C2SMessagePreFireSound(player));
                                }
                                fireTimer--;
                            } else {
                                this.fire(player, heldItem);
                                if (gun.getGeneral().getFireMode() == FireMode.SEMI_AUTO || gun.getGeneral().getFireMode() == FireMode.SEMI_BEAM) {
                                    mc.options.keyAttack.setDown(false);
                                    fireTimer = maxChargeTime;
                                    ChargeHandler.updateChargeTime(player, heldItem, false);
                                }
                            }
                        } else {
                            this.fire(player, heldItem);
                            if (gun.getGeneral().getFireMode() == FireMode.SEMI_AUTO || gun.getGeneral().getFireMode() == FireMode.SEMI_BEAM) {
                                mc.options.keyAttack.setDown(false);
                            }
                        }
                        ChargeHandler.updateChargeTime(player, heldItem, true);
                    } else {
                        ChargeHandler.updateChargeTime(player, heldItem, false);
                        doEmptyClick = true;
                     }
                }
            }
            slot = player.getInventory().selected;
        }
    }

    private static boolean isCanAutoReload(ItemStack heldItem) {
        if (!(heldItem.getItem() instanceof AnimatedGunItem)) {
            return true;
        }
        CompoundTag tag = heldItem.getOrCreateTag();

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            boolean syncedReloading = ModSyncedDataKeys.RELOADING.getValue(player);
            if (syncedReloading) {
                return false;
            }
        }
        String reloadState = tag.getString("scguns:ReloadState");
        if (!reloadState.isEmpty() &&
                (reloadState.equals("LOADING") || reloadState.equals("STOPPING"))) {
            return false;
        }

        if (tag.getBoolean("scguns:IsPlayingReloadStop")) {
            return false;
        }

        if (heldItem.getItem() instanceof AnimatedGunItem animatedGun) {
            long id = GeoItem.getId(heldItem);
            AnimationController<GeoAnimatable> controller = animatedGun.getAnimatableInstanceCache()
                    .getManagerForId(id)
                    .getAnimationControllers()
                    .get("controller");

            if (controller != null) {
                String currentAnim = controller.getCurrentAnimation() != null ?
                        controller.getCurrentAnimation().animation().name() : "none";

                boolean playingReload = animatedGun.isAnimationPlaying(controller, "reload");
                boolean playingCarbineReload = animatedGun.isAnimationPlaying(controller, "carbine_reload");
                boolean playingReloadStart = animatedGun.isAnimationPlaying(controller, "reload_start");
                boolean playingCarbineReloadStart = animatedGun.isAnimationPlaying(controller, "carbine_reload_start");
                boolean playingReloadLoop = animatedGun.isAnimationPlaying(controller, "reload_loop");
                boolean playingCarbineReloadLoop = animatedGun.isAnimationPlaying(controller, "carbine_reload_loop");
                boolean playingReloadStop = animatedGun.isAnimationPlaying(controller, "reload_stop");
                boolean playingCarbineReloadStop = animatedGun.isAnimationPlaying(controller, "carbine_reload_stop");

                return !playingReload && !playingCarbineReload && !playingReloadStart && !playingCarbineReloadStart &&
                        !playingReloadLoop && !playingCarbineReloadLoop && !playingReloadStop && !playingCarbineReloadStop;
            }
        }
        return true;
    }
    private boolean canFire(Player player, ItemStack heldItem) {
        if (player.isSpectator())
            return false;

        if (player.isCreative())
            return true;

        if (!Gun.hasAmmo(heldItem))
            return false;

        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
        if (gun.getGeneral().getFireMode() == FireMode.PULSE) {
            float chargeProgress = ChargeHandler.getChargeProgress(player, heldItem);
            return chargeProgress > 0;
        }

        return Gun.canShoot(heldItem);
    }

    private boolean canUseTrigger(Player player, ItemStack heldItem) {
        if (player.isSpectator())
            return false;

        if (player.isCreative())
            return true;

        return Gun.canShoot(heldItem) || !Gun.hasAmmo(heldItem);
    }

    private boolean isSameWeapon(Player player)
    {
        if (slot == -1)
            return true;
        return player.getInventory().selected == slot;
    }

    public boolean isShooting() {
        return shooting;
    }
}