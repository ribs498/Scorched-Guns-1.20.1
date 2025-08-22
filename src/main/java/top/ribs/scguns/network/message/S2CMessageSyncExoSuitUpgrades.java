package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import top.ribs.scguns.client.network.ClientPlayHandler;

import java.util.UUID;

public class S2CMessageSyncExoSuitUpgrades extends PlayMessage<S2CMessageSyncExoSuitUpgrades> {

    private UUID playerId;
    private EquipmentSlot armorSlot;
    private CompoundTag upgradeData;

    public S2CMessageSyncExoSuitUpgrades() {}

    public S2CMessageSyncExoSuitUpgrades(UUID playerId, EquipmentSlot armorSlot, CompoundTag upgradeData) {
        this.playerId = playerId;
        this.armorSlot = armorSlot;
        this.upgradeData = upgradeData;
    }

    @Override
    public void encode(S2CMessageSyncExoSuitUpgrades message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.playerId);
        buffer.writeEnum(message.armorSlot);
        buffer.writeNbt(message.upgradeData);
    }

    @Override
    public S2CMessageSyncExoSuitUpgrades decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        EquipmentSlot armorSlot = buffer.readEnum(EquipmentSlot.class);
        CompoundTag upgradeData = buffer.readNbt();
        return new S2CMessageSyncExoSuitUpgrades(playerId, armorSlot, upgradeData);
    }

    @Override
    public void handle(S2CMessageSyncExoSuitUpgrades message, MessageContext context) {
        context.execute(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPlayHandler.handleSyncExoSuitUpgrades(message)));
        context.setHandled(true);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public EquipmentSlot getArmorSlot() {
        return armorSlot;
    }

    public CompoundTag getUpgradeData() {
        return upgradeData;
    }
}