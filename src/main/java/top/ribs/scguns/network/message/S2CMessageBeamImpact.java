package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.client.handler.BeamHandler;

import java.util.UUID;
import java.util.function.Supplier;

public class S2CMessageBeamImpact extends PlayMessage<S2CMessageBeamImpact> {

    private Vec3 hitPosition;
    private UUID playerUUID;

    // Default constructor required for decoding
    public S2CMessageBeamImpact() {}

    public S2CMessageBeamImpact(Vec3 hitPosition, UUID playerUUID) {
        this.hitPosition = hitPosition;
        this.playerUUID = playerUUID;
    }

    @Override
    public void encode(S2CMessageBeamImpact message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.hitPosition.x);
        buffer.writeDouble(message.hitPosition.y);
        buffer.writeDouble(message.hitPosition.z);
        buffer.writeUUID(message.playerUUID);
    }

    @Override
    public S2CMessageBeamImpact decode(FriendlyByteBuf buffer) {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        Vec3 hitPosition = new Vec3(x, y, z);
        UUID playerUUID = buffer.readUUID();
        return new S2CMessageBeamImpact(hitPosition, playerUUID);
    }

    @Override
    public void handle(S2CMessageBeamImpact message, MessageContext context) {
        context.execute(() -> {
            ClientLevel world = Minecraft.getInstance().level;
            if (world != null) {
                Player player = world.getPlayerByUUID(message.playerUUID);
                if (player != null) {
                    BeamHandler.spawnBeamImpactParticles(world, message.hitPosition, player);
                }
            }
        });
        context.setHandled(true);
    }
}
