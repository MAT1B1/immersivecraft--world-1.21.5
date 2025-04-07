package com.matibi.immersivecraftworld;

import com.matibi.immersivecraftworld.command.SeasonCommand;
import com.matibi.immersivecraftworld.world.SeasonManager;
import com.matibi.immersivecraftworld.world.WorldGrowthManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImmersiveCraftWorld implements ModInitializer {
	public static final String MOD_ID = "immersivecraft--world";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WorldGrowthManager.register();
		ServerTickEvents.END_SERVER_TICK.register(SeasonManager::tick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				SeasonCommand.register(dispatcher));

	}
}