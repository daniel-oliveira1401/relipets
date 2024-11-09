package net.daniel.relipets.registries;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class C2SPacketHandlers {

    public static final Identifier TOGGLE_SUMMON_PET = new Identifier(Relipets.MOD_ID, "toggle_summon_pet");
    public static final Identifier CYCLE_PET_SLOT = new Identifier(Relipets.MOD_ID, "cycle_pet_slot");

    public static void onInitialize(){

        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_SUMMON_PET, (server, player, handler, buf, responseSender) -> {

            PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

            petOwnerSystem.getPetParty().toggleSummonSelectedPet((ServerWorld) player.getWorld(), player.raycast(30, 1, false).getPos(), player);

        });

        ServerPlayNetworking.registerGlobalReceiver(CYCLE_PET_SLOT, (server, player, handler, buf, responseSender) -> {

            PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
            int direction = buf.readInt();
            System.out.println("Received direction in server " + direction);
            petOwnerSystem.getPetParty().cyclePetSlot(direction);

        });

    }

}
