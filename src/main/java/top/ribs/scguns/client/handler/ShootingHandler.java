package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.common.ChargeHandler;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.network.ServerPlayHandler;
import top.ribs.scguns.compat.PlayerReviveHelper;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessagePreFireSound;
import top.ribs.scguns.network.message.C2SMessageShoot;
import top.ribs.scguns.network.message.C2SMessageShooting;
import top.ribs.scguns.network.message.C2SMessageStopBeam;
import top.ribs.scguns.util.GunCompositeStatHelper;

/**
 * Author: MrCrayfish
 */
public class ShootingHandler
{
    private static ShootingHandler instance;
    private int fireTimer;
    private int burstCooldownTimer;

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

    private ShootingHandler() {}

    private boolean isInGame()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.getOverlay() != null)
            return false;
        if(mc.screen != null)
            return false;
        if(!mc.mouseHandler.isMouseGrabbed())
            return false;
        return mc.isWindowActive();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMouseClick(InputEvent.InteractionKeyMappingTriggered event)
    {
        if(event.isCanceled())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player == null)
            return;

        if(PlayerReviveHelper.isBleeding(player))
            return;

        if(event.isAttack())
        {
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem gunItem)
            {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
        else if(event.isUseItem())
        {
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem gunItem)
            {
                if(event.getHand() == InteractionHand.OFF_HAND)
                {
                    if(player.getOffhandItem().getItem() == Items.SHIELD)
                    {
                        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
                        if(modifiedGun.getGeneral().getGripType(heldItem) == GripType.ONE_HANDED ||modifiedGun.getGeneral().getGripType(heldItem) == GripType.ONE_HANDED_2)
                        {
                            return;
                        }
                    }
                    event.setCanceled(true);
                    event.setSwingHand(false);
                    return;
                }
                if(AimingHandler.get().isZooming() && AimingHandler.get().isLookingAtInteractableBlock())
                {
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

        if (!this.isInGame())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof GunItem gunItem && !isEmpty(player, heldItem) && !PlayerReviveHelper.isBleeding(player)) {
                Gun modifiedGun = gunItem.getModifiedGun(heldItem);

                boolean shouldShoot = KeyBinds.getShootMapping().isDown() || (burstCounter > 0 && Gun.hasBurstFire(heldItem));
                if (ScorchedGuns.controllableLoaded) {
                    shouldShoot |= ControllerHandler.isShooting();
                }

                // Handle beam fire mode
                if (modifiedGun.getGeneral().getFireMode() == FireMode.BEAM) {
                    if (shouldShoot && burstCooldownTimer <= 0) {
                        if (!this.shooting) {
                            this.shooting = true;
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(true));
                        }
                    } else if (this.shooting) {
                        this.shooting = false;
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
                        // Send a stop beam message to the server
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageStopBeam());
                    }
                } else {  // Non-beam weapons
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
                // Send a stop beam message to the server if the player is no longer shooting
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageStopBeam());
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

    @SubscribeEvent
    public void onPostClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        if (!isInGame())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null) {
            if (PlayerReviveHelper.isBleeding(player))
                return;

            if (!isSameWeapon(player)) {
                ModSyncedDataKeys.BURSTCOUNT.setValue(player, 0);
                if (player.getMainHandItem().getItem() instanceof GunItem) {
                    burstCounter = 0;
                    burstCooldownTimer = 0;
                }
            }
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof GunItem) {
                Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
                int maxChargeTime = gun.getGeneral().getFireTimer();
                if (burstCooldownTimer > 0) {
                    burstCooldownTimer--;
                }
                if (!KeyBinds.getShootMapping().isDown() && maxChargeTime != 0) {
                    fireTimer = maxChargeTime;
                }
                if ((KeyBinds.getShootMapping().isDown() || burstCounter > 0) && burstCooldownTimer <= 0) {
                    if (maxChargeTime != 0) {
                        ItemCooldowns tracker = player.getCooldowns();
                        if (fireTimer > 0 && !tracker.isOnCooldown(heldItem.getItem())) {
                            if (fireTimer == maxChargeTime - 2) {
                                PacketHandler.getPlayChannel().sendToServer(new C2SMessagePreFireSound(player));
                            }
                            fireTimer--;

                        } else {
                            this.fire(player, heldItem);
                            if (gun.getGeneral().getFireMode() == FireMode.SEMI_AUTO || gun.getGeneral().getFireMode() == FireMode.PULSE) {
                                mc.options.keyAttack.setDown(false);
                                fireTimer = maxChargeTime;
                                ChargeHandler.updateChargeTime(maxChargeTime, false);
                            }
                        }
                    } else {
                        this.fire(player, heldItem);
                        if (gun.getGeneral().getFireMode() == FireMode.SEMI_AUTO) {
                            mc.options.keyAttack.setDown(false);
                        }
                    }
                    ChargeHandler.updateChargeTime(maxChargeTime, true);
                } else {
                    ChargeHandler.updateChargeTime(maxChargeTime, false);
                    doEmptyClick = true;
                }
            }
            slot = player.getInventory().selected;
        }
    }

    public void fire(Player player, ItemStack heldItem)
    {
        if(!(heldItem.getItem() instanceof GunItem))
            return;

        if(isEmpty(player, heldItem))
        {
            ItemCooldowns tracker = player.getCooldowns();
            if(!tracker.isOnCooldown(heldItem.getItem()))
            {
                if (doEmptyClick && heldItem.getItem() instanceof GunItem gunItem && canUseTrigger(player, heldItem))
                {
                   doEmptyClick = false;
                }
            }
            burstCounter = 0;
            return;
        }

        if(player.isSprinting())
            player.setSprinting(false);

        if(!canFire(player, heldItem))
            return;

        ItemCooldowns tracker = player.getCooldowns();
        if(!tracker.isOnCooldown(heldItem.getItem()))
        {
            GunItem gunItem = (GunItem) heldItem.getItem();
            Gun modifiedGun = gunItem.getModifiedGun(heldItem);

            if(MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(player, heldItem)))
                return;

            int rate = GunCompositeStatHelper.getCompositeRate(heldItem, modifiedGun, player);
            tracker.addCooldown(heldItem.getItem(), rate);

            if (Gun.hasBurstFire(heldItem))
            {
                if (burstCounter == 0)
                {
                    burstCounter = Gun.getBurstCount(heldItem);
                }
                burstCounter--;

                if (burstCounter == 0)
                {
                    burstCooldownTimer = Gun.getBurstCooldown(heldItem);
                }
            }

            PacketHandler.getPlayChannel().sendToServer(new C2SMessageShoot(player));

            MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(player, heldItem));
        }
    }

    private boolean canUseTrigger(Player player, ItemStack heldItem) {
        if (player.isSpectator())
            return false;

        if (player.isCreative())
            return true;

        if (!Gun.hasAmmo(heldItem))
            return false;

        return Gun.canShoot(heldItem);
    }

    private boolean canFire(Player player, ItemStack heldItem) {
        if (player.isSpectator())
            return false;

        if (player.isCreative())
            return true;

        if (!Gun.hasAmmo(heldItem))
            return false;

        return Gun.canShoot(heldItem);
    }


    private boolean isSameWeapon(Player player)
    {
        if (slot == -1)
            return true;
        return player.getInventory().selected == slot;
    }
}