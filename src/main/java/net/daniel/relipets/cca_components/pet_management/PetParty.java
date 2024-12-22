package net.daniel.relipets.cca_components.pet_management;

import lombok.Getter;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.daniel.relipets.registries.RelipetsItemRegistry;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PetParty implements ISerializable {

    public static final String SELECTED_PET_INDEX = "selected_pet_index";

    private int slotCount = 5;

    @Getter
    int selectedPetIndex = 0;

    @Getter
    private PetSlotManager<PetData> slotManager = new PetSlotManager<>(slotCount, PetData::new);

    PetPartyEventListener onPetPartyModifiedListener;

    private PlayerEntity player;

    public PetParty(PlayerEntity player){
        this.player = player;
    }
    int partyUpdateCooldown = 0;
    int petSummonCooldown = 0;
    int naturalHealingCooldown = 0;
    public void tick(ServerWorld world){

        List<PetSlot<PetData>> slotsWithPets = this.getSlotManager().getSlotsWithContent();

        for(PetSlot<PetData> slot : slotsWithPets){
            PetData petData = slot.getContent();
            if(petData != null){
                petData.tick(world, player);
            }

        }

        if(partyUpdateCooldown <= 0){
            for(PetSlot<PetData> slot : slotsWithPets){
                PetData petData = slot.getContent();
                if(petData != null)
                    petData.updateVolatilePetInfoIfPossible();
            }
            partyUpdateCooldown = 5;
            onPetPartyModifiedListener.onPetPartyEvent(this);
        }

        partyUpdateCooldown = Math.max(partyUpdateCooldown - 1, 0);

        petSummonCooldown = Math.max(petSummonCooldown - 1, 0);

        naturalHealingCooldown = Math.max(naturalHealingCooldown - 1, 0);

        if(naturalHealingCooldown == 0){
            applyHealingToSummonedPetsIfPossible();
            naturalHealingCooldown = 40;
        }

    }

    public void applyHealingToSummonedPetsIfPossible(){
        List<PetSlot<PetData>> healablePets = this.getSlotManager()
                .getSlotsWithContent().stream()
                .filter((s)-> s.getContent() != null &&
                        s.getContent().isSummoned() &&
                        s.getContent().getPetInfo().getCurrentHealth() < s.getContent().getPetInfo().getMaxHealth())
                .toList();

        for(PetSlot<PetData> pet : healablePets){
            PetData petData = pet.getContent();

            if(petData != null) //this is redundant but the IDE screams at me if i dont do this
                petData.applyNaturalHealing();
        }

    }

    public void setOnPartyModifiedListener(PetPartyEventListener listener){
        this.onPetPartyModifiedListener = listener;
    }

    private void triggerOnPartyModifiedEvent(){
        if(this.onPetPartyModifiedListener != null)
            this.onPetPartyModifiedListener.onPetPartyEvent(this);
    }

    public void readFromNbt(NbtCompound nbt){

        this.slotCount = nbt.getInt(RelipetsConstantsRegistry.PET_SLOT_COUNT_KEY);

        this.slotManager = new PetSlotManager<PetData>(this.slotCount, PetData::new);

        this.slotManager.readFromNbt(nbt.getCompound(RelipetsConstantsRegistry.PET_SLOT_MANAGER_KEY));

        this.selectedPetIndex = nbt.getInt(SELECTED_PET_INDEX);
    }

    public NbtCompound writeToNbt(){
        NbtCompound nbt = new NbtCompound();

        NbtCompound slotManagerNbt = slotManager.writeToNbt();

        nbt.putInt(RelipetsConstantsRegistry.PET_SLOT_COUNT_KEY, this.slotCount);
        nbt.put(RelipetsConstantsRegistry.PET_SLOT_MANAGER_KEY, slotManagerNbt);
        nbt.putInt(SELECTED_PET_INDEX, this.selectedPetIndex);

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
        this.onPetPartyModifiedListener.onPetPartyEvent(this);
    }

    public void toggleSummonSelectedPet(ServerWorld world, Vec3d pos, PlayerEntity player){
        if(petSummonCooldown > 0){
            Relipets.LOGGER.debug("Pet summon is on cooldown");
            return;
        }

        PetData selectedPet = this.getSelectedPet();

        if(selectedPet == null){
            Relipets.LOGGER.debug("There is no pet in the selected slot");
            return;
        }

        boolean operationExecuted = false;

        if(selectedPet.isSummoned()){
            selectedPet.recall(world);
            operationExecuted = true;
            Utils.message("Recalled " + selectedPet.getPetInfo().getPetName() + ".", player);
        }else if (selectedPet.isRecalled()){
            selectedPet.summon(world, pos, player);
            operationExecuted = true;
            Utils.message("Summoned " + selectedPet.getPetInfo().getPetName() + ".", player);

        }else{
            Relipets.LOGGER.debug("The selected pet is healing");
            Utils.message(selectedPet.getPetInfo().getPetName() + " is healing. Wait "+ Utils.tickToSecond(selectedPet.getHealingCooldown()) + "s.", player);

        }

        if(operationExecuted){
            player.getItemCooldownManager().set(RelipetsItemRegistry.PETIFICATOR_ITEM.asItem(), 60);
            petSummonCooldown = 20;
        }else{
            petSummonCooldown = 10;
        }

        triggerOnPartyModifiedEvent();

    }

    public void onPetFainted(LivingEntity petEntity, ServerWorld world){
        PetData petData = getPetByEntityUUID(petEntity.getUuidAsString());
        if(petData != null){

            petData.onFaint(petEntity, world);
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
            newPet.recall((ServerWorld) entity.getWorld());
            Utils.message("Added " + newPet.getPetInfo().getPetName() + " to party!", player);
            triggerOnPartyModifiedEvent();
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

    public interface PetPartyEventListener{
        void onPetPartyEvent(PetParty party);
    }



}
