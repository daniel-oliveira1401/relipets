package net.daniel.relipets.registries;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.PetMoveMode;
import net.daniel.relipets.cca_components.pet_management.PetParty;
import net.daniel.relipets.cca_components.pet_management.event.PetPartyUpdateNotifier;
import net.daniel.relipets.cca_components.pet_management.progression.StatsEnum;
import net.daniel.relipets.cca_components.pet_management.progression.StatsOperationEnum;
import net.daniel.relipets.entity.cores.YellowCore;
import net.daniel.relipets.gui.screen.PartManagementScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class C2SPacketHandlers {

    public static final Identifier TOGGLE_SUMMON_PET = new Identifier(Relipets.MOD_ID, "toggle_summon_pet");
    public static final Identifier CYCLE_PET_SLOT = new Identifier(Relipets.MOD_ID, "cycle_pet_slot");
    public static final Identifier STAT_POINT_CHANGE = new Identifier(Relipets.MOD_ID, "stat_point_change");

    public static final Identifier CREATE_GROUP = new Identifier(Relipets.MOD_ID, "create_group");
    public static final Identifier REMOVE_GROUP = new Identifier(Relipets.MOD_ID, "remove_group");
    public static final Identifier ADD_SLOT_TO_GROUP = new Identifier(Relipets.MOD_ID, "add_slot_to_group");
    public static final Identifier REMOVE_SLOT_FROM_GROUP = new Identifier(Relipets.MOD_ID, "remove_slot_from_group");
    public static final Identifier CHANGE_GROUP_COLOR = new Identifier(Relipets.MOD_ID, "change_group_color");
    public static final Identifier CHANGE_GROUP_NAME = new Identifier(Relipets.MOD_ID, "change_group_name");
    public static final Identifier SUMMON_GROUP = new Identifier(Relipets.MOD_ID, "summon_group");
    public static final Identifier RECALL_GROUP = new Identifier(Relipets.MOD_ID, "recall_group");

    public static final Identifier REORDER_PETS = new Identifier(Relipets.MOD_ID, "reorder_pets");
    public static final Identifier SELECT_PET = new Identifier(Relipets.MOD_ID, "select_pet");
    public static final Identifier BOOST_PET_FLIGHT = new Identifier(Relipets.MOD_ID, "boost_pet_flight");
    public static final Identifier RECALL_ALL_PETS = new Identifier(Relipets.MOD_ID, "recall_all_pets");
    public static final Identifier RENAME_PET = new Identifier(Relipets.MOD_ID, "rename_pet");
    public static final Identifier RELEASE_PET = new Identifier(Relipets.MOD_ID, "release_pet");
    public static final Identifier RECOVER_PET = new Identifier(Relipets.MOD_ID, "recover_pet");
    public static final Identifier CHANGE_MOVE_MODE = new Identifier(Relipets.MOD_ID, "change_move_mode");
    public static final Identifier CYCLE_GROUP_MOVE_MODE = new Identifier(Relipets.MOD_ID, "cycle_group_move_mode");;
    public static final Identifier OPEN_PART_MANAGEMENT_SCREEN = new Identifier(Relipets.MOD_ID, "open_part_management_screen");
    public static final Identifier LOAD_AREA_AROUND_PET = new Identifier(Relipets.MOD_ID, "load_area_around_pet");
    public static final Identifier UNLOAD_AREA_AROUND_PET = new Identifier(Relipets.MOD_ID, "unload_area_around_pet");
    public static final Identifier GET_PARTY = new Identifier(Relipets.MOD_ID, "get_party");

    public static void onInitialize(){

        ServerPlayNetworking.registerGlobalReceiver(OPEN_PART_MANAGEMENT_SCREEN, (server, player, handler, buf, responseSender) -> {
            int selectedSlot = buf.readInt();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                PetData petData = petOwnerSystem.getPetParty().getSlotManager().getSlotAt(selectedSlot).getContent();
                if(petData != null){
                    player.openHandledScreen(new NamedScreenHandlerFactory() {
                        @Override
                        public Text getDisplayName() {
                            return Text.of("Part Management");
                        }

                        @Override
                        public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                            return new PartManagementScreenHandler(syncId, playerInventory, player,
                                    petData,
                                    selectedSlot);
                        }
                    });
                }

            });

        });

        ServerPlayNetworking.registerGlobalReceiver(LOAD_AREA_AROUND_PET, (server, player, handler, buf, responseSender) -> {

            int slot = buf.readInt();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                PetData pet = petOwnerSystem.getPetParty().getSlotManager().getSlotAt(slot).getContent();
                if(pet != null){
                    petOwnerSystem.getPetParty().loadAreaAroundPet(slot, player);
                }
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(GET_PARTY, (server, player, handler, buf, responseSender) -> {

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                petOwnerSystem.getPetParty().pushChangesToClient();
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(SELECT_PET, (server, player, handler, buf, responseSender) -> {

            int slot = buf.readInt();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                petOwnerSystem.getPetParty().setSelectedPetIndex(slot);

            });

        });

        ServerPlayNetworking.registerGlobalReceiver(UNLOAD_AREA_AROUND_PET, (server, player, handler, buf, responseSender) -> {

            int slot = buf.readInt();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                PetData pet = petOwnerSystem.getPetParty().getSlotManager().getSlotAt(slot).getContent();
                if(pet != null){
                    petOwnerSystem.getPetParty().unloadAreaAroundPet(slot, player);
                }
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(RELEASE_PET, (server, player, handler, buf, responseSender) -> {

            int slot = buf.readInt();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                PetData pet = petOwnerSystem.getPetParty().getSlotManager().getSlotAt(slot).getContent();
                if(pet != null){
                    petOwnerSystem.getPetParty().releasePetFromParty(pet, server);
                }
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(CYCLE_GROUP_MOVE_MODE, (server, player, handler, buf, responseSender) -> {

            String stringUuid = buf.readString();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                petOwnerSystem.getPetParty().cycleGroupMoveMode(stringUuid);

                petOwnerSystem.getPetParty().pushChangesToClient();
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(CHANGE_MOVE_MODE, (server, player, handler, buf, responseSender) -> {

            int slot = buf.readInt();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                PetData pet = petOwnerSystem.getPetParty().getSlotManager().getSlotAt(slot).getContent();

                if(pet != null){
                    petOwnerSystem.getPetParty().cyclePetMoveMode(pet);
                }

                petOwnerSystem.getPetParty().pushChangesToClient();
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(RECOVER_PET, (server, player, handler, buf, responseSender) -> {

            int slot = buf.readInt();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                PetData pet = petOwnerSystem.getPetParty().getSlotManager().getSlotAt(slot).getContent();
                if(pet != null){
                    pet.forceSummon((ServerWorld) player.getWorld(), player.getPos(), player);
                }
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(RENAME_PET, (server, player, handler, buf, responseSender) -> {

            int slot = buf.readInt();
            String name = buf.readString();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                petOwnerSystem.getPetParty().renamePet(slot, name, server);


            });

        });

        ServerPlayNetworking.registerGlobalReceiver(RECALL_ALL_PETS, (server, player, handler, buf, responseSender) -> {
            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                petOwnerSystem.getPetParty().recallAllPets((ServerWorld) player.getWorld(), player);


            });

        });

        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_SUMMON_PET, (server, player, handler, buf, responseSender) -> {
            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                petOwnerSystem.getPetParty().toggleSummonSelectedPet((ServerWorld) player.getWorld(), player.raycast(30, 1, false).getPos(), player);


            });

        });

        ServerPlayNetworking.registerGlobalReceiver(STAT_POINT_CHANGE, (server, player, handler, buf, responseSender) -> {

            StatsOperationEnum operation = StatsOperationEnum.valueOf(buf.readString());
            StatsEnum stat = StatsEnum.valueOf(buf.readString());
            server.execute(()-> {

                //read operation and stat from buf

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                PetData petData = petOwnerComponent.getPetParty().getSelectedPet();

                if(petData != null){
                    petData.changeStatPoint(petOwnerComponent.getPetParty(), operation, stat, (ServerWorld) player.getWorld());
                    petOwnerComponent.onPartyModified();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(CREATE_GROUP, (server, player, handler, buf, responseSender) -> {

            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().getPetGroupManager().addGroup();
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(REMOVE_GROUP, (server, player, handler, buf, responseSender) -> {

            String stringUuid = buf.readString();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().getPetGroupManager().removeGroup(UUID.fromString(stringUuid));
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(SUMMON_GROUP, (server, player, handler, buf, responseSender) -> {
            String stringUuid = buf.readString();
            server.execute(()-> {
                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().summonGroup(UUID.fromString(stringUuid), (ServerWorld) player.getWorld(), player.getPos() ,player);
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }

            });



        });

        ServerPlayNetworking.registerGlobalReceiver(RECALL_GROUP, (server, player, handler, buf, responseSender) -> {

            String stringUuid = buf.readString();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().recallGroup(UUID.fromString(stringUuid), (ServerWorld) player.getWorld(), player.getPos() ,player);
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(CHANGE_GROUP_COLOR, (server, player, handler, buf, responseSender) -> {

            String stringUuid = buf.readString();
            int color = buf.readInt();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().getPetGroupManager().changeGroupColor(UUID.fromString(stringUuid), color);
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(CHANGE_GROUP_NAME, (server, player, handler, buf, responseSender) -> {

            String stringUuid = buf.readString();
            String name = buf.readString();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().getPetGroupManager().changeGroupName(UUID.fromString(stringUuid), name);
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });



        });

        ServerPlayNetworking.registerGlobalReceiver(ADD_SLOT_TO_GROUP, (server, player, handler, buf, responseSender) -> {

            String stringUuid = buf.readString();
            int slotIndex = buf.readInt();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().getPetGroupManager().addSlotToGroup(UUID.fromString(stringUuid), slotIndex);
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(REMOVE_SLOT_FROM_GROUP, (server, player, handler, buf, responseSender) -> {

            String stringUuid = buf.readString();
            int slotIndex = buf.readInt();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().getPetGroupManager().removeSlotFromGroup(UUID.fromString(stringUuid), slotIndex);
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(REORDER_PETS, (server, player, handler, buf, responseSender) -> {
            int originIndex = buf.readInt();
            int destinationIndex = buf.readInt();
            server.execute(()-> {

                //read operation and stat from buf

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                petOwnerComponent.getPetParty().reorderPets(originIndex, destinationIndex);


            });

        });

        ServerPlayNetworking.registerGlobalReceiver(CYCLE_PET_SLOT, (server, player, handler, buf, responseSender) -> {

            int direction = buf.readInt();
            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                petOwnerSystem.getPetParty().cyclePetSlot(direction, server);


            });

        });

        ServerPlayNetworking.registerGlobalReceiver(BOOST_PET_FLIGHT, (server, player, handler, buf, responseSender) -> {

            server.execute(()-> {

                if(player.getVehicle() instanceof YellowCore core){
                    core.boost();
                }


            });

        });

    }

}
