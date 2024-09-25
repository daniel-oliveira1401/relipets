package net.daniel.relipets.items;

import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class TitanWand extends Item {

    public static final String TITAN_UUID_KEY = "ars_titans:titanUUID";
    public String titanType = "";

    public TitanWand(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {

        if(!user.getWorld().isClient()){
//            if(entity instanceof Titan titan){
//                titan.toggleTitanSit();
//                return ActionResult.CONSUME;
//            }
            int number = CardinalComponentsRegistry.PET_OWNER_KEY.get(user).getNumber();

            System.out.println("Number is " + number);

            CardinalComponentsRegistry.PET_OWNER_KEY.get(user).setNumber((int)(Math.random() * 10));

            number = CardinalComponentsRegistry.PET_OWNER_KEY.get(user).getNumber();

            System.out.println("Set number to " + number);

        }

        return ActionResult.FAIL;
    }
}
