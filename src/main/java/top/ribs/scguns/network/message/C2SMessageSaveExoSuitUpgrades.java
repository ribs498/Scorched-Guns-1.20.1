package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.client.screen.ExoSuitMenu;
import top.ribs.scguns.common.exosuit.ExoSuitData;
import top.ribs.scguns.item.animated.ExoSuitItem;
import top.ribs.scguns.network.PacketHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Network message to save ExoSuit upgrades from client to server
 */
public class C2SMessageSaveExoSuitUpgrades extends PlayMessage<C2SMessageSaveExoSuitUpgrades> {
    private List<ItemStack> upgradeStacks;

    public C2SMessageSaveExoSuitUpgrades() {
        this.upgradeStacks = new ArrayList<>();
    }

    public C2SMessageSaveExoSuitUpgrades(List<ItemStack> upgradeStacks) {
        this.upgradeStacks = upgradeStacks != null ? upgradeStacks : new ArrayList<>();
    }

    @Override
    public void encode(C2SMessageSaveExoSuitUpgrades message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.upgradeStacks.size());

        for (ItemStack stack : message.upgradeStacks) {
            buffer.writeItem(stack);
        }
    }

    @Override
    public C2SMessageSaveExoSuitUpgrades decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<ItemStack> stacks = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            stacks.add(buffer.readItem());
        }

        return new C2SMessageSaveExoSuitUpgrades(stacks);
    }

    @Override
    public void handle(C2SMessageSaveExoSuitUpgrades message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer serverPlayer = context.getPlayer();
            if (serverPlayer != null && serverPlayer.containerMenu instanceof ExoSuitMenu menu) {
                ItemStack menuArmorPiece = menu.getArmorPiece();

                if (!menuArmorPiece.isEmpty() && menuArmorPiece.getItem() instanceof ExoSuitItem exoSuit) {
                    CompoundTag upgradeData = getCompoundTag(message);

                    ExoSuitData.setUpgradeData(menuArmorPiece, upgradeData);

                    EquipmentSlot armorSlot = getEquipmentSlotForArmorType(exoSuit.getType());
                    ItemStack equippedPiece = serverPlayer.getItemBySlot(armorSlot);

                    if (!equippedPiece.isEmpty() && equippedPiece.getItem() instanceof ExoSuitItem) {
                        ExoSuitData.setUpgradeData(equippedPiece, upgradeData);
                        serverPlayer.setItemSlot(armorSlot, equippedPiece);

                        // Send sync to ALL nearby players INCLUDING the one who made the change
                        List<ServerPlayer> playersToSync = serverPlayer.serverLevel().getEntitiesOfClass(ServerPlayer.class,
                                serverPlayer.getBoundingBox().inflate(64.0));

                        // Make sure the originating player is included
                        if (!playersToSync.contains(serverPlayer)) {
                            playersToSync.add(serverPlayer);
                        }

                        for (ServerPlayer nearbyPlayer : playersToSync) {
                            PacketHandler.getPlayChannel().sendToPlayer(
                                    () -> nearbyPlayer,
                                    new S2CMessageSyncExoSuitUpgrades(serverPlayer.getUUID(), armorSlot, upgradeData)
                            );
                        }
                    }
                }
            }
        });
        context.setHandled(true);
    }

    private static @NotNull CompoundTag getCompoundTag(C2SMessageSaveExoSuitUpgrades message) {
        CompoundTag upgradeData = new CompoundTag();
        ListTag upgradeList = new ListTag();

        for (int i = 0; i < message.upgradeStacks.size(); i++) {
            ItemStack upgradeStack = message.upgradeStacks.get(i);
            if (!upgradeStack.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putInt("Slot", i);

                CompoundTag itemTag = new CompoundTag();
                upgradeStack.save(itemTag);
                slotTag.put("Item", itemTag);

                upgradeList.add(slotTag);
            }
        }
        upgradeData.put("Upgrades", upgradeList);
        return upgradeData;
    }

    private EquipmentSlot getEquipmentSlotForArmorType(ArmorItem.Type armorType) {
        return switch (armorType) {
            case HELMET -> EquipmentSlot.HEAD;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case LEGGINGS -> EquipmentSlot.LEGS;
            case BOOTS -> EquipmentSlot.FEET;
        };
    }
    private static void saveUpgradesToArmor(ItemStack armorPiece, ExoSuitItem exoSuit, List<ItemStack> upgradeStacks) {
        CompoundTag upgradeData = new CompoundTag();
        ListTag upgradeList = new ListTag();

        for (int i = 0; i < upgradeStacks.size() && i < exoSuit.getMaxUpgradeSlots(); i++) {
            ItemStack upgradeStack = upgradeStacks.get(i);
            if (!upgradeStack.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putInt("Slot", i);

                // Create a separate NBT tag for the ItemStack data
                CompoundTag itemTag = new CompoundTag();
                upgradeStack.save(itemTag);
                slotTag.put("Item", itemTag);

                upgradeList.add(slotTag);
            }
        }
        upgradeData.put("Upgrades", upgradeList);
        ExoSuitData.setUpgradeData(armorPiece, upgradeData);
    }
}