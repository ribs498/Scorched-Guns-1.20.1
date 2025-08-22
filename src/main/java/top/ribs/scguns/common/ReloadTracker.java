package top.ribs.scguns.common;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.attributes.SCAttributes;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.common.exosuit.ExoSuitAmmoHelper;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.AmmoBoxItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.ammo_boxes.CreativeAmmoBoxItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageGunSound;
import top.ribs.scguns.network.message.S2CMessageStopReload;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import static top.ribs.scguns.common.network.ServerPlayHandler.hasCreativeAmmoBoxInCurios;

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

    public ReloadTracker(Player player) {
        this.startTick = player.tickCount;
        this.slot = player.getInventory().selected;
        this.stack = player.getInventory().getSelected();
        this.gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
    }


    private void handleReloadByproduct(Player player) {
        if (this.gun.getReloads().getReloadType() != ReloadType.SINGLE_ITEM) {
            return;
        }

        if (this.gun.getReloads().shouldGiveByproduct(player.level().getRandom(), this.stack)) {
            Item byproduct = this.gun.getReloads().getReloadByproduct();
            if (byproduct != null) {
                ItemStack byproductStack = new ItemStack(byproduct);

                boolean added = player.getInventory().add(byproductStack);
                if (!added) {
                    Level level = player.level();
                    double x = player.getX();
                    double y = player.getY();
                    double z = player.getZ();
                    ItemEntity itemEntity = new ItemEntity(level, x, y, z, byproductStack);
                    itemEntity.setDeltaMovement(
                            level.random.nextDouble() * 0.2 - 0.1,
                            0.2,
                            level.random.nextDouble() * 0.2 - 0.1
                    );
                    level.addFreshEntity(itemEntity);
                }
            }
        }
    }

    public boolean isWeaponFull(Player player) {
        ItemStack currentStack = player.getMainHandItem();
        CompoundTag tag = currentStack.getOrCreateTag();
        int currentAmmo = tag.getInt("AmmoCount");
        int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(currentStack, this.gun);
        return currentAmmo >= maxAmmo;
    }

    private boolean isWeaponEmpty() {
        CompoundTag tag = this.stack.getOrCreateTag();
        return tag.getInt("AmmoCount") == 0;
    }

    public boolean hasNoAmmo(Player player) {
        boolean result;
        if (gun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) {
            result = Gun.findAmmo(player, this.gun.getReloads().getReloadItem()).stack().isEmpty();
        } else {
            result = Gun.findAmmo(player, this.gun.getProjectile().getItem()).stack().isEmpty();
        }
        return result;
    }

    private boolean canReload(Player player) {
        if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
            return true;
        }

        int deltaTicks = player.tickCount - this.startTick;
        double reloadSpeed = Objects.requireNonNull(player.getAttribute(SCAttributes.RELOAD_SPEED.get())).getValue();
        int interval = (gun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) ?
                (int) Math.ceil((double) GunEnchantmentHelper.getMagReloadSpeed(this.stack) / reloadSpeed) :
                (int) Math.ceil((double) GunEnchantmentHelper.getReloadInterval(this.stack) / reloadSpeed);
        return deltaTicks >= interval;
    }

    public static int ammoInInventory(ItemStack[] ammoStack) {
        int result = 0;
        for (ItemStack x : ammoStack) {
            result += x.getCount();
        }
        return result;
    }

    private void shrinkFromAmmoPool(ItemStack[] ammoStack, Player player, int shrinkAmount) {
        final int[] shrinkAmt = {shrinkAmount};

        int exoSuitShrinkAmount = Math.min(shrinkAmt[0], ExoSuitAmmoHelper.getAmmoCountInExoSuit(player, gun.getProjectile().getItem()));
        if (exoSuitShrinkAmount > 0) {
            ExoSuitAmmoHelper.shrinkAmmoInExoSuit(player, gun.getProjectile().getItem(), exoSuitShrinkAmount);
            shrinkAmt[0] -= exoSuitShrinkAmount;

            if (shrinkAmt[0] == 0) {
                return;
            }
        }

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                ItemStack stack = curios.getStackInSlot(i);
                if (stack.getItem() instanceof AmmoBoxItem) {
                    List<ItemStack> contents = AmmoBoxItem.getContents(stack).collect(Collectors.toList());
                    for (ItemStack pouchAmmoStack : contents) {
                        if (!pouchAmmoStack.isEmpty() && pouchAmmoStack.getItem() == gun.getProjectile().getItem()) {
                            int max = Math.min(shrinkAmt[0], pouchAmmoStack.getCount());
                            pouchAmmoStack.shrink(max);
                            shrinkAmt[0] -= max;
                            if (shrinkAmt[0] == 0) {
                                updateAmmoPouchContents(stack, contents);
                                return;
                            }
                        }
                    }
                    updateAmmoPouchContents(stack, contents);
                }
            }
        });

        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof AmmoBoxItem) {
                List<ItemStack> contents = AmmoBoxItem.getContents(itemStack).collect(Collectors.toList());
                for (ItemStack pouchAmmoStack : contents) {
                    if (!pouchAmmoStack.isEmpty() && pouchAmmoStack.getItem() == gun.getProjectile().getItem()) {
                        int max = Math.min(shrinkAmt[0], pouchAmmoStack.getCount());
                        pouchAmmoStack.shrink(max);
                        shrinkAmt[0] -= max;
                        if (shrinkAmt[0] == 0) {
                            updateAmmoPouchContents(itemStack, contents);
                            return;
                        }
                    }
                }
                updateAmmoPouchContents(itemStack, contents);
            }
        }

        for (ItemStack itemStack : ammoStack) {
            if (shrinkAmt[0] > 0 && !itemStack.isEmpty() && itemStack.getItem() == gun.getProjectile().getItem()) {
                int max = Math.min(shrinkAmt[0], itemStack.getCount());
                itemStack.shrink(max);
                shrinkAmt[0] -= max;
            }
        }
    }

    private void shrinkFromAmmoPool(ItemStack ammoStack, Player player, int shrinkAmount) {
        final int[] shrinkAmt = {shrinkAmount};

        int exoSuitShrinkAmount = Math.min(shrinkAmt[0], ExoSuitAmmoHelper.getAmmoCountInExoSuit(player, gun.getProjectile().getItem()));
        if (exoSuitShrinkAmount > 0) {
            ExoSuitAmmoHelper.shrinkAmmoInExoSuit(player, gun.getProjectile().getItem(), exoSuitShrinkAmount);
            shrinkAmt[0] -= exoSuitShrinkAmount;

            if (shrinkAmt[0] == 0) {
                return;
            }
        }

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                ItemStack stack = curios.getStackInSlot(i);
                if (stack.getItem() instanceof AmmoBoxItem) {
                    List<ItemStack> contents = AmmoBoxItem.getContents(stack).collect(Collectors.toList());
                    for (ItemStack pouchAmmoStack : contents) {
                        if (!pouchAmmoStack.isEmpty() && pouchAmmoStack.getItem() == gun.getProjectile().getItem()) {
                            int max = Math.min(shrinkAmt[0], pouchAmmoStack.getCount());
                            pouchAmmoStack.shrink(max);
                            shrinkAmt[0] -= max;
                            if (shrinkAmt[0] == 0) {
                                updateAmmoPouchContents(stack, contents);
                                return;
                            }
                        }
                    }
                    updateAmmoPouchContents(stack, contents);
                }
            }
        });
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof AmmoBoxItem) {
                List<ItemStack> contents = AmmoBoxItem.getContents(itemStack).collect(Collectors.toList());
                for (ItemStack pouchAmmoStack : contents) {
                    if (!pouchAmmoStack.isEmpty() && pouchAmmoStack.getItem() == gun.getProjectile().getItem()) {
                        int max = Math.min(shrinkAmt[0], pouchAmmoStack.getCount());
                        pouchAmmoStack.shrink(max);
                        shrinkAmt[0] -= max;
                        if (shrinkAmt[0] == 0) {
                            updateAmmoPouchContents(itemStack, contents);
                            return;
                        }
                    }
                }
                updateAmmoPouchContents(itemStack, contents);
            }
        }
        if (shrinkAmt[0] > 0 && !ammoStack.isEmpty() && ammoStack.getItem() == gun.getProjectile().getItem()) {
            int max = Math.min(shrinkAmt[0], ammoStack.getCount());
            ammoStack.shrink(max);
            shrinkAmt[0] -= max;
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

    public void increaseMagAmmo(Player player) {
        ItemStack[] ammoStack = Gun.findAmmoStack(player, this.gun.getProjectile().getItem());
        if (ammoStack.length > 0) {
            CompoundTag tag = this.stack.getTag();
            if (tag != null) {
                int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(this.stack, this.gun);
                boolean hasCreativeBox = player.getInventory().items.stream()
                        .anyMatch(i -> i.getItem() instanceof CreativeAmmoBoxItem) ||
                        hasCreativeAmmoBoxInCurios((ServerPlayer) player);
                if (hasCreativeBox) {
                    tag.putInt("AmmoCount", maxAmmo);
                    return;
                }
                int currentAmmo = tag.getInt("AmmoCount");
                if (currentAmmo < 0 || currentAmmo > maxAmmo) {
                    currentAmmo = 0;
                }
                int ammoAmount = Math.min(ammoInInventory(ammoStack), maxAmmo);
                int amount = maxAmmo - currentAmmo;
                if (ammoAmount < amount) {
                    tag.putInt("AmmoCount", currentAmmo + ammoAmount);
                    this.shrinkFromAmmoPool(ammoStack, player, ammoAmount);
                } else {
                    tag.putInt("AmmoCount", maxAmmo);
                    this.shrinkFromAmmoPool(ammoStack, player, amount);
                }
            }
        }
        playReloadSound(player);
    }


    public void reloadItem(Player player) {
        Item reloadItem = this.gun.getReloads().getReloadItem();
        ItemStack[] ammoStacks = Gun.findAmmoStack(player, reloadItem);

        if (ammoStacks.length > 0) {
            CompoundTag tag = this.stack.getTag();
            if (tag != null) {
                int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(this.stack, this.gun);
                int currentAmmo = tag.getInt("AmmoCount");

                if (currentAmmo < maxAmmo) {
                    tag.putInt("AmmoCount", maxAmmo);
                    this.shrinkFromAmmoPool(ammoStacks, player, 1);
                    handleReloadByproduct(player);
                }
            }
            playReloadSound(player);
        }
    }

    public void increaseAmmo(Player player) {
        AmmoContext context = Gun.findAmmo(player, this.gun.getProjectile().getItem());
        ItemStack ammo = context.stack();

        if (!ammo.isEmpty()) {
            int amount = Math.min(ammo.getCount(), this.gun.getReloads().getReloadAmount());
            ItemStack currentStack = player.getMainHandItem();
            CompoundTag tag = currentStack.getTag();

            if (tag != null) {
                int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(currentStack, this.gun);
                int currentAmmo = tag.getInt("AmmoCount");
                amount = Math.min(amount, maxAmmo - currentAmmo);
                tag.putInt("AmmoCount", currentAmmo + amount);

            }
            shrinkFromAmmoPool(Gun.findAmmoStack(player, this.gun.getProjectile().getItem()), player, amount);
        }

        playReloadSound(player);
    }

    private void playReloadSound(Player player) {
        if (stack.getItem() instanceof AnimatedGunItem) {
            return;
        }
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
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }
        if (ModSyncedDataKeys.RELOADING.getValue(player)) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof GunItem gunItem) {
                Gun gun = gunItem.getModifiedGun(heldItem);

                if (gun.getReloads().getReloadType() != ReloadType.MANUAL) {
                    CompoundTag tag = heldItem.getOrCreateTag();
                    if (tag.getBoolean("scguns:PausedDuringReload")) {
                        RELOAD_TRACKER_MAP.remove(player);
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        tag.remove("IsReloading");
                        tag.remove("scguns:PausedDuringReload");
                        return;
                    }
                }

                CompoundTag tag = heldItem.getOrCreateTag();

                if (!(heldItem.getItem().getClass().getPackageName().startsWith("top.ribs.scguns"))) {
                    return;
                }

                ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);

                boolean needsNewTracker = false;
                if (tracker == null) {
                    needsNewTracker = true;
                } else {
                    ItemStack currentWeapon = player.getInventory().getSelected();
                    int currentSlot = player.getInventory().selected;
                    boolean isActivelyReloading = ModSyncedDataKeys.RELOADING.getValue(player);

                    if (!isActivelyReloading && (tracker.slot != currentSlot ||
                            currentWeapon.isEmpty() ||
                            !currentWeapon.getItem().getClass().equals(tracker.stack.getItem().getClass()))) {
                        needsNewTracker = true;
                        RELOAD_TRACKER_MAP.remove(player);
                    }
                }

                if (needsNewTracker) {
                    if (!(player.getInventory().getSelected().getItem() instanceof GunItem)) {
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        return;
                    }

                    tracker = new ReloadTracker(player);
                    RELOAD_TRACKER_MAP.put(player, tracker);
                }
                boolean weaponFull = tracker.isWeaponFull(player);
                boolean hasNoAmmo = tracker.hasNoAmmo(player);
                if (weaponFull || hasNoAmmo) {
                    RELOAD_TRACKER_MAP.remove(player);
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    tag.remove("IsReloading");
                    tag.remove("scguns:IsReloading");
                    if (player.getMainHandItem().getItem() instanceof AnimatedGunItem) {
                        tag.putString("scguns:ReloadState", "STOPPING");
                        tag.putBoolean("scguns:IsPlayingReloadStop", true);
                        PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageStopReload());
                    }
                }
            }
        } else {
            RELOAD_TRACKER_MAP.remove(player);
            CompoundTag tag = player.getMainHandItem().getTag();
            if (tag != null) {
                tag.remove("IsReloading");
            }
        }
    }


    public static void loaded(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem().getClass().getPackageName().startsWith("top.ribs.scguns"))) {
            return;
        }

        CompoundTag tag = player.getMainHandItem().getTag();
        if (!ModSyncedDataKeys.RELOADING.getValue(player)) {
            RELOAD_TRACKER_MAP.remove(player);
            if (tag != null) {
                tag.remove("IsReloading");
            }
            return;
        }

        if (!RELOAD_TRACKER_MAP.containsKey(player)) {
            if (!(player.getInventory().getSelected().getItem() instanceof GunItem)) {
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                return;
            }
            RELOAD_TRACKER_MAP.put(player, new ReloadTracker(player));
        }

        ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);

        boolean weaponChanged = false;
        ItemStack currentWeapon = player.getInventory().getSelected();
        int currentSlot = player.getInventory().selected;
        if (tracker.slot != currentSlot ||
                !currentWeapon.getItem().equals(tracker.stack.getItem()) ||
                currentWeapon.isEmpty()) {
            weaponChanged = true;
        }

        if (weaponChanged ||
                tracker.hasNoAmmo(player) ||
                (tracker.isWeaponFull(player) && tracker.gun.getReloads().getReloadType() != ReloadType.MANUAL)) {
            RELOAD_TRACKER_MAP.remove(player);
            ModSyncedDataKeys.RELOADING.setValue(player, false);
            if (tag != null) {
                tag.remove("IsReloading");
            }
            return;
        }

        Item item = player.getMainHandItem().getItem();
        if (item instanceof GunItem) {
            Gun gun = tracker.gun;
            ReloadType reloadType = gun.getReloads().getReloadType();
            if (!(item instanceof AnimatedGunItem)) {
                if (reloadType == ReloadType.MAG_FED) {
                    tracker.increaseMagAmmo(player);
                } else if (reloadType == ReloadType.SINGLE_ITEM) {
                    tracker.reloadItem(player);
                } else if (reloadType == ReloadType.MANUAL) {
                    tracker.increaseAmmo(player);
                }

                RELOAD_TRACKER_MAP.remove(player);
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                if (tag != null) {
                    tag.remove("IsReloading");
                }
                return;
            }
            if (item instanceof AnimatedGunItem gunItem &&
                    item.getClass().getPackageName().startsWith("top.ribs.scguns")) {
                if (reloadType == ReloadType.MANUAL) {
                    tracker.increaseAmmo(player);
                }

                if (tracker.isWeaponFull(player) || tracker.hasNoAmmo(player)) {
                    long id = GeoItem.getId(player.getMainHandItem());
                    AnimationController<GeoAnimatable> animationController = gunItem.getAnimatableInstanceCache()
                            .getManagerForId(id)
                            .getAnimationControllers()
                            .get("controller");

                    animationController.setAnimationSpeed(1.0);
                    animationController.forceAnimationReset();

                    if (reloadType == ReloadType.MANUAL) {
                        ReloadHandler.loaded(player);
                    }
                    tracker.handleReloadByproduct(player);
                    RELOAD_TRACKER_MAP.remove(player);
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    if (tag != null) {
                        tag.remove("IsReloading");
                    }
                }
            }
        }
    }
}