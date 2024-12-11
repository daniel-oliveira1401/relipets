package net.daniel.relipets;

import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.config.RelipetsConfig;
import net.daniel.relipets.entity.brain.activity.CoreCustomActivities;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.brain.sensor.RelipetsSensorTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.events.PetFaintedCallback;
import net.daniel.relipets.registries.C2SPacketHandlers;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.RelipetsEntityRegistry;
import net.daniel.relipets.registries.RelipetsItemRegistry;
import net.daniel.relipets.utils.SetTimeoutManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;

import java.util.UUID;

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

		CoreCustomActivities.init();

		RelipetsSensorTypes.init();

		RelipetsMemoryTypes.init();

		ServerTickEvents.END_SERVER_TICK.register(server -> SetTimeoutManager.tickActiveTimeouts());

		PetFaintedCallback.EVENT.register((pet)->{
			Relipets.LOGGER.debug("Pet entity died (inside listener)");

			PetMetadataComponent petMetadataComponent = CardinalComponentsRegistry.PET_METADATA_KEY.get(pet);

			String ownerUUID = petMetadataComponent.getPlayerUUID();

			Relipets.LOGGER.debug("Owner is: " + ownerUUID);

			if(!pet.getWorld().isClient() && pet.getWorld() instanceof ServerWorld world){
				ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(UUID.fromString(ownerUUID));
				if(player != null){
					PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
					petOwnerComponent.getPetParty().onPetFainted(pet, world);

				}else{
					Relipets.LOGGER.debug("Could not find the player bound to this pet using UUID");
				}

			}

		});


	}
}