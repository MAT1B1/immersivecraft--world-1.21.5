package com.matibi.immersivecraftworld.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class SeasonManager {
    private static final int DAYS_PER_SEASON = 10;

    public static void tick(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        if (world == null) return;

        SeasonState state = SeasonState.get(world);
        state.tick();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(Text.literal("Season " + state.getSeason() + ", ticks : " + state.getTicksSinceSeasonStart()), true);
        }

        if (state.getTicksSinceSeasonStart() >= DAYS_PER_SEASON * 24000) {
            int newSeason = (state.getSeason() + 1) % 4; // 4 seasons (0 à 3)
            state.setSeason(newSeason);
            onSeasonChange(server, newSeason);
        }
    }

    private static void onSeasonChange(MinecraftServer server, int newSeason) {
        System.out.println("New season : " + seasonName(newSeason));

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