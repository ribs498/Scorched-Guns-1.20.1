package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import top.ribs.scguns.common.network.ServerPlayHandler;

public class C2SMessageBeam extends PlayMessage<C2SMessageBeam> {
    private float rotationYaw;
    private float rotationPitch;
    private boolean isFiring;

    public C2SMessageBeam() {}

    public C2SMessageBeam(Player player, boolean isFiring) {
        this.rotationYaw = player.getYRot();
        this.rotationPitch = player.getXRot();
        this.isFiring = isFiring;
    }

    public C2SMessageBeam(float rotationYaw, float rotationPitch, boolean isFiring) {
        this.rotationYaw = rotationYaw;
        this.rotationPitch = rotationPitch;
        this.isFiring = isFiring;
    }

    @Override
    public void encode(C2SMessageBeam message, FriendlyByteBuf buffer) {
        buffer.writeFloat(message.rotationYaw);
        buffer.writeFloat(message.rotationPitch);
        buffer.writeBoolean(message.isFiring);
    }

    @Override
    public C2SMessageBeam decode(FriendlyByteBuf buffer) {
        float rotationYaw = buffer.readFloat();
        float rotationPitch = buffer.readFloat();
        boolean isFiring = buffer.readBoolean();
        return new C2SMessageBeam(rotationYaw, rotationPitch, isFiring);
    }

    @Override
    public void handle(C2SMessageBeam message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if (player != null) {
                //ServerPlayHandler.handleBeam(message, player);
            }
        });
        context.setHandled(true);
    }

    public float getRotationYaw() {
        return this.rotationYaw;
    }

    public float getRotationPitch() {
        return this.rotationPitch;
    }

    public boolean isFiring() {
        return this.isFiring;
    }
}
