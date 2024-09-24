package net.daniel.relipets;

import net.daniel.relipets.registries.RelipetsItemRegistry;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Relipets implements ModInitializer {

	public static final String AIR_TYPE = "Air";
	public static final String EARTH_TYPE = "Earth";
	public static final String ICE_TYPE = "Ice";

	public static final String MOD_ID = "relipets";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public Relipets() {
		
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		RelipetsItemRegistry.initialize();
	}
}