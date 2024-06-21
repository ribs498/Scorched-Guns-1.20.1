package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import top.ribs.scguns.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessageShoot extends PlayMessage<C2SMessageShoot>
{
    private float rotationYaw;
    private float rotationPitch;

    public C2SMessageShoot() {}

    public C2SMessageShoot(Player player)
    {
        this.rotationYaw = player.getYRot();
        this.rotationPitch = player.getXRot();
    }

    public C2SMessageShoot(float rotationYaw, float rotationPitch)
    {
        this.rotationYaw = rotationYaw;
        this.rotationPitch = rotationPitch;
    }

    @Override
    public void encode(C2SMessageShoot message, FriendlyByteBuf buffer)
    {
        buffer.writeFloat(message.rotationYaw);
        buffer.writeFloat(message.rotationPitch);
    }

    @Override
    public C2SMessageShoot decode(FriendlyByteBuf buffer)
    {
        float rotationYaw = buffer.readFloat();
        float rotationPitch = buffer.readFloat();
        return new C2SMessageShoot(rotationYaw, rotationPitch);
    }

    @Override
    public void handle(C2SMessageShoot message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null)
            {
                ServerPlayHandler.handleShoot(message, player);
            }
        });
        context.setHandled(true);
    }

    public float getRotationYaw()
    {
        return this.rotationYaw;
    }

    public float getRotationPitch()
    {
        return this.rotationPitch;
    }
}