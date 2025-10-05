package net.daniel.relipets.registries;

import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.PetParty;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.Objects;

public class S2CPayloads {

    public record S2CPartyUpdatePayload(PetParty petParty) implements CustomPayload {

        public static final Id<S2CPartyUpdatePayload> ID = new Id<>(S2CPacketHandlers.PARTY_UPDATE);
        public static final PacketCodec<RegistryByteBuf, S2CPartyUpdatePayload> CODEC = PacketCodec.of((payload, buf) -> {
            buf.writeNbt(payload.petParty().writeToNbt());
        }, buf -> {
            PetParty party = new PetParty(null);
            party.readFromNbt(Objects.requireNonNull(buf.readNbt()));

            return new S2CPartyUpdatePayload(party);
        });


        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record S2CReopenSpectatorScreenPayload(int slot, PetData petData) implements CustomPayload {

        public static final Id<S2CReopenSpectatorScreenPayload> ID = new Id<>(S2CPacketHandlers.REOPEN_SPECTATOR_SCREEN);
        public static final PacketCodec<RegistryByteBuf, S2CReopenSpectatorScreenPayload> CODEC = PacketCodec.of((payload, buf) -> {
            buf.writeInt(payload.slot());
            buf.writeNbt(payload.petData().writeToNbt());
        }, buf -> {
            int slot = buf.readInt();

            PetData petData = new PetData();
            petData.readFromNbt(Objects.requireNonNull(buf.readNbt()));

            return new S2CReopenSpectatorScreenPayload(slot, petData);
        });
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void onInitialize(){
        PayloadTypeRegistry.playS2C().register(S2CPartyUpdatePayload.ID, S2CPartyUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(S2CReopenSpectatorScreenPayload.ID, S2CReopenSpectatorScreenPayload.CODEC);
    }

}
