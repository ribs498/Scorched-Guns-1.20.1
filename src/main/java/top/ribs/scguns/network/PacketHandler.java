package top.ribs.scguns.network;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import com.mrcrayfish.framework.api.network.MessageDirection;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.network.message.*;

public class PacketHandler
{
    private static FrameworkNetwork playChannel;

    public static void init()
    {
        playChannel = FrameworkAPI.createNetworkBuilder(new ResourceLocation(Reference.MOD_ID, "play"), 1)
                .registerPlayMessage(C2SMessageAim.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageMeleeAttack.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(S2CMessageMuzzleFlash.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(C2SMessageReload.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageReloadByproduct.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageGunLoaded.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageEjectCasing.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageManualReloadState.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageManualReloadEnd.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(S2CMessageUpdateAmmo.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(C2SMessageShoot.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageChargeSync.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessagePreFireSound.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageUnload.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(S2CMessageStunGrenade.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageBulletTrail.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(C2SMessageAttachments.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(S2CMessageUpdateGuns.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageBlood.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageReload.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageBeamUpdate.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageBeamPenetration.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageStopBeam.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageBeamImpact.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(C2SMessageShooting.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageStopBeam.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(S2CMessageGunSound.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageMeleeAttack.class, MessageDirection.PLAY_CLIENT_BOUND)

                .registerPlayMessage(S2CMessageProjectileHitBlock.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageProjectileHitEntity.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(C2SMessageLeftOverAmmo.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(S2CMessageRemoveProjectile.class, MessageDirection.PLAY_CLIENT_BOUND)
                .build();

    }

    public static FrameworkNetwork getPlayChannel()
    {
        return playChannel;
    }


}
