package com.matibi.immersivecraftworld;

import net.fabricmc.api.ClientModInitializer;

public class ImmersiveCraftWorldClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ImmersiveCraftWorld.LOGGER.info("Client initialization for " + ImmersiveCraftWorld.MOD_ID);
    }
}
