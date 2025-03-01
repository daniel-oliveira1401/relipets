package net.daniel.relipets.items;

import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class AddPetSlotItem extends Item {
    public AddPetSlotItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        if(!world.isClient()){

            System.out.println("Used item!!");
            //get party
            PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(user);


            //call add one more slot
            petOwnerComponent.getPetParty().addPetSlot();

            //decrease the amount of items in the stack
            ItemStack pointItem = user.getStackInHand(hand);
            pointItem.decrement(1);

        }

        return super.use(world, user, hand);
    }
}
