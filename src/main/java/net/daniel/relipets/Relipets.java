package net.daniel.relipets;

import net.daniel.relipets.config.RelipetsConfig;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.registries.C2SPacketHandlers;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.RelipetsEntityRegistry;
import net.daniel.relipets.registries.RelipetsItemRegistry;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Relipets implements ModInitializer {

	public static final String MOD_ID = "relipets";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final RelipetsConfig CONFIG = RelipetsConfig.createAndLoad();

	public Relipets() {
		
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		RelipetsItemRegistry.onInitialize();
		CardinalComponentsRegistry.onInitialize();

		RelipetsEntityRegistry.onInitialize();

		C2SPacketHandlers.onInitialize();

	}
}