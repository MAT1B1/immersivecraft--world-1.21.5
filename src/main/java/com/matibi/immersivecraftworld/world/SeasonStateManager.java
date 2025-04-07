package com.matibi.immersivecraftworld.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.Objects;

public class SeasonStateManager {
    private static final String KEY = "saison_state";

    public static SeasonState getOrCreateSeasonState(MinecraftServer server) {
        return Objects.requireNonNull(server.getWorld(World.OVERWORLD)).getPersistentStateManager().getOrCreate(SeasonState.TYPE);
    }

}
