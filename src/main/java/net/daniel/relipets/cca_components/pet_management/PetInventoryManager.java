package net.daniel.relipets.cca_components.pet_management;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.cca_components.PartSystem;
import net.daniel.relipets.utils.Utils;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Getter
public class PetInventoryManager extends SimpleInventory {

    @AllArgsConstructor
    @Getter
    @Setter
    public static class PetInventorySlot implements ISerializable {
        int slotIndex;
        PetInventorySlotData data;

        public PetInventorySlot(NbtCompound slotData){
            this.readFromNbt(slotData);
        }

        @Override
        public void readFromNbt(NbtCompound nbt) {
            this.slotIndex = nbt.getInt("slotIndex");
            this.data = new PetInventorySlotData(nbt.getCompound("data"));
        }

        @Override
        public NbtCompound writeToNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putInt("slotIndex", this.slotIndex);
            nbt.put("data", this.data.writeToNbt());

            return nbt;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PetInventorySlotData implements ISerializable{
        String name;
        String compatiblePart;

        public PetInventorySlotData(NbtCompound data){
            this.readFromNbt(data);
        }

        @Override
        public void readFromNbt(NbtCompound nbt) {
            this.name = nbt.getString("name");
            this.compatiblePart = nbt.getString("part");
        }

        @Override
        public NbtCompound writeToNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("name", this.name);
            nbt.putString("part", this.compatiblePart);
            return nbt;
        }
    }
    List<PetInventorySlot> inventorySlots;

    public PetInventoryManager(){
        super(6);
        this.inventorySlots = new ArrayList<>(6);
    }
    public PetInventoryManager(NbtCompound nbt){
        this();
        this.readFromNbt(nbt);
    }

    public PetInventoryManager.PetInventorySlot getInventorySlotByIndex(int index){
        return this.inventorySlots.get(index);
    }

    public void readFromNbt(NbtCompound nbt){
        //if the nbt is empty, then we should initialize this class using empty values
        if(nbt.isEmpty()){
            for(int i = 0; i < 6; i++){
                this.inventorySlots.add(new PetInventorySlot(i, new PetInventorySlotData("", PartSystem.availableParts[i])));
                this.setStack(i, ItemStack.EMPTY);
            }
        }else{
            for(String key : nbt.getKeys()){
                NbtCompound inventorySlotNbt = nbt.getCompound(key);

                try{
                    ItemStack item = ItemStack.fromNbt(inventorySlotNbt.getCompound("item"));
                    PetInventorySlot slot = new PetInventorySlot(inventorySlotNbt.getCompound("inventorySlot"));
                    int index = Integer.parseInt(key);
                    slot.setSlotIndex(index);
                    slot.getData().setCompatiblePart(PartSystem.availableParts[index]);
                    this.setStack(slot.getSlotIndex(), item);
                    this.inventorySlots.add(slot);
                }catch (Exception e){
                    Utils.log("Could not read inventory");
                }
            }
        }


    }

    public NbtCompound writeToNbt(){
        NbtCompound nbt = new NbtCompound();

        for (int i = 0; i < this.size(); i++) {
            NbtCompound inventorySlotNbt = new NbtCompound();
            PetInventorySlot slot = this.inventorySlots.get(i);

            ItemStack itemStack = this.getStack(slot.getSlotIndex());

            inventorySlotNbt.put("item", itemStack.writeNbt(new NbtCompound()));
            inventorySlotNbt.put("inventorySlot", slot.writeToNbt());

            nbt.put(i+"", inventorySlotNbt);

        }

        return nbt;
    }
}
