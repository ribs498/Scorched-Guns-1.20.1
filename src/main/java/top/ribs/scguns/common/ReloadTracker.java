package top.ribs.scguns.common;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.AmmoBoxItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageGunSound;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReloadTracker {
    private static final Map<Player, ReloadTracker> RELOAD_TRACKER_MAP = new WeakHashMap<>();

    private final int startTick;
    private final int slot;
    private final ItemStack stack;
    private final Gun gun;
    private int currentBulletReloadTick = 0;
    private ReloadTracker(Player player) {
        this.startTick = player.tickCount;
        this.slot = player.getInventory().selected;
        this.stack = player.getInventory().getSelected();
        this.gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
    }

    private boolean isSameWeapon(Player player) {
        return !this.stack.isEmpty() && player.getInventory().selected == this.slot && player.getInventory().getSelected() == this.stack;
    }

    private boolean isWeaponFull()
    {
        CompoundTag tag = this.stack.getOrCreateTag();
        return tag.getInt("AmmoCount") >= GunModifierHelper.getModifiedAmmoCapacity(this.stack, this.gun);
    }

    private boolean isWeaponEmpty() {
        CompoundTag tag = this.stack.getOrCreateTag();
        return tag.getInt("AmmoCount") == 0;
    }

    private boolean hasNoAmmo(Player player) {
        if (gun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) {
            return Gun.findAmmo(player, this.gun.getReloads().getReloadItem()).stack().isEmpty();
        }
        return Gun.findAmmo(player, this.gun.getProjectile().getItem()).stack().isEmpty();
    }

    private boolean canReload(Player player) {
        int deltaTicks = player.tickCount - this.startTick;
        if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
            if (currentBulletReloadTick <= 0) { // Check if it's time to reload the next bullet
                currentBulletReloadTick = GunEnchantmentHelper.getReloadInterval(this.stack); // Set reload time for one bullet
                return true; // Allow reloading of the next bullet
            } else {
                currentBulletReloadTick--; // Decrement the reload tick for continuous reloading
                return false; // Not yet time to reload the next bullet
            }
        } else {
            int interval = (gun.getReloads().getReloadType() == ReloadType.MAG_FED) ?
                    GunEnchantmentHelper.getMagReloadSpeed(this.stack) :
                    GunEnchantmentHelper.getReloadInterval(this.stack);
            return deltaTicks >= interval;
        }
    }
    public static int ammoInInventory(ItemStack[] ammoStack) {
        int result = 0;
        for (ItemStack x : ammoStack) {
            result += x.getCount();
        }
        return result;
    }
    private void shrinkFromAmmoPool(ItemStack[] ammoStack, Player player, int shrinkAmount) {
        int shrinkAmt = shrinkAmount;
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof AmmoBoxItem) {
                List<ItemStack> contents = AmmoBoxItem.getContents(itemStack).collect(Collectors.toList());
                for (ItemStack pouchAmmoStack : contents) {
                    if (!pouchAmmoStack.isEmpty() && pouchAmmoStack.getItem() == gun.getProjectile().getItem()) {
                        int max = Math.min(shrinkAmt, pouchAmmoStack.getCount());
                        pouchAmmoStack.shrink(max);
                        shrinkAmt -= max;
                        if (shrinkAmt == 0) {
                            updateAmmoPouchContents(itemStack, contents);
                            return;
                        }
                    }
                }
                updateAmmoPouchContents(itemStack, contents);
            }
        }
        for (ItemStack x : ammoStack) {
            if (!x.isEmpty()) {
                int max = Math.min(shrinkAmt, x.getCount());
                x.shrink(max);
                shrinkAmt -= max;
                if (shrinkAmt == 0) {
                    return;
                }
            }
        }
    }
    private void updateAmmoPouchContents(ItemStack ammoPouch, List<ItemStack> contents) {
        ListTag listTag = new ListTag();
        for (ItemStack stack : contents) {
            CompoundTag itemTag = new CompoundTag();
            stack.save(itemTag);
            listTag.add(itemTag);
        }
        ammoPouch.getOrCreateTag().put(AmmoBoxItem.TAG_ITEMS, listTag);
    }
    private void increaseMagAmmo(Player player) {
        ItemStack[] ammoStack = Gun.findAmmoStack(player, this.gun.getProjectile().getItem());
        if (ammoStack.length > 0) {
            CompoundTag tag = this.stack.getTag();
            int ammoAmount = Math.min(ammoInInventory(ammoStack), GunModifierHelper.getModifiedAmmoCapacity(this.stack, this.gun));
            assert tag != null;
            int currentAmmo = tag.getInt("AmmoCount");
            int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(this.stack, this.gun);
            int amount = maxAmmo - currentAmmo;
            if (ammoAmount < amount) {
                tag.putInt("AmmoCount", currentAmmo + ammoAmount);
                this.shrinkFromAmmoPool(ammoStack, player, ammoAmount);
            } else {
                tag.putInt("AmmoCount", maxAmmo);
                this.shrinkFromAmmoPool(ammoStack, player, amount);
            }
        }

        playReloadSound(player);
    }
    private void reloadItem(Player player) {
        AmmoContext context = Gun.findAmmo(player, this.gun.getReloads().getReloadItem());
        ItemStack ammo = context.stack();
        if (!ammo.isEmpty()) {
            CompoundTag tag = this.stack.getTag();
            if (tag != null) {
                int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(this.stack, this.gun);
                tag.putInt("AmmoCount", maxAmmo);
                ammo.shrink(1);
            }

            Container container = context.container();
            if (container != null) {
                container.setChanged();
            }
            shrinkFromAmmoPool(Gun.findAmmoStack(player, this.gun.getReloads().getReloadItem()), player, 1);
        }

        playReloadSound(player);
    }
    private void increaseAmmo(Player player) {
        AmmoContext context = Gun.findAmmo(player, this.gun.getProjectile().getItem());
        ItemStack ammo = context.stack();
        if (!ammo.isEmpty()) {
            int amount = Math.min(ammo.getCount(), this.gun.getReloads().getReloadAmount());
            CompoundTag tag = this.stack.getTag();
            if (tag != null) {
                int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(this.stack, this.gun);
                amount = Math.min(amount, maxAmmo - tag.getInt("AmmoCount"));
                tag.putInt("AmmoCount", tag.getInt("AmmoCount") + amount);
            }
            shrinkFromAmmoPool(Gun.findAmmoStack(player, this.gun.getProjectile().getItem()), player, amount);
        }

        playReloadSound(player);
    }
    private void playReloadSound(Player player) {
        ResourceLocation reloadSound = this.gun.getSounds().getReload();
        if (reloadSound != null) {
            double radius = Config.SERVER.reloadMaxDistance.get();
            double soundX = player.getX();
            double soundY = player.getY() + 1.0;
            double soundZ = player.getZ();
            S2CMessageGunSound message = new S2CMessageGunSound(reloadSound, SoundSource.PLAYERS, (float) soundX, (float) soundY, (float) soundZ, 1.0F, 1.0F, player.getId(), false, true);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), soundX, soundY, soundZ, radius), message);
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide) {
            Player player = event.player;
            if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                if (!RELOAD_TRACKER_MAP.containsKey(player)) {
                    if (!(player.getInventory().getSelected().getItem() instanceof GunItem)) {
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        return;
                    }
                    RELOAD_TRACKER_MAP.put(player, new ReloadTracker(player));
                }
                ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);
                if (!tracker.isSameWeapon(player) || tracker.isWeaponFull() || tracker.hasNoAmmo(player)) {
                    RELOAD_TRACKER_MAP.remove(player);
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    return;
                }
                if (tracker.canReload(player)) {
                    final Player finalPlayer = player;
                    final Gun gun = tracker.gun;
                    if (gun.getReloads().getReloadType() == ReloadType.MAG_FED) {
                        tracker.increaseMagAmmo(player);
                    } else if (gun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) {
                        tracker.reloadItem(player);
                    } else if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                        tracker.increaseAmmo(player);
                    }
                    if (tracker.isWeaponFull() || tracker.hasNoAmmo(player)) {
                        RELOAD_TRACKER_MAP.remove(player);
                        ModSyncedDataKeys.RELOADING.setValue(player, false);

                        DelayedTask.runAfter(4, () -> {
                            ResourceLocation cockSound = gun.getSounds().getCock();
                            if (cockSound != null && finalPlayer.isAlive()) {
                                double soundX = finalPlayer.getX();
                                double soundY = finalPlayer.getY() + 1.0;
                                double soundZ = finalPlayer.getZ();
                                double radius = Config.SERVER.reloadMaxDistance.get();
                                S2CMessageGunSound messageSound = new S2CMessageGunSound(cockSound, SoundSource.PLAYERS, (float) soundX, (float) soundY, (float) soundZ, 1.0F, 1.0F, finalPlayer.getId(), false, true);
                                PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(finalPlayer.level(), soundX, soundY, soundZ, radius), messageSound);
                            }
                        });
                    }
                }
            } else RELOAD_TRACKER_MAP.remove(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server != null) {
            server.execute(() -> RELOAD_TRACKER_MAP.remove(event.getEntity()));
        }
    }
}