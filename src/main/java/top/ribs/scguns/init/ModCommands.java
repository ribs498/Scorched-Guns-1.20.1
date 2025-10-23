package top.ribs.scguns.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.entity.player.GunTier;
import top.ribs.scguns.entity.player.GunTierRegistry;
import top.ribs.scguns.entity.player.PlayerGunProgression;
import top.ribs.scguns.event.GunProgressionEventHandler;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import top.ribs.scguns.config.RaidConfig;
import top.ribs.scguns.entity.raid.RaidManager;
import top.ribs.scguns.entity.raid.ActiveRaid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("scguns")
                        .then(Commands.literal("progression")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("tier", StringArgumentType.string())
                                                        .suggests((context, builder) -> {
                                                            for (GunTier tier : GunTierRegistry.getAllTiers()) {
                                                                builder.suggest(tier.getId());
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(context -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                            String tierName = StringArgumentType.getString(context, "tier");
                                                            return executeSetProgression(context.getSource(), player, tierName);
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("clear")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    return executeClearProgression(context.getSource(), player);
                                                })
                                        )
                                )
                                .then(Commands.literal("check")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    return executeCheckProgression(context.getSource(), player);
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("raid")
                                .then(Commands.literal("start")
                                        .then(Commands.argument("raid_id", StringArgumentType.string())
                                                .suggests((context, builder) -> {
                                                    for (RaidConfig.RaidData raid : RaidConfig.getAllRaids()) {
                                                        builder.suggest(raid.raidId());
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    String raidId = StringArgumentType.getString(context, "raid_id");
                                                    return executeStartRaidById(context.getSource(), raidId);
                                                })
                                        )
                                )
                                .then(Commands.literal("stop")
                                        .executes(context -> executeStopAllRaids(context.getSource()))
                                )
                                .then(Commands.literal("list")
                                        .executes(context -> executeListRaids(context.getSource()))
                                )
                                .then(Commands.literal("listall")
                                        .executes(context -> executeListAllAvailableRaids(context.getSource()))
                                )
                                .then(Commands.literal("startnext")
                                        .executes(context -> executeStartNextRaid(context.getSource()))
                                )
                        )
        );
    }

    private static int executeStartRaidById(CommandSourceStack source, String raidId) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.translatable("commands.scguns.no_permission"));
            return 0;
        }

        ServerLevel serverLevel = source.getLevel();
        RaidConfig.RaidData raidConfig = RaidConfig.getRaidById(raidId);

        if (raidConfig == null) {
            source.sendFailure(Component.translatable("commands.scguns.raid.no_config", raidId));
            return 0;
        }

        Vec3 sourcePos = source.getPosition();
        RaidManager manager = RaidManager.get(serverLevel);
        manager.startRaid(raidConfig, serverLevel, sourcePos);

        Component raidName = Component.literal(raidConfig.raidId()).withStyle(ChatFormatting.GOLD);
        source.sendSuccess(() -> Component.translatable("commands.scguns.raid.started", raidName), true);

        return 1;
    }

    private static int executeListAllAvailableRaids(CommandSourceStack source) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.translatable("commands.scguns.no_permission"));
            return 0;
        }

        Collection<RaidConfig.RaidData> progressionRaids = RaidConfig.getProgressionRaids();
        Collection<RaidConfig.RaidData> customRaids = RaidConfig.getCustomRaids();

        source.sendSuccess(() -> Component.literal("=== Available Raids ===").withStyle(ChatFormatting.GOLD), false);

        if (!progressionRaids.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Progression Raids:").withStyle(ChatFormatting.YELLOW), false);
            for (RaidConfig.RaidData raid : progressionRaids) {
                String levelStr = raid.raidLevel() != null ? "Level " + raid.raidLevel() : "NONE";
                source.sendSuccess(() -> Component.literal("  - " + raid.raidId() + " (" + levelStr + ")")
                        .withStyle(ChatFormatting.WHITE), false);
            }
        }

        if (!customRaids.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Custom Raids:").withStyle(ChatFormatting.AQUA), false);
            for (RaidConfig.RaidData raid : customRaids) {
                source.sendSuccess(() -> Component.literal("  - " + raid.raidId())
                        .withStyle(ChatFormatting.WHITE), false);
            }
        }

        if (progressionRaids.isEmpty() && customRaids.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No raids configured!").withStyle(ChatFormatting.RED), false);
        }

        return 1;
    }

    private static int executeStartNextRaid(CommandSourceStack source) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.translatable("commands.scguns.no_permission"));
            return 0;
        }

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("commands.scguns.requires_player"));
            return 0;
        }

        ServerLevel serverLevel = source.getLevel();
        PlayerGunProgression progression = PlayerGunProgression.get(player);
        int currentRaidLevel = progression.getCurrentRaidLevel();

        if (currentRaidLevel == 0) {
            source.sendFailure(Component.translatable("commands.scguns.unlock_gun"));
            return 0;
        }

        List<RaidConfig.RaidData> availableRaids = RaidConfig.getRaidsForLevel(currentRaidLevel);
        if (availableRaids.isEmpty()) {
            source.sendFailure(Component.translatable("commands.scguns.raid.none_for_level", currentRaidLevel));
            return 0;
        }

        RaidConfig.RaidData selectedRaid = availableRaids.get(serverLevel.getRandom().nextInt(availableRaids.size()));

        Vec3 sourcePos = source.getPosition();
        RaidManager manager = RaidManager.get(serverLevel);
        manager.startRaid(selectedRaid, serverLevel, sourcePos);

        Component raidName = Component.literal(selectedRaid.raidId()).withStyle(ChatFormatting.GOLD);
        source.sendSuccess(() -> Component.translatable("commands.scguns.raid.started", raidName), true);

        return 1;
    }

    private static int executeSetProgression(CommandSourceStack source, ServerPlayer player, String tierName) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.translatable("commands.scguns.no_permission"));
            return 0;
        }

        GunTier tier = GunTierRegistry.getTier(tierName.toLowerCase());

        if (tier == null) {
            source.sendFailure(Component.translatable("commands.scguns.progression.invalid_tier", tierName));

            // Show available tiers
            source.sendFailure(Component.literal("Available tiers: ").withStyle(ChatFormatting.GRAY));
            StringBuilder tiersList = new StringBuilder();
            for (GunTier availableTier : GunTierRegistry.getAllTiers()) {
                if (tiersList.length() > 0) tiersList.append(", ");
                tiersList.append(availableTier.getId());
            }
            source.sendFailure(Component.literal(tiersList.toString()).withStyle(ChatFormatting.YELLOW));

            return 0;
        }

        PlayerGunProgression progression = PlayerGunProgression.get(player);
        progression.setTier(tier);
        PlayerGunProgression.save(player, progression);

        GunProgressionEventHandler.sendTierUnlockedMessage(player, tier);

        Component tierComponent = Component.translatable("gun_tier.scguns." + tier.getId())
                .withStyle(ChatFormatting.GOLD);

        source.sendSuccess(() -> Component.translatable("commands.scguns.progression.set",
                player.getDisplayName(), tierComponent), true);

        return 1;
    }

    private static int executeClearProgression(CommandSourceStack source, ServerPlayer player) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.translatable("commands.scguns.no_permission"));
            return 0;
        }

        PlayerGunProgression progression = new PlayerGunProgression();
        PlayerGunProgression.save(player, progression);

        player.sendSystemMessage(Component.translatable("commands.scguns.progression.reset")
                .withStyle(ChatFormatting.RED));

        source.sendSuccess(() -> Component.translatable("commands.scguns.progression.cleared",
                player.getDisplayName()), true);

        return 1;
    }

    private static int executeCheckProgression(CommandSourceStack source, ServerPlayer player) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.translatable("commands.scguns.no_permission"));
            return 0;
        }

        PlayerGunProgression progression = PlayerGunProgression.get(player);
        GunTier currentTier = progression.getCurrentTier();
        int raidLevel = progression.getCurrentRaidLevel();
        List<GunTier> availableTiers = progression.getAvailableMobTiers();

        Component tierComponent = Component.translatable("gun_tier.scguns." + currentTier.getId())
                .withStyle(ChatFormatting.GOLD);

        source.sendSuccess(() -> Component.literal("Player: ").append(player.getDisplayName()), false);
        source.sendSuccess(() -> Component.literal("Gun Tier: ").append(tierComponent)
                .append(" (Level " + currentTier.getLevel() + ")").withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("Raid Level: " + raidLevel).withStyle(ChatFormatting.AQUA), false);

        if (!availableTiers.isEmpty()) {
            Component mobTiersMessage = Component.literal("Available Mob Tiers: ");
            for (int i = 0; i < availableTiers.size(); i++) {
                GunTier mobTier = availableTiers.get(i);
                Component mobTierComponent = Component.translatable("gun_tier.scguns." + mobTier.getId())
                        .withStyle(ChatFormatting.RED);

                mobTiersMessage = mobTiersMessage.copy().append(mobTierComponent);

                if (i < availableTiers.size() - 1) {
                    mobTiersMessage = mobTiersMessage.copy().append(", ");
                }
            }

            Component finalMobTiersMessage = mobTiersMessage;
            source.sendSuccess(() -> finalMobTiersMessage, false);
        }

        List<RaidConfig.RaidData> availableRaids = RaidConfig.getRaidsForLevel(raidLevel);
        if (!availableRaids.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Available Raids (" + availableRaids.size() + "):").withStyle(ChatFormatting.GREEN), false);
            for (RaidConfig.RaidData raid : availableRaids) {
                String levelStr = raid.raidLevel() != null ? " (Level " + raid.raidLevel() + ")" : "";
                source.sendSuccess(() -> Component.literal("  - " + raid.raidId() + levelStr), false);
            }
        }

        return 1;
    }

    private static int executeStopAllRaids(CommandSourceStack source) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.translatable("commands.scguns.no_permission"));
            return 0;
        }

        ServerLevel serverLevel = source.getLevel();

        RaidManager manager = RaidManager.get(serverLevel);
        Collection<ActiveRaid> activeRaids = manager.getActiveRaids();

        if (activeRaids.isEmpty()) {
            source.sendFailure(Component.translatable("commands.scguns.raid.none_active"));
            return 0;
        }

        int count = 0;
        for (ActiveRaid raid : new ArrayList<>(activeRaids)) {
            raid.endRaid(false);
            count++;
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.translatable("commands.scguns.raid.stopped", finalCount), true);

        return 1;
    }

    private static int executeListRaids(CommandSourceStack source) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.translatable("commands.scguns.no_permission"));
            return 0;
        }

        ServerLevel serverLevel = source.getLevel();

        RaidManager manager = RaidManager.get(serverLevel);
        Collection<ActiveRaid> activeRaids = manager.getActiveRaids();

        if (activeRaids.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.scguns.raid.list_none"), false);
            return 1;
        }

        source.sendSuccess(() -> Component.translatable("commands.scguns.raid.list_header")
                .withStyle(ChatFormatting.GOLD), false);

        for (ActiveRaid raid : activeRaids) {
            Integer raidLevel = raid.getRaidLevel();
            Component raidInfo;

            if (raidLevel != null) {
                raidInfo = Component.literal(raid.getConfig().raidId() + " (Level " + raidLevel + ")")
                        .withStyle(ChatFormatting.YELLOW);
            } else {
                raidInfo = Component.literal(raid.getConfig().raidId() + " (Custom)")
                        .withStyle(ChatFormatting.AQUA);
            }

            int henchmenCount = raid.getAliveHenchmenCount();
            long duration = raid.getRaidDuration() / 20;

            Component finalRaidInfo = raidInfo;
            source.sendSuccess(() -> Component.translatable("commands.scguns.raid.list_entry",
                    finalRaidInfo, henchmenCount, duration), false);
        }

        return 1;
    }
}