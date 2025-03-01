package net.daniel.relipets.registries;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.PetParty;
import net.daniel.relipets.cca_components.pet_management.event.PetPartyUpdateNotifier;
import net.daniel.relipets.cca_components.pet_management.progression.StatsEnum;
import net.daniel.relipets.cca_components.pet_management.progression.StatsOperationEnum;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class S2CPacketHandlers {

    public static final Identifier PARTY_UPDATE = new Identifier(Relipets.MOD_ID, "party_update");

    public static void onInitialize(){



        ClientPlayNetworking.registerGlobalReceiver(PARTY_UPDATE, (client, handler, buf, sender) -> {

            if(client.player == null) return;

            NbtCompound nbt = buf.readNbt();

            if(nbt == null) return;

            PetParty party = new PetParty(client.player);
            party.readFromNbt(nbt);

            PetPartyUpdateNotifier.getInstance().emit(party);


        });

    }

}
