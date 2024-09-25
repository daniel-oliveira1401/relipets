package net.daniel.relipets.cca_components;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.daniel.relipets.registries.NBTKeysRegistry;
import net.minecraft.nbt.NbtCompound;

public class PetOwnerComponent implements Component {

    private int myNumber;

    public int getNumber(){
        return this.myNumber;
    }

    public void setNumber(int n){
        this.myNumber = n;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.myNumber = tag.getInt(NBTKeysRegistry.MY_NUMBER);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt(NBTKeysRegistry.MY_NUMBER, this.myNumber);
    }
}
