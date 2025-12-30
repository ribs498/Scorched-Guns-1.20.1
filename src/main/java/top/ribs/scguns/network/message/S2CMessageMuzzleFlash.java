package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.client.ClientUtil;
import top.ribs.scguns.init.ModParticleTypes;

public class S2CMessageMuzzleFlash extends PlayMessage<S2CMessageMuzzleFlash> {

    private Vec3 position;
    private float yaw;
    private float pitch;

    public S2CMessageMuzzleFlash() {}

    public S2CMessageMuzzleFlash(Vec3 position, float yaw, float pitch) {
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void encode(S2CMessageMuzzleFlash message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.position.x);
        buffer.writeDouble(message.position.y);
        buffer.writeDouble(message.position.z);
        buffer.writeFloat(message.yaw);
        buffer.writeFloat(message.pitch);
    }

    @Override
    public S2CMessageMuzzleFlash decode(FriendlyByteBuf buffer) {
        return new S2CMessageMuzzleFlash(
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readFloat(),
                buffer.readFloat()
        );
    }

    @Override
    public void handle(S2CMessageMuzzleFlash message, MessageContext context) {
        context.execute(() -> {
            ClientLevel level = ClientUtil.getClientWorld();
            if (level != null) {
                level.addParticle(
                        ModParticleTypes.TURRET_MUZZLE_FLASH.get(),
                        message.position.x,
                        message.position.y,
                        message.position.z,
                        0, 0, 0
                );
            }
        });
        context.setHandled(true);
    }
}