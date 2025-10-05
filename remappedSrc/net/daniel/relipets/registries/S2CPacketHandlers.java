package net.daniel.relipets.registries;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.PetParty;
import net.daniel.relipets.cca_components.pet_management.event.PetPartyUpdateNotifier;
import net.daniel.relipets.cca_components.pet_management.progression.StatsEnum;
import net.daniel.relipets.cca_components.pet_management.progression.StatsOperationEnum;
import net.daniel.relipets.gui.screen.PetSpectatorScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class S2CPacketHandlers {

    public static final Identifier PARTY_UPDATE = Identifier.of(Relipets.MOD_ID, "party_update");
    public static final Identifier REOPEN_SPECTATOR_SCREEN = Identifier.of(Relipets.MOD_ID, "reopen_spectator_screen");

    public static void onInitialize(){

        ClientPlayNetworking.registerGlobalReceiver(S2CPayloads.S2CPartyUpdatePayload.ID, (payload, context) -> {
            PetParty party = payload.petParty();
            party.player = context.player();

            context.client().execute(()-> {
                PetPartyUpdateNotifier.getInstance().emit(party);
            });

        });

        ClientPlayNetworking.registerGlobalReceiver(S2CPayloads.S2CReopenSpectatorScreenPayload.ID, (payload, context) -> {

            if(context.player() == null) return;

            int slot = payload.slot();

            PetData petData = payload.petData();

            context.client().execute(()-> {
                context.client().setScreen(new PetSpectatorScreen(slot, petData));
            });

        });

    }

}
