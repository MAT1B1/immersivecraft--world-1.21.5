package com.matibi.immersivecraftworld.command;

import com.matibi.immersivecraftworld.world.SeasonManager;
import com.matibi.immersivecraftworld.world.SeasonState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public class SeasonCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("season")
                .requires(source -> source.hasPermissionLevel(2)) // Permission: opérateur
                .then(literal("next")
                        .executes(ctx -> {
                            SeasonState state = SeasonState.get(ctx.getSource().getServer().getOverworld());
                            int nextSeason = (state.getSeason() + 1) % 4;
                            state.setSeason(nextSeason);
                            ctx.getSource().sendFeedback(() -> Text.literal("Season changed to " + SeasonManager.seasonName(nextSeason)), true);
                            return 1;
                        })
                )
                .then(literal("set")
                        .then(CommandManager.argument("saison", StringArgumentType.word())
                                .suggests((context, builder) -> net.minecraft.command.CommandSource.suggestMatching(
                                        List.of("spring", "summer", "autumn", "winter"), builder
                                ))
                                .executes(ctx -> {
                                    String saison = StringArgumentType.getString(ctx, "saison").toLowerCase();
                                    int index = switch (saison) {
                                        case "spring" -> 0;
                                        case "summer" -> 1;
                                        case "autumn" -> 2;
                                        case "winter" -> 3;
                                        default -> -1;
                                    };

                                    if (index == -1) {
                                        ctx.getSource().sendError(Text.literal("Invalid season name: " + saison));
                                        return 0;
                                    }

                                    SeasonState state = SeasonState.get(ctx.getSource().getServer().getOverworld());
                                    state.setSeason(index);
                                    ctx.getSource().sendFeedback(() -> Text.literal("Season set to " + SeasonManager.seasonName(index)), true);
                                    return 1;
                                })
                        )
                )
        );
    }
}