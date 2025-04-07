package com.matibi.immersivecraftworld.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class SeasonManager {
    private static final int TICKS_PER_SEASON = 24000;

    public static void tick(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        if (world == null) return;

        SeasonState state = SeasonState.get(world);
        state.tick();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(Text.literal("Season " + state.getSeason() + ", ticks : " + state.getTicksSinceSeasonStart()), true);
        }

        if (state.getTicksSinceSeasonStart() >= TICKS_PER_SEASON) {
            int newSeason = (state.getSeason() + 1) % 4; // 4 saisons (0 à 3)
            state.setSeason(newSeason);
            onSeasonChange(server, newSeason); // Hook pour que tu fasses des actions (plantes, météo, etc.)
        }
    }

    private static void onSeasonChange(MinecraftServer server, int newSeason) {
        // Tu peux logguer, déclencher des modifications de biomes, de blocs, etc.
        System.out.println("Nouvelle saison : " + seasonName(newSeason));
        // Exemples à compléter :
        // - updateGlobalWeather(server, newSeason);
        // - applySeasonalEffects(server, newSeason);
    }

    public static String seasonName(int season) {
        return switch (season) {
            case 0 -> "Printemps";
            case 1 -> "Été";
            case 2 -> "Automne";
            case 3 -> "Hiver";
            default -> "Inconnu";
        };
    }
}