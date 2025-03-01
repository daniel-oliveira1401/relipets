package net.daniel.relipets.registries;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.PetParty;
import net.daniel.relipets.cca_components.pet_management.event.PetPartyUpdateNotifier;
import net.daniel.relipets.cca_components.pet_management.progression.StatsEnum;
import net.daniel.relipets.cca_components.pet_management.progression.StatsOperationEnum;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class C2SPacketHandlers {

    public static final Identifier TOGGLE_SUMMON_PET = new Identifier(Relipets.MOD_ID, "toggle_summon_pet");
    public static final Identifier CYCLE_PET_SLOT = new Identifier(Relipets.MOD_ID, "cycle_pet_slot");
    public static final Identifier STAT_POINT_CHANGE = new Identifier(Relipets.MOD_ID, "stat_point_change");
    public static final Identifier REORDER_PETS = new Identifier(Relipets.MOD_ID, "reorder_pets");

    public static void onInitialize(){

        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_SUMMON_PET, (server, player, handler, buf, responseSender) -> {

            PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

            petOwnerSystem.getPetParty().toggleSummonSelectedPet((ServerWorld) player.getWorld(), player.raycast(30, 1, false).getPos(), player);

        });

        ServerPlayNetworking.registerGlobalReceiver(STAT_POINT_CHANGE, (server, player, handler, buf, responseSender) -> {

            //read operation and stat from buf
            StatsOperationEnum operation = StatsOperationEnum.valueOf(buf.readString());
            StatsEnum stat = StatsEnum.valueOf(buf.readString());

            PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
            PetData petData = petOwnerComponent.getPetParty().getSelectedPet();

            if(petData != null){
                petData.changeStatPoint(operation, stat, player.getWorld());
                petOwnerComponent.onPartyModified();
            }

        });

        ServerPlayNetworking.registerGlobalReceiver(REORDER_PETS, (server, player, handler, buf, responseSender) -> {

            //read operation and stat from buf
            int originIndex = buf.readInt();
            int destinationIndex = buf.readInt();

            PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
            petOwnerComponent.getPetParty().reorderPets(originIndex, destinationIndex);

        });

        ServerPlayNetworking.registerGlobalReceiver(CYCLE_PET_SLOT, (server, player, handler, buf, responseSender) -> {

            PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
            int direction = buf.readInt();
            petOwnerSystem.getPetParty().cyclePetSlot(direction);

        });

    }

}
