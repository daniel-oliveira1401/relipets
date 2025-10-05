package net.daniel.relipets.cca_components.pet_management;

import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.cca_components.ISerializable;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class PetGroup {

    private final UUID id;
    @Setter
    private int color;
    @Setter
    private String name;
    private final List<Integer> slots = new ArrayList<>();

    public PetGroup(NbtCompound nbt) {
        this.id = UUID.fromString(nbt.contains("id")? nbt.getString("id") : UUID.randomUUID().toString());
        this.name = nbt.getString("name");
        this.color = nbt.getInt("color");
        readSlots(nbt.getString("slots"));
    }

    private PetGroup(){
        this.color = 0xff00ff00; // green?
        this.name = "New Group";
        this.id = UUID.randomUUID();
    }

    public static PetGroup empty(){
        return new PetGroup();
    }

    private void readSlots(String slots) {
        if(slots.isEmpty()) return;

        String[] splitSlots = slots.split(",");

        for(String slot : splitSlots){
            this.slots.add(Integer.parseInt(slot));
        }

    }

    private String writeSlots(){
        if(this.slots.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        for(Integer slot : this.slots){
            result.append(slot).append(",");
        }

        return result.substring(0, result.length()-1); //-1 is meant to remove the last comma (,)
    }

    public NbtCompound writeToNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", this.id.toString());
        nbt.putString("name", this.name);
        nbt.putInt("color", this.color);
        nbt.putString("slots", this.writeSlots());

        return nbt;
    }

    public void addSlot(int slotIndex){
        slots.add(slotIndex);
    }

    public void removeSlot(Integer slotIndex){
        slots.remove(slotIndex);
    }


}
