package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
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
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.compat.PlayerReviveHelper;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessagePreFireSound;
import top.ribs.scguns.network.message.C2SMessageShoot;
import top.ribs.scguns.network.message.C2SMessageShooting;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

/**
 * Author: MrCrayfish
 */
public class ShootingHandler
{
    private static ShootingHandler instance;
    private int fireTimer;

    public static ShootingHandler get()
    {
        if(instance == null)
        {
            instance = new ShootingHandler();
        }
        return instance;
    }

    private boolean shooting;

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
                    // Allow shields to be used if weapon is one-handed
                    if(player.getOffhandItem().getItem() == Items.SHIELD)
                    {
                        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
                        if(modifiedGun.getGeneral().getGripType() == GripType.ONE_HANDED || modifiedGun.getGeneral().getGripType() == GripType.ONE_HANDED_2 ||modifiedGun.getGeneral().getGripType() == GripType.TWO_HANDED_ONE_HANDED)
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
            if (heldItem.getItem() instanceof GunItem gunItem && (Gun.hasAmmo(heldItem) || player.isCreative()) && !PlayerReviveHelper.isBleeding(player)) {
                boolean shooting = KeyBinds.getShootMapping().isDown();
                if (ScorchedGuns.controllableLoaded) {
                    shooting |= ControllerHandler.isShooting();
                }
                if (shooting) {
                    if (!this.shooting) {
                        this.shooting = true;
                        Gun gun = gunItem.getModifiedGun(heldItem);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(true));

                    }
                } else if (this.shooting) {
                    this.shooting = false;
                    ChargeHandler.setChargeTime(0);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
                }
            } else if (this.shooting) {
                this.shooting = false;
                ChargeHandler.setChargeTime(0);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
            }
        } else {
            this.shooting = false;
        }
    }

    // Props to Moon-404 for the double-tap fix!
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

            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof GunItem) {
                Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
                int maxChargeTime = gun.getGeneral().getFireTimer();

                if (!KeyBinds.getShootMapping().isDown() && maxChargeTime != 0) {
                    fireTimer = maxChargeTime;
                }
                if (KeyBinds.getShootMapping().isDown()) {
                    if (maxChargeTime != 0) {
                        ItemCooldowns tracker = player.getCooldowns();
                        if (fireTimer > 0 && !tracker.isOnCooldown(heldItem.getItem())) {
                            if (fireTimer == maxChargeTime - 2) {
                                PacketHandler.getPlayChannel().sendToServer(new C2SMessagePreFireSound(player));
                            }
                            // If the player is in water, reduce the preFiring in half
                            if (player.isUnderWater()) {
                                fireTimer--;
                            }
                            fireTimer--;
                        } else {
                            // Execute after preFire timer ends
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
                    ChargeHandler.updateChargeTime(maxChargeTime, true); // Update charge time while holding down the shoot key
                } else {
                    ChargeHandler.updateChargeTime(maxChargeTime, false); // Reset charge time when not holding down the shoot key
                }
            }
        }
    }

    public void fire(Player player, ItemStack heldItem) {
        if (!(heldItem.getItem() instanceof GunItem))
            return;

        if (!Gun.hasAmmo(heldItem) && !player.isCreative())
            return;

        if (player.isSpectator())
            return;

        if (player.getUseItem().getItem() == Items.SHIELD)
            return;

        ItemCooldowns tracker = player.getCooldowns();
        int maxDamage = heldItem.getMaxDamage();
        int currentDamage = heldItem.getDamageValue();
        if (!tracker.isOnCooldown(heldItem.getItem())) {
            GunItem gunItem = (GunItem) heldItem.getItem();
            Gun modifiedGun = gunItem.getModifiedGun(heldItem);
            ItemStack stack = player.getMainHandItem();

            if (MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(player, heldItem)))
                return;

            if (stack.isDamageableItem() && currentDamage < (maxDamage - 1)) {
                int rate = GunEnchantmentHelper.getRate(heldItem, modifiedGun);
                rate = GunModifierHelper.getModifiedRate(heldItem, rate);
                tracker.addCooldown(heldItem.getItem(), rate);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageShoot(player));

                MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(player, heldItem));
            } else if (!stack.isDamageableItem()) {
                int rate = GunEnchantmentHelper.getRate(heldItem, modifiedGun);
                rate = GunModifierHelper.getModifiedRate(heldItem, rate);
                tracker.addCooldown(heldItem.getItem(), rate);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageShoot(player));

                MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(player, heldItem));
            }
        }
    }
}
