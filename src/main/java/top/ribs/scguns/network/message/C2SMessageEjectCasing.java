package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.GunEventBus;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.ammo_boxes.EmptyCasingPouchItem;

import java.util.Objects;

public class C2SMessageEjectCasing extends PlayMessage<C2SMessageEjectCasing> {
    public C2SMessageEjectCasing() {}

    public void encode(C2SMessageEjectCasing message, FriendlyByteBuf buffer) {}

    public C2SMessageEjectCasing decode(FriendlyByteBuf buffer) {
        return new C2SMessageEjectCasing();
    }

    public void handle(C2SMessageEjectCasing message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if (player != null && !player.isSpectator()) {
                ItemStack heldItem = player.getMainHandItem();
                if (heldItem.getItem() instanceof GunItem) {
                    if (!heldItem.getItem().getClass().getPackageName().startsWith("top.ribs.scguns")) {
                        return;
                    }

                    Gun gun = ((GunItem)heldItem.getItem()).getModifiedGun(heldItem);
                    if (gun.getProjectile().casingType != null && !player.getAbilities().instabuild) {
                        ItemStack casingStack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(gun.getProjectile().casingType)));

                        double baseChance = 0.4;
                        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SHELL_CATCHER.get(), heldItem);
                        double finalChance = baseChance + (enchantmentLevel * 0.15);

                        if (Math.random() < finalChance) {
                            // Try to add to pouch first
                            boolean addedToPouch = false;
                            for (ItemStack itemStack : player.getInventory().items) {
                                if (itemStack.getItem() instanceof EmptyCasingPouchItem) {
                                    int insertedItems = EmptyCasingPouchItem.add(itemStack, casingStack);
                                    if (insertedItems > 0) {
                                        addedToPouch = true;
                                        // Sync the pouch contents with client
                                        player.getInventory().setChanged();
                                        break;
                                    }
                                }
                            }

                            // If not added to pouch, spawn in world
                            if (!addedToPouch) {
                                GunEventBus.spawnCasingInWorld(player.level(), player, casingStack.copy());
                            }
                        }
                    }
                }
            }
            context.setHandled(true);
        });
    }
}