package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.client.network.ClientPlayHandler;

public class S2CMessageEntityCasingEject extends PlayMessage<S2CMessageEntityCasingEject> {
    private int entityId;
    private ResourceLocation particleLocation;

    public S2CMessageEntityCasingEject() {}

    public S2CMessageEntityCasingEject(int entityId, ResourceLocation particleLocation) {
        this.entityId = entityId;
        this.particleLocation = particleLocation;
    }

    @Override
    public void encode(S2CMessageEntityCasingEject message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeResourceLocation(message.particleLocation);
    }

    @Override
    public S2CMessageEntityCasingEject decode(FriendlyByteBuf buffer) {
        return new S2CMessageEntityCasingEject(buffer.readInt(), buffer.readResourceLocation());
    }

    @Override
    public void handle(S2CMessageEntityCasingEject message, MessageContext context) {
        context.execute(() -> {
            ClientPlayHandler.handleEntityCasingEject(message);
        });
        context.setHandled(true);
    }

    public int getEntityId() {
        return entityId;
    }

    public ResourceLocation getParticleLocation() {
        return particleLocation;
    }
}