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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class C2SPacketHandlers {

    public static final Identifier TOGGLE_SUMMON_PET = Identifier.of(Relipets.MOD_ID, "toggle_summon_pet");
    public static final Identifier CYCLE_PET_SLOT = Identifier.of(Relipets.MOD_ID, "cycle_pet_slot");
    public static final Identifier STAT_POINT_CHANGE = Identifier.of(Relipets.MOD_ID, "stat_point_change");

    public static final Identifier CREATE_GROUP = Identifier.of(Relipets.MOD_ID, "create_group");
    public static final Identifier REMOVE_GROUP = Identifier.of(Relipets.MOD_ID, "remove_group");
    public static final Identifier ADD_SLOT_TO_GROUP = Identifier.of(Relipets.MOD_ID, "add_slot_to_group");
    public static final Identifier REMOVE_SLOT_FROM_GROUP = Identifier.of(Relipets.MOD_ID, "remove_slot_from_group");
    public static final Identifier CHANGE_GROUP_COLOR = Identifier.of(Relipets.MOD_ID, "change_group_color");
    public static final Identifier CHANGE_GROUP_NAME = Identifier.of(Relipets.MOD_ID, "change_group_name");
    public static final Identifier SUMMON_GROUP = Identifier.of(Relipets.MOD_ID, "summon_group");
    public static final Identifier RECALL_GROUP = Identifier.of(Relipets.MOD_ID, "recall_group");

    public static final Identifier REORDER_PETS = Identifier.of(Relipets.MOD_ID, "reorder_pets");
    public static final Identifier SELECT_PET = Identifier.of(Relipets.MOD_ID, "select_pet");
    public static final Identifier BOOST_PET_FLIGHT = Identifier.of(Relipets.MOD_ID, "boost_pet_flight");
    public static final Identifier RECALL_ALL_PETS = Identifier.of(Relipets.MOD_ID, "recall_all_pets");
    public static final Identifier RENAME_PET = Identifier.of(Relipets.MOD_ID, "rename_pet");
    public static final Identifier RELEASE_PET = Identifier.of(Relipets.MOD_ID, "release_pet");
    public static final Identifier RECOVER_PET = Identifier.of(Relipets.MOD_ID, "recover_pet");
    public static final Identifier CHANGE_MOVE_MODE = Identifier.of(Relipets.MOD_ID, "change_move_mode");
    public static final Identifier CYCLE_GROUP_MOVE_MODE = Identifier.of(Relipets.MOD_ID, "cycle_group_move_mode");;
    public static final Identifier OPEN_PART_MANAGEMENT_SCREEN = Identifier.of(Relipets.MOD_ID, "open_part_management_screen");
    public static final Identifier LOAD_AREA_AROUND_PET = Identifier.of(Relipets.MOD_ID, "load_area_around_pet");
    public static final Identifier UNLOAD_AREA_AROUND_PET = Identifier.of(Relipets.MOD_ID, "unload_area_around_pet");
    public static final Identifier GET_PARTY = Identifier.of(Relipets.MOD_ID, "get_party");

    public static void onInitialize(){

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.OpenPartManagementScreen.ID, (payload, context) -> {
            int selectedSlot = payload.selectedSlot();

            context.server().execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(context.player());

                PetData petData = petOwnerSystem.getPetParty().getSlotManager().getSlotAt(selectedSlot).getContent();
                if(petData != null){
                    context.player().openHandledScreen(new NamedScreenHandlerFactory() {
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

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.LoadAreaAroundPet.ID, (payload, context) -> {

            int slot = payload.slot();
            ServerPlayerEntity player = context.player();
            var server = context.server();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                PetData pet = petOwnerSystem.getPetParty().getSlotManager().getSlotAt(slot).getContent();
                if(pet != null){
                    petOwnerSystem.getPetParty().loadAreaAroundPet(slot, player);
                }
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.GetParty.ID, (payload, context) -> {
            var server = context.server();
            PlayerEntity player = context.player();
            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                petOwnerSystem.getPetParty().pushChangesToClient();
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.SelectPet.ID, (payload, context) -> {
            var server = context.server();
            PlayerEntity player = context.player();
            int slot = payload.slot();
            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                petOwnerSystem.getPetParty().setSelectedPetIndex(slot);

            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.UnloadAreaAroundPet.ID, (payload, context) -> {

            int slot = payload.slot();
            ServerPlayerEntity player = context.player();
            var server = context.server();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                PetData pet = petOwnerSystem.getPetParty().getSlotManager().getSlotAt(slot).getContent();
                if(pet != null){
                    petOwnerSystem.getPetParty().unloadAreaAroundPet(slot, player);
                }
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.ReleasePet.ID, (payload, context) -> {

            int slot = payload.slot();
            PlayerEntity player = context.player();
            var server = context.server();
            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                PetData pet = petOwnerSystem.getPetParty().getSlotManager().getSlotAt(slot).getContent();
                if(pet != null){
                    petOwnerSystem.getPetParty().releasePetFromParty(pet, server);
                }

            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.CycleGroupMoveMode.ID, (payload, context) -> {

            String stringUuid = payload.stringUuid();
            PlayerEntity player = context.player();
            var server = context.server();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                petOwnerSystem.getPetParty().cycleGroupMoveMode(stringUuid);

                petOwnerSystem.getPetParty().pushChangesToClient();
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.ChangeMoveMode.ID, (payload, context) -> {

            int slot = payload.slot();
            PlayerEntity player = context.player();
            var server = context.server();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                PetData pet = petOwnerSystem.getPetParty().getSlotManager().getSlotAt(slot).getContent();

                if(pet != null){
                    petOwnerSystem.getPetParty().cyclePetMoveMode(pet);
                }

                petOwnerSystem.getPetParty().pushChangesToClient();
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.RecoverPet.ID, (payload, context) -> {

            int slot = payload.slot();
            PlayerEntity player = context.player();
            var server = context.server();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                PetData pet = petOwnerSystem.getPetParty().getSlotManager().getSlotAt(slot).getContent();
                if(pet != null){
                    pet.forceSummon((ServerWorld) player.getWorld(), player.getPos(), player);
                }
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.RenamePet.ID, (payload, context) -> {

            int slot = payload.slot();
            String name = payload.name();
            PlayerEntity player = context.player();
            var server = context.server();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                petOwnerSystem.getPetParty().renamePet(slot, name, server);


            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.RecallAllPets.ID, (payload, context) -> {
            PlayerEntity player = context.player();
            var server = context.server();
            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                petOwnerSystem.getPetParty().recallAllPets((ServerWorld) player.getWorld(), player);


            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.ToggleSummonPet.ID, (payload, context) -> {

            boolean spawnAtPlayerPos = payload.spawnAtPlayerPos();
            PlayerEntity player = context.player();
            var server = context.server();

            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                Vec3d pos;

                if(spawnAtPlayerPos){
                    pos = player.getBlockPos().up().toCenterPos();
                }else{
                    pos = player.raycast(30, 1, false).getPos();
                }

                petOwnerSystem.getPetParty().toggleSummonSelectedPet((ServerWorld) player.getWorld(), pos, player);


            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.StatPointChange.ID, (payload, context) -> {
            PlayerEntity player = context.player();
            var server = context.server();

            StatsOperationEnum operation = StatsOperationEnum.valueOf(payload.operation());
            StatsEnum stat = StatsEnum.valueOf(payload.stat());
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

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.CreateGroup.ID, (payload, context) -> {
            PlayerEntity player = context.player();
            var server = context.server();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().getPetGroupManager().addGroup();
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.RemoveGroup.ID, (payload, context) -> {

            String stringUuid = payload.stringUuid();
            PlayerEntity player = context.player();
            var server = context.server();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().getPetGroupManager().removeGroup(UUID.fromString(stringUuid));
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.SummonGroup.ID, (payload, context) -> {
            String stringUuid = payload.stringUuid();
            PlayerEntity player = context.player();
            var server = context.server();
            server.execute(()-> {
                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().summonGroup(UUID.fromString(stringUuid), (ServerWorld) player.getWorld(), player.getPos() ,player);
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });



        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.RecallGroup.ID, (payload, context) -> {

            String stringUuid = payload.stringUuid();
            PlayerEntity player = context.player();
            var server = context.server();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().recallGroup(UUID.fromString(stringUuid), (ServerWorld) player.getWorld(), player.getPos() ,player);
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.ChangeGroupColor.ID, (payload, context) -> {

            String stringUuid = payload.stringUuid();
            int color = payload.color();
            PlayerEntity player = context.player();
            var server = context.server();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().getPetGroupManager().changeGroupColor(UUID.fromString(stringUuid), color);
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.ChangeGroupName.ID, (payload, context) -> {

            PlayerEntity player = context.player();
            var server = context.server();
            String stringUuid = payload.stringUuid();
            String name = payload.name();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().getPetGroupManager().changeGroupName(UUID.fromString(stringUuid), name);
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });



        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.AddSlotToGroup.ID, (payload, context) -> {

            String stringUuid = payload.stringUuid();
            int slotIndex = payload.slotIndex();
            PlayerEntity player = context.player();
            var server = context.server();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().getPetGroupManager().addSlotToGroup(UUID.fromString(stringUuid), slotIndex);
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.RemoveSlotFromGroup.ID, (payload, context) -> {

            String stringUuid = payload.stringUuid();
            int slotIndex = payload.slotIndex();
            PlayerEntity player = context.player();
            var server = context.server();
            server.execute(()-> {

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

                if(petOwnerComponent.getPetParty().getPetGroupManager() != null){
                    petOwnerComponent.getPetParty().getPetGroupManager().removeSlotFromGroup(UUID.fromString(stringUuid), slotIndex);
                    petOwnerComponent.getPetParty().pushChangesToClient();
                }


            });


        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.ReorderPets.ID, (payload, context) -> {
            int originIndex = payload.originIndex();
            int destinationIndex = payload.destinationIndex();
            PlayerEntity player = context.player();
            var server = context.server();
            server.execute(()-> {

                //read operation and stat from buf

                PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                petOwnerComponent.getPetParty().reorderPets(originIndex, destinationIndex);


            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.CyclePetSlot.ID, (payload, context) -> {

            int direction = payload.direction();
            PlayerEntity player = context.player();
            var server = context.server();
            server.execute(()-> {

                PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                petOwnerSystem.getPetParty().cyclePetSlot(direction, server);


            });

        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPayloads.BoostPetFlight.ID, (payload, context) -> {
            PlayerEntity player = context.player();
            var server = context.server();
            server.execute(()-> {

                if(player.getVehicle() instanceof YellowCore core){
                    core.boost();
                }


            });

        });

    }

}
