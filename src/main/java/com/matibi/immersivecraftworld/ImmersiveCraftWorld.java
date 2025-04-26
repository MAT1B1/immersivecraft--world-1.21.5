package com.matibi.immersivecraftworld;

import com.matibi.immersivecraftworld.world.WorldGrowthManager;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImmersiveCraftWorld implements ModInitializer {
	public static final String MOD_ID = "immersivecraft--world";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WorldGrowthManager.register();

	}
}