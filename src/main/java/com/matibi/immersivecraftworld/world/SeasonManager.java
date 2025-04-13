package com.matibi.immersivecraftworld.world;

import com.matibi.immersivecraftworld.command.SeasonCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

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

        Random random = world.getRandom();
        if (SeasonManager.seasonName(state.getSeason()).equals("Winter")) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                BlockPos base = player.getBlockPos();

                for (int i = 0; i < 15; i++) {
                    int dx = random.nextBetween(-10, 11);
                    int dz = random.nextBetween(-10, 11);
                    int dy = random.nextBetween(1, 6); // simulate falling from the sky

                    BlockPos pos = base.add(dx, dy, dz);
                    if (!world.getBlockState(pos).isAir()) continue;

                    world.spawnParticles(
                            ParticleTypes.SNOWFLAKE,
                            pos.getX() + 0.5,
                            pos.getY(),
                            pos.getZ() + 0.5,
                            1, 0.2, 0.5, 0.2, 0.01
                    );
                }
            }
        }
    }

    public static void onSeasonChange(MinecraftServer server, int newSeason) {
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