package net.daniel.relipets.cca_components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.daniel.relipets.cca_components.slots.PetParty;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.minecraft.nbt.NbtCompound;

public class PetOwnerComponent implements Component, AutoSyncedComponent {

    /*

    TODO:

        - Handle when pet "dies"

        - Handle slot selection

        - Handle displaying slots

        - Handle releasing pets

     */

    PetParty petParty = new PetParty();

    @Override
    public void readFromNbt(NbtCompound tag) {

        this.petParty = new PetParty();

        if(tag.contains(RelipetsConstantsRegistry.PET_PARTY_KEY)){
            this.petParty.readFromNbt(tag.getCompound(RelipetsConstantsRegistry.PET_PARTY_KEY));
        }

    }

    @Override
    public void writeToNbt(NbtCompound tag) {

        if(this.petParty != null){
            NbtCompound petParty = this.petParty.writeToNbt();

            tag.put(RelipetsConstantsRegistry.PET_PARTY_KEY, petParty);

        }

    }

    public PetParty getPetParty(){
        return  this.petParty;
    }
}

/*

Goal here:

Be able to petify entities.

Petification consists in:

    - Being able to summon and unsummon the entity


 */
