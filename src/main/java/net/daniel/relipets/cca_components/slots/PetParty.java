package net.daniel.relipets.cca_components.slots;

import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class PetParty implements ISerializable {

    private int slotCount = 5;

    int selectedPetIndex = 0;

    private PetSlotManager<PetData> slotManager = new PetSlotManager<>(slotCount, PetData::new);

    public void readFromNbt(NbtCompound nbt){

        //TODO: add deserialization code here
        this.slotCount = nbt.getInt(RelipetsConstantsRegistry.PET_SLOT_COUNT_KEY);

        this.slotManager = new PetSlotManager<PetData>(this.slotCount, PetData::new);

        this.slotManager.readFromNbt(nbt.getCompound(RelipetsConstantsRegistry.PET_SLOT_MANAGER_KEY));
    }

    public NbtCompound writeToNbt(){
        NbtCompound nbt = new NbtCompound();

        NbtCompound slotManagerNbt = slotManager.writeToNbt();

        nbt.putInt(RelipetsConstantsRegistry.PET_SLOT_COUNT_KEY, this.slotCount);
        nbt.put(RelipetsConstantsRegistry.PET_SLOT_MANAGER_KEY, slotManagerNbt);

        return nbt;
    }

    public PetSlotManager<PetData> getSlotManager(){
        return this.slotManager;
    }

    @Nullable
    public PetData getSelectedPet(){

        return this.getSlotManager().getSlotAt(this.selectedPetIndex).getContent();
    }

    public void toggleSummonSelectedPet(ServerWorld world, Vec3d pos){
        PetData selectedPet = this.getSelectedPet();

        if(selectedPet == null){
            System.out.println("There is no pet in the selected slot");
            return;
        }

        if(selectedPet.isSummoned()){
            selectedPet.recall();
        }else if (selectedPet.isRecalled()){
            selectedPet.summon(world, pos);
        }else{
            System.out.println("The selected pet is no in a good condition right now.");
        }

    }

    public void addPetToParty(LivingEntity entity){

        if(!this.getSlotManager().isFull()){
            PetData newPet = new PetData();
            newPet.fillFromEntity(entity);

            this.getSlotManager().getFirstEmptySlot().setContent(newPet);

        }else{
            System.out.println("Can not add this entity to party. All slots are full");
        }

    }

    public void removeSelectedPetFromParty(ServerWorld world, Vec3d pos){

        PetData selectedPet = this.getSelectedPet();

        if(selectedPet == null){
            System.out.println("There is no pet in this slot to remove from party");
            return;
        }

        if(selectedPet.isRecalled()){
            selectedPet.summon(world, pos);
        }

        this.getSlotManager().getSlotAt(selectedPetIndex).clear();

    }

}
