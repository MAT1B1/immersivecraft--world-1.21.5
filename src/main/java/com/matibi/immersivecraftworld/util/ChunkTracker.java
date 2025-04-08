package com.matibi.immersivecraftworld.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;

public class ChunkTracker {
    private static final Set<ChunkPos> loadedChunks = new HashSet<>();

    public static void init() {
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) ->
                loadedChunks.add(chunk.getPos()));

        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) ->
                loadedChunks.remove(chunk.getPos()));
    }

    public static Set<ChunkPos> getLoadedChunks() {
        return loadedChunks;
    }
}
