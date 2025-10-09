package top.ribs.scguns.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.player.PlayerGunProgression;

import java.util.List;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunProgressionEventHandler {

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItem().getItem();

        if (!player.level().isClientSide) {
            PlayerGunProgression progression = PlayerGunProgression.get(player);

            if (progression.checkAndUpdateFromItem(stack)) {
                PlayerGunProgression.save(player, progression);
                sendTierUnlockedMessage(player, progression.getCurrentTier());
            }
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getCrafting();

        if (!player.level().isClientSide) {
            PlayerGunProgression progression = PlayerGunProgression.get(player);

            if (progression.checkAndUpdateFromItem(stack)) {
                PlayerGunProgression.save(player, progression);
                sendTierUnlockedMessage(player, progression.getCurrentTier());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide) {
            PlayerGunProgression progression = PlayerGunProgression.get(player);
            boolean updated = false;

            for (ItemStack stack : player.getInventory().items) {
                if (progression.checkAndUpdateFromItem(stack)) {
                    updated = true;
                }
            }

            if (updated) {
                PlayerGunProgression.save(player, progression);
            }
        }
    }

    public static void sendTierUnlockedMessage(Player player, PlayerGunProgression.GunTier tier) {
        if (tier == PlayerGunProgression.GunTier.NONE) {
            return;
        }

        Component tierName = Component.translatable("gun_tier.scguns." + tier.name().toLowerCase())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);

        Component message = Component.translatable("progression.scguns.tier_unlocked", tierName)
                .withStyle(ChatFormatting.YELLOW);

        player.sendSystemMessage(message);

        List<PlayerGunProgression.GunTier> availableTiers = tier.getAvailableMobTiers();

        if (!availableTiers.isEmpty()) {
            Component mobMessage = Component.translatable("progression.scguns.enemies_can_spawn")
                    .withStyle(ChatFormatting.GRAY);

            for (int i = 0; i < availableTiers.size(); i++) {
                PlayerGunProgression.GunTier mobTier = availableTiers.get(i);
                Component tierComponent = Component.translatable("gun_tier.scguns." + mobTier.name().toLowerCase())
                        .withStyle(ChatFormatting.RED);

                mobMessage = mobMessage.copy().append(tierComponent);

                if (i < availableTiers.size() - 1) {
                    mobMessage = mobMessage.copy().append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                }
            }

            player.sendSystemMessage(mobMessage);
        }
    }
}