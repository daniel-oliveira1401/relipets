package net.daniel.relipets.items;

import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class AddPetSlotItem extends Item {
    public AddPetSlotItem(net.minecraft.item.Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        if(!world.isClient()){

            //get party
            PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(user);


            //call add one more slot
            petOwnerComponent.getPetParty().addPetSlot();

            //decrease the amount of items in the stack
            ItemStack pointItem = user.getStackInHand(hand);
            pointItem.decrement(1);

            Utils.message("+ 1 slot. Total slot count: " + petOwnerComponent.getPetParty().getSlotManager().getSlotCount(), user);

        }

        return super.use(world, user, hand);
    }
}
