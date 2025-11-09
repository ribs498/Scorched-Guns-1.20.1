package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;

public class C2SMessageSwapAmmo extends PlayMessage<C2SMessageSwapAmmo> {

    public C2SMessageSwapAmmo() {
    }

    @Override
    public void encode(C2SMessageSwapAmmo message, FriendlyByteBuf buffer) {
    }

    @Override
    public C2SMessageSwapAmmo decode(FriendlyByteBuf buffer) {
        return new C2SMessageSwapAmmo();
    }

    @Override
    public void handle(C2SMessageSwapAmmo message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if (player == null) return;

            ItemStack heldItem = player.getMainHandItem();
            if (!(heldItem.getItem() instanceof GunItem gunItem)) {
                return;
            }

            Gun modifiedGun = gunItem.getModifiedGun(heldItem);
            Gun.General general = modifiedGun.getGeneral();

            if (!general.allowsAmmoChange()) {
                player.sendSystemMessage(
                        Component.translatable("message.scguns.ammo_swap.not_supported")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            if (general.getAvailableAmmoTypes().isEmpty()) {
                player.sendSystemMessage(
                        Component.translatable("message.scguns.ammo_swap.no_types")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            CompoundTag tag = heldItem.getOrCreateTag();
            int currentAmmo = tag.getInt("AmmoCount");

            if (currentAmmo > 0 && !player.isCreative()) {
                Item currentAmmoItem = modifiedGun.getProjectile(heldItem).getItem();
                assert currentAmmoItem != null;
                ItemStack ammoStack = new ItemStack(currentAmmoItem, currentAmmo);
                if (!player.getInventory().add(ammoStack)) {
                    player.drop(ammoStack, false);
                }
            }

            tag.putInt("AmmoCount", 0);
            PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageUpdateAmmo(0));

            int currentIndex = general.getCurrentAmmoTypeIndex();
            int nextIndex = (currentIndex + 1) % general.getAvailableAmmoTypes().size();

            CompoundTag gunTag = tag.getCompound("Gun");
            CompoundTag generalTag = gunTag.getCompound("General");
            generalTag.putInt("CurrentAmmoTypeIndex", nextIndex);
            gunTag.put("General", generalTag);
            tag.put("Gun", gunTag);

            modifiedGun.getGeneral().setCurrentAmmoTypeIndex(nextIndex);

            String ammoType = general.getAvailableAmmoTypes().get(nextIndex);

            String displayName = ammoType;
            if (ammoType.contains(":")) {
                displayName = ammoType.substring(ammoType.indexOf(":") + 1);
            }
            displayName = displayName.replace("_", " ");
            String[] words = displayName.split(" ");
            StringBuilder titleCase = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    titleCase.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1).toLowerCase())
                            .append(" ");
                }
            }

            Component statusMessage = Component.translatable("message.scguns.ammo_swap.changed")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(titleCase.toString().trim()).withStyle(ChatFormatting.YELLOW));

            player.sendSystemMessage(statusMessage, true);

            var ammoContext = Gun.findAmmo(player, modifiedGun.getProjectile(heldItem).getItem());
            if (!ammoContext.stack().isEmpty() || player.isCreative()) {
                ModSyncedDataKeys.RELOADING.setValue(player, true);
                tag.putBoolean("IsReloading", true);
                tag.putBoolean("scguns:IsReloading", true);

                if (modifiedGun.getReloads().getReloadType() == ReloadType.MANUAL) {
                    tag.putBoolean("IsManualReload", true);
                    tag.putString("scguns:ReloadState", "NONE");
                } else {
                    tag.putBoolean("InCriticalReloadPhase", true);
                }

                PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageReload(true));
            }
        });
        context.setHandled(true);
    }
}