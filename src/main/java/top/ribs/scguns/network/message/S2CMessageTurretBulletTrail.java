package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.client.TurretBulletTrail;
import top.ribs.scguns.client.handler.TurretBulletTrailRenderingHandler;
import top.ribs.scguns.network.BufferUtil;

public class S2CMessageTurretBulletTrail extends PlayMessage<S2CMessageTurretBulletTrail> {
    private int entityId;
    private Vec3 position;
    private Vec3 motion;
    private int trailColor;
    private double trailLengthMultiplier;
    private int maxAge;
    private double trailThickness;

    public S2CMessageTurretBulletTrail() {}

    public S2CMessageTurretBulletTrail(int entityId, Vec3 position, Vec3 motion,
                                       int trailColor, double trailLengthMultiplier,
                                       int maxAge, double trailThickness) {
        this.entityId = entityId;
        this.position = position;
        this.motion = motion;
        this.trailColor = trailColor;
        this.trailLengthMultiplier = trailLengthMultiplier;
        this.maxAge = maxAge;
        this.trailThickness = trailThickness;
    }

    @Override
    public void encode(S2CMessageTurretBulletTrail message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        BufferUtil.writeVec3(buffer, message.position);
        BufferUtil.writeVec3(buffer, message.motion);
        buffer.writeInt(message.trailColor);
        buffer.writeDouble(message.trailLengthMultiplier);
        buffer.writeInt(message.maxAge);
        buffer.writeDouble(message.trailThickness);
    }

    @Override
    public S2CMessageTurretBulletTrail decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        Vec3 position = BufferUtil.readVec3(buffer);
        Vec3 motion = BufferUtil.readVec3(buffer);
        int trailColor = buffer.readInt();
        double trailLengthMultiplier = buffer.readDouble();
        int maxAge = buffer.readInt();
        double trailThickness = buffer.readDouble();

        return new S2CMessageTurretBulletTrail(entityId, position, motion, trailColor,
                trailLengthMultiplier, maxAge, trailThickness);
    }

    @Override
    public void handle(S2CMessageTurretBulletTrail message, MessageContext context) {
        context.execute(() -> {
            TurretBulletTrail trail = new TurretBulletTrail(
                    message.entityId,
                    message.position,
                    message.motion,
                    message.trailColor,
                    message.trailLengthMultiplier,
                    message.maxAge,
                    message.trailThickness
            );
            TurretBulletTrailRenderingHandler.get().add(trail);
        });
        context.setHandled(true);
    }
}