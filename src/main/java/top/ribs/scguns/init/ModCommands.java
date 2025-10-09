package top.ribs.scguns.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.entity.player.PlayerGunProgression;
import top.ribs.scguns.event.GunProgressionEventHandler;

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
                                                            for (PlayerGunProgression.GunTier tier : PlayerGunProgression.GunTier.values()) {
                                                                builder.suggest(tier.name().toLowerCase());
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
        );
    }

    private static int executeSetProgression(CommandSourceStack source, ServerPlayer player, String tierName) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.literal("You do not have permission to execute this command"));
            return 0;
        }

        PlayerGunProgression.GunTier tier;
        try {
            tier = PlayerGunProgression.GunTier.valueOf(tierName.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid tier: " + tierName));
            return 0;
        }

        PlayerGunProgression progression = PlayerGunProgression.get(player);
        progression.setTier(tier);
        PlayerGunProgression.save(player, progression);

        GunProgressionEventHandler.sendTierUnlockedMessage(player, tier);

        Component tierComponent = Component.translatable("gun_tier.scguns." + tier.name().toLowerCase())
                .withStyle(ChatFormatting.GOLD);

        source.sendSuccess(() -> Component.literal("Set ")
                .append(player.getDisplayName())
                .append("'s progression to ")
                .append(tierComponent), true);

        return 1;
    }

    private static int executeClearProgression(CommandSourceStack source, ServerPlayer player) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.literal("You do not have permission to execute this command"));
            return 0;
        }

        PlayerGunProgression progression = new PlayerGunProgression();
        PlayerGunProgression.save(player, progression);

        player.sendSystemMessage(Component.literal("Your gun progression has been reset")
                .withStyle(ChatFormatting.RED));

        source.sendSuccess(() -> Component.literal("Cleared ")
                .append(player.getDisplayName())
                .append("'s gun progression"), true);

        return 1;
    }

    private static int executeCheckProgression(CommandSourceStack source, ServerPlayer player) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.literal("You do not have permission to execute this command"));
            return 0;
        }

        PlayerGunProgression progression = PlayerGunProgression.get(player);
        PlayerGunProgression.GunTier currentTier = progression.getCurrentTier();
        List<PlayerGunProgression.GunTier> availableTiers = progression.getAvailableMobTiers();

        Component tierComponent = Component.translatable("gun_tier.scguns." + currentTier.name().toLowerCase())
                .withStyle(ChatFormatting.GOLD);

        if (availableTiers.isEmpty()) {
            source.sendSuccess(() -> player.getDisplayName()
                    .copy()
                    .append(" - Current Tier: ")
                    .append(tierComponent)
                    .append(" | Mob Tiers: None"), false);
        } else {
            Component mobTiersMessage = Component.literal("");
            for (int i = 0; i < availableTiers.size(); i++) {
                PlayerGunProgression.GunTier mobTier = availableTiers.get(i);
                Component mobTierComponent = Component.translatable("gun_tier.scguns." + mobTier.name().toLowerCase())
                        .withStyle(ChatFormatting.RED);

                mobTiersMessage = mobTiersMessage.copy().append(mobTierComponent);

                if (i < availableTiers.size() - 1) {
                    mobTiersMessage = mobTiersMessage.copy().append(", ");
                }
            }

            Component finalMobTiersMessage = mobTiersMessage;
            source.sendSuccess(() -> player.getDisplayName()
                    .copy()
                    .append(" - Current Tier: ")
                    .append(tierComponent)
                    .append(" | Mob Tiers: ")
                    .append(finalMobTiersMessage), false);
        }

        return 1;
    }
}