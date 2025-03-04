package net.daniel.relipets.cca_components.pet_management;

import lombok.Getter;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PetGroupManager {

    @Getter
    private final List<PetGroup> groups = new ArrayList<>();

    public PetGroupManager(NbtCompound nbt){
        if(!nbt.isEmpty()){
            for(String key : nbt.getKeys()){
                this.groups.add(new PetGroup(nbt.getCompound(key)));
            }
        }
    }

    public PetGroupManager(){

    }

    public void addGroup(){
        this.groups.add(PetGroup.empty());
    }

    public void removeGroup(UUID groupId){
        this.groups.removeIf((g) -> g.getId().toString().equals(groupId.toString()));
    }

    @Nullable
    public PetGroup getGroupById(UUID groupId){
        for (PetGroup group : groups){
            if(group.getId().toString().equals(groupId.toString())) return group;
        }

        return null;
    }

    public void addSlotToGroup(UUID groupId, int slotIndex){
        PetGroup group = getGroupById(groupId);

        if(group != null){
            group.addSlot(slotIndex);
        }
    }

    public void removeSlotFromGroup(UUID groupId, int slotIndex){
        PetGroup group = getGroupById(groupId);

        if(group != null){
            group.removeSlot(slotIndex);
        }
    }

    public NbtCompound writeToNbt(){
        NbtCompound nbtCompound = new NbtCompound();
        int i = 0;
        for (PetGroup group : groups){
            nbtCompound.put(String.valueOf(i), group.writeToNbt());
            i++;
        }

        return nbtCompound;
    }

    public List<PetGroup> getSlotGroups(Integer slot){

        return this.groups.stream().filter((g)-> g.getSlots().contains(slot)).toList();

    }

    public void changeGroupColor(UUID uuid, int color) {
        if(this.getGroupById(uuid) != null){
            this.getGroupById(uuid).setColor(color);
        }
    }

    public void changeGroupName(UUID uuid, String name) {
        if(this.getGroupById(uuid) != null){
            this.getGroupById(uuid).setName(name);
        }
    }

    //TODO; add serialization

}
