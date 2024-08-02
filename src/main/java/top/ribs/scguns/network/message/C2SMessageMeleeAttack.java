package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.PacketDistributor;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.handler.MeleeAttackHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.minecraft.world.item.ItemStack;

import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;

public class C2SMessageMeleeAttack extends PlayMessage<C2SMessageMeleeAttack> {
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@Override
	public void encode(C2SMessageMeleeAttack message, FriendlyByteBuf buffer) {
		// No data to encode
	}

	@Override
	public C2SMessageMeleeAttack decode(FriendlyByteBuf buffer) {
		return new C2SMessageMeleeAttack();
	}

	@Override
	public void handle(C2SMessageMeleeAttack message, MessageContext context) {
		context.execute(() -> {
			ServerPlayer player = context.getPlayer();
			if (player != null && !player.isSpectator()) {
				if (MeleeAttackHandler.isBanzaiActive()) {
					MeleeAttackHandler.stopBanzai();
				} else if (player.isSprinting()) {
					ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
					if (!(heldItem.getItem() instanceof GunItem gunItem)) {
						return;
					}
					if (gunItem.hasBayonet(heldItem)) {
						MeleeAttackHandler.startBanzai(player);
						scheduler.scheduleAtFixedRate(() -> {
							if (MeleeAttackHandler.isBanzaiActive() && player.isSprinting()) {
								MeleeAttackHandler.handleBanzaiMode(player);
							} else if (MeleeAttackHandler.isBanzaiActive() && !player.isSprinting()) {
								MeleeAttackHandler.stopBanzai();
							}
						}, 0, 1, TimeUnit.SECONDS);
					} else {
						MeleeAttackHandler.performNormalMeleeAttack(player);
					}
				} else {
					if (ModSyncedDataKeys.MELEE.getValue(player) || player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())) {
						return;
					}
					ModSyncedDataKeys.MELEE.setValue(player, true);
					MeleeAttackHandler.performMeleeAttack(player);

					// Send S2CMessageMeleeAttack to the client
					//System.out.println("Sending S2CMessageMeleeAttack to client.");
					PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageMeleeAttack(player.getItemInHand(InteractionHand.MAIN_HAND)));

					scheduler.schedule(() -> {
						ModSyncedDataKeys.MELEE.setValue(player, false);
					}, (long) GunRenderingHandler.MELEE_DURATION, TimeUnit.MILLISECONDS);
				}
			}
		});
		context.setHandled(true);
	}
}
