package net.daniel.relipets.cca_components.pet_management;

import net.daniel.relipets.cca_components.ISerializable;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
        slots.add(new PetSlot<T>());
    }

    public void popSlot(){
        slots.remove(this.slots.size()-1);
    }

    public List<PetSlot<T>> getSlotsWithContent(){

        List<PetSlot<T>> slotsWithContent = new ArrayList<>();

        for(PetSlot<T> slot : slots){
            if(!slot.isEmpty())
                slotsWithContent.add(slot);
        }

        return slotsWithContent;
    }

    public PetSlot<T> getSlotAt(int index){
        return slots.get(index);
    }

    public boolean isFull(){

        for(PetSlot<T> slot : slots){
           if(slot.isEmpty())
               return false;
       }

       return true;
    }

    public int getSlotCount(){
        return this.slots.size();
    }

    public List<PetSlot<T>> getSlots(){
        return this.slots.stream().collect(Collectors.toUnmodifiableList());
    }

    @Nullable
    public PetSlot<T> getFirstEmptySlot(){

        if(this.isFull())
            return null;

        for(PetSlot<T> slot : slots){
            if(slot.isEmpty())
                return slot;
        }

        return null;

    }

    @Nullable
    public PetSlot<T> getFirstFullSlot(){
        if(this.isFull())
            return this.getSlotAt(0);

        for (PetSlot<T> slot : slots){
            if(!slot.isEmpty())
                return slot;
        }

        return null;

    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        int i = 0;
        for(String key : nbt.getKeys()){

            PetSlot<T> slot = this.getSlotAt(i);

            if(slot != null){

                NbtCompound contentNbt = nbt.getCompound(key);
                if(contentNbt.isEmpty()){
                    slot.clear();
                }else{
                    T content = this.contentSupplier.get();
                    content.readFromNbt(contentNbt);
                    slot.setContent(content);
                }
            }
            i++;
        }
    }

    @Override
    public NbtCompound writeToNbt() {

        NbtCompound nbt = new NbtCompound();

        int slotIndex = 0;
        for(PetSlot<T> slot : slots){
            if(!slot.isEmpty() && slot.getContent() != null){
                nbt.put(""+slotIndex, slot.getContent().writeToNbt());
            }else{
                nbt.put(""+slotIndex, new NbtCompound());
            }
            slotIndex++;
        }

        return nbt;
    }
}
