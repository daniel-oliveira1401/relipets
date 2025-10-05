package net.daniel.relipets.items;

import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.registries.RelipetsItemRegistry;
import net.minecraft.item.ItemStack;

public class PartItemFactory {

    public static ItemStack createStackByType(String type){
        ItemStack stack;

        switch (type){
            case PetPart.ARM_PART -> stack = new ItemStack(RelipetsItemRegistry.ARM_PART_ITEM);
            case PetPart.LEG_PART -> stack = new ItemStack(RelipetsItemRegistry.LEG_PART_ITEM);
            case PetPart.WING_PART -> stack = new ItemStack(RelipetsItemRegistry.WING_PART_ITEM);
            case PetPart.HEAD_PART -> stack = new ItemStack(RelipetsItemRegistry.HEAD_PART_ITEM);
            case PetPart.TORSO_PART -> stack = new ItemStack(RelipetsItemRegistry.TORSO_PART_ITEM);
            case PetPart.TAIL_PART -> stack = new ItemStack(RelipetsItemRegistry.TAIL_PART_ITEM);

            default -> throw new IllegalArgumentException("Type " + type +" is not a valid pet part type.");
        }

        return stack;
    }

}
