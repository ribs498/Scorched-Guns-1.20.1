package top.ribs.scguns.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.config.RaidConfig;
import top.ribs.scguns.entity.player.GunTier;
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
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player oldPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();

            oldPlayer.reviveCaps();

            PlayerGunProgression oldProgression = PlayerGunProgression.get(oldPlayer);
            PlayerGunProgression.save(newPlayer, oldProgression);

            oldPlayer.invalidateCaps();
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

    public static void sendTierUnlockedMessage(Player player, GunTier tier) {
        if (!Config.CLIENT.display.showProgressionMessages.get()) {
            return;
        }

        if (tier == null || tier.getLevel() == 0) {
            return;
        }

        Component tierName = Component.translatable("gun_tier.scguns." + tier.getId())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);

        Component message = Component.translatable("progression.scguns.tier_unlocked", tierName)
                .withStyle(ChatFormatting.YELLOW);

        player.sendSystemMessage(message);

        List<GunTier> availableTiers = tier.getAvailableMobTiers();

        if (!availableTiers.isEmpty()) {
            Component mobMessage = Component.translatable("progression.scguns.enemies_can_spawn")
                    .withStyle(ChatFormatting.GRAY);

            for (int i = 0; i < availableTiers.size(); i++) {
                GunTier mobTier = availableTiers.get(i);
                Component tierComponent = Component.translatable("gun_tier.scguns." + mobTier.getId())
                        .withStyle(ChatFormatting.RED);

                mobMessage = mobMessage.copy().append(tierComponent);

                if (i < availableTiers.size() - 1) {
                    mobMessage = mobMessage.copy().append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                }
            }

            player.sendSystemMessage(mobMessage);
        }

        sendRaidUnlockedMessage(player, tier);
    }

    private static void sendRaidUnlockedMessage(Player player, GunTier tier) {
        int raidLevel = tier.getRaidLevel();

        if (raidLevel <= 0) {
            return;
        }

        List<RaidConfig.RaidData> availableRaids = RaidConfig.getRaidsForLevel(raidLevel);

        if (availableRaids.isEmpty()) {
            return;
        }

        Component raidMessage = Component.translatable("progression.scguns.raids_can_spawn")
                .withStyle(ChatFormatting.DARK_GRAY);

        for (int i = 0; i < availableRaids.size(); i++) {
            RaidConfig.RaidData raid = availableRaids.get(i);
            Component raidComponent = Component.translatable("raid.scguns." + raid.raidId())
                    .withStyle(ChatFormatting.DARK_RED);

            raidMessage = raidMessage.copy().append(raidComponent);

            if (i < availableRaids.size() - 1) {
                raidMessage = raidMessage.copy().append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        player.sendSystemMessage(raidMessage);
    }
}