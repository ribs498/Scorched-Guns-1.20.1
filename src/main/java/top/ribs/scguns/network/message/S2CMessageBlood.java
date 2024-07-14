package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.client.network.ClientPlayHandler;

/**
 * Author: MrCrayfish
 */
public class S2CMessageBlood extends PlayMessage<S2CMessageBlood>
{
    private double x;
    private double y;
    private double z;
    private EntityType<?> entityType;

    public S2CMessageBlood() {}

    public S2CMessageBlood(double x, double y, double z, EntityType<?> entityType)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.entityType = entityType;
    }

    @Override
    public void encode(S2CMessageBlood message, FriendlyByteBuf buffer)
    {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeResourceLocation(ForgeRegistries.ENTITY_TYPES.getKey(message.entityType));
    }

    @Override
    public S2CMessageBlood decode(FriendlyByteBuf buffer)
    {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        ResourceLocation entityTypeLocation = buffer.readResourceLocation();
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityTypeLocation);
        return new S2CMessageBlood(x, y, z, entityType);
    }

    @Override
    public void handle(S2CMessageBlood message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleMessageBlood(message));
        context.setHandled(true);
    }

    public double getX() { return this.x; }
    public double getY() { return this.y; }
    public double getZ() { return this.z; }
    public EntityType<?> getEntityType() { return this.entityType; }
}
