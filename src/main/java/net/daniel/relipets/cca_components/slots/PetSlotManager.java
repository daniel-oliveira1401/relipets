package net.daniel.relipets.cca_components.slots;

import net.daniel.relipets.cca_components.ISerializable;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PetSlotManager<T extends ISerializable> implements ISerializable {

    private List<PetSlot<T>> slots;

    private Supplier<T> contentSupplier;

    public PetSlotManager(int initialSlotCount, Supplier<T> contentSupplier){

        this.slots = new ArrayList<>();
        this.contentSupplier = contentSupplier;

        for(int i = 0; i < initialSlotCount; i++)
            slots.add(new PetSlot<T>());

    }

    public void clearSlots(){
        for(PetSlot slot : slots)
            slot.clear();
    }

    public void pushSlots(int count){
        for(int i = 0; i < count; i++)
            slots.add(new PetSlot());
    }

    public void pushSlot(){
        slots.add(new PetSlot());
    }

    public void popSlot(){
        slots.remove(this.slots.size()-1);
    }

    public PetSlot<T> getSlotAt(int index){
        return slots.get(index);
    }

    public boolean isFull(){

        for(PetSlot slot : slots){
           if(slot.isEmpty())
               return false;
       }

       return true;
    }

    @Nullable
    public PetSlot<T> getFirstEmptySlot(){

        if(this.isFull())
            return null;

        for(PetSlot slot : slots){
            if(slot.isEmpty())
                return slot;
        }

        return null;

    }

    @Nullable
    public PetSlot<T> getFirstFullSlot(){
        if(this.isFull())
            return this.getSlotAt(0);

        for (PetSlot slot : slots){
            if(!slot.isEmpty())
                return slot;
        }

        return null;

    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        for(String key : nbt.getKeys()){

            PetSlot<T> emptySlot = this.getFirstEmptySlot();

            if(emptySlot != null){

                NbtCompound contentNbt = nbt.getCompound(key);
                T content = this.contentSupplier.get();
                content.readFromNbt(contentNbt);

                emptySlot.setContent(content);
            }

        }
    }

    @Override
    public NbtCompound writeToNbt() {

        NbtCompound nbt = new NbtCompound();

        int slotIndex = 0;
        for(PetSlot<T> slot : slots){
            if(!slot.isEmpty() && slot.getContent() != null){
                nbt.put(""+slotIndex, slot.getContent().writeToNbt());
            }
        }

        return nbt;
    }
}
