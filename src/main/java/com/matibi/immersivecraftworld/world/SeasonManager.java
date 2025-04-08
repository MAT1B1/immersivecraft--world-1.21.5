package com.matibi.immersivecraftworld.world;

import com.matibi.immersivecraftworld.command.SeasonCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class SeasonManager {
    private static final int DAYS_PER_SEASON = 10;

    public static void register(){
        ServerTickEvents.END_SERVER_TICK.register(SeasonManager::tick);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SeasonCommand.register(dispatcher));
    }

    public static void tick(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        if (world == null) return;

        SeasonState state = SeasonState.get(world);
        state.tick();

        if (state.getTicksSinceSeasonStart() >= DAYS_PER_SEASON * 24000) {
            int newSeason = (state.getSeason() + 1) % 4;
            state.setSeason(newSeason);
            onSeasonChange(server, newSeason);
        }
    }

    public static void onSeasonChange(MinecraftServer server, int newSeason) {
        ServerWorld world = server.getOverworld();
    }

    public static String seasonName(int season) {
        return switch (season) {
            case 0 -> "Spring";
            case 1 -> "Summer";
            case 2 -> "Autumn";
            case 3 -> "Winter";
            default -> "Unknown";
        };
    }
}