package net.daniel.relipets.cca_components.pet_management;

import lombok.Getter;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.daniel.relipets.registries.RelipetsItemRegistry;
import net.daniel.relipets.registries.S2CPacketHandlers;
import net.daniel.relipets.utils.Utils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/*

How unlocking more slots will work:

    player crafts an item

    When the player uses the item, the item is consumed and the player gets one more slot

 */

public class PetParty implements ISerializable {

    public static final String SELECTED_PET_INDEX = "selected_pet_index";
    public static final String PET_GROUP_MANAGER = "pet_group_manager";

    private int slotCount = 10;

    @Getter
    int selectedPetIndex = 0;

    @Getter
    private PetSlotManager<PetData> slotManager = new PetSlotManager<>(slotCount, PetData::new);

    PetPartyEventListener onPetPartyModifiedListener;

    @Getter
    private PetGroupManager petGroupManager = new PetGroupManager();

    private PlayerEntity player;
    private static final int baseSlotCount = 10;
    int partyUpdateCooldown = 0;
    int petSummonCooldown = 0;
    int naturalHealingCooldown = 0;

    public PetParty(PlayerEntity player){
        this.player = player;
    }

    public void tick(ServerWorld world){

        List<PetSlot<PetData>> slotsWithPets = this.getSlotManager().getSlotsWithContent();
        List<PetData> summonedPets = getSummonedPets(slotsWithPets);

        tickPets(slotsWithPets, world);

        updateParty(slotsWithPets);

        applyHealingToSummonedPetsIfPossible(summonedPets);

        tickCooldowns();

    }

    private List<PetData> getSummonedPets(List<PetSlot<PetData>> slotsWithPets){
        return slotsWithPets.stream()
                .filter((s)-> s.getContent() != null && s.getContent().isSummoned())
                .map(PetSlot::getContent).toList();
    }

    public void tickCooldowns(){
        partyUpdateCooldown = Math.max(partyUpdateCooldown - 1, 0);

        petSummonCooldown = Math.max(petSummonCooldown - 1, 0);

        naturalHealingCooldown = Math.max(naturalHealingCooldown - 1, 0);

    }

    private void updateParty(List<PetSlot<PetData>> slotsWithPets) {
        if(partyUpdateCooldown <= 0){
            for(PetSlot<PetData> slot : slotsWithPets){
                PetData petData = slot.getContent();
                if(petData != null)
                    petData.updateVolatilePetInfoIfPossible();
            }
            partyUpdateCooldown = 5;
            onPetPartyModifiedListener.onPetPartyEvent();
        }
    }

    public void tickPets(List<PetSlot<PetData>> slotsWithPets, ServerWorld world){
        for(PetSlot<PetData> slot : slotsWithPets){
            PetData petData = slot.getContent();
            if(petData != null){
                petData.tick(world, player);
            }

        }
    }

    public void applyHealingToSummonedPetsIfPossible(List<PetData> summonedPets){
        if(naturalHealingCooldown == 0) {
            List<PetData> healablePets = summonedPets.stream()
                    .filter((p)-> p.getPetInfo().getCurrentHealth() < p.getPetInfo().getMaxHealth()).toList();

            for(PetData pet : healablePets){
                pet.applyNaturalHealing();
            }

            naturalHealingCooldown = 40;
        }


    }

    public void pushChangesToClient(){
        if(player instanceof ServerPlayerEntity serverPlayer){
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeNbt(this.writeToNbt());
            serverPlayer.networkHandler.sendPacket(new CustomPayloadS2CPacket(S2CPacketHandlers.PARTY_UPDATE, buf));
        }
    }

    public void setOnPartyModifiedListener(PetPartyEventListener listener){
        this.onPetPartyModifiedListener = listener;
    }

    private void triggerOnPartyModifiedEvent(){
        if(this.onPetPartyModifiedListener != null)
            this.onPetPartyModifiedListener.onPetPartyEvent();
    }

    public void readFromNbt(NbtCompound nbt){

        this.slotCount = nbt.getInt(RelipetsConstantsRegistry.PET_SLOT_COUNT_KEY);

        this.slotManager = new PetSlotManager<PetData>(this.slotCount, PetData::new);

        this.slotManager.readFromNbt(nbt.getCompound(RelipetsConstantsRegistry.PET_SLOT_MANAGER_KEY));

        this.selectedPetIndex = nbt.getInt(SELECTED_PET_INDEX);

        this.petGroupManager = new PetGroupManager(nbt.getCompound(PET_GROUP_MANAGER));
    }

    public NbtCompound writeToNbt(){
        NbtCompound nbt = new NbtCompound();

        NbtCompound slotManagerNbt = slotManager.writeToNbt();

        if(this.slotCount < baseSlotCount) this.slotCount = baseSlotCount;

        nbt.putInt(RelipetsConstantsRegistry.PET_SLOT_COUNT_KEY, this.slotCount);
        nbt.put(RelipetsConstantsRegistry.PET_SLOT_MANAGER_KEY, slotManagerNbt);
        nbt.putInt(SELECTED_PET_INDEX, this.selectedPetIndex);

        if(this.petGroupManager != null)
            nbt.put(PET_GROUP_MANAGER, this.petGroupManager.writeToNbt());

        return nbt;
    }

    @Nullable
    public PetData getSelectedPet(){

        return this.getSlotManager().getSlotAt(this.selectedPetIndex).getContent();
    }

    public void cyclePetSlot(int direction){
        //-1 -> scroll down (should go to the right)
        //1 -> scroll up (should go to the left)

        this.selectedPetIndex -= direction;
        this.selectedPetIndex = Math.min(this.selectedPetIndex, this.getSlotManager().getSlotCount()-1);
        this.selectedPetIndex = Math.max(this.selectedPetIndex, 0);

        Relipets.LOGGER.debug("Selected pet index: " + this.selectedPetIndex);

        if(this.getSelectedPet() != null && this.getSelectedPet().isSummoned()){
            this.getSelectedPet().addHighlight();
        }
        this.onPetPartyModifiedListener.onPetPartyEvent();
    }

    public void toggleSummonSelectedPet(ServerWorld world, Vec3d pos, PlayerEntity player){


        PetData selectedPet = this.getSelectedPet();

        if(selectedPet == null){
            Relipets.LOGGER.debug("There is no pet in the selected slot");
            return;
        }

        boolean operationExecuted = false;

        if(selectedPet.isSummoned()){
            selectedPet.recall(world, player);
            operationExecuted = true;
        }else if (selectedPet.isRecalled()){
            selectedPet.summon(world, pos, player);
            operationExecuted = true;

        }else if(selectedPet.isHealing()){
            Relipets.LOGGER.debug("The selected pet is healing");
            Utils.message(selectedPet.getPetInfo().getPetName() + " is healing. Wait "+ Utils.tickToSecond(selectedPet.getHealingCooldown()) + "s.", player);

        }else{
            Utils.message("Summoned from an unknown state " + selectedPet.getPetInfo().getPetName() + ".", player);
            //create a copy of the pet using the last known state of the pet
            selectedPet.summon(world, pos, player);
        }

        if(operationExecuted){
            player.getItemCooldownManager().set(RelipetsItemRegistry.PETIFICATOR_ITEM.asItem(), 20);
        }

    }

    public void onPetFainted(LivingEntity petEntity, ServerWorld world){
        PetData petData = getPetByEntityUUID(petEntity.getUuidAsString());
        if(petData != null){

            petData.onFaint(petEntity, world, player);
            Relipets.LOGGER.debug("Recalled pet that was about to die");
            Utils.message("Pet " + petData.getPetInfo().getPetName() + " fainted! They are healing now...", player);
        }else{
            Relipets.LOGGER.debug("Could not find pet bound to this entity that fainted");
        }
    }

    @Nullable
    public PetData getPetByEntityUUID(String entityUUID){
        for(PetSlot<PetData> slot : this.getSlotManager().getSlotsWithContent()){
            PetData pet = slot.getContent();
            if(pet != null && pet.getPetEntityData().getEntityUUID().equals(entityUUID)){
                return pet;
            }
        }

        return null;
    }

    public void addPetToParty(LivingEntity entity, PlayerEntity player){

        if(!this.getSlotManager().isFull()){
            PetData newPet = new PetData();
            newPet.fillFromEntity(entity, player);

            if(this.getSlotManager().getSlotAt(this.selectedPetIndex).isEmpty()){
                this.getSlotManager().getSlotAt(this.selectedPetIndex).setContent(newPet);
            }else{
                Relipets.LOGGER.debug("Released pet from party to put another one in place");
                //add a strategy here?
                PetData currentPetInSlot = this.getSlotManager().getSlotAt(this.selectedPetIndex).getContent();
                if(currentPetInSlot != null && currentPetInSlot.isRecalled()){
                    currentPetInSlot.summon((ServerWorld) player.getWorld(), entity.getPos(), player);
                }

                this.releasePetFromParty(currentPetInSlot);

                this.getSlotManager().getSlotAt(this.selectedPetIndex).setContent(newPet);
                //this.getSlotManager().getFirstEmptySlot().setContent(newPet);
            }

            Relipets.LOGGER.debug(entity.getDisplayName().getString() + " has been petified!");
            newPet.updateVolatilePetInfoIfPossible();
            newPet.recall((ServerWorld) entity.getWorld(), player);
            Utils.message("Added " + newPet.getPetInfo().getPetName() + " to party!", player);
            triggerOnPartyModifiedEvent();
            this.pushChangesToClient();
        }else{
            Relipets.LOGGER.debug("Can not add this entity to party. All slots are full");
        }

    }

    public void releasePetFromParty(PetData pet){
        int petIndex = -1;
        int currentIndex = 0;
        for(PetSlot<PetData> slot : this.getSlotManager().getSlots()){
            PetData petData = slot.getContent();
            if(petData != null){
                if(petData.getPetEntityData().getEntityUUID().equals(pet.getPetEntityData().getEntityUUID())){
                    petIndex = currentIndex;
                    break;
                }
            }
            currentIndex++;
        }

        if(petIndex == -1){
            Relipets.LOGGER.debug("Could not find pet in the party. Release failed.");
            return;
        }

        PetData petToBeReleased = this.getSlotManager().getSlotAt(petIndex).getContent();
        if(petToBeReleased != null && petToBeReleased.getPetEntityData().getEntity() != null){
            PetMetadataComponent petMetadata = CardinalComponentsRegistry.PET_METADATA_KEY.get(petToBeReleased.getPetEntityData().getEntity());
            petMetadata.clear();
            Utils.message("Released "+ petToBeReleased.getPetInfo().getPetName() + " from party", player);
        }

        this.getSlotManager().getSlotAt(petIndex).clear();
        triggerOnPartyModifiedEvent();
        this.pushChangesToClient();

        Relipets.LOGGER.debug("Pet released");

    }

    public void removeSelectedPetFromParty(ServerWorld world, Vec3d pos, PlayerEntity player){

        PetData selectedPet = this.getSelectedPet();

        if(selectedPet == null){
            Relipets.LOGGER.debug("There is no pet in this slot to remove from party");
            return;
        }

        if(selectedPet.isRecalled()){
            selectedPet.summon(world, pos, player);
        }

        this.getSlotManager().getSlotAt(selectedPetIndex).clear();

        triggerOnPartyModifiedEvent();
    }

    public void reorderPets(int originIndex, int destinationIndex) {
        //get the content at origin
        //get the content at destination
        //place the content from origin in destination
        //place the content from destination in origin

        PetData originData = this.getSlotManager().getSlotAt(originIndex).getContent();

        PetData destinationData = this.getSlotManager().getSlotAt(destinationIndex).getContent();

        this.getSlotManager().getSlotAt(destinationIndex).setContent(originData);
        this.getSlotManager().getSlotAt(originIndex).setContent(destinationData);

        this.onPetPartyModifiedListener.onPetPartyEvent();
        this.pushChangesToClient();
    }

    public void addPetSlot() {
        this.slotCount++;
        this.getSlotManager().addSlot();
        this.onPetPartyModifiedListener.onPetPartyEvent();
        this.pushChangesToClient();
    }

    public void recallAllPets(ServerWorld world, PlayerEntity player) {
        this.getSummonedPets(this.getSlotManager().getSlotsWithContent()).forEach((p)-> p.recall(world, player));
    }

    public void summonGroup(UUID uuid, ServerWorld world, Vec3d pos, PlayerEntity player) {
        PetGroup group = this.getPetGroupManager().getGroupById(uuid);
        if(group != null){
            List<Integer> slots = group.getSlots();
            if(!slots.isEmpty()){
                for(int slot : slots){
                    PetData petData = this.getSlotManager().getSlotAt(slot).getContent();

                    if(petData != null){
                        petData.summon(world, pos, player);
                    }
                }

                Utils.message("Summoned group "+ group.getName(), player);

            }
        }
    }

    public void recallGroup(UUID uuid, ServerWorld world, Vec3d pos, PlayerEntity player) {
        PetGroup group = this.getPetGroupManager().getGroupById(uuid);
        if(group != null){
            List<Integer> slots = group.getSlots();
            if(!slots.isEmpty()){
                for(int slot : slots){
                    PetData petData = this.getSlotManager().getSlotAt(slot).getContent();

                    if(petData != null){
                        petData.recall(world, player);
                    }
                }

                Utils.message("Recalled group "+ group.getName(), player);

            }
        }
    }

    public interface PetPartyEventListener{
        void onPetPartyEvent();
    }



}

/*
Problem: The data in the client is different from the data in the server.

How does data goes from the server to the client?
    Server writes the data to NBT.
    Client reads the data from NBT.

 */

/*
Pet Groups:


        There will be a screen for creating/updating and deleting pet groups.

        The screen will have a Title saying "Pet Groups"

        Then there will be a button for creating a new pet group.

        [+ New Group]

        Once clicked, a new pet group will appear in the list of pet groups. The group will be empty.
        The group will have a name and a color associated to them

        {color} Group 1 [Add Slot] [Remove Slot]                [Delete Group]
        [][][][][][]

        {color} Group 2 [Add Slot] [Remove Slot]                [Delete Group]
        [][]

        On the right side there will be a panel listing all the slots the player currently has and the slots will
        be colored based on which group they belong to.


        Party

            - Groups

                {
                    color: "",
                    name: "",
                    slots: [0, 3, 5],

                }


 */
