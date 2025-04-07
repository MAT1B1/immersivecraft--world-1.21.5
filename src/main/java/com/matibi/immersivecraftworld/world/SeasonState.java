package com.matibi.immersivecraftworld.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class SeasonState extends PersistentState {
    private int currentSeason;
    private int ticksSinceSeasonStart;

    // --- Codec pour la persistance
    public static final Codec<SeasonState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("season").forGetter(state -> state.currentSeason),
            Codec.INT.fieldOf("ticks").forGetter(state -> state.ticksSinceSeasonStart)
    ).apply(instance, SeasonState::new));

    // --- Type Fabric moderne
    public static final PersistentStateType<SeasonState> TYPE = new PersistentStateType<>(
            "season_state",
            SeasonState::new,  // <- Ce constructeur-là
            CODEC,
            DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES
    );

    // --- Accès
    public static SeasonState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    // --- Méthodes utiles
    public void setSeason(int season) {
        this.currentSeason = season;
        this.ticksSinceSeasonStart = 0;
        markDirty();
    }

    public SeasonState() {
        this(0, 0);
    }

    public SeasonState(int season, int ticks) {
        this.currentSeason = season;
        this.ticksSinceSeasonStart = ticks;
    }

    public void tick() {
        this.ticksSinceSeasonStart++;
        markDirty();
    }

    public int getSeason() {
        return currentSeason;
    }

    public int getTicksSinceSeasonStart() {
        return ticksSinceSeasonStart;
    }
}
