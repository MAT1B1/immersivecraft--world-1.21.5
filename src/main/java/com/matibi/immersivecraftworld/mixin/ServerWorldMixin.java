package com.matibi.immersivecraftworld.mixin;

import com.matibi.immersivecraftworld.world.SeasonState;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(method = "wakeSleepingPlayers", at = @At("HEAD"))
    private void onWakeSleepingPlayers(CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        SeasonState state = SeasonState.get(world);

        long toNextDay = 24000 - (state.getTicksSinceSeasonStart() % 24000);
        state.setTicksSinceSeasonStart(state.getTicksSinceSeasonStart() + (int) toNextDay);
    }
}