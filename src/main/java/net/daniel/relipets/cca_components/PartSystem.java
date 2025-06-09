package net.daniel.relipets.cca_components;

import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.cca_components.pet_management.PetInventoryManager;
import net.daniel.relipets.entity.cores.abilities.CoreAbility;
import net.daniel.relipets.items.PartItem;
import net.daniel.relipets.items.PartItemFactory;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

@Setter
@Getter
public class PartSystem {

    private HashMap<String, PetPart> parts;

    private static final String INVENTORY = "inventory";

    public static final String[] availableParts = {
            PetPart.HEAD_PART,
            PetPart.TORSO_PART,
            PetPart.WING_PART,
            PetPart.ARM_PART,
            PetPart.TAIL_PART,
            PetPart.LEG_PART,
    };

    @Getter
    private PetInventoryManager petInventoryManager = new PetInventoryManager();

    public PartSystem(NbtCompound nbt){
        this();
        this.readFromNbt(nbt);
    }

    public PartSystem(){
        parts = (HashMap<String, PetPart>) PetPart.EMPTY_PARTS.clone();
    }

    public void updateSystemBasedOnSlotIndex(int index){

        //get the part that corresponds to the index
        PetInventoryManager.PetInventorySlot petInventorySlot = this.getPetInventoryManager().getInventorySlotByIndex(index);
        String partType = petInventorySlot.getData().getCompatiblePart();
        //get the item thats in that index
        ItemStack item = this.getPetInventoryManager().getStack(index);
        //update the system accordingly ( If there isn't an item,
        // remove that part from the system if there is a part)

        // if there is an item, add that part to the system.
        if(!item.isEmpty()){
            NbtCompound itemTag = item.getOrCreateNbt().getCompound(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY);

            PetPart partFromItem = PetPart.readFromNbt(itemTag);

            this.addOrUpdatePart(partFromItem);
        }else{
            this.removePartByType(partType);
        }

    }

    private void removePartByType(String partType) {
        this.parts.put(partType, PetPart.EMPTY_PARTS.get(partType));
    }

    public void applyInventoryChange() {

        //what this function actually does:
        // it looks at the current state of the inventory and updates the related system accordingly
        //right now, that consists of basically:
        //  getting the items that are currently in the pet inventory and assigning the corresponding
        //  parts to the pet system.

        //additional stuff that it needs to do:
        //  it also needs to update the pet inventory data system
        for(int i = 0; i < 6; i++){

            //add a validation to check if there is a part in the system for that slot but there isnt an item.
            //  If so, then create the item using the part nbt data
            String partType = this.getPetInventoryManager().getInventorySlots().get(i).getData().getCompatiblePart();
            PetPart existingPart = this.getPartByType(partType);
            ItemStack stack = this.getPetInventoryManager().getStack(i);

            //in theory this should never happen, but when migrating from old part system to the new one, this will happen.
            //So in most cases, this should be a one time thing
            if(existingPart != null && existingPart.isValid() && stack.isEmpty()){
                //has this part in the part system but there isnt an item for it

                //create the item
                ItemStack itemCreated = PartItemFactory.createStackByType(existingPart.getPartType());
                itemCreated.getOrCreateNbt().put(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY, existingPart.writeToNbt());
                this.getPetInventoryManager().setStack(i, itemCreated);
            }


            stack = this.getPetInventoryManager().getStack(i);
            //update the part system
            if(stack.getItem() instanceof PartItem){
                NbtCompound itemTag = stack.getOrCreateNbt().getCompound(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY);

                PetPart partInHand = PetPart.readFromNbt(itemTag);
                this.addOrUpdatePart(partInHand);
            }
        }
    }


    public void readFromNbt(NbtCompound tag) {

        for(String key : tag.getKeys()){
            PetPart part = PetPart.readFromNbt(tag.getCompound(key));
            this.parts.put(part.getPartType(), part);
        }

        this.petInventoryManager = new PetInventoryManager(tag.getCompound(INVENTORY));

    }

    public NbtCompound writeToNbt(NbtCompound tag) {

        for(String partType : this.parts.keySet()){
            tag.put(partType + "_part_key", this.parts.get(partType).writeToNbt());
        }

        tag.put(INVENTORY, this.petInventoryManager.writeToNbt());

        return tag;
    }

    public void addOrUpdatePart(PetPart part){
        this.parts.put(part.partType, part);
    }

    @Nullable
    public PetPart getPartByType(String partType){
        return this.parts.get(partType);
    }

    public boolean hasValidPart(String partType){
        PetPart part = this.getPartByType(partType);

        return part != null && part.isValid();
    }

    public List<CoreAbility> getAllAbilities(){
        List<PetPart> parts = this.parts.values().stream().toList();

        return parts.stream().map(PetPart::getSignatureAbility).filter(Objects::nonNull).toList();
    }
}
